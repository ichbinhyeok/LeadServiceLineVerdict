package owner.leadserviceline.recommendation;

import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "lead-service-line")
public record RecommendationLinkProperties(
		String amazonAssociateTag,
		Map<String, String> recommendationOverrideUrls
) {

	public RecommendationLinkProperties {
		recommendationOverrideUrls = recommendationOverrideUrls == null ? Map.of() : Map.copyOf(recommendationOverrideUrls);
	}
}
