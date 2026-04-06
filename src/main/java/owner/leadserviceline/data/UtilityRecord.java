package owner.leadserviceline.data;

import java.time.LocalDate;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record UtilityRecord(
		String utilityId,
		String utilitySlug,
		String utilityName,
		List<String> aliases,
		String city,
		String county,
		String state,
		String serviceAreaName,
		String serviceAreaType,
		String inventoryUrl,
		String lookupUrl,
		String notificationUrl,
		List<String> programIds,
		String contactPhone,
		String contactEmail,
		String inventoryStatus,
		String inventorySummary,
		LineCounts lineCounts,
		String addressLookupMode,
		List<String> resolverMunicipalities,
		List<String> resolverHints,
		List<String> resolverPostalPrefixes,
		List<String> sourceRefs,
		String verificationStatus,
		LocalDate lastVerified
) {
	public UtilityRecord {
		aliases = aliases == null ? List.of() : List.copyOf(aliases);
		programIds = programIds == null ? List.of() : List.copyOf(programIds);
		resolverMunicipalities = resolverMunicipalities == null ? List.of() : List.copyOf(resolverMunicipalities);
		resolverHints = resolverHints == null ? List.of() : List.copyOf(resolverHints);
		resolverPostalPrefixes = resolverPostalPrefixes == null ? List.of() : List.copyOf(resolverPostalPrefixes);
		sourceRefs = sourceRefs == null ? List.of() : List.copyOf(sourceRefs);
	}
}
