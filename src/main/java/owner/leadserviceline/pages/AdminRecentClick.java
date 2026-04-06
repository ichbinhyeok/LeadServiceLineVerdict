package owner.leadserviceline.pages;

public record AdminRecentClick(
		String timestampLabel,
		String productName,
		String guideTitle,
		String guidePath,
		String sourcePath,
		String destinationDomain
) {
	public AdminRecentClick {
		timestampLabel = timestampLabel == null ? "" : timestampLabel;
		productName = productName == null ? "" : productName;
		guideTitle = guideTitle == null ? "" : guideTitle;
		guidePath = guidePath == null ? "" : guidePath;
		sourcePath = sourcePath == null ? "" : sourcePath;
		destinationDomain = destinationDomain == null ? "" : destinationDomain;
	}
}
