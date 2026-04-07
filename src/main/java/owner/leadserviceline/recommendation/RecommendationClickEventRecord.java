package owner.leadserviceline.recommendation;

public record RecommendationClickEventRecord(
		String timestamp,
		String recommendationSlug,
		String guideSlug,
		String productName,
		String sourcePath,
		String slot,
		String validationLabel,
		String destinationDomain
) {
}
