package owner.leadserviceline.recommendation;

public record RecommendationEventVerification(
		boolean trusted,
		String trackedPath,
		String slot,
		String validationLabel
) {

	public static RecommendationEventVerification rejected(String validationLabel) {
		return new RecommendationEventVerification(false, "", "", validationLabel == null ? "" : validationLabel);
	}

	public static RecommendationEventVerification trusted(String trackedPath, String slot, String validationLabel) {
		return new RecommendationEventVerification(true,
				trackedPath == null ? "" : trackedPath,
				slot == null ? "" : slot,
				validationLabel == null ? "" : validationLabel);
	}
}
