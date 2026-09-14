(() => {
  "use strict";
  if (document.getElementById("irisen-chaos-switch")) return;

  const root = document.documentElement;
  const storageKey = "irisen.chaosEffects";
  const reducedMotion = window.matchMedia("(prefers-reduced-motion: reduce)");
  const finePointer = window.matchMedia("(hover: hover) and (pointer: fine)");
  const controls = 'button, a[href], summary, [role="button"], input:not([type="hidden"]), select, textarea, [contenteditable="true"], .board-card[data-gid]';
  const feedbackSelector = '.error-box, .success-box, .feedback, .m-feedback-error, .m-feedback-success, [role="alert"], [role="status"]';
  const popupSelector = '[role="dialog"], .profile-popup, .m-profile-popup, dialog[open]';
  const cardSelector = '.feed-card, .quick-card, .board-card, .mini-link-card, .post-row, .comment-card, .board-directory-entry, .m-compact-post, .m-post-card, .m-comment, .result-item';
  const revealSelector = `${cardSelector}, ${feedbackSelector}, ${popupSelector}`;
  const excluded = '.crawler-snapshot, [hidden], [inert], [data-chaos="off"], .chaos-layer, .chaos-switch';
  const colors = ["#fa1687", "#00cde0", "#ffe659", "#9761ff"];
  const seen = new WeakSet();
  const observed = new Set();
  const animations = new Set();
  const animatedElements = new WeakMap();
  const pending = new Set();
  let enabled = true;
  let active = false;
  let frame = 0;
  let scanFrame = 0;
  let hoverCard = null;
  let tiltAnimation = null;
  let pointer = null;
  let lastTrail = 0;
  let lastBurst = 0;
  let lastInput = 0;

  try { enabled = localStorage.getItem(storageKey) !== "off"; } catch (_) { /* Storage may be unavailable. */ }

  const layer = document.createElement("div");
  layer.className = "chaos-layer";
  layer.setAttribute("aria-hidden", "true");
  layer.setAttribute("inert", "");
  const toggle = document.createElement("button");
  toggle.type = "button";
  toggle.id = "irisen-chaos-switch";
  toggle.className = "chaos-switch";
  document.body.append(layer, toggle);

  function eligible(element) {
    return element instanceof Element && !element.closest(excluded)
      && !element.closest(':disabled, [aria-disabled="true"]');
  }

  function visible(element) {
    if (!element.isConnected || !element.getClientRects().length) return false;
    const rect = element.getBoundingClientRect();
    return rect.bottom > 0 && rect.top < window.innerHeight && rect.right > 0 && rect.left < window.innerWidth;
  }

  function animate(element, keyframes, options = {}, replace = true) {
    if (!active || typeof element.animate !== "function") return null;
    if (replace) animatedElements.get(element)?.cancel();
    const animation = element.animate(keyframes, { duration: 480, easing: "cubic-bezier(.2,.8,.2,1)", ...options });
    animations.add(animation);
    if (replace) animatedElements.set(element, animation);
    const cleanup = () => {
      animations.delete(animation);
      if (animatedElements.get(element) === animation) animatedElements.delete(element);
    };
    animation.onfinish = () => { cleanup(); options.fill === "forwards" || animation.cancel(); };
    animation.oncancel = cleanup;
    return animation;
  }

  function decorate(className, x, y, keyframes, duration, color) {
    // Cap decoration even when the user rapidly clicks or moves the pointer.
    if (!active || layer.childElementCount >= 64) return;
    const node = document.createElement("span");
    node.className = className;
    node.style.left = `${x}px`;
    node.style.top = `${y}px`;
    if (color) node.style.setProperty("--particle-color", color);
    layer.append(node);
    const animation = animate(node, keyframes, { duration, easing: "cubic-bezier(.1,.7,.25,1)" });
    if (!animation) { node.remove(); return; }
    animation.finished.then(() => node.remove(), () => node.remove());
  }

  function burst(x, y, compact = false) {
    const now = performance.now();
    if (now - lastBurst < 90) return;
    lastBurst = now;
    decorate("chaos-ring", x, y, [
      { transform: "scale(.2) rotate(-30deg)", opacity: .85 },
      { transform: `scale(${compact ? 1.6 : 3.2}) rotate(80deg)`, opacity: 0 }
    ], 560);
    const count = compact ? 5 : 11;
    for (let i = 0; i < count; i++) {
      const angle = (Math.PI * 2 * i / count) + Math.random() * .35;
      const distance = (compact ? 25 : 45) + Math.random() * (compact ? 20 : 65);
      decorate("chaos-particle", x, y, [
        { transform: "translate(-50%, -50%) rotate(0deg) scale(1)", opacity: .95 },
        { transform: `translate(${Math.cos(angle) * distance}px, ${Math.sin(angle) * distance + 22}px) rotate(${180 + i * 47}deg) scale(.05)`, opacity: 0 }
      ], 450 + Math.random() * 300, colors[i % colors.length]);
    }
  }

  function kick(element) {
    if (element.matches('input, textarea, select, [contenteditable="true"]')) return;
    animate(element, [
      { scale: "1 1", rotate: "0deg" },
      { scale: ".88 1.13", rotate: "-3deg", offset: .2 },
      { scale: "1.09 .94", rotate: "2deg", offset: .5 },
      { scale: "1 1", rotate: "0deg" }
    ]);
  }

  function reveal(element, delay = 0) {
    if (!eligible(element) || !visible(element)) return;
    // Never transform a container holding an active editor or another popup.
    if (element.contains(document.activeElement)) return;
    if (element.matches(feedbackSelector)) {
      animate(element, [
        { translate: "-6px 0", boxShadow: "6px 0 #fa1687, -6px 0 #00cde0" },
        { translate: "4px 0", offset: .25 },
        { translate: "-2px 0", offset: .5 },
        { translate: "0 0", boxShadow: "0 0 transparent" }
      ], { duration: 500, delay });
    } else if (element.matches(popupSelector)) {
      animate(element, [
        { scale: ".82 1.05", rotate: "-4deg", opacity: .4 },
        { scale: "1.02 .98", rotate: "1deg", opacity: 1, offset: .65 },
        { scale: "1", rotate: "0deg", opacity: 1 }
      ], { duration: 560, delay });
    } else {
      animate(element, [
        { translate: "0 16px", rotate: "-1.5deg", opacity: .65 },
        { translate: "0 -3px", rotate: ".5deg", opacity: 1, offset: .7 },
        { translate: "0 0", rotate: "0deg", opacity: 1 }
      ], { duration: 430, delay });
    }
  }

  const intersection = typeof IntersectionObserver === "function" ? new IntersectionObserver((entries) => {
    let order = 0;
    for (const entry of entries) {
      if (!entry.isIntersecting) continue;
      intersection.unobserve(entry.target);
      observed.delete(entry.target);
      reveal(entry.target, Math.min(order++ * 24, 144));
    }
  }, { threshold: .08 }) : null;

  function register(element) {
    if (!eligible(element) || seen.has(element)) return;
    seen.add(element);
    if (element.matches(`${feedbackSelector}, ${popupSelector}`) || !intersection) {
      reveal(element);
    } else if (observed.size < 200) {
      observed.add(element);
      intersection.observe(element);
    }
  }

  function scan() {
    scanFrame = 0;
    if (!active) { pending.clear(); return; }
    for (const element of observed) {
      if (!element.isConnected) { intersection.unobserve(element); observed.delete(element); }
    }
    for (const node of pending) {
      if (!node.isConnected || node.closest(excluded)) continue;
      if (node.matches(revealSelector)) register(node);
      node.querySelectorAll(revealSelector).forEach(register);
    }
    pending.clear();
  }

  function queueScan(node) {
    if (!(node instanceof Element) || node.closest(excluded)) return;
    pending.add(node);
    if (!scanFrame) scanFrame = requestAnimationFrame(scan);
  }

  const observer = new MutationObserver((records) => {
    if (!active) return;
    for (const record of records) {
      if (record.target instanceof Element && record.target.closest(excluded)) continue;
      for (const node of record.addedNodes) queueScan(node);
      const feedback = record.target.parentElement?.closest(feedbackSelector) || record.target.closest?.(feedbackSelector);
      if (feedback) reveal(feedback);
    }
    // Also prune targets removed by SPA navigation, even if no elements were added.
    if (!scanFrame) scanFrame = requestAnimationFrame(scan);
  });

  function resetTilt() {
    tiltAnimation?.cancel();
    tiltAnimation = null;
    hoverCard = null;
    pointer = null;
  }

  function stop() {
    observer.disconnect();
    intersection?.disconnect();
    observed.clear();
    pending.clear();
    cancelAnimationFrame(frame);
    cancelAnimationFrame(scanFrame);
    frame = scanFrame = 0;
    resetTilt();
    for (const animation of animations) animation.cancel();
    animations.clear();
    layer.replaceChildren();
  }

  function updateMode() {
    stop();
    active = enabled && !reducedMotion.matches && !document.hidden;
    root.dataset.irisenFx = active ? "on" : enabled && reducedMotion.matches ? "quiet" : "off";
    toggle.textContent = !enabled ? "CHAOS OFF" : reducedMotion.matches ? "효과 축소" : "CHAOS ON";
    toggle.setAttribute("aria-pressed", String(enabled));
    toggle.setAttribute("aria-label", enabled ? "괴랄한 효과 끄기" : "괴랄한 효과 켜기");
    toggle.title = reducedMotion.matches ? "기기의 동작 줄이기 설정이 적용되어 있습니다." : "글리치 · 네온 파편 · 3D 반동 효과";
    if (active) {
      observer.observe(document.body, { childList: true, subtree: true, characterData: true });
      queueScan(document.body);
    }
  }

  toggle.addEventListener("click", () => {
    enabled = !enabled;
    try { localStorage.setItem(storageKey, enabled ? "on" : "off"); } catch (_) { /* Keep a session-only preference. */ }
    updateMode();
  });

  document.addEventListener("click", (event) => {
    if (!active || !(event.target instanceof Element)) return;
    const control = event.target.closest(controls);
    if (!eligible(control)) return;
    const rect = control.getBoundingClientRect();
    const x = event.detail === 0 ? rect.left + rect.width / 2 : event.clientX;
    const y = event.detail === 0 ? rect.top + rect.height / 2 : event.clientY;
    burst(x, y, control.matches("input, textarea, select"));
    kick(control);
  }, { capture: true, passive: true });

  document.addEventListener("input", (event) => {
    const element = event.target;
    if (!active || !eligible(element) || event.isComposing || performance.now() - lastInput < 180) return;
    lastInput = performance.now();
    // Only decorate the outline; never read, copy, or alter entered text.
    animate(element, [
      { boxShadow: "-6px 0 #00cde066, 6px 0 #fa168755" },
      { boxShadow: "2px 0 #00cde044, -2px 0 #fa168744", offset: .45 },
      { boxShadow: "0 0 transparent" }
    ], { duration: 280 });
  }, { passive: true });

  document.addEventListener("change", (event) => {
    if (!active || !eligible(event.target)) return;
    const rect = event.target.getBoundingClientRect();
    burst(rect.right - Math.min(rect.width / 2, 18), rect.top + Math.min(rect.height / 2, 20), true);
  }, { passive: true });

  document.addEventListener("pointermove", (event) => {
    if (!active || !finePointer.matches || event.pointerType !== "mouse" || !(event.target instanceof Element)) return;
    const control = event.target.closest(controls);
    const card = event.target.closest(cardSelector);
    const nextCard = eligible(card) && !card.querySelector('input, textarea, select, [contenteditable], [role="dialog"]') ? card : null;
    if (hoverCard !== nextCard) resetTilt();
    hoverCard = nextCard;
    pointer = { x: event.clientX, y: event.clientY, trail: eligible(control) };
    if (!frame) frame = requestAnimationFrame(() => {
      frame = 0;
      if (!pointer || !active) return;
      if (hoverCard?.isConnected) {
        const rect = hoverCard.getBoundingClientRect();
        const x = Math.max(-1, Math.min(1, (pointer.x - rect.left) / Math.max(rect.width, 1) * 2 - 1));
        const y = Math.max(-1, Math.min(1, (pointer.y - rect.top) / Math.max(rect.height, 1) * 2 - 1));
        const rotation = `${-y} ${x || .001} 0 5deg`;
        const previousRotation = getComputedStyle(hoverCard).rotate;
        tiltAnimation?.cancel();
        tiltAnimation = animate(hoverCard, [{ rotate: previousRotation }, { rotate: rotation }], { duration: 140, fill: "forwards" });
      }
      if (pointer.trail && performance.now() - lastTrail > 70) {
        lastTrail = performance.now();
        decorate("chaos-comet", pointer.x, pointer.y, [
          { transform: "rotate(-35deg) scale(1)", opacity: .7 },
          { transform: "translate(-15px, 18px) rotate(45deg) scale(.1)", opacity: 0 }
        ], 360);
      }
    });
  }, { passive: true });

  document.addEventListener("pointerout", (event) => {
    if (!event.relatedTarget || (hoverCard && !hoverCard.contains(event.relatedTarget))) resetTilt();
  }, { passive: true });
  window.addEventListener("scroll", resetTilt, { passive: true });
  window.addEventListener("blur", resetTilt);
  document.addEventListener("toggle", (event) => {
    if (active && eligible(event.target) && event.target.open) reveal(event.target);
  }, true);

  function routeEffect() {
    resetTilt();
    if (!active) return;
    decorate("chaos-route-streak", 0, 0, [
      { transform: "translateX(-45vw) skewX(-30deg)", opacity: .8 },
      { transform: "translateX(105vw) skewX(-30deg)", opacity: 0 }
    ], 650);
  }
  ["app:navigate", "mobile:navigate", "popstate"].forEach((name) => window.addEventListener(name, routeEffect));
  reducedMotion.addEventListener("change", updateMode);
  document.addEventListener("visibilitychange", updateMode);
  window.addEventListener("pagehide", stop);
  window.addEventListener("pageshow", updateMode);
  window.addEventListener("storage", (event) => {
    if (event.key !== storageKey) return;
    enabled = event.newValue !== "off";
    updateMode();
  });
  updateMode();
})();
