package owner.leadserviceline.lookup;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.regex.Pattern;

import com.fasterxml.jackson.databind.ObjectMapper;
import owner.leadserviceline.pages.LookupPageModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@EnableConfigurationProperties(LookupLoggingProperties.class)
public class LookupEventLogger {

	private static final Logger LOGGER = LoggerFactory.getLogger(LookupEventLogger.class);
	private static final Pattern POSTAL_PATTERN = Pattern.compile("\\b(\\d{5})(?:-\\d{4})?\\b");

	private final ObjectMapper objectMapper;
	private final LookupLoggingProperties properties;
	private final Object appendLock = new Object();

	public LookupEventLogger(ObjectMapper objectMapper, LookupLoggingProperties properties) {
		this.objectMapper = objectMapper;
		this.properties = properties;
	}

	public void logLookup(String query, String city, String state, LookupPageModel page) {
		if (!properties.lookupLogEnabled() || !page.attempted()) {
			return;
		}

		var topMatch = page.matches().isEmpty() ? null : page.matches().get(0);
		var confidenceLabel = topMatch != null
				? topMatch.confidenceLabel()
				: (page.attempted() ? "No match" : "No lookup attempted");
		var lookupLabel = buildLookupLabel(query, city, state);
		var event = new LookupEventRecord(
				Instant.now().toString(),
				lookupLabel,
				buildLookupBucketKey(lookupLabel),
				detectInputType(query, city, state),
				null,
				null,
				null,
				topMatch == null ? null : topMatch.utility().utilityId(),
				topMatch == null ? null : topMatch.utility().utilityName(),
				topMatch == null ? null : topMatch.localPath(),
				confidenceLabel,
				page.ambiguous(),
				page.geocoderUsed()
		);

		try {
			appendLine(event);
		} catch (IOException exception) {
			LOGGER.warn("Unable to append lookup event to {}", properties.lookupLogPath(), exception);
		}
	}

	private void appendLine(LookupEventRecord event) throws IOException {
		var logPath = Path.of(properties.lookupLogPath());
		var parent = logPath.getParent();
		if (parent != null) {
			Files.createDirectories(parent);
		}
		synchronized (appendLock) {
			pruneExpiredEvents(logPath);
			var line = objectMapper.writeValueAsString(event) + System.lineSeparator();
			Files.writeString(logPath, line, StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.APPEND, StandardOpenOption.WRITE);
		}
	}

	private void pruneExpiredEvents(Path logPath) throws IOException {
		if (!Files.exists(logPath)) {
			return;
		}
		var cutoff = Instant.now().minus(properties.lookupLogRetentionDays(), ChronoUnit.DAYS);
		var originalLines = Files.readAllLines(logPath, StandardCharsets.UTF_8);
		var keptLines = originalLines.stream()
				.map(String::trim)
				.filter(line -> !line.isBlank())
				.filter(line -> shouldKeepLine(line, cutoff))
				.toList();
		if (keptLines.size() == originalLines.stream().map(String::trim).filter(line -> !line.isBlank()).count()) {
			return;
		}
		var rewritten = keptLines.isEmpty()
				? ""
				: String.join(System.lineSeparator(), keptLines) + System.lineSeparator();
		Files.writeString(
				logPath,
				rewritten,
				StandardCharsets.UTF_8,
				StandardOpenOption.CREATE,
				StandardOpenOption.TRUNCATE_EXISTING,
				StandardOpenOption.WRITE
		);
	}

	private boolean shouldKeepLine(String line, Instant cutoff) {
		var event = readEvent(line);
		if (event.isEmpty()) {
			return false;
		}
		try {
			return Instant.parse(event.get().timestamp()).isAfter(cutoff);
		} catch (DateTimeParseException exception) {
			return false;
		}
	}

	private Optional<LookupEventRecord> readEvent(String line) {
		try {
			return Optional.of(objectMapper.readValue(line, LookupEventRecord.class));
		} catch (IOException exception) {
			LOGGER.warn("Skipping unreadable lookup log line during append");
			return Optional.empty();
		}
	}

	private String buildLookupLabel(String query, String city, String state) {
		var normalizedCity = normalizeText(city);
		var normalizedState = normalizeState(state);
		if (!normalizedCity.isBlank() && !normalizedState.isBlank()) {
			return normalizedCity + ", " + normalizedState;
		}
		if (!normalizedCity.isBlank()) {
			return normalizedCity;
		}
		var normalizedQuery = normalizeText(query);
		if (normalizedQuery.isBlank()) {
			return "Lookup without location";
		}
		var postalMatcher = POSTAL_PATTERN.matcher(normalizedQuery);
		if (postalMatcher.find()) {
			return "ZIP " + postalMatcher.group(1).substring(0, 3) + "xx";
		}
		if (looksLikeStreetAddress(normalizedQuery)) {
			return "Redacted address lookup";
		}
		if (isSafeShortFreeform(normalizedQuery)) {
			return normalizedQuery;
		}
		return "Freeform lookup";
	}

	private String buildLookupBucketKey(String lookupLabel) {
		return normalizeText(lookupLabel)
				.toLowerCase(Locale.US)
				.replaceAll("[^a-z0-9]+", "-")
				.replaceAll("^-+|-+$", "");
	}

	private String detectInputType(String query, String city, String state) {
		var normalizedCity = normalizeText(city);
		var normalizedState = normalizeState(state);
		if (!normalizedCity.isBlank() && !normalizedState.isBlank()) {
			return "city-state";
		}
		if (!normalizedCity.isBlank()) {
			return "city-only";
		}
		var normalizedQuery = normalizeText(query);
		if (normalizedQuery.isBlank()) {
			return "blank";
		}
		if (POSTAL_PATTERN.matcher(normalizedQuery).find()) {
			return "postal";
		}
		if (looksLikeStreetAddress(normalizedQuery)) {
			return "address-redacted";
		}
		if (isSafeShortFreeform(normalizedQuery)) {
			return "freeform-short";
		}
		return "freeform-redacted";
	}

	private boolean looksLikeStreetAddress(String value) {
		return value.matches(".*\\d+.*[A-Za-z].*");
	}

	private boolean isSafeShortFreeform(String value) {
		return value.length() <= 32 && value.matches("[A-Za-z .,'-]+");
	}

	private String normalizeText(String value) {
		return value == null ? "" : value.trim().replaceAll("\\s+", " ");
	}

	private String normalizeState(String value) {
		var normalized = normalizeText(value);
		return normalized.isBlank() ? "" : normalized.toUpperCase(Locale.US);
	}
}
