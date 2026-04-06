package owner.leadserviceline.pages;

import java.time.LocalDate;
import java.util.List;

public record StateProgramCard(
		String programName,
		String geography,
		String subsidyType,
		String publicSideCovered,
		String privateSideCovered,
		String applicationUrl,
		List<ProgramUtilityLink> utilityLinks,
		String verificationStatus,
		LocalDate lastVerified
) {
}
