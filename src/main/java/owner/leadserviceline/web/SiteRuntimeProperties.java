package owner.leadserviceline.web;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "lead-service-line")
public record SiteRuntimeProperties(
		String siteBaseUrl,
		boolean opsReviewEnabled,
		String opsReviewToken
) {
}
