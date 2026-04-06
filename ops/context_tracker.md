# Context Tracker

## Current status
- Product docs are merged and agent-readable.
- Docs now assume a file-based build with `CSV` and `JSON`, not a runtime database.
- Spring Boot + `jte` implementation has started.
- The first vertical slice loads normalized JSON and route records from `data/`.
- A reproducible `raw CSV and JSON -> normalized JSON -> derived routes` build step now exists.
- Utility subroutes now render with section-specific templates for overview, notification, program, cost, filter, and buyer-seller intent.
- The current launch cohort uses official-source seed data for 63 utilities and now generates 272 routes after support-route narrowing.
- A phase 1 `/lookup` route now resolves address or utility hints to the most likely utility page and official lookup URL.
- The resolver now also uses postal-prefix hints for regional utilities.
- The resolver now uses explicit service-area municipality coverage and confidence labels, not just broad hint matching.
- Structured street-address lookups can now use the U.S. Census geocoder as an optional enrichment step before local utility scoring.
- Source records now fail build validation when freshness, schema, or scope-link requirements are broken.
- A `verifySourceUrls` task now checks live reachability of official utility, program, and source URLs.
- A noindex `/ops/review` route now exposes a lightweight manual review queue for stale records, low-confidence costs, and coverage gaps.
- A JSON `/ops/review/export` route now exposes the same review queue as a reusable snapshot for ops workflows.
- `/ops/review` now also surfaces lookup ambiguity hot spots and no-match requests from the optional JSONL lookup log when that file exists.
- State program rollups and evergreen guide pages now render from the same file-based route inventory.
- `/lookup` now uses `POST` for actual submissions, redirects privacy-unsafe querystring attempts back to a clean URL, and redacts logged query text.
- `/ops/review` and `/ops/review/export` are now internal-only routes gated behind both `lead-service-line.ops-review-enabled` and `X-Ops-Token`.
- SEO delivery is now present at the route level with canonical tags, robots tags, JSON-LD, `robots.txt`, and `sitemap.xml`.
- `jte` now runs with precompiled-template configuration rather than development-mode rendering defaults.
- Support routes are now selective support-layer pages instead of universal utility routes.
- This remains the highest-priority vertical.

## Latest decisions
- Utility pages are canonical.
- City pages require real local differentiation.
- Revenue starts with tests, filters, and replacement referrals.
- Recommended stack is `Spring Boot` + `jte`.
- Phase 1 storage is raw `CSV` plus normalized and derived `JSON`.

## What changed this session
- Merged project-local docs with the independent spec set.
- Added agent read order, context tracker, and missing spec files.
- Kept the JVM-first recommendation and paired it with a file-based web architecture.
- Tightened route conventions and data-contract requirements.
- Bootstrapped a Spring Boot 4 project with Gradle wrapper.
- Added `jte` templates, file-based data loading, sample `CSV` and `JSON` data, state and utility routes, and passing tests.
- Added the `buildData` Gradle task and Java ingest pipeline that regenerates normalized records and route inventory from raw inputs.
- Split utility subroutes into distinct section templates and added route-specific web tests, including `noindex` coverage for low-confidence cost pages.
- Replaced fake sample utilities with official-source seed records for DC, Denver, and Milwaukee.
- Relaxed the data contract so utilities can publish a narrative inventory summary when a full four-bucket count breakdown is not available.
- Relaxed program deadlines to allow verified rolling or contact-first programs without fake end dates.
- Expanded the official-source launch cohort to Saint Paul Providence Philadelphia Pittsburgh Newark Cincinnati and Madison.
- Added `resolver_hints` to utility records and implemented the phase 1 utility matcher at `/lookup`.
- Added lookup tests and broader data-build assertions for the expanded cohort.
- Added `resolver_postal_prefixes` and ZIP-prefix scoring to improve heuristic utility resolution for regional service areas.
- Added strict source validation for freshness windows, HTTPS URLs, scope-key references, and allowed status/type values.
- Added live URL verification and swapped unstable Providence and Pittsburgh URLs to stable official alternatives.
- Added an ops review dashboard built from the normalized snapshot and linked it from the home page.
- Added a JSON export for the ops review snapshot so the manual queue can be reused outside the HTML page.
- Added raw guide JSON, normalized guide records, state-level program rollups, and evergreen guide routes.
- Added `resolver_municipalities`, stronger resolver scoring thresholds, and clearer lookup-result confidence messaging.
- Added an optional Census geocoder client with graceful fallback to local matching and isolated tests.
- Re-read the original spec criteria and expanded the cohort only where official inventory and clear notice/program pages were strong enough to keep quality gates intact.
- Added City of Columbus Division of Water and City of Minneapolis Public Works Water Treatment & Distribution Services, including program and cost coverage where the official pages supported it.
- Deepened `MN` coverage with City of Duluth and `OH` coverage with City of Hamilton where official inventory and notice pages were strong enough to pass the same quality gates.
- Added a first `AZ` state cluster with Phoenix Tempe and Tucson, including cost and program coverage only where the official pages supported those claims.
- Renamed the Minneapolis and Columbus raw source bundle to remove the Cleveland placeholder after Cleveland was held out of the launch cohort.
- Deepened `AZ` coverage with Mesa and Chandler and `OH` coverage with Toledo and Akron using only official inventory or notice pages that passed the same freshness and URL checks.
- Added a first `MI` state cluster with Detroit St. Joseph Monroe and Lansing BWL, opening program routes only where the official pages clearly described an active replacement path.
- Expanded regression coverage so Arizona, Ohio, and Michigan cohort routes are pinned in both ingest and MVC tests.
- Added a first `IN` state cluster with Bloomington Citizens Speedway Evansville and Kendallville, opening program and cost routes only where the official pages clearly described an active replacement path.
- Fixed an Evansville raw-data alignment bug and expanded regression coverage so Indiana cohort routes are pinned in both ingest and MVC tests.
- Bulk-expanded the cohort with South Bend Lafayette Mishawaka Bloomington Illinois Aurora Joliet Rockford Evanston Green Bay and Grand Rapids, opening program and cost routes only where the official pages clearly described a current replacement path.
- Added a first `IL` state cluster and expanded regression coverage so the new Illinois Indiana Wisconsin and Michigan routes are pinned in both ingest and MVC tests.
- Added a follow-up Midwest batch with Chicago Fort Wayne Racine and Kalamazoo, using official lookup or program pages to extend the launch cohort while preserving the file-based build model.
- Tightened `/lookup` scoring so exact city matches, official lookup paths, postal prefixes, and normalized service-area aliases break ties more cleanly in ambiguous Midwest queries.
- Replaced unstable Chicago and Racine verification URLs with stable official pages or PDFs so `buildData`, `test`, and `verifySourceUrls` all pass again on the 44-utility cohort.
- Added file-based `/lookup` event logging so requests can be captured as compact JSONL without introducing a runtime database.
- Added Missouri American Water and Blue Springs so Missouri now has both regional and city utility coverage plus a real state program rollup.
- Added Grand Island, Council Bluffs, Ames, and West Des Moines to deepen the Iowa and Nebraska clusters with official inventory pages and selective program or cost coverage where the sources were strong enough.
- Added lookup-log review groups so ambiguous or failed resolver traffic can drive the next alias and boundary cleanup pass without a runtime database.
- Fixed `resolveStateCode` coverage for currently seeded state names such as Missouri Iowa and Nebraska.
- Deepened `IA` and `NE` with Dubuque Sioux City and Hastings, opening program routes only where the official pages clearly described an active replacement path.
- Relaxed live URL verification so civic sites and official PDFs that fail `HEAD` but succeed on `GET` do not fail the build unnecessarily.
- Shifted the build back toward the original product priorities by narrowing support-layer routes and keeping core page families dominant.
- Hardened `/lookup` privacy by moving submitted address text off the URL and redacting logged query values.
- Moved `/ops/review` and `/ops/review/export` behind an explicit internal access boundary.
- Added route-level SEO delivery with canonical metadata, route-aware robots handling, JSON-LD, `robots.txt`, and `sitemap.xml`.
- Switched `jte` runtime configuration to precompiled templates for production-style behavior in app and tests.
- Updated web and ingest tests to validate the new privacy boundary, SEO delivery, internal ops access, and reduced route inventory.
- Strengthened the core page family so overview, notification, program, and replacement-cost routes now expose route-specific decision facts, cautions, and responsibility splits instead of relying on generic local copy.
- Replaced the core public-facing templates with a civic-editorial frontend pass based on the imported design draft, using local image assets and a stronger Newsreader/Public Sans design system instead of the earlier plain card layout.
- Refreshed the home page, lookup flow, utility overview, notification, program, replacement-cost, and filter/testing pages so they now share the same branded header, footer, hero treatment, editorial section rhythm, and downloaded local imagery.
- Finished the secondary-page frontend pass so state hubs, state program rollups, evergreen guides, buyer-seller support pages, and the internal ops-review dashboard now use the same design system rather than the old placeholder shells.
- Added a shared favicon asset and ran browser QA against home lookup guide state-program and utility pages so the current frontend pass no longer throws page-local console errors in the checked routes.
- A deeper Playwright pass across desktop and iPhone SE caught a real mobile regression where the header nav disappeared under `720px`; the mobile header now keeps navigation accessible as a horizontal scroll row instead of hiding it.
- The same browser QA pass also exposed stale static-asset caching on `/app.css`; page templates now append a version query to shared CSS and favicon assets so fresh frontend changes are picked up reliably after deploy.
- Added partial structured-count support so utilities can surface official `known` or `unknown` counts even when they do not publish a full four-bucket breakdown.
- Refreshed Saint Paul Madison Chandler Toledo and Monroe inventory or notice evidence with sharper official counts, notice language, and updated verification dates.
- Refreshed Milwaukee West Des Moines and Lansing BWL with official partial-count or zero-remaining-line evidence so their core utility pages no longer depend only on narrative summaries.
- Refreshed Detroit South Bend Flint Dubuque Sioux City and Hastings with sharper official notice or progress language, and promoted Hastings to partial structured-count coverage using the published current lead-line count.
- Refreshed Lafayette Mishawaka Grand Rapids Jackson and Iowa American Water with sharper official notice program and cost language, while keeping mixed-category or progress-only counts out of structured line-count fields.
- Refreshed Citizens Bloomington Utilities Aurora Rockford and Kansas City Water with sharper official notice and next-step language, including clearer boundaries between annual lead notices, qualifying replacement paths, and non-replacement service-line protection communications.
- Refreshed Speedway Evansville Joliet Evanston and Fort Wayne with sharper official notice and replacement-boundary language, including clearer trigger-based no-cost rules, project-area versus homeowner-initiated paths, and private-side responsibility splits.
- Refreshed Hamilton Akron Bloomington IL Green Bay and Dearborn with sharper official inventory and notice language, including clearer public-versus-private ownership, unknown-line verification paths, rate-funded cost caveats, GRR guidance, and annual testing context.
- Refreshed Mesa Tempe Tucson South Bend and Providence with sharper official notice and eligibility language, including no-public-lead disclaimers, precautionary unknown notices, no-cost confirmed replacement rules, ongoing private-side verification, and contract-versus-loan replacement paths.
- Refreshed Philadelphia Pittsburgh Newark Greater Cincinnati Water Works and Madison with sharper official notice and next-step language, including mailed notice timing, 2027 replacement timing, dot-map replacement guidance, full-coverage GCWW triggers, and Madison's zero-known-lead plus private-rebate framing.
- Refreshed DC Water Denver Water Columbus Minneapolis and Phoenix with sharper official notice and program-path language, including multilingual DC letters, Denver's 2026-2031 outlook and reimbursement rule, Columbus project-area and work-agreement framing, Minneapolis unknown-record plus 2033 goal wording, and Phoenix's fall 2024 notice plus full-cost replacement coverage.
- Refreshed Springfield Columbia Independence M.U.D. Omaha and Iowa City with sharper narrative evidence, including Springfield's 38000 identified lines, Columbia's survey-plus-inspection flow, Independence's current PDF inventory and staff-help path, Omaha's unknown-status support toolkit, and Iowa City's flushing plus cost-share workflow.
- Tightened resolver precision so freeform lookup text can promote direct city mentions and normalized utility aliases into stronger confidence signals.
- Strengthened medium-confidence cost delivery so routes stay noindex unless methodology evidence clears a stricter indexing gate, while support routes remain selectively scoped.
- Stabilized live URL verification so repeated external connect timeouts are retried and downgraded to warnings while ordinary reachability failures still fail the build.
- Tightened lookup-log privacy so optional `/lookup` diagnostics now keep only coarse bucket labels, skip blank submissions, and prune events after 14 days by default.
- Replaced unstable Citizens and Akron source URLs with stable official alternatives so `verifySourceUrls` passes again without the recent connect failures.
- Added explicit cost methodology and owner-trigger narratives to the strongest medium-confidence utility cost pages so only locally grounded replacement-cost routes can graduate to `index`.
- Promoted exact place normalization in the resolver so abbreviations like `St Paul` and `Ft Wayne` score like their canonical utility-city names instead of falling back to weaker broad-area matches.
- Refreshed another narrative-heavy batch around Blue Springs Grand Island Council Bluffs Ames West Des Moines and Missouri American Water so overview copy now points to exact lookup survey map notice and replacement triggers instead of generic summaries.
- Added another medium-confidence cost hardening batch for Saint Paul Newark Greater Cincinnati Water Works Minneapolis Duluth Phoenix and Tucson so locally grounded no-cost utility workflows can pass the stronger indexing gate.
- Refreshed St. Joseph Bloomington IL Rockford Green Bay and Kalamazoo so narrative-heavy overview pages now point to the exact map inspection survey testing or replacement boundary published by the utility.
- Added another medium-confidence cost hardening batch for Toledo Lafayette Evanston Fort Wayne Missouri American Water and M.U.D. Omaha so local trigger language and methodology narratives can move those routes from noindex to index.
- Refreshed Speedway Evansville Kendallville and Iowa City so narrative-heavy overview pages now distinguish mailed notices survey work phased funding and contractor cost-share steps more explicitly.
- Added a follow-up cost hardening batch for Aurora Joliet and Iowa American Water, including a less brittle cost-band gate for `pay the`, `no additional cost`, and `no direct cost` phrasing so locally specific cost routes can graduate without fake bid ranges.
- Refreshed Citizens South Bend Kansas City Springfield and Independence so narrative-heavy overview pages now distinguish annual notices survey-driven verification optional coverage letters self-report cleanup and staff-help workflows more explicitly.
- Added another medium-confidence cost hardening batch for Grand Rapids Flint Jackson Kalamazoo Kendallville and Racine, promoting the utilities whose local trigger language and funded workflow detail were specific enough to clear the stronger indexing gate.
- Refreshed Providence Grand Rapids Racine Kalamazoo Jackson Flint and Kendallville so narrative-heavy overview pages now distinguish contract-versus-loan paths, leak-or-construction triggers, invitation-only batches, funded no-charge replacement, city-area scheduling, consent-form workflows, and annual notice plus right-of-entry requirements more clearly.
- Refreshed Denver Providence Philadelphia Pittsburgh Madison Iowa City and Ames low-confidence cost records so `replacement-cost` pages now explain reimbursement caps, contract-versus-loan paths, customer-owned line rules, income-tier reimbursement, rebate-only support, and owner-managed contractor workflows while remaining `noindex`.
- Re-verified the project sequentially after confirming that `test` and `verifySourceUrls` should not run in parallel because both touch generated data under `data/normalized` and `data/derived`.
- Verified the current stronger cost gate now leaves twenty-six indexable replacement-cost routes in the dataset: DC Water Milwaukee Water Works Columbus Water Saint Paul Greater Cincinnati Water Works Newark Minneapolis Duluth Phoenix Tucson Evanston Fort Wayne Lafayette Missouri American Water M.U.D. Omaha Toledo Aurora Joliet Iowa American Water Grand Rapids Flint Jackson Kalamazoo Kendallville Racine and Bloomington Illinois.
- Tightened the resolver so explicit city input is scored separately from geocoder-derived city normalization, ambiguity is no longer hidden by lookup-mode rank, and broad service-area hints are downweighted when they conflict with a named city.
- Promoted Bloomington Illinois replacement-cost to indexable by adding utility-specific methodology and owner-trigger narratives tied to the city's rate-funded 2027 block replacement plan, while keeping the remaining low-confidence cost routes noindex.
- Refreshed South Bend Bloomington Illinois Aurora Chicago Green Bay Grand Rapids Fort Wayne Racine and Kalamazoo summaries plus source claims so narrative-heavy overview pages point more directly to the exact lookup labels, notice boundaries, and replacement triggers the utilities actually publish.
- Ran a deep design-review QA pass with Playwright across desktop and iPhone SE scenarios covering home, lookup, utility overview, replacement-cost, guide, state hub, and state program rollup flows.
- Fixed mobile navigation regression by keeping the header nav visible under 720px as a horizontal scroll row instead of hiding it.
- Added CSS cache-busting query strings to shared stylesheet and favicon references so real browser sessions stop reusing stale assets during QA.
- Fixed iPhone SE readability and overflow issues by giving grid children `min-width: 0`, forcing long source links to wrap inside cards, raising mobile body copy and form labels to 16px, and increasing header/tab touch targets to a 44px mobile-friendly height.
- Re-verified after the CSS pass that the sampled desktop and iPhone SE pages have no horizontal overflow, no sub-16px body/link/form text in the audited selectors, working click flows, and zero browser console errors.
- Ran a route-inventory-wide visual QA pass across all `274` public routes plus `/` and `/lookup`, split into four desktop chunks and four iPhone SE chunks, and found no remaining horizontal overflow, no below-threshold body/form/link text, and no undersized mobile nav/tab/button targets under the current audit rules.
- Verified representative interactive flows after the full scan, including home to guides, state program rollup to utility program, utility tab transitions, and mobile lookup resolution, with no application console errors during the app scan.

## Next recommended tasks
1. Add more structured inventory counts and sharper notice-language evidence for narrative-only utilities where official sources allow it.
2. Improve resolver precision beyond municipality and ZIP heuristics with parcel-authoritative geocoding or a better boundary dataset.
3. Add narrative-quality checks for cost methodology copy before medium-confidence cost pages are indexable.
4. Keep support-layer routes selective and explicitly tied to local evidence rather than broad utility expansion.
5. Resume cohort expansion only after another round of core page evidence improvements is in place.
6. Run browser-based visual QA once the local Playwright permission issue is cleared so the unified frontend pass can be checked in a real browser.

## Open questions
- How aggressive the first city-page layer should be.
- Whether phase 1 address resolution should stay heuristic or graduate to parcel-authoritative geocoding.
- Whether medium-confidence cost pages should be indexable from launch or held back until more methodology text exists.
