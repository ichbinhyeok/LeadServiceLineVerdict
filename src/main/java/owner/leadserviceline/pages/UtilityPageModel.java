package owner.leadserviceline.pages;

import java.util.List;

import owner.leadserviceline.data.CostRecord;
import owner.leadserviceline.data.ProgramRecord;
import owner.leadserviceline.data.RouteRecord;
import owner.leadserviceline.data.SourceEvidenceRecord;
import owner.leadserviceline.data.UtilityRecord;

public record UtilityPageModel(
		String pageTitle,
		UtilityPageSection section,
		String heroTitle,
		String heroSummary,
		UtilityRecord utility,
		RouteRecord currentRoute,
		List<UtilityRouteLink> navigation,
		List<String> nextSteps,
		List<UtilityDecisionFact> keyFacts,
		List<String> routeCautions,
		List<ProgramRecord> programs,
		List<UtilityProgramSummary> programSummaries,
		CostRecord cost,
		List<UtilityCostResponsibility> costResponsibilities,
		List<SourceEvidenceRecord> sources,
		UtilityTrustModel trust
) {
}
