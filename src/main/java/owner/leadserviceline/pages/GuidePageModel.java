package owner.leadserviceline.pages;

import java.util.List;

import owner.leadserviceline.data.GuideRecord;
import owner.leadserviceline.data.ProductRecommendationRecord;

public record GuidePageModel(
		String pageTitle,
		GuideRecord guide,
		List<GuideRecord> relatedGuides,
		List<ProductRecommendationRecord> recommendations
) {
	public GuidePageModel {
		relatedGuides = relatedGuides == null ? List.of() : List.copyOf(relatedGuides);
		recommendations = recommendations == null ? List.of() : List.copyOf(recommendations);
	}
}
