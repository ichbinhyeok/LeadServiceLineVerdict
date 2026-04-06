package owner.leadserviceline.pages;

import java.util.List;

public record UtilityTrustModel(
		String reviewedBy,
		String reviewSummary,
		String lastVerifiedLabel,
		String correctionEmail,
		int evidenceCount,
		List<String> publisherNames
) {
	public UtilityTrustModel {
		reviewedBy = reviewedBy == null ? "" : reviewedBy;
		reviewSummary = reviewSummary == null ? "" : reviewSummary;
		lastVerifiedLabel = lastVerifiedLabel == null ? "" : lastVerifiedLabel;
		correctionEmail = correctionEmail == null ? "" : correctionEmail;
		publisherNames = publisherNames == null ? List.of() : List.copyOf(publisherNames);
	}
}
