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
                <div class="status-pill warm">Interim protection</div>
                <h1>${page.heroTitle()}</h1>
                <p>${page.heroSummary()}</p>
                <div class="hero-actions">
                    <a class="button" href="${page.utility().lookupUrl()}">Official lookup</a>
                    <a class="button secondary" href="${page.utility().inventoryUrl()}">Inventory record</a>
                </div>
                <div class="utility-nav">
                    @for (var link : page.navigation())
                        <a href="${link.path()}" class="${link.active() ? "active" : ""}">${link.label()}</a>
                    @endfor
                </div>
            </div>
            <div class="hero-media">
                <div class="hero-media__frame">
                    <img src="/media/kitchen-faucet.jpg" alt="Modern kitchen faucet with running water">
                </div>
                <div class="floating-note">
                    <strong>Interim only</strong>
                    <p>Filters and testing are interim safety layers. They are not equivalent to a utility-confirmed full replacement path.</p>
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
                <p>This route is an interim safety layer, not a replacement for utility-confirmed replacement planning.</p>
            </div>
        </div>
    </section>

    <section class="section-block section-soft">
        <div class="support-note">
            <p><strong>Use this only when replacement is not immediate:</strong> filter guidance sits below inventory, notice, and replacement decisions. It is not a substitute for line replacement.</p>
        </div>
    </section>

    <section class="section-block">
        <div class="editorial-line">
            <div class="kicker">Quick picks</div>
        </div>
        <h2>Direct interim options for this utility page.</h2>
        <div class="section-grid three">
            <div class="content-card tint-blue">
                <img src="/events/recommendation-impression?slug=pur-plus-faucet-system-vertical&pagePath=${page.currentRoute().path()}&slot=utility-filter-testing-faucet"
                     alt=""
                     width="1"
                     height="1"
                     loading="eager"
                     decoding="async"
                     style="position:absolute; width:1px; height:1px; opacity:0; pointer-events:none;">
                <span class="label">Fastest setup</span>
                <h3>PUR PLUS Faucet Filtration System</h3>
                <p>Best when the household wants filtered drinking water on demand without waiting on a pitcher refill.</p>
                <a class="button" href="/go/pur-plus-faucet-system-vertical?slot=utility-filter-testing-faucet" rel="nofollow noopener" target="_blank">Open quick-pick filter</a>
            </div>
            <div class="content-card tint-soft">
                <img src="/events/recommendation-impression?slug=zerowater-10-cup-pitcher&pagePath=${page.currentRoute().path()}&slot=utility-filter-testing-pitcher"
                     alt=""
                     width="1"
                     height="1"
                     loading="eager"
                     decoding="async"
                     style="position:absolute; width:1px; height:1px; opacity:0; pointer-events:none;">
                <span class="label">Pitcher option</span>
                <h3>ZeroWater 10 Cup Pitcher</h3>
                <p>Best when the household prefers a portable pitcher and can stay disciplined on more active filter changes.</p>
                <a class="button" href="/go/zerowater-10-cup-pitcher?slot=utility-filter-testing-pitcher" rel="nofollow noopener" target="_blank">Open quick-pick pitcher</a>
            </div>
            <div class="content-card tint-warm">
                <img src="/events/recommendation-impression?slug=tap-score-lead-and-copper&pagePath=${page.currentRoute().path()}&slot=utility-filter-testing-test"
                     alt=""
                     width="1"
                     height="1"
                     loading="eager"
                     decoding="async"
                     style="position:absolute; width:1px; height:1px; opacity:0; pointer-events:none;">
                <span class="label">Targeted lab test</span>
                <h3>Tap Score Lead and Copper Water Test</h3>
                <p>Best when a narrower lab-backed result would change the next step for this utility-specific situation.</p>
                <a class="button" href="/go/tap-score-lead-and-copper?slot=utility-filter-testing-test" rel="nofollow noopener" target="_blank">Open quick-pick test</a>
            </div>
        </div>
    </section>

    <section class="section-block">
        <div class="editorial-line">
            <div class="kicker">Buying guides</div>
        </div>
        <h2>Use dedicated product guides when you are ready to compare actual options.</h2>
        <div class="section-grid three">
            <div class="content-card tint-blue">
                <span class="label">Filter guide</span>
                <h3><a href="/guides/best-lead-reduction-filters-after-a-lead-notice">Best lead-reduction filters after a lead notice</a></h3>
                <p>Use this to compare filter formats, cartridge burden, and household fit once the need for interim protection is clear.</p>
            </div>
            <div class="content-card tint-warm">
                <span class="label">Testing guide</span>
                <h3><a href="/guides/best-water-test-kits-after-a-lead-notice">Best water test kits after a lead notice</a></h3>
                <p>Use this to compare test workflows when the next decision depends on a stronger sample, not a guess.</p>
            </div>
            <div class="content-card tint-soft">
                <span class="label">Disclosure</span>
                <h3><a href="/affiliate-disclosure">How recommendations work</a></h3>
                <p>Buying guides may later include affiliate links, but they remain editorially separate from the utility record.</p>
            </div>
        </div>
    </section>

    <section class="section-block">
        <div class="section-grid two">
            <div>
                <div class="editorial-line">
                    <div class="kicker">Certified filter selection</div>
                </div>
                <h2>Use local utility guidance, then narrow to certified interim protection.</h2>
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
                <div class="content-card">
                    <h3>Maintenance tips</h3>
                    <div class="content-stack" style="margin-top: 18px;">
                        @for (var step : page.nextSteps())
                            <div class="card nested">
                                <p>${step}</p>
                            </div>
                        @endfor
                    </div>
                </div>
            </div>
        </div>
    </section>

    <section class="section-block" style="background: linear-gradient(135deg, rgba(0,52,111,0.96) 0%, rgba(0,74,153,0.92) 100%); color: #ffffff;">
        <div class="section-grid two">
            <div>
                <div class="kicker" style="color: rgba(255,255,255,0.84);">Water testing steps</div>
                <h2 style="color: #ffffff;">Testing should change the next action, not distract from the utility record.</h2>
                <div class="content-stack">
                    @for (var caution : page.routeCautions())
                        <div class="card nested" style="background: rgba(255,255,255,0.08);">
                            <p style="color: rgba(255,255,255,0.88);">${caution}</p>
                        </div>
                    @endfor
                    <div class="card nested" style="background: rgba(255,255,255,0.08);">
                        <p style="color: rgba(255,255,255,0.88);">Official inventory page: <a href="${page.utility().inventoryUrl()}">${page.utility().inventoryUrl()}</a></p>
                        <p style="color: rgba(255,255,255,0.88);">Official lookup: <a href="${page.utility().lookupUrl()}">${page.utility().lookupUrl()}</a></p>
                    </div>
                </div>
            </div>
            <div class="hero-media">
                <div class="hero-media__frame">
                    <img src="/media/water-bottles.jpg" alt="Water testing bottles in a laboratory setting">
                </div>
            </div>
        </div>
    </section>

    <section class="section-block">
        <div class="editorial-line">
            <div class="kicker">Verified official resources</div>
        </div>
        <h2>Use the source stack that supports this interim route.</h2>
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
            <p class="muted">${page.utility().utilityName()} filter and testing guide for ${page.utility().city()}, ${page.utility().state().toUpperCase()}.</p>
        </div>
        <div class="footer__links">
            <a href="${page.utility().lookupUrl()}">Official Lookup</a>
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

