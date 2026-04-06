package owner.leadserviceline.pages;

import java.util.List;

public record LookupPageModel(
		String pageTitle,
		String query,
		String city,
		String state,
		boolean attempted,
		boolean geocoderUsed,
		String geocoderSummary,
		String resultSummary,
		boolean ambiguous,
		List<UtilityLookupMatch> matches
) {
}
