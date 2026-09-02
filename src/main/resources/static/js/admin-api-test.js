(function () {
  const unsafeMethods = new Set(["POST", "PUT", "PATCH", "DELETE"]);
  const forbiddenHeaders = new Set([
    "cookie", "set-cookie", "host", "origin", "referer",
    "content-length", "connection", "transfer-encoding"
  ]);
  const history = [];

  const form = document.getElementById("apiRequestForm");
  const methodInput = document.getElementById("requestMethod");
  const pathInput = document.getElementById("requestPath");
  const bodyTypeInput = document.getElementById("requestBodyType");
  const bodyInput = document.getElementById("requestBody");
  const headersInput = document.getElementById("requestHeaders");
  const jsonBodyField = document.getElementById("jsonBodyField");
  const multipartFields = document.getElementById("multipartFields");
  const fileInput = document.getElementById("requestFile");
  const gallIdInput = document.getElementById("multipartGallId");
  const mutationConfirm = document.getElementById("mutationConfirm");
  const mutationConfirmLabel = document.getElementById("mutationConfirmLabel");
  const sendButton = document.getElementById("sendRequestButton");
  const feedback = document.getElementById("requestFeedback");
  const responseMeta = document.getElementById("responseMeta");
  const responseOutput = document.getElementById("responseOutput");
  const historyContainer = document.getElementById("requestHistory");

  function setFeedback(message) {
    feedback.textContent = message || "";
    feedback.className = message ? "feedback visible error" : "feedback";
  }

  function normalizedPath(rawPath) {
    const value = String(rawPath || "").trim();
    if (!value) {
      throw new Error("API 경로를 입력하세요.");
    }
    let url;
    try {
      url = new URL(value, window.location.origin);
    } catch (error) {
      throw new Error("올바른 API 경로가 아닙니다.");
    }
    if (url.origin !== window.location.origin || !url.pathname.startsWith("/api/")) {
      throw new Error("현재 호스트의 /api/** 경로만 호출할 수 있습니다.");
    }
    return `${url.pathname}${url.search}`;
  }

  function parseJsonObject(rawValue, label) {
    let parsed;
    try {
      parsed = JSON.parse(String(rawValue || "{}"));
    } catch (error) {
      throw new Error(`${label} 형식이 올바르지 않습니다.`);
    }
    if (!parsed || Array.isArray(parsed) || typeof parsed !== "object") {
      throw new Error(`${label}은 JSON 객체여야 합니다.`);
    }
    return parsed;
  }

  function requestHeaders(bodyType) {
    const source = parseJsonObject(headersInput.value, "추가 헤더 JSON");
    const headers = new Headers();
    Object.entries(source).forEach(([name, value]) => {
      const normalizedName = String(name).trim().toLowerCase();
      if (!normalizedName || forbiddenHeaders.has(normalizedName)) {
        return;
      }
      headers.set(name, String(value));
    });
    if (bodyType === "multipart") {
      headers.delete("Content-Type");
    } else if (bodyType === "json") {
      headers.set("Content-Type", "application/json");
    }
    return headers;
  }

  function requestBody(method, bodyType) {
    if (!unsafeMethods.has(method)) {
      return undefined;
    }
    if (bodyType === "multipart") {
      if (!fileInput.files || !fileInput.files[0]) {
        throw new Error("업로드할 이미지 파일을 선택하세요.");
      }
      const formData = new FormData();
      formData.append("file", fileInput.files[0]);
      const gallId = gallIdInput.value.trim();
      if (gallId) {
        formData.append("gallId", gallId);
      }
      return formData;
    }
    const payload = bodyType === "json" ? parseJsonObject(bodyInput.value, "요청 본문") : {};
    return JSON.stringify(payload);
  }

  function updateFormState() {
    const method = methodInput.value.toUpperCase();
    if (!unsafeMethods.has(method) && bodyTypeInput.value !== "none") {
      bodyTypeInput.value = "none";
    }
    const bodyType = bodyTypeInput.value;
    jsonBodyField.hidden = bodyType !== "json";
    multipartFields.hidden = bodyType !== "multipart";
    mutationConfirmLabel.hidden = !unsafeMethods.has(method);
    if (!unsafeMethods.has(method)) {
      mutationConfirm.checked = false;
    }
  }

  function setMeta(items) {
    responseMeta.replaceChildren();
    items.forEach((item) => {
      const badge = document.createElement("span");
      badge.textContent = item;
      responseMeta.appendChild(badge);
    });
  }

  function responseText(rawText, contentType) {
    if (!rawText) {
      return "(빈 응답)";
    }
    if (String(contentType || "").includes("json")) {
      try {
        return JSON.stringify(JSON.parse(rawText), null, 2);
      } catch (error) {
        return rawText;
      }
    }
    return rawText;
  }

  function renderHistory() {
    historyContainer.replaceChildren();
    if (history.length === 0) {
      const empty = document.createElement("p");
      empty.className = "empty-history";
      empty.textContent = "아직 실행한 요청이 없습니다.";
      historyContainer.appendChild(empty);
      return;
    }
    history.forEach((item, index) => {
      const button = document.createElement("button");
      button.type = "button";
      button.className = "history-button";
      button.dataset.historyIndex = String(index);

      const method = document.createElement("span");
      method.className = "history-method";
      method.textContent = item.method;
      const path = document.createElement("span");
      path.className = "history-path";
      path.textContent = item.path;
      const status = document.createElement("span");
      status.className = "history-status";
      status.textContent = String(item.status);

      button.append(method, path, status);
      historyContainer.appendChild(button);
    });
  }

  function rememberRequest(item) {
    history.unshift(item);
    if (history.length > 10) {
      history.length = 10;
    }
    renderHistory();
  }

  async function executeRequest() {
    setFeedback("");
    const method = methodInput.value.toUpperCase();
    if (unsafeMethods.has(method) && !mutationConfirm.checked) {
      setFeedback("데이터 변경 가능성 확인란을 선택해야 실행할 수 있습니다.");
      return;
    }

    let path;
    let bodyType;
    let options;
    try {
      path = normalizedPath(pathInput.value);
      bodyType = bodyTypeInput.value;
      options = {
        method,
        credentials: "same-origin",
        headers: requestHeaders(bodyType)
      };
      const body = requestBody(method, bodyType);
      if (body !== undefined) {
        options.body = body;
      }
    } catch (error) {
      setFeedback(error.message);
      return;
    }

    sendButton.disabled = true;
    sendButton.textContent = "요청 중…";
    responseOutput.textContent = `${method} ${path}\n요청을 보내는 중입니다.`;
    setMeta(["요청 중", method]);
    const startedAt = performance.now();

    try {
      const response = await fetch(path, options);
      const elapsed = Math.round(performance.now() - startedAt);
      const contentType = response.headers.get("Content-Type") || "";
      const rawText = await response.text();
      responseOutput.textContent = responseText(rawText, contentType);
      setMeta([
        `${response.status} ${response.statusText || ""}`.trim(),
        `${elapsed} ms`,
        contentType || "content-type 없음",
        new Date().toLocaleTimeString()
      ]);
      rememberRequest({ method, path, status: response.status, bodyType, elapsed });
      if (!response.ok) {
        setFeedback(`서버가 ${response.status} 상태로 응답했습니다.`);
      }
    } catch (error) {
      const elapsed = Math.round(performance.now() - startedAt);
      responseOutput.textContent = JSON.stringify({ error: error.message }, null, 2);
      setMeta(["네트워크 오류", `${elapsed} ms`, new Date().toLocaleTimeString()]);
      rememberRequest({ method, path, status: "ERR", bodyType, elapsed });
      setFeedback("서버 요청을 완료하지 못했습니다.");
    } finally {
      sendButton.disabled = false;
      sendButton.textContent = "요청 실행";
    }
  }

  function applyPreset(button) {
    const method = String(button.dataset.method || "GET").toUpperCase();
    methodInput.value = method;
    pathInput.value = button.dataset.path || "/api/check-login";
    bodyTypeInput.value = button.dataset.bodyType || "none";
    mutationConfirm.checked = false;
    setFeedback("");
    updateFormState();
    if (!unsafeMethods.has(method)) {
      executeRequest();
    } else {
      pathInput.focus();
    }
  }

  document.querySelectorAll(".preset-button").forEach((button) => {
    button.addEventListener("click", () => applyPreset(button));
  });

  form.addEventListener("submit", (event) => {
    event.preventDefault();
    executeRequest();
  });

  methodInput.addEventListener("change", updateFormState);
  bodyTypeInput.addEventListener("change", updateFormState);
  historyContainer.addEventListener("click", (event) => {
    const button = event.target.closest("[data-history-index]");
    if (!button) {
      return;
    }
    const item = history[Number(button.dataset.historyIndex)];
    if (!item) {
      return;
    }
    methodInput.value = item.method;
    pathInput.value = item.path;
    bodyTypeInput.value = item.bodyType;
    mutationConfirm.checked = false;
    updateFormState();
    pathInput.focus();
  });

  updateFormState();
  renderHistory();
})();
