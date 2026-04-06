package owner.leadserviceline.data;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Repository;

@Repository
@EnableConfigurationProperties(LeadServiceLineProperties.class)
public class LeadServiceLineRepository {

	private final ObjectMapper objectMapper;
	private final DataSnapshot snapshot;

	public LeadServiceLineRepository(ObjectMapper objectMapper, LeadServiceLineProperties properties) {
		this.objectMapper = objectMapper;
		this.snapshot = loadSnapshot(Path.of(properties.dataRoot()));
	}

	public Collection<UtilityRecord> utilities() {
		return snapshot.utilitiesById.values();
	}

	public Collection<ProgramRecord> programs() {
		return snapshot.programsById.values();
	}

	public List<ProgramRecord> programsForState(String state) {
		return snapshot.programsById.values().stream()
				.filter(program -> state.equals(program.state()))
				.sorted(Comparator.comparing(ProgramRecord::programName))
				.toList();
	}

	public Collection<CostRecord> costs() {
		return snapshot.costsByUtilityId.values().stream()
				.flatMap(List::stream)
				.toList();
	}

	public Collection<SourceEvidenceRecord> sources() {
		return snapshot.sourcesById.values();
	}

	public Collection<RouteRecord> routes() {
		return snapshot.routesByPath.values();
	}

	public List<RouteRecord> routesForState(String state) {
		return snapshot.routesByPath.values().stream()
				.filter(route -> state.equals(route.state()))
				.sorted(Comparator.comparing(RouteRecord::path))
				.toList();
	}

	public Collection<GuideRecord> guides() {
		return snapshot.guidesBySlug.values();
	}

	public Optional<GuideRecord> findGuideBySlug(String slug) {
		return Optional.ofNullable(snapshot.guidesBySlug.get(slug));
	}

	public Optional<UtilityRecord> findUtilityById(String utilityId) {
		return Optional.ofNullable(snapshot.utilitiesById.get(utilityId));
	}

	public Optional<RouteRecord> findRoute(String path) {
		return Optional.ofNullable(snapshot.routesByPath.get(normalizePath(path)));
	}

	public List<RouteRecord> routesForUtility(String utilityId) {
		return snapshot.routesByPath.values().stream()
				.filter(route -> utilityId.equals(route.utilityId()))
				.sorted(Comparator.comparing(RouteRecord::path))
				.toList();
	}

	public List<ProgramRecord> programsForUtility(String utilityId) {
		return snapshot.programsById.values().stream()
				.filter(program -> program.utilityIds().contains(utilityId))
				.sorted(Comparator.comparing(ProgramRecord::programName))
				.toList();
	}

	public Optional<CostRecord> costForUtility(String utilityId) {
		return snapshot.costsByUtilityId.getOrDefault(utilityId, List.of()).stream().findFirst();
	}

	public List<SourceEvidenceRecord> sourcesForRefs(Collection<String> sourceRefs) {
		return sourceRefs.stream()
				.map(snapshot.sourcesById::get)
				.filter(source -> source != null)
				.toList();
	}

	private DataSnapshot loadSnapshot(Path dataRoot) {
		var utilities = loadDirectory(dataRoot.resolve("normalized/utilities"), UtilityRecord.class).stream()
				.sorted(Comparator.comparing(UtilityRecord::state).thenComparing(UtilityRecord::city).thenComparing(UtilityRecord::utilityName))
				.toList();
		var programs = loadDirectory(dataRoot.resolve("normalized/programs"), ProgramRecord.class);
		var costs = loadDirectory(dataRoot.resolve("normalized/costs"), CostRecord.class);
		var sources = loadDirectory(dataRoot.resolve("normalized/sources"), SourceEvidenceRecord.class);
		var guides = loadDirectory(dataRoot.resolve("normalized/guides"), GuideRecord.class);
		var routes = loadRoutes(dataRoot.resolve("derived/routes.json"));

		return new DataSnapshot(
				indexBy(utilities, UtilityRecord::utilityId),
				indexBy(programs, ProgramRecord::programId),
				costs.stream().collect(Collectors.groupingBy(CostRecord::utilityId)),
				indexBy(sources, SourceEvidenceRecord::sourceId),
				indexBy(guides, GuideRecord::slug),
				routes.stream().collect(Collectors.toMap(route -> normalizePath(route.path()), Function.identity()))
		);
	}

	private List<RouteRecord> loadRoutes(Path path) {
		if (!Files.exists(path)) {
			return List.of();
		}
		try {
			return objectMapper.readValue(path.toFile(), new TypeReference<List<RouteRecord>>() {
			});
		} catch (IOException exception) {
			throw new UncheckedIOException("Failed to read route data from " + path, exception);
		}
	}

	private <T> List<T> loadDirectory(Path directory, Class<T> type) {
		if (!Files.exists(directory)) {
			return List.of();
		}
		try (Stream<Path> files = Files.list(directory)) {
			return files
					.filter(path -> path.toString().endsWith(".json"))
					.sorted()
					.map(path -> readJson(path, type))
					.toList();
		} catch (IOException exception) {
			throw new UncheckedIOException("Failed to read data from " + directory, exception);
		}
	}

	private <T> T readJson(Path path, Class<T> type) {
		try {
			return objectMapper.readValue(path.toFile(), type);
		} catch (IOException exception) {
			throw new UncheckedIOException("Failed to read JSON from " + path, exception);
		}
	}

	private <T> Map<String, T> indexBy(List<T> items, Function<T, String> keyExtractor) {
		return items.stream().collect(Collectors.toMap(keyExtractor, Function.identity()));
	}

	private String normalizePath(String path) {
		if (path == null || path.isBlank()) {
			return "/";
		}
		var trimmed = path.trim();
		return trimmed.startsWith("/") ? trimmed : "/" + trimmed;
	}

	private record DataSnapshot(
			Map<String, UtilityRecord> utilitiesById,
			Map<String, ProgramRecord> programsById,
			Map<String, List<CostRecord>> costsByUtilityId,
			Map<String, SourceEvidenceRecord> sourcesById,
			Map<String, GuideRecord> guidesBySlug,
			Map<String, RouteRecord> routesByPath
	) {
	}
}
