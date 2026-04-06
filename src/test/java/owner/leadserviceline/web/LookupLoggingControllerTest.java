package owner.leadserviceline.web;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
		"lead-service-line.census-geocoder-enabled=false",
		"lead-service-line.ops-review-enabled=true",
		"lead-service-line.ops-review-token=test-token"
})
@AutoConfigureMockMvc
class LookupLoggingControllerTest {

	@TempDir
	static Path tempDir;

	@Autowired
	private MockMvc mockMvc;

	@DynamicPropertySource
	static void lookupLogProperties(DynamicPropertyRegistry registry) {
		registry.add("lead-service-line.lookup-log-enabled", () -> true);
		registry.add("lead-service-line.lookup-log-path", () -> tempDir.resolve("lookup-events.jsonl").toString());
		registry.add("lead-service-line.lookup-log-retention-days", () -> 14);
	}

	@Test
	void lookupRequestAppendsCompactJsonlEvent() throws Exception {
		Files.deleteIfExists(tempDir.resolve("lookup-events.jsonl"));

		mockMvc.perform(post("/lookup")
						.param("query", "123 Main St")
						.param("city", "Milwaukee")
						.param("state", "WI"))
				.andExpect(status().isOk())
				.andExpect(header().string("Cache-Control", Matchers.containsString("no-store")));

		var logPath = tempDir.resolve("lookup-events.jsonl");
		assertTrue(Files.exists(logPath));
		var lines = Files.readAllLines(logPath);
		assertEquals(1, lines.size());
		assertTrue(lines.get(0).contains("\"lookupLabel\":\"Milwaukee, WI\""));
		assertTrue(lines.get(0).contains("\"lookupInputType\":\"city-state\""));
		assertTrue(lines.get(0).contains("\"confidenceLabel\""));
		assertTrue(lines.get(0).contains("\"ambiguous\":false"));
		assertTrue(lines.get(0).contains("\"geocoderUsed\":false"));
		assertFalse(lines.get(0).contains("\"query\""));
		assertFalse(lines.get(0).contains("\"city\""));
		assertFalse(lines.get(0).contains("\"state\""));
	}

	@Test
	void blankLookupGetDoesNotAppendAnEvent() throws Exception {
		Files.deleteIfExists(tempDir.resolve("lookup-events.jsonl"));

		mockMvc.perform(get("/lookup"))
				.andExpect(status().isOk());

		var logPath = tempDir.resolve("lookup-events.jsonl");
		assertFalse(Files.exists(logPath));
	}

	@Test
	void blankLookupPostDoesNotAppendAnEvent() throws Exception {
		Files.deleteIfExists(tempDir.resolve("lookup-events.jsonl"));

		mockMvc.perform(post("/lookup"))
				.andExpect(status().isOk());

		var logPath = tempDir.resolve("lookup-events.jsonl");
		assertFalse(Files.exists(logPath));
	}

	@Test
	void opsReviewIncludesLookupHotspotGroupsWhenLogFileExists() throws Exception {
		var logPath = tempDir.resolve("lookup-events.jsonl");
		Files.writeString(
				logPath,
				"""
				{"timestamp":"2026-04-04T10:00:00Z","lookupLabel":"Blue Springs, MO","lookupBucketKey":"blue-springs-mo","lookupInputType":"city-state","matchedUtilityId":"blue-springs-water-mo","matchedUtilityName":"City of Blue Springs Water and Sewer Services","matchedUtilityPath":"/lead-service-line/mo/blue-springs/blue-springs-water-services","confidenceLabel":"Likely utility match","ambiguous":true,"geocoderUsed":false}
				{"timestamp":"2026-04-04T10:05:00Z","lookupLabel":"Blue Springs, MO","lookupBucketKey":"blue-springs-mo","lookupInputType":"city-state","matchedUtilityId":"blue-springs-water-mo","matchedUtilityName":"City of Blue Springs Water and Sewer Services","matchedUtilityPath":"/lead-service-line/mo/blue-springs/blue-springs-water-services","confidenceLabel":"Likely utility match","ambiguous":true,"geocoderUsed":false}
				{"timestamp":"2026-04-04T10:10:00Z","lookupLabel":"Unknownville, MO","lookupBucketKey":"unknownville-mo","lookupInputType":"city-state","matchedUtilityId":null,"matchedUtilityName":null,"matchedUtilityPath":null,"confidenceLabel":"No match","ambiguous":false,"geocoderUsed":false}
				""",
				StandardCharsets.UTF_8
		);

		mockMvc.perform(get("/ops/review")
						.header("X-Ops-Token", "test-token"))
				.andExpect(status().isOk())
				.andExpect(content().string(Matchers.containsString("Lookup ambiguity hot spots")))
				.andExpect(content().string(Matchers.containsString("Blue Springs, MO")))
				.andExpect(content().string(Matchers.containsString("No-match lookup requests")))
				.andExpect(content().string(Matchers.containsString("Unknownville, MO")));
	}

	@Test
	void oldLookupEventsArePrunedWhenNewLookupIsLogged() throws Exception {
		var logPath = tempDir.resolve("lookup-events.jsonl");
		Files.writeString(
				logPath,
				"""
				{"timestamp":"2026-03-01T10:00:00Z","lookupLabel":"Old City, OH","lookupBucketKey":"old-city-oh","lookupInputType":"city-state","matchedUtilityId":null,"matchedUtilityName":null,"matchedUtilityPath":null,"confidenceLabel":"No match","ambiguous":false,"geocoderUsed":false}
				""",
				StandardCharsets.UTF_8
		);

		mockMvc.perform(post("/lookup")
						.param("city", "Milwaukee")
						.param("state", "WI"))
				.andExpect(status().isOk());

		var lines = Files.readAllLines(logPath);
		assertEquals(1, lines.size());
		assertTrue(lines.get(0).contains("\"lookupLabel\":\"Milwaukee, WI\""));
		assertFalse(lines.get(0).contains("Old City, OH"));
	}
}
