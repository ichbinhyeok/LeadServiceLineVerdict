package owner.leadserviceline.pages;

import java.util.List;

public record AdminPageModel(
		String pageTitle,
		List<OpsMetric> metrics,
		List<AdminRecentClick> recentClicks,
		List<AdminPerformanceRow> slotPerformance,
		List<AdminPerformanceRow> pagePerformance,
		boolean recommendationLogEnabled,
		boolean recommendationTrackingProtected,
		boolean lookupLogEnabled,
		boolean opsReviewEnabled
) {
	public AdminPageModel {
		metrics = metrics == null ? List.of() : List.copyOf(metrics);
		recentClicks = recentClicks == null ? List.of() : List.copyOf(recentClicks);
		slotPerformance = slotPerformance == null ? List.of() : List.copyOf(slotPerformance);
		pagePerformance = pagePerformance == null ? List.of() : List.copyOf(pagePerformance);
	}
}
