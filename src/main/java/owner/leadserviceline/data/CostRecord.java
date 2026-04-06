package owner.leadserviceline.data;

import java.time.LocalDate;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record CostRecord(
		String costId,
		String utilityId,
		String city,
		String state,
		String publicSideBand,
		String privateSideBand,
		String fullReplacementBand,
		String costConfidence,
		String housingTypeAssumption,
		String permitAssumption,
		String restorationAssumption,
		String methodologySummary,
		String ownerTriggerSummary,
		List<String> sourceRefs,
		LocalDate lastVerified
) {
	public CostRecord {
		sourceRefs = sourceRefs == null ? List.of() : List.copyOf(sourceRefs);
	}

	public boolean isLowConfidence() {
		return "low".equalsIgnoreCase(costConfidence);
	}

	public boolean isMediumConfidence() {
		return "medium".equalsIgnoreCase(costConfidence);
	}

	public boolean hasStrongMethodologySignals() {
		return sourceRefs.size() >= 2
				&& hasSpecificBand(publicSideBand)
				&& hasSpecificBand(privateSideBand)
				&& hasSpecificBand(fullReplacementBand)
				&& hasDetailedAssumption(housingTypeAssumption)
				&& hasDetailedAssumption(permitAssumption)
				&& hasDetailedAssumption(restorationAssumption)
				&& hasDetailedNarrative(methodologySummary)
				&& hasDetailedNarrative(ownerTriggerSummary);
	}

	public boolean shouldIndexRoute() {
		if (isLowConfidence()) {
			return false;
		}
		if (isMediumConfidence()) {
			return hasStrongMethodologySignals();
		}
		return true;
	}

	public boolean needsMethodologyHardening() {
		return isMediumConfidence() && !hasStrongMethodologySignals();
	}

	private boolean hasSpecificBand(String value) {
		if (value == null || value.isBlank()) {
			return false;
		}
		var normalized = value.toLowerCase();
		return normalized.matches(".*\\d.*")
				|| normalized.contains("$")
				|| normalized.contains("no cost")
				|| normalized.contains("no additional cost")
				|| normalized.contains("no direct cost")
				|| normalized.contains("no direct charge")
				|| normalized.contains("free of charge")
				|| normalized.contains("100 percent")
				|| normalized.contains("free replacement")
				|| normalized.contains("utility-managed")
				|| normalized.contains("city-managed")
				|| normalized.contains("pay the")
				|| normalized.contains("pays the")
				|| normalized.contains("funded through");
	}

	private boolean hasDetailedAssumption(String value) {
		return value != null && value.trim().length() >= 24;
	}

	private boolean hasDetailedNarrative(String value) {
		return value != null && value.trim().length() >= 40;
	}
}
