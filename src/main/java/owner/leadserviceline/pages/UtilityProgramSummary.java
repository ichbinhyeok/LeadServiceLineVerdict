package owner.leadserviceline.pages;

public record UtilityProgramSummary(
		String programName,
		String subsidyType,
		String publicSideCovered,
		String privateSideCovered,
		String incomeRules,
		String propertyRules,
		String contractorRules,
		String deadlineLabel,
		String applicationUrl,
		String verificationLabel
) {
}
