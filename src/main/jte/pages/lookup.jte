@import owner.leadserviceline.pages.LookupPageModel
@import owner.leadserviceline.pages.PageSeoModel
@param LookupPageModel page
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
    <script src="/app.js?v=20260406-mobile-nav1" defer></script>
    <link rel="stylesheet" href="/app.css?v=20260406-mobile-nav1">
    <link rel="icon" href="/favicon.svg?v=20260405-qa3" type="image/svg+xml">
</head>
<body>
<header class="site-header">
    <div class="site-header__inner">
        <a class="brand-mark" href="/">Lead Line Record</a>
        <nav class="site-nav">
            <a href="/">Home</a>
            <a class="active" href="/lookup">Lookup</a>
            <a href="/#states">States</a>
            <a href="/#guides">Guides</a>
        </nav>
        <details class="site-nav-mobile" data-mobile-nav>
            <summary class="site-nav-mobile__toggle">
                <span class="material-symbols-outlined site-nav-mobile__icon--menu">menu</span>
                <span class="material-symbols-outlined site-nav-mobile__icon--close">close</span>
                <span>Menu</span>
            </summary>
            <nav class="site-nav-mobile__panel">
                <a href="/">Home</a>
                <a class="active" href="/lookup">Lookup</a>
                <a href="/#states">States</a>
                <a href="/#guides">Guides</a>
            </nav>
        </details>
        <div class="site-avatar"><span class="material-symbols-outlined">water_drop</span></div>
    </div>
</header>

<main class="page-shell">
    <section class="hero-band">
        <div class="hero-grid">
            <div class="hero-copy">
                <div class="status-pill ${page.ambiguous() ? "warm" : ""}">Utility lookup</div>
                <h1>Find the most likely utility before you rely on a notice, program, or cost page.</h1>
                <p>This lookup is meant to narrow to the right utility page. The utility's own map, address checker, or mailed notice still controls the final handoff.</p>
                @if (page.attempted())
                    <div class="note-panel" style="margin-top: 24px;">
                        <strong>Match summary</strong>
                        <p>${page.resultSummary()}</p>
                        @if (page.geocoderSummary() != null && !page.geocoderSummary().isBlank())
                            <p class="muted">${page.geocoderSummary()}</p>
                        @endif
                    </div>
                @endif
            </div>
            <div class="hero-media">
                <div class="hero-media__frame">
                    <img src="/media/boundary-diagram.jpg" alt="Cross section showing public and private water line boundary">
                </div>
                <div class="floating-note">
                    <strong>Privacy boundary</strong>
                    <p>Address lookups submit by POST, not GET. Optional internal diagnostics avoid storing raw address text.</p>
                </div>
            </div>
        </div>
    </section>

    <section class="lookup-panel">
        <div class="editorial-line">
            <div class="kicker">Utility lookup</div>
        </div>
        <h2>Enter an address hint, exact city, or known utility name.</h2>
        <form class="lookup-form" method="post" action="/lookup">
            <label>
                Address or utility hint
                <input type="text" name="query" value="${page.query()}" placeholder="123 Main St or utility name">
            </label>
            <label>
                City
                <input type="text" name="city" value="${page.city()}" placeholder="Milwaukee">
            </label>
            <label>
                State
                <input type="text" name="state" value="${page.state()}" placeholder="WI">
            </label>
            <button type="submit">Resolve utility</button>
        </form>
    </section>

    <section class="section-block">
        <div class="editorial-line">
            <div class="kicker">Lookup results</div>
        </div>
        <h2>Best local matches</h2>
        @if (!page.attempted())
            <div class="support-note">
                Enter a street hint, city, state, or utility name to narrow to the right local record.
            </div>
        @elseif (page.matches().isEmpty())
            <div class="support-note">
                <p class="warning">No utility match cleared the threshold.</p>
                <p class="muted">Add an exact city, ZIP, or utility name, then confirm on the official utility lookup before relying on local guidance.</p>
            </div>
        @else
            <div class="section-grid two">
                @for (var match : page.matches())
                    <div class="content-card">
                        <span class="status-pill ${match.manualReviewRecommended() ? "warm" : ""}">${match.confidenceLabel()}</span>
                        <h3 style="margin-top: 14px;"><a href="${match.localPath()}">${match.utility().utilityName()}</a></h3>
                        <p class="muted">${match.utility().city()}, ${match.utility().state().toUpperCase()}</p>
                        <p>${match.utility().inventorySummary() != null && !match.utility().inventorySummary().isBlank()
                                ? match.utility().inventorySummary()
                                : match.utility().inventoryStatus()}</p>
                        <div class="content-stack" style="margin-top: 16px;">
                            <div class="card nested">
                                <h3>Why this looks like the right utility</h3>
                                <p>${String.join(" | ", match.reasons())}</p>
                            </div>
                            <div class="hero-actions" style="margin-top: 0;">
                                <a class="button line" href="${match.localPath()}">Open utility page</a>
                                <a class="button secondary" href="${match.utility().lookupUrl()}">Official lookup</a>
                            </div>
                        </div>
                    </div>
                @endfor
            </div>
        @endif
    </section>

    <section class="section-block section-soft">
        <div class="editorial-line">
            <div class="kicker">Interim buying guides</div>
        </div>
        <h2>Use product decisions only after you land on the right utility.</h2>
        <div class="section-grid three">
            <div class="content-card tint-blue">
                <span class="label">Filters</span>
                <h3><a href="/guides/best-lead-reduction-filters-after-a-lead-notice">Best lead-reduction filters after a lead notice</a></h3>
                <p>For readers who need an interim drinking-and-cooking water plan while the official replacement path is still pending.</p>
            </div>
            <div class="content-card tint-warm">
                <span class="label">Testing</span>
                <h3><a href="/guides/best-water-test-kits-after-a-lead-notice">Best water test kits after a lead notice</a></h3>
                <p>For readers whose next decision depends on a stronger sampling workflow, not a generic strip result.</p>
            </div>
            <div class="content-card tint-soft">
                <span class="label">Disclosure</span>
                <h3><a href="/affiliate-disclosure">How recommendations work</a></h3>
                <p>Buying guides are secondary support pages and do not outrank the utility's own lookup, map, or notice language.</p>
            </div>
        </div>
    </section>
</main>

<footer class="footer">
    <div class="footer__inner">
        <div>
            <div class="brand-mark">Lead Line Record</div>
            <p class="muted">Use lookup to find the likely utility, then confirm on the official address checker or map.</p>
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
