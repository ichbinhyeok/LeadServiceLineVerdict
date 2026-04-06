package owner.leadserviceline.data;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "lead-service-line")
public record LeadServiceLineProperties(
		String dataRoot,
		boolean censusGeocoderEnabled,
		String censusGeocoderBaseUrl,
		String censusGeocoderBenchmark,
		String censusGeocoderVintage
) {
}
