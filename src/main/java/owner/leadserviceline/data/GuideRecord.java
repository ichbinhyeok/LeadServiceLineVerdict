package owner.leadserviceline.data;

import java.time.LocalDate;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record GuideRecord(
		String guideId,
		String slug,
		String title,
		String eyebrow,
		String heroSummary,
		String verdict,
		List<String> keyPoints,
		List<String> nextSteps,
		List<GuideFaq> faqs,
		LocalDate lastVerified
) {
	public GuideRecord {
		keyPoints = keyPoints == null ? List.of() : List.copyOf(keyPoints);
		nextSteps = nextSteps == null ? List.of() : List.copyOf(nextSteps);
		faqs = faqs == null ? List.of() : List.copyOf(faqs);
	}
}
