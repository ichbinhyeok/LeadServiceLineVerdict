package owner.leadserviceline.pages;

public record UtilityRouteLink(
		String label,
		String path,
		boolean active,
		boolean indexable,
		boolean supportLayer
) {
}
