@import owner.leadserviceline.pages.UtilityPageModel
@import owner.leadserviceline.pages.PageSeoModel
@param UtilityPageModel page
@param PageSeoModel seo
@param String gaMeasurementId
<!doctype html>
<html lang="en">
<head>
    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>${seo.title()}</title>
    <meta name="description" content="${seo.description()}">
    <link rel="canonical" href="${seo.canonicalUrl()}">
    <meta name="robots" content="${seo.robots()}">
    @if (gaMeasurementId != null && !gaMeasurementId.isBlank())
        <meta name="ga-measurement-id" content="${gaMeasurementId}">
        <script async src="https://www.googletagmanager.com/gtag/js?id=${gaMeasurementId}"></script>
        <script>
            const gaMeasurementId = document.querySelector('meta[name="ga-measurement-id"]').content;
            window.dataLayer = window.dataLayer || [];
            function gtag(){dataLayer.push(arguments);}
            gtag('js', new Date());
            gtag('config', gaMeasurementId);
        </script>
    @endif
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
    <script src="/app.js?v=20260407-ga-events1" defer></script>
    <link rel="stylesheet" href="/app.css?v=20260406-mobile-nav1">
    <link rel="icon" href="/favicon.svg?v=20260405-qa3" type="image/svg+xml">
    <link rel="alternate icon" href="/favicon.ico?v=20260408-ico1" type="image/x-icon">
    <link rel="apple-touch-icon" sizes="180x180" href="/apple-touch-icon.png?v=20260408-icons1">
    <link rel="manifest" href="/site.webmanifest?v=20260408-icons1">
    <meta name="theme-color" content="#0b5fff">
</head>
<body
        data-ga-page-type="utility"
        data-ga-utility-id="${page.utility().utilityId()}"
        data-ga-utility-slug="${page.utility().utilitySlug()}"
        data-ga-utility-name="${page.utility().utilityName()}"
        data-ga-utility-city="${page.utility().city()}"
        data-ga-utility-state="${page.utility().state()}"
        data-ga-utility-section="${page.section().routeTemplate()}"
        data-ga-route-indexable="${page.currentRoute().indexable()}">
<header class="site-header">
    <div class="site-header__inner">
        <a class="brand-mark" href="/">Lead Line Record</a>
        <nav class="site-nav">
            <a href="/">Home</a>
            <a href="/lookup">Lookup</a>
            <a class="active" href="${page.navigation().stream().filter(candidate -> !candidate.supportLayer()).findFirst().map(link -> link.path()).orElse(seo.canonicalUrl())}">Utility Record</a>
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
                <a href="/lookup">Lookup</a>
                <a class="active" href="${page.navigation().stream().filter(candidate -> !candidate.supportLayer()).findFirst().map(link -> link.path()).orElse(seo.canonicalUrl())}">Utility Record</a>
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
                <div class="status-pill warm">Notice reading</div>
                <h1>${page.heroTitle()}</h1>
                <p>${page.heroSummary()}</p>
                <div class="utility-nav">
                    @for (var link : page.navigation().stream().filter(candidate -> !candidate.supportLayer()).toList())
                        <a href="${link.path()}" class="${link.active() ? "active" : ""}">${link.label()}</a>
                    @endfor
                </div>
            </div>
            <div class="hero-media">
                <div class="hero-media__frame">
                    <img src="/media/service-line-diagram.jpg" alt="Underground service line diagram from main to home">
                </div>
                <div class="floating-note">
                    <strong>Official notice action</strong>
                    <p>Read the mailed or published utility wording exactly, then confirm the address on the utility's own lookup before treating the line status as settled.</p>
                </div>
            </div>
        </div>
    </section>

    <section class="section-block section-soft">
        <div class="section-grid two">
            <div class="content-card">
                <span class="status-pill">Reviewed by</span>
                <h2 style="margin-top: 14px;">${page.trust().reviewedBy()}</h2>
                <p>${page.trust().reviewSummary()}</p>
                <p><strong>Last verified:</strong> ${page.trust().lastVerifiedLabel()}</p>
                <p><strong>Corrections:</strong> <a href="mailto:${page.trust().correctionEmail()}">${page.trust().correctionEmail()}</a></p>
            </div>
            <div class="content-card">
                <span class="status-pill soft">Evidence base</span>
                <h2 style="margin-top: 14px;">${page.trust().evidenceCount()} local source${page.trust().evidenceCount() == 1 ? "" : "s"}</h2>
                @if (!page.trust().publisherNames().isEmpty())
                    <p><strong>Primary publishers:</strong> ${String.join(", ", page.trust().publisherNames())}</p>
                @endif
                <p>The official lookup and notice page still control parcel-level interpretation.</p>
            </div>
        </div>
    </section>

    <section class="section-block">
        <div class="section-grid two">
            <div>
                <div class="editorial-line">
                    <div class="kicker">What this means</div>
                </div>
                <h2>Interpret the local notice, not a generic national script.</h2>
                <p class="lede">${page.heroSummary()}</p>
                <div class="content-stack">
                    @for (var fact : page.keyFacts())
                        <div class="content-card">
                            <h3>${fact.label()}</h3>
                            <p><strong>${fact.value()}</strong></p>
                            @if (fact.detail() != null && !fact.detail().isBlank())
                                <p>${fact.detail()}</p>
                            @endif
                        </div>
                    @endfor
                </div>
            </div>
            <div>
                <div class="aside-panel">
                    <h2>Official utility action</h2>
                    <p>Use the utility record to confirm whether the notice represents a known line, a modeled risk, or a still-unverified material category.</p>
                    <div class="hero-actions" style="margin-top: 20px;">
                        <a class="button" href="${page.utility().lookupUrl()}">Verify address</a>
                        <a class="button ghost" href="${page.utility().notificationUrl()}">Open notice page</a>
                    </div>
                    @if (page.cost() != null)
                        <div class="note-panel" style="margin-top: 18px; background: rgba(255,255,255,0.08);">
                            <strong style="color: rgba(255,255,255,0.84);">Estimated replacement scope</strong>
                            <span class="price" style="color: #ffffff;">${page.cost().fullReplacementBand()}</span>
                            <p style="color: rgba(215, 226, 255, 0.84);">${page.cost().ownerTriggerSummary()}</p>
                        </div>
                    @endif
                </div>
            </div>
        </div>
    </section>

    <section class="section-block section-soft">
        <div class="section-grid two">
            <div>
                <div class="editorial-line">
                    <div class="kicker">Replacement decision logic</div>
                </div>
                <h2>Do not let a notice flatten the ownership boundary.</h2>
                <p class="lede">Do not overread this notice. The utility's exact category definitions, lookup record, and replacement path still control the real decision.</p>
                <div class="content-stack">
                    @for (var step : page.nextSteps())
                        <div class="content-card">
                            <h3>Action step</h3>
                            <p>${step}</p>
                        </div>
                    @endfor
                    @for (var caution : page.routeCautions())
                        <div class="content-card">
                            <h3>Do not overread this notice</h3>
                            <p>${caution}</p>
                        </div>
                    @endfor
                </div>
            </div>
            <div class="hero-media">
                <div class="hero-media__frame">
                    <img src="/media/boundary-diagram.jpg" alt="Diagram showing public and private responsibility boundary">
                </div>
            </div>
        </div>
    </section>

    <section class="section-block">
        <div class="editorial-line">
            <div class="kicker">Interim buying guides</div>
        </div>
        <h2>If the notice is real but the full fix is still pending.</h2>
        <div class="section-grid three">
            <div class="content-card tint-blue">
                <span class="label">Filter guide</span>
                <h3><a href="/guides/best-lead-reduction-filters-after-a-lead-notice">Best lead-reduction filters after a lead notice</a></h3>
                <p>Use this when the utility wants interim protection while the confirmed replacement or verification path is still moving.</p>
            </div>
            <div class="content-card tint-warm">
                <span class="label">Testing guide</span>
                <h3><a href="/guides/best-water-test-kits-after-a-lead-notice">Best water test kits after a lead notice</a></h3>
                <p>Use this when the question is whether a stronger sample would change the next local action.</p>
            </div>
            <div class="content-card tint-soft">
                <span class="label">Disclosure</span>
                <h3><a href="/affiliate-disclosure">How recommendations work</a></h3>
                <p>Interim product guidance should stay separate from notice interpretation and never soften the official wording.</p>
            </div>
        </div>
    </section>

    <section class="section-block">
        <div class="editorial-line">
            <div class="kicker">Utility snapshot</div>
        </div>
        <h2>Current utility counts and inventory status.</h2>
        @if (page.utility().lineCounts().hasAnyValues())
            <div class="stats-grid">
                @if (page.utility().lineCounts().known() != null)
                    <div class="stat-card danger">
                        <small>Known lead</small>
                        <strong>${page.utility().lineCounts().known()}</strong>
                        <p>Structured counts that support the notice interpretation for this utility.</p>
                    </div>
                @endif
                @if (page.utility().lineCounts().potential() != null)
                    <div class="stat-card warm">
                        <small>Potential lead</small>
                        <strong>${page.utility().lineCounts().potential()}</strong>
                        <p>Modeled or likely lead records that still need confirmation at the address level.</p>
                    </div>
                @endif
                @if (page.utility().lineCounts().unknown() != null)
                    <div class="stat-card">
                        <small>Unknown material</small>
                        <strong>${page.utility().lineCounts().unknown()}</strong>
                        <p>Records that still need verification, survey, or inspection before a clean call can be made.</p>
                    </div>
                @endif
                @if (page.utility().lineCounts().nonLead() != null)
                    <div class="stat-card soft">
                        <small>Verified non-lead</small>
                        <strong>${page.utility().lineCounts().nonLead()}</strong>
                        <p>Addresses already cleared or categorized as non-lead in the local dataset.</p>
                    </div>
                @endif
            </div>
        @else
            <div class="support-note">
                ${page.utility().inventorySummary() != null && !page.utility().inventorySummary().isBlank()
                        ? page.utility().inventorySummary()
                        : page.utility().inventoryStatus()}
            </div>
        @endif
    </section>

    <section class="section-block">
        <div class="editorial-line">
            <div class="kicker">Notice evidence block</div>
        </div>
        <h2>Route-level evidence behind this interpretation.</h2>
        <div class="evidence-list">
            @for (var source : page.sources())
                <div class="evidence-item">
                    <strong>${source.publisherName()}</strong>
                    <p>${source.claimSummary()}</p>
                    <p class="muted"><a href="${source.sourceUrl()}">${source.sourceUrl()}</a></p>
                </div>
            @endfor
        </div>
    </section>
</main>

<footer class="footer">
    <div class="footer__inner">
        <div>
            <div class="brand-mark">Lead Line Record</div>
            <p class="muted">${page.utility().utilityName()} notice interpretation for ${page.utility().city()}, ${page.utility().state().toUpperCase()}.</p>
        </div>
        <div class="footer__links">
            <a href="${page.utility().lookupUrl()}">Verify Address</a>
            <a href="${page.utility().notificationUrl()}">Notice Page</a>
            <a href="${page.utility().inventoryUrl()}">Inventory</a>
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

