package owner.leadserviceline.lookup;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Optional;
import java.util.stream.Stream;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import owner.leadserviceline.data.LeadServiceLineProperties;
import org.springframework.stereotype.Service;

@Service
public class CensusAddressGeocoder implements AddressGeocoder {

	private final ObjectMapper objectMapper;
	private final HttpClient httpClient;
	private final LeadServiceLineProperties properties;

	public CensusAddressGeocoder(ObjectMapper objectMapper, LeadServiceLineProperties properties) {
		this.objectMapper = objectMapper;
		this.properties = properties;
		this.httpClient = HttpClient.newBuilder()
				.connectTimeout(Duration.ofSeconds(4))
				.followRedirects(HttpClient.Redirect.NORMAL)
				.build();
	}

	@Override
	public Optional<GeocodedAddress> geocode(String query, String city, String state) {
		if (!properties.censusGeocoderEnabled() || !looksLikeStreetAddress(query)) {
			return Optional.empty();
		}

		var oneLineAddress = Stream.of(query, city, state)
				.filter(part -> part != null && !part.isBlank())
				.reduce((left, right) -> left + ", " + right)
				.orElse(query);

		try {
			var url = properties.censusGeocoderBaseUrl()
					+ "/geocoder/geographies/onelineaddress?address="
					+ URLEncoder.encode(oneLineAddress, StandardCharsets.UTF_8)
					+ "&benchmark=" + URLEncoder.encode(properties.censusGeocoderBenchmark(), StandardCharsets.UTF_8)
					+ "&vintage=" + URLEncoder.encode(properties.censusGeocoderVintage(), StandardCharsets.UTF_8)
					+ "&format=json";
			var request = HttpRequest.newBuilder(URI.create(url))
					.header("User-Agent", "LeadServiceLineVerdict/1.0")
					.timeout(Duration.ofSeconds(6))
					.GET()
					.build();
			var response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
			if (response.statusCode() < 200 || response.statusCode() >= 300) {
				return Optional.empty();
			}

			var root = objectMapper.readTree(response.body());
			var firstMatch = root.path("result").path("addressMatches");
			if (!firstMatch.isArray() || firstMatch.isEmpty()) {
				return Optional.empty();
			}
			return parseMatch(firstMatch.get(0));
		} catch (IOException | InterruptedException exception) {
			if (exception instanceof InterruptedException) {
				Thread.currentThread().interrupt();
			}
			return Optional.empty();
		}
	}

	private Optional<GeocodedAddress> parseMatch(JsonNode node) {
		var addressComponents = node.path("addressComponents");
		var matchedAddress = text(node, "matchedAddress");
		var city = text(addressComponents, "city");
		var state = text(addressComponents, "state");
		var zipCode = text(addressComponents, "zip");
		if (matchedAddress.isBlank() || city.isBlank() || state.isBlank()) {
			return Optional.empty();
		}
		return Optional.of(new GeocodedAddress(matchedAddress, city, state, zipCode));
	}

	private String text(JsonNode node, String field) {
		var value = node.path(field);
		return value.isMissingNode() || value.isNull() ? "" : value.asText("");
	}

	private boolean looksLikeStreetAddress(String query) {
		return query != null
				&& query.matches(".*\\d+.*")
				&& query.matches(".*[A-Za-z].*");
	}
}
