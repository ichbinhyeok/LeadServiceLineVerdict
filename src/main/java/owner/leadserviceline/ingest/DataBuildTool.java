package owner.leadserviceline.ingest;

import java.io.IOException;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpConnectTimeoutException;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpTimeoutException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;

import owner.leadserviceline.data.CostRecord;
import owner.leadserviceline.data.GuideRecord;
import owner.leadserviceline.data.LineCounts;
import owner.leadserviceline.data.ProgramRecord;
import owner.leadserviceline.data.ProductRecommendationRecord;
import owner.leadserviceline.data.RouteRecord;
import owner.leadserviceline.data.SourceEvidenceRecord;
import owner.leadserviceline.data.UtilityRecord;

public final class DataBuildTool {

	private static final int MAX_RECORD_AGE_DAYS = 400;
	private static final Duration URL_CHECK_TIMEOUT = Duration.ofSeconds(12);
	private static final int URL_CHECK_ATTEMPTS = 3;
	private static final long URL_CHECK_RETRY_DELAY_MS = 350L;
	private static final Set<String> ALLOWED_SOURCE_TYPES = Set.of("utility_inventory", "utility_notice", "utility_program", "utility_cost");
	private static final Set<String> ALLOWED_SCOPE_TYPES = Set.of("utility", "program", "cost");
	private static final Set<String> ALLOWED_RECORD_STATUSES = Set.of("verified", "needs_review", "draft");
	private static final Set<String> ALLOWED_COST_CONFIDENCE = Set.of("low", "medium", "high");
	private static final Set<String> ALLOWED_ADDRESS_LOOKUP_MODES = Set.of("official_lookup", "service_area_notes", "contact_only");
	private static final Set<String> ALLOWED_RECOMMENDATION_CATEGORIES = Set.of(
			"replacement_filter",
			"faucet_system",
			"pitcher",
			"under_sink",
			"lead_copper_test",
			"city_water_test",
			"advanced_city_water_test"
	);

	private static final CSVFormat CSV = CSVFormat.DEFAULT.builder()
			.setHeader()
			.setSkipHeaderRecord(true)
			.setTrim(true)
			.build();

	private final ObjectMapper objectMapper;
	private final RouteInventoryBuilder routeInventoryBuilder;

	public DataBuildTool() {
		this.objectMapper = JsonMapper.builder()
				.addModule(new JavaTimeModule())
				.enable(SerializationFeature.INDENT_OUTPUT)
				.build();
		this.routeInventoryBuilder = new RouteInventoryBuilder();
	}

	public static void main(String[] args) {
		var dataRootArg = Stream.of(args)
				.filter(arg -> !arg.startsWith("--"))
				.findFirst()
				.orElse("data");
		var checkUrls = Stream.of(args).anyMatch("--check-urls"::equals);
		new DataBuildTool().build(Path.of(dataRootArg), checkUrls);
	}

	public void build(Path dataRoot) {
		build(dataRoot, false);
	}

	public void build(Path dataRoot, boolean checkUrls) {
		cleanOutputDirectories(dataRoot);

		var utilities = readUtilities(dataRoot.resolve("raw/utilities"));
		var programs = readPrograms(dataRoot.resolve("raw/programs"));
		var costs = readCosts(dataRoot.resolve("raw/costs"));
		var sources = readSources(dataRoot.resolve("raw/sources"));
		var guides = readGuides(dataRoot.resolve("raw/guides"));
		var recommendations = readRecommendations(dataRoot.resolve("raw/recommendations"));

		validateUtilities(utilities, sources);
		validatePrograms(programs, utilities, sources);
		validateCosts(costs, utilities, sources);
		validateSources(sources, utilities, programs, costs);
		validateGuides(guides);
		validateRecommendations(recommendations, guides);
		if (checkUrls) {
			validateReachableUrls(utilities, programs, sources, recommendations);
		}

		var costsByUtilityId = costs.stream().collect(java.util.stream.Collectors.toMap(CostRecord::utilityId, cost -> cost));
		var routes = routeInventoryBuilder.build(utilities, programs, guides, costsByUtilityId);

		writeEntityDirectory(dataRoot.resolve("normalized/utilities"), utilities, UtilityRecord::utilityId);
		writeEntityDirectory(dataRoot.resolve("normalized/programs"), programs, ProgramRecord::programId);
		writeEntityDirectory(dataRoot.resolve("normalized/costs"), costs, CostRecord::costId);
		writeEntityDirectory(dataRoot.resolve("normalized/sources"), sources, SourceEvidenceRecord::sourceId);
		writeEntityDirectory(dataRoot.resolve("normalized/guides"), guides, GuideRecord::guideId);
		writeEntityDirectory(dataRoot.resolve("normalized/recommendations"), recommendations, ProductRecommendationRecord::recommendationId);
		writeRoutes(dataRoot.resolve("derived/routes.json"), routes);
	}

	private void cleanOutputDirectories(Path dataRoot) {
		deleteDirectoryIfPresent(dataRoot.resolve("normalized"));
		deleteDirectoryIfPresent(dataRoot.resolve("derived"));
	}

	private List<UtilityRecord> readUtilities(Path directory) {
		return readCsv(directory).stream()
				.map(this::toUtility)
				.sorted(Comparator.comparing(UtilityRecord::utilityId))
				.toList();
	}

	private List<ProgramRecord> readPrograms(Path directory) {
		return readCsv(directory).stream()
				.map(this::toProgram)
				.sorted(Comparator.comparing(ProgramRecord::programId))
				.toList();
	}

	private List<CostRecord> readCosts(Path directory) {
		return readCsv(directory).stream()
				.map(this::toCost)
				.sorted(Comparator.comparing(CostRecord::costId))
				.toList();
	}

	private List<SourceEvidenceRecord> readSources(Path directory) {
		if (!Files.exists(directory)) {
			return List.of();
		}
		try (Stream<Path> files = Files.list(directory)) {
			return files
					.filter(path -> path.toString().endsWith(".json"))
					.sorted()
					.flatMap(this::readSourceFile)
					.sorted(Comparator.comparing(SourceEvidenceRecord::sourceId))
					.toList();
		} catch (IOException exception) {
			throw new UncheckedIOException("Failed to read raw sources from " + directory, exception);
		}
	}

	private List<GuideRecord> readGuides(Path directory) {
		if (!Files.exists(directory)) {
			return List.of();
		}
		try (Stream<Path> files = Files.list(directory)) {
			return files
					.filter(path -> path.toString().endsWith(".json"))
					.sorted()
					.flatMap(this::readGuideFile)
					.sorted(Comparator.comparing(GuideRecord::guideId))
					.toList();
		} catch (IOException exception) {
			throw new UncheckedIOException("Failed to read raw guides from " + directory, exception);
		}
	}

	private List<ProductRecommendationRecord> readRecommendations(Path directory) {
		if (!Files.exists(directory)) {
			return List.of();
		}
		try (Stream<Path> files = Files.list(directory)) {
			return files
					.filter(path -> path.toString().endsWith(".json"))
					.sorted()
					.flatMap(this::readRecommendationFile)
					.sorted(Comparator.comparing(ProductRecommendationRecord::guideSlug).thenComparingInt(ProductRecommendationRecord::displayOrder))
					.toList();
		} catch (IOException exception) {
			throw new UncheckedIOException("Failed to read raw recommendations from " + directory, exception);
		}
	}

	private Stream<SourceEvidenceRecord> readSourceFile(Path path) {
		try {
			var sources = objectMapper.readValue(path.toFile(), new TypeReference<List<SourceEvidenceRecord>>() {
			});
			return sources.stream();
		} catch (IOException exception) {
			throw new UncheckedIOException("Failed to read source file " + path, exception);
		}
	}

	private Stream<ProductRecommendationRecord> readRecommendationFile(Path path) {
		try {
			var recommendations = objectMapper.readValue(path.toFile(), new TypeReference<List<ProductRecommendationRecord>>() {
			});
			return recommendations.stream();
		} catch (IOException exception) {
			throw new UncheckedIOException("Failed to read recommendation file " + path, exception);
		}
	}

	private Stream<GuideRecord> readGuideFile(Path path) {
		try {
			var guides = objectMapper.readValue(path.toFile(), new TypeReference<List<GuideRecord>>() {
			});
			return guides.stream();
		} catch (IOException exception) {
			throw new UncheckedIOException("Failed to read guide file " + path, exception);
		}
	}

	private List<CSVRecord> readCsv(Path directory) {
		if (!Files.exists(directory)) {
			return List.of();
		}
		try (Stream<Path> files = Files.list(directory)) {
			return files
					.filter(path -> path.toString().endsWith(".csv"))
					.sorted()
					.flatMap(this::parseCsv)
					.toList();
		} catch (IOException exception) {
			throw new UncheckedIOException("Failed to read raw CSV from " + directory, exception);
		}
	}

	private Stream<CSVRecord> parseCsv(Path path) {
		try {
			Reader reader = Files.newBufferedReader(path);
			var records = CSV.parse(reader).getRecords();
			reader.close();
			return records.stream();
		} catch (IOException exception) {
			throw new UncheckedIOException("Failed to parse CSV " + path, exception);
		}
	}

	private UtilityRecord toUtility(CSVRecord record) {
		return new UtilityRecord(
				required(record, "utility_id"),
				required(record, "utility_slug"),
				required(record, "utility_name"),
				splitPipe(record.get("aliases")),
				required(record, "city"),
				required(record, "county"),
				required(record, "state"),
				required(record, "service_area_name"),
				required(record, "service_area_type"),
				required(record, "inventory_url"),
				required(record, "lookup_url"),
				required(record, "notification_url"),
				splitPipe(record.get("program_ids")),
				required(record, "contact_phone"),
				optionalString(record, "contact_email"),
				required(record, "inventory_status"),
				optionalString(record, "inventory_summary"),
				new LineCounts(
						optionalInt(record, "known_count"),
						optionalInt(record, "potential_count"),
						optionalInt(record, "unknown_count"),
						optionalInt(record, "non_lead_count")
				),
				required(record, "address_lookup_mode"),
				splitPipe(record.get("resolver_municipalities")),
				splitPipe(record.get("resolver_hints")),
				splitPipe(record.get("resolver_postal_prefixes")),
				splitPipe(record.get("source_refs")),
				required(record, "verification_status"),
				LocalDate.parse(required(record, "last_verified"))
		);
	}

	private ProgramRecord toProgram(CSVRecord record) {
		return new ProgramRecord(
				required(record, "program_id"),
				required(record, "program_slug"),
				required(record, "program_name"),
				required(record, "geography"),
				required(record, "state"),
				splitPipe(record.get("utility_ids")),
				required(record, "subsidy_type"),
				required(record, "public_side_covered"),
				required(record, "private_side_covered"),
				required(record, "income_rules"),
				required(record, "property_rules"),
				required(record, "contractor_rules"),
				optionalDate(record, "deadline"),
				required(record, "application_url"),
				splitPipe(record.get("source_refs")),
				required(record, "verification_status"),
				LocalDate.parse(required(record, "last_verified"))
		);
	}

	private CostRecord toCost(CSVRecord record) {
		return new CostRecord(
				required(record, "cost_id"),
				required(record, "utility_id"),
				required(record, "city"),
				required(record, "state"),
				required(record, "public_side_band"),
				required(record, "private_side_band"),
				required(record, "full_replacement_band"),
				required(record, "cost_confidence"),
				required(record, "housing_type_assumption"),
				required(record, "permit_assumption"),
				required(record, "restoration_assumption"),
				optionalString(record, "methodology_summary"),
				optionalString(record, "owner_trigger_summary"),
				splitPipe(record.get("source_refs")),
				LocalDate.parse(required(record, "last_verified"))
		);
	}

	private void validateUtilities(List<UtilityRecord> utilities, List<SourceEvidenceRecord> sources) {
		var sourceIds = sources.stream().map(SourceEvidenceRecord::sourceId).collect(java.util.stream.Collectors.toSet());
		for (var utility : utilities) {
			requireNotBlank(utility.utilityId(), "utility.utilityId");
			requireNotBlank(utility.utilitySlug(), "utility.utilitySlug");
			validateStateCode(utility.state(), "utility.state");
			validateHttpsUrl(requiredNonBlank(utility.inventoryUrl(), "utility.inventoryUrl"), "utility.inventoryUrl");
			validateHttpsUrl(requiredNonBlank(utility.notificationUrl(), "utility.notificationUrl"), "utility.notificationUrl");
			validateHttpsUrl(requiredNonBlank(utility.lookupUrl(), "utility.lookupUrl"), "utility.lookupUrl");
			require(ALLOWED_ADDRESS_LOOKUP_MODES.contains(utility.addressLookupMode()), "Unsupported address lookup mode for " + utility.utilityId());
			require(!utility.resolverMunicipalities().isEmpty(), "Utility must include resolver municipalities for " + utility.utilityId());
			var hasCountBreakdown = utility.lineCounts() != null && utility.lineCounts().hasCompleteBreakdown();
			var hasInventorySummary = utility.inventorySummary() != null && !utility.inventorySummary().isBlank();
			if (!hasCountBreakdown && !hasInventorySummary) {
				throw new IllegalArgumentException("Utility must have either a complete count breakdown or inventory summary for " + utility.utilityId());
			}
			validateFreshness(utility.lastVerified(), "utility.lastVerified " + utility.utilityId());
			require(!utility.sourceRefs().isEmpty(), "Utility must reference at least one source: " + utility.utilityId());
			for (var postalPrefix : utility.resolverPostalPrefixes()) {
				require(postalPrefix.matches("\\d{3}"), "Resolver postal prefixes must be three digits for " + utility.utilityId());
			}
			for (var sourceRef : utility.sourceRefs()) {
				require(sourceIds.contains(sourceRef), "Unknown utility source ref: " + sourceRef);
			}
		}
	}

	private void validatePrograms(List<ProgramRecord> programs, List<UtilityRecord> utilities, List<SourceEvidenceRecord> sources) {
		var utilityIds = utilities.stream().map(UtilityRecord::utilityId).collect(java.util.stream.Collectors.toSet());
		var sourceIds = sources.stream().map(SourceEvidenceRecord::sourceId).collect(java.util.stream.Collectors.toSet());
		for (var program : programs) {
			requireNotBlank(program.programId(), "program.programId");
			validateStateCode(program.state(), "program.state");
			validateHttpsUrl(requiredNonBlank(program.applicationUrl(), "program.applicationUrl"), "program.applicationUrl");
			validateFreshness(program.lastVerified(), "program.lastVerified " + program.programId());
			require(!program.utilityIds().isEmpty(), "Program must target at least one utility: " + program.programId());
			require(!program.sourceRefs().isEmpty(), "Program must reference at least one source: " + program.programId());
			for (var utilityId : program.utilityIds()) {
				require(utilityIds.contains(utilityId), "Unknown program utility id: " + utilityId);
			}
			for (var sourceRef : program.sourceRefs()) {
				require(sourceIds.contains(sourceRef), "Unknown program source ref: " + sourceRef);
			}
		}
	}

	private void validateCosts(List<CostRecord> costs, List<UtilityRecord> utilities, List<SourceEvidenceRecord> sources) {
		var utilityIds = utilities.stream().map(UtilityRecord::utilityId).collect(java.util.stream.Collectors.toSet());
		var sourceIds = sources.stream().map(SourceEvidenceRecord::sourceId).collect(java.util.stream.Collectors.toSet());
		for (var cost : costs) {
			require(utilityIds.contains(cost.utilityId()), "Unknown cost utility id: " + cost.utilityId());
			validateStateCode(cost.state(), "cost.state");
			require(ALLOWED_COST_CONFIDENCE.contains(cost.costConfidence().toLowerCase()), "Unsupported cost confidence for " + cost.costId());
			validateFreshness(cost.lastVerified(), "cost.lastVerified " + cost.costId());
			require(!cost.sourceRefs().isEmpty(), "Cost record must reference at least one source: " + cost.costId());
			for (var sourceRef : cost.sourceRefs()) {
				require(sourceIds.contains(sourceRef), "Unknown cost source ref: " + sourceRef);
			}
		}
	}

	private void validateSources(
			List<SourceEvidenceRecord> sources,
			List<UtilityRecord> utilities,
			List<ProgramRecord> programs,
			List<CostRecord> costs
	) {
		var utilityIds = utilities.stream().map(UtilityRecord::utilityId).collect(java.util.stream.Collectors.toSet());
		var programIds = programs.stream().map(ProgramRecord::programId).collect(java.util.stream.Collectors.toSet());
		var costIds = costs.stream().map(CostRecord::costId).collect(java.util.stream.Collectors.toSet());
		var sourceIds = new LinkedHashSet<String>();
		for (var source : sources) {
			require(sourceIds.add(requiredNonBlank(source.sourceId(), "source.sourceId")), "Duplicate source id: " + source.sourceId());
			validateHttpsUrl(requiredNonBlank(source.sourceUrl(), "source.sourceUrl"), "source.sourceUrl");
			requiredNonBlank(source.claimSummary(), "source.claimSummary");
			require(ALLOWED_SOURCE_TYPES.contains(source.sourceType()), "Unsupported source type: " + source.sourceType());
			require(ALLOWED_SCOPE_TYPES.contains(source.scopeType()), "Unsupported scope type: " + source.scopeType());
			require(ALLOWED_RECORD_STATUSES.contains(source.status()), "Unsupported source status: " + source.status());
			var capturedAt = OffsetDateTime.parse(requiredNonBlank(source.capturedAt(), "source.capturedAt"));
			var effectiveDate = LocalDate.parse(requiredNonBlank(source.effectiveDate(), "source.effectiveDate"));
			validateFreshness(capturedAt.toLocalDate(), "source.capturedAt " + source.sourceId());
			validateFreshness(effectiveDate, "source.effectiveDate " + source.sourceId());
			require(!effectiveDate.isAfter(capturedAt.toLocalDate()), "Source effectiveDate cannot be after capturedAt for " + source.sourceId());
			switch (source.scopeType()) {
				case "utility" -> require(utilityIds.contains(source.scopeKey()), "Unknown utility scope key for source " + source.sourceId());
				case "program" -> require(programIds.contains(source.scopeKey()), "Unknown program scope key for source " + source.sourceId());
				case "cost" -> require(costIds.contains(source.scopeKey()), "Unknown cost scope key for source " + source.sourceId());
				default -> throw new IllegalArgumentException("Unsupported source scope type: " + source.scopeType());
			}
			switch (source.sourceType()) {
				case "utility_inventory", "utility_notice" -> require("utility".equals(source.scopeType()), "Utility inventory/notice sources must use utility scope: " + source.sourceId());
				case "utility_program" -> require("program".equals(source.scopeType()), "Utility program sources must use program scope: " + source.sourceId());
				case "utility_cost" -> require("cost".equals(source.scopeType()), "Utility cost sources must use cost scope: " + source.sourceId());
				default -> throw new IllegalArgumentException("Unsupported source type: " + source.sourceType());
			}
		}
	}

	private void validateGuides(List<GuideRecord> guides) {
		var guideIds = new LinkedHashSet<String>();
		var slugs = new LinkedHashSet<String>();
		for (var guide : guides) {
			require(guideIds.add(requiredNonBlank(guide.guideId(), "guide.guideId")), "Duplicate guide id: " + guide.guideId());
			require(slugs.add(requiredNonBlank(guide.slug(), "guide.slug")), "Duplicate guide slug: " + guide.slug());
			require(guide.slug().matches("[a-z0-9-]+"), "Guide slug must be lowercase kebab-case: " + guide.slug());
			requiredNonBlank(guide.title(), "guide.title");
			requiredNonBlank(guide.eyebrow(), "guide.eyebrow");
			requiredNonBlank(guide.heroSummary(), "guide.heroSummary");
			requiredNonBlank(guide.verdict(), "guide.verdict");
			require(!guide.keyPoints().isEmpty(), "Guide must include key points: " + guide.guideId());
			require(!guide.nextSteps().isEmpty(), "Guide must include next steps: " + guide.guideId());
			validateFreshness(guide.lastVerified(), "guide.lastVerified " + guide.guideId());
		}
	}

	private void validateRecommendations(List<ProductRecommendationRecord> recommendations, List<GuideRecord> guides) {
		var recommendationIds = new LinkedHashSet<String>();
		var recommendationSlugs = new LinkedHashSet<String>();
		var guideSlugs = guides.stream().map(GuideRecord::slug).collect(java.util.stream.Collectors.toSet());
		for (var recommendation : recommendations) {
			require(recommendationIds.add(requiredNonBlank(recommendation.recommendationId(), "recommendation.recommendationId")),
					"Duplicate recommendation id: " + recommendation.recommendationId());
			require(recommendationSlugs.add(requiredNonBlank(recommendation.slug(), "recommendation.slug")),
					"Duplicate recommendation slug: " + recommendation.slug());
			require(recommendation.slug().matches("[a-z0-9-]+"), "Recommendation slug must be lowercase kebab-case: " + recommendation.slug());
			require(guideSlugs.contains(recommendation.guideSlug()), "Unknown recommendation guide slug: " + recommendation.guideSlug());
			require(recommendation.displayOrder() > 0, "Recommendation display order must be positive: " + recommendation.recommendationId());
			require(ALLOWED_RECOMMENDATION_CATEGORIES.contains(recommendation.category()),
					"Unsupported recommendation category: " + recommendation.category());
			requiredNonBlank(recommendation.name(), "recommendation.name");
			requiredNonBlank(recommendation.badge(), "recommendation.badge");
			requiredNonBlank(recommendation.destinationLabel(), "recommendation.destinationLabel");
			validateHttpsUrl(requiredNonBlank(recommendation.destinationUrl(), "recommendation.destinationUrl"), "recommendation.destinationUrl");
			requiredNonBlank(recommendation.bestFor(), "recommendation.bestFor");
			requiredNonBlank(recommendation.whyItFits(), "recommendation.whyItFits");
			requiredNonBlank(recommendation.watchout(), "recommendation.watchout");
			requiredNonBlank(recommendation.evidenceNote(), "recommendation.evidenceNote");
			validateFreshness(recommendation.lastVerified(), "recommendation.lastVerified " + recommendation.recommendationId());
		}
	}

	private void validateReachableUrls(
			List<UtilityRecord> utilities,
			List<ProgramRecord> programs,
			List<SourceEvidenceRecord> sources,
			List<ProductRecommendationRecord> recommendations
	) {
		var urls = new LinkedHashSet<String>();
		utilities.forEach(utility -> {
			urls.add(utility.inventoryUrl());
			urls.add(utility.lookupUrl());
			urls.add(utility.notificationUrl());
		});
		programs.forEach(program -> urls.add(program.applicationUrl()));
		sources.forEach(source -> urls.add(source.sourceUrl()));
		recommendations.forEach(recommendation -> urls.add(recommendation.destinationUrl()));

		var client = HttpClient.newBuilder()
				.followRedirects(HttpClient.Redirect.NORMAL)
				.build();
		var failures = new ArrayList<String>();

		for (var url : urls) {
			try {
				var statusCode = checkUrlWithRetry(client, url);
				System.out.println("URL check " + statusCode + " " + url);
				if (!isReachableStatus(statusCode)) {
					failures.add(statusCode + " " + url);
				}
			} catch (Exception exception) {
				if (isTimeoutException(exception)) {
					System.out.println("URL timeout warning " + url + " -> " + exception.getClass().getSimpleName() + ": " + exception.getMessage());
					continue;
				}
				failures.add(url + " -> " + exception.getClass().getSimpleName() + ": " + exception.getMessage());
			}
		}

		require(failures.isEmpty(), "URL reachability failures:\n" + String.join("\n", failures));
	}

	private int checkUrl(HttpClient client, String url) throws IOException, InterruptedException {
		var uri = URI.create(url);
		var headRequest = HttpRequest.newBuilder(uri)
				.method("HEAD", HttpRequest.BodyPublishers.noBody())
				.header("User-Agent", "LeadLineRecord/1.0")
				.timeout(URL_CHECK_TIMEOUT)
				.build();
		var headResponse = client.send(headRequest, HttpResponse.BodyHandlers.discarding());
		if (!isReachableStatus(headResponse.statusCode())) {
			var getStatus = getUrl(client, uri);
			return isReachableStatus(getStatus) ? getStatus : headResponse.statusCode();
		}
		return headResponse.statusCode();
	}

	private int getUrl(HttpClient client, URI uri) throws IOException, InterruptedException {
		var getRequest = HttpRequest.newBuilder(uri)
				.GET()
				.header("User-Agent", "LeadLineRecord/1.0")
				.timeout(URL_CHECK_TIMEOUT)
				.build();
		return client.send(getRequest, HttpResponse.BodyHandlers.discarding()).statusCode();
	}

	private int checkUrlWithRetry(HttpClient client, String url) throws IOException, InterruptedException {
		IOException lastIo = null;
		InterruptedException lastInterrupted = null;
		for (int attempt = 1; attempt <= URL_CHECK_ATTEMPTS; attempt++) {
			try {
				return checkUrl(client, url);
			} catch (IOException exception) {
				if (!isTimeoutException(exception) || attempt == URL_CHECK_ATTEMPTS) {
					throw exception;
				}
				lastIo = exception;
				Thread.sleep(URL_CHECK_RETRY_DELAY_MS);
			} catch (InterruptedException exception) {
				Thread.currentThread().interrupt();
				if (!isTimeoutException(exception) || attempt == URL_CHECK_ATTEMPTS) {
					throw exception;
				}
				lastInterrupted = exception;
			}
		}
		if (lastIo != null) {
			throw lastIo;
		}
		if (lastInterrupted != null) {
			throw lastInterrupted;
		}
		throw new IllegalStateException("Unreachable URL check retry state for " + url);
	}

	private boolean isReachableStatus(int statusCode) {
		return (statusCode >= 200 && statusCode < 400) || statusCode == 401 || statusCode == 403;
	}

	private boolean isTimeoutException(Exception exception) {
		return exception instanceof HttpConnectTimeoutException || exception instanceof HttpTimeoutException;
	}

	private <T> void writeEntityDirectory(Path directory, List<T> entities, java.util.function.Function<T, String> idExtractor) {
		resetDirectory(directory);
		entities.forEach(entity -> writeJson(directory.resolve(idExtractor.apply(entity) + ".json"), entity));
	}

	private void writeRoutes(Path path, List<RouteRecord> routes) {
		try {
			Files.createDirectories(path.getParent());
		} catch (IOException exception) {
			throw new UncheckedIOException("Failed to create route directory " + path.getParent(), exception);
		}
		writeJson(path, routes);
	}

	private void resetDirectory(Path directory) {
		try {
			Files.createDirectories(directory);
			try (Stream<Path> files = Files.list(directory)) {
				files.filter(path -> path.toString().endsWith(".json")).forEach(path -> {
					try {
						Files.deleteIfExists(path);
					} catch (IOException exception) {
						throw new UncheckedIOException("Failed to clear generated file " + path, exception);
					}
				});
			}
		} catch (IOException exception) {
			throw new UncheckedIOException("Failed to prepare directory " + directory, exception);
		}
	}

	private void writeJson(Path path, Object value) {
		try {
			Files.createDirectories(path.getParent());
			objectMapper.writeValue(path.toFile(), value);
		} catch (IOException exception) {
			throw new UncheckedIOException("Failed to write JSON to " + path, exception);
		}
	}

	private List<String> splitPipe(String value) {
		if (value == null || value.isBlank()) {
			return List.of();
		}
		return Stream.of(value.split("\\|"))
				.map(String::trim)
				.filter(part -> !part.isBlank())
				.toList();
	}

	private Integer optionalInt(CSVRecord record, String header) {
		var raw = optionalString(record, header);
		if (raw == null) {
			return null;
		}
		var value = Integer.parseInt(raw);
		require(value >= 0, "Expected non-negative integer for " + header);
		return value;
	}

	private LocalDate optionalDate(CSVRecord record, String header) {
		var raw = optionalString(record, header);
		return raw == null ? null : LocalDate.parse(raw);
	}

	private void validateFreshness(LocalDate date, String label) {
		require(!date.isAfter(LocalDate.now()), label + " cannot be in the future");
		require(ChronoUnit.DAYS.between(date, LocalDate.now()) <= MAX_RECORD_AGE_DAYS, label + " is stale");
	}

	private void validateStateCode(String state, String label) {
		require(state != null && state.matches("[a-z]{2}"), "Expected two-letter lowercase state code for " + label);
	}

	private void validateHttpsUrl(String value, String label) {
		try {
			var uri = new URI(value);
			require("https".equalsIgnoreCase(uri.getScheme()), "Expected https URL for " + label);
			require(uri.getHost() != null && !uri.getHost().isBlank(), "Expected host for " + label);
		} catch (URISyntaxException exception) {
			throw new IllegalArgumentException("Invalid URL for " + label + ": " + value, exception);
		}
	}

	private String requiredNonBlank(String value, String label) {
		requireNotBlank(value, label);
		return value;
	}

	private String optionalString(CSVRecord record, String header) {
		if (!record.isMapped(header) || !record.isSet(header)) {
			return null;
		}
		var value = record.get(header);
		return value == null || value.isBlank() ? null : value;
	}

	private String required(CSVRecord record, String header) {
		var value = record.get(header);
		requireNotBlank(value, header);
		return value;
	}

	private void requireNotBlank(String value, String label) {
		require(value != null && !value.isBlank(), "Missing required value for " + label);
	}

	private void require(boolean condition, String message) {
		if (!condition) {
			throw new IllegalArgumentException(message);
		}
	}

	private void deleteDirectoryIfPresent(Path directory) {
		if (!Files.exists(directory)) {
			return;
		}
		try (var paths = Files.walk(directory)) {
			paths.sorted(Comparator.reverseOrder()).forEach(path -> {
				try {
					Files.deleteIfExists(path);
				} catch (IOException exception) {
					throw new UncheckedIOException(exception);
				}
			});
		} catch (IOException exception) {
			throw new UncheckedIOException(exception);
		}
	}
}
