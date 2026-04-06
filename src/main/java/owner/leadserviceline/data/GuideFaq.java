package owner.leadserviceline.data;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record GuideFaq(
		String question,
		String answer
) {
}
