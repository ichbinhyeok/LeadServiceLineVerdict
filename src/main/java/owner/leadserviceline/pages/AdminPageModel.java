package owner.leadserviceline.pages;

import java.util.List;

public record AdminPageModel(
		String pageTitle,
		List<OpsMetric> metrics,
		List<AdminRecentClick> recentClicks,
		boolean recommendationLogEnabled,
		boolean lookupLogEnabled,
		boolean opsReviewEnabled
) {
	public AdminPageModel {
		metrics = metrics == null ? List.of() : List.copyOf(metrics);
		recentClicks = recentClicks == null ? List.of() : List.copyOf(recentClicks);
	}
}
