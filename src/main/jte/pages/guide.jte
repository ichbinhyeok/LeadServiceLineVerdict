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
                    @if (!page.recommendations().isEmpty())
                        <a class="button secondary" href="#recommendations">See recommendations</a>
                    @else
                        <a class="button secondary" href="/#guides">More guides</a>
                    @endif
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
                    @elseif (page.guide().slug().contains("test"))
                        <img src="/media/water-bottles.jpg" alt="${page.guide().title()}">
                    @elseif (page.guide().slug().contains("cost"))
                        <img src="/media/pipes-hero.jpg" alt="${page.guide().title()}">
                    @else
                        <img src="/media/service-line-diagram.jpg" alt="${page.guide().title()}">
                    @endif
                </div>
                <div class="floating-note">
                    <strong>How to use this guide</strong>
                    <p>These explainers give background context. The local utility record still decides what a reader should do next for a real address.</p>
                </div>
            </div>
        </div>
    </section>

    <section class="section-block">
        <div class="section-grid two">
            <div>
                <div class="editorial-line">
                    <div class="kicker">What to know first</div>
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
                    <div class="kicker">Then go local</div>
                </div>
                <h2>How to move from background context to the right utility page.</h2>
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

    @if (!page.recommendations().isEmpty())
        <section class="section-block" id="recommendations">
            <div class="editorial-line">
                <div class="kicker">Recommended products</div>
            </div>
            <h2>Products that match this exact decision lane.</h2>
            <p class="muted" style="margin-top: 14px;">These links currently open official product pages through a local redirect, so merchant targets can be swapped later without rewriting the guide.</p>
            @if ("best-lead-reduction-filters-after-a-lead-notice".equals(page.guide().slug()))
                <div class="support-banner" style="margin-top: 22px;">
                    <strong>Certification first</strong>
                    <p>EPA and CDC both push readers toward point-of-use filters that are explicitly certified for lead reduction. Cartridge discipline matters as much as the first purchase.</p>
                </div>
            @elseif ("best-water-test-kits-after-a-lead-notice".equals(page.guide().slug()))
                <div class="support-banner" style="margin-top: 22px;">
                    <strong>Testing is a decision tool</strong>
                    <p>Use a lab-backed kit only when the result would change the next step. Testing adds signal, but it does not replace the official utility lookup or classify pipe material by itself.</p>
                </div>
            @endif
            <div class="section-grid two" style="margin-top: 24px;">
                @for (var recommendation : page.recommendations())
                    <div class="content-card">
                        <img src="/events/recommendation-impression?slug=${recommendation.slug()}&pagePath=/guides/${page.guide().slug()}&slot=guide-card"
                             alt=""
                             width="1"
                             height="1"
                             loading="eager"
                             decoding="async"
                             style="position:absolute; width:1px; height:1px; opacity:0; pointer-events:none;">
                        <span class="label">${recommendation.badge()}</span>
                        <h3>${recommendation.name()}</h3>
                        <p><strong>Best for:</strong> ${recommendation.bestFor()}</p>
                        <p>${recommendation.whyItFits()}</p>
                        <p><strong>Watchout:</strong> ${recommendation.watchout()}</p>
                        <p class="muted">${recommendation.evidenceNote()}</p>
                        <div class="hero-actions" style="margin-top: 18px;">
                            <a class="button" href="/go/${recommendation.slug()}?slot=guide-card" rel="nofollow noopener" target="_blank">${recommendation.destinationLabel()}</a>
                        </div>
                        <p class="muted" style="margin-top: 14px;">${recommendation.merchantName()} - last verified ${recommendation.lastVerified().toString()}.</p>
                    </div>
                @endfor
            </div>
        </section>
    @endif

    <section class="section-block section-soft">
        <div class="editorial-line">
            <div class="kicker">Interim buying guides</div>
        </div>
        <h2>Use products only when the utility record says interim action is appropriate.</h2>
        <div class="section-grid three">
            @if (!"best-lead-reduction-filters-after-a-lead-notice".equals(page.guide().slug()))
                <div class="content-card tint-blue">
                    <span class="label">Filter guide</span>
                    <h3><a href="/guides/best-lead-reduction-filters-after-a-lead-notice">Best lead-reduction filters after a lead notice</a></h3>
                    <p>For readers comparing pitcher, faucet, countertop, and under-sink options while replacement is still pending.</p>
                </div>
            @endif
            @if (!"best-water-test-kits-after-a-lead-notice".equals(page.guide().slug()))
                <div class="content-card tint-warm">
                    <span class="label">Testing guide</span>
                    <h3><a href="/guides/best-water-test-kits-after-a-lead-notice">Best water test kits after a lead notice</a></h3>
                    <p>For readers whose next move depends on a stronger sampling workflow rather than a generic screening result.</p>
                </div>
            @endif
            <div class="content-card tint-soft">
                <span class="label">Disclosure</span>
                <h3><a href="/affiliate-disclosure">How recommendations work</a></h3>
                <p>Buying guides may later include affiliate links, but the utility record stays source-first and editorially separate.</p>
            </div>
        </div>
    </section>

    @if (!page.relatedGuides().isEmpty())
        <section class="section-block">
            <div class="editorial-line">
                <div class="kicker">Related guides</div>
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
            <div class="brand-mark">Lead Line Record</div>
            <p class="muted">${page.guide().title()} guide last verified ${page.guide().lastVerified().toString()}.</p>
        </div>
        <div class="footer__links">
            <a href="/">Home</a>
            <a href="/lookup">Lookup</a>
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
