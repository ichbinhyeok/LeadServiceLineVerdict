package owner.leadserviceline.pages;

import java.util.Optional;

import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import owner.leadserviceline.data.LeadServiceLineProperties;
import owner.leadserviceline.data.LeadServiceLineRepository;
import owner.leadserviceline.lookup.GeocodedAddress;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LeadServiceLinePageServiceTest {

	@Test
	void geocoderEnrichmentImprovesAddressOnlyLookup() {
		var objectMapper = JsonMapper.builder()
				.addModule(new JavaTimeModule())
				.build();
		var repository = new LeadServiceLineRepository(
				objectMapper,
				new LeadServiceLineProperties(
						"data",
						false,
						"https://geocoding.geo.census.gov",
						"Public_AR_Current",
						"Current_Current"
				)
		);
		var geocoder = (owner.leadserviceline.lookup.AddressGeocoder) (query, city, state) -> Optional.of(
				new GeocodedAddress("123 MAIN ST, MAPLEWOOD, MN, 55104", "Maplewood", "MN", "55104")
		);
		var pageService = new LeadServiceLinePageService(repository, geocoder);

		var page = pageService.lookupPage("123 Main St", null, null);

		assertFalse(page.matches().isEmpty());
		assertEquals("Saint Paul Regional Water Services", page.matches().getFirst().utility().utilityName());
		assertTrue(page.geocoderSummary().contains("MAPLEWOOD"));
		assertTrue(page.resultSummary().contains("after address normalization"));
		assertTrue(page.matches().getFirst().reasons().contains("Geocoder municipality matches maplewood"));
		assertEquals("High-confidence match", page.matches().getFirst().confidenceLabel());
	}
}
