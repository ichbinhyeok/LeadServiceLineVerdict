package owner.leadserviceline.web;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

import owner.leadserviceline.pages.LeadServiceLinePageService;
import owner.leadserviceline.pages.OpsReviewSnapshotModel;
import owner.leadserviceline.lookup.LookupEventLogger;
import owner.leadserviceline.recommendation.RecommendationDestinationResolver;
import owner.leadserviceline.recommendation.RecommendationClickLogger;
import owner.leadserviceline.recommendation.RecommendationImpressionLogger;
import owner.leadserviceline.recommendation.RecommendationTrackingService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.server.ResponseStatusException;

@Controller
@EnableConfigurationProperties(SiteRuntimeProperties.class)
public class SiteController {

	private static final byte[] TRACKING_PIXEL_GIF = Base64.getDecoder().decode("R0lGODlhAQABAIABAP///wAAACwAAAAAAQABAAACAkQBADs=");

	private final LeadServiceLinePageService pageService;
	private final LookupEventLogger lookupEventLogger;
	private final RecommendationDestinationResolver recommendationDestinationResolver;
	private final RecommendationClickLogger recommendationClickLogger;
	private final RecommendationImpressionLogger recommendationImpressionLogger;
	private final RecommendationTrackingService recommendationTrackingService;
	private final SiteRuntimeProperties siteRuntimeProperties;

	public SiteController(
			LeadServiceLinePageService pageService,
			LookupEventLogger lookupEventLogger,
			RecommendationDestinationResolver recommendationDestinationResolver,
			RecommendationClickLogger recommendationClickLogger,
			RecommendationImpressionLogger recommendationImpressionLogger,
			RecommendationTrackingService recommendationTrackingService,
			SiteRuntimeProperties siteRuntimeProperties
	) {
		this.pageService = pageService;
		this.lookupEventLogger = lookupEventLogger;
		this.recommendationDestinationResolver = recommendationDestinationResolver;
		this.recommendationClickLogger = recommendationClickLogger;
		this.recommendationImpressionLogger = recommendationImpressionLogger;
		this.recommendationTrackingService = recommendationTrackingService;
		this.siteRuntimeProperties = siteRuntimeProperties;
	}

	@ModelAttribute("gaMeasurementId")
	public String gaMeasurementId() {
		return hasText(siteRuntimeProperties.gaMeasurementId()) ? siteRuntimeProperties.gaMeasurementId().trim() : "";
	}

	@ModelAttribute("recommendationTracking")
	public RecommendationTrackingService recommendationTracking() {
		return recommendationTrackingService;
	}

	@GetMapping("/")
	public String home(Model model) {
		var page = pageService.homePage();
		model.addAttribute("page", page);
		model.addAttribute("seo", pageService.homeSeo(page));
		return "pages/home";
	}

	@GetMapping("/lookup")
	public String lookup(
			@RequestParam(required = false) String query,
			@RequestParam(required = false) String city,
			@RequestParam(required = false) String state,
			HttpServletResponse response,
			Model model
	) {
		applySensitiveNoStoreHeaders(response, "noindex,nofollow");
		if (hasText(query) || hasText(city) || hasText(state)) {
			return "redirect:/lookup";
		}
		var page = pageService.lookupPage("", "", "");
		model.addAttribute("page", page);
		model.addAttribute("seo", pageService.lookupSeo(page));
		return "pages/lookup";
	}

	@PostMapping("/lookup")
	public String lookupSubmit(
			@RequestParam(required = false) String query,
			@RequestParam(required = false) String city,
			@RequestParam(required = false) String state,
			HttpServletResponse response,
			Model model
	) {
		applySensitiveNoStoreHeaders(response, "noindex,nofollow");
		var page = pageService.lookupPage(query, city, state);
		lookupEventLogger.logLookup(query, city, state, page);
		model.addAttribute("page", page);
		model.addAttribute("seo", pageService.lookupSeo(page));
		return "pages/lookup";
	}

	@GetMapping("/ops/review")
	public String opsReview(
			@RequestHeader(value = "X-Ops-Token", required = false) String opsToken,
			HttpServletResponse response,
			Model model
	) {
		requireOpsAccess(opsToken);
		applySensitiveNoStoreHeaders(response, "noindex,nofollow");
		var page = pageService.opsReviewPage();
		model.addAttribute("page", page);
		model.addAttribute("seo", pageService.opsReviewSeo(page));
		return "pages/ops-review";
	}

	@GetMapping("/admin")
	public Object admin(
			@RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorization,
			HttpServletResponse response,
			Model model
	) {
		if (!siteRuntimeProperties.adminConfigured()) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND);
		}
		if (!hasValidAdminAuthorization(authorization)) {
			return unauthorizedAdmin();
		}
		applySensitiveNoStoreHeaders(response, "noindex,nofollow");
		var page = pageService.adminPage();
		model.addAttribute("page", page);
		model.addAttribute("seo", pageService.adminSeo(page));
		return "pages/admin";
	}

	@GetMapping(value = "/ops/review/export", produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseEntity<OpsReviewSnapshotModel> opsReviewExport(
			@RequestHeader(value = "X-Ops-Token", required = false) String opsToken
	) {
		requireOpsAccess(opsToken);
		return ResponseEntity.ok()
				.header(HttpHeaders.CACHE_CONTROL, "no-store, max-age=0")
				.header("Pragma", "no-cache")
				.header("X-Robots-Tag", "noindex, nofollow, noarchive")
				.body(pageService.opsReviewSnapshot());
	}

	@GetMapping("/lead-service-line/{state}")
	public String stateHub(@PathVariable String state, Model model) {
		var page = pageService.statePage(state)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
		model.addAttribute("page", page);
		model.addAttribute("seo", pageService.stateSeo(page));
		return "pages/state";
	}

	@GetMapping("/lead-service-line/{state}/programs")
	public String statePrograms(@PathVariable String state, Model model) {
		var page = pageService.stateProgramsPage(state)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
		model.addAttribute("page", page);
		model.addAttribute("seo", pageService.stateProgramsSeo(page));
		return "pages/state-programs";
	}

	@GetMapping("/guides/{slug}")
	public String guidePage(@PathVariable String slug, Model model) {
		var page = pageService.guidePage(slug)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
		model.addAttribute("page", page);
		model.addAttribute("seo", pageService.guideSeo(page));
		return "pages/guide";
	}

	@GetMapping("/go/{slug}")
	public ResponseEntity<Void> recommendationRedirect(
			@PathVariable String slug,
			@RequestParam(required = false) String sourcePath,
			@RequestParam(required = false) String slot,
			@RequestParam(required = false, name = "sig") String signature,
			@RequestHeader(value = "Referer", required = false) String referer,
			@RequestHeader(value = "Sec-Fetch-Site", required = false) String secFetchSite,
			@RequestHeader(value = HttpHeaders.USER_AGENT, required = false) String userAgent,
			HttpServletRequest request
	) {
		var recommendation = pageService.recommendation(slug)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
		var destination = recommendationDestinationResolver.resolve(recommendation);
		var verification = recommendationTrackingService.validateClick(
				recommendation.slug(),
				sourcePath,
				slot,
				signature,
				referer,
				secFetchSite,
				userAgent,
				request.getRemoteAddr()
		);
		if (verification.trusted()) {
			recommendationClickLogger.logClick(
					recommendation,
					destination.url(),
					verification.trackedPath(),
					verification.slot(),
					verification.validationLabel()
			);
		}
		return ResponseEntity.status(HttpStatus.FOUND)
				.header(HttpHeaders.CACHE_CONTROL, "no-store, max-age=0")
				.header("Pragma", "no-cache")
				.header("Referrer-Policy", "no-referrer")
				.header("X-Robots-Tag", "noindex, nofollow, noarchive")
				.location(URI.create(destination.url()))
				.build();
	}

	@GetMapping(value = "/events/recommendation-impression", produces = MediaType.IMAGE_GIF_VALUE)
	@ResponseBody
	public ResponseEntity<byte[]> recommendationImpression(
			@RequestParam String slug,
			@RequestParam(required = false) String pagePath,
			@RequestParam(required = false) String slot,
			@RequestParam(required = false, name = "sig") String signature,
			@RequestHeader(value = "Referer", required = false) String referer,
			@RequestHeader(value = "Sec-Fetch-Site", required = false) String secFetchSite,
			@RequestHeader(value = "Sec-Fetch-Dest", required = false) String secFetchDest,
			@RequestHeader(value = HttpHeaders.USER_AGENT, required = false) String userAgent,
			HttpServletRequest request
	) {
		var recommendation = pageService.recommendation(slug)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
		var verification = recommendationTrackingService.validateImpression(
				recommendation.slug(),
				pagePath,
				slot,
				signature,
				referer,
				secFetchSite,
				secFetchDest,
				userAgent,
				request.getRemoteAddr()
		);
		if (verification.trusted()) {
			recommendationImpressionLogger.logImpression(
					recommendation,
					verification.trackedPath(),
					verification.slot(),
					verification.validationLabel()
			);
		}
		return ResponseEntity.ok()
				.header(HttpHeaders.CACHE_CONTROL, "no-store, max-age=0")
				.header("Pragma", "no-cache")
				.header("X-Robots-Tag", "noindex, nofollow, noarchive")
				.body(TRACKING_PIXEL_GIF);
	}

	@GetMapping({
			"/about",
			"/affiliate-disclosure",
			"/methodology",
			"/editorial-policy",
			"/privacy",
			"/terms",
			"/contact"
	})
	public String staticPage(HttpServletRequest request, Model model) {
		var page = pageService.staticPage(request.getRequestURI())
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
		model.addAttribute("page", page);
		model.addAttribute("seo", pageService.staticSeo(page));
		return "pages/static-page";
	}

	@GetMapping({
			"/lead-service-line/{state}/{city}/{utilitySlug}",
			"/lead-service-line/{state}/{city}/{utilitySlug}/{section}"
	})
	public String utilityPage(
			@PathVariable String state,
			@PathVariable String city,
			@PathVariable String utilitySlug,
			@PathVariable(required = false) String section,
			Model model
	) {
		var page = pageService.utilityPage(state, city, utilitySlug, section)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
		model.addAttribute("page", page);
		model.addAttribute("seo", pageService.utilitySeo(page));
		return page.section().viewName();
	}

	@GetMapping(value = "/robots.txt", produces = MediaType.TEXT_PLAIN_VALUE)
	@ResponseBody
	public ResponseEntity<String> robotsTxt() {
		return ResponseEntity.ok()
				.header(HttpHeaders.CACHE_CONTROL, "no-store, max-age=0")
				.header("Pragma", "no-cache")
				.body(pageService.robotsTxt());
	}

	@GetMapping(value = "/sitemap.xml", produces = MediaType.APPLICATION_XML_VALUE)
	@ResponseBody
	public ResponseEntity<String> sitemapXml() {
		return ResponseEntity.ok()
				.header(HttpHeaders.CACHE_CONTROL, "no-store, max-age=0")
				.header("Pragma", "no-cache")
				.body(pageService.sitemapXml());
	}

	@GetMapping(value = "/site.webmanifest", produces = "application/manifest+json")
	@ResponseBody
	public ResponseEntity<Resource> siteWebManifest() {
		return ResponseEntity.ok()
				.contentType(MediaType.parseMediaType("application/manifest+json"))
				.body(new ClassPathResource("static/site.webmanifest"));
	}

	private void requireOpsAccess(String opsToken) {
		if (!siteRuntimeProperties.opsReviewEnabled()) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND);
		}
		var expectedToken = siteRuntimeProperties.opsReviewToken();
		if (expectedToken == null || expectedToken.isBlank() || !expectedToken.equals(opsToken)) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND);
		}
	}

	private boolean hasValidAdminAuthorization(String authorization) {
		if (!siteRuntimeProperties.adminConfigured()) {
			return false;
		}
		if (authorization == null || !authorization.startsWith("Basic ")) {
			return false;
		}
		try {
			var decoded = new String(Base64.getDecoder().decode(authorization.substring(6)), StandardCharsets.UTF_8);
			var expected = siteRuntimeProperties.adminUsername() + ":" + siteRuntimeProperties.adminPassword();
			return decoded.equals(expected);
		} catch (IllegalArgumentException exception) {
			return false;
		}
	}

	private ResponseEntity<Void> unauthorizedAdmin() {
		return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
				.header(HttpHeaders.WWW_AUTHENTICATE, "Basic realm=\"Lead Line Admin\"")
				.header(HttpHeaders.CACHE_CONTROL, "no-store, max-age=0")
				.header("Pragma", "no-cache")
				.header("X-Robots-Tag", "noindex, nofollow, noarchive")
				.build();
	}

	private void applySensitiveNoStoreHeaders(HttpServletResponse response, String robots) {
		response.setHeader(HttpHeaders.CACHE_CONTROL, "no-store, max-age=0");
		response.setHeader("Pragma", "no-cache");
		response.setHeader("Referrer-Policy", "no-referrer");
		response.setHeader("X-Robots-Tag", robots + ", noarchive");
	}

	private boolean hasText(String value) {
		return value != null && !value.isBlank();
	}
}
