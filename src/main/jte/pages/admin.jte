@import owner.leadserviceline.pages.AdminPageModel
@import owner.leadserviceline.pages.PageSeoModel
@param AdminPageModel page
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
            <a class="active" href="/admin">Admin</a>
            @if (page.opsReviewEnabled())
                <a href="/ops/review">Ops Review</a>
            @endif
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
                <a class="active" href="/admin">Admin</a>
                @if (page.opsReviewEnabled())
                    <a href="/ops/review">Ops Review</a>
                @endif
            </nav>
        </details>
        <div class="site-avatar"><span class="material-symbols-outlined">shield</span></div>
    </div>
</header>

<main class="page-shell">
    <section class="hero-band hero-band--dark">
        <div class="page-shell">
            <div class="hero-grid hero-grid--dark">
                <div class="hero-copy">
                    <div class="kicker">Internal admin dashboard</div>
                    <h1>Recommendation click activity and internal review entry points.</h1>
                    <p>This page is intentionally narrow. It is here to confirm whether buying-guide clicks are happening at all, which products get the earliest traction, and whether the internal ops queue is reachable.</p>
                    <div class="support-banner" style="margin-top: 28px; background: rgba(255, 255, 255, 0.1); color: rgba(255, 255, 255, 0.9);">
                        <strong style="color: #ffffff;">Security note</strong>
                        <p style="color: rgba(215, 226, 255, 0.88);">This route uses HTTP Basic auth and should still sit behind environment-level secret overrides in production.</p>
                    </div>
                </div>
                <div class="hero-copy">
                    <div class="stats-grid">
                        @for (var metric : page.metrics())
                            <div class="stat-card soft">
                                <small>${metric.label()}</small>
                                <strong>${metric.value()}</strong>
                                <p>Current internal count from the local log snapshot.</p>
                            </div>
                        @endfor
                    </div>
                </div>
            </div>
        </div>
    </section>

    <section class="section-block">
        <div class="section-grid two">
            <div>
                <div class="editorial-line">
                    <div class="kicker">Logging status</div>
                </div>
                <h2>What is switched on right now.</h2>
                <div class="checklist-list" style="margin-top: 22px;">
                    <div class="checklist-item">
                        <strong>Recommendation click log</strong>
                        <p>@if (page.recommendationLogEnabled()) Enabled. @else Disabled. @endif This controls whether `/go/...` redirect clicks are written to the local JSONL log.</p>
                    </div>
                    <div class="checklist-item">
                        <strong>Lookup log</strong>
                        <p>@if (page.lookupLogEnabled()) Enabled. @else Disabled. @endif This controls whether privacy-safe lookup diagnostics are stored for internal review.</p>
                    </div>
                    <div class="checklist-item">
                        <strong>Ops review</strong>
                        <p>@if (page.opsReviewEnabled()) Enabled at <a href="/ops/review">/ops/review</a>. @else Disabled in runtime properties. @endif</p>
                    </div>
                </div>
            </div>
            <div class="aside-panel">
                <h2>Use this page for one question.</h2>
                <p>Are readers reaching the product layer yet? If the click log stays empty after indexing starts, the next job is CTA tuning, not adding more products.</p>
            </div>
        </div>
    </section>

    <section class="section-block section-soft">
        <div class="editorial-line">
            <div class="kicker">Recent recommendation clicks</div>
        </div>
        <h2>Latest outbound product activity.</h2>
        @if (page.recentClicks().isEmpty())
            <div class="support-banner">
                <strong>No click events yet</strong>
                <p>Once readers click the buying-guide buttons, the latest events will appear here.</p>
            </div>
        @else
            <div class="ops-grid">
                @for (var click : page.recentClicks())
                    <article class="content-card">
                        <span class="label">${click.timestampLabel()}</span>
                        <h3>${click.productName()}</h3>
                        <p><strong>Guide:</strong>
                            @if (click.guidePath() != null && !click.guidePath().isBlank())
                                <a href="${click.guidePath()}">${click.guideTitle()}</a>
                            @else
                                ${click.guideTitle()}
                            @endif
                        </p>
                        <p><strong>Source path:</strong> @if (click.sourcePath() != null && !click.sourcePath().isBlank()) ${click.sourcePath()} @else unknown @endif</p>
                        <p><strong>Destination domain:</strong> ${click.destinationDomain()}</p>
                    </article>
                @endfor
            </div>
        @endif
    </section>

    <section class="section-block">
        <div class="section-grid two">
            <div>
                <div class="editorial-line">
                    <div class="kicker">Slot performance</div>
                </div>
                <h2>Which CTA surfaces are getting action.</h2>
                @if (page.slotPerformance().isEmpty())
                    <div class="support-banner">
                        <strong>No slot data yet</strong>
                        <p>Slot-level impressions and clicks will appear here once pages start loading and readers hit recommendation CTAs.</p>
                    </div>
                @else
                    <div class="content-stack">
                        @for (var row : page.slotPerformance())
                            <div class="content-card">
                                <h3>${row.label()}</h3>
                                <p><strong>Impressions:</strong> ${row.impressions()}</p>
                                <p><strong>Clicks:</strong> ${row.clicks()}</p>
                                <p><strong>CTR:</strong> ${row.ctrLabel()}</p>
                            </div>
                        @endfor
                    </div>
                @endif
            </div>
            <div>
                <div class="editorial-line">
                    <div class="kicker">Page performance</div>
                </div>
                <h2>Which pages are generating recommendation activity.</h2>
                @if (page.pagePerformance().isEmpty())
                    <div class="support-banner">
                        <strong>No page data yet</strong>
                        <p>Page-level impressions and clicks will appear here once recommendation surfaces start loading.</p>
                    </div>
                @else
                    <div class="content-stack">
                        @for (var row : page.pagePerformance())
                            <div class="content-card">
                                <h3>${row.label()}</h3>
                                <p><strong>Impressions:</strong> ${row.impressions()}</p>
                                <p><strong>Clicks:</strong> ${row.clicks()}</p>
                                <p><strong>CTR:</strong> ${row.ctrLabel()}</p>
                            </div>
                        @endfor
                    </div>
                @endif
            </div>
        </div>
    </section>
</main>
</body>
</html>
