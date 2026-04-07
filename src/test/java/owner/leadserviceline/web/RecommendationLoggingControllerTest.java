package owner.leadserviceline.web;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import owner.leadserviceline.recommendation.RecommendationTrackingService;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
		"lead-service-line.census-geocoder-enabled=false",
		"lead-service-line.site-base-url=https://example.test",
		"lead-service-line.recommendation-log-enabled=true",
		"lead-service-line.recommendation-event-secret=test-recommendation-secret"
})
@AutoConfigureMockMvc
class RecommendationLoggingControllerTest {

	@TempDir
	static Path tempDir;

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private RecommendationTrackingService recommendationTracking;

	@DynamicPropertySource
	static void recommendationLogProperties(DynamicPropertyRegistry registry) {
		registry.add("lead-service-line.recommendation-log-path", () -> tempDir.resolve("recommendation-clicks.jsonl").toString());
		registry.add("lead-service-line.recommendation-impression-log-path", () -> tempDir.resolve("recommendation-impressions.jsonl").toString());
		registry.add("lead-service-line.recommendation-log-retention-days", () -> 30);
	}

	@Test
	void signedRecommendationRedirectLogsOnceAndSuppressesImmediateDuplicates() throws Exception {
		var logPath = tempDir.resolve("recommendation-clicks.jsonl");
		Files.deleteIfExists(logPath);
		var redirectPath = recommendationTracking.clickPath(
				"brita-tahoe-pitcher-elite-filter",
				"/guides/best-lead-reduction-filters-after-a-lead-notice",
				"guide-card"
		);

		mockMvc.perform(get(redirectPath)
						.header("Referer", "https://example.test/guides/best-lead-reduction-filters-after-a-lead-notice")
						.header("Sec-Fetch-Site", "same-origin")
						.header(HttpHeaders.USER_AGENT, "JUnit Browser"))
				.andExpect(status().is3xxRedirection())
				.andExpect(header().string("Location", Matchers.containsString("brita.com/products/tahoe-water-pitcher-elite-filter")));

		mockMvc.perform(get(redirectPath)
						.header("Referer", "https://example.test/guides/best-lead-reduction-filters-after-a-lead-notice")
						.header("Sec-Fetch-Site", "same-origin")
						.header(HttpHeaders.USER_AGENT, "JUnit Browser"))
				.andExpect(status().is3xxRedirection());

		assertTrue(Files.exists(logPath));
		var lines = Files.readAllLines(logPath, StandardCharsets.UTF_8);
		assertEquals(1, lines.size());
		assertTrue(lines.get(0).contains("\"sourcePath\":\"/guides/best-lead-reduction-filters-after-a-lead-notice\""));
		assertTrue(lines.get(0).contains("\"slot\":\"guide-card\""));
		assertTrue(lines.get(0).contains("\"validationLabel\":\"signed-referrer-match\""));
	}

	@Test
	void invalidOrCrossSiteRecommendationRedirectStillRedirectsButDoesNotLog() throws Exception {
		var logPath = tempDir.resolve("recommendation-clicks.jsonl");
		Files.deleteIfExists(logPath);
		var redirectPath = recommendationTracking.clickPath(
				"brita-tahoe-pitcher-elite-filter",
				"/guides/best-lead-reduction-filters-after-a-lead-notice",
				"guide-card"
		);

		mockMvc.perform(get(redirectPath.replace("sig=", "sig=bad")))
				.andExpect(status().is3xxRedirection());

		mockMvc.perform(get(redirectPath)
						.header("Referer", "https://attacker.test/fake-guide")
						.header("Sec-Fetch-Site", "cross-site")
						.header(HttpHeaders.USER_AGENT, "JUnit Browser"))
				.andExpect(status().is3xxRedirection());

		assertFalse(Files.exists(logPath));
	}

	@Test
	void signedRecommendationImpressionLogsTrustedImageRequest() throws Exception {
		var logPath = tempDir.resolve("recommendation-impressions.jsonl");
		Files.deleteIfExists(logPath);
		var impressionPath = recommendationTracking.impressionPath(
				"brita-tahoe-pitcher-elite-filter",
				"/guides/best-lead-reduction-filters-after-a-lead-notice",
				"guide-card"
		);

		mockMvc.perform(get(impressionPath)
						.header("Referer", "https://example.test/guides/best-lead-reduction-filters-after-a-lead-notice")
						.header("Sec-Fetch-Site", "same-origin")
						.header("Sec-Fetch-Dest", "image")
						.header(HttpHeaders.USER_AGENT, "JUnit Browser"))
				.andExpect(status().isOk())
				.andExpect(content().contentTypeCompatibleWith(MediaType.IMAGE_GIF));

		assertTrue(Files.exists(logPath));
		var lines = Files.readAllLines(logPath, StandardCharsets.UTF_8);
		assertEquals(1, lines.size());
		assertTrue(lines.get(0).contains("\"pagePath\":\"/guides/best-lead-reduction-filters-after-a-lead-notice\""));
		assertTrue(lines.get(0).contains("\"validationLabel\":\"signed-referrer-match\""));
	}

	@Test
	void invalidRecommendationImpressionDoesNotAppendLog() throws Exception {
		var logPath = tempDir.resolve("recommendation-impressions.jsonl");
		Files.deleteIfExists(logPath);
		var impressionPath = recommendationTracking.impressionPath(
				"brita-tahoe-pitcher-elite-filter",
				"/guides/best-lead-reduction-filters-after-a-lead-notice",
				"guide-card"
		);

		mockMvc.perform(get(impressionPath.replace("sig=", "sig=bad"))
						.header("Referer", "https://example.test/guides/best-lead-reduction-filters-after-a-lead-notice")
						.header("Sec-Fetch-Site", "same-origin")
						.header("Sec-Fetch-Dest", "script")
						.header(HttpHeaders.USER_AGENT, "JUnit Browser"))
				.andExpect(status().isOk());

		assertFalse(Files.exists(logPath));
	}
}
