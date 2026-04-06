# Lead Line Record

Public brand: `Lead Line Record`  
Primary site URL: `https://leadlinerecord.com`

**Date:** 2026-04-04 (Asia/Seoul)  
**Purpose:** This folder is a self-contained design packet for building a US-focused **lead service line inventory and replacement decision site** with utility-first SEO and sponsor-friendly local monetization.

## What you are building
A utility-first decision site for homeowners, buyers, and sellers who need to understand a `known` or `potential` lead service line notice, estimate replacement cost, compare filters and testing, and find the right local next step.

## Why this concept is attractive
- The regulatory trigger is recent and real.
- Utility and city long-tail queries are natural and specific.
- The canonical page unit is clear: utility plus city.
- Monetization can start with filters, water tests, and local plumbers before any lead platform matters.

## File map
- `AGENT_START_HERE.md` - read order and handoff rules for any future agent
- `ops/context_tracker.md` - current status, decisions, and next tasks
- `spec/00_strategy.md` - market thesis, positioning, search wedge, page system, and rollout plan
- `spec/01_query_and_user_map.md` - user segments, query families, intent states, and priority page set
- `spec/02_site_architecture.md` - canonical entities, URL graph, page modules, structured data, and internal linking
- `spec/03_data_and_operations.md` - registry schema, inventory/program data model, verification workflow, and refresh cadence
- `spec/04_commercial_model.md` - monetization paths, CTA logic, lead handling, and sponsor packaging
- `spec/05_editorial_rules_and_execution.md` - writing rules, trust guardrails, launch phases, and success metrics
- `spec/06_indexing_quality_and_analytics.md` - indexing rules, quality gates, kill rules, and measurement plan
- `spec/07_technical_architecture.md` - system boundaries, package map, rendering model, and operational services
- `spec/08_delivery_and_handoff.md` - workstreams, milestones, acceptance criteria, and handoff order

## Recommended build stack
- `Spring Boot` + `jte`
- Server-rendered utility, state, program, and guide pages with aggressive caching
- File-based data pipeline using raw `CSV` plus normalized and derived `JSON`
- No runtime database in phase 1
- Java runtime baseline: `21`  
  Spring Boot `4.0.5` is already in use here, so deployment stays on Java `21`, not `17`

## Deployment
- Docker image: `shinhyeok22/leadline`
- Release tag strategy: push both `latest` and the full Git commit SHA, then deploy the SHA-pinned image
- Docker Compose port mapping: external `8094` -> internal `8080`
- Docker Compose memory cap: `mem_limit: 512m`
- GitHub Actions workflow: `.github/workflows/deploy.yml`
- Deployment trigger: manual `workflow_dispatch` only. `main` pushes do not auto-deploy.
- Domain, ingress, and public hostname wiring stay manual and must be verified outside this workflow.
- Required GitHub secrets:
  - `DOCKERHUB_USERNAME`
  - `DOCKERHUB_TOKEN`
  - `OCI_HOST`
  - `OCI_USERNAME`
  - `OCI_KEY`

## Current launch cohort
- `DC Water`
- `Denver Water`
- `Milwaukee Water Works`
- `Saint Paul Regional Water Services`
- `Providence Water`
- `Philadelphia Water Department`
- `Pittsburgh Water`
- `City of Newark Water Office`
- `Greater Cincinnati Water Works`
- `Madison Water Utility`
- `City of Columbus Division of Water`
- `City of Minneapolis Public Works Water Treatment & Distribution Services`
- `City of Duluth Lead Removal Program`
- `City of Hamilton Utilities + Public Works Water`
- `City of Mesa Water Resources`
- `City of Chandler Water Utilities`
- `City of Phoenix Water Services`
- `City of Tempe Water Utilities`
- `Tucson Water`
- `City of Toledo Division of Water Distribution`
- `City of Akron Water Supply Bureau`
- `Detroit Water and Sewerage Department`
- `City of St. Joseph Water and Sewer Department`
- `City of Monroe Water and Wastewater Department`
- `Lansing Board of Water and Light`
- `City of Bloomington Utilities`
- `Citizens Energy Group`
- `Town of Speedway Water Works`
- `Evansville Water and Sewer Utility`
- `City of Kendallville Water Department`
- `City of South Bend Water Works`
- `City of Lafayette Water Works`
- `Mishawaka Utilities`
- `City of Bloomington Water Department`
- `City of Aurora Water Production Division`
- `City of Joliet Department of Public Utilities`
- `City of Rockford Water Division`
- `City of Evanston Water Production Bureau`
- `Green Bay Water Utility`
- `City of Grand Rapids Water System`
- `City of Chicago Department of Water Management`
- `Fort Wayne City Utilities`
- `City of Racine Water Utility`
- `City of Kalamazoo Water Department`
- `Kansas City Water`
- `City Utilities of Springfield`
- `City of Columbia Water Utility`
- `City of Independence Water Department`
- `Missouri American Water`
- `City of Blue Springs Water and Sewer Services`
- `Metropolitan Utilities District`
- `Iowa American Water`
- `Iowa City Water Division`
- `Grand Island Utilities Department`
- `Council Bluffs Water Works`
- `City of Ames Water & Pollution Control`
- `West Des Moines Water Works`
- `City of Dubuque Water Department`
- `City of Sioux City Water Plant`
- `Hastings Utilities Water Department`
- `City of Jackson Water Department`
- `City of Flint Water Service Center`
- `City of Dearborn Water and Sewerage Division`

## Current footprint
- `63` verified utility records
- `272` generated routes
- `49` verified utility-linked programs
- `33` cost methodology records
- Support routes are now selective and no longer generated mechanically for every utility
- Core public pages now use a civic-editorial frontend system with local downloaded imagery, branded header/footer chrome, and high-contrast section structure inspired by the imported design draft

## Phase 1 resolver
- `GET /lookup`
- `POST /lookup`
- Utility matcher that narrows an address, city, state, service-area municipality, postal prefix, or utility hint to the most likely utility page
- Exact city matches and official lookup paths are weighted above softer service-area hints when multiple utilities are close
- Freeform lookup text now upgrades direct city mentions into stronger resolver signals even when the city field is left blank
- Full state-name input now resolves correctly for the current seeded states, including `Missouri`, `Iowa`, and `Nebraska`
- Resolver coverage now distinguishes municipality coverage from softer neighborhood hints
- Lookup results expose stronger confidence labels and suppress weak state-only candidates
- Structured street-address queries can optionally be normalized through the U.S. Census geocoder before scoring
- `GET /lookup` with query parameters now redirects back to a clean URL instead of leaving address text in browser history
- Lookup requests can be appended to `data/logs/lookup-events.jsonl` when `lead-service-line.lookup-log-enabled=true`
- Logged lookup events now store only a coarse location bucket or safe short freeform label, not raw submitted address fields
- Blank or no-op lookup submissions are not written to disk
- Lookup event retention now defaults to 14 days so the optional JSONL log stays short-lived
- Always hands users back to the utility page and official lookup URL rather than claiming parcel-authoritative resolution

## State and guide routes
- `GET /lead-service-line/{state}/programs`
- `GET /guides/{slug}`
- State program rollups summarize verified utility-linked support paths
- Evergreen guides provide reusable decision content that links back into local utility pages

## Ops review
- `GET /ops/review`
- `GET /ops/review/export`
- Internal noindex dashboard for stale records, low-confidence cost routes, and data gaps in the current normalized snapshot
- Disabled by default unless `lead-service-line.ops-review-enabled=true`
- Requires `X-Ops-Token` to access either the HTML view or JSON export
- When lookup logging is enabled and events exist, the same dashboard now surfaces ambiguous lookup hot spots and no-match lookup requests from coarse JSONL buckets rather than replaying raw submitted input
- JSON snapshot export for the same review groups and metrics so ops can consume the queue outside the HTML page
- Built directly from repository-loaded JSON so it works without a runtime database

## SEO delivery
- Every rendered page now emits route-level `<title>`, description, canonical URL, and robots metadata
- Utility, state, guide, and lookup pages emit JSON-LD
- `GET /robots.txt` disallows `/lookup` and `/ops/`
- `GET /sitemap.xml` includes indexable routes only
- Low-confidence cost routes remain `noindex` at the route level
- Some medium-confidence cost routes are now also held `noindex` until methodology quality clears the stronger indexing gate
- Medium-confidence cost routes now need explicit local methodology and owner-trigger narratives before they can flip to `index`
- The current dataset now has twenty-six indexable replacement-cost routes that clear the stronger gate: `DC Water`, `Milwaukee Water Works`, `Columbus Water`, `Saint Paul Regional Water Services`, `Greater Cincinnati Water Works`, `City of Newark Water Office`, `City of Minneapolis Public Works`, `City of Duluth Lead Removal Program`, `City of Phoenix Water Services`, `Tucson Water`, `City of Evanston Water Production Bureau`, `Fort Wayne City Utilities`, `City of Lafayette Water Works`, `Missouri American Water`, `Metropolitan Utilities District`, `City of Toledo Division of Water Distribution`, `City of Aurora Water Production Division`, `City of Joliet Department of Public Utilities`, `Iowa American Water`, `City of Grand Rapids Water System`, `City of Flint Water Service Center`, `City of Jackson Water Department`, `City of Kalamazoo Water Department`, `City of Kendallville Water Department`, `City of Racine Water Utility`, and `City of Bloomington Water Department`

## Core vs support page families
- Core routes stay centered on `overview`, `notification`, `program`, and `replacement-cost`
- `filter-and-testing` and `buyer-seller` are now treated as support-layer routes, not universal utility routes
- Support routes are only generated when local evidence justifies them
- Utility pages now separate core navigation from support navigation
- Core utility pages now surface route-specific decision blocks instead of generic copy:
  - overview: inventory status, lookup path, notice availability, replacement-support status, cost-route status
  - notification: notice-reading facts, address-confirmation step, and explicit overread cautions
  - program: public/private coverage split, income/property/contractor rules, and verification labels
  - replacement-cost: responsibility split, assumption stack, program offsets, and cost cautions
- Narrative-only utilities can now show partial structured counts when only some official count fields are available
- Recent data-quality upgrades added sharper inventory or notice evidence for `Saint Paul`, `Madison`, `Chandler`, `Toledo`, `Monroe`, `Milwaukee`, `West Des Moines`, `Lansing BWL`, `Detroit`, `South Bend`, `Flint`, `Dubuque`, `Sioux City`, `Hastings`, `Lafayette`, `Mishawaka`, `Grand Rapids`, `Jackson`, `Iowa American Water`, `Citizens Energy Group`, `Bloomington Utilities`, `Aurora`, `Rockford`, `Kansas City Water`, `Speedway`, `Evansville`, `Joliet`, `Evanston`, `Fort Wayne`, `Hamilton`, `Akron`, `Bloomington IL`, `Green Bay`, `Dearborn`, `Mesa`, `Tempe`, `Tucson`, `Providence`, `Philadelphia`, `Pittsburgh`, `Newark`, `Greater Cincinnati Water Works`, `DC Water`, `Denver Water`, `Columbus`, `Minneapolis`, `Phoenix`, `Springfield`, `Columbia`, `Independence`, `M.U.D. Omaha`, and `Iowa City`
- The latest evidence pass also sharpened `Blue Springs`, `Grand Island`, `Council Bluffs`, `Ames`, `West Des Moines`, and `Missouri American Water` so narrative-heavy overview pages point to the exact local lookup, survey, notice, or program boundary instead of generic local copy
- Another narrative-heavy pass sharpened `St. Joseph`, `Bloomington IL`, `Rockford`, `Green Bay`, and `Kalamazoo` so overview pages now point to the specific lookup map, inspection, survey, testing, or replacement boundary that the utility actually publishes
- A follow-up backend-only pass tightened `South Bend`, `Bloomington IL`, `Aurora`, `Chicago`, `Green Bay`, `Grand Rapids`, `Fort Wayne`, `Racine`, and `Kalamazoo` so the summaries draw a harder line between lookup labels, notice language, replacement triggers, and owner-managed paths
- Resolver scoring now separates explicit city input from geocoder-derived city normalization, treats ambiguity independently of lookup-mode rank, and keeps broad service-area hints from overpowering a mismatched explicit city
- The latest narrative-heavy pass sharpened `Speedway`, `Evansville`, `Kendallville`, and `Iowa City` so overview pages now distinguish letters, surveys, phased funding, and cost-share workflows instead of collapsing them into generic local summaries
- Another narrative-heavy pass sharpened `Citizens`, `South Bend`, `Kansas City`, `Springfield`, and `Independence` so overview pages now distinguish annual notices, survey-driven verification, optional coverage letters, ongoing self-report cleanup, and staff-help workflows more explicitly
- The latest narrative-heavy pass sharpened `Providence`, `Grand Rapids`, `Racine`, `Kalamazoo`, `Jackson`, `Flint`, and `Kendallville` so overview pages now distinguish contract-versus-loan paths, leak-or-construction triggers, invitation-only batches, funded no-charge replacement, city-area scheduling, consent-form workflows, and annual notice plus right-of-entry requirements more explicitly
- Another low-confidence cost pass sharpened `Denver`, `Providence`, `Philadelphia`, `Pittsburgh`, `Madison`, `Iowa City`, and `Ames` so `replacement-cost` pages now explain capped reimbursements, contract-versus-loan paths, customer-owned service lines, income-tier reimbursement, rebate-only private-side support, and owner-managed contractor workflows without promoting those routes out of `noindex`

## Production hardening
- `gg.jte.developmentMode=false`
- `gg.jte.usePrecompiledTemplates=true`
- Runtime still uses file-based JSON loads, but template rendering is now configured for production-style execution
- Public and internal templates now share the same frontend system rather than the earlier placeholder layout: home, lookup, utility routes, state hubs, state program rollups, evergreen guides, buyer-seller support, and ops-review all render with the same editorial shell or internal dashboard treatment
- A browser QA pass now confirms the main public routes render cleanly without current-page console errors, and shared head metadata now includes a real favicon asset instead of returning the earlier `favicon.ico` 404

## Data validation
- `.\gradlew.bat buildData` regenerates normalized JSON and derived routes from raw CSV and JSON inputs
- `.\gradlew.bat verifySourceUrls` runs the same schema and freshness checks plus live official URL reachability checks
- Chicago and Racine now use stable official guidance pages that pass the live reachability checks in this environment
- Official PDFs and civic pages that incorrectly answer `HEAD` with an error now get a fallback `GET` check before the build fails
- Repeated external connect timeouts are now retried and downgraded to warnings so transient municipal-site outages do not fail the whole batch
- Source records older than the configured freshness window fail the build
- Utility and program source URLs must stay HTTPS and map back to a known utility, program, or cost record

## Run the app
- `.\gradlew.bat buildData`
- `.\gradlew.bat verifySourceUrls`
- `.\gradlew.bat bootRun`
- `.\gradlew.bat cleanTest test`

## Agent read order
1. `AGENT_START_HERE.md`
2. `ops/context_tracker.md`
3. This file
4. `spec/00_strategy.md` through `spec/08_delivery_and_handoff.md`

## Build principles
- Treat utility and program data as the moat.
- Keep public-side and private-side responsibilities explicit.
- Distinguish service line material from interior plumbing and fixtures.
- Build trust through exact utility links, update dates, and narrow claims.
- Keep all publishable data reproducible from versioned `CSV` and `JSON` files.
