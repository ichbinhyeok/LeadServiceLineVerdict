package owner.leadserviceline.data;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record RouteRecord(
		String path,
		String template,
		String state,
		String city,
		String utilityId,
		String programId,
		String canonicalPath,
		boolean indexable,
		String decisionReason,
		String lastGenerated
) {
}
