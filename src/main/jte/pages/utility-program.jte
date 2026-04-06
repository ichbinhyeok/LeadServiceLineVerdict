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
            <a class="active" href="${page.navigation().stream().filter(candidate -> !candidate.supportLayer()).findFirst().map(link -> link.path()).orElse(seo.canonicalUrl())}">Utility Record</a>
            <a href="/#guides">Guides</a>
        </nav>
        <div class="site-avatar"><span class="material-symbols-outlined">water_drop</span></div>
    </div>
</header>

<main class="page-shell">
    <section class="hero-band">
        <div class="hero-grid">
            <div class="hero-copy">
                <div class="status-pill">Verified programs</div>
                <h1>${page.heroTitle()}</h1>
                <p>${page.heroSummary()}</p>
                <div class="hero-actions">
                    <a class="button" href="${page.utility().lookupUrl()}">Official lookup</a>
                    @if (!page.programSummaries().isEmpty())
                        <a class="button secondary" href="${page.programSummaries().get(0).applicationUrl()}">Open official program info</a>
                    @else
                        <a class="button secondary" href="${page.utility().notificationUrl()}">Utility update page</a>
                    @endif
                </div>
                <div class="utility-nav">
                    @for (var link : page.navigation().stream().filter(candidate -> !candidate.supportLayer()).toList())
                        <a href="${link.path()}" class="${link.active() ? "active" : ""}">${link.label()}</a>
                    @endfor
                </div>
            </div>
            <div class="hero-media">
                <div class="hero-media__frame">
                    <img src="/media/inspection-tech.jpg" alt="Technician inspecting utility service line">
                </div>
                <div class="floating-note">
                    <strong>Verification rule</strong>
                    <p>Programs stay visible only when public-side and private-side coverage, application path, and verification status are locally documented.</p>
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
                <p>Program claims stay limited to locally verified public-side, private-side, and application rules.</p>
            </div>
        </div>
    </section>

    <section class="section-block">
        <div class="section-grid two">
            <div>
                <div class="editorial-line">
                    <div class="kicker">Verified replacement support</div>
                </div>
                <h2>Program detail should stay narrower than the marketing copy.</h2>
                @if (page.programSummaries().isEmpty())
                    <div class="support-note">
            No verified local replacement program is loaded yet. This route stays source-first and avoids promising funding or eligibility.
                    </div>
                @else
                    <div class="content-stack">
                        @for (var program : page.programSummaries())
                            <div class="content-card">
                                <span class="status-pill">${program.subsidyType()}</span>
                                <h3 style="margin-top: 14px;">${program.programName()}</h3>
                                <p><strong>Public side:</strong> ${program.publicSideCovered()}</p>
                                <p><strong>Private side:</strong> ${program.privateSideCovered()}</p>
                                <p><strong>Income rules:</strong> ${program.incomeRules()}</p>
                                <p><strong>Property rules:</strong> ${program.propertyRules()}</p>
                                <p><strong>Contractor rules:</strong> ${program.contractorRules()}</p>
                                <p><strong>Deadline:</strong> ${program.deadlineLabel()}</p>
                                <p><strong>Verification:</strong> ${program.verificationLabel()}</p>
                                <div class="hero-actions" style="margin-top: 16px;">
                                    <a class="button line" href="${program.applicationUrl()}">Program page</a>
                                </div>
                            </div>
                        @endfor
                    </div>
                @endif
            </div>
            <div>
                <div class="aside-panel">
                    <h2>Before you apply</h2>
                    <p>Screen the program against the actual utility record before you assume full replacement coverage or no-cost private-side work.</p>
                    <div class="content-stack" style="margin-top: 18px;">
                        @for (var fact : page.keyFacts())
                            <div class="card nested">
                                <h3>${fact.label()}</h3>
                                <p>${fact.value()}</p>
                                @if (fact.detail() != null && !fact.detail().isBlank())
                                    <p>${fact.detail()}</p>
                                @endif
                            </div>
                        @endfor
                    </div>
                </div>
            </div>
        </div>
    </section>

    <section class="section-block section-soft">
        <div class="section-grid two">
            <div>
                <div class="editorial-line">
                    <div class="kicker">Application sequence</div>
                </div>
                <h2>Use the utility record before the funding promise.</h2>
                <div class="content-stack">
                    @for (var step : page.nextSteps())
                        <div class="content-card">
                            <h3>Step</h3>
                            <p>${step}</p>
                        </div>
                    @endfor
                </div>
            </div>
            <div>
                <div class="editorial-line">
                    <div class="kicker">Coverage cautions</div>
                </div>
                <h2>Coverage cautions</h2>
                <div class="content-stack">
                    @for (var caution : page.routeCautions())
                        <div class="content-card">
                            <h3>Coverage caution</h3>
                            <p>${caution}</p>
                        </div>
                    @endfor
                </div>
            </div>
        </div>
    </section>

    <section class="section-block">
        <div class="editorial-line">
            <div class="kicker">Interim buying guides</div>
        </div>
        <h2>Useful while an application, inspection, or queue is still open.</h2>
        <div class="section-grid three">
            <div class="content-card tint-blue">
                <span class="label">Filter guide</span>
                <h3><a href="/guides/best-lead-reduction-filters-after-a-lead-notice">Best lead-reduction filters after a lead notice</a></h3>
                <p>Use this when the program path may take time and the reader still needs a practical interim drinking-water setup.</p>
            </div>
            <div class="content-card tint-warm">
                <span class="label">Testing guide</span>
                <h3><a href="/guides/best-water-test-kits-after-a-lead-notice">Best water test kits after a lead notice</a></h3>
                <p>Use this when a lab-backed result would sharpen the conversation with the utility, city, or contractor.</p>
            </div>
            <div class="content-card tint-soft">
                <span class="label">Disclosure</span>
                <h3><a href="/affiliate-disclosure">How recommendations work</a></h3>
                <p>Buying guides stay secondary to verified program coverage and should never inflate what the program actually pays for.</p>
            </div>
        </div>
    </section>

    <section class="section-block">
        <div class="editorial-line">
            <div class="kicker">Program evidence block</div>
        </div>
        <h2>Route-level evidence behind the local support paths.</h2>
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
            <p class="muted">${page.utility().utilityName()} replacement program guide for ${page.utility().city()}, ${page.utility().state().toUpperCase()}.</p>
        </div>
        <div class="footer__links">
            <a href="${page.utility().lookupUrl()}">Official Lookup</a>
            <a href="${page.utility().notificationUrl()}">Utility Updates</a>
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

