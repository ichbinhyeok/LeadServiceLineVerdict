# 07 Technical Architecture

## Recommended application shape
- `Spring Boot` MVC application
- `jte` templates for utility, state, program, and guide pages
- Request-time rendering backed by in-memory file data and aggressive page caching
- Selective POST endpoints for lead forms, lightweight lookup helpers, and ops utilities
- File-based storage with raw `CSV` imports plus normalized and derived `JSON`
- Build-time schema validation and scheduled refresh jobs for source verification

## Package root
`owner.leadserviceline`

## Repository map
- `src/main/java/owner/leadserviceline/web`
- `src/main/java/owner/leadserviceline/pages`
- `src/main/java/owner/leadserviceline/data`
- `src/main/java/owner/leadserviceline/ingest`
- `src/main/java/owner/leadserviceline/seo`
- `src/main/java/owner/leadserviceline/leads`
- `src/main/java/owner/leadserviceline/analytics`
- `src/main/java/owner/leadserviceline/ops`
- `src/main/jte`
- `src/main/resources`
- `data/raw`
- `data/normalized`
- `data/derived`
- `scripts/ingest`
- `scripts/verify`
- `scripts/build-data`
- `ops/overrides`

## Core services
- Raw source importers
- Entity normalizers
- Source evidence ledger builder
- Route inventory builder
- Page model builder
- Source freshness auditor
- CTA routing service
- Sitemap and index policy builder

## Rendering model
- Utility page is the main render target
- Supporting routes render from the same entity graph with intent-specific modules
- State hubs aggregate utility and program entities
- Most routes should render from normalized JSON loaded into memory at startup
- Form submission and optional lookup helpers require request-time execution
- Public pages should be cacheable at the CDN and application level

## Data and build rules
- Raw `CSV` and source snapshots are the editable input layer
- Normalized `JSON` is the source of truth for templates
- Derived `JSON` contains route inventory, state aggregates, and lookup helpers
- Build must fail when schema validation or source freshness rules fail
- Manual overrides should be file-based and auditable

## Operational requirements
- Source refresh cadence by page family
- Manual override when utility URLs break or rules conflict
- Page-level freshness labels
- Suppression of stale or unverifiable pages from index
- Rebuild triggers after data-file changes or scheduled refresh runs
