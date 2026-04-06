@import owner.leadserviceline.pages.StateProgramsPageModel
@import owner.leadserviceline.pages.PageSeoModel
@param StateProgramsPageModel page
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
            <a class="active" href="/lead-service-line/${page.state()}/programs">States</a>
            <a href="/#guides">Guides</a>
        </nav>
        <div class="site-avatar"><span class="material-symbols-outlined">water_drop</span></div>
    </div>
</header>

<main class="page-shell">
    <section class="hero-band">
        <div class="hero-grid">
            <div class="hero-copy">
                <div class="kicker">State program tracker</div>
                <h1>${page.state().toUpperCase()} lead service line programs.</h1>
                <p>This page is for the reader whose first question is <em>what funding or utility-managed replacement path exists in this state?</em> Every entry still leads back to a local utility page.</p>
                <div class="hero-actions">
                    <a class="button" href="/lead-service-line/${page.state()}">Back to ${page.state().toUpperCase()} hub</a>
                    <a class="button secondary" href="/lookup">Run address lookup</a>
                </div>
                <div class="stats-grid">
                    <div class="stat-card">
                        <small>Verified programs</small>
                        <strong>${page.programs().size()}</strong>
                        <p>Support paths with live local evidence in the current state coverage set.</p>
                    </div>
                    <div class="stat-card soft">
                        <small>Covered utilities</small>
                        <strong>${page.utilityCount()}</strong>
                        <p>Utility pages currently linked to at least one replacement, reimbursement, or inspection-support path.</p>
                    </div>
                    <div class="stat-card warm">
                        <small>Pages live</small>
                        <strong>${page.routeCount()}</strong>
                        <p>This page helps you move from a statewide funding question into the right local utility record.</p>
                    </div>
                </div>
            </div>
            <div class="hero-media">
                <div class="hero-media__frame">
                    <img src="/media/pipes-hero.jpg" alt="Underground utility piping and replacement support context">
                </div>
                <div class="floating-note">
                    <strong>Read the coverage first</strong>
                    <p>Grant, reimbursement, loan, and utility-managed replacement should never be collapsed into the same claim. Coverage boundaries matter more than the headline label.</p>
                </div>
            </div>
        </div>
    </section>

    <section class="section-block">
        <div class="editorial-line">
            <div class="kicker">How to compare programs</div>
        </div>
        <h2>Read the funding mechanism before the headline.</h2>
        <div class="section-grid three">
            <div class="content-card tint-blue">
                <span class="label">Coverage split</span>
                <h3>Public-side and private-side rules can diverge.</h3>
                <p>A program may fully cover the public side while leaving private-side work to the owner, or it may only reimburse after a pre-approved process.</p>
            </div>
            <div class="content-card tint-warm">
                <span class="label">Owner trigger</span>
                <h3>Timing changes the outcome.</h3>
                <p>Some programs only work if the owner applies first, waits for a city project, uses an approved contractor, or coordinates during a utility-led replacement cycle.</p>
            </div>
            <div class="content-card tint-soft">
                <span class="label">Return path</span>
                <h3>Always route back to the utility page.</h3>
                <p>Once the funding path is clear, the utility overview, notification, and cost pages still control how the record should be interpreted for a real address.</p>
            </div>
        </div>
    </section>

    <section class="section-block section-soft">
        <div class="editorial-line">
            <div class="kicker">Programs in this state</div>
        </div>
        <h2>Current verified support paths in ${page.state().toUpperCase()}.</h2>
        <div class="content-stack">
            @for (var program : page.programs())
                <article class="content-card">
                    <span class="status-pill">${program.geography()}</span>
                    <h3>${program.programName()}</h3>
                    <p>${program.subsidyType()}</p>
                    <div class="resource-grid two" style="margin-top: 18px;">
                        <div class="checklist-item">
                            <strong>Public side</strong>
                            <p>${program.publicSideCovered()}</p>
                        </div>
                        <div class="checklist-item">
                            <strong>Private side</strong>
                            <p>${program.privateSideCovered()}</p>
                        </div>
                    </div>
                    <div class="resource-grid two" style="margin-top: 18px;">
                        <div class="content-card tint-soft">
                            <span class="label">Verification</span>
                            <p>${program.verificationStatus()} as of ${program.lastVerified().toString()}.</p>
                        </div>
                        <div class="content-card tint-blue">
                            <span class="label">Official program path</span>
                            <p><a href="${program.applicationUrl()}">${program.applicationUrl()}</a></p>
                        </div>
                    </div>
                    @if (!program.utilityLinks().isEmpty())
                        <div class="utility-nav">
                            @for (var utility : program.utilityLinks())
                                <a href="${utility.path()}">${utility.label()}</a>
                            @endfor
                        </div>
                    @endif
                </article>
            @endfor
        </div>
    </section>
</main>

<footer class="footer">
    <div class="footer__inner">
        <div>
            <div class="brand-mark">Lead Service Line Verdict</div>
            <p class="muted">${page.state().toUpperCase()} program tracker for local lead line replacement support.</p>
        </div>
        <div class="footer__links">
            <a href="/lead-service-line/${page.state()}">State page</a>
            <a href="/lookup">Lookup</a>
        </div>
    </div>
</footer>
</body>
</html>

