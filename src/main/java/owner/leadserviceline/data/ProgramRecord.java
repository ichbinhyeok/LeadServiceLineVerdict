package owner.leadserviceline.data;

import java.time.LocalDate;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ProgramRecord(
		String programId,
		String programSlug,
		String programName,
		String geography,
		String state,
		List<String> utilityIds,
		String subsidyType,
		String publicSideCovered,
		String privateSideCovered,
		String incomeRules,
		String propertyRules,
		String contractorRules,
		LocalDate deadline,
		String applicationUrl,
		List<String> sourceRefs,
		String verificationStatus,
		LocalDate lastVerified
) {
	public ProgramRecord {
		utilityIds = utilityIds == null ? List.of() : List.copyOf(utilityIds);
		sourceRefs = sourceRefs == null ? List.of() : List.copyOf(sourceRefs);
	}
}
