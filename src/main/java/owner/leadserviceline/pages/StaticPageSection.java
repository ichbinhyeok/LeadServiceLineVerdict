package owner.leadserviceline.pages;

import java.util.List;

public record StaticPageSection(
		String heading,
		List<String> paragraphs
) {
	public StaticPageSection {
		paragraphs = paragraphs == null ? List.of() : List.copyOf(paragraphs);
	}
}
