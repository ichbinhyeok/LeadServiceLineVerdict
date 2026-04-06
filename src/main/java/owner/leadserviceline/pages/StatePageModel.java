package owner.leadserviceline.pages;

import java.util.List;

import owner.leadserviceline.data.UtilityRecord;

public record StatePageModel(
		String pageTitle,
		String state,
		List<UtilityRecord> utilities,
		int programCount,
		int routeCount
) {
}
