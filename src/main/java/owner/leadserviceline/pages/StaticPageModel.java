package owner.leadserviceline.pages;

import java.util.List;

public record StaticPageModel(
		String path,
		String pageTitle,
		String eyebrow,
		String heroTitle,
		String heroSummary,
		String contactEmail,
		List<StaticPageSection> sections
) {
	public StaticPageModel {
		contactEmail = contactEmail == null ? "" : contactEmail;
		sections = sections == null ? List.of() : List.copyOf(sections);
	}
}
