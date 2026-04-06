package owner.leadserviceline.pages;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.fasterxml.jackson.databind.ObjectMapper;
import owner.leadserviceline.data.LeadServiceLineRepository;
import owner.leadserviceline.data.GuideRecord;
import owner.leadserviceline.lookup.AddressGeocoder;
import owner.leadserviceline.lookup.GeocodedAddress;
import owner.leadserviceline.lookup.LookupEventRecord;
import owner.leadserviceline.lookup.LookupLoggingProperties;
import owner.leadserviceline.web.SiteRuntimeProperties;
import owner.leadserviceline.data.SourceEvidenceRecord;
import owner.leadserviceline.data.ProgramRecord;
import owner.leadserviceline.data.RouteRecord;
import owner.leadserviceline.data.UtilityRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class LeadServiceLinePageService {

	private static final Logger LOGGER = LoggerFactory.getLogger(LeadServiceLinePageService.class);
	private static final Map<String, String> STATE_NAMES = buildStateNames();
	private static final int SOURCE_REVIEW_WARNING_DAYS = 90;
	private static final int RECORD_REVIEW_WARNING_DAYS = 120;

	private final LeadServiceLineRepository repository;
	private final AddressGeocoder addressGeocoder;
	private final ObjectMapper objectMapper;
	private final LookupLoggingProperties lookupLoggingProperties;
	private final SiteRuntimeProperties siteRuntimeProperties;

	@Autowired
	public LeadServiceLinePageService(
			LeadServiceLineRepository repository,
			AddressGeocoder addressGeocoder,
			ObjectMapper objectMapper,
			LookupLoggingProperties lookupLoggingProperties,
			SiteRuntimeProperties siteRuntimeProperties
	) {
		this.repository = repository;
		this.addressGeocoder = addressGeocoder;
		this.objectMapper = objectMapper;
		this.lookupLoggingProperties = lookupLoggingProperties;
		this.siteRuntimeProperties = siteRuntimeProperties;
	}

	LeadServiceLinePageService(LeadServiceLineRepository repository, AddressGeocoder addressGeocoder) {
		this(
				repository,
				addressGeocoder,
				new ObjectMapper(),
				new LookupLoggingProperties(false, "data/logs/lookup-events.jsonl", 14),
				new SiteRuntimeProperties("http://localhost:8080", false, "")
		);
	}

	public HomePageModel homePage() {
		var utilities = repository.utilities().stream()
				.sorted(Comparator.comparing(UtilityRecord::state).thenComparing(UtilityRecord::city).thenComparing(UtilityRecord::utilityName))
				.toList();
		var allRoutes = repository.routes();
		var guides = repository.guides().stream()
				.sorted(Comparator.comparing(GuideRecord::title))
				.toList();
		var states = utilities.stream()
				.collect(Collectors.groupingBy(UtilityRecord::state))
				.entrySet().stream()
				.map(entry -> new StateSummary(
						entry.getKey(),
						entry.getValue().size(),
						(int) allRoutes.stream()
								.filter(route -> entry.getKey().equals(route.state()))
								.count()
				))
				.sorted(Comparator.comparing(StateSummary::state))
				.toList();
		return new HomePageModel(
				"Lead Service Line Verdict",
				states,
				utilities,
				guides,
				allRoutes.size(),
				repository.programs().size()
		);
	}

	public Optional<StatePageModel> statePage(String state) {
		var normalizedState = normalizeSegment(state);
		var utilities = repository.utilities().stream()
				.filter(utility -> normalizedState.equals(utility.state()))
				.sorted(Comparator.comparing(UtilityRecord::city).thenComparing(UtilityRecord::utilityName))
				.toList();
		if (utilities.isEmpty()) {
			return Optional.empty();
		}
		var routeCount = repository.routesForState(normalizedState).size();
		var programCount = repository.programsForState(normalizedState).size();
		return Optional.of(new StatePageModel(
				"Lead service line routes for " + normalizedState.toUpperCase(Locale.US),
				normalizedState,
				utilities,
				programCount,
				routeCount
		));
	}

	public Optional<StateProgramsPageModel> stateProgramsPage(String state) {
		var normalizedState = normalizeSegment(state);
		var programs = repository.programsForState(normalizedState);
		if (programs.isEmpty()) {
			return Optional.empty();
		}

		var cards = programs.stream()
				.map(program -> new StateProgramCard(
						program.programName(),
						program.geography(),
						program.subsidyType(),
						program.publicSideCovered(),
						program.privateSideCovered(),
						program.applicationUrl(),
						program.utilityIds().stream()
								.map(repository::findUtilityById)
								.flatMap(Optional::stream)
								.map(utility -> new ProgramUtilityLink(
										utility.utilityName(),
										buildUtilityPath(utility.state(), utility.city(), utility.utilitySlug(), "program")
								))
								.toList(),
						program.verificationStatus(),
						program.lastVerified()
				))
				.toList();

		var utilityCount = (int) programs.stream()
				.flatMap(program -> program.utilityIds().stream())
				.distinct()
				.count();

		return Optional.of(new StateProgramsPageModel(
				"Lead service line programs for " + normalizedState.toUpperCase(Locale.US),
				normalizedState,
				cards,
				utilityCount,
				repository.routesForState(normalizedState).size()
		));
	}

	public Optional<GuidePageModel> guidePage(String slug) {
		return repository.findGuideBySlug(normalizeSegment(slug))
				.map(guide -> new GuidePageModel(
						guide.title(),
						guide,
						repository.guides().stream()
								.filter(candidate -> !candidate.slug().equals(guide.slug()))
								.sorted(Comparator.comparing(GuideRecord::title))
								.limit(3)
								.toList()
				));
	}

	public Optional<UtilityPageModel> utilityPage(String state, String city, String utilitySlug, String routeSection) {
		var path = buildUtilityPath(state, city, utilitySlug, routeSection);
		var route = repository.findRoute(path);
		if (route.isEmpty()) {
			return Optional.empty();
		}

		var utility = repository.findUtilityById(route.get().utilityId());
		if (utility.isEmpty()) {
			return Optional.empty();
		}

		var pageSection = UtilityPageSection.fromRouteTemplate(route.get().template());
		var programs = repository.programsForUtility(utility.get().utilityId());
		var cost = repository.costForUtility(utility.get().utilityId()).orElse(null);

		var utilityRoutes = repository.routesForUtility(utility.get().utilityId()).stream()
				.sorted(Comparator.comparingInt(this::routeOrder))
				.map(candidate -> new UtilityRouteLink(
						UtilityPageSection.fromRouteTemplate(candidate.template()).label(),
						candidate.path(),
						candidate.path().equals(route.get().path()),
						candidate.indexable(),
						UtilityPageSection.fromRouteTemplate(candidate.template()).supportLayer()
				))
				.toList();

		return Optional.of(new UtilityPageModel(
				buildPageTitle(pageSection, utility.get()),
				pageSection,
				buildHeroTitle(pageSection, utility.get()),
				buildHeroSummary(pageSection, utility.get(), programs),
				utility.get(),
				route.get(),
				utilityRoutes,
				buildNextSteps(pageSection, utility.get(), programs),
				buildKeyFacts(pageSection, utility.get(), route.get(), programs, cost),
				buildRouteCautions(pageSection, utility.get(), route.get(), programs, cost),
				programs,
				buildProgramSummaries(programs),
				cost,
				buildCostResponsibilities(cost, programs),
				buildRouteSources(pageSection, utility.get(), programs, cost)
		));
	}

	public LookupPageModel lookupPage(String query, String city, String state) {
		var attempted = hasText(query) || hasText(city) || hasText(state);
		if (!attempted) {
			return new LookupPageModel(
					"Find your utility",
					safe(query),
					safe(city),
					safe(state),
					false,
					false,
					"",
					"",
					false,
					List.of()
			);
		}

		var geocodedAddress = addressGeocoder.geocode(query, city, state);
		var explicitNormalizedCity = normalizeLookupText(city);
		var geocodedNormalizedCity = normalizeLookupText(geocodedAddress.map(GeocodedAddress::city).orElse(""));
		var resolvedNormalizedCity = hasText(explicitNormalizedCity) ? explicitNormalizedCity : geocodedNormalizedCity;
		var normalizedState = resolveStateCode(Stream.of(state, query, geocodedAddress.map(GeocodedAddress::state).orElse(null))
				.filter(this::hasText)
				.collect(Collectors.joining(" ")));
		var resolvedPostalPrefix = geocodedAddress.map(GeocodedAddress::zipCode)
				.filter(this::hasText)
				.map(zip -> zip.length() >= 3 ? zip.substring(0, 3) : zip)
				.orElse("");
		var queryText = Stream.of(
						query,
						city,
						state,
						geocodedAddress.map(GeocodedAddress::matchedAddress).orElse(null),
						geocodedAddress.map(GeocodedAddress::city).orElse(null),
						geocodedAddress.map(GeocodedAddress::zipCode).orElse(null)
				)
				.filter(this::hasText)
				.collect(Collectors.joining(" "));

		var matches = repository.utilities().stream()
				.map(utility -> scoreUtilityLookup(
						utility,
						queryText,
						explicitNormalizedCity,
						resolvedNormalizedCity,
						normalizedState,
						resolvedPostalPrefix
				))
				.filter(match -> match.score() >= 60 || !match.manualReviewRecommended())
				.sorted(Comparator.comparingInt(UtilityLookupMatch::score).reversed()
						.thenComparing(Comparator.comparingInt((UtilityLookupMatch match) -> lookupModeRank(match.utility().addressLookupMode())).reversed())
						.thenComparing(match -> match.utility().utilityName()))
				.limit(8)
				.toList();

		var ambiguous = matches.size() > 1
				&& matches.get(0).score() - matches.get(1).score() < 15;
		var resultSummary = buildLookupSummary(matches, ambiguous, hasText(explicitNormalizedCity), geocodedAddress.isPresent());
		var geocoderSummary = geocodedAddress
				.map(match -> "Census geocoder normalized the address to " + match.matchedAddress() + ".")
				.orElse("");

		return new LookupPageModel(
				"Find your utility",
				safe(query),
				safe(city),
				safe(state),
				true,
				geocodedAddress.isPresent(),
				geocoderSummary,
				resultSummary,
				ambiguous,
				matches
		);
	}

	public OpsReviewPageModel opsReviewPage() {
		var snapshot = buildOpsReviewSnapshot();
		return new OpsReviewPageModel(
				"Ops review",
				snapshot.metrics(),
				snapshot.groups()
		);
	}

	public OpsReviewSnapshotModel opsReviewSnapshot() {
		return buildOpsReviewSnapshot();
	}

	public PageSeoModel homeSeo(HomePageModel page) {
		var description = "Find utility-by-utility lead service line answers: inventory status, mailed notices, replacement programs, and likely replacement cost.";
		return new PageSeoModel(
				page.pageTitle(),
				description,
				absoluteUrl("/"),
				"index,follow",
				List.of(toJsonLd(Map.of(
						"@context", "https://schema.org",
						"@type", "WebSite",
						"name", page.pageTitle(),
						"url", absoluteUrl("/"),
						"description", description
				)))
		);
	}

	public PageSeoModel stateSeo(StatePageModel page) {
		var description = page.state().toUpperCase(Locale.US) + " utility directory for lead service line notices, replacement programs, and local replacement questions.";
		return new PageSeoModel(
				page.pageTitle(),
				description,
				absoluteUrl("/lead-service-line/" + page.state()),
				"index,follow",
				List.of(toJsonLd(Map.of(
						"@context", "https://schema.org",
						"@type", "CollectionPage",
						"name", page.pageTitle(),
						"url", absoluteUrl("/lead-service-line/" + page.state()),
						"description", description
				)))
		);
	}

	public PageSeoModel stateProgramsSeo(StateProgramsPageModel page) {
		var description = page.state().toUpperCase(Locale.US) + " utility-linked lead service line program tracker for replacement support, reimbursement, and loan paths.";
		return new PageSeoModel(
				page.pageTitle(),
				description,
				absoluteUrl("/lead-service-line/" + page.state() + "/programs"),
				"index,follow",
				List.of(toJsonLd(Map.of(
						"@context", "https://schema.org",
						"@type", "CollectionPage",
						"name", page.pageTitle(),
						"url", absoluteUrl("/lead-service-line/" + page.state() + "/programs"),
						"description", description
				)))
		);
	}

	public PageSeoModel guideSeo(GuidePageModel page) {
		var path = "/guides/" + page.guide().slug();
		return new PageSeoModel(
				page.pageTitle(),
				page.guide().heroSummary(),
				absoluteUrl(path),
				"index,follow",
				List.of(
						toJsonLd(Map.of(
								"@context", "https://schema.org",
								"@type", "Article",
								"headline", page.guide().title(),
								"description", page.guide().heroSummary(),
								"url", absoluteUrl(path),
								"dateModified", page.guide().lastVerified().toString()
						)),
						toJsonLd(breadcrumbData(List.of(
								Map.entry("Guides", absoluteUrl("/")),
								Map.entry(page.guide().title(), absoluteUrl(path))
						)))
				)
		);
	}

	public PageSeoModel utilitySeo(UtilityPageModel page) {
		var path = page.currentRoute().canonicalPath();
		var description = buildUtilitySeoDescription(page);
		var jsonLd = new ArrayList<String>();
		jsonLd.add(toJsonLd(Map.of(
				"@context", "https://schema.org",
				"@type", "WebPage",
				"name", page.pageTitle(),
				"url", absoluteUrl(path),
				"description", description
		)));
		jsonLd.add(toJsonLd(breadcrumbData(List.of(
				Map.entry("States", absoluteUrl("/lead-service-line/" + page.utility().state())),
				Map.entry(page.utility().utilityName(), absoluteUrl(buildUtilityPath(page.utility().state(), page.utility().city(), page.utility().utilitySlug(), null))),
				Map.entry(page.section().label(), absoluteUrl(path))
		))));
		return new PageSeoModel(
				page.pageTitle(),
				description,
				absoluteUrl(path),
				page.currentRoute().indexable() ? "index,follow" : "noindex,follow",
				jsonLd
		);
	}

	public PageSeoModel lookupSeo(LookupPageModel page) {
		var description = "Use an address, city, or utility hint to find the likely utility page, then confirm on the official utility lookup.";
		return new PageSeoModel(
				page.pageTitle(),
				description,
				absoluteUrl("/lookup"),
				"noindex,nofollow",
				List.of(toJsonLd(Map.of(
						"@context", "https://schema.org",
						"@type", "WebPage",
						"name", page.pageTitle(),
						"url", absoluteUrl("/lookup"),
						"description", description
				)))
		);
	}

	public PageSeoModel opsReviewSeo(OpsReviewPageModel page) {
		return new PageSeoModel(
				page.pageTitle(),
				"Internal review queue for stale records, low-confidence cost routes, and dataset gaps.",
				absoluteUrl("/ops/review"),
				"noindex,nofollow",
				List.of()
		);
	}

	public String robotsTxt() {
		return String.join("\n",
				"User-agent: *",
				"Allow: /",
				"Disallow: /lookup",
				"Disallow: /ops/",
				"Sitemap: " + absoluteUrl("/sitemap.xml"),
				""
		);
	}

	public String sitemapXml() {
		var urls = new ArrayList<String>();
		urls.add(urlEntry("/", OffsetDateTime.now().toLocalDate().toString()));
		repository.routes().stream()
				.filter(RouteRecord::indexable)
				.sorted(Comparator.comparing(RouteRecord::canonicalPath))
				.map(RouteRecord::canonicalPath)
				.distinct()
				.forEach(path -> urls.add(urlEntry(path, OffsetDateTime.now().toLocalDate().toString())));
		return """
				<?xml version="1.0" encoding="UTF-8"?>
				<urlset xmlns="http://www.sitemaps.org/schemas/sitemap/0.9">
				%s
				</urlset>
				""".formatted(String.join("\n", urls));
	}

	private OpsReviewSnapshotModel buildOpsReviewSnapshot() {
		var utilities = repository.utilities().stream()
				.sorted(Comparator.comparing(UtilityRecord::state).thenComparing(UtilityRecord::city).thenComparing(UtilityRecord::utilityName))
				.toList();
		var programs = repository.programs().stream()
				.sorted(Comparator.comparing(ProgramRecord::programName))
				.toList();
		var costs = repository.costs().stream()
				.sorted(Comparator.comparing(owner.leadserviceline.data.CostRecord::utilityId))
				.toList();
		var sources = repository.sources().stream()
				.sorted(Comparator.comparing(SourceEvidenceRecord::scopeType).thenComparing(SourceEvidenceRecord::scopeKey))
				.toList();
		var routes = repository.routes();

		var metrics = new ArrayList<>(List.of(
				new OpsMetric("Utilities", utilities.size()),
				new OpsMetric("Programs", programs.size()),
				new OpsMetric("Cost records", costs.size()),
				new OpsMetric("Sources", sources.size()),
				new OpsMetric("Routes", routes.size())
		));

		var groups = new ArrayList<OpsReviewGroup>();
		addGroupIfEntries(groups, narrativeInventoryGroup(utilities));
		addGroupIfEntries(groups, lowConfidenceCostGroup());
		addGroupIfEntries(groups, missingCostGroup(utilities));
		addGroupIfEntries(groups, staleRecordGroup(utilities, programs, costs));
		addGroupIfEntries(groups, staleSourceGroup(sources));
		addGroupIfEntries(groups, programLinkGroup(utilities));
		var lookupEvents = readLookupEvents();
		if (!lookupEvents.isEmpty()) {
			metrics.add(new OpsMetric("Lookup events", lookupEvents.size()));
			metrics.add(new OpsMetric("Ambiguous lookups", (int) lookupEvents.stream().filter(LookupEventRecord::ambiguous).count()));
			metrics.add(new OpsMetric("No-match lookups", (int) lookupEvents.stream()
					.filter(event -> !hasText(event.matchedUtilityId()) && !"No lookup attempted".equals(event.confidenceLabel()))
					.count()));
			addGroupIfEntries(groups, lookupAmbiguityGroup(lookupEvents));
			addGroupIfEntries(groups, lookupNoMatchGroup(lookupEvents));
		}

		return new OpsReviewSnapshotModel(
				OffsetDateTime.now().toString(),
				List.copyOf(metrics),
				List.copyOf(groups)
		);
	}

	private List<SourceEvidenceRecord> buildRouteSources(
			UtilityPageSection section,
			UtilityRecord utility,
			List<ProgramRecord> programs,
			owner.leadserviceline.data.CostRecord cost
	) {
		var refs = new LinkedHashSet<String>();
		refs.addAll(utility.sourceRefs());
		if (section == UtilityPageSection.PROGRAM || section == UtilityPageSection.REPLACEMENT_COST) {
			programs.stream()
					.flatMap(program -> program.sourceRefs().stream())
					.forEach(refs::add);
		}
		if (section == UtilityPageSection.REPLACEMENT_COST && cost != null) {
			refs.addAll(cost.sourceRefs());
		}
		return repository.sourcesForRefs(refs);
	}

	private List<UtilityDecisionFact> buildKeyFacts(
			UtilityPageSection section,
			UtilityRecord utility,
			RouteRecord route,
			List<ProgramRecord> programs,
			owner.leadserviceline.data.CostRecord cost
	) {
		var facts = new ArrayList<UtilityDecisionFact>();
		switch (section) {
			case OVERVIEW -> {
				facts.add(new UtilityDecisionFact(
						"Inventory status",
						utility.inventoryStatus(),
						inventoryEvidenceSummary(utility)
				));
				facts.add(new UtilityDecisionFact(
						"Address confirmation path",
						lookupModeLabel(utility),
						"Official lookup: " + utility.lookupUrl()
				));
				facts.add(new UtilityDecisionFact(
						"Notice path",
						hasText(utility.notificationUrl()) ? "Utility notice guidance published" : "No standalone notice page loaded",
						hasText(utility.notificationUrl()) ? utility.notificationUrl() : utility.inventoryUrl()
				));
				facts.add(new UtilityDecisionFact(
						"Replacement support",
						programs.isEmpty() ? "No verified utility-linked program loaded" : programs.size() + " verified replacement path(s)",
						programs.isEmpty() ? "Stay source-first and confirm the utility path before promising funding." : programNameSummary(programs)
				));
				facts.add(new UtilityDecisionFact(
						"Cost route status",
						cost == null ? "No local cost record loaded" : costConfidenceLabel(cost, route),
						cost == null ? "Do not generalize a national replacement number onto this utility." : "Public/private assumptions stay on the replacement-cost route."
				));
			}
			case NOTIFICATION -> {
				facts.add(new UtilityDecisionFact(
						"Official notice page",
						hasText(utility.notificationUrl()) ? "Published utility notice path" : "No standalone notice page loaded",
						hasText(utility.notificationUrl()) ? utility.notificationUrl() : utility.inventoryUrl()
				));
				facts.add(new UtilityDecisionFact(
						"Current inventory status",
						utility.inventoryStatus(),
						inventoryEvidenceSummary(utility)
				));
				facts.add(new UtilityDecisionFact(
						"Address confirmation step",
						lookupModeLabel(utility),
						"Use the utility checker before treating this notice as parcel-level certainty."
				));
				facts.add(new UtilityDecisionFact(
						"Published line-count depth",
						utility.lineCounts() != null && utility.lineCounts().hasCompleteBreakdown() ? "Structured line counts published" : "Narrative-only utility summary",
						inventoryEvidenceSummary(utility)
				));
				facts.add(new UtilityDecisionFact(
						"Replacement path after notice",
						programs.isEmpty() ? "No verified replacement program loaded" : programs.size() + " verified local replacement path(s)",
						programs.isEmpty() ? "Move from notice to utility lookup before discussing funding." : programNameSummary(programs)
				));
			}
			case PROGRAM -> {
				facts.add(new UtilityDecisionFact(
						"Program count",
						programs.isEmpty() ? "No verified program record" : programs.size() + " verified program record(s)",
						programs.isEmpty() ? "Stay source-first and avoid assuming free replacement." : programNameSummary(programs)
				));
				facts.add(new UtilityDecisionFact(
						"Public/private split",
						programs.isEmpty() ? "Coverage split not verified" : "Coverage differs by program and side of line",
						programs.isEmpty() ? "No verified utility-linked program record is loaded yet." : "Check each program below instead of flattening coverage into a single claim."
				));
				facts.add(new UtilityDecisionFact(
						"Address confirmation path",
						lookupModeLabel(utility),
						"Make sure the property falls inside the utility's actual program geography before applying."
				));
				facts.add(new UtilityDecisionFact(
						"Notice and inventory context",
						utility.inventoryStatus(),
						"Use the utility inventory and notice language to decide whether replacement timing is urgent."
				));
			}
			case REPLACEMENT_COST -> {
				facts.add(new UtilityDecisionFact(
						"Cost confidence",
						cost == null ? "No local cost record loaded" : costConfidenceLabel(cost, route),
						cost == null ? "Without a local cost record, this utility should not inherit a generic bid range." : "Indexing stays route-level and evidence-based."
				));
				facts.add(new UtilityDecisionFact(
						"Housing assumption",
						cost == null ? "No assumption published" : safe(cost.housingTypeAssumption()),
						cost == null ? "Replacement scope depends on property type and line length." : "Read this before comparing contractor quotes."
				));
				facts.add(new UtilityDecisionFact(
						"Permit and restoration",
						cost == null ? "No permit/restoration assumption loaded" : safe(cost.permitAssumption()),
						cost == null ? "" : safe(cost.restorationAssumption())
				));
				facts.add(new UtilityDecisionFact(
						"Methodology basis",
						cost == null ? "No local methodology summary loaded" : fallback(cost.methodologySummary(), "No local methodology summary loaded"),
						cost == null ? "" : "This explains why the estimate is local enough to publish or why it still stays noindex."
				));
				facts.add(new UtilityDecisionFact(
						"Owner payment trigger",
						cost == null ? "No owner-payment trigger loaded" : fallback(cost.ownerTriggerSummary(), "No owner-payment trigger loaded"),
						cost == null ? "" : "Use this before treating the private-side band as an immediate out-of-pocket obligation."
				));
				facts.add(new UtilityDecisionFact(
						"Program offsets",
						programs.isEmpty() ? "No verified offset program loaded" : programs.size() + " verified offset program(s)",
						programs.isEmpty() ? "Do not assume the owner pays the full private-side band if the utility later publishes support." : programNameSummary(programs)
				));
			}
			default -> {
			}
		}
		return List.copyOf(facts);
	}

	private List<String> buildRouteCautions(
			UtilityPageSection section,
			UtilityRecord utility,
			RouteRecord route,
			List<ProgramRecord> programs,
			owner.leadserviceline.data.CostRecord cost
	) {
		var cautions = new ArrayList<String>();
		switch (section) {
			case OVERVIEW -> {
				cautions.add("Use the utility page and official lookup first. Cost or support pages should not outrank the utility record.");
				cautions.add("Keep public-side responsibility and private-side responsibility separate from the start.");
				if (utility.lineCounts() == null || !utility.lineCounts().hasCompleteBreakdown()) {
					cautions.add("This utility is still partly narrative-only, so address-level confirmation matters more than generic interpretation.");
				}
			}
			case NOTIFICATION -> {
				cautions.add("Known, potential, and unknown mean whatever this utility says they mean. Do not import another utility's definitions.");
				cautions.add("A notice is not parcel certainty unless the utility lookup or map confirms the specific address.");
				cautions.add("Filter and testing are interim steps, not equal substitutes for replacement when a local replacement path exists.");
			}
			case PROGRAM -> {
				cautions.add("Coverage can diverge sharply between public-side and private-side replacement, even inside the same program.");
				cautions.add("Income, property, and contractor rules can block a program that otherwise sounds broad.");
				if (programs.isEmpty()) {
					cautions.add("No verified utility-linked program record is loaded here, so avoid promising funding or free replacement.");
				}
			}
			case REPLACEMENT_COST -> {
				cautions.add("Cost bands are assumptions, not bids. They should never be used as a substitute for a local quote.");
				cautions.add("Permit, restoration, and housing assumptions can shift who pays and how wide the final range becomes.");
				if (cost != null && !route.indexable()) {
					cautions.add(cost.needsMethodologyHardening()
							? "This cost route stays noindex because the medium-confidence methodology still needs stronger local evidence."
							: "This cost route stays noindex because the current local evidence is still low confidence.");
				}
				if (!programs.isEmpty()) {
					cautions.add("Check verified replacement programs before treating the private-side band as an out-of-pocket obligation.");
				}
			}
			default -> {
			}
		}
		return List.copyOf(cautions);
	}

	private List<UtilityProgramSummary> buildProgramSummaries(List<ProgramRecord> programs) {
		return programs.stream()
				.map(program -> new UtilityProgramSummary(
						program.programName(),
						safe(program.subsidyType()),
						safe(program.publicSideCovered()),
						safe(program.privateSideCovered()),
						fallback(program.incomeRules(), "No income rule published"),
						fallback(program.propertyRules(), "No property rule published"),
						fallback(program.contractorRules(), "No contractor restriction published"),
						program.deadline() == null ? "No fixed deadline published" : "Deadline " + program.deadline(),
						program.applicationUrl(),
						"Verified " + program.lastVerified() + " / " + program.verificationStatus()
				))
				.toList();
	}

	private List<UtilityCostResponsibility> buildCostResponsibilities(
			owner.leadserviceline.data.CostRecord cost,
			List<ProgramRecord> programs
	) {
		if (cost == null) {
			return List.of();
		}
		var responsibilities = new ArrayList<UtilityCostResponsibility>();
		responsibilities.add(new UtilityCostResponsibility(
				"Public side",
				fallback(cost.publicSideBand(), "No public-side band loaded"),
				"Utility-side work may follow a different funding path than homeowner-side work."
		));
		responsibilities.add(new UtilityCostResponsibility(
				"Private side",
				fallback(cost.privateSideBand(), "No private-side band loaded"),
				"Use the private-side band only after checking permit, restoration, and utility support rules."
		));
		responsibilities.add(new UtilityCostResponsibility(
				"Full replacement",
				fallback(cost.fullReplacementBand(), "No full-replacement band loaded"),
				"Treat this as a combined scenario, not as proof that one party will pay the whole amount."
		));
		responsibilities.add(new UtilityCostResponsibility(
				"Program offset",
				programs.isEmpty() ? "No verified utility-linked program loaded" : programNameSummary(programs),
				programs.isEmpty()
						? "No verified offset is loaded, so keep responsibility assumptions explicit."
						: "Verified program support can change who actually bears the private-side cost."
		));
		return List.copyOf(responsibilities);
	}

	private String buildPageTitle(UtilityPageSection section, UtilityRecord utility) {
		return switch (section) {
			case OVERVIEW -> utility.utilityName() + " lead service line overview in " + utility.city() + ", " + utility.state().toUpperCase(Locale.US);
			case NOTIFICATION -> utility.utilityName() + " lead service line notice guide in " + utility.city() + ", " + utility.state().toUpperCase(Locale.US);
			case PROGRAM -> utility.utilityName() + " lead service line replacement programs in " + utility.city() + ", " + utility.state().toUpperCase(Locale.US);
			case REPLACEMENT_COST -> utility.utilityName() + " lead service line replacement cost in " + utility.city() + ", " + utility.state().toUpperCase(Locale.US);
			case FILTER_AND_TESTING -> utility.utilityName() + " lead service line filter and testing guidance";
			case BUYER_SELLER -> utility.utilityName() + " lead service line buyer and seller checklist";
		};
	}

	private OpsReviewGroup narrativeInventoryGroup(List<UtilityRecord> utilities) {
		var entries = utilities.stream()
				.filter(utility -> utility.lineCounts() == null || !utility.lineCounts().hasCompleteBreakdown())
				.map(utility -> new OpsReviewEntry(
						utility.utilityName(),
						"Utility page still relies on narrative inventory summary instead of a four-bucket count breakdown.",
						buildUtilityPath(utility.state(), utility.city(), utility.utilitySlug(), null),
						utility.inventoryUrl()
				))
				.toList();
		return new OpsReviewGroup(
				"warning",
				"Narrative-only inventory records",
				"These utilities are publishable, but ops should look for a structured line-count breakdown whenever the official source exposes one.",
				entries
		);
	}

	private OpsReviewGroup lowConfidenceCostGroup() {
		var entries = repository.costs().stream()
				.filter(cost -> !cost.shouldIndexRoute())
				.map(cost -> repository.findUtilityById(cost.utilityId())
						.map(utility -> new OpsReviewEntry(
								utility.utilityName(),
								cost.needsMethodologyHardening()
										? "Replacement-cost route is held at noindex until medium-confidence methodology evidence is stronger."
										: "Replacement-cost route is held at noindex until the methodology and evidence quality improve.",
								buildUtilityPath(utility.state(), utility.city(), utility.utilitySlug(), "replacement-cost"),
								firstSourceUrl(cost.sourceRefs()).orElse(null)
						)))
				.flatMap(Optional::stream)
				.sorted(Comparator.comparing(OpsReviewEntry::label))
				.toList();
		return new OpsReviewGroup(
				"critical",
				"Cost routes held out of index",
				"These cost pages are intentionally held back from index because confidence or methodology quality is still too weak for public SEO delivery.",
				entries
		);
	}

	private OpsReviewGroup missingCostGroup(List<UtilityRecord> utilities) {
		var entries = utilities.stream()
				.filter(utility -> repository.costForUtility(utility.utilityId()).isEmpty())
				.map(utility -> new OpsReviewEntry(
						utility.utilityName(),
						"No local cost methodology record is loaded yet for this utility.",
						buildUtilityPath(utility.state(), utility.city(), utility.utilitySlug(), null),
						utility.inventoryUrl()
				))
				.toList();
		return new OpsReviewGroup(
				"info",
				"Utilities without a cost methodology record",
				"These utilities can still ship inventory and program guidance, but the replacement-cost route is missing or intentionally deferred.",
				entries
		);
	}

	private OpsReviewGroup staleRecordGroup(
			List<UtilityRecord> utilities,
			List<ProgramRecord> programs,
			List<owner.leadserviceline.data.CostRecord> costs
	) {
		var today = LocalDate.now();
		var entries = new ArrayList<OpsReviewEntry>();

		utilities.stream()
				.filter(utility -> utility.lastVerified() != null && ageInDays(utility.lastVerified(), today) > RECORD_REVIEW_WARNING_DAYS)
				.map(utility -> new OpsReviewEntry(
						utility.utilityName(),
						"Utility record has not been re-verified in " + ageInDays(utility.lastVerified(), today) + " days.",
						buildUtilityPath(utility.state(), utility.city(), utility.utilitySlug(), null),
						utility.inventoryUrl()
				))
				.forEach(entries::add);

		programs.stream()
				.filter(program -> program.lastVerified() != null && ageInDays(program.lastVerified(), today) > RECORD_REVIEW_WARNING_DAYS)
				.map(program -> new OpsReviewEntry(
						program.programName(),
						"Program record has not been re-verified in " + ageInDays(program.lastVerified(), today) + " days.",
						programLocalPath(program),
						program.applicationUrl()
				))
				.forEach(entries::add);

		costs.stream()
				.filter(cost -> cost.lastVerified() != null && ageInDays(cost.lastVerified(), today) > RECORD_REVIEW_WARNING_DAYS)
				.map(cost -> repository.findUtilityById(cost.utilityId())
						.map(utility -> new OpsReviewEntry(
								utility.utilityName(),
								"Cost record has not been re-verified in " + ageInDays(cost.lastVerified(), today) + " days.",
								buildUtilityPath(utility.state(), utility.city(), utility.utilitySlug(), "replacement-cost"),
								firstSourceUrl(cost.sourceRefs()).orElse(null)
						)))
				.flatMap(Optional::stream)
				.forEach(entries::add);

		return new OpsReviewGroup(
				"warning",
				"Stale normalized records",
				"Anything older than the current re-verification window should move into the manual review queue.",
				List.copyOf(entries)
		);
	}

	private OpsReviewGroup staleSourceGroup(List<SourceEvidenceRecord> sources) {
		var today = LocalDate.now();
		var entries = sources.stream()
				.filter(source -> hasText(source.effectiveDate()) && ageInDays(LocalDate.parse(source.effectiveDate()), today) > SOURCE_REVIEW_WARNING_DAYS)
				.map(source -> new OpsReviewEntry(
						source.publisherName() + " / " + source.scopeKey(),
						"Source evidence is " + ageInDays(LocalDate.parse(source.effectiveDate()), today) + " days old and should be checked for changes.",
						scopeLocalPath(source),
						source.sourceUrl()
				))
				.toList();
		return new OpsReviewGroup(
				"warning",
				"Stale source evidence",
				"Older source evidence is not automatically wrong, but it should be manually reviewed before more pages are expanded from it.",
				entries
		);
	}

	private OpsReviewGroup programLinkGroup(List<UtilityRecord> utilities) {
		var entries = utilities.stream()
				.flatMap(utility -> utility.programIds().stream()
						.filter(programId -> repository.programsForUtility(utility.utilityId()).stream()
								.noneMatch(program -> programId.equals(program.programId())))
						.map(programId -> new OpsReviewEntry(
								utility.utilityName(),
								"Utility lists program id " + programId + " but no matching program record loaded for this utility.",
								buildUtilityPath(utility.state(), utility.city(), utility.utilitySlug(), "program"),
								utility.notificationUrl()
						)))
				.toList();
		return new OpsReviewGroup(
				"critical",
				"Broken utility-to-program links",
				"These mismatches indicate a data-contract problem between utility records and normalized program files.",
				entries
		);
	}

	private OpsReviewGroup lookupAmbiguityGroup(List<LookupEventRecord> events) {
		var entries = events.stream()
				.filter(LookupEventRecord::ambiguous)
				.collect(Collectors.groupingBy(this::lookupBucketKey))
				.values().stream()
				.sorted(Comparator.comparingInt((List<LookupEventRecord> bucket) -> bucket.size()).reversed()
						.thenComparing(bucket -> lookupBucketLabel(bucket.getFirst())))
				.limit(8)
				.map(bucket -> {
					var latest = bucket.stream()
							.max(Comparator.comparing(LookupEventRecord::timestamp))
							.orElse(bucket.getFirst());
					var detail = bucket.size() + " logged lookups stayed ambiguous in this bucket.";
					if (hasText(latest.matchedUtilityName())) {
						detail += " Most recent top match was " + latest.matchedUtilityName() + ".";
					}
					return new OpsReviewEntry(
							lookupBucketLabel(latest),
							detail,
							hasText(latest.matchedUtilityPath()) ? latest.matchedUtilityPath() : null,
							null
					);
				})
				.toList();
		return new OpsReviewGroup(
				"warning",
				"Lookup ambiguity hot spots",
				"These city and ZIP patterns are generating close matches and should drive the next resolver alias or boundary cleanup pass.",
				entries
		);
	}

	private OpsReviewGroup lookupNoMatchGroup(List<LookupEventRecord> events) {
		var entries = events.stream()
				.filter(event -> !hasText(event.matchedUtilityId()) && !"No lookup attempted".equals(event.confidenceLabel()))
				.collect(Collectors.groupingBy(this::lookupBucketKey))
				.values().stream()
				.sorted(Comparator.comparingInt((List<LookupEventRecord> bucket) -> bucket.size()).reversed()
						.thenComparing(bucket -> lookupBucketLabel(bucket.getFirst())))
				.limit(8)
				.map(bucket -> new OpsReviewEntry(
						lookupBucketLabel(bucket.getFirst()),
						bucket.size() + " logged lookups ended with no utility match in this bucket.",
						null,
						null
				))
				.toList();
		return new OpsReviewGroup(
				"critical",
				"No-match lookup requests",
				"These misses indicate resolver gaps or missing utility coverage in the current seed footprint.",
				entries
		);
	}

	private UtilityLookupMatch scoreUtilityLookup(
			UtilityRecord utility,
			String queryText,
			String explicitNormalizedCity,
			String resolvedNormalizedCity,
			String normalizedState,
			String resolvedPostalPrefix
	) {
		var score = 0;
		var reasons = new ArrayList<String>();
		var normalizedQuery = normalizeLookupText(queryText);
		var hasExplicitCity = hasText(explicitNormalizedCity);
		var usingGeocodedCity = !hasExplicitCity && hasText(resolvedNormalizedCity);
		var postalPrefix = hasText(resolvedPostalPrefix) ? resolvedPostalPrefix : extractPostalPrefix(queryText);
		var normalizedUtilityCity = normalizeLookupText(utility.city());
		var normalizedCounty = normalizeLookupText(utility.county());
		var normalizedServiceAreaAlias = normalizeLookupAlias(utility.serviceAreaName());
		var utilityStateName = STATE_NAMES.getOrDefault(utility.state(), utility.state());
		var municipalityMatch = false;
		var utilityIdentityMatch = false;
		var postalMatch = false;
		var cityMatch = false;
		var broadCoverageMatch = false;
		var countyMatch = false;

		if (hasText(normalizedState)) {
			if (!normalizedState.equals(utility.state())) {
				return new UtilityLookupMatch(utility, buildUtilityPath(utility.state(), utility.city(), utility.utilitySlug(), null), 0, "No match", true, List.of());
			}
			score += 40;
			reasons.add("State matches " + utility.state().toUpperCase(Locale.US));
		} else if (containsWholePhrase(normalizedQuery, utilityStateName) || containsWholePhrase(normalizedQuery, utility.state())) {
			score += 20;
			reasons.add("State appears in the lookup text");
		}

		if (hasText(resolvedNormalizedCity)) {
			if (resolvedNormalizedCity.equals(normalizedUtilityCity) || matchesExactPlace(resolvedNormalizedCity, utility.city())) {
				score += usingGeocodedCity ? 55 : 70;
				reasons.add((usingGeocodedCity ? "Geocoder city matches " : "City matches ") + utility.city());
				cityMatch = true;
			} else if (containsWholePhrase(resolvedNormalizedCity, utility.city())) {
				score += usingGeocodedCity ? 25 : 35;
				reasons.add(usingGeocodedCity ? "Geocoder city points to " + utility.city() : "City appears in the lookup text");
				cityMatch = true;
			}
		} else if (containsWholePhrase(normalizedQuery, utility.city())) {
			score += 55;
			reasons.add("Utility city appears in the lookup text");
			cityMatch = true;
		}

		var explicitMunicipality = findExactPlaceMatch(explicitNormalizedCity, utility.resolverMunicipalities())
				.or(() -> findMatchingPhrase(explicitNormalizedCity, utility.resolverMunicipalities()));
		if (explicitMunicipality.isPresent()) {
			score += 75;
			reasons.add("Service-area municipality matches " + explicitMunicipality.get());
			municipalityMatch = true;
		} else {
			var geocodedMunicipality = usingGeocodedCity
					? findExactPlaceMatch(resolvedNormalizedCity, utility.resolverMunicipalities())
							.or(() -> findMatchingPhrase(resolvedNormalizedCity, utility.resolverMunicipalities()))
					: Optional.<String>empty();
			if (geocodedMunicipality.isPresent()) {
				score += 55;
				reasons.add("Geocoder municipality matches " + geocodedMunicipality.get());
				municipalityMatch = true;
			} else {
				var queryMunicipality = findMatchingPhrase(normalizedQuery, utility.resolverMunicipalities());
				if (queryMunicipality.isPresent()) {
					score += 45;
					reasons.add("Service-area municipality appears in the lookup text");
					municipalityMatch = true;
				}
			}
		}

		if (containsWholePhrase(normalizedQuery, utility.utilityName())) {
			score += 80;
			reasons.add("Utility name matches");
			utilityIdentityMatch = true;
		} else if (matchesNormalizedUtilityIdentity(normalizedQuery, utility.utilityName(), utility)) {
			score += 45;
			reasons.add("Utility name matches a normalized utility alias");
			utilityIdentityMatch = true;
		}

		if (hasText(postalPrefix) && utility.resolverPostalPrefixes().contains(postalPrefix)) {
			score += 65;
			reasons.add("Postal prefix matches " + postalPrefix);
			postalMatch = true;
		}

		for (var alias : utility.aliases()) {
			if (containsWholePhrase(normalizedQuery, alias)) {
				score += 65;
				reasons.add("Alias matches " + alias);
				utilityIdentityMatch = true;
				break;
			}
			if (matchesNormalizedUtilityIdentity(normalizedQuery, alias, utility)) {
				score += 28;
				reasons.add("Alias matches a normalized utility alias");
				utilityIdentityMatch = true;
				break;
			}
		}

		if (hasText(utility.serviceAreaName()) && containsWholePhrase(normalizedQuery, normalizedServiceAreaAlias)) {
			score += 10;
			reasons.add("Service-area name matches a normalized alias");
			broadCoverageMatch = true;
		}

		if (containsWholePhrase(normalizedQuery, utility.serviceAreaName())) {
			score += 18;
			reasons.add("Service area matches");
			broadCoverageMatch = true;
		}

		for (var hint : utility.resolverHints()) {
			if (containsWholePhrase(normalizedQuery, hint)) {
				score += 18;
				reasons.add("Service area hint matches " + hint);
				broadCoverageMatch = true;
				break;
			}
		}

		if (containsWholePhrase(normalizedQuery, normalizedCounty)) {
			score += 6;
			reasons.add("County appears in the lookup text");
			countyMatch = true;
		}

		score += lookupModeBonus(utility.addressLookupMode());
		if (lookupModeRank(utility.addressLookupMode()) > 0) {
			reasons.add("Official lookup path preferred");
		}

		if (hasText(explicitNormalizedCity) && !cityMatch && !municipalityMatch && !utilityIdentityMatch && !postalMatch) {
			if (broadCoverageMatch) {
				score -= 55;
				reasons.add("Broad service-area hints are weaker than the explicit city");
			} else {
				score -= 35;
				reasons.add("Explicit city does not clearly match this service area");
			}
		}

		score = Math.max(score, 0);
		var manualReviewRecommended = !cityMatch && !municipalityMatch && !utilityIdentityMatch && !postalMatch;
		if (!manualReviewRecommended && broadCoverageMatch && !cityMatch && !municipalityMatch && score < 105) {
			manualReviewRecommended = true;
		}
		if (!manualReviewRecommended && countyMatch && !cityMatch && !municipalityMatch && !postalMatch && score < 85) {
			manualReviewRecommended = true;
		}
		var confidenceLabel = buildLookupConfidenceLabel(score, cityMatch, municipalityMatch, utilityIdentityMatch, postalMatch, manualReviewRecommended);

		return new UtilityLookupMatch(
				utility,
				buildUtilityPath(utility.state(), utility.city(), utility.utilitySlug(), null),
				score,
				confidenceLabel,
				manualReviewRecommended,
				List.copyOf(reasons)
		);
	}

	private String buildHeroTitle(UtilityPageSection section, UtilityRecord utility) {
		return switch (section) {
			case OVERVIEW -> utility.utilityName() + " lead service line verdict";
			case NOTIFICATION -> "What the " + utility.utilityName() + " notice means";
			case PROGRAM -> utility.utilityName() + " replacement programs";
			case REPLACEMENT_COST -> utility.utilityName() + " replacement cost assumptions";
			case FILTER_AND_TESTING -> utility.utilityName() + " filter and testing guidance";
			case BUYER_SELLER -> utility.utilityName() + " buyer and seller checklist";
		};
	}

	private String buildHeroSummary(UtilityPageSection section, UtilityRecord utility, List<ProgramRecord> programs) {
		return switch (section) {
			case OVERVIEW -> "Start with " + utility.utilityName() + "'s own inventory status, address lookup, and utility-specific next step before using any cost or support page.";
			case NOTIFICATION -> "Read " + utility.utilityName() + "'s exact notice and inventory language without collapsing service line risk into interior plumbing or fixture claims.";
			case PROGRAM -> programs.isEmpty()
					? "No verified utility-linked program is loaded yet, so this route stays source-first and avoids funding or eligibility overclaims."
					: "Compare verified public-side and private-side coverage, eligibility rules, and application friction for " + utility.utilityName() + ".";
			case REPLACEMENT_COST -> "Keep local replacement responsibility, permit friction, restoration scope, and utility support in view before treating any band as a real quote.";
			case FILTER_AND_TESTING -> "Use this only as an interim protection layer when replacement is not immediate and the utility-specific next step is still unresolved.";
			case BUYER_SELLER -> "Use this only when a real transaction is in motion and the utility's own inventory, notice, and cost evidence are already clear.";
		};
	}

	private List<String> buildNextSteps(UtilityPageSection section, UtilityRecord utility, List<ProgramRecord> programs) {
		var steps = new ArrayList<String>();
		switch (section) {
			case OVERVIEW -> {
				steps.add("Use the official utility lookup before making any replacement decision.");
				steps.add("Separate public-side responsibility from private-side responsibility.");
				steps.add("Use a program, cost, filter, or transaction page only after the inventory path is clear.");
			}
			case NOTIFICATION -> {
				steps.add("Check the exact notice language against the official utility page.");
				steps.add("Do not treat a potential line notice as proof of parcel-level certainty unless the source says so.");
				steps.add("If replacement is not immediate, use the interim protection route next.");
			}
			case PROGRAM -> {
				if (programs.isEmpty()) {
					steps.add("Follow the official utility inventory and contact path first.");
					steps.add("Do not promise funding or eligibility without a verified program record.");
				} else {
					steps.add("Confirm whether the utility covers the public side, private side, or both.");
					steps.add("Check deadline, property rules, and contractor restrictions before requesting quotes.");
				}
			}
			case REPLACEMENT_COST -> {
				steps.add("Treat cost bands as estimates tied to local assumptions, not promises.");
				steps.add("Confirm whether permit, restoration, and driveway work are included.");
				if (!programs.isEmpty()) {
					steps.add("Cross-check the cost route against any verified replacement program.");
				}
			}
			case FILTER_AND_TESTING -> {
				steps.add("Use a certified filter as an interim protection step, not as a replacement for line replacement.");
				steps.add("Use testing when it changes the next action or clarifies uncertainty.");
				steps.add("Return to the utility lookup if the service-line status is still unclear.");
			}
			case BUYER_SELLER -> {
				steps.add("Tie negotiation language to the utility inventory and notice status, not generic plumbing language.");
				steps.add("Use replacement cost assumptions to frame credits or escrow timing.");
				steps.add("Keep disclosure wording specific to the utility record and current evidence.");
			}
		}
		if (steps.isEmpty()) {
			steps.add("Use the official utility inventory as the first source of truth.");
		}
		return List.copyOf(steps);
	}

	private int routeOrder(RouteRecord route) {
		return switch (UtilityPageSection.fromRouteTemplate(route.template())) {
			case OVERVIEW -> 0;
			case NOTIFICATION -> 1;
			case PROGRAM -> 2;
			case REPLACEMENT_COST -> 3;
			case FILTER_AND_TESTING -> 4;
			case BUYER_SELLER -> 5;
			default -> 100;
		};
	}

	private String buildUtilityPath(String state, String city, String utilitySlug, String section) {
		var basePath = "/lead-service-line/%s/%s/%s".formatted(
				normalizeSegment(state),
				normalizeSegment(city),
				normalizeSegment(utilitySlug)
		);
		if (section == null || section.isBlank()) {
			return basePath;
		}
		return basePath + "/" + normalizeSegment(section);
	}

	private String normalizeSegment(String value) {
		return value.trim().toLowerCase(Locale.US);
	}

	private String normalizeLookupText(String value) {
		if (!hasText(value)) {
			return "";
		}
		return value.toLowerCase(Locale.US)
				.replaceAll("[^a-z0-9]+", " ")
				.replaceAll("\\s+", " ")
				.trim();
	}

	private String normalizeLookupAlias(String value) {
		var normalized = normalizeLookupText(value);
		if (!hasText(normalized)) {
			return "";
		}
		return normalized
				.replaceAll("\\b(city|town|village|borough|department|public|utility|utilities|water|works|board|division|bureau|production|sewer|sewerage|services?|district|authority|and|of|the)\\b", " ")
				.replaceAll("\\s+", " ")
				.trim();
	}

	private boolean matchesNormalizedUtilityIdentity(String haystack, String value, UtilityRecord utility) {
		var normalizedIdentity = normalizeLookupAlias(value);
		if (!hasText(haystack) || !hasText(normalizedIdentity) || normalizedIdentity.length() < 5) {
			return false;
		}
		if (normalizedIdentity.equals(normalizeLookupText(utility.city()))) {
			return false;
		}
		if (hasText(utility.serviceAreaName()) && normalizedIdentity.equals(normalizeLookupAlias(utility.serviceAreaName()))) {
			return false;
		}
		return (" " + normalizeLookupText(haystack) + " ").contains(" " + normalizedIdentity + " ")
				|| (" " + normalizePlaceText(haystack) + " ").contains(" " + normalizePlaceText(normalizedIdentity) + " ");
	}

	private boolean matchesExactPlace(String left, String right) {
		return hasText(left)
				&& hasText(right)
				&& normalizePlaceText(left).equals(normalizePlaceText(right));
	}

	private boolean containsWholePhrase(String haystack, String needle) {
		var normalizedNeedle = normalizeLookupText(needle);
		if (!hasText(haystack) || !hasText(normalizedNeedle)) {
			return false;
		}
		var normalizedHaystack = normalizeLookupText(haystack);
		if ((" " + normalizedHaystack + " ").contains(" " + normalizedNeedle + " ")) {
			return true;
		}
		var normalizedPlaceHaystack = normalizePlaceText(haystack);
		var normalizedPlaceNeedle = normalizePlaceText(needle);
		return hasText(normalizedPlaceNeedle)
				&& (" " + normalizedPlaceHaystack + " ").contains(" " + normalizedPlaceNeedle + " ");
	}

	private Optional<String> findMatchingPhrase(String haystack, List<String> phrases) {
		if (!hasText(haystack)) {
			return Optional.empty();
		}
		return phrases.stream()
				.filter(phrase -> containsWholePhrase(haystack, phrase))
				.findFirst();
	}

	private Optional<String> findExactPlaceMatch(String haystack, List<String> phrases) {
		if (!hasText(haystack)) {
			return Optional.empty();
		}
		return phrases.stream()
				.filter(phrase -> matchesExactPlace(haystack, phrase))
				.findFirst();
	}

	private String buildLookupConfidenceLabel(
			int score,
			boolean cityMatch,
			boolean municipalityMatch,
			boolean utilityIdentityMatch,
			boolean postalMatch,
			boolean manualReviewRecommended
	) {
		if ((cityMatch && postalMatch)
				|| (municipalityMatch && postalMatch)
				|| (municipalityMatch && utilityIdentityMatch)
				|| (cityMatch && score >= 110)
				|| (municipalityMatch && score >= 120)
				|| score >= 150) {
			return "High-confidence match";
		}
		if (!manualReviewRecommended && (cityMatch || municipalityMatch || utilityIdentityMatch || postalMatch)) {
			return "Likely utility match";
		}
		return "Broad-area match";
	}

	private String buildLookupSummary(List<UtilityLookupMatch> matches, boolean ambiguous, boolean explicitCityProvided, boolean geocoderUsed) {
		if (matches.isEmpty()) {
			return "";
		}
		if (ambiguous) {
			if (explicitCityProvided) {
				return "That city still maps to multiple close utility candidates. Confirm the official lookup before using any local program or cost guidance.";
			}
			if (geocoderUsed) {
				return "The normalized address still maps to multiple close utility candidates. Confirm the official lookup before using any local program or cost guidance.";
			}
			return "Top matches are close together. Add an exact city, ZIP, or utility name before relying on any local program or cost guidance.";
		}
		if (matches.get(0).manualReviewRecommended()) {
			if (explicitCityProvided) {
				return "That city did not create a clean utility handoff. Add a ZIP code or utility name for a tighter match.";
			}
			return "These results are broad-area matches. Add an exact municipality or ZIP code for a tighter utility handoff.";
		}
		if (geocoderUsed) {
			return "Top result has a strong city, municipality, utility-name, or postal signal after address normalization. Use the official lookup next to confirm the address.";
		}
		return "Top result has a strong city, municipality, utility-name, or postal signal. Use the official lookup next to confirm the address.";
	}

	private String buildUtilitySeoDescription(UtilityPageModel page) {
		var utility = page.utility();
		return switch (page.section()) {
			case OVERVIEW -> utility.utilityName() + " lead service line overview for " + utility.city() + ", " + utility.state().toUpperCase(Locale.US)
					+ ". Check inventory status, official lookup, local notice context, and next-step guidance.";
			case NOTIFICATION -> "Read " + utility.utilityName() + " notice language for " + utility.city() + ", " + utility.state().toUpperCase(Locale.US)
					+ ". Understand known, potential, or unknown line status and what to do next.";
			case PROGRAM -> utility.utilityName() + " replacement program details for " + utility.city() + ", " + utility.state().toUpperCase(Locale.US)
					+ ". Compare public-side and private-side coverage, eligibility, and application friction.";
			case REPLACEMENT_COST -> utility.utilityName() + " replacement cost guidance for " + utility.city() + ", " + utility.state().toUpperCase(Locale.US)
					+ ". Separate public-side and private-side responsibility and keep local assumptions visible.";
			case FILTER_AND_TESTING -> utility.utilityName() + " interim filter and testing guidance when replacement cannot happen immediately.";
			case BUYER_SELLER -> utility.utilityName() + " buyer and seller checklist for lead service line notices, responsibility, and timing.";
		};
	}

	private List<LookupEventRecord> readLookupEvents() {
		var logPathValue = lookupLoggingProperties.lookupLogPath();
		if (!hasText(logPathValue)) {
			return List.of();
		}
		var logPath = Path.of(logPathValue);
		if (!Files.exists(logPath)) {
			return List.of();
		}
		try (var lines = Files.lines(logPath)) {
			return lines
					.map(String::trim)
					.filter(this::hasText)
					.map(this::readLookupEvent)
					.flatMap(Optional::stream)
					.toList();
		} catch (IOException exception) {
			LOGGER.warn("Unable to read lookup log file {}", logPath, exception);
			return List.of();
		}
	}

	private Optional<LookupEventRecord> readLookupEvent(String line) {
		try {
			return Optional.of(objectMapper.readValue(line, LookupEventRecord.class));
		} catch (IOException exception) {
			LOGGER.warn("Skipping unreadable lookup log line");
			return Optional.empty();
		}
	}

	private String lookupBucketKey(LookupEventRecord event) {
		if (hasText(event.lookupBucketKey())) {
			return event.lookupBucketKey();
		}
		if (hasText(event.city()) && hasText(event.state())) {
			return "city:" + normalizeLookupText(event.city()) + "|" + event.state().toLowerCase(Locale.US);
		}
		if (hasText(event.city())) {
			return "city:" + normalizeLookupText(event.city());
		}
		if (hasText(event.query())) {
			return "query:" + normalizeLookupText(event.query());
		}
		return "unknown";
	}

	private String lookupBucketLabel(LookupEventRecord event) {
		if (hasText(event.lookupLabel())) {
			return event.lookupLabel();
		}
		if (hasText(event.city()) && hasText(event.state())) {
			return event.city() + ", " + event.state().toUpperCase(Locale.US);
		}
		if (hasText(event.city())) {
			return event.city();
		}
		if (hasText(event.query())) {
			return event.query();
		}
		return "Unspecified lookup";
	}

	private String absoluteUrl(String path) {
		var baseUrl = siteRuntimeProperties.siteBaseUrl();
		var normalizedBaseUrl = hasText(baseUrl) ? baseUrl.trim() : "http://localhost:8080";
		if (normalizedBaseUrl.endsWith("/")) {
			normalizedBaseUrl = normalizedBaseUrl.substring(0, normalizedBaseUrl.length() - 1);
		}
		var normalizedPath = path.startsWith("/") ? path : "/" + path;
		return normalizedBaseUrl + normalizedPath;
	}

	private Map<String, Object> breadcrumbData(List<Map.Entry<String, String>> crumbs) {
		var items = new ArrayList<Map<String, Object>>();
		for (int index = 0; index < crumbs.size(); index++) {
			var crumb = crumbs.get(index);
			items.add(Map.of(
					"@type", "ListItem",
					"position", index + 1,
					"name", crumb.getKey(),
					"item", crumb.getValue()
			));
		}
		return Map.of(
				"@context", "https://schema.org",
				"@type", "BreadcrumbList",
				"itemListElement", items
		);
	}

	private String toJsonLd(Map<String, Object> data) {
		try {
			return objectMapper.writeValueAsString(data);
		} catch (IOException exception) {
			LOGGER.warn("Unable to serialize JSON-LD payload");
			return "{}";
		}
	}

	private String urlEntry(String path, String lastModified) {
		return """
				  <url>
				    <loc>%s</loc>
				    <lastmod>%s</lastmod>
				  </url>""".formatted(escapeXml(absoluteUrl(path)), escapeXml(lastModified));
	}

	private String escapeXml(String value) {
		return safe(value)
				.replace("&", "&amp;")
				.replace("<", "&lt;")
				.replace(">", "&gt;")
				.replace("\"", "&quot;")
				.replace("'", "&apos;");
	}

	private String resolveStateCode(String text) {
		var normalized = normalizeLookupText(text);
		if (!hasText(normalized)) {
			return "";
		}
		var tokens = Stream.of(normalized.split(" "))
				.filter(this::hasText)
				.collect(Collectors.toSet());
		for (var entry : STATE_NAMES.entrySet()) {
			if (tokens.contains(entry.getKey()) || containsWholePhrase(normalized, entry.getValue())) {
				return entry.getKey();
			}
		}
		return "";
	}

	private String extractPostalPrefix(String value) {
		if (!hasText(value)) {
			return "";
		}
		var matcher = java.util.regex.Pattern.compile("\\b(\\d{5})(?:-\\d{4})?\\b").matcher(value);
		if (!matcher.find()) {
			return "";
		}
		return matcher.group(1).substring(0, 3);
	}

	private boolean hasText(String value) {
		return value != null && !value.isBlank();
	}

	private int lookupModeBonus(String addressLookupMode) {
		return switch (addressLookupMode) {
			case "official_lookup" -> 12;
			case "service_area_notes" -> 5;
			default -> 0;
		};
	}

	private int lookupModeRank(String addressLookupMode) {
		return switch (addressLookupMode) {
			case "official_lookup" -> 2;
			case "service_area_notes" -> 1;
			default -> 0;
		};
	}

	private String normalizePlaceText(String value) {
		if (!hasText(value)) {
			return "";
		}
		return normalizeLookupText(value)
				.replaceAll("\\bst\\b", "saint")
				.replaceAll("\\bft\\b", "fort")
				.replaceAll("\\bmt\\b", "mount");
	}

	private String safe(String value) {
		return value == null ? "" : value;
	}

	private long ageInDays(LocalDate start, LocalDate end) {
		return ChronoUnit.DAYS.between(start, end);
	}

	private Optional<String> firstSourceUrl(List<String> sourceRefs) {
		return repository.sourcesForRefs(sourceRefs).stream()
				.map(SourceEvidenceRecord::sourceUrl)
				.filter(this::hasText)
				.findFirst();
	}

	private String scopeLocalPath(SourceEvidenceRecord source) {
		return switch (source.scopeType()) {
			case "utility" -> repository.findUtilityById(source.scopeKey())
					.map(utility -> buildUtilityPath(utility.state(), utility.city(), utility.utilitySlug(), null))
					.orElse("/ops/review");
			case "program" -> repository.programs().stream()
					.filter(program -> source.scopeKey().equals(program.programId()))
					.findFirst()
					.map(this::programLocalPath)
					.orElse("/ops/review");
			case "cost" -> repository.costs().stream()
					.filter(cost -> source.scopeKey().equals(cost.costId()))
					.findFirst()
					.flatMap(cost -> repository.findUtilityById(cost.utilityId()))
					.map(utility -> buildUtilityPath(utility.state(), utility.city(), utility.utilitySlug(), "replacement-cost"))
					.orElse("/ops/review");
			default -> "/ops/review";
		};
	}

	private String programLocalPath(ProgramRecord program) {
		return program.utilityIds().stream()
				.findFirst()
				.flatMap(repository::findUtilityById)
				.map(utility -> buildUtilityPath(utility.state(), utility.city(), utility.utilitySlug(), "program"))
				.orElse("/ops/review");
	}

	private void addGroupIfEntries(List<OpsReviewGroup> groups, OpsReviewGroup group) {
		if (!group.entries().isEmpty()) {
			groups.add(group);
		}
	}

	private String inventoryEvidenceSummary(UtilityRecord utility) {
		if (utility.lineCounts() != null && utility.lineCounts().hasAnyValues()) {
			var segments = new ArrayList<String>();
			if (utility.lineCounts().known() != null) {
				segments.add("Known " + utility.lineCounts().known());
			}
			if (utility.lineCounts().potential() != null) {
				segments.add("Potential " + utility.lineCounts().potential());
			}
			if (utility.lineCounts().unknown() != null) {
				segments.add("Unknown " + utility.lineCounts().unknown());
			}
			if (utility.lineCounts().nonLead() != null) {
				segments.add("Non-lead " + utility.lineCounts().nonLead());
			}
			if (!segments.isEmpty()) {
				return String.join(" / ", segments);
			}
		}
		if (hasText(utility.inventorySummary())) {
			return utility.inventorySummary();
		}
		return "No structured count breakdown is loaded for this utility yet.";
	}

	private String lookupModeLabel(UtilityRecord utility) {
		return switch (utility.addressLookupMode()) {
			case "official_lookup" -> "Official utility lookup available";
			case "service_area_notes" -> "Service-area notes only";
			default -> "Inventory page only";
		};
	}

	private String programNameSummary(List<ProgramRecord> programs) {
		if (programs.isEmpty()) {
			return "";
		}
		return programs.stream()
				.map(ProgramRecord::programName)
				.collect(Collectors.joining(", "));
	}

	private String costConfidenceLabel(owner.leadserviceline.data.CostRecord cost, RouteRecord route) {
		var confidence = safe(cost.costConfidence());
		if (!route.indexable()) {
			return confidence + " confidence / noindex";
		}
		return confidence + " confidence";
	}

	private String fallback(String value, String fallback) {
		return hasText(value) ? value : fallback;
	}

	private static Map<String, String> buildStateNames() {
		var states = new HashMap<String, String>();
		states.put("az", "arizona");
		states.put("co", "colorado");
		states.put("dc", "district of columbia");
		states.put("ia", "iowa");
		states.put("il", "illinois");
		states.put("in", "indiana");
		states.put("mi", "michigan");
		states.put("mn", "minnesota");
		states.put("mo", "missouri");
		states.put("ne", "nebraska");
		states.put("oh", "ohio");
		states.put("pa", "pennsylvania");
		states.put("ri", "rhode island");
		states.put("wi", "wisconsin");
		return Map.copyOf(states);
	}
}
