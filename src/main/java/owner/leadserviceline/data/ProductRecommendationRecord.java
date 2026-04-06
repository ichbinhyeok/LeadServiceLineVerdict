package owner.leadserviceline.data;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ProductRecommendationRecord(
		String recommendationId,
		String slug,
		String guideSlug,
		int displayOrder,
		String name,
		String badge,
		String category,
		String merchantName,
		String destinationLabel,
		String destinationUrl,
		String bestFor,
		String whyItFits,
		String watchout,
		String evidenceNote,
		LocalDate lastVerified
) {
	public ProductRecommendationRecord {
		recommendationId = recommendationId == null ? "" : recommendationId;
		slug = slug == null ? "" : slug;
		guideSlug = guideSlug == null ? "" : guideSlug;
		name = name == null ? "" : name;
		badge = badge == null ? "" : badge;
		category = category == null ? "" : category;
		merchantName = merchantName == null ? "" : merchantName;
		destinationLabel = destinationLabel == null ? "" : destinationLabel;
		destinationUrl = destinationUrl == null ? "" : destinationUrl;
		bestFor = bestFor == null ? "" : bestFor;
		whyItFits = whyItFits == null ? "" : whyItFits;
		watchout = watchout == null ? "" : watchout;
		evidenceNote = evidenceNote == null ? "" : evidenceNote;
	}
}
