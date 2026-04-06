# Launch Index Cohort

Last updated: 2026-04-06

This is the first utility batch to open and request in Search Console. The list favors utilities with:

- `4` indexable utility routes already live
- verified program coverage
- an indexable cost route
- an official lookup path when available
- current local source references and verification status

## Cache + crawl notes

- After deploy, purge Cloudflare cache for `/`, `/sitemap.xml`, `/robots.txt`, and the specific utility overview URLs below before using URL Inspection or Request Indexing.
- If Search Console still shows stale HTML, use live inspection first and confirm the returned HTML includes the new title, trust block, and social meta.
- Request the overview URL first. Let sitemap discovery pick up the matching `notification`, `program`, and `replacement-cost` URLs that are already indexable.

## First 25 utility overview URLs

| Priority | Utility | State | Overview URL | Why it made the first batch |
| --- | --- | --- | --- | --- |
| 1 | Saint Paul Regional Water Services | MN | `/lead-service-line/mn/saint-paul/saint-paul-regional-water-services` | 4 indexable routes, official lookup, verified program, indexed cost route |
| 2 | Milwaukee Water Works | WI | `/lead-service-line/wi/milwaukee/milwaukee-water-works` | 4 indexable routes, official lookup, 2 linked programs, indexed cost route |
| 3 | City of Phoenix Water Services | AZ | `/lead-service-line/az/phoenix/phoenix-water-services` | 4 indexable routes, official lookup, verified program, indexed cost route |
| 4 | Tucson Water | AZ | `/lead-service-line/az/tucson/tucson-water` | 4 indexable routes, official lookup, verified program, indexed cost route |
| 5 | DC Water | DC | `/lead-service-line/dc/washington/dc-water` | 4 indexable routes, official lookup, 2 linked programs, indexed cost route |
| 6 | Iowa American Water | IA | `/lead-service-line/ia/davenport/iowa-american-water` | 4 indexable routes, official lookup, verified program, indexed cost route |
| 7 | City of Aurora Water Production Division | IL | `/lead-service-line/il/aurora/aurora-water-production-division` | 4 indexable routes, official lookup, verified program, indexed cost route |
| 8 | City of Bloomington Water Department | IL | `/lead-service-line/il/bloomington/bloomington-water-department` | 4 indexable routes, official lookup, verified program, indexed cost route |
| 9 | City of Joliet Department of Public Utilities | IL | `/lead-service-line/il/joliet/joliet-public-utilities` | 4 indexable routes, official lookup, verified program, indexed cost route |
| 10 | Fort Wayne City Utilities | IN | `/lead-service-line/in/fort-wayne/fort-wayne-city-utilities` | 4 indexable routes, official lookup, verified program, indexed cost route |
| 11 | City of Lafayette Water Works | IN | `/lead-service-line/in/lafayette/lafayette-water-works` | 4 indexable routes, official lookup, verified program, indexed cost route |
| 12 | City of Flint Water Service Center | MI | `/lead-service-line/mi/flint/flint-water-service-center` | 4 indexable routes, official lookup, verified program, indexed cost route |
| 13 | City of Grand Rapids Water System | MI | `/lead-service-line/mi/grand-rapids/grand-rapids-water-system` | 4 indexable routes, official lookup, verified program, indexed cost route |
| 14 | City of Kalamazoo Water Department | MI | `/lead-service-line/mi/kalamazoo/kalamazoo-water-department` | 4 indexable routes, official lookup, verified program, indexed cost route |
| 15 | City of Duluth Lead Removal Program | MN | `/lead-service-line/mn/duluth/duluth-lead-removal-program` | 4 indexable routes, official lookup, verified program, indexed cost route |
| 16 | City of Minneapolis Public Works Water Treatment & Distribution Services | MN | `/lead-service-line/mn/minneapolis/minneapolis-public-works-water` | 4 indexable routes, official lookup, verified program, indexed cost route |
| 17 | Metropolitan Utilities District | NE | `/lead-service-line/ne/omaha/mud-omaha` | 4 indexable routes, official lookup, verified program, indexed cost route |
| 18 | Greater Cincinnati Water Works | OH | `/lead-service-line/oh/cincinnati/greater-cincinnati-water-works` | 4 indexable routes, official lookup, verified program, indexed cost route |
| 19 | City of Columbus Division of Water | OH | `/lead-service-line/oh/columbus/columbus-division-of-water` | 4 indexable routes, official lookup, 2 linked programs, indexed cost route |
| 20 | City of Newark Water Office | OH | `/lead-service-line/oh/newark/newark-water` | 4 indexable routes, official lookup, verified program, indexed cost route |
| 21 | City of Toledo Division of Water Distribution | OH | `/lead-service-line/oh/toledo/toledo-water-distribution` | 4 indexable routes, official lookup, 2 linked programs, indexed cost route |
| 22 | City of Evanston Water Production Bureau | IL | `/lead-service-line/il/evanston/evanston-water-production-bureau` | 4 indexable routes, verified program, indexed cost route, weaker than top 21 because lookup is not official_lookup |
| 23 | City of Kendallville Water Department | IN | `/lead-service-line/in/kendallville/kendallville-water-department` | 4 indexable routes, verified program, indexed cost route, weaker than top 21 because lookup is contact_only |
| 24 | City of Jackson Water Department | MI | `/lead-service-line/mi/jackson/jackson-water-department` | 4 indexable routes, verified program, indexed cost route, weaker than top 21 because lookup is service_area_notes |
| 25 | Missouri American Water | MO | `/lead-service-line/mo/st-louis/missouri-american-water` | 4 indexable routes, verified program, indexed cost route, weaker than top 21 because lookup is contact_only |

## Hold for a second wave

- Utilities with only `3` indexable routes or weaker lookup handoff should wait until the first batch is crawled cleanly.
- National guides should support the utility pages, not lead the first indexing push.
- Keep `noindex` support routes and evidence-held cost pages out of manual indexing requests.

## Deferred admin work

- `admin` is intentionally disabled for launch right now.
- When it is worth turning back on, re-enable it with `lead-service-line.admin-enabled=true`.
- Before re-enabling, set `LEAD_LINE_ADMIN_USERNAME` and `LEAD_LINE_ADMIN_PASSWORD` in the deploy environment instead of relying on defaults.
- If click activity becomes useful, confirm the deploy target keeps `data/logs/*.jsonl` between restarts or move the logs to durable storage first.
