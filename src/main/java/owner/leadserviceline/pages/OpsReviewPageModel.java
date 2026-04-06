package owner.leadserviceline.pages;

import java.util.List;

public record OpsReviewPageModel(
		String pageTitle,
		List<OpsMetric> metrics,
		List<OpsReviewGroup> groups
) {
}
