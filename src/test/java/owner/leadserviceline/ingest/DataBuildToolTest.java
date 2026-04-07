package owner.leadserviceline.ingest;

import java.lang.reflect.Method;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.http.HttpClient;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Comparator;

import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DataBuildToolTest {

	@TempDir
	Path tempDir;

	@Test
	void buildsNormalizedJsonAndRoutesFromRawInputs() throws Exception {
		var dataRoot = tempDir.resolve("data");
		copyDirectory(Path.of("data/raw"), dataRoot.resolve("raw"));

		new DataBuildTool().build(dataRoot);

		assertTrue(Files.exists(dataRoot.resolve("normalized/utilities/dc-water-dc.json")));
		assertTrue(Files.exists(dataRoot.resolve("normalized/utilities/chandler-water-az.json")));
		assertTrue(Files.exists(dataRoot.resolve("normalized/utilities/aurora-water-il.json")));
		assertTrue(Files.exists(dataRoot.resolve("normalized/utilities/bloomington-water-il.json")));
		assertTrue(Files.exists(dataRoot.resolve("normalized/utilities/bloomington-water-in.json")));
		assertTrue(Files.exists(dataRoot.resolve("normalized/utilities/citizens-energy-group-in.json")));
		assertTrue(Files.exists(dataRoot.resolve("normalized/utilities/columbus-water-oh.json")));
		assertTrue(Files.exists(dataRoot.resolve("normalized/utilities/detroit-water-mi.json")));
		assertTrue(Files.exists(dataRoot.resolve("normalized/utilities/duluth-water-mn.json")));
		assertTrue(Files.exists(dataRoot.resolve("normalized/utilities/evanston-water-il.json")));
		assertTrue(Files.exists(dataRoot.resolve("normalized/utilities/evansville-water-in.json")));
		assertTrue(Files.exists(dataRoot.resolve("normalized/utilities/chicago-water-il.json")));
		assertTrue(Files.exists(dataRoot.resolve("normalized/utilities/columbia-water-mo.json")));
		assertTrue(Files.exists(dataRoot.resolve("normalized/utilities/fort-wayne-water-in.json")));
		assertTrue(Files.exists(dataRoot.resolve("normalized/utilities/iowa-american-water-ia.json")));
		assertTrue(Files.exists(dataRoot.resolve("normalized/utilities/independence-water-mo.json")));
		assertTrue(Files.exists(dataRoot.resolve("normalized/utilities/akron-water-oh.json")));
		assertTrue(Files.exists(dataRoot.resolve("normalized/utilities/grand-rapids-water-mi.json")));
		assertTrue(Files.exists(dataRoot.resolve("normalized/utilities/green-bay-water-wi.json")));
		assertTrue(Files.exists(dataRoot.resolve("normalized/utilities/joliet-water-il.json")));
		assertTrue(Files.exists(dataRoot.resolve("normalized/utilities/kalamazoo-water-mi.json")));
		assertTrue(Files.exists(dataRoot.resolve("normalized/utilities/kendallville-water-in.json")));
		assertTrue(Files.exists(dataRoot.resolve("normalized/utilities/lafayette-water-in.json")));
		assertTrue(Files.exists(dataRoot.resolve("normalized/utilities/lansing-bwl-mi.json")));
		assertTrue(Files.exists(dataRoot.resolve("normalized/utilities/missouri-american-water-mo.json")));
		assertTrue(Files.exists(dataRoot.resolve("normalized/utilities/mesa-water-az.json")));
		assertTrue(Files.exists(dataRoot.resolve("normalized/utilities/minneapolis-water-mn.json")));
		assertTrue(Files.exists(dataRoot.resolve("normalized/utilities/mishawaka-water-in.json")));
		assertTrue(Files.exists(dataRoot.resolve("normalized/utilities/monroe-water-mi.json")));
		assertTrue(Files.exists(dataRoot.resolve("normalized/utilities/mud-omaha-ne.json")));
		assertTrue(Files.exists(dataRoot.resolve("normalized/utilities/blue-springs-water-mo.json")));
		assertTrue(Files.exists(dataRoot.resolve("normalized/utilities/grand-island-utilities-ne.json")));
		assertTrue(Files.exists(dataRoot.resolve("normalized/utilities/council-bluffs-water-ia.json")));
		assertTrue(Files.exists(dataRoot.resolve("normalized/utilities/ames-water-ia.json")));
		assertTrue(Files.exists(dataRoot.resolve("normalized/utilities/west-des-moines-water-ia.json")));
		assertTrue(Files.exists(dataRoot.resolve("normalized/utilities/dubuque-water-ia.json")));
		assertTrue(Files.exists(dataRoot.resolve("normalized/utilities/sioux-city-water-ia.json")));
		assertTrue(Files.exists(dataRoot.resolve("normalized/utilities/hastings-water-ne.json")));
		assertTrue(Files.exists(dataRoot.resolve("normalized/utilities/phoenix-water-az.json")));
		assertTrue(Files.exists(dataRoot.resolve("normalized/utilities/racine-water-wi.json")));
		assertTrue(Files.exists(dataRoot.resolve("normalized/utilities/rockford-water-il.json")));
		assertTrue(Files.exists(dataRoot.resolve("normalized/utilities/springfield-water-mo.json")));
		assertTrue(Files.exists(dataRoot.resolve("normalized/utilities/st-joseph-water-mi.json")));
		assertTrue(Files.exists(dataRoot.resolve("normalized/utilities/south-bend-water-in.json")));
		assertTrue(Files.exists(dataRoot.resolve("normalized/utilities/speedway-water-in.json")));
		assertTrue(Files.exists(dataRoot.resolve("normalized/utilities/tempe-water-az.json")));
		assertTrue(Files.exists(dataRoot.resolve("normalized/utilities/toledo-water-oh.json")));
		assertTrue(Files.exists(dataRoot.resolve("normalized/utilities/tucson-water-az.json")));
		assertTrue(Files.exists(dataRoot.resolve("normalized/utilities/saint-paul-regional-water-services-mn.json")));
		assertTrue(Files.exists(dataRoot.resolve("normalized/programs/detroit-neighborhood-replacement.json")));
		assertTrue(Files.exists(dataRoot.resolve("normalized/programs/denver-water-reimbursement.json")));
		assertTrue(Files.exists(dataRoot.resolve("normalized/programs/aurora-lslr-program.json")));
		assertTrue(Files.exists(dataRoot.resolve("normalized/programs/bloomington-il-lslr-project.json")));
		assertTrue(Files.exists(dataRoot.resolve("normalized/programs/citizens-lslr-program.json")));
		assertTrue(Files.exists(dataRoot.resolve("normalized/programs/columbus-lslr-program.json")));
		assertTrue(Files.exists(dataRoot.resolve("normalized/programs/duluth-citywide-replacement.json")));
		assertTrue(Files.exists(dataRoot.resolve("normalized/programs/evanston-homeowner-initiated-lslr.json")));
		assertTrue(Files.exists(dataRoot.resolve("normalized/programs/chicago-lslr-program.json")));
		assertTrue(Files.exists(dataRoot.resolve("normalized/programs/fort-wayne-lslr-program.json")));
		assertTrue(Files.exists(dataRoot.resolve("normalized/programs/grand-rapids-lslr-program.json")));
		assertTrue(Files.exists(dataRoot.resolve("normalized/programs/green-bay-grr-replacement.json")));
		assertTrue(Files.exists(dataRoot.resolve("normalized/programs/iowa-american-water-lslr-program.json")));
		assertTrue(Files.exists(dataRoot.resolve("normalized/programs/kalamazoo-citywide-lslr-program.json")));
		assertTrue(Files.exists(dataRoot.resolve("normalized/programs/kendallville-lslr-program.json")));
		assertTrue(Files.exists(dataRoot.resolve("normalized/programs/lafayette-lslr-program.json")));
		assertTrue(Files.exists(dataRoot.resolve("normalized/programs/missouri-american-water-lslr-program.json")));
		assertTrue(Files.exists(dataRoot.resolve("normalized/programs/minneapolis-citywide-replacement.json")));
		assertTrue(Files.exists(dataRoot.resolve("normalized/programs/phoenix-full-replacement.json")));
		assertTrue(Files.exists(dataRoot.resolve("normalized/programs/racine-private-lead-service-line-replacement.json")));
		assertTrue(Files.exists(dataRoot.resolve("normalized/programs/rockford-lslr-program.json")));
		assertTrue(Files.exists(dataRoot.resolve("normalized/programs/mud-omaha-lslr-program.json")));
		assertTrue(Files.exists(dataRoot.resolve("normalized/programs/grand-island-lslr-project.json")));
		assertTrue(Files.exists(dataRoot.resolve("normalized/programs/council-bluffs-lslr-project.json")));
		assertTrue(Files.exists(dataRoot.resolve("normalized/programs/dubuque-lslr-pilot-program.json")));
		assertTrue(Files.exists(dataRoot.resolve("normalized/programs/hastings-water-main-lslr-program.json")));
		assertTrue(Files.exists(dataRoot.resolve("normalized/programs/st-joseph-lead-service-replacement.json")));
		assertTrue(Files.exists(dataRoot.resolve("normalized/programs/tucson-replacement-program.json")));
		assertTrue(Files.exists(dataRoot.resolve("normalized/programs/toledo-childcare-facility-replacement.json")));
		assertTrue(Files.exists(dataRoot.resolve("normalized/programs/toledo-lead-line-replacement.json")));
		assertTrue(Files.exists(dataRoot.resolve("normalized/programs/lead-free-sprws.json")));
		assertTrue(Files.exists(dataRoot.resolve("normalized/costs/milwaukee-water-works-cost-2026.json")));
		assertTrue(Files.exists(dataRoot.resolve("normalized/costs/aurora-water-il-cost-2026.json")));
		assertTrue(Files.exists(dataRoot.resolve("normalized/costs/bloomington-water-il-cost-2026.json")));
		assertTrue(Files.exists(dataRoot.resolve("normalized/costs/columbus-water-cost-2026.json")));
		assertTrue(Files.exists(dataRoot.resolve("normalized/costs/duluth-water-cost-2026.json")));
		assertTrue(Files.exists(dataRoot.resolve("normalized/costs/evanston-water-cost-2026.json")));
		assertTrue(Files.exists(dataRoot.resolve("normalized/costs/fort-wayne-water-cost-2026.json")));
		assertTrue(Files.exists(dataRoot.resolve("normalized/costs/grand-rapids-water-cost-2026.json")));
		assertTrue(Files.exists(dataRoot.resolve("normalized/costs/iowa-american-water-cost-2026.json")));
		assertTrue(Files.exists(dataRoot.resolve("normalized/costs/joliet-water-cost-2026.json")));
		assertTrue(Files.exists(dataRoot.resolve("normalized/costs/kalamazoo-water-cost-2026.json")));
		assertTrue(Files.exists(dataRoot.resolve("normalized/costs/kendallville-water-cost-2026.json")));
		assertTrue(Files.exists(dataRoot.resolve("normalized/costs/lafayette-water-cost-2026.json")));
		assertTrue(Files.exists(dataRoot.resolve("normalized/costs/missouri-american-water-cost-2026.json")));
		assertTrue(Files.exists(dataRoot.resolve("normalized/costs/phoenix-water-cost-2026.json")));
		assertTrue(Files.exists(dataRoot.resolve("normalized/costs/racine-water-cost-2026.json")));
		assertTrue(Files.exists(dataRoot.resolve("normalized/costs/toledo-water-cost-2026.json")));
		assertTrue(Files.exists(dataRoot.resolve("normalized/costs/mud-omaha-water-cost-2026.json")));
		assertTrue(Files.exists(dataRoot.resolve("normalized/costs/tucson-water-cost-2026.json")));
		assertTrue(Files.exists(dataRoot.resolve("normalized/costs/ames-water-cost-2026.json")));
		assertTrue(Files.exists(dataRoot.resolve("normalized/sources/src-akron-inventory.json")));
		assertTrue(Files.exists(dataRoot.resolve("normalized/sources/src-aurora-program.json")));
		assertTrue(Files.exists(dataRoot.resolve("normalized/sources/src-bloomington-il-cost.json")));
		assertTrue(Files.exists(dataRoot.resolve("normalized/sources/src-bloomington-inventory.json")));
		assertTrue(Files.exists(dataRoot.resolve("normalized/sources/src-citizens-program.json")));
		assertTrue(Files.exists(dataRoot.resolve("normalized/sources/src-milwaukee-owner-request.json")));
		assertTrue(Files.exists(dataRoot.resolve("normalized/sources/src-columbus-cost.json")));
		assertTrue(Files.exists(dataRoot.resolve("normalized/sources/src-detroit-program.json")));
		assertTrue(Files.exists(dataRoot.resolve("normalized/sources/src-evanston-program.json")));
		assertTrue(Files.exists(dataRoot.resolve("normalized/sources/src-evansville-notification.json")));
		assertTrue(Files.exists(dataRoot.resolve("normalized/sources/src-chicago-program.json")));
		assertTrue(Files.exists(dataRoot.resolve("normalized/sources/src-columbia-inventory.json")));
		assertTrue(Files.exists(dataRoot.resolve("normalized/sources/src-fort-wayne-cost.json")));
		assertTrue(Files.exists(dataRoot.resolve("normalized/sources/src-grand-rapids-program.json")));
		assertTrue(Files.exists(dataRoot.resolve("normalized/sources/src-green-bay-program.json")));
		assertTrue(Files.exists(dataRoot.resolve("normalized/sources/src-lansing-bwl-inventory.json")));
		assertTrue(Files.exists(dataRoot.resolve("normalized/sources/src-lafayette-inventory.json")));
		assertTrue(Files.exists(dataRoot.resolve("normalized/sources/src-hamilton-inventory.json")));
		assertTrue(Files.exists(dataRoot.resolve("normalized/sources/src-independence-notification.json")));
		assertTrue(Files.exists(dataRoot.resolve("normalized/sources/src-iowa-amwater-program.json")));
		assertTrue(Files.exists(dataRoot.resolve("normalized/sources/src-joliet-cost.json")));
		assertTrue(Files.exists(dataRoot.resolve("normalized/sources/src-kalamazoo-program.json")));
		assertTrue(Files.exists(dataRoot.resolve("normalized/sources/src-kendallville-program.json")));
		assertTrue(Files.exists(dataRoot.resolve("normalized/sources/src-mud-omaha-program.json")));
		assertTrue(Files.exists(dataRoot.resolve("normalized/sources/src-phoenix-program.json")));
		assertTrue(Files.exists(dataRoot.resolve("normalized/sources/src-providence-program.json")));
		assertTrue(Files.exists(dataRoot.resolve("normalized/sources/src-racine-program.json")));
		assertTrue(Files.exists(dataRoot.resolve("normalized/sources/src-rockford-program.json")));
		assertTrue(Files.exists(dataRoot.resolve("normalized/sources/src-springfield-inventory.json")));
		assertTrue(Files.exists(dataRoot.resolve("normalized/sources/src-south-bend-notification.json")));
		assertTrue(Files.exists(dataRoot.resolve("normalized/sources/src-dubuque-program.json")));
		assertTrue(Files.exists(dataRoot.resolve("normalized/sources/src-hastings-program.json")));
		assertTrue(Files.exists(dataRoot.resolve("normalized/sources/src-sioux-city-inventory.json")));
		assertTrue(Files.exists(dataRoot.resolve("normalized/guides/guide-who-pays-for-lead-service-line-replacement.json")));
		assertTrue(Files.exists(dataRoot.resolve("normalized/recommendations/rec-brita-tahoe-pitcher-elite-filter.json")));
		assertTrue(Files.exists(dataRoot.resolve("normalized/recommendations/rec-tap-score-advanced-city-water.json")));

		var routes = Files.readString(dataRoot.resolve("derived/routes.json"));
		assertTrue(routes.contains("\"path\" : \"/lead-service-line/az/programs\""));
		assertTrue(routes.contains("\"path\" : \"/lead-service-line/az/chandler/chandler-water\""));
		assertTrue(routes.contains("\"path\" : \"/lead-service-line/az/mesa/mesa-water\""));
		assertTrue(routes.contains("\"path\" : \"/lead-service-line/az/phoenix/phoenix-water-services/replacement-cost\""));
		assertTrue(routes.contains("\"path\" : \"/lead-service-line/az/tucson/tucson-water/program\""));
		assertTrue(routes.contains("\"path\" : \"/lead-service-line/dc/washington/dc-water\""));
		assertTrue(routes.contains("\"path\" : \"/lead-service-line/ia/davenport/iowa-american-water/program\""));
		assertTrue(routes.contains("\"path\" : \"/lead-service-line/ia/ames/ames-water-pollution-control/replacement-cost\""));
		assertTrue(routes.contains("\"path\" : \"/lead-service-line/ia/council-bluffs/council-bluffs-water-works/program\""));
		assertTrue(routes.contains("\"path\" : \"/lead-service-line/ia/dubuque/dubuque-water-department/program\""));
		assertTrue(routes.contains("\"path\" : \"/lead-service-line/ia/programs\""));
		assertTrue(routes.contains("\"path\" : \"/lead-service-line/ia/sioux-city/sioux-city-water-plant\""));
		assertTrue(routes.contains("\"path\" : \"/lead-service-line/ia/west-des-moines/west-des-moines-water-works\""));
		assertTrue(routes.contains("\"path\" : \"/lead-service-line/il\""));
		assertTrue(routes.contains("\"path\" : \"/lead-service-line/il/chicago/chicago-water-department/program\""));
		assertTrue(routes.contains("\"path\" : \"/lead-service-line/il/aurora/aurora-water-production-division/program\""));
		assertTrue(routes.contains("\"path\" : \"/lead-service-line/il/bloomington/bloomington-water-department/replacement-cost\""));
		assertTrue(routes.contains("\"path\" : \"/lead-service-line/il/evanston/evanston-water-production-bureau/program\""));
		assertTrue(routes.contains("\"path\" : \"/lead-service-line/il/joliet/joliet-public-utilities/replacement-cost\""));
		assertTrue(routes.contains("\"path\" : \"/lead-service-line/il/programs\""));
		assertTrue(routes.contains("\"path\" : \"/lead-service-line/il/rockford/rockford-water-division/program\""));
		assertTrue(routes.contains("\"path\" : \"/lead-service-line/in\""));
		assertTrue(routes.contains("\"path\" : \"/lead-service-line/in/bloomington/bloomington-utilities\""));
		assertTrue(routes.contains("\"path\" : \"/lead-service-line/in/evansville/evansville-water-and-sewer-utility\""));
		assertTrue(routes.contains("\"path\" : \"/lead-service-line/in/fort-wayne/fort-wayne-city-utilities/replacement-cost\""));
		assertTrue(routes.contains("\"path\" : \"/lead-service-line/in/indianapolis/citizens-energy-group/program\""));
		assertTrue(routes.contains("\"path\" : \"/lead-service-line/in/kendallville/kendallville-water-department/replacement-cost\""));
		assertTrue(routes.contains("\"path\" : \"/lead-service-line/in/lafayette/lafayette-water-works/program\""));
		assertTrue(routes.contains("\"path\" : \"/lead-service-line/in/mishawaka/mishawaka-utilities\""));
		assertTrue(routes.contains("\"path\" : \"/lead-service-line/in/programs\""));
		assertTrue(routes.contains("\"path\" : \"/lead-service-line/in/south-bend/south-bend-water-works\""));
		assertTrue(routes.contains("\"path\" : \"/lead-service-line/in/speedway/speedway-water-works\""));
		assertTrue(routes.contains("\"path\" : \"/lead-service-line/mi/detroit/detroit-water-and-sewerage-department/program\""));
		assertTrue(routes.contains("\"path\" : \"/lead-service-line/mi/grand-rapids/grand-rapids-water-system/program\""));
		assertTrue(routes.contains("\"path\" : \"/lead-service-line/mi/kalamazoo/kalamazoo-water-department/program\""));
		assertTrue(routes.contains("\"path\" : \"/lead-service-line/mi/programs\""));
		assertTrue(routes.contains("\"path\" : \"/lead-service-line/mi/st-joseph/st-joseph-water-and-sewer/program\""));
		assertTrue(routes.contains("\"path\" : \"/lead-service-line/mo/columbia/columbia-water-utility\""));
		assertTrue(routes.contains("\"path\" : \"/lead-service-line/mo/independence/independence-water-department/notification\""));
		assertTrue(routes.contains("\"path\" : \"/lead-service-line/mo/programs\""));
		assertTrue(routes.contains("\"path\" : \"/lead-service-line/mo/springfield/springfield-water-utility\""));
		assertTrue(routes.contains("\"path\" : \"/lead-service-line/mo/st-louis/missouri-american-water/program\""));
		assertTrue(routes.contains("\"path\" : \"/lead-service-line/mo/blue-springs/blue-springs-water-services\""));
		assertTrue(routes.contains("\"path\" : \"/lead-service-line/mn/duluth/duluth-lead-removal-program/program\""));
		assertTrue(routes.contains("\"path\" : \"/lead-service-line/oh/columbus/columbus-division-of-water/program\""));
		assertTrue(routes.contains("\"path\" : \"/lead-service-line/oh/akron/akron-water-supply\""));
		assertTrue(routes.contains("\"path\" : \"/lead-service-line/oh/hamilton/hamilton-utilities-water\""));
		assertTrue(routes.contains("\"path\" : \"/lead-service-line/oh/toledo/toledo-water-distribution/replacement-cost\""));
		assertTrue(routes.contains("\"path\" : \"/lead-service-line/mn/minneapolis/minneapolis-public-works-water/replacement-cost\""));
		assertTrue(routes.contains("\"path\" : \"/lead-service-line/mn/saint-paul/saint-paul-regional-water-services/program\""));
		assertTrue(routes.contains("\"path\" : \"/lead-service-line/pa/programs\""));
		assertTrue(routes.contains("\"path\" : \"/lead-service-line/ne/omaha/mud-omaha/program\""));
		assertTrue(routes.contains("\"path\" : \"/lead-service-line/ne/hastings/hastings-utilities-water/program\""));
		assertTrue(routes.contains("\"path\" : \"/lead-service-line/ne/grand-island/grand-island-utilities-department/program\""));
		assertTrue(routes.contains("\"path\" : \"/lead-service-line/ne/programs\""));
		assertTrue(routes.contains("\"path\" : \"/lead-service-line/wi/racine/racine-water-utility/replacement-cost\""));
		assertTrue(routes.contains("\"path\" : \"/lead-service-line/wi/green-bay/green-bay-water-utility/program\""));
		assertTrue(routes.contains("\"path\" : \"/guides/who-pays-for-lead-service-line-replacement\""));
		assertTrue(routes.contains("\"indexable\" : false"));
		assertEquals(63L, countFiles(dataRoot.resolve("normalized/utilities")));
		assertEquals(49L, countFiles(dataRoot.resolve("normalized/programs")));
		assertEquals(33L, countFiles(dataRoot.resolve("normalized/costs")));
		assertEquals(7L, countFiles(dataRoot.resolve("normalized/recommendations")));
		assertEquals(274L, countOccurrences(routes, "\"path\" : "));
	}

	@Test
	void failsWhenSourceFreshnessIsStale() throws Exception {
		var dataRoot = tempDir.resolve("stale-data");
		copyDirectory(Path.of("data/raw"), dataRoot.resolve("raw"));

		var sourceFile = dataRoot.resolve("raw/sources/01-dc-denver-milwaukee-saint-paul.json");
		var content = Files.readString(sourceFile);
		content = replaceFirst(content, "\"effectiveDate\": \"2026-04-04\"", "\"effectiveDate\": \"2024-01-01\"");
		content = replaceFirst(content, "\"capturedAt\": \"2026-04-04T20:20:00+09:00\"", "\"capturedAt\": \"2024-01-01T20:20:00+09:00\"");
		Files.writeString(sourceFile, content);

		assertThrows(IllegalArgumentException.class, () -> new DataBuildTool().build(dataRoot));
	}

	@Test
	void urlCheckFallsBackToGetWhenHeadReturnsNotReachable() throws Exception {
		var server = HttpServer.create(new InetSocketAddress(0), 0);
		server.createContext("/head404-get200", exchange -> {
			if ("HEAD".equals(exchange.getRequestMethod())) {
				exchange.sendResponseHeaders(404, -1);
			}
			else {
				exchange.sendResponseHeaders(200, 0);
				exchange.getResponseBody().close();
			}
			exchange.close();
		});
		server.start();

		try {
			var checkUrl = DataBuildTool.class.getDeclaredMethod("checkUrl", HttpClient.class, String.class);
			checkUrl.setAccessible(true);
			var client = HttpClient.newBuilder()
					.connectTimeout(Duration.ofSeconds(2))
					.followRedirects(HttpClient.Redirect.NORMAL)
					.build();
			var status = (int) checkUrl.invoke(
					new DataBuildTool(),
					client,
					"http://localhost:" + server.getAddress().getPort() + "/head404-get200"
			);
			assertEquals(200, status);
		}
		finally {
			server.stop(0);
		}
	}

	private String replaceFirst(String source, String target, String replacement) {
		var index = source.indexOf(target);
		if (index < 0) {
			throw new IllegalArgumentException("Target not found in fixture: " + target);
		}
		return source.substring(0, index) + replacement + source.substring(index + target.length());
	}

	private long countFiles(Path directory) throws IOException {
		try (var files = Files.list(directory)) {
			return files.count();
		}
	}

	private long countOccurrences(String content, String token) {
		long count = 0;
		int index = 0;
		while ((index = content.indexOf(token, index)) >= 0) {
			count++;
			index += token.length();
		}
		return count;
	}

	private void copyDirectory(Path source, Path target) throws IOException {
		try (var paths = Files.walk(source)) {
			paths.sorted(Comparator.naturalOrder()).forEach(path -> {
				try {
					var destination = target.resolve(source.relativize(path).toString());
					if (Files.isDirectory(path)) {
						Files.createDirectories(destination);
					} else {
						Files.createDirectories(destination.getParent());
						Files.copy(path, destination);
					}
				} catch (IOException exception) {
					throw new RuntimeException(exception);
				}
			});
		}
	}
}
