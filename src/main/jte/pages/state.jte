@import owner.leadserviceline.pages.StatePageModel
@import owner.leadserviceline.pages.PageSeoModel
@param StatePageModel page
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
        <script type="application/ld+json">${jsonLd}</script>
    @endfor
    <link rel="stylesheet" href="/app.css?v=20260405-qa3">
    <link rel="icon" href="/favicon.svg?v=20260405-qa3" type="image/svg+xml">
</head>
<body>
<header class="site-header">
    <div class="site-header__inner">
        <a class="brand-mark" href="/">Lead Line Record</a>
        <nav class="site-nav">
            <a href="/">Home</a>
            <a href="/lookup">Lookup</a>
            <a class="active" href="/lead-service-line/${page.state()}">States</a>
            <a href="/#guides">Guides</a>
        </nav>
        <div class="site-avatar"><span class="material-symbols-outlined">water_drop</span></div>
    </div>
</header>

<main class="page-shell">
    <section class="hero-band">
        <div class="hero-grid">
            <div class="hero-copy">
                <div class="kicker">State utility list</div>
                <h1>${page.state().toUpperCase()} lead service line utilities.</h1>
                <p>Use the state page when you do not know the utility yet. The real answer still lives on the local utility page, where notice language, replacement support, and cost responsibility can differ by operator.</p>
                <div class="hero-actions">
                    <a class="button" href="/lookup">Find your utility</a>
                    @if (page.programCount() > 0)
                        <a class="button secondary" href="/lead-service-line/${page.state()}/programs">Open state program rollup</a>
                    @endif
                </div>
                <div class="stats-grid">
                    <div class="stat-card">
                        <small>Utilities covered</small>
                        <strong>${page.utilities().size()}</strong>
                        <p>Utility pages currently covered in ${page.state().toUpperCase()}.</p>
                    </div>
                    <div class="stat-card soft">
                        <small>Verified programs</small>
                        <strong>${page.programCount()}</strong>
                        <p>Replacement support records currently surfaced for this state.</p>
                    </div>
                    <div class="stat-card warm">
                        <small>Pages live</small>
                        <strong>${page.routeCount()}</strong>
                        <p>Overview, notice, program, cost, and selective support pages currently live in this state.</p>
                    </div>
                </div>
            </div>
            <div class="hero-media">
                <div class="hero-media__frame">
                    <img src="/media/service-line-diagram.jpg" alt="Lead service line diagram and local record blueprint">
                </div>
                <div class="floating-note">
                    <strong>How to use this page</strong>
                    <p>State pages help readers orient. Utility pages still carry the authoritative local distinctions around lead, possible lead, unknown material, and who pays.</p>
                </div>
            </div>
        </div>
    </section>

    <section class="section-block">
        <div class="editorial-line">
            <div class="kicker">How to use the state page</div>
        </div>
        <h2>Use the state page to narrow the right utility.</h2>
        <div class="section-grid three">
            <div class="content-card tint-soft">
                <span class="label">1. Find the utility</span>
                <h3>Use the state page to narrow the local operator.</h3>
                <p>The correct utility matters more than the city label alone because the inventory record, notice language, and program rules can all change by operator.</p>
            </div>
            <div class="content-card tint-blue">
                <span class="label">2. Open the utility page</span>
                <h3>Then open the utility page.</h3>
                <p>The utility page is where the local record separates overview, notification, program, and replacement-cost questions instead of flattening them into one generic answer.</p>
            </div>
            <div class="content-card tint-warm">
                <span class="label">3. Confirm on the official lookup</span>
                <h3>Address truth still lives with the utility.</h3>
                <p>Every directory link should end with the official address lookup, service line map, or notice page before any replacement or transaction decision gets made.</p>
            </div>
        </div>
    </section>

    <section class="section-block section-soft">
        <div class="section-grid two">
            <div>
                <div class="editorial-line">
                    <div class="kicker">Utility directory</div>
                </div>
                <h2>Verified utility cohort for ${page.state().toUpperCase()}.</h2>
                <p class="lede">Use these entries to find the right utility. Then open the local record for the actual answer.</p>
            </div>
            <div class="support-banner">
                    <strong>Keep the utility page primary</strong>
                    <p>City and state pages help you navigate. The utility page is still the place to read the actual notice, program, and replacement details.</p>
            </div>
        </div>
        <div class="section-grid two" style="margin-top: 28px;">
            @for (var utility : page.utilities())
                <div class="content-card">
                    <span class="status-pill">${utility.city()}</span>
                    <h3><a href="/lead-service-line/${utility.state()}/${utility.city()}/${utility.utilitySlug()}">${utility.utilityName()}</a></h3>
                    @if (utility.inventorySummary() != null && !utility.inventorySummary().isBlank())
                        <p>${utility.inventorySummary()}</p>
                    @else
                        <p>${utility.inventoryStatus()}.</p>
                    @endif
                    <p><strong>Lookup mode:</strong> ${utility.addressLookupMode()}</p>
                    <p class="muted">Last verified ${utility.lastVerified().toString()}.</p>
                </div>
            @endfor
        </div>
    </section>

    <section class="section-block">
        <div class="section-grid two">
            <div>
                <div class="editorial-line">
                    <div class="kicker">Program layer</div>
                </div>
                <h2>Statewide support still flows through utility pages.</h2>
                <p>The state program page is useful when the question is <em>which utilities in this state have verified support?</em> It is not a replacement for the local utility record.</p>
            </div>
            <div class="aside-panel">
                <h2>Use the rollup when funding is the first question.</h2>
                <p>Once the program or reimbursement path matters more than the inventory label itself, open the state program page and then move back into the matching utility page.</p>
                @if (page.programCount() > 0)
                    <div class="hero-actions">
                        <a class="button" href="/lead-service-line/${page.state()}/programs">Open ${page.state().toUpperCase()} program rollup</a>
                    </div>
                @endif
            </div>
        </div>
    </section>

    <section class="section-block section-soft">
        <div class="editorial-line">
            <div class="kicker">Interim buying guides</div>
        </div>
        <h2>Useful only after the right utility is identified.</h2>
        <div class="section-grid three">
            <div class="content-card tint-blue">
                <span class="label">Filter guide</span>
                <h3><a href="/guides/best-lead-reduction-filters-after-a-lead-notice">Best lead-reduction filters after a lead notice</a></h3>
                <p>Use this after the utility record is clear and the household needs an interim filter format that actually fits the kitchen setup.</p>
            </div>
            <div class="content-card tint-warm">
                <span class="label">Testing guide</span>
                <h3><a href="/guides/best-water-test-kits-after-a-lead-notice">Best water test kits after a lead notice</a></h3>
                <p>Use this after the local notice is clear and a lab-backed test would change the next decision.</p>
            </div>
            <div class="content-card tint-soft">
                <span class="label">Disclosure</span>
                <h3><a href="/affiliate-disclosure">How recommendations work</a></h3>
                <p>Buying guides remain secondary and should never rewrite a utility's own inventory, notice, or replacement path.</p>
            </div>
        </div>
    </section>
</main>

<footer class="footer">
    <div class="footer__inner">
        <div>
            <div class="brand-mark">Lead Line Record</div>
            <p class="muted">${page.state().toUpperCase()} utility directory and program tracker for lead service line decisions.</p>
        </div>
        <div class="footer__links">
            <a href="/">Home</a>
            <a href="/lookup">Lookup</a>
            @if (page.programCount() > 0)
                <a href="/lead-service-line/${page.state()}/programs">Programs</a>
            @endif
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

