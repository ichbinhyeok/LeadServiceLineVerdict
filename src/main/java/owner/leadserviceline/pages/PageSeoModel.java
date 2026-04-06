package owner.leadserviceline.pages;

import java.util.List;

public record PageSeoModel(
		String title,
		String description,
		String canonicalUrl,
		String robots,
		List<String> jsonLd,
		String socialImageUrl
) {
	public PageSeoModel(String title, String description, String canonicalUrl, String robots, List<String> jsonLd) {
		this(title, description, canonicalUrl, robots, jsonLd, "");
	}

	public PageSeoModel {
		jsonLd = jsonLd == null ? List.of() : List.copyOf(jsonLd);
		socialImageUrl = socialImageUrl == null ? "" : socialImageUrl;
	}
}
