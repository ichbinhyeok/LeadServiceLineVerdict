package owner.leadserviceline.recommendation;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "lead-service-line")
public record RecommendationLoggingProperties(
		boolean recommendationLogEnabled,
		String recommendationLogPath,
		String recommendationImpressionLogPath,
		int recommendationLogRetentionDays
) {

	public RecommendationLoggingProperties {
		if (recommendationLogRetentionDays <= 0) {
			recommendationLogRetentionDays = 30;
		}
	}
}
