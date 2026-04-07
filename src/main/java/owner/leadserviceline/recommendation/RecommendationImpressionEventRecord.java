package owner.leadserviceline.recommendation;

public record RecommendationImpressionEventRecord(
		String timestamp,
		String recommendationSlug,
		String guideSlug,
		String pagePath,
		String slot,
		String validationLabel
) {
}
