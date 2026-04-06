# 00 Strategy and Product Architecture

## 1) One-line thesis
LeadServiceLineVerdict should be a utility-first decision site for homeowners, buyers, and sellers who need to understand a `known` or `potential` lead service line notice, estimate replacement cost, and compare filters, testing, and replacement options.

## 2) Why this vertical works
- Service line inventory and customer notification obligations became a real homeowner-facing issue on October 16, 2024.
- Query intent is strong and local:
  - `city + lead pipe`
  - `utility + lead service line inventory`
  - `replacement cost`
  - `program`
  - `notification letter`
- The page unit is clean. Utility and city pages are much more natural than vague national advice.
- Monetization can start without a lead network through filters, water test kits, local plumbers, and replacement sponsors.

## 3) Primary users
- Homeowners who received a `known` or `potential lead service line` notice.
- Buyers under contract who want replacement cost or negotiation context.
- Sellers who need a disclosure or repair strategy.
- Landlords and property managers handling tenant concerns.
- Residents checking whether the public side, private side, or both are flagged.

## 4) Search thesis
Do not chase `lead pipe replacement cost` nationally. Win utility and city long-tail instead.

### Core long-tail families
- `{utility} lead service line`
- `{city} lead pipe inventory`
- `{utility} lead line replacement program`
- `{city} lead service line replacement cost`
- `what does potential lead service line mean`
- `who pays for lead service line replacement in {city}`

## 5) Canonical entities and URL structure
### Primary canonical page
- `/lead-service-line/{state}/{city}/{utility-slug}`

### Supporting pages
- `/lead-service-line/{state}/{city}/{utility-slug}/replacement-cost`
- `/lead-service-line/{state}/{city}/{utility-slug}/notification`
- `/lead-service-line/{state}/{city}/{utility-slug}/program`
- `/lead-service-line/{state}/{city}/{utility-slug}/filter-and-testing`
- `/lead-service-line/{state}/programs`
- `/lead-service-line/{state}/{city}`

## 6) Page modules
Every utility page should include:

1. Direct-answer hero
- What the inventory says
- What `known`, `potential`, `unknown`, and `non-lead` mean
- The recommended next action

2. Inventory snapshot
- Utility counts
- Public/private side status
- Lookup or map link
- Last update date

3. Replacement decision block
- Replace now
- Test first
- Filter while waiting
- Ask utility
- Ask seller

4. Cost module
- Public-side vs private-side replacement
- Street-to-home complexity
- Restoration assumptions

5. Program module
- Utility funding
- State grants
- Partial/full replacement policy

6. Testing and filter module
- When a water test matters
- When a certified filter is the right interim step

7. Buyer/seller negotiation module
- Inspection objection framing
- Seller credit framing
- Escrow timing checklist

8. Sources and trust block
- Utility source links
- EPA guidance
- Last reviewed date

## 7) Data moat
The moat is not article copy. The moat is the normalized utility and program dataset.

### Utility registry
- Utility name
- City, state, and service area
- Inventory URL
- Lookup URL
- Notification language
- Counts by material status
- Address-to-utility mapping hints and aliases

### Program registry
- Replacement subsidies
- Eligibility
- Private-side responsibility
- Contractor rules
- Deadlines

### Local cost registry
- Street restoration intensity
- Permit friction
- Trenchless vs open trench assumptions

### Housing context
- Older housing stock share
- Likely exposure narrative

## 8) Monetization roadmap
### Phase 1
- NSF/ANSI lead-removal filter affiliates
- Certified water test kit or lab affiliates

### Phase 2
- Local plumber or replacement sponsor blocks on utility pages
- Buyer/seller lead forms on replacement-cost pages

### Phase 3
- Direct city or metro sponsorships for replacement providers
- Premium "program navigator" pages if coverage exists

## 9) Compliance and trust rules
- Do not imply all lead in tap water comes from the service line.
- Distinguish service line material from interior plumbing and fixtures.
- Do not provide medical advice.
- Do not promise that replacement will be utility-funded.
- Treat utility source pages as the authority for current inventory and program status.
- Keep educational estimates clearly labeled as estimates.

## 10) Build sequence
1. Build the file-based utility, program, cost, and source-evidence registries first.
2. Lock the route inventory and page eligibility rules before writing page templates.
3. Launch 10-15 utility pages across two states with strong public inventory data.
4. Add replacement-cost pages only where public/private responsibility is documented.
5. Add buyer/seller negotiation pages after cost and program pages are stable.
6. Expand the seeded cohort to 25-50 utility pages and then add state program hubs.

## 11) Why it can win
- It is boring, local, regulatory, and action-driven.
- Large incumbents usually have national explainers, not deep utility-level decision pages.
- That makes it a strong long-tail asset candidate if the data normalization is done well.

## 12) Anchor sources
- EPA Revised Lead and Copper Rule: https://www.epa.gov/ground-water-and-drinking-water/revised-lead-and-copper-rule
- EPA notification guidance for known or potential lead service lines: https://www.epa.gov/region8-waterops/notification-known-or-potential-service-line-containing-lead
- EPA service line inventory fact sheet: https://www.epa.gov/system/files/documents/2024-10/final_lcri_fact-sheet_service-line-inventory.pdf
