@import owner.leadserviceline.pages.HomePageModel
@import owner.leadserviceline.pages.PageSeoModel
@param HomePageModel page
@param PageSeoModel seo
<!doctype html>
<html lang="en">
<head>
    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>${seo.title()}</title>
    <meta name="description" content="${seo.description()}">
    <link rel="canonical" href="${seo.canonicalUrl()}">
    <meta name="robots" content="${seo.robots()}">
    <meta property="og:site_name" content="Lead Line Record">
    <meta property="og:title" content="${seo.title()}">
    <meta property="og:description" content="${seo.description()}">
    <meta property="og:url" content="${seo.canonicalUrl()}">
    <meta property="og:type" content="website">
    @if (seo.socialImageUrl() != null && !seo.socialImageUrl().isBlank())
        <meta property="og:image" content="${seo.socialImageUrl()}">
        <meta name="twitter:card" content="summary_large_image">
        <meta name="twitter:image" content="${seo.socialImageUrl()}">
    @else
        <meta name="twitter:card" content="summary">
    @endif
    <meta name="twitter:title" content="${seo.title()}">
    <meta name="twitter:description" content="${seo.description()}">
    @for (var jsonLd : seo.jsonLd())
        <script type="application/ld+json">$unsafe{jsonLd}</script>
    @endfor
    <link rel="stylesheet" href="/app.css?v=20260405-qa3">
    <link rel="icon" href="/favicon.svg?v=20260405-qa3" type="image/svg+xml">
</head>
<body>
<header class="site-header">
    <div class="site-header__inner">
        <a class="brand-mark" href="/">Lead Line Record</a>
        <nav class="site-nav">
            <a class="active" href="/">Home</a>
            <a href="/lookup">Lookup</a>
            <a href="/#states">States</a>
            <a href="/#guides">Guides</a>
        </nav>
        <div class="site-avatar"><span class="material-symbols-outlined">water_drop</span></div>
    </div>
</header>

<main class="page-shell">
    <section class="hero-band">
        <div class="hero-grid">
            <div class="hero-copy">
                <div class="kicker">Utility-by-utility lead line answers</div>
                <h1>Find your utility's lead line record, replacement program, and likely cost.</h1>
                <p>Start with the utility that serves the address. Use the local record to see what the inventory says, what a mailed notice means, whether a replacement program exists, and how replacement costs may be split when the published local record is strong enough.</p>
                <div class="hero-actions">
                    <a class="button" href="#lookup">Find your utility</a>
                    <a class="button secondary" href="#featured">Browse utilities</a>
                </div>
                <div class="stats-grid">
                    <div class="stat-card">
                        <small>Utilities covered</small>
                        <strong>${page.featuredUtilities().size()}</strong>
                        <p>Current local records for utilities with inventory, notice, program, or cost guidance.</p>
                    </div>
                    <div class="stat-card soft">
                        <small>States covered</small>
                        <strong>${page.states().size()}</strong>
                        <p>Use the state page to find the right utility, then move into the local record.</p>
                    </div>
                    <div class="stat-card warm">
                        <small>Programs tracked</small>
                        <strong>${page.programCount()}</strong>
                        <p>Local replacement programs, reimbursements, and funded support paths now tracked on the site.</p>
                    </div>
                </div>
            </div>
            <div class="hero-media">
                <div class="hero-media__frame">
                    <img src="/media/utility-meter.jpg" alt="Water meter and copper service piping">
                </div>
                <div class="floating-note">
                    <strong>Built for real local decisions</strong>
                    <p>Every page is tied to a real utility, its notices, and its published replacement rules. When the local record is weak, the site stays cautious.</p>
                </div>
            </div>
        </div>
    </section>

    <section class="lookup-panel" id="lookup">
        <div class="editorial-line">
            <div class="kicker">Utility lookup</div>
        </div>
        <h2>Start with your utility, then confirm on the official address checker.</h2>
        <p class="lede">Use this when you know the address, city, or utility name but not the right local record yet. The lookup points you to the most likely utility page, then hands you to the official lookup.</p>
        <form class="lookup-form" method="post" action="/lookup">
            <label>
                Address or utility hint
                <input type="text" name="query" placeholder="123 Main St or DC Water">
            </label>
            <label>
                City
                <input type="text" name="city" placeholder="Washington">
            </label>
            <label>
                State
                <input type="text" name="state" placeholder="DC">
            </label>
            <button type="submit">Find utility</button>
        </form>
        <div class="chip-row">
            <span><span class="dot danger"></span>Known lead or modeled lead risk</span>
            <span><span class="dot warm"></span>Potential or verification-needed notice</span>
            <span><span class="dot soft"></span>Unknown material or incomplete records</span>
            <span><span class="dot blue"></span>Verified non-lead or zero-known-lead inventory</span>
        </div>
    </section>

    <section class="section-block section-soft">
        <div class="section-grid two">
            <div id="states">
                <div class="editorial-line">
                    <div class="kicker">Browse by state</div>
                </div>
                <h2>If you do not know the utility yet, start with the state and move into the local record.</h2>
                <p class="lede">State pages help you narrow the search. The utility page is still where notice, program, and replacement questions get answered.</p>
                <div class="content-stack">
                    @for (var state : page.states())
                        <div class="content-card">
                            <h3><a href="/lead-service-line/${state.state()}">${state.state().toUpperCase()}</a></h3>
                            <p>${state.utilityCount()} utilities and ${state.indexableRouteCount()} pages currently live.</p>
                        </div>
                    @endfor
                </div>
            </div>
            <div id="featured">
                <div class="editorial-line">
                    <div class="kicker">Featured utility records</div>
                </div>
                <h2>Utility pages are where notice, program, and replacement questions get answered.</h2>
                <p class="lede">Each record keeps four questions separate: what the utility says about the line, what the notice means, whether a replacement program exists, and who may have to pay.</p>
                <div class="content-stack">
                    @for (var utility : page.featuredUtilities())
                        <div class="content-card">
                            <span class="status-pill">${utility.state().toUpperCase()} / ${utility.city()}</span>
                            <h3><a href="/lead-service-line/${utility.state()}/${utility.city()}/${utility.utilitySlug()}">${utility.utilityName()}</a></h3>
                            @if (utility.inventorySummary() != null && !utility.inventorySummary().isBlank())
                                <p>${utility.inventorySummary()}</p>
                            @else
                                <p>${utility.inventoryStatus()}.</p>
                            @endif
                        </div>
                    @endfor
                </div>
            </div>
        </div>
    </section>

    <section class="section-block">
        <div class="editorial-line">
            <div class="kicker">What you can trust here</div>
        </div>
        <h2>The site stays narrow on purpose.</h2>
        <div class="section-grid three">
            <div class="content-card">
                <h3>Start with the utility record</h3>
                <p>Inventory status, address lookup, and notice language come first. Everything else follows after the utility record is clear.</p>
            </div>
            <div class="content-card">
                <h3>Program and cost only with local proof</h3>
                <p>Replacement support and price guidance only appear when the local utility or city record is specific enough to support them.</p>
            </div>
            <div class="content-card">
                <h3>No generic national drift</h3>
                <p>The question is always local: this utility, this city, this notice, this replacement path.</p>
            </div>
        </div>
    </section>

    <section class="section-block section-soft">
        <div class="editorial-line">
            <div class="kicker">Interim buying guides</div>
        </div>
        <h2>When the utility record is clear but replacement is not immediate.</h2>
        <div class="section-grid three">
            <div class="content-card tint-blue">
                <span class="label">Filter guide</span>
                <h3><a href="/guides/best-lead-reduction-filters-after-a-lead-notice">Best lead-reduction filters after a lead notice</a></h3>
                <p>Use this when a notice is real, but the next few weeks still depend on utility verification, funding, or scheduling.</p>
            </div>
            <div class="content-card tint-warm">
                <span class="label">Testing guide</span>
                <h3><a href="/guides/best-water-test-kits-after-a-lead-notice">Best water test kits after a lead notice</a></h3>
                <p>Use this when a lab-backed test would actually change the next action instead of creating more noise.</p>
            </div>
            <div class="content-card tint-soft">
                <span class="label">Disclosure</span>
                <h3><a href="/affiliate-disclosure">How recommendations work</a></h3>
                <p>Buying guides stay secondary to the local utility record and remain editorially separate from future affiliate links.</p>
            </div>
        </div>
    </section>

    <section class="section-block section-soft" id="guides">
        <div class="editorial-line">
            <div class="kicker">Background guides</div>
        </div>
        <h2>Guides help with context, but the utility page stays primary.</h2>
        <p class="lede">Use a guide when you need the basics first. Then move back into the local utility record for the actual decision.</p>
        <div class="section-grid three">
            @for (var guide : page.guides())
                <div class="content-card">
                    <h3><a href="/guides/${guide.slug()}">${guide.title()}</a></h3>
                    <p>${guide.heroSummary()}</p>
                </div>
            @endfor
        </div>
    </section>
</main>

<footer class="footer">
    <div class="footer__inner">
        <div>
            <div class="brand-mark">Lead Line Record</div>
            <p class="muted">Utility-by-utility guidance on lead line notices, replacement programs, and who may pay for replacement.</p>
        </div>
        <div class="footer__links">
            <a href="/">Home</a>
            <a href="/lookup">Lookup</a>
            <a href="/about">About</a>
            <a href="/affiliate-disclosure">Affiliate Disclosure</a>
            <a href="/methodology">Methodology</a>
            <a href="/editorial-policy">Editorial Policy</a>
            <a href="/privacy">Privacy</a>
            <a href="/terms">Terms</a>
            <a href="/contact">Contact</a>
        </div>
    </div>
</footer>
</body>
</html>

