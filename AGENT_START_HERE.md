# Agent Start Here

## Project
LeadServiceLineVerdict

## Current implementation direction
- Preferred stack: `Spring Boot` + `jte`
- Preferred storage: raw `CSV` imports plus normalized and derived `JSON`
- No runtime database in phase 1

## What this folder contains
- A self-contained product and implementation spec for a utility-first lead service line decision site.
- Enough context for a new agent to resume work without chat history.

## Read order
1. `ops/context_tracker.md`
2. `README.md`
3. `spec/00_strategy.md`
4. `spec/01_query_and_user_map.md`
5. `spec/02_site_architecture.md`
6. `spec/03_data_and_operations.md`
7. `spec/04_commercial_model.md`
8. `spec/05_editorial_rules_and_execution.md`
9. `spec/06_indexing_quality_and_analytics.md`
10. `spec/07_technical_architecture.md`
11. `spec/08_delivery_and_handoff.md`

## Rules for any future agent
- Utility pages are the canonical SEO unit.
- City pages only exist when they change the answer materially.
- Official utility and EPA sources outrank all secondary sources.
- The source of truth is the file-based data pipeline, not a database.
- The canonical route inventory lives in `spec/02_site_architecture.md`.
- If strategy changes, update the relevant spec file and `ops/context_tracker.md`.

## Minimum handoff standard
- Update `Current status`
- Update `Latest decisions`
- Update `What changed this session`
- Update `Next recommended tasks`
- Update `Open questions`
