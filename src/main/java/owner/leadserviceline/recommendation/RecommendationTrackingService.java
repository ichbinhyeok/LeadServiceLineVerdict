package owner.leadserviceline.recommendation;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.time.Duration;
import java.time.Instant;
import java.util.HexFormat;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import owner.leadserviceline.web.SiteRuntimeProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

@Component
@EnableConfigurationProperties(RecommendationLoggingProperties.class)
public class RecommendationTrackingService {

	private static final Pattern SLOT_PATTERN = Pattern.compile("[a-z0-9-]{1,80}");
	private static final Pattern PATH_PATTERN = Pattern.compile("^/[a-z0-9/_-]{1,200}$");
	private static final Duration CLICK_DEDUPE_WINDOW = Duration.ofSeconds(30);
	private static final Duration IMPRESSION_DEDUPE_WINDOW = Duration.ofMinutes(10);
	private static final Duration PRUNE_INTERVAL = Duration.ofMinutes(10);

	private final RecommendationLoggingProperties loggingProperties;
	private final URI siteBaseUri;
	private final ConcurrentHashMap<String, Instant> recentAcceptedEvents = new ConcurrentHashMap<>();
	private volatile Instant lastPruneAt = Instant.EPOCH;

	public RecommendationTrackingService(
			RecommendationLoggingProperties loggingProperties,
			SiteRuntimeProperties siteRuntimeProperties
	) {
		this.loggingProperties = loggingProperties;
		this.siteBaseUri = parseBaseUri(siteRuntimeProperties.siteBaseUrl());
	}

	public String clickPath(String slug, String sourcePath, String slot) {
		var normalizedSourcePath = normalizeTrackedPath(sourcePath);
		var normalizedSlot = normalizeSlot(slot);
		var builder = UriComponentsBuilder.fromPath("/go/{slug}")
				.queryParam("slot", normalizedSlot)
				.queryParam("sourcePath", normalizedSourcePath);
		appendSignature(builder, "click", slug, normalizedSourcePath, normalizedSlot);
		return builder.buildAndExpand(slug).encode().toUriString();
	}

	public String impressionPath(String slug, String pagePath, String slot) {
		var normalizedPagePath = normalizeTrackedPath(pagePath);
		var normalizedSlot = normalizeSlot(slot);
		var builder = UriComponentsBuilder.fromPath("/events/recommendation-impression")
				.queryParam("slug", slug)
				.queryParam("pagePath", normalizedPagePath)
				.queryParam("slot", normalizedSlot);
		appendSignature(builder, "impression", slug, normalizedPagePath, normalizedSlot);
		return builder.build().encode().toUriString();
	}

	public boolean eventProtectionEnabled() {
		return loggingProperties.eventProtectionEnabled();
	}

	public RecommendationEventVerification validateClick(
			String slug,
			String sourcePath,
			String slot,
			String signature,
			String referer,
			String secFetchSite,
			String userAgent,
			String remoteAddress
	) {
		return validate(
				"click",
				slug,
				sourcePath,
				slot,
				signature,
				referer,
				secFetchSite,
				null,
				userAgent,
				remoteAddress,
				CLICK_DEDUPE_WINDOW
		);
	}

	public RecommendationEventVerification validateImpression(
			String slug,
			String pagePath,
			String slot,
			String signature,
			String referer,
			String secFetchSite,
			String secFetchDest,
			String userAgent,
			String remoteAddress
	) {
		return validate(
				"impression",
				slug,
				pagePath,
				slot,
				signature,
				referer,
				secFetchSite,
				secFetchDest,
				userAgent,
				remoteAddress,
				IMPRESSION_DEDUPE_WINDOW
		);
	}

	private RecommendationEventVerification validate(
			String eventType,
			String slug,
			String trackedPath,
			String slot,
			String signature,
			String referer,
			String secFetchSite,
			String secFetchDest,
			String userAgent,
			String remoteAddress,
			Duration dedupeWindow
	) {
		var normalizedPath = normalizeTrackedPath(trackedPath);
		var normalizedSlot = normalizeSlot(slot);
		if (!hasText(slug) || !hasText(normalizedPath) || !hasText(normalizedSlot)) {
			return RecommendationEventVerification.rejected("invalid-shape");
		}
		if ("impression".equals(eventType) && hasText(secFetchDest) && !"image".equalsIgnoreCase(secFetchDest.trim())) {
			return RecommendationEventVerification.rejected("unexpected-fetch-dest");
		}
		if (!allowedFetchSite(secFetchSite)) {
			return RecommendationEventVerification.rejected("cross-site-fetch");
		}
		if (!signatureMatches(eventType, slug, normalizedPath, normalizedSlot, signature)) {
			return RecommendationEventVerification.rejected("invalid-signature");
		}

		var referrerState = inspectReferer(referer, normalizedPath);
		if (referrerState == ReferrerState.MISMATCH) {
			return RecommendationEventVerification.rejected("referrer-mismatch");
		}
		if (!acceptOnce(eventType, slug, normalizedPath, normalizedSlot, userAgent, remoteAddress, dedupeWindow)) {
			return RecommendationEventVerification.rejected("duplicate-suppressed");
		}

		var validationLabel = referrerState == ReferrerState.MATCH ? "signed-referrer-match" : "signed-no-referrer";
		return RecommendationEventVerification.trusted(normalizedPath, normalizedSlot, validationLabel);
	}

	private void appendSignature(UriComponentsBuilder builder, String eventType, String slug, String trackedPath, String slot) {
		var signature = signatureFor(eventType, slug, trackedPath, slot);
		if (hasText(signature)) {
			builder.queryParam("sig", signature);
		}
	}

	private boolean signatureMatches(String eventType, String slug, String trackedPath, String slot, String signature) {
		if (!eventProtectionEnabled() || !hasText(signature)) {
			return false;
		}
		var expected = signatureFor(eventType, slug, trackedPath, slot);
		return hasText(expected)
				&& MessageDigest.isEqual(expected.getBytes(StandardCharsets.UTF_8), signature.trim().getBytes(StandardCharsets.UTF_8));
	}

	private String signatureFor(String eventType, String slug, String trackedPath, String slot) {
		if (!eventProtectionEnabled()) {
			return "";
		}
		var payload = String.join("\n",
				eventType,
				safe(slug),
				safe(trackedPath),
				safe(slot));
		try {
			var mac = Mac.getInstance("HmacSHA256");
			mac.init(new SecretKeySpec(loggingProperties.recommendationEventSecret().getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
			return HexFormat.of().formatHex(mac.doFinal(payload.getBytes(StandardCharsets.UTF_8)));
		} catch (GeneralSecurityException exception) {
			return "";
		}
	}

	private ReferrerState inspectReferer(String referer, String expectedPath) {
		if (!hasText(referer)) {
			return ReferrerState.ABSENT;
		}
		try {
			var refererUri = new URI(referer.trim());
			if (!sameOrigin(refererUri)) {
				return ReferrerState.MISMATCH;
			}
			var refererPath = normalizeTrackedPath(refererUri.getPath());
			return expectedPath.equals(refererPath) ? ReferrerState.MATCH : ReferrerState.MISMATCH;
		} catch (URISyntaxException exception) {
			return ReferrerState.MISMATCH;
		}
	}

	private boolean sameOrigin(URI candidate) {
		return Objects.equals(normalizeHost(siteBaseUri.getHost()), normalizeHost(candidate.getHost()))
				&& Objects.equals(normalizeScheme(siteBaseUri.getScheme()), normalizeScheme(candidate.getScheme()))
				&& effectivePort(siteBaseUri) == effectivePort(candidate);
	}

	private boolean allowedFetchSite(String secFetchSite) {
		if (!hasText(secFetchSite)) {
			return true;
		}
		var normalized = secFetchSite.trim().toLowerCase(Locale.US);
		return "same-origin".equals(normalized)
				|| "same-site".equals(normalized)
				|| "none".equals(normalized);
	}

	private boolean acceptOnce(
			String eventType,
			String slug,
			String trackedPath,
			String slot,
			String userAgent,
			String remoteAddress,
			Duration dedupeWindow
	) {
		var now = Instant.now();
		pruneRecentAcceptedEvents(now, dedupeWindow);
		var fingerprint = String.join("|",
				eventType,
				safe(slug),
				safe(trackedPath),
				safe(slot),
				safe(remoteAddress).trim(),
				safe(userAgent).trim().toLowerCase(Locale.US));
		var existing = recentAcceptedEvents.putIfAbsent(fingerprint, now);
		if (existing == null) {
			return true;
		}
		if (Duration.between(existing, now).compareTo(dedupeWindow) < 0) {
			return false;
		}
		recentAcceptedEvents.put(fingerprint, now);
		return true;
	}

	private void pruneRecentAcceptedEvents(Instant now, Duration dedupeWindow) {
		if (Duration.between(lastPruneAt, now).compareTo(PRUNE_INTERVAL) < 0) {
			return;
		}
		var cutoff = now.minus(dedupeWindow.multipliedBy(4));
		recentAcceptedEvents.entrySet().removeIf(entry -> entry.getValue().isBefore(cutoff));
		lastPruneAt = now;
	}

	private String normalizeTrackedPath(String value) {
		if (!hasText(value)) {
			return "";
		}
		var candidate = value.trim();
		if (!PATH_PATTERN.matcher(candidate).matches()) {
			return "";
		}
		return candidate;
	}

	private String normalizeSlot(String value) {
		if (!hasText(value)) {
			return "";
		}
		var candidate = value.trim().toLowerCase(Locale.US);
		return SLOT_PATTERN.matcher(candidate).matches() ? candidate : "";
	}

	private URI parseBaseUri(String siteBaseUrl) {
		try {
			return new URI(siteBaseUrl == null ? "https://leadlinerecord.com" : siteBaseUrl.trim());
		} catch (URISyntaxException exception) {
			return URI.create("https://leadlinerecord.com");
		}
	}

	private int effectivePort(URI uri) {
		if (uri.getPort() >= 0) {
			return uri.getPort();
		}
		return "https".equalsIgnoreCase(uri.getScheme()) ? 443 : 80;
	}

	private String normalizeHost(String value) {
		return value == null ? "" : value.trim().toLowerCase(Locale.US);
	}

	private String normalizeScheme(String value) {
		return value == null ? "" : value.trim().toLowerCase(Locale.US);
	}

	private boolean hasText(String value) {
		return value != null && !value.isBlank();
	}

	private String safe(String value) {
		return value == null ? "" : value;
	}

	private enum ReferrerState {
		MATCH,
		ABSENT,
		MISMATCH
	}
}
