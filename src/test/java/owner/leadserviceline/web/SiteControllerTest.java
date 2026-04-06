package owner.leadserviceline.web;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
		"lead-service-line.census-geocoder-enabled=false",
		"lead-service-line.ops-review-enabled=true",
		"lead-service-line.ops-review-token=test-token",
		"lead-service-line.admin-enabled=true",
		"lead-service-line.admin-username=admin",
		"lead-service-line.admin-password=tlsgur3108",
		"lead-service-line.site-base-url=https://example.test"
})
@AutoConfigureMockMvc
class SiteControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Test
	void homePageRendersSeedUtilities() throws Exception {
		mockMvc.perform(get("/"))
				.andExpect(status().isOk())
				.andExpect(content().string(Matchers.containsString("DC Water")))
				.andExpect(content().string(Matchers.containsString("Denver Water")))
				.andExpect(content().string(Matchers.containsString("City of Columbus Division of Water")))
				.andExpect(content().string(Matchers.containsString("What is a lead service line?")));
	}

	@Test
	void utilityPageRendersFromJsonData() throws Exception {
		mockMvc.perform(get("/lead-service-line/dc/washington/dc-water"))
				.andExpect(status().isOk())
				.andExpect(content().string(Matchers.containsString("verified lead suspected lead no information")))
				.andExpect(content().string(Matchers.containsString("Where this utility stands now")))
				.andExpect(content().string(Matchers.containsString("Cost route status")))
				.andExpect(content().string(Matchers.containsString("Lead Line Record editorial review")))
				.andExpect(content().string(Matchers.containsString("mailto:shinhyeok22@gmail.com")));
	}

	@Test
	void dcOverviewShowsMultilingualLettersAndProgramSplit() throws Exception {
		mockMvc.perform(get("/lead-service-line/dc/washington/dc-water"))
				.andExpect(status().isOk())
				.andExpect(content().string(Matchers.containsString("galvanized iron letters")))
				.andExpect(content().string(Matchers.containsString("100 percent private-side assistance")))
				.andExpect(content().string(Matchers.containsString("voluntary full replacement path")));
	}

	@Test
	void programRouteRendersProgramSpecificContent() throws Exception {
		mockMvc.perform(get("/lead-service-line/wi/milwaukee/milwaukee-water-works/program"))
				.andExpect(status().isOk())
				.andExpect(content().string(Matchers.containsString("Verified replacement support")))
				.andExpect(content().string(Matchers.containsString("Owner Request Program")))
				.andExpect(content().string(Matchers.containsString("Income rules:")))
				.andExpect(content().string(Matchers.containsString("Coverage cautions")));
	}

	@Test
	void milwaukeeOverviewShowsLeadLineCount() throws Exception {
		mockMvc.perform(get("/lead-service-line/wi/milwaukee/milwaukee-water-works"))
				.andExpect(status().isOk())
				.andExpect(content().string(Matchers.containsString(">65000<")))
				.andExpect(content().string(Matchers.containsString("Known")));
	}

	@Test
	void lookupRouteResolvesLikelyUtilityFromCityAndState() throws Exception {
		mockMvc.perform(post("/lookup")
						.param("query", "123 Main St")
						.param("city", "Milwaukee")
						.param("state", "WI"))
				.andExpect(status().isOk())
				.andExpect(header().string("Cache-Control", Matchers.containsString("no-store")))
				.andExpect(header().string("Referrer-Policy", "no-referrer"))
				.andExpect(content().string(Matchers.containsString("Milwaukee Water Works")))
				.andExpect(content().string(Matchers.containsString("High-confidence match")))
				.andExpect(content().string(Matchers.containsString("/lead-service-line/wi/milwaukee/milwaukee-water-works")))
				.andExpect(content().string(Matchers.containsString("<meta name=\"robots\" content=\"noindex,nofollow\">")));
	}

	@Test
	void lookupRouteUsesPostalPrefixForRegionalUtility() throws Exception {
		mockMvc.perform(post("/lookup")
						.param("query", "55104")
						.param("state", "MN"))
				.andExpect(status().isOk())
				.andExpect(content().string(Matchers.containsString("Saint Paul Regional Water Services")))
				.andExpect(content().string(Matchers.containsString("Postal prefix matches 551")));
	}

	@Test
	void lookupRouteUsesCityInsideFreeformQueryForStrongerMatch() throws Exception {
		mockMvc.perform(post("/lookup")
						.param("query", "123 Main St Denver CO"))
				.andExpect(status().isOk())
				.andExpect(content().string(Matchers.containsString("Denver Water")))
				.andExpect(content().string(Matchers.containsString("Utility city appears in the lookup text")))
				.andExpect(content().string(Matchers.containsString("High-confidence match")));
	}

	@Test
	void lookupRouteTreatsSaintAbbreviationAsExactCityMatch() throws Exception {
		mockMvc.perform(post("/lookup")
						.param("city", "St Paul")
						.param("state", "MN"))
				.andExpect(status().isOk())
				.andExpect(content().string(Matchers.containsString("Saint Paul Regional Water Services")))
				.andExpect(content().string(Matchers.containsString("City matches saint-paul")))
				.andExpect(content().string(Matchers.containsString("High-confidence match")));
	}

	@Test
	void lookupRouteTreatsFortAbbreviationAsExactCityMatch() throws Exception {
		mockMvc.perform(post("/lookup")
						.param("city", "Ft Wayne")
						.param("state", "IN"))
				.andExpect(status().isOk())
				.andExpect(content().string(Matchers.containsString("Fort Wayne City Utilities")))
				.andExpect(content().string(Matchers.containsString("City matches fort-wayne")))
				.andExpect(content().string(Matchers.containsString("High-confidence match")));
	}

	@Test
	void lookupRouteUsesServiceAreaMunicipalityCoverage() throws Exception {
		mockMvc.perform(post("/lookup")
						.param("query", "123 Main St")
						.param("city", "Maplewood")
						.param("state", "MN"))
				.andExpect(status().isOk())
				.andExpect(content().string(Matchers.containsString("Saint Paul Regional Water Services")))
				.andExpect(content().string(Matchers.containsString("Service-area municipality matches maplewood")))
				.andExpect(content().string(Matchers.containsString("High-confidence match")));
	}

	@Test
	void lookupRoutePrefersExactCityAndOfficialLookup() throws Exception {
		mockMvc.perform(post("/lookup")
						.param("query", "123 Main St")
						.param("city", "Rockford")
						.param("state", "IL"))
				.andExpect(status().isOk())
				.andExpect(content().string(Matchers.containsString("Rockford Water Division")))
				.andExpect(content().string(Matchers.containsString("City matches rockford")))
				.andExpect(content().string(Matchers.containsString("High-confidence match")));
	}

	@Test
	void addedUtilityProgramRouteRenders() throws Exception {
		mockMvc.perform(get("/lead-service-line/mn/saint-paul/saint-paul-regional-water-services/program"))
				.andExpect(status().isOk())
				.andExpect(content().string(Matchers.containsString("Lead Free SPRWS")));
	}

	@Test
	void saintPaulCostRouteIsIndexableWhenNoCostProgramHasMethodologyNarrative() throws Exception {
		mockMvc.perform(get("/lead-service-line/mn/saint-paul/saint-paul-regional-water-services/replacement-cost"))
				.andExpect(status().isOk())
				.andExpect(content().string(Matchers.containsString("Methodology basis")))
				.andExpect(content().string(Matchers.containsString("Lead Free SPRWS 10-year replacement program")))
				.andExpect(content().string(Matchers.containsString("<meta name=\"robots\" content=\"index,follow\">")));
	}

	@Test
	void saintPaulOverviewShowsPartialStructuredCounts() throws Exception {
		mockMvc.perform(get("/lead-service-line/mn/saint-paul/saint-paul-regional-water-services"))
				.andExpect(status().isOk())
				.andExpect(content().string(Matchers.containsString(">17000<")))
				.andExpect(content().string(Matchers.containsString(">4500<")))
				.andExpect(content().string(Matchers.containsString("Known")))
				.andExpect(content().string(Matchers.containsString("Unknown")));
	}

	@Test
	void opsReviewRouteRendersReviewGroups() throws Exception {
		mockMvc.perform(get("/ops/review")
						.header("X-Ops-Token", "test-token"))
				.andExpect(status().isOk())
				.andExpect(header().string("Cache-Control", Matchers.containsString("no-store")))
				.andExpect(header().string("X-Robots-Tag", Matchers.containsString("noindex,nofollow")))
				.andExpect(content().string(Matchers.containsString("Snapshot quality checks for the launch cohort.")))
				.andExpect(content().string(Matchers.containsString("Narrative-only inventory records")))
				.andExpect(content().string(Matchers.containsString("Denver Water")))
				.andExpect(content().string(Matchers.containsString("noindex,nofollow")));
	}

	@Test
	void opsReviewExportReturnsJsonSnapshot() throws Exception {
		mockMvc.perform(get("/ops/review/export")
						.header("X-Ops-Token", "test-token"))
				.andExpect(status().isOk())
				.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
				.andExpect(header().string("Cache-Control", Matchers.containsString("no-store")))
				.andExpect(header().string("X-Robots-Tag", Matchers.containsString("noindex, nofollow")))
				.andExpect(jsonPath("$.generatedAt").exists())
				.andExpect(jsonPath("$.metrics[0].label").value("Utilities"))
				.andExpect(jsonPath("$.groups[0].title").value("Narrative-only inventory records"));
	}

	@Test
	void stateProgramsRouteRendersVerifiedPrograms() throws Exception {
		mockMvc.perform(get("/lead-service-line/pa/programs"))
				.andExpect(status().isOk())
				.andExpect(content().string(Matchers.containsString("PA lead service line programs")))
				.andExpect(content().string(Matchers.containsString("Philadelphia Water Department")))
				.andExpect(content().string(Matchers.containsString("Pittsburgh Water")));
	}

	@Test
	void evergreenGuideRouteRendersGuideContent() throws Exception {
		mockMvc.perform(get("/guides/who-pays-for-lead-service-line-replacement"))
				.andExpect(status().isOk())
				.andExpect(content().string(Matchers.containsString("Who pays for lead service line replacement?")))
				.andExpect(content().string(Matchers.containsString("Public-side replacement and private-side replacement are often funded under different rules.")));
	}

	@Test
	void homePageShowsBuyingGuidesAndDisclosure() throws Exception {
		mockMvc.perform(get("/"))
				.andExpect(status().isOk())
				.andExpect(content().string(Matchers.containsString("Best lead-reduction filters after a lead notice")))
				.andExpect(content().string(Matchers.containsString("Best water test kits after a lead notice")))
				.andExpect(content().string(Matchers.containsString("/affiliate-disclosure")));
	}

	@Test
	void affiliateDisclosurePageRenders() throws Exception {
		mockMvc.perform(get("/affiliate-disclosure"))
				.andExpect(status().isOk())
				.andExpect(content().string(Matchers.containsString("Affiliate and recommendation disclosure")))
				.andExpect(content().string(Matchers.containsString("no extra cost to the reader")));
	}

	@Test
	void replacementCostRouteLinksToBuyingGuides() throws Exception {
		mockMvc.perform(get("/lead-service-line/dc/washington/dc-water/replacement-cost"))
				.andExpect(status().isOk())
				.andExpect(content().string(Matchers.containsString("/guides/best-lead-reduction-filters-after-a-lead-notice")))
				.andExpect(content().string(Matchers.containsString("/guides/best-water-test-kits-after-a-lead-notice")));
	}

	@Test
	void buyingGuideRendersRecommendationCards() throws Exception {
		mockMvc.perform(get("/guides/best-lead-reduction-filters-after-a-lead-notice"))
				.andExpect(status().isOk())
				.andExpect(content().string(Matchers.containsString("Brita Elite Replacement Filters")))
				.andExpect(content().string(Matchers.containsString("Epic Smart Shield Under-Sink Water Filter System")))
				.andExpect(content().string(Matchers.containsString("/go/brita-elite-replacement-filters?slot=guide-card")))
				.andExpect(content().string(Matchers.containsString("/events/recommendation-impression?slug=brita-elite-replacement-filters&pagePath=/guides/best-lead-reduction-filters-after-a-lead-notice&slot=guide-card")));
	}

	@Test
	void recommendationRedirectUsesOfficialProductUrl() throws Exception {
		mockMvc.perform(get("/go/brita-elite-replacement-filters")
						.param("slot", "guide-card")
						.header("Referer", "https://example.test/guides/best-lead-reduction-filters-after-a-lead-notice"))
				.andExpect(status().is3xxRedirection())
				.andExpect(header().string("Location", "https://www.brita.com/products/elite-replacement-filters/"))
				.andExpect(header().string("Cache-Control", Matchers.containsString("no-store")))
				.andExpect(header().string("X-Robots-Tag", Matchers.containsString("noindex, nofollow")));
	}

	@Test
	void recommendationImpressionPixelResponds() throws Exception {
		mockMvc.perform(get("/events/recommendation-impression")
						.param("slug", "brita-elite-replacement-filters")
						.param("pagePath", "/guides/best-lead-reduction-filters-after-a-lead-notice")
						.param("slot", "guide-card"))
				.andExpect(status().isOk())
				.andExpect(content().contentTypeCompatibleWith(MediaType.IMAGE_GIF))
				.andExpect(header().string("Cache-Control", Matchers.containsString("no-store")));
	}

	@Test
	void utilityFilterPageShowsDirectQuickPickLinks() throws Exception {
		mockMvc.perform(get("/lead-service-line/co/denver/denver-water/filter-and-testing"))
				.andExpect(status().isOk())
				.andExpect(content().string(Matchers.containsString("/go/pur-plus-faucet-system-vertical?slot=utility-filter-testing-faucet")))
				.andExpect(content().string(Matchers.containsString("/go/tap-score-lead-and-copper?slot=utility-filter-testing-test")));
	}

	@Test
	void utilityReplacementCostPageShowsDirectQuickPickLinks() throws Exception {
		mockMvc.perform(get("/lead-service-line/dc/washington/dc-water/replacement-cost"))
				.andExpect(status().isOk())
				.andExpect(content().string(Matchers.containsString("/go/pur-plus-faucet-system-vertical?slot=utility-cost-faucet")))
				.andExpect(content().string(Matchers.containsString("/go/tap-score-essential-city-water?slot=utility-cost-test")));
	}

	@Test
	void newMinneapolisProgramRouteRenders() throws Exception {
		mockMvc.perform(get("/lead-service-line/mn/minneapolis/minneapolis-public-works-water/program"))
				.andExpect(status().isOk())
				.andExpect(content().string(Matchers.containsString("Lead Water Service Line Replacement")))
				.andExpect(content().string(Matchers.containsString("no cost")));
	}

	@Test
	void newDuluthProgramRouteRenders() throws Exception {
		mockMvc.perform(get("/lead-service-line/mn/duluth/duluth-lead-removal-program/program"))
				.andExpect(status().isOk())
				.andExpect(content().string(Matchers.containsString("Lead Service Line Replacement")))
				.andExpect(content().string(Matchers.containsString("City of Duluth")));
	}

	@Test
	void newColumbusCostRouteRenders() throws Exception {
		mockMvc.perform(get("/lead-service-line/oh/columbus/columbus-division-of-water/replacement-cost"))
				.andExpect(status().isOk())
				.andExpect(content().string(Matchers.containsString("$6000-$10000")))
				.andExpect(content().string(Matchers.containsString("Methodology basis")))
				.andExpect(content().string(Matchers.containsString("Owner payment trigger")))
				.andExpect(content().string(Matchers.containsString("Lead Elimination Assistance Program")))
				.andExpect(content().string(Matchers.containsString("Program offset")))
				.andExpect(content().string(Matchers.containsString("Cost cautions")));
	}

	@Test
	void bloomingtonIllinoisCostRouteIsIndexableWhenRateFundingMethodologyIsExplicit() throws Exception {
		mockMvc.perform(get("/lead-service-line/il/bloomington/bloomington-water-department/replacement-cost"))
				.andExpect(status().isOk())
				.andExpect(content().string(Matchers.containsString("Methodology basis")))
				.andExpect(content().string(Matchers.containsString("recent water rate increases")))
				.andExpect(content().string(Matchers.containsString("<meta name=\"robots\" content=\"index,follow\">")));
	}

	@Test
	void dcCostRouteIsIndexableWhenLocalMethodologyIsStrong() throws Exception {
		mockMvc.perform(get("/lead-service-line/dc/washington/dc-water/replacement-cost"))
				.andExpect(status().isOk())
				.andExpect(content().string(Matchers.containsString("$21000")))
				.andExpect(content().string(Matchers.containsString("Methodology basis")))
				.andExpect(content().string(Matchers.containsString("private-side-only and full replacement estimates")))
				.andExpect(content().string(Matchers.containsString("<meta name=\"robots\" content=\"index,follow\">")));
	}

	@Test
	void newPhoenixCostRouteRenders() throws Exception {
		mockMvc.perform(get("/lead-service-line/az/phoenix/phoenix-water-services/replacement-cost"))
				.andExpect(status().isOk())
				.andExpect(content().string(Matchers.containsString("no cost")))
				.andExpect(content().string(Matchers.containsString("Phoenix Water Service Line Replacement Plan")))
				.andExpect(content().string(Matchers.containsString("Methodology basis")))
				.andExpect(content().string(Matchers.containsString("meter box to the house connection")))
				.andExpect(content().string(Matchers.containsString("<meta name=\"robots\" content=\"index,follow\">")));
	}

	@Test
	void denverOverviewShowsLongerOutlookAndReimbursementRule() throws Exception {
		mockMvc.perform(get("/lead-service-line/co/denver/denver-water"))
				.andExpect(status().isOk())
				.andExpect(content().string(Matchers.containsString("60000 to 64000")))
				.andExpect(content().string(Matchers.containsString("2026 to 2031")))
				.andExpect(content().string(Matchers.containsString("3800 dollar reimbursement")));
	}

	@Test
	void columbusOverviewShowsProjectAreaAndWorkAgreementPath() throws Exception {
		mockMvc.perform(get("/lead-service-line/oh/columbus/columbus-division-of-water"))
				.andExpect(status().isOk())
				.andExpect(content().string(Matchers.containsString("active or upcoming project area")))
				.andExpect(content().string(Matchers.containsString("not water quality notices")))
				.andExpect(content().string(Matchers.containsString("signed work agreement")));
	}

	@Test
	void minneapolisOverviewShowsUnknownMeaningAnd2033Goal() throws Exception {
		mockMvc.perform(get("/lead-service-line/mn/minneapolis/minneapolis-public-works-water"))
				.andExpect(status().isOk())
				.andExpect(content().string(Matchers.containsString("updated daily")))
				.andExpect(content().string(Matchers.containsString("valid address but the matching record is not available")))
				.andExpect(content().string(Matchers.containsString("2033 state goal")));
	}

	@Test
	void phoenixOverviewShowsUnknownFollowUpAndFullCostCoverage() throws Exception {
		mockMvc.perform(get("/lead-service-line/az/phoenix/phoenix-water-services"))
				.andExpect(status().isOk())
				.andExpect(content().string(Matchers.containsString("unknown lines are unlikely to be lead")))
				.andExpect(content().string(Matchers.containsString("fall 2024")))
				.andExpect(content().string(Matchers.containsString("no cost")));
	}

	@Test
	void mesaOverviewShowsNoPublicLeadAndNoLetterNoActionRule() throws Exception {
		mockMvc.perform(get("/lead-service-line/az/mesa/mesa-water"))
				.andExpect(status().isOk())
				.andExpect(content().string(Matchers.containsString("no lead containing service lines connecting city mains to customer meters")))
				.andExpect(content().string(Matchers.containsString("if a letter was not sent no further action is needed")))
				.andExpect(content().string(Matchers.containsString("galvanized or unknown private-side material")));
	}

	@Test
	void newTempeOverviewRouteRenders() throws Exception {
		mockMvc.perform(get("/lead-service-line/az/tempe/tempe-water-utilities"))
				.andExpect(status().isOk())
				.andExpect(content().string(Matchers.containsString("City of Tempe Water Utilities")))
				.andExpect(content().string(Matchers.containsString("no records of any known public lead service lines")))
				.andExpect(content().string(Matchers.containsString("precautionary")))
				.andExpect(content().string(Matchers.containsString("replacement or funding options")));
	}

	@Test
	void tucsonOverviewShowsNoImmediateActionAndNoCostReplacement() throws Exception {
		mockMvc.perform(get("/lead-service-line/az/tucson/tucson-water"))
				.andExpect(status().isOk())
				.andExpect(content().string(Matchers.containsString("do not by themselves mean there is lead at the address")))
				.andExpect(content().string(Matchers.containsString("citywide inspections continue")))
				.andExpect(content().string(Matchers.containsString("no cost to the customer")));
	}

	@Test
	void monroeOverviewShowsStructuredLeadCountFact() throws Exception {
		mockMvc.perform(get("/lead-service-line/mi/monroe/monroe-water-wastewater"))
				.andExpect(status().isOk())
				.andExpect(content().string(Matchers.containsString(">1315<")))
				.andExpect(content().string(Matchers.containsString("Known")))
				.andExpect(content().string(Matchers.containsString("City of Monroe Water and Wastewater Department")));
	}

	@Test
	void updatedArizonaProgramsRouteIncludesPhoenixAndTucson() throws Exception {
		mockMvc.perform(get("/lead-service-line/az/programs"))
				.andExpect(status().isOk())
				.andExpect(content().string(Matchers.containsString("AZ lead service line programs")))
				.andExpect(content().string(Matchers.containsString("City of Phoenix Water Services")))
				.andExpect(content().string(Matchers.containsString("Tucson Water")));
	}

	@Test
	void newHamiltonOverviewRouteRenders() throws Exception {
		mockMvc.perform(get("/lead-service-line/oh/hamilton/hamilton-utilities-water"))
				.andExpect(status().isOk())
				.andExpect(content().string(Matchers.containsString("City of Hamilton Utilities + Public Works Water")))
				.andExpect(content().string(Matchers.containsString("best available data for both customer-owned and utility-owned portions")))
				.andExpect(content().string(Matchers.containsString("owner's responsibility")))
				.andExpect(content().string(Matchers.containsString("water main projects")));
	}

	@Test
	void akronOverviewShowsNoPrivateLeadAndUnknownVerificationPath() throws Exception {
		mockMvc.perform(get("/lead-service-line/oh/akron/akron-water-supply"))
				.andExpect(status().isOk())
				.andExpect(content().string(Matchers.containsString("no homeowner-owned lead service lines")))
				.andExpect(content().string(Matchers.containsString("phone-based photo workflow")))
				.andExpect(content().string(Matchers.containsString("end of 2025")));
	}

	@Test
	void newToledoCostRouteRenders() throws Exception {
		mockMvc.perform(get("/lead-service-line/oh/toledo/toledo-water-distribution/replacement-cost"))
				.andExpect(status().isOk())
				.andExpect(content().string(Matchers.containsString("There is no direct charge to the property owner for replacement under the current Toledo program")))
				.andExpect(content().string(Matchers.containsString("Methodology basis")))
				.andExpect(content().string(Matchers.containsString("45-day advance replacement letters")))
				.andExpect(content().string(Matchers.containsString("<meta name=\"robots\" content=\"index,follow\">")));
	}

	@Test
	void newChandlerOverviewRouteRenders() throws Exception {
		mockMvc.perform(get("/lead-service-line/az/chandler/chandler-water"))
				.andExpect(status().isOk())
				.andExpect(content().string(Matchers.containsString("City of Chandler Water Utilities")))
				.andExpect(content().string(Matchers.containsString("has not identified a lead service line")));
	}

	@Test
	void auroraOverviewShowsActionLevelAndSplitReplacementPaths() throws Exception {
		mockMvc.perform(get("/lead-service-line/il/aurora/aurora-water-production-division"))
				.andExpect(status().isOk())
				.andExpect(content().string(Matchers.containsString("2025")))
				.andExpect(content().string(Matchers.containsString("action-level exceedance")))
				.andExpect(content().string(Matchers.containsString("owner-managed early replacement")));
	}

	@Test
	void auroraCostRouteIsIndexableWhenTriggerRulesAreExplicit() throws Exception {
		mockMvc.perform(get("/lead-service-line/il/aurora/aurora-water-production-division/replacement-cost"))
				.andExpect(status().isOk())
				.andExpect(content().string(Matchers.containsString("Methodology basis")))
				.andExpect(content().string(Matchers.containsString("qualifying city trigger")))
				.andExpect(content().string(Matchers.containsString("<meta name=\"robots\" content=\"index,follow\">")));
	}

	@Test
	void newDetroitProgramRouteRenders() throws Exception {
		mockMvc.perform(get("/lead-service-line/mi/detroit/detroit-water-and-sewerage-department/program"))
				.andExpect(status().isOk())
				.andExpect(content().string(Matchers.containsString("DWSD Neighborhood Lead Service Line Replacement Program")))
				.andExpect(content().string(Matchers.containsString("homeowner permission")));
	}

	@Test
	void detroitOverviewShowsNoticeScaleAndModeledLeadCount() throws Exception {
		mockMvc.perform(get("/lead-service-line/mi/detroit/detroit-water-and-sewerage-department"))
				.andExpect(status().isOk())
				.andExpect(content().string(Matchers.containsString("123387")))
				.andExpect(content().string(Matchers.containsString("80000")))
				.andExpect(content().string(Matchers.containsString("15000")));
	}

	@Test
	void michiganProgramsRouteIncludesDetroitAndStJoseph() throws Exception {
		mockMvc.perform(get("/lead-service-line/mi/programs"))
				.andExpect(status().isOk())
				.andExpect(content().string(Matchers.containsString("MI lead service line programs")))
				.andExpect(content().string(Matchers.containsString("Detroit Water and Sewerage Department")))
				.andExpect(content().string(Matchers.containsString("City of St. Joseph Water and Sewer Department")));
	}

	@Test
	void stJosephOverviewShowsDsmiMapAndInspectionWorkflow() throws Exception {
		mockMvc.perform(get("/lead-service-line/mi/st-joseph/st-joseph-water-and-sewer"))
				.andExpect(status().isOk())
				.andExpect(content().string(Matchers.containsString("public DSMI map")))
				.andExpect(content().string(Matchers.containsString("point-of-entry inspections")))
				.andExpect(content().string(Matchers.containsString("voluntary testing workflow")));
	}

	@Test
	void bloomingtonOverviewRouteRenders() throws Exception {
		mockMvc.perform(get("/lead-service-line/in/bloomington/bloomington-utilities"))
				.andExpect(status().isOk())
				.andExpect(content().string(Matchers.containsString("City of Bloomington Utilities")))
				.andExpect(content().string(Matchers.containsString("27000")))
				.andExpect(content().string(Matchers.containsString("right-of-entry forms for field verification")));
	}

	@Test
	void citizensProgramRouteRenders() throws Exception {
		mockMvc.perform(get("/lead-service-line/in/indianapolis/citizens-energy-group/program"))
				.andExpect(status().isOk())
				.andExpect(content().string(Matchers.containsString("Citizens Lead Service Line Replacement Program")))
				.andExpect(content().string(Matchers.containsString("no additional cost")));
	}

	@Test
	void citizensOverviewShowsAnnualLetterAndInventoryScale() throws Exception {
		mockMvc.perform(get("/lead-service-line/in/indianapolis/citizens-energy-group"))
				.andExpect(status().isOk())
				.andExpect(content().string(Matchers.containsString("75000")))
				.andExpect(content().string(Matchers.containsString("annual notices")))
				.andExpect(content().string(Matchers.containsString("no additional cost")));
	}

	@Test
	void kendallvilleCostRouteRenders() throws Exception {
		mockMvc.perform(get("/lead-service-line/in/kendallville/kendallville-water-department/replacement-cost"))
				.andExpect(status().isOk())
				.andExpect(content().string(Matchers.containsString("no additional cost")))
				.andExpect(content().string(Matchers.containsString("Methodology basis")))
				.andExpect(content().string(Matchers.containsString("March 2026")))
				.andExpect(content().string(Matchers.containsString("<meta name=\"robots\" content=\"index,follow\">")));
	}

	@Test
	void speedwayOverviewShowsInventoryScaleAndUnknownReductionPath() throws Exception {
		mockMvc.perform(get("/lead-service-line/in/speedway/speedway-water-works"))
				.andExpect(status().isOk())
				.andExpect(content().string(Matchers.containsString("4300")))
				.andExpect(content().string(Matchers.containsString("targeted field investigation")))
				.andExpect(content().string(Matchers.containsString("letters to lead and unknown addresses")));
	}

	@Test
	void evansvilleOverviewShowsNovemberNoticeAndDownloadableCopies() throws Exception {
		mockMvc.perform(get("/lead-service-line/in/evansville/evansville-water-and-sewer-utility"))
				.andExpect(status().isOk())
				.andExpect(content().string(Matchers.containsString("November 2024")))
				.andExpect(content().string(Matchers.containsString("downloadable lead GRR and unknown notice copies")))
				.andExpect(content().string(Matchers.containsString("field verification")));
	}

	@Test
	void indianaProgramsRouteIncludesCitizensAndKendallville() throws Exception {
		mockMvc.perform(get("/lead-service-line/in/programs"))
				.andExpect(status().isOk())
				.andExpect(content().string(Matchers.containsString("IN lead service line programs")))
				.andExpect(content().string(Matchers.containsString("Citizens Energy Group")))
				.andExpect(content().string(Matchers.containsString("City of Kendallville Water Department")));
	}

	@Test
	void southBendOverviewRouteRenders() throws Exception {
		mockMvc.perform(get("/lead-service-line/in/south-bend/south-bend-water-works"))
				.andExpect(status().isOk())
				.andExpect(content().string(Matchers.containsString("City of South Bend Water Works")))
				.andExpect(content().string(Matchers.containsString("34000")))
				.andExpect(content().string(Matchers.containsString("coordinate free lead testing")))
				.andExpect(content().string(Matchers.containsString("private-side verification")));
	}

	@Test
	void lafayetteProgramRouteRenders() throws Exception {
		mockMvc.perform(get("/lead-service-line/in/lafayette/lafayette-water-works/program"))
				.andExpect(status().isOk())
				.andExpect(content().string(Matchers.containsString("Lead Service Line Replacement Program")))
				.andExpect(content().string(Matchers.containsString("no cost")));
	}

	@Test
	void lafayetteOverviewShowsProjectAreaNoticeDetails() throws Exception {
		mockMvc.perform(get("/lead-service-line/in/lafayette/lafayette-water-works"))
				.andExpect(status().isOk())
				.andExpect(content().string(Matchers.containsString("3200")))
				.andExpect(content().string(Matchers.containsString("45-day")))
				.andExpect(content().string(Matchers.containsString("right-of-entry")));
	}

	@Test
	void lafayetteCostRouteIsIndexableWhenProjectAreaMethodologyIsExplicit() throws Exception {
		mockMvc.perform(get("/lead-service-line/in/lafayette/lafayette-water-works/replacement-cost"))
				.andExpect(status().isOk())
				.andExpect(content().string(Matchers.containsString("Methodology basis")))
				.andExpect(content().string(Matchers.containsString("45-day construction notice")))
				.andExpect(content().string(Matchers.containsString("<meta name=\"robots\" content=\"index,follow\">")));
	}

	@Test
	void mishawakaOverviewShowsUtilitySideCoordinationTiming() throws Exception {
		mockMvc.perform(get("/lead-service-line/in/mishawaka/mishawaka-utilities"))
				.andExpect(status().isOk())
				.andExpect(content().string(Matchers.containsString("4100")))
				.andExpect(content().string(Matchers.containsString("45 days")))
				.andExpect(content().string(Matchers.containsString("late 2024")));
	}

	@Test
	void bloomingtonIllinoisCostRouteRenders() throws Exception {
		mockMvc.perform(get("/lead-service-line/il/bloomington/bloomington-water-department/replacement-cost"))
				.andExpect(status().isOk())
				.andExpect(content().string(Matchers.containsString("no additional cost")))
				.andExpect(content().string(Matchers.containsString("Lead Service Line Replacement Project")));
	}

	@Test
	void bloomingtonIllinoisOverviewShowsDashboardAnd2027FundingCaveat() throws Exception {
		mockMvc.perform(get("/lead-service-line/il/bloomington/bloomington-water-department"))
				.andExpect(status().isOk())
				.andExpect(content().string(Matchers.containsString("interactive map and dashboard with lead GRR unknown and non-lead categories")))
				.andExpect(content().string(Matchers.containsString("2027")))
				.andExpect(content().string(Matchers.containsString("rate-funded path")))
				.andExpect(content().string(Matchers.containsString("final customer-side coverage")));
	}

	@Test
	void chicagoProgramRouteRenders() throws Exception {
		mockMvc.perform(get("/lead-service-line/il/chicago/chicago-water-department/program"))
				.andExpect(status().isOk())
				.andExpect(content().string(Matchers.containsString("Lead Service Line Replacement Program")))
				.andExpect(content().string(Matchers.containsString("citywide lead service line replacement program")))
				.andExpect(content().string(Matchers.containsString("https://www.leadsafechicago.org/lead-service-line-replacement")));
	}

	@Test
	void jolietCostRouteRenders() throws Exception {
		mockMvc.perform(get("/lead-service-line/il/joliet/joliet-public-utilities/replacement-cost"))
				.andExpect(status().isOk())
				.andExpect(content().string(Matchers.containsString("no cost to the homeowner")))
				.andExpect(content().string(Matchers.containsString("Methodology basis")))
				.andExpect(content().string(Matchers.containsString("<meta name=\"robots\" content=\"index,follow\">")));
	}

	@Test
	void jolietOverviewShowsTriggerBasedNoCostRules() throws Exception {
		mockMvc.perform(get("/lead-service-line/il/joliet/joliet-public-utilities"))
				.andExpect(status().isOk())
				.andExpect(content().string(Matchers.containsString("water main rehabilitation")))
				.andExpect(content().string(Matchers.containsString("point-of-use pitchers")))
				.andExpect(content().string(Matchers.containsString("no cost")));
	}

	@Test
	void rockfordProgramRouteRenders() throws Exception {
		mockMvc.perform(get("/lead-service-line/il/rockford/rockford-water-division/program"))
				.andExpect(status().isOk())
				.andExpect(content().string(Matchers.containsString("Lead Service Line Replacement Program")))
				.andExpect(content().string(Matchers.containsString("Illinois SRF")));
	}

	@Test
	void rockfordOverviewShowsTestingAndPrivateSideResponsibility() throws Exception {
		mockMvc.perform(get("/lead-service-line/il/rockford/rockford-water-division"))
				.andExpect(status().isOk())
				.andExpect(content().string(Matchers.containsString("free annual lead testing")))
				.andExpect(content().string(Matchers.containsString("owner covers the private side")))
				.andExpect(content().string(Matchers.containsString("interactive service line map")));
	}

	@Test
	void evanstonProgramRouteRenders() throws Exception {
		mockMvc.perform(get("/lead-service-line/il/evanston/evanston-water-production-bureau/program"))
				.andExpect(status().isOk())
				.andExpect(content().string(Matchers.containsString("Homeowner Initiated LSLR")))
				.andExpect(content().string(Matchers.containsString("waived permit fees")));
	}

	@Test
	void evanstonOverviewShowsProjectAreaAndHomeownerInitiatedSplit() throws Exception {
		mockMvc.perform(get("/lead-service-line/il/evanston/evanston-water-production-bureau"))
				.andExpect(status().isOk())
				.andExpect(content().string(Matchers.containsString("April 2025")))
				.andExpect(content().string(Matchers.containsString("project areas")))
				.andExpect(content().string(Matchers.containsString("waived permit fees")));
	}

	@Test
	void evanstonCostRouteIsIndexableWhenAnnualProjectAndPilotAreSeparated() throws Exception {
		mockMvc.perform(get("/lead-service-line/il/evanston/evanston-water-production-bureau/replacement-cost"))
				.andExpect(status().isOk())
				.andExpect(content().string(Matchers.containsString("Methodology basis")))
				.andExpect(content().string(Matchers.containsString("$2500 reimbursement")))
				.andExpect(content().string(Matchers.containsString("<meta name=\"robots\" content=\"index,follow\">")));
	}

	@Test
	void greenBayOverviewRouteRenders() throws Exception {
		mockMvc.perform(get("/lead-service-line/wi/green-bay/green-bay-water-utility"))
				.andExpect(status().isOk())
				.andExpect(content().string(Matchers.containsString("Green Bay Water Utility")))
				.andExpect(content().string(Matchers.containsString("customer-side and utility-side lead GRR unknown and non-lead records")))
				.andExpect(content().string(Matchers.containsString("inspection or documentation follow-up")))
				.andExpect(content().string(Matchers.containsString("limited-time municipal replacement contacts")));
	}

	@Test
	void grandRapidsProgramRouteRenders() throws Exception {
		mockMvc.perform(get("/lead-service-line/mi/grand-rapids/grand-rapids-water-system/program"))
				.andExpect(status().isOk())
				.andExpect(content().string(Matchers.containsString("Lead Line Replacement Program")))
				.andExpect(content().string(Matchers.containsString("ten-pay")));
	}

	@Test
	void grandRapidsOverviewShowsAssumedLeadAndEligibilityRules() throws Exception {
		mockMvc.perform(get("/lead-service-line/mi/grand-rapids/grand-rapids-water-system"))
				.andExpect(status().isOk())
				.andExpect(content().string(Matchers.containsString("built before 1950")))
				.andExpect(content().string(Matchers.containsString("assumed to have lead service lines")))
				.andExpect(content().string(Matchers.containsString("ten-pay")));
	}

	@Test
	void grandRapidsCostRouteIsIndexableWhenLeakAndVoluntaryPathsAreSeparated() throws Exception {
		mockMvc.perform(get("/lead-service-line/mi/grand-rapids/grand-rapids-water-system/replacement-cost"))
				.andExpect(status().isOk())
				.andExpect(content().string(Matchers.containsString("Methodology basis")))
				.andExpect(content().string(Matchers.containsString("Water Service Agreement")))
				.andExpect(content().string(Matchers.containsString("<meta name=\"robots\" content=\"index,follow\">")));
	}

	@Test
	void fortWayneCostRouteRenders() throws Exception {
		mockMvc.perform(get("/lead-service-line/in/fort-wayne/fort-wayne-city-utilities/replacement-cost"))
				.andExpect(status().isOk())
				.andExpect(content().string(Matchers.containsString("grant-funded")))
				.andExpect(content().string(Matchers.containsString("Methodology basis")))
				.andExpect(content().string(Matchers.containsString("<meta name=\"robots\" content=\"index,follow\">")));
	}

	@Test
	void fortWayneOverviewShowsOwnerResponsibilityAndGrantAreaSplit() throws Exception {
		mockMvc.perform(get("/lead-service-line/in/fort-wayne/fort-wayne-city-utilities"))
				.andExpect(status().isOk())
				.andExpect(content().string(Matchers.containsString("before 1937")))
				.andExpect(content().string(Matchers.containsString("private pipe to the meter")))
				.andExpect(content().string(Matchers.containsString("selected neighborhoods")));
	}

	@Test
	void kalamazooProgramRouteRenders() throws Exception {
		mockMvc.perform(get("/lead-service-line/mi/kalamazoo/kalamazoo-water-department/program"))
				.andExpect(status().isOk())
				.andExpect(content().string(Matchers.containsString("Lead Service Line Replacement Program")))
				.andExpect(content().string(Matchers.containsString("no charge")));
	}

	@Test
	void kalamazooOverviewShowsTestingAsInterimSupport() throws Exception {
		mockMvc.perform(get("/lead-service-line/mi/kalamazoo/kalamazoo-water-department"))
				.andExpect(status().isOk())
				.andExpect(content().string(Matchers.containsString("service material lookup tool to check an address")))
				.andExpect(content().string(Matchers.containsString("free lead and copper testing plus filters are interim support")))
				.andExpect(content().string(Matchers.containsString("testing workflow itself")));
	}

	@Test
	void kalamazooCostRouteIsIndexableWhenFundingAndSchedulingAreExplicit() throws Exception {
		mockMvc.perform(get("/lead-service-line/mi/kalamazoo/kalamazoo-water-department/replacement-cost"))
				.andExpect(status().isOk())
				.andExpect(content().string(Matchers.containsString("Methodology basis")))
				.andExpect(content().string(Matchers.containsString("state revolving fund support")))
				.andExpect(content().string(Matchers.containsString("<meta name=\"robots\" content=\"index,follow\">")));
	}

	@Test
	void racineCostRouteRenders() throws Exception {
		mockMvc.perform(get("/lead-service-line/wi/racine/racine-water-utility/replacement-cost"))
				.andExpect(status().isOk())
				.andExpect(content().string(Matchers.containsString("free of charge")))
				.andExpect(content().string(Matchers.containsString("Methodology basis")))
				.andExpect(content().string(Matchers.containsString("invitation workflow")))
				.andExpect(content().string(Matchers.containsString("<meta name=\"robots\" content=\"index,follow\">")));
	}

	@Test
	void springfieldOverviewRouteRenders() throws Exception {
		mockMvc.perform(get("/lead-service-line/mo/springfield/springfield-water-utility"))
				.andExpect(status().isOk())
				.andExpect(content().string(Matchers.containsString("City Utilities of Springfield")))
				.andExpect(content().string(Matchers.containsString("38000")))
				.andExpect(content().string(Matchers.containsString("before 1989")))
				.andExpect(content().string(Matchers.containsString("free neighborhood assistance")));
	}

	@Test
	void kansasCityOverviewShowsStructuredCountsAndProtectionLetterContext() throws Exception {
		mockMvc.perform(get("/lead-service-line/mo/kansas-city/kansas-city-water"))
				.andExpect(status().isOk())
				.andExpect(content().string(Matchers.containsString(">0<")))
				.andExpect(content().string(Matchers.containsString(">23109<")))
				.andExpect(content().string(Matchers.containsString(">24842<")))
				.andExpect(content().string(Matchers.containsString("optional homeowner coverage")));
	}

	@Test
	void columbiaOverviewRouteRenders() throws Exception {
		mockMvc.perform(get("/lead-service-line/mo/columbia/columbia-water-utility"))
				.andExpect(status().isOk())
				.andExpect(content().string(Matchers.containsString("City of Columbia Water Utility")))
				.andExpect(content().string(Matchers.containsString("searchable assessment map")))
				.andExpect(content().string(Matchers.containsString("online survey")))
				.andExpect(content().string(Matchers.containsString("inspection guidance")));
	}

	@Test
	void independenceOverviewShowsPdfInventoryAndStaffHelp() throws Exception {
		mockMvc.perform(get("/lead-service-line/mo/independence/independence-water-department"))
				.andExpect(status().isOk())
				.andExpect(content().string(Matchers.containsString("current PDF inventory")))
				.andExpect(content().string(Matchers.containsString("galvanized requiring replacement")))
				.andExpect(content().string(Matchers.containsString("staff help")));
	}

	@Test
	void independenceNotificationRouteRenders() throws Exception {
		mockMvc.perform(get("/lead-service-line/mo/independence/independence-water-department/notification"))
				.andExpect(status().isOk())
				.andExpect(content().string(Matchers.containsString("City of Independence Water Department")))
				.andExpect(content().string(Matchers.containsString("galvanized requiring replacement")))
				.andExpect(content().string(Matchers.containsString("Do not overread this notice")))
				.andExpect(content().string(Matchers.containsString("Address confirmation step")));
	}

	@Test
	void omahaProgramRouteRenders() throws Exception {
		mockMvc.perform(get("/lead-service-line/ne/omaha/mud-omaha/program"))
				.andExpect(status().isOk())
				.andExpect(content().string(Matchers.containsString("Lead Service Line Replacement Program")))
				.andExpect(content().string(Matchers.containsString("free of charge")));
	}

	@Test
	void omahaOverviewShowsUnknownSupportAnd2024ProgramStart() throws Exception {
		mockMvc.perform(get("/lead-service-line/ne/omaha/mud-omaha"))
				.andExpect(status().isOk())
				.andExpect(content().string(Matchers.containsString("lead-status-unknown")))
				.andExpect(content().string(Matchers.containsString("sample kits")))
				.andExpect(content().string(Matchers.containsString("since 2024")));
	}

	@Test
	void omahaCostRouteRenders() throws Exception {
		mockMvc.perform(get("/lead-service-line/ne/omaha/mud-omaha/replacement-cost"))
				.andExpect(status().isOk())
				.andExpect(content().string(Matchers.containsString("$8000")))
				.andExpect(content().string(Matchers.containsString("average replacement value")))
				.andExpect(content().string(Matchers.containsString("Lead Service Line Replacement Program")))
				.andExpect(content().string(Matchers.containsString("<meta name=\"robots\" content=\"index,follow\">")));
	}

	@Test
	void iowaAmericanProgramRouteRenders() throws Exception {
		mockMvc.perform(get("/lead-service-line/ia/davenport/iowa-american-water/program"))
				.andExpect(status().isOk())
				.andExpect(content().string(Matchers.containsString("Lead Service Line Replacement Program")))
				.andExpect(content().string(Matchers.containsString("no direct cost")));
	}

	@Test
	void iowaCityOverviewShowsFlushingAndCostShareWorkflow() throws Exception {
		mockMvc.perform(get("/lead-service-line/ia/iowa-city/iowa-city-water-division"))
				.andExpect(status().isOk())
				.andExpect(content().string(Matchers.containsString("flush stagnant water")))
				.andExpect(content().string(Matchers.containsString("cost-share reimbursement")))
				.andExpect(content().string(Matchers.containsString("before work begins")));
	}

	@Test
	void iowaAmericanOverviewShowsReplacementProgress() throws Exception {
		mockMvc.perform(get("/lead-service-line/ia/davenport/iowa-american-water"))
				.andExpect(status().isOk())
				.andExpect(content().string(Matchers.containsString("1300")))
				.andExpect(content().string(Matchers.containsString("self-report prompts")))
				.andExpect(content().string(Matchers.containsString("no direct cost")));
	}

	@Test
	void iowaAmericanCostRouteIsIndexableWhenProgramProgressIsLocal() throws Exception {
		mockMvc.perform(get("/lead-service-line/ia/davenport/iowa-american-water/replacement-cost"))
				.andExpect(status().isOk())
				.andExpect(content().string(Matchers.containsString("Methodology basis")))
				.andExpect(content().string(Matchers.containsString("1300 completed customer-owned replacements")))
				.andExpect(content().string(Matchers.containsString("<meta name=\"robots\" content=\"index,follow\">")));
	}

	@Test
	void iowaProgramsRouteIncludesIowaAmericanWater() throws Exception {
		mockMvc.perform(get("/lead-service-line/ia/programs"))
				.andExpect(status().isOk())
				.andExpect(content().string(Matchers.containsString("IA lead service line programs")))
				.andExpect(content().string(Matchers.containsString("Iowa American Water")))
				.andExpect(content().string(Matchers.containsString("City of Dubuque Water Department")));
	}

	@Test
	void westDesMoinesOverviewRouteRenders() throws Exception {
		mockMvc.perform(get("/lead-service-line/ia/west-des-moines/west-des-moines-water-works"))
				.andExpect(status().isOk())
				.andExpect(content().string(Matchers.containsString("West Des Moines Water Works")))
				.andExpect(content().string(Matchers.containsString(">7000<")))
				.andExpect(content().string(Matchers.containsString("Unknown")));
	}

	@Test
	void lansingOverviewShowsNoRemainingLeadOrUnknownLines() throws Exception {
		mockMvc.perform(get("/lead-service-line/mi/lansing/lansing-board-of-water-light"))
				.andExpect(status().isOk())
				.andExpect(content().string(Matchers.containsString(">0<")))
				.andExpect(content().string(Matchers.containsString("Known")))
				.andExpect(content().string(Matchers.containsString("Unknown")));
	}

	@Test
	void flintOverviewShowsReplacementProgressAndOptIn() throws Exception {
		mockMvc.perform(get("/lead-service-line/mi/flint/flint-water-service-center"))
				.andExpect(status().isOk())
				.andExpect(content().string(Matchers.containsString("97 percent")))
				.andExpect(content().string(Matchers.containsString("immediately opt in")))
				.andExpect(content().string(Matchers.containsString("no cost")));
	}

	@Test
	void flintCostRouteIsIndexableWhenConsentWorkflowIsExplicit() throws Exception {
		mockMvc.perform(get("/lead-service-line/mi/flint/flint-water-service-center/replacement-cost"))
				.andExpect(status().isOk())
				.andExpect(content().string(Matchers.containsString("Methodology basis")))
				.andExpect(content().string(Matchers.containsString("consent forms")))
				.andExpect(content().string(Matchers.containsString("<meta name=\"robots\" content=\"index,follow\">")));
	}

	@Test
	void jacksonOverviewShowsEarlySignupAndMultiChannelNotice() throws Exception {
		mockMvc.perform(get("/lead-service-line/mi/jackson/jackson-water-department"))
				.andExpect(status().isOk())
				.andExpect(content().string(Matchers.containsString("sign up early")))
				.andExpect(content().string(Matchers.containsString("door notices")))
				.andExpect(content().string(Matchers.containsString("dedicated crew")));
	}

	@Test
	void jacksonCostRouteIsIndexableWhenAreaSchedulingIsExplicit() throws Exception {
		mockMvc.perform(get("/lead-service-line/mi/jackson/jackson-water-department/replacement-cost"))
				.andExpect(status().isOk())
				.andExpect(content().string(Matchers.containsString("Methodology basis")))
				.andExpect(content().string(Matchers.containsString("DWSRF loans local revenue and grants")))
				.andExpect(content().string(Matchers.containsString("<meta name=\"robots\" content=\"index,follow\">")));
	}

	@Test
	void dearbornOverviewShowsAnnualTestingScaleAndSurveyPrompt() throws Exception {
		mockMvc.perform(get("/lead-service-line/mi/dearborn/dearborn-water-sewerage-division"))
				.andExpect(status().isOk())
				.andExpect(content().string(Matchers.containsString("100 homes are tested annually")))
				.andExpect(content().string(Matchers.containsString("federal action levels")))
				.andExpect(content().string(Matchers.containsString("service line survey")));
	}

	@Test
	void amesCostRouteRenders() throws Exception {
		mockMvc.perform(get("/lead-service-line/ia/ames/ames-water-pollution-control/replacement-cost"))
				.andExpect(status().isOk())
				.andExpect(content().string(Matchers.containsString("Homeowners own the water service line to the meter")))
				.andExpect(content().string(Matchers.containsString("No official local bid range is published")));
	}

	@Test
	void councilBluffsProgramRouteRenders() throws Exception {
		mockMvc.perform(get("/lead-service-line/ia/council-bluffs/council-bluffs-water-works/program"))
				.andExpect(status().isOk())
				.andExpect(content().string(Matchers.containsString("Lead Service Line Replacement Project")))
				.andExpect(content().string(Matchers.containsString("Council Bluffs Water Works")));
	}

	@Test
	void blueSpringsOverviewShowsAddressCheckAndFlushingGuidance() throws Exception {
		mockMvc.perform(get("/lead-service-line/mo/blue-springs/blue-springs-water-services"))
				.andExpect(status().isOk())
				.andExpect(content().string(Matchers.containsString("address-check document")))
				.andExpect(content().string(Matchers.containsString("flushing exposure-reduction")))
				.andExpect(content().string(Matchers.containsString("resident private-side survey")));
	}

	@Test
	void councilBluffsOverviewShowsMapListsAndProjectNotice() throws Exception {
		mockMvc.perform(get("/lead-service-line/ia/council-bluffs/council-bluffs-water-works"))
				.andExpect(status().isOk())
				.andExpect(content().string(Matchers.containsString("galvanized-requiring-replacement")))
				.andExpect(content().string(Matchers.containsString("SRF-funded project notice")))
				.andExpect(content().string(Matchers.containsString("public address map")));
	}

	@Test
	void dubuqueProgramRouteRenders() throws Exception {
		mockMvc.perform(get("/lead-service-line/ia/dubuque/dubuque-water-department/program"))
				.andExpect(status().isOk())
				.andExpect(content().string(Matchers.containsString("Lead Service Line Replacement Pilot Program")))
				.andExpect(content().string(Matchers.containsString("no cost to the owner or tenant")));
	}

	@Test
	void dubuqueOverviewShowsPilotTargetCount() throws Exception {
		mockMvc.perform(get("/lead-service-line/ia/dubuque/dubuque-water-department"))
				.andExpect(status().isOk())
				.andExpect(content().string(Matchers.containsString("585")))
				.andExpect(content().string(Matchers.containsString("three phases")));
	}

	@Test
	void siouxCityOverviewRouteRenders() throws Exception {
		mockMvc.perform(get("/lead-service-line/ia/sioux-city/sioux-city-water-plant"))
				.andExpect(status().isOk())
				.andExpect(content().string(Matchers.containsString("City of Sioux City Water Plant")))
				.andExpect(content().string(Matchers.containsString("7300 properties")));
	}

	@Test
	void nebraskaProgramsRouteIncludesMud() throws Exception {
		mockMvc.perform(get("/lead-service-line/ne/programs"))
				.andExpect(status().isOk())
				.andExpect(content().string(Matchers.containsString("NE lead service line programs")))
				.andExpect(content().string(Matchers.containsString("Metropolitan Utilities District")))
				.andExpect(content().string(Matchers.containsString("Hastings Utilities Water Department")));
	}

	@Test
	void grandIslandProgramRouteRenders() throws Exception {
		mockMvc.perform(get("/lead-service-line/ne/grand-island/grand-island-utilities-department/program"))
				.andExpect(status().isOk())
				.andExpect(content().string(Matchers.containsString("Grand Island Utilities Department")))
				.andExpect(content().string(Matchers.containsString("Lead Service Line Replacement Project")));
	}

	@Test
	void hastingsProgramRouteRenders() throws Exception {
		mockMvc.perform(get("/lead-service-line/ne/hastings/hastings-utilities-water/program"))
				.andExpect(status().isOk())
				.andExpect(content().string(Matchers.containsString("Water Main and Lead Service Replacement Program")))
				.andExpect(content().string(Matchers.containsString("city-managed neighborhood replacement program")));
	}

	@Test
	void hastingsOverviewShowsStructuredLeadCount() throws Exception {
		mockMvc.perform(get("/lead-service-line/ne/hastings/hastings-utilities-water"))
				.andExpect(status().isOk())
				.andExpect(content().string(Matchers.containsString(">1500<")))
				.andExpect(content().string(Matchers.containsString("Known")))
				.andExpect(content().string(Matchers.containsString("279")));
	}

	@Test
	void missouriProgramsRouteIncludesMissouriAmericanWater() throws Exception {
		mockMvc.perform(get("/lead-service-line/mo/programs"))
				.andExpect(status().isOk())
				.andExpect(content().string(Matchers.containsString("MO lead service line programs")))
				.andExpect(content().string(Matchers.containsString("Missouri American Water")));
	}

	@Test
	void missouriAmericanProgramRouteRenders() throws Exception {
		mockMvc.perform(get("/lead-service-line/mo/st-louis/missouri-american-water/program"))
				.andExpect(status().isOk())
				.andExpect(content().string(Matchers.containsString("Missouri American Water")))
				.andExpect(content().string(Matchers.containsString("no direct cost")));
	}

	@Test
	void missouriAmericanCostRouteIsIndexableWhenVerificationWorkflowIsExplicit() throws Exception {
		mockMvc.perform(get("/lead-service-line/mo/st-louis/missouri-american-water/replacement-cost"))
				.andExpect(status().isOk())
				.andExpect(content().string(Matchers.containsString("Methodology basis")))
				.andExpect(content().string(Matchers.containsString("self-report or inspection")))
				.andExpect(content().string(Matchers.containsString("<meta name=\"robots\" content=\"index,follow\">")));
	}

	@Test
	void blueSpringsOverviewRouteRenders() throws Exception {
		mockMvc.perform(get("/lead-service-line/mo/blue-springs/blue-springs-water-services"))
				.andExpect(status().isOk())
				.andExpect(content().string(Matchers.containsString("City of Blue Springs Water and Sewer Services")))
				.andExpect(content().string(Matchers.containsString("address-check document with a resident private-side survey")));
	}

	@Test
	void lookupRouteRecognizesFullStateNamesForNewMissouriCoverage() throws Exception {
		mockMvc.perform(post("/lookup")
						.param("query", "64015")
						.param("city", "Blue Springs")
						.param("state", "Missouri"))
				.andExpect(status().isOk())
				.andExpect(content().string(Matchers.containsString("City of Blue Springs Water and Sewer Services")))
				.andExpect(content().string(Matchers.containsString("High-confidence match")));
	}

	@Test
	void illinoisProgramsRouteIncludesAuroraAndJoliet() throws Exception {
		mockMvc.perform(get("/lead-service-line/il/programs"))
				.andExpect(status().isOk())
				.andExpect(content().string(Matchers.containsString("IL lead service line programs")))
				.andExpect(content().string(Matchers.containsString("City of Aurora Water Production Division")))
				.andExpect(content().string(Matchers.containsString("City of Joliet Department of Public Utilities")));
	}

	@Test
	void updatedOhioProgramsRouteIncludesColumbus() throws Exception {
		mockMvc.perform(get("/lead-service-line/oh/programs"))
				.andExpect(status().isOk())
				.andExpect(content().string(Matchers.containsString("City of Columbus Division of Water")))
				.andExpect(content().string(Matchers.containsString("Lead Service Line Replacement Program")));
	}

	@Test
	void providenceOverviewShowsPublicPrivateMapAndLoanSplit() throws Exception {
		mockMvc.perform(get("/lead-service-line/ri/providence/providence-water"))
				.andExpect(status().isOk())
				.andExpect(content().string(Matchers.containsString("utility side or private side is lead or unknown")))
				.andExpect(content().string(Matchers.containsString("current construction queue")))
				.andExpect(content().string(Matchers.containsString("10-year 0% loan")));
	}

	@Test
	void providenceCostRouteShowsContractVsLoanMethodologyButStaysNoindex() throws Exception {
		mockMvc.perform(get("/lead-service-line/ri/providence/providence-water/replacement-cost"))
				.andExpect(status().isOk())
				.andExpect(content().string(Matchers.containsString("Methodology basis")))
				.andExpect(content().string(Matchers.containsString("funded contract schedule")))
				.andExpect(content().string(Matchers.containsString("noindex,follow")));
	}

	@Test
	void philadelphiaOverviewShowsMailingTimelineAndHelpLoanPath() throws Exception {
		mockMvc.perform(get("/lead-service-line/pa/philadelphia/philadelphia-water-department"))
				.andExpect(status().isOk())
				.andExpect(content().string(Matchers.containsString("November 2024")))
				.andExpect(content().string(Matchers.containsString("December 2025")))
				.andExpect(content().string(Matchers.containsString("zero-interest HELP loan")));
	}

	@Test
	void philadelphiaCostRouteShowsVerificationVsHelpMethodologyButStaysNoindex() throws Exception {
		mockMvc.perform(get("/lead-service-line/pa/philadelphia/philadelphia-water-department/replacement-cost"))
				.andExpect(status().isOk())
				.andExpect(content().string(Matchers.containsString("Methodology basis")))
				.andExpect(content().string(Matchers.containsString("limited free verification-replacement path")))
				.andExpect(content().string(Matchers.containsString("noindex,follow")));
	}

	@Test
	void pittsburghOverviewShows2027TargetAndStipendTier() throws Exception {
		mockMvc.perform(get("/lead-service-line/pa/pittsburgh/pittsburgh-water"))
				.andExpect(status().isOk())
				.andExpect(content().string(Matchers.containsString("by 2027")))
				.andExpect(content().string(Matchers.containsString("1000 dollar stipend")))
				.andExpect(content().string(Matchers.containsString("water-main work")));
	}

	@Test
	void newarkOverviewShowsDotMapAndFreeMainToMeterReplacement() throws Exception {
		mockMvc.perform(get("/lead-service-line/oh/newark/newark-water"))
				.andExpect(status().isOk())
				.andExpect(content().string(Matchers.containsString("yellow or orange dots")))
				.andExpect(content().string(Matchers.containsString("main to the meter")))
				.andExpect(content().string(Matchers.containsString("free of charge")));
	}

	@Test
	void newarkCostRouteIsIndexableWhenGrantProgramCoversMainToMeter() throws Exception {
		mockMvc.perform(get("/lead-service-line/oh/newark/newark-water/replacement-cost"))
				.andExpect(status().isOk())
				.andExpect(content().string(Matchers.containsString("Methodology basis")))
				.andExpect(content().string(Matchers.containsString("grant-funded promise")))
				.andExpect(content().string(Matchers.containsString("<meta name=\"robots\" content=\"index,follow\">")));
	}

	@Test
	void gcwwOverviewShowsFullCoverageAndPrioritizationTriggers() throws Exception {
		mockMvc.perform(get("/lead-service-line/oh/cincinnati/greater-cincinnati-water-works"))
				.andExpect(status().isOk())
				.andExpect(content().string(Matchers.containsString("100 percent")))
				.andExpect(content().string(Matchers.containsString("child care facilities")))
				.andExpect(content().string(Matchers.containsString("targeted areas")));
	}

	@Test
	void gcwwCostRouteIsIndexableWhenTriggerModelIsExplicit() throws Exception {
		mockMvc.perform(get("/lead-service-line/oh/cincinnati/greater-cincinnati-water-works/replacement-cost"))
				.andExpect(status().isOk())
				.andExpect(content().string(Matchers.containsString("Methodology basis")))
				.andExpect(content().string(Matchers.containsString("child care facilities leaks water-main work")))
				.andExpect(content().string(Matchers.containsString("<meta name=\"robots\" content=\"index,follow\">")));
	}

	@Test
	void madisonOverviewShowsZeroKnownLeadAndPrivateRebateCap() throws Exception {
		mockMvc.perform(get("/lead-service-line/wi/madison/madison-water-utility"))
				.andExpect(status().isOk())
				.andExpect(content().string(Matchers.containsString("November 4")))
				.andExpect(content().string(Matchers.containsString("no known lead")))
				.andExpect(content().string(Matchers.containsString("3000")));
	}

	@Test
	void lowConfidenceCostRouteIsNoindex() throws Exception {
		mockMvc.perform(get("/lead-service-line/co/denver/denver-water/replacement-cost"))
				.andExpect(status().isOk())
				.andExpect(content().string(Matchers.containsString("noindex,follow")));
	}

	@Test
	void lookupGetWithQueryParamsRedirectsToPrivacySafeUrl() throws Exception {
		mockMvc.perform(get("/lookup")
						.param("query", "123 Main St")
						.param("city", "Milwaukee")
						.param("state", "WI"))
				.andExpect(status().is3xxRedirection())
				.andExpect(header().string("Location", "/lookup"))
				.andExpect(header().string("Cache-Control", Matchers.containsString("no-store")));
	}

	@Test
	void opsReviewWithoutTokenReturnsNotFound() throws Exception {
		mockMvc.perform(get("/ops/review"))
				.andExpect(status().isNotFound());
	}

	@Test
	void adminRequiresBasicAuth() throws Exception {
		mockMvc.perform(get("/admin"))
				.andExpect(status().isUnauthorized())
				.andExpect(header().string("WWW-Authenticate", Matchers.containsString("Basic realm=\"Lead Line Admin\"")));
	}

	@Test
	void adminPageRendersWithBasicAuth() throws Exception {
		mockMvc.perform(get("/admin")
						.header("Authorization", basicAuth("admin", "tlsgur3108")))
				.andExpect(status().isOk())
				.andExpect(header().string("Cache-Control", Matchers.containsString("no-store")))
				.andExpect(content().string(Matchers.containsString("Recommendation click activity and internal review entry points.")))
				.andExpect(content().string(Matchers.containsString("Recommendation click log")))
				.andExpect(content().string(Matchers.containsString("Ops review")));
	}

	@Test
	void homePageIncludesCanonicalMetadata() throws Exception {
		mockMvc.perform(get("/"))
				.andExpect(status().isOk())
				.andExpect(content().string(Matchers.containsString("<title>Lead service line lookup, notices, programs")))
				.andExpect(content().string(Matchers.containsString("<link rel=\"canonical\" href=\"https://example.test/\">")))
				.andExpect(content().string(Matchers.containsString("<meta name=\"robots\" content=\"index,follow\">")))
				.andExpect(content().string(Matchers.containsString("<meta property=\"og:title\" content=\"Lead service line lookup, notices, programs")))
				.andExpect(content().string(Matchers.containsString("<meta name=\"twitter:card\" content=\"summary_large_image\">")));
	}

	@Test
	void robotsTxtKeepsLookupCrawlableAndDisallowsOps() throws Exception {
		mockMvc.perform(get("/robots.txt"))
				.andExpect(status().isOk())
				.andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_PLAIN))
				.andExpect(header().string("Cache-Control", Matchers.containsString("no-store")))
				.andExpect(content().string(Matchers.not(Matchers.containsString("Disallow: /lookup"))))
				.andExpect(content().string(Matchers.containsString("Disallow: /admin")))
				.andExpect(content().string(Matchers.containsString("Disallow: /ops/")))
				.andExpect(content().string(Matchers.containsString("Sitemap: https://example.test/sitemap.xml")));
	}

	@Test
	void sitemapXmlListsIndexableRoutesOnly() throws Exception {
		mockMvc.perform(get("/sitemap.xml"))
				.andExpect(status().isOk())
				.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_XML))
				.andExpect(header().string("Cache-Control", Matchers.containsString("no-store")))
				.andExpect(content().string(Matchers.containsString("<loc>https://example.test/about</loc>")))
				.andExpect(content().string(Matchers.containsString("<loc>https://example.test/lead-service-line/pa</loc>")))
				.andExpect(content().string(Matchers.containsString("<lastmod>2026-04-06</lastmod>")))
				.andExpect(content().string(Matchers.containsString("<loc>https://example.test/lead-service-line/dc/washington/dc-water</loc>")))
				.andExpect(content().string(Matchers.not(Matchers.containsString("/lead-service-line/co/denver/denver-water/replacement-cost"))));
	}

	@Test
	void trustPagesRenderAndAreIndexable() throws Exception {
		mockMvc.perform(get("/methodology"))
				.andExpect(status().isOk())
				.andExpect(content().string(Matchers.containsString("How the site builds a local record")))
				.andExpect(content().string(Matchers.containsString("<meta name=\"robots\" content=\"index,follow\">")))
				.andExpect(content().string(Matchers.containsString("<meta property=\"og:title\" content=\"Lead service line methodology | Lead Line Record\">")));

		mockMvc.perform(get("/contact"))
				.andExpect(status().isOk())
				.andExpect(content().string(Matchers.containsString("mailto:shinhyeok22@gmail.com")))
				.andExpect(content().string(Matchers.containsString("shinhyeok22@gmail.com")));
	}

	@Test
	void statePageUsesSearchFriendlyTitle() throws Exception {
		mockMvc.perform(get("/lead-service-line/pa"))
				.andExpect(status().isOk())
				.andExpect(content().string(Matchers.containsString("<title>Pennsylvania lead service line lookup, notices")));
	}

	@Test
	void missingUtilityReturnsNotFound() throws Exception {
		mockMvc.perform(get("/lead-service-line/ca/unknown/none"))
				.andExpect(status().isNotFound());
	}

	private String basicAuth(String username, String password) {
		var token = Base64.getEncoder().encodeToString((username + ":" + password).getBytes(StandardCharsets.UTF_8));
		return "Basic " + token;
	}
}
