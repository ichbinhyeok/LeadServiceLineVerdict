# 03 Data And Operations

## 1) Source classes
- EPA rule and guidance pages
- Utility inventories and customer notices
- Water quality and lead line program pages
- City or county program pages
- Replacement grant and assistance pages
- Lab or filter certification references

## 2) Storage decision
- No database in phase 1.
- Raw imports live in `CSV` when the shape is tabular and `JSON` when the source is nested.
- Normalized entities, source evidence, and derived route indexes live in `JSON`.
- Every public page must be reproducible from committed data files and build scripts.

## 3) Directory contract
- `data/raw/utilities/*.csv`
- `data/raw/programs/*.csv`
- `data/raw/costs/*.csv`
- `data/raw/filters/*.csv`
- `data/raw/sources/*.json`
- `data/normalized/utilities/{utility_id}.json`
- `data/normalized/programs/{program_id}.json`
- `data/normalized/costs/{cost_id}.json`
- `data/normalized/filters/{record_id}.json`
- `data/normalized/sources/{source_id}.json`
- `data/derived/routes.json`
- `data/derived/states/{state}.json`
- `data/derived/utilities/{utility_id}.json`
- `ops/overrides/*.json`

## 4) Entity contract
### Utility record
- `utility_id`
- `utility_slug`
- `utility_name`
- `aliases[]`
- `city`
- `county`
- `state`
- `service_area_name`
- `service_area_type`
- `inventory_url`
- `lookup_url`
- `notification_url`
- `program_ids[]`
- `contact_phone`
- `contact_email`
- `inventory_status`
- `line_counts.known`
- `line_counts.potential`
- `line_counts.unknown`
- `line_counts.non_lead`
- `address_lookup_mode`
- `source_refs[]`
- `verification_status`
- `last_verified`

### Program record
- `program_id`
- `program_slug`
- `program_name`
- `geography`
- `state`
- `utility_ids[]`
- `subsidy_type`
- `public_side_covered`
- `private_side_covered`
- `income_rules`
- `property_rules`
- `contractor_rules`
- `deadline`
- `application_url`
- `source_refs[]`
- `verification_status`
- `last_verified`

### Cost record
- `cost_id`
- `utility_id`
- `city`
- `state`
- `public_side_band`
- `private_side_band`
- `full_replacement_band`
- `cost_confidence`
- `housing_type_assumption`
- `permit_assumption`
- `restoration_assumption`
- `source_refs[]`
- `last_verified`

### Filter and testing record
- `record_id`
- `product_or_lab_name`
- `type`
- `certification_note`
- `coverage_area`
- `price_band`
- `affiliate_or_sponsor_status`
- `source_refs[]`
- `last_reviewed`

### Source evidence record
- `source_id`
- `source_type`
- `publisher_name`
- `source_url`
- `scope_type`
- `scope_key`
- `captured_at`
- `effective_date`
- `claim_summary`
- `reviewer_note`
- `status`

### Route record
- `path`
- `template`
- `state`
- `city`
- `utility_id`
- `program_id`
- `canonical_path`
- `indexable`
- `decision_reason`
- `last_generated`

## 5) Verification workflow
1. Capture the official source URL and store a source-evidence record.
2. Extract the current inventory, program, or notice language into a claim summary.
3. Record the exact scope: utility, city, neighborhood, or state.
4. Update the normalized entity with `source_refs[]`, verification status, and reviewer note.
5. Regenerate the route inventory and confirm the index or noindex outcome.
6. Re-check pilot utilities monthly and long-tail utilities quarterly.

## 6) Address and utility mapping strategy
- Prefer the official utility lookup URL when the utility provides one.
- If no direct lookup exists, map by service-area name, aliases, and clear city coverage notes.
- Mark mapping confidence when the page is city-led rather than address-led.
- Do not imply parcel-level certainty when only city or service-area evidence exists.

## 7) Refresh cadence
- High-traffic utility pages: monthly
- State program hubs: monthly
- Cost pages: every 60-90 days
- Buyer/seller and filter pages: quarterly
- Long-tail utility pages: 90-120 days

## 8) Data quality gates
- Never infer lead counts without source support.
- Never merge public-side and private-side status unless the source does.
- Never publish a replacement program if the eligibility is unclear.
- Never treat a filter as equivalent to line replacement.
- Never claim a city inventory is current if only an old PDF exists.
- Never publish an indexable route without a route record and a source-evidence record.

## 9) Enrichment workflow
- Normalize utility names and aliases.
- Normalize city, county, and state spellings.
- Map notices to the exact utility service area.
- Attach the inventory lookup or notice page to the utility record.
- Track whether the user can self-serve lookup, call, or download a form.
- Generate derived state, utility, and route artifacts from normalized JSON.

## 10) Scale strategy
- Start with utilities that have public inventories and clear notice pages.
- Build the program registry before writing large cost pages.
- Add buyer/seller pages only after the cost and program data are stable.
- Expand state hubs after the utility model works repeatably.
