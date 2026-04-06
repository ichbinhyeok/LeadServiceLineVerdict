package owner.leadserviceline.ingest;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import owner.leadserviceline.data.CostRecord;
import owner.leadserviceline.data.GuideRecord;
import owner.leadserviceline.data.ProgramRecord;
import owner.leadserviceline.data.RouteRecord;
import owner.leadserviceline.data.UtilityRecord;

public class RouteInventoryBuilder {

	public List<RouteRecord> build(
			Collection<UtilityRecord> utilities,
			Collection<ProgramRecord> programs,
			Collection<GuideRecord> guides,
			Map<String, CostRecord> costsByUtilityId
	) {
		var generatedAt = OffsetDateTime.now().toString();
		var routes = new ArrayList<RouteRecord>();
		var programsByState = programs.stream().collect(Collectors.groupingBy(ProgramRecord::state));
		var programIdsByUtilityId = programs.stream()
				.flatMap(program -> program.utilityIds().stream()
						.map(utilityId -> Map.entry(utilityId, program.programId())))
				.collect(Collectors.groupingBy(
						Map.Entry::getKey,
						Collectors.mapping(Map.Entry::getValue, Collectors.toList())
				));

		utilities.stream()
				.map(UtilityRecord::state)
				.distinct()
				.sorted()
				.forEach(state -> {
					routes.add(new RouteRecord(
							"/lead-service-line/" + state,
							"state-hub",
							state,
							null,
							null,
							null,
							"/lead-service-line/" + state,
							true,
							"state hub with seeded utility depth",
							generatedAt
					));
					if (!programsByState.getOrDefault(state, List.of()).isEmpty()) {
						routes.add(new RouteRecord(
								"/lead-service-line/" + state + "/programs",
								"state-programs",
								state,
								null,
								null,
								null,
								"/lead-service-line/" + state + "/programs",
								true,
								"state has verified utility-linked program coverage",
								generatedAt
						));
					}
				});

		guides.stream()
				.sorted(Comparator.comparing(GuideRecord::slug))
				.forEach(guide -> routes.add(new RouteRecord(
						"/guides/" + guide.slug(),
						"guide",
						null,
						null,
						null,
						null,
						"/guides/" + guide.slug(),
						true,
						"evergreen editorial guide",
						generatedAt
				)));

		utilities.stream()
				.sorted(Comparator.comparing(UtilityRecord::state).thenComparing(UtilityRecord::city).thenComparing(UtilityRecord::utilitySlug))
				.forEach(utility -> routes.addAll(buildUtilityRoutes(utility, programIdsByUtilityId, costsByUtilityId, generatedAt)));

		return routes.stream()
				.sorted(Comparator.comparing(RouteRecord::path))
				.toList();
	}

	private List<RouteRecord> buildUtilityRoutes(
			UtilityRecord utility,
			Map<String, List<String>> programIdsByUtilityId,
			Map<String, CostRecord> costsByUtilityId,
			String generatedAt
	) {
		var routes = new ArrayList<RouteRecord>();
		var basePath = "/lead-service-line/%s/%s/%s".formatted(
				utility.state(),
				utility.city(),
				utility.utilitySlug()
		);

		routes.add(route(basePath, "utility-overview", utility, null, true, "official source plus next-step path available", generatedAt));

		if (!isBlank(utility.notificationUrl())) {
			routes.add(route(basePath + "/notification", "utility-notification", utility, null, true, "official notice source available", generatedAt));
		}

		var programIds = programIdsByUtilityId.getOrDefault(utility.utilityId(), List.of());
		if (!programIds.isEmpty()) {
			routes.add(route(basePath + "/program", "utility-program", utility, programIds.get(0), true, "verified utility-linked program available", generatedAt));
		}

		var cost = costsByUtilityId.get(utility.utilityId());
		if (cost != null) {
			var indexable = cost.shouldIndexRoute();
			var reason = indexable
					? "cost assumptions and source evidence available"
					: cost.needsMethodologyHardening()
							? "cost route exists for internal review but remains noindex until medium-confidence methodology is stronger"
							: "cost route exists for internal review but remains noindex in the seed dataset";
			routes.add(route(basePath + "/replacement-cost", "utility-replacement-cost", utility, null, indexable, reason, generatedAt));
		}

		if (shouldIncludeFilterAndTesting(utility, cost, programIds)) {
			routes.add(route(
					basePath + "/filter-and-testing",
					"utility-filter-and-testing",
					utility,
					null,
					false,
					"support-layer route kept for utilities that need interim protection guidance",
					generatedAt
			));
		}
		if (shouldIncludeBuyerSeller(utility, cost, programIds)) {
			routes.add(route(
					basePath + "/buyer-seller",
					"utility-buyer-seller",
					utility,
					null,
					false,
					"support-layer route kept for utilities with strong local transaction evidence",
					generatedAt
			));
		}

		return routes;
	}

	private boolean shouldIncludeFilterAndTesting(UtilityRecord utility, CostRecord cost, List<String> programIds) {
		if (isBlank(utility.notificationUrl())) {
			return false;
		}
		return cost == null || cost.isLowConfidence() || programIds.isEmpty();
	}

	private boolean shouldIncludeBuyerSeller(UtilityRecord utility, CostRecord cost, List<String> programIds) {
		if (cost == null || !cost.shouldIndexRoute() || programIds.isEmpty()) {
			return false;
		}
		return "official_lookup".equalsIgnoreCase(utility.addressLookupMode())
				&& utility.lineCounts() != null
				&& utility.lineCounts().hasCompleteBreakdown();
	}

	private RouteRecord route(
			String path,
			String template,
			UtilityRecord utility,
			String programId,
			boolean indexable,
			String reason,
			String generatedAt
	) {
		return new RouteRecord(
				path,
				template,
				utility.state(),
				utility.city(),
				utility.utilityId(),
				programId,
				path.toLowerCase(Locale.US),
				indexable,
				reason,
				generatedAt
		);
	}

	private boolean isBlank(String value) {
		return value == null || value.isBlank();
	}
}
