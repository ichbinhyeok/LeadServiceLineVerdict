document.addEventListener("DOMContentLoaded", () => {
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
});
