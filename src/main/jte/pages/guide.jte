@import owner.leadserviceline.pages.GuidePageModel
@import owner.leadserviceline.pages.PageSeoModel
@param GuidePageModel page
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
            <a href="/#states">States</a>
            <a class="active" href="/guides/${page.guide().slug()}">Guides</a>
        </nav>
        <div class="site-avatar"><span class="material-symbols-outlined">water_drop</span></div>
    </div>
</header>

<main class="page-shell">
    <section class="hero-band">
        <div class="hero-grid">
            <div class="hero-copy">
                <div class="kicker">${page.guide().eyebrow()}</div>
                <h1>${page.guide().title()}</h1>
                <p>${page.guide().heroSummary()}</p>
                <div class="hero-actions">
                    <a class="button" href="/lookup">Find your utility</a>
                    <a class="button secondary" href="/#guides">More guides</a>
                </div>
                <div class="support-banner" style="margin-top: 28px;">
                    <strong>Verdict</strong>
                    <p>${page.guide().verdict()}</p>
                </div>
            </div>
            <div class="hero-media">
                <div class="hero-media__frame">
                    @if (page.guide().slug().contains("buyer-seller"))
                        <img src="/media/inspection-tech.jpg" alt="${page.guide().title()}">
                    @elseif (page.guide().slug().contains("filter"))
                        <img src="/media/kitchen-faucet.jpg" alt="${page.guide().title()}">
                    @elseif (page.guide().slug().contains("cost"))
                        <img src="/media/pipes-hero.jpg" alt="${page.guide().title()}">
                    @else
                        <img src="/media/service-line-diagram.jpg" alt="${page.guide().title()}">
                    @endif
                </div>
                <div class="floating-note">
                    <strong>Guide boundary</strong>
                    <p>These evergreen explainers are support material. The local utility record still decides what a reader should do next for a real address.</p>
                </div>
            </div>
        </div>
    </section>

    <section class="section-block">
        <div class="section-grid two">
            <div>
                <div class="editorial-line">
                    <div class="kicker">Core reading</div>
                </div>
                <h2>What to keep straight before you move local.</h2>
                <div class="checklist-list" style="margin-top: 22px;">
                    @for (var point : page.guide().keyPoints())
                        <div class="checklist-item">
                            <strong>Key point</strong>
                            <p>${point}</p>
                        </div>
                    @endfor
                </div>
            </div>
            <div>
                <div class="editorial-line">
                    <div class="kicker">Local handoff</div>
                </div>
                <h2>How to use this guide without drifting generic.</h2>
                <div class="checklist-list" style="margin-top: 22px;">
                    @for (var step : page.guide().nextSteps())
                        <div class="checklist-item">
                            <strong>Next step</strong>
                            <p>${step}</p>
                        </div>
                    @endfor
                </div>
            </div>
        </div>
    </section>

    <section class="section-block section-soft">
        <div class="editorial-line">
            <div class="kicker">Common questions</div>
        </div>
        <h2>Questions that usually appear before a utility lookup.</h2>
        <div class="section-grid two">
            @for (var faq : page.guide().faqs())
                <div class="content-card">
                    <h3>${faq.question()}</h3>
                    <p>${faq.answer()}</p>
                </div>
            @endfor
        </div>
        <p class="muted" style="margin-top: 18px;">Last verified ${page.guide().lastVerified().toString()}.</p>
    </section>

    @if (!page.relatedGuides().isEmpty())
        <section class="section-block">
            <div class="editorial-line">
                <div class="kicker">Read next</div>
            </div>
            <h2>Stay in the same narrow decision lane.</h2>
            <div class="section-grid three">
                @for (var guide : page.relatedGuides())
                    <div class="content-card">
                        <h3><a href="/guides/${guide.slug()}">${guide.title()}</a></h3>
                        <p>${guide.heroSummary()}</p>
                    </div>
                @endfor
            </div>
        </section>
    @endif
</main>

<footer class="footer">
    <div class="footer__inner">
        <div>
            <div class="brand-mark">Lead Service Line Verdict</div>
            <p class="muted">${page.guide().title()} guide last verified ${page.guide().lastVerified().toString()}.</p>
        </div>
        <div class="footer__links">
            <a href="/lookup">Lookup</a>
            <a href="/#guides">Guides</a>
        </div>
    </div>
</footer>
</body>
</html>

