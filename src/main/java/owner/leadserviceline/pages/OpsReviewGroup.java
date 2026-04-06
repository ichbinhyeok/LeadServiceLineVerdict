package owner.leadserviceline.pages;

import java.util.List;

public record OpsReviewGroup(
		String severity,
		String title,
		String summary,
		List<OpsReviewEntry> entries
) {
}
