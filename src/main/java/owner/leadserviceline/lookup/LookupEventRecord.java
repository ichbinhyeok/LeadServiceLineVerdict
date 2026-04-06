package owner.leadserviceline.lookup;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public record LookupEventRecord(
		String timestamp,
		String lookupLabel,
		String lookupBucketKey,
		String lookupInputType,
		String query,
		String city,
		String state,
		String matchedUtilityId,
		String matchedUtilityName,
		String matchedUtilityPath,
		String confidenceLabel,
		boolean ambiguous,
		boolean geocoderUsed
) {
}
