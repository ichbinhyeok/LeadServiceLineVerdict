package owner.leadserviceline.pages;

import java.util.List;

import owner.leadserviceline.data.GuideRecord;
import owner.leadserviceline.data.UtilityRecord;

public record HomePageModel(
		String pageTitle,
		List<StateSummary> states,
		List<UtilityRecord> featuredUtilities,
		List<GuideRecord> guides,
		int routeCount,
		int programCount
) {
}
