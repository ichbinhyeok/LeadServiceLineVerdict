package owner.leadserviceline.pages;

import java.util.List;

import owner.leadserviceline.data.GuideRecord;

public record GuidePageModel(
		String pageTitle,
		GuideRecord guide,
		List<GuideRecord> relatedGuides
) {
}
