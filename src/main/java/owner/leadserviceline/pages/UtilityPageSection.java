package owner.leadserviceline.pages;

import java.util.Arrays;

public enum UtilityPageSection {
	OVERVIEW("utility-overview", "pages/utility-overview", "Overview"),
	NOTIFICATION("utility-notification", "pages/utility-notification", "Notification"),
	PROGRAM("utility-program", "pages/utility-program", "Program"),
	REPLACEMENT_COST("utility-replacement-cost", "pages/utility-replacement-cost", "Replacement Cost"),
	FILTER_AND_TESTING("utility-filter-and-testing", "pages/utility-filter-and-testing", "Filter and Testing", true),
	BUYER_SELLER("utility-buyer-seller", "pages/utility-buyer-seller", "Buyer and Seller", true);

	private final String routeTemplate;
	private final String viewName;
	private final String label;
	private final boolean supportLayer;

	UtilityPageSection(String routeTemplate, String viewName, String label) {
		this(routeTemplate, viewName, label, false);
	}

	UtilityPageSection(String routeTemplate, String viewName, String label, boolean supportLayer) {
		this.routeTemplate = routeTemplate;
		this.viewName = viewName;
		this.label = label;
		this.supportLayer = supportLayer;
	}

	public String routeTemplate() {
		return routeTemplate;
	}

	public String viewName() {
		return viewName;
	}

	public String label() {
		return label;
	}

	public boolean supportLayer() {
		return supportLayer;
	}

	public static UtilityPageSection fromRouteTemplate(String routeTemplate) {
		return Arrays.stream(values())
				.filter(section -> section.routeTemplate.equals(routeTemplate))
				.findFirst()
				.orElse(OVERVIEW);
	}
}
