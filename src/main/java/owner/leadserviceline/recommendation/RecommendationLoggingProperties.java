package owner.leadserviceline.recommendation;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "lead-service-line")
public record RecommendationLoggingProperties(
		boolean recommendationLogEnabled,
		String recommendationLogPath,
		String recommendationImpressionLogPath,
		int recommendationLogRetentionDays,
		String recommendationEventSecret
) {

	public RecommendationLoggingProperties {
		if (recommendationLogRetentionDays <= 0) {
			recommendationLogRetentionDays = 30;
		}
		recommendationEventSecret = recommendationEventSecret == null ? "" : recommendationEventSecret.trim();
	}

	public boolean eventProtectionEnabled() {
		return recommendationLogEnabled && recommendationEventSecret != null && !recommendationEventSecret.isBlank();
	}
}
