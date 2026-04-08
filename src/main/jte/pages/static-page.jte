@import owner.leadserviceline.pages.PageSeoModel
@import owner.leadserviceline.pages.StaticPageModel
@param StaticPageModel page
@param PageSeoModel seo
@param String gaMeasurementId
<!doctype html>
<html lang="en">
<head>
    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>${seo.title()}</title>
    <meta name="description" content="${seo.description()}">
    <link rel="canonical" href="${seo.canonicalUrl()}">
    <meta name="robots" content="${seo.robots()}">
    @if (gaMeasurementId != null && !gaMeasurementId.isBlank())
        <meta name="ga-measurement-id" content="${gaMeasurementId}">
        <script async src="https://www.googletagmanager.com/gtag/js?id=${gaMeasurementId}"></script>
        <script>
            const gaMeasurementId = document.querySelector('meta[name="ga-measurement-id"]').content;
            window.dataLayer = window.dataLayer || [];
            function gtag(){dataLayer.push(arguments);}
            gtag('js', new Date());
            gtag('config', gaMeasurementId);
        </script>
    @endif
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
            <a href="/#states">States</a>
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
                <a href="/#states">States</a>
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
                <div class="kicker">${page.eyebrow()}</div>
                <h1>${page.heroTitle()}</h1>
                <p>${page.heroSummary()}</p>
                @if (page.contactEmail() != null && !page.contactEmail().isBlank())
                    <div class="hero-actions">
                        <a class="button" href="mailto:${page.contactEmail()}">${page.contactEmail()}</a>
                    </div>
                @endif
            </div>
            <div class="hero-media">
                <div class="hero-media__frame">
                    <img src="/media/pipes-hero.jpg" alt="Lead service line and utility infrastructure">
                </div>
                <div class="floating-note">
                    <strong>Trust surface</strong>
                    <p>Use these pages to understand how the site is sourced, reviewed, and scoped before relying on a local decision page.</p>
                </div>
            </div>
        </div>
    </section>

    <section class="section-block">
        <div class="section-grid two">
            @for (var section : page.sections())
                <div class="content-card">
                    <h2>${section.heading()}</h2>
                    @for (var paragraph : section.paragraphs())
                        <p>${paragraph}</p>
                    @endfor
                </div>
            @endfor
        </div>
    </section>
</main>

<footer class="footer">
    <div class="footer__inner">
        <div>
            <div class="brand-mark">Lead Line Record</div>
            <p class="muted">Utility-by-utility guidance on lead line notices, replacement programs, and who may pay for replacement.</p>
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
