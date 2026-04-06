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
    @for (var jsonLd : seo.jsonLd())
        <script type="application/ld+json">${jsonLd}</script>
    @endfor
    <link rel="stylesheet" href="/app.css?v=20260405-qa3">
    <link rel="icon" href="/favicon.svg?v=20260405-qa3" type="image/svg+xml">
</head>
<body>
<header class="site-header">
    <div class="site-header__inner">
        <a class="brand-mark" href="/">Lead Service Line Verdict</a>
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
                    <strong>Certified authority</strong>
                    <p>Filters and testing are interim safety layers. They are not equivalent to a utility-confirmed full replacement path.</p>
                </div>
            </div>
        </div>
    </section>

    <section class="section-block section-soft">
        <div class="support-note">
            <p><strong>Trust constraint:</strong> filter guidance sits below inventory, notice, and replacement decisions. Use it when replacement is delayed or line status remains unresolved, not as a substitute for line replacement.</p>
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
            <div class="brand-mark">Lead Service Line Verdict</div>
            <p class="muted">${page.utility().utilityName()} filter and testing guide for ${page.utility().city()}, ${page.utility().state().toUpperCase()}.</p>
        </div>
        <div class="footer__links">
            <a href="${page.utility().lookupUrl()}">Official Lookup</a>
            <a href="${page.utility().inventoryUrl()}">Inventory</a>
        </div>
    </div>
</footer>
</body>
</html>

