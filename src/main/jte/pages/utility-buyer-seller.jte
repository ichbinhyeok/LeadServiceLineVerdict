@import owner.leadserviceline.pages.UtilityPageModel
@import owner.leadserviceline.pages.PageSeoModel
@param UtilityPageModel page
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
                <div class="kicker">${page.utility().state().toUpperCase()} / ${page.utility().city()} / support route</div>
                <h1>${page.heroTitle()}</h1>
                <p>${page.heroSummary()}</p>
                <div class="utility-nav">
                    @for (var link : page.navigation())
                        <a href="${link.path()}" class="${link.active() ? "active" : ""}">${link.label()}</a>
                    @endfor
                </div>
                <div class="support-banner" style="margin-top: 24px;">
                    <strong>Transaction support only</strong>
                    <p>This page should help a transaction conversation after the utility overview, notice, program, and cost pages are understood. It should not replace them, and it should not read like a generic plumbing checklist.</p>
                </div>
            </div>
            <div class="hero-media">
                <div class="hero-media__frame">
                    <img src="/media/inspection-tech.jpg" alt="Inspection and property review context for buyer seller decisions">
                </div>
                <div class="floating-note">
                    <strong>Noindex boundary</strong>
                    <p>Buyer-seller guidance stays subordinate by design. The official utility record and local replacement path remain the primary evidence.</p>
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
                <p>Buyer-seller guidance is secondary by design and should stay anchored to the official utility record.</p>
            </div>
        </div>
    </section>

    <section class="section-block">
        <div class="section-grid two">
            <div>
                <div class="editorial-line">
                    <div class="kicker">Transaction order</div>
                </div>
                <h2>What the parties should anchor first.</h2>
                <div class="checklist-list" style="margin-top: 22px;">
                    @for (var step : page.nextSteps())
                        <div class="checklist-item">
                            <strong>Negotiation step</strong>
                            <p>${step}</p>
                        </div>
                    @endfor
                </div>
            </div>
            <div class="aside-panel">
                <h2>Pull the local record into the file before negotiating.</h2>
                <p>Escrow, seller credit, early replacement, and contractor timing all depend on whether the utility record says known lead, possible lead, unknown material, or already cleared.</p>
                <div class="hero-actions">
                    <a class="button" href="${page.utility().lookupUrl()}">Official lookup</a>
                    <a class="button ghost" href="${page.utility().inventoryUrl()}">Inventory record</a>
                    <a class="button ghost" href="${page.utility().notificationUrl()}">Notice page</a>
                </div>
            </div>
        </div>
    </section>

    <section class="section-block section-soft">
        <div class="section-grid two">
            <div>
                <div class="editorial-line">
                    <div class="kicker">Local facts that change the deal</div>
                </div>
                <h2>Utility-specific signals to surface in the negotiation.</h2>
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
                <div class="editorial-line">
                    <div class="kicker">Cost framing</div>
                </div>
                <h2>Use local responsibility splits, not a national average.</h2>
                @if (page.cost() != null)
                    <div class="checklist-list" style="margin-top: 22px;">
                        @for (var responsibility : page.costResponsibilities())
                            <div class="checklist-item">
                                <strong>${responsibility.scope()}</strong>
                                <p>${responsibility.band()}</p>
                                <p>${responsibility.detail()}</p>
                            </div>
                        @endfor
                    </div>
                @else
                    <div class="support-banner">
                        <strong>Cost route held back</strong>
                        <p>This utility does not currently have a strong enough local methodology to turn replacement cost into a public landing page. Keep the negotiation anchored to the utility record and any verified program rules.</p>
                    </div>
                @endif
            </div>
        </div>
    </section>

    @if (!page.programSummaries().isEmpty())
        <section class="section-block">
            <div class="editorial-line">
                <div class="kicker">Program leverage</div>
            </div>
            <h2>Replacement support that can change escrow or credit math.</h2>
            <div class="section-grid two">
                @for (var program : page.programSummaries())
                    <div class="content-card">
                        <span class="status-pill">${program.verificationLabel()}</span>
                        <h3>${program.programName()}</h3>
                        <p>${program.subsidyType()}</p>
                        <p><strong>Public side:</strong> ${program.publicSideCovered()}</p>
                        <p><strong>Private side:</strong> ${program.privateSideCovered()}</p>
                        @if (program.incomeRules() != null && !program.incomeRules().isBlank())
                            <p><strong>Income rules:</strong> ${program.incomeRules()}</p>
                        @endif
                        @if (program.propertyRules() != null && !program.propertyRules().isBlank())
                            <p><strong>Property rules:</strong> ${program.propertyRules()}</p>
                        @endif
                        @if (program.contractorRules() != null && !program.contractorRules().isBlank())
                            <p><strong>Contractor rules:</strong> ${program.contractorRules()}</p>
                        @endif
                        <p><a href="${program.applicationUrl()}">${program.applicationUrl()}</a></p>
                    </div>
                @endfor
            </div>
        </section>
    @endif

    <section class="section-block section-soft">
        <div class="editorial-line">
            <div class="kicker">Interim buying guides</div>
        </div>
        <h2>Useful when the deal keeps moving before the full replacement is done.</h2>
        <div class="section-grid three">
            <div class="content-card tint-blue">
                <span class="label">Filter guide</span>
                <h3><a href="/guides/best-lead-reduction-filters-after-a-lead-notice">Best lead-reduction filters after a lead notice</a></h3>
                <p>Useful for a short interim plan when buyer and seller still need a practical drinking-water setup during escrow or closing.</p>
            </div>
            <div class="content-card tint-warm">
                <span class="label">Testing guide</span>
                <h3><a href="/guides/best-water-test-kits-after-a-lead-notice">Best water test kits after a lead notice</a></h3>
                <p>Useful when a better sample would clarify negotiation language, interim protections, or post-work confirmation.</p>
            </div>
            <div class="content-card tint-soft">
                <span class="label">Disclosure</span>
                <h3><a href="/affiliate-disclosure">How recommendations work</a></h3>
                <p>Buying guides are support tools only and should never replace the local utility record in a transaction file.</p>
            </div>
        </div>
    </section>

    <section class="section-block">
        <div class="editorial-line">
            <div class="kicker">Route cautions</div>
        </div>
        <h2>What this page should not overclaim.</h2>
        <div class="section-grid two">
            @for (var caution : page.routeCautions())
                <div class="content-card tint-warm">
                    <p>${caution}</p>
                </div>
            @endfor
        </div>
    </section>
</main>

<footer class="footer">
    <div class="footer__inner">
        <div>
            <div class="brand-mark">Lead Line Record</div>
            <p class="muted">${page.utility().utilityName()} buyer-seller support route for ${page.utility().city()}, ${page.utility().state().toUpperCase()}.</p>
        </div>
        <div class="footer__links">
            <a href="${page.utility().lookupUrl()}">Official Lookup</a>
            <a href="${page.navigation().stream().filter(candidate -> !candidate.supportLayer()).findFirst().map(link -> link.path()).orElse(seo.canonicalUrl())}">Utility Record</a>
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

