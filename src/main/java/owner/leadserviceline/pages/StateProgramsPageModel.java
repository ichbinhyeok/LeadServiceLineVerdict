package owner.leadserviceline.pages;

import java.util.List;

public record StateProgramsPageModel(
		String pageTitle,
		String state,
		List<StateProgramCard> programs,
		int utilityCount,
		int routeCount
) {
}
