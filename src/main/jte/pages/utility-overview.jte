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
                <div class="kicker">${page.utility().state().toUpperCase()} / ${page.utility().city()} / ${page.section().label()}</div>
                <h1>${page.heroTitle()}</h1>
                <p>${page.heroSummary()}</p>
                <div class="hero-actions">
                    <a class="button" href="${page.utility().lookupUrl()}">Official address lookup</a>
                    <a class="button secondary" href="${page.utility().inventoryUrl()}">Inventory record</a>
                </div>
                <div class="utility-nav">
                    @for (var link : page.navigation().stream().filter(candidate -> !candidate.supportLayer()).toList())
                        <a href="${link.path()}" class="${link.active() ? "active" : ""}">${link.label()}</a>
                    @endfor
                </div>
                @if (page.navigation().stream().anyMatch(candidate -> candidate.supportLayer()))
                    <div class="utility-nav" style="margin-top: 12px;">
                        @for (var link : page.navigation().stream().filter(candidate -> candidate.supportLayer()).toList())
                            <a href="${link.path()}" class="${link.active() ? "active" : ""}">${link.label()}</a>
                        @endfor
                    </div>
                @endif
            </div>
            <div class="hero-media">
                <div class="hero-media__frame">
                    <img src="/media/utility-meter.jpg" alt="Water meter and service line equipment">
                </div>
                <div class="floating-note">
                    <strong>Record verification</strong>
                    <p>${page.utility().verificationStatus()} record last verified ${page.utility().lastVerified().toString()}. Official lookup and inventory still control address-level truth.</p>
                </div>
            </div>
        </div>
    </section>

    <section class="section-block section-soft">
        <div class="editorial-line">
            <div class="kicker">City-wide inventory snapshot</div>
        </div>
        <h2>Where this utility stands now.</h2>
        @if (page.utility().lineCounts().hasAnyValues())
            <div class="stats-grid">
                @if (page.utility().lineCounts().known() != null)
                    <div class="stat-card danger">
                        <small>Known lead</small>
                        <strong>${page.utility().lineCounts().known()}</strong>
                        <p>Confirmed line records still carrying lead designation in the utility dataset.</p>
                    </div>
                @endif
                @if (page.utility().lineCounts().potential() != null)
                    <div class="stat-card warm">
                        <small>Potential lead</small>
                        <strong>${page.utility().lineCounts().potential()}</strong>
                        <p>Modeled or unverified records that still need a stronger field-level determination.</p>
                    </div>
                @endif
                @if (page.utility().lineCounts().unknown() != null)
                    <div class="stat-card">
                        <small>Unknown material</small>
                        <strong>${page.utility().lineCounts().unknown()}</strong>
                        <p>Addresses that still require a utility lookup, survey, inspection, or physical confirmation.</p>
                    </div>
                @endif
                @if (page.utility().lineCounts().nonLead() != null)
                    <div class="stat-card soft">
                        <small>Verified non-lead</small>
                        <strong>${page.utility().lineCounts().nonLead()}</strong>
                        <p>Records the utility currently treats as non-lead or already fully cleared from the program queue.</p>
                    </div>
                @endif
            </div>
        @else
            <div class="note-panel">
                <strong>Inventory narrative</strong>
                <p>${page.utility().inventorySummary() != null && !page.utility().inventorySummary().isBlank()
                        ? page.utility().inventorySummary()
                        : page.utility().inventoryStatus()}</p>
            </div>
        @endif
    </section>

    <section class="section-block">
        <div class="section-grid two">
            <div>
                <div class="editorial-line">
                    <div class="kicker">Administrative record</div>
                </div>
                <h2>Official pages that control the next step.</h2>
                <div class="content-stack">
                    <div class="content-card">
                        <h3>Inventory record</h3>
                        <p><a href="${page.utility().inventoryUrl()}">${page.utility().inventoryUrl()}</a></p>
                    </div>
                    <div class="content-card">
                        <h3>Address confirmation path</h3>
                        <p><a href="${page.utility().lookupUrl()}">${page.utility().lookupUrl()}</a></p>
                    </div>
                    <div class="content-card">
                        <h3>Notice or replacement updates</h3>
                        <p><a href="${page.utility().notificationUrl()}">${page.utility().notificationUrl()}</a></p>
                    </div>
                </div>
            </div>
            <div>
                <div class="editorial-line">
                    <div class="kicker">Decision frame</div>
                </div>
                <h2>Utility-specific facts that change what comes next.</h2>
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
        </div>
    </section>

    <section class="section-block section-soft">
        <div class="section-grid two">
            <div>
                <div class="editorial-line">
                    <div class="kicker">Decision order</div>
                </div>
                <h2>The anatomy of the local handoff.</h2>
                <div class="content-stack">
                    @for (var step : page.nextSteps())
                        <div class="content-card">
                            <h3>Next step</h3>
                            <p>${step}</p>
                        </div>
                    @endfor
                </div>
            </div>
            <div>
                <div class="aside-panel">
                    <h2>Replacement support stays secondary to the record.</h2>
                    <p>Program or cost guidance only becomes meaningful after the utility's own lookup, inventory status, and notice framing are clear for the address.</p>
                    <div class="content-stack" style="margin-top: 18px;">
                        @for (var caution : page.routeCautions())
                            <div class="card nested">
                                <p>${caution}</p>
                            </div>
                        @endfor
                    </div>
                </div>
            </div>
        </div>
    </section>

    <section class="section-block">
        <div class="editorial-line">
            <div class="kicker">Route-level evidence</div>
        </div>
        <h2>Source evidence behind this utility overview.</h2>
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
            <p class="muted">${page.utility().utilityName()} overview for ${page.utility().city()}, ${page.utility().state().toUpperCase()}.</p>
        </div>
        <div class="footer__links">
            <a href="${page.utility().lookupUrl()}">Official Lookup</a>
            <a href="${page.utility().inventoryUrl()}">Inventory</a>
            <a href="${page.utility().notificationUrl()}">Notice</a>
        </div>
    </div>
</footer>
</body>
</html>

