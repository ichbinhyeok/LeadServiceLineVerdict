package owner.leadserviceline.recommendation;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import owner.leadserviceline.data.ProductRecommendationRecord;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@EnableConfigurationProperties(RecommendationLinkProperties.class)
public class RecommendationDestinationResolver {

	private final RecommendationLinkProperties properties;

	public RecommendationDestinationResolver(RecommendationLinkProperties properties) {
		this.properties = properties;
	}

	public ResolvedRecommendationDestination resolve(ProductRecommendationRecord recommendation) {
		var overrideUrl = properties.recommendationOverrideUrls().get(recommendation.slug());
		if (hasText(overrideUrl)) {
			return new ResolvedRecommendationDestination(overrideUrl.trim(), "override");
		}
		if (hasText(properties.amazonAssociateTag()) && hasText(recommendation.amazonFallbackUrl())) {
			return new ResolvedRecommendationDestination(
					appendAmazonTag(recommendation.amazonFallbackUrl(), properties.amazonAssociateTag().trim()),
					"amazon-fallback"
			);
		}
		return new ResolvedRecommendationDestination(recommendation.destinationUrl(), "default");
	}

	private String appendAmazonTag(String url, String associateTag) {
		var trimmed = url.trim();
		if (!hasText(trimmed) || !hasText(associateTag)) {
			return trimmed;
		}
		var uri = URI.create(trimmed);
		var existingQuery = uri.getRawQuery();
		var tagParam = "tag=" + URLEncoder.encode(associateTag, StandardCharsets.UTF_8);
		var base = uri.getScheme() + "://" + uri.getRawAuthority() + uri.getRawPath();
		var query = hasText(existingQuery) ? existingQuery + "&" + tagParam : tagParam;
		var fragment = hasText(uri.getRawFragment()) ? "#" + uri.getRawFragment() : "";
		return base + "?" + query + fragment;
	}

	private boolean hasText(String value) {
		return value != null && !value.isBlank();
	}
}
