@import owner.leadserviceline.pages.OpsReviewPageModel
@import owner.leadserviceline.pages.PageSeoModel
@param OpsReviewPageModel page
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
    <link rel="alternate icon" href="/favicon.ico?v=20260408-ico1" type="image/x-icon">
    <link rel="apple-touch-icon" sizes="180x180" href="/apple-touch-icon.png?v=20260408-icons1">
    <link rel="manifest" href="/site.webmanifest?v=20260408-icons1">
    <meta name="theme-color" content="#0b5fff">
</head>
<body>
<header class="site-header">
    <div class="site-header__inner">
        <a class="brand-mark" href="/">Lead Line Record</a>
        <nav class="site-nav">
            <a href="/">Home</a>
            <a href="/lookup">Lookup</a>
            <a class="active" href="/ops/review">Ops Review</a>
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
                <a class="active" href="/ops/review">Ops Review</a>
                <a href="/#guides">Guides</a>
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
                    <div class="kicker">Internal review queue</div>
                    <h1>Snapshot quality checks for the launch cohort.</h1>
                    <p>This page is internal-only. It is built from the normalized JSON snapshot and exists to surface stale records, unsupported cost routes, broken source paths, and resolver ambiguity without adding a runtime database.</p>
                    <div class="support-banner" style="margin-top: 28px; background: rgba(255, 255, 255, 0.1); color: rgba(255, 255, 255, 0.9);">
                        <strong style="color: #ffffff;">Boundary rule</strong>
                        <p style="color: rgba(215, 226, 255, 0.88);">Keep this route behind the ops token boundary. Do not surface raw input, raw address queries, or protected export endpoints anywhere in public navigation.</p>
                    </div>
                </div>
                <div class="hero-copy">
                    <div class="stats-grid">
                        @for (var metric : page.metrics())
                            <div class="stat-card soft">
                                <small>${metric.label()}</small>
                                <strong>${metric.value()}</strong>
                                <p>Current snapshot metric from the internal review build.</p>
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
                    <div class="kicker">Ops handling</div>
                </div>
                <h2>What this screen is for.</h2>
                <div class="checklist-list" style="margin-top: 22px;">
                    <div class="checklist-item">
                        <strong>Review queue</strong>
                        <p>Use the grouped issues below to decide which utility records, cost routes, or source bundles need the next evidence pass.</p>
                    </div>
                    <div class="checklist-item">
                        <strong>Export boundary</strong>
                        <p>Use <code>/ops/review/export</code> only with the matching <code>X-Ops-Token</code> header when JSON snapshot export is actually required.</p>
                    </div>
                    <div class="checklist-item">
                        <strong>Privacy rule</strong>
                        <p>Resolver diagnostics should remain bucketed and redacted. This page is for operational prioritization, not user-input replay.</p>
                    </div>
                </div>
            </div>
            <div class="aside-panel">
                <h2>Internal only.</h2>
                <p>The goal is not to make ops pretty for the public. The goal is to make broken evidence and stale records obvious enough that the next editorial pass is fast and defensible.</p>
            </div>
        </div>
    </section>

    <section class="section-block section-soft">
        <div class="editorial-line">
            <div class="kicker">Prioritized review groups</div>
        </div>
        <h2>Issues worth checking next.</h2>
        @if (page.groups().isEmpty())
            <div class="support-banner">
                <strong>No current groups</strong>
                <p>No review groups were generated from the latest snapshot.</p>
            </div>
        @else
            <div class="ops-grid">
                @for (var group : page.groups())
                    <article class="content-card">
                        <span class="badge severity-${group.severity()}">${group.entries().size()} items</span>
                        <h3>${group.title()}</h3>
                        <p>${group.summary()}</p>
                        <div class="ops-grid" style="margin-top: 18px;">
                            @for (var entry : group.entries())
                                <div class="ops-entry">
                                    <h4>
                                        @if (entry.localPath() != null && !entry.localPath().isBlank())
                                            <a href="${entry.localPath()}">${entry.label()}</a>
                                        @else
                                            ${entry.label()}
                                        @endif
                                    </h4>
                                    <p>${entry.detail()}</p>
                                    @if (entry.externalUrl() != null && !entry.externalUrl().isBlank())
                                        <p><strong>Official source:</strong> <a href="${entry.externalUrl()}">${entry.externalUrl()}</a></p>
                                    @endif
                                </div>
                            @endfor
                        </div>
                    </article>
                @endfor
            </div>
        @endif
    </section>
</main>
</body>
</html>

