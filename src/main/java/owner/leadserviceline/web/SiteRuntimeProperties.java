package owner.leadserviceline.web;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "lead-service-line")
public record SiteRuntimeProperties(
		String siteBaseUrl,
		boolean opsReviewEnabled,
		String opsReviewToken,
		boolean adminEnabled,
		String adminUsername,
		String adminPassword,
		String gaMeasurementId
) {

	private static final String LEGACY_ADMIN_USERNAME = "admin";
	private static final String LEGACY_ADMIN_PASSWORD = "tlsgur3108";

	public SiteRuntimeProperties {
		siteBaseUrl = hasText(siteBaseUrl) ? siteBaseUrl.trim() : "https://leadlinerecord.com";
		opsReviewToken = normalize(opsReviewToken);
		adminUsername = normalize(adminUsername);
		adminPassword = normalize(adminPassword);
		gaMeasurementId = normalize(gaMeasurementId);

		if (adminEnabled && !adminConfigured(adminUsername, adminPassword)) {
			throw new IllegalStateException("Admin is enabled but explicit credentials were not supplied.");
		}
		if (adminEnabled
				&& LEGACY_ADMIN_USERNAME.equals(adminUsername)
				&& LEGACY_ADMIN_PASSWORD.equals(adminPassword)) {
			throw new IllegalStateException("Admin cannot use the legacy fallback credentials.");
		}
	}

	public boolean adminConfigured() {
		return adminEnabled && adminConfigured(adminUsername, adminPassword);
	}

	private static boolean adminConfigured(String username, String password) {
		return hasText(username) && hasText(password);
	}

	private static String normalize(String value) {
		return value == null ? "" : value.trim();
	}

	private static boolean hasText(String value) {
		return value != null && !value.isBlank();
	}
}
