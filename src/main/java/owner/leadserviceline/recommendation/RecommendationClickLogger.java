package owner.leadserviceline.recommendation;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Duration;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import com.fasterxml.jackson.databind.ObjectMapper;
import owner.leadserviceline.data.ProductRecommendationRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@EnableConfigurationProperties(RecommendationLoggingProperties.class)
public class RecommendationClickLogger {

	private static final Logger LOGGER = LoggerFactory.getLogger(RecommendationClickLogger.class);
	private static final Duration PRUNE_INTERVAL = Duration.ofHours(6);

	private final ObjectMapper objectMapper;
	private final RecommendationLoggingProperties properties;
	private final Object appendLock = new Object();
	private Instant lastPruneAt = Instant.EPOCH;

	public RecommendationClickLogger(ObjectMapper objectMapper, RecommendationLoggingProperties properties) {
		this.objectMapper = objectMapper;
		this.properties = properties;
	}

	public void logClick(
			ProductRecommendationRecord recommendation,
			String destinationUrl,
			String sourcePath,
			String slot,
			String validationLabel
	) {
		if (!properties.recommendationLogEnabled()) {
			return;
		}

		var event = new RecommendationClickEventRecord(
				Instant.now().toString(),
				recommendation.slug(),
				recommendation.guideSlug(),
				recommendation.name(),
				normalizeValue(sourcePath),
				normalizeValue(slot),
				normalizeValue(validationLabel),
				extractDestinationDomain(destinationUrl)
		);

		try {
			appendLine(event);
		} catch (IOException exception) {
			LOGGER.warn("Unable to append recommendation click event to {}", properties.recommendationLogPath(), exception);
		}
	}

	private void appendLine(RecommendationClickEventRecord event) throws IOException {
		var logPath = Path.of(properties.recommendationLogPath());
		var parent = logPath.getParent();
		if (parent != null) {
			Files.createDirectories(parent);
		}
		synchronized (appendLock) {
			pruneExpiredEventsIfDue(logPath);
			var line = objectMapper.writeValueAsString(event) + System.lineSeparator();
			Files.writeString(logPath, line, StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.APPEND, StandardOpenOption.WRITE);
		}
	}

	private void pruneExpiredEventsIfDue(Path logPath) throws IOException {
		var now = Instant.now();
		if (Duration.between(lastPruneAt, now).compareTo(PRUNE_INTERVAL) < 0) {
			return;
		}
		pruneExpiredEvents(logPath, now);
		lastPruneAt = now;
	}

	private void pruneExpiredEvents(Path logPath, Instant now) throws IOException {
		if (!Files.exists(logPath)) {
			return;
		}
		var cutoff = now.minus(properties.recommendationLogRetentionDays(), ChronoUnit.DAYS);
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

	private Optional<RecommendationClickEventRecord> readEvent(String line) {
		try {
			return Optional.of(objectMapper.readValue(line, RecommendationClickEventRecord.class));
		} catch (IOException exception) {
			LOGGER.warn("Skipping unreadable recommendation log line during append");
			return Optional.empty();
		}
	}

	private String extractDestinationDomain(String destinationUrl) {
		if (destinationUrl == null || destinationUrl.isBlank()) {
			return "";
		}
		try {
			return Optional.ofNullable(URI.create(destinationUrl).getHost()).orElse("");
		} catch (IllegalArgumentException exception) {
			return "";
		}
	}

	private String normalizeValue(String value) {
		return value == null ? "" : value.trim();
	}
}
