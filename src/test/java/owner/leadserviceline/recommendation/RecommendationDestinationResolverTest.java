package owner.leadserviceline.recommendation;

import java.time.LocalDate;
import java.util.Map;

import owner.leadserviceline.data.ProductRecommendationRecord;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RecommendationDestinationResolverTest {

	@Test
	void usesDirectOverrideWhenConfigured() {
		var resolver = new RecommendationDestinationResolver(new RecommendationLinkProperties(
				"",
				Map.of("zerowater-10-cup-pitcher", "https://tracking.example/zerowater")
		));

		var resolved = resolver.resolve(recommendation(
				"zerowater-10-cup-pitcher",
				"https://www.zerowater.com/products/10-cup-water-filter-pitcher",
				""
		));

		assertEquals("https://tracking.example/zerowater", resolved.url());
		assertEquals("override", resolved.strategy());
	}

	@Test
	void usesAmazonFallbackWhenTagAndFallbackExist() {
		var resolver = new RecommendationDestinationResolver(new RecommendationLinkProperties(
				"leadline-20",
				Map.of()
		));

		var resolved = resolver.resolve(recommendation(
				"brita-tahoe-pitcher-elite-filter",
				"https://www.brita.com/products/tahoe-water-pitcher-elite-filter/",
				"https://www.amazon.com/s?k=Brita+Tahoe+Water+Pitcher+with+Elite+Filter"
		));

		assertEquals("https://www.amazon.com/s?k=Brita+Tahoe+Water+Pitcher+with+Elite+Filter&tag=leadline-20", resolved.url());
		assertEquals("amazon-fallback", resolved.strategy());
	}

	@Test
	void keepsDirectDestinationWhenNoOverrideOrAmazonTagExists() {
		var resolver = new RecommendationDestinationResolver(new RecommendationLinkProperties("", Map.of()));

		var resolved = resolver.resolve(recommendation(
				"epic-smart-shield-under-sink",
				"https://www.epicwaterfilters.com/products/epic-smart-shield-under-sink-water-filter-system",
				""
		));

		assertEquals("https://www.epicwaterfilters.com/products/epic-smart-shield-under-sink-water-filter-system", resolved.url());
		assertEquals("default", resolved.strategy());
	}

	private ProductRecommendationRecord recommendation(String slug, String destinationUrl, String amazonFallbackUrl) {
		return new ProductRecommendationRecord(
				"rec-" + slug,
				slug,
				"best-lead-reduction-filters-after-a-lead-notice",
				1,
				"Recommendation",
				"Badge",
				"pitcher",
				"Merchant",
				"See current price",
				destinationUrl,
				amazonFallbackUrl,
				"Best for",
				"Why it fits",
				"Watchout",
				"Evidence",
				LocalDate.of(2026, 4, 7)
		);
	}
}
