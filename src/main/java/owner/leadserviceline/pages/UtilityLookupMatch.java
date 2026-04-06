package owner.leadserviceline.pages;

import java.util.List;

import owner.leadserviceline.data.UtilityRecord;

public record UtilityLookupMatch(
		UtilityRecord utility,
		String localPath,
		int score,
		String confidenceLabel,
		boolean manualReviewRecommended,
		List<String> reasons
) {
}
