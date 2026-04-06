package owner.leadserviceline.pages;

public record AdminPerformanceRow(
		String label,
		int impressions,
		int clicks,
		String ctrLabel
) {
	public AdminPerformanceRow {
		label = label == null ? "" : label;
		ctrLabel = ctrLabel == null ? "" : ctrLabel;
	}
}
