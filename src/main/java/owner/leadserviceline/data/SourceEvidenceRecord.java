package owner.leadserviceline.data;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record SourceEvidenceRecord(
		String sourceId,
		String sourceType,
		String publisherName,
		String sourceUrl,
		String scopeType,
		String scopeKey,
		String capturedAt,
		String effectiveDate,
		String claimSummary,
		String reviewerNote,
		String status
) {
}
