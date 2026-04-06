# 02 Site Architecture

## 1) Canonical entities
- Utility or water authority
- State
- City
- Inventory record
- Program record
- Cost record
- Buyer/seller decision guide
- Filter and testing guide

## 2) URL graph
### Utility pages
- `/lead-service-line/{state}/{city}/{utility-slug}/`
- `/lead-service-line/{state}/{city}/{utility-slug}/notification`
- `/lead-service-line/{state}/{city}/{utility-slug}/replacement-cost`
- `/lead-service-line/{state}/{city}/{utility-slug}/program`
- `/lead-service-line/{state}/{city}/{utility-slug}/filter-and-testing`
- `/lead-service-line/{state}/{city}/{utility-slug}/buyer-seller`

### State and cluster pages
- `/lead-service-line/{state}/`
- `/lead-service-line/{state}/programs`
- `/lead-service-line/{state}/cities`
- `/lead-service-line/{state}/{city}/`
- `/lead-service-line/{state}/programs/{program-slug}`

### Evergreen support content
- `/guides/what-is-a-lead-service-line`
- `/guides/known-vs-potential-lead-service-line`
- `/guides/who-pays-for-lead-service-line-replacement`
- `/guides/lead-service-line-replacement-cost`
- `/guides/lead-water-filter-vs-replacement`
- `/guides/lead-service-line-buyer-seller-checklist`

## 3) Local page render order
1. H1 + direct verdict
2. Inventory summary box
3. Address-to-utility verification block
4. Replacement decision block
5. Cost module
6. Program module
7. Filter and testing module
8. Buyer/seller module
9. Sources and last verified
10. FAQ

## 4) Page module definitions
### Inventory summary box
- Known, potential, unknown, non-lead counts
- Public-side and private-side language
- Link to the utility inventory or notice page

### Address-to-utility verification block
- Official utility lookup link when available
- Utility service-area notes when direct lookup is unavailable
- Confidence label for utility mapping when the page is city-led

### Replacement decision block
- Replace now
- Apply for program
- Test and filter now
- Ask the seller or utility

### Cost module
- Public-side replacement estimate
- Private-side replacement estimate
- Full replacement estimate
- Restoration and permit assumptions

### Program module
- Utility subsidy
- State or city grant
- Contractor eligibility
- Deadline or application step

### Filter and testing module
- When a test is useful
- What a filter can and cannot do
- When interim filtration is appropriate

### Buyer/seller module
- Disclosure checklist
- Repair credit language
- Escrow timing
- Inspection objection language

## 5) Structured data
- `FAQPage` on all educational and local pages
- `BreadcrumbList` for state/city/utility hierarchy
- `HowTo` only on evergreen process pages
- `LocalBusiness` only on provider referral pages, not on utility pages

## 6) Internal linking system
- Utility page -> notification / cost / program / filter / buyer-seller
- State page -> top utilities + active programs
- Program page -> affected utility pages and cost pages
- Buyer/seller page -> notification guide and cost page
- Filter page -> testing guide and utility inventory page

## 7) Programmatic page eligibility
A local page should go live only if it has:
- a utility or program entity
- an official source URL
- a current inventory or notice statement
- at least one clear next-action path
- a verified date
- a generated route record with an index or noindex decision

## 8) Hold rules
- No utility source or inventory page
- conflicting public and private side information without explanation
- no address, utility, or program mapping
- no actionable next step beyond generic education
- no route-level index decision or canonical target
