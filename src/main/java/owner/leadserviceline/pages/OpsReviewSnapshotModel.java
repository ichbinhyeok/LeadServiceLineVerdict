package owner.leadserviceline.pages;

import java.util.List;

public record OpsReviewSnapshotModel(
		String generatedAt,
		List<OpsMetric> metrics,
		List<OpsReviewGroup> groups
) {
}
