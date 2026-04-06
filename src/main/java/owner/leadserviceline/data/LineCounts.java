package owner.leadserviceline.data;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record LineCounts(
		Integer known,
		Integer potential,
		Integer unknown,
		Integer nonLead
) {
	public int total() {
		return safe(known) + safe(potential) + safe(unknown) + safe(nonLead);
	}

	public boolean hasAnyValues() {
		return known != null || potential != null || unknown != null || nonLead != null;
	}

	public boolean hasCompleteBreakdown() {
		return known != null && potential != null && unknown != null && nonLead != null;
	}

	private int safe(Integer value) {
		return value == null ? 0 : value;
	}
}
