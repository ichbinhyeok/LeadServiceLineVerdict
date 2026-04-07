const compactParams = (params) =>
  Object.fromEntries(
    Object.entries(params).filter(([, value]) => {
      if (value === undefined || value === null) {
        return false;
      }

      if (typeof value === "string") {
        return value.trim().length > 0;
      }

      return true;
    }),
  );

const trackGaEvent = (eventName, params = {}) => {
  if (typeof window.gtag !== "function" || !eventName) {
    return;
  }

  window.gtag("event", eventName, compactParams({
    transport_type: "beacon",
    ...params,
  }));
};

const readPageContext = () => {
  const body = document.body;
  return compactParams({
    page_type: body.dataset.gaPageType,
    page_title: document.title,
    page_path: window.location.pathname,
    utility_id: body.dataset.gaUtilityId,
    utility_slug: body.dataset.gaUtilitySlug,
    utility_name: body.dataset.gaUtilityName,
    utility_city: body.dataset.gaUtilityCity,
    utility_state: body.dataset.gaUtilityState,
    utility_section: body.dataset.gaUtilitySection,
    route_indexable: body.dataset.gaRouteIndexable,
    guide_slug: body.dataset.gaGuideSlug,
    guide_title: body.dataset.gaGuideTitle,
  });
};

const buildRecommendationParams = (element, pageContext) =>
  compactParams({
    ...pageContext,
    slot: element.dataset.gaSlot,
    recommendation_slug: element.dataset.gaRecommendationSlug,
    recommendation_name: element.dataset.gaRecommendationName,
    recommendation_category: element.dataset.gaRecommendationCategory,
    merchant_name: element.dataset.gaMerchantName,
    destination_label: element.dataset.gaDestinationLabel,
  });

const setupMobileNav = () => {
  const menus = Array.from(document.querySelectorAll("[data-mobile-nav]"));
  if (menus.length === 0) {
    return;
  }

  const closeAll = () => {
    menus.forEach((menu) => {
      menu.open = false;
    });
  };

  menus.forEach((menu) => {
    menu.querySelectorAll("a").forEach((link) => {
      link.addEventListener("click", () => {
        menu.open = false;
      });
    });
  });

  document.addEventListener("click", (event) => {
    menus.forEach((menu) => {
      if (menu.open && !menu.contains(event.target)) {
        menu.open = false;
      }
    });
  });

  document.addEventListener("keydown", (event) => {
    if (event.key === "Escape") {
      closeAll();
    }
  });

  const desktopQuery = window.matchMedia("(min-width: 721px)");
  const handleBreakpointChange = (event) => {
    if (event.matches) {
      closeAll();
    }
  };

  if (typeof desktopQuery.addEventListener === "function") {
    desktopQuery.addEventListener("change", handleBreakpointChange);
  } else if (typeof desktopQuery.addListener === "function") {
    desktopQuery.addListener(handleBreakpointChange);
  }
};

const setupGaTracking = () => {
  const pageContext = readPageContext();

  if (pageContext.page_type === "utility") {
    trackGaEvent("utility_page_view", pageContext);
  }

  document.querySelectorAll("[data-ga-click-event]").forEach((element) => {
    element.addEventListener("click", () => {
      const params = buildRecommendationParams(element, pageContext);
      trackGaEvent(element.dataset.gaClickEvent, params);

      // Future direct affiliate links can reuse the same hook with a second event name.
      if (element.dataset.gaSecondaryEvent) {
        trackGaEvent(element.dataset.gaSecondaryEvent, params);
      }
    });
  });

  const impressionTargets = Array.from(document.querySelectorAll("[data-ga-impression-event]"));
  if (impressionTargets.length === 0) {
    return;
  }

  const seenTargets = new WeakSet();
  const emitImpression = (element) => {
    if (seenTargets.has(element)) {
      return;
    }

    seenTargets.add(element);
    trackGaEvent(element.dataset.gaImpressionEvent, buildRecommendationParams(element, pageContext));
  };

  if (typeof window.IntersectionObserver !== "function") {
    impressionTargets.forEach(emitImpression);
    return;
  }

  const observer = new window.IntersectionObserver(
    (entries) => {
      entries.forEach((entry) => {
        if (!entry.isIntersecting) {
          return;
        }

        emitImpression(entry.target);
        observer.unobserve(entry.target);
      });
    },
    { threshold: 0.45 },
  );

  impressionTargets.forEach((element) => observer.observe(element));
};

document.addEventListener("DOMContentLoaded", () => {
  setupMobileNav();
  setupGaTracking();
});
