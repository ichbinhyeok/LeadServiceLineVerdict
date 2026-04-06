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

<section class="hero-band hero-band--dark">
    <div class="page-shell">
        <div class="hero-grid hero-grid--dark">
            <div class="hero-copy">
                <div class="status-pill ${page.currentRoute().indexable() ? "" : "warm"}">${page.currentRoute().indexable() ? "Indexed cost guide" : "Evidence-held cost guide"}</div>
                <h1>${page.heroTitle()}</h1>
                <p class="lede">${page.heroSummary()}</p>
                <div class="hero-actions">
                    @if (!page.programSummaries().isEmpty())
                        <a class="button" href="${page.navigation().stream().filter(candidate -> "Program".equals(candidate.label())).findFirst().map(link -> link.path()).orElse(page.utility().notificationUrl())}">Find a program</a>
                    @endif
                    <a class="button ghost" href="${page.utility().lookupUrl()}">Official lookup</a>
                </div>
                @if (page.cost() != null)
                    <div class="note-panel" style="margin-top: 24px; background: rgba(255,255,255,0.10);">
                        <strong style="color: rgba(255,255,255,0.84);">Methodology signal</strong>
                        <p style="color: rgba(215, 226, 255, 0.92);">${page.cost().methodologySummary()}</p>
                    </div>
                @endif
            </div>
            <div class="hero-media">
                <div class="hero-media__frame">
                    <img src="/media/pipes-hero.jpg" alt="Utility pipes and valves in a clean industrial room">
                </div>
            </div>
        </div>
    </div>
</section>

<main class="page-shell">
    <section class="section-block">
        <div class="editorial-line">
            <div class="kicker">Step-by-step guide</div>
        </div>
        <h2>How to use a local replacement estimate without over-trusting it.</h2>
        <div class="section-grid two">
            @for (var step : page.nextSteps())
                <div class="content-card">
                    <h3>Step</h3>
                    <p>${step}</p>
                </div>
            @endfor
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
    </section>

    <section class="section-block section-soft">
        <div class="section-grid two">
            <div>
                <div class="editorial-line">
                    <div class="kicker">Cost breakdown</div>
                </div>
                <h2>Public side and private side must stay separated.</h2>
                @if (page.cost() == null)
                    <div class="support-note">
                        No local cost record is loaded for this utility yet. Use the utility lookup and program route instead of guessing from a generic range.
                    </div>
                @else
                    <div class="content-stack">
                        @for (var item : page.costResponsibilities())
                            <div class="content-card">
                                <h3>${item.scope()}</h3>
                                <p><strong>${item.band()}</strong></p>
                                <p>${item.detail()}</p>
                            </div>
                        @endfor
                        <div class="content-card">
                            <h3>Housing and permit assumptions</h3>
                            <p>${page.cost().housingTypeAssumption()}</p>
                            <p>${page.cost().permitAssumption()}</p>
                            <p>${page.cost().restorationAssumption()}</p>
                        </div>
                    </div>
                @endif
            </div>
            <div>
                <div class="hero-media">
                    <div class="hero-media__frame">
                        <img src="/media/boundary-diagram.jpg" alt="Infrastructure boundary between public and private service line sections">
                    </div>
                </div>
                @if (page.cost() != null)
                    <div class="note-panel" style="margin-top: 18px;">
                        <strong>Owner payment trigger</strong>
                        <p>${page.cost().ownerTriggerSummary()}</p>
                    </div>
                @endif
                <div class="editorial-line" style="margin-top: 18px;">
                    <div class="kicker">Cost cautions</div>
                </div>
                <h2>Cost cautions</h2>
                <div class="content-stack" style="margin-top: 18px;">
                    @for (var caution : page.routeCautions())
                        <div class="content-card">
                            <h3>Cost caution</h3>
                            <p>${caution}</p>
                        </div>
                    @endfor
                </div>
            </div>
        </div>
    </section>

    <section class="section-block">
        <div class="editorial-line">
            <div class="kicker">Financial assistance</div>
        </div>
        <h2>Programs can offset the private-side burden, but only on local terms.</h2>
        @if (page.programSummaries().isEmpty())
            <div class="support-note">
                No verified replacement program is loaded for this utility yet, so this cost guide should stay tightly tied to the utility's own replacement notices.
            </div>
        @else
            <div class="section-grid three">
                @for (var program : page.programSummaries())
                    <div class="content-card">
                        <span class="status-pill">${program.subsidyType()}</span>
                        <h3 style="margin-top: 14px;">${program.programName()}</h3>
                        <p><strong>Public side:</strong> ${program.publicSideCovered()}</p>
                        <p><strong>Private side:</strong> ${program.privateSideCovered()}</p>
                        <p>${program.deadlineLabel()}</p>
                        <a class="button line" href="${program.applicationUrl()}" style="margin-top: 14px;">Open program</a>
                    </div>
                @endfor
            </div>
        @endif
    </section>

    <section class="section-block">
        <div class="editorial-line">
            <div class="kicker">Cost evidence block</div>
        </div>
        <h2>Route-level evidence behind the estimate bands.</h2>
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
            <p class="muted">${page.utility().utilityName()} replacement cost guide for ${page.utility().city()}, ${page.utility().state().toUpperCase()}.</p>
        </div>
        <div class="footer__links">
            <a href="${page.utility().lookupUrl()}">Official Lookup</a>
            @if (!page.programSummaries().isEmpty())
                <a href="${page.navigation().stream().filter(candidate -> "Program".equals(candidate.label())).findFirst().map(link -> link.path()).orElse(page.utility().notificationUrl())}">Programs</a>
            @endif
        </div>
    </div>
</footer>
</body>
</html>

