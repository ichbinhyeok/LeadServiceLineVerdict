package owner.leadserviceline.web;

import owner.leadserviceline.pages.LeadServiceLinePageService;
import owner.leadserviceline.pages.OpsReviewSnapshotModel;
import owner.leadserviceline.lookup.LookupEventLogger;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
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
import org.springframework.web.server.ResponseStatusException;

@Controller
@EnableConfigurationProperties(SiteRuntimeProperties.class)
public class SiteController {

	private final LeadServiceLinePageService pageService;
	private final LookupEventLogger lookupEventLogger;
	private final SiteRuntimeProperties siteRuntimeProperties;

	public SiteController(
			LeadServiceLinePageService pageService,
			LookupEventLogger lookupEventLogger,
			SiteRuntimeProperties siteRuntimeProperties
	) {
		this.pageService = pageService;
		this.lookupEventLogger = lookupEventLogger;
		this.siteRuntimeProperties = siteRuntimeProperties;
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

	private void requireOpsAccess(String opsToken) {
		if (!siteRuntimeProperties.opsReviewEnabled()) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND);
		}
		var expectedToken = siteRuntimeProperties.opsReviewToken();
		if (expectedToken == null || expectedToken.isBlank() || !expectedToken.equals(opsToken)) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND);
		}
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
