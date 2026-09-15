// WorkLink client helper: works with VS Code Live Server and Render.
(function () {
    const originalFetch = window.fetch.bind(window);
    const host = window.location.hostname;
    const isLocal = host === "localhost" || host === "127.0.0.1";
    window.API_BASE = (isLocal && window.location.port !== "8080")
        ? "http://localhost:8080"
        : window.location.origin;

    window.fetch = function (input, init) {
        init = init || {};
        let url = typeof input === "string" ? input : input.url;
        if (typeof url === "string" && url.indexOf("/api/") === 0) {
            url = window.API_BASE + url;
            input = url;
        }
        if (url && url.indexOf("/api/") !== -1) {
            const token = localStorage.getItem("worklinkToken");
            const headers = new Headers(init.headers || (input instanceof Request ? input.headers : undefined));
            if (token) headers.set("Authorization", "Bearer " + token);
            init.headers = headers;
        }
        return originalFetch(input, init);
    };

    window.workLinkLogout = async function () {
        try { await originalFetch(window.API_BASE + "/api/logout", { method: "POST", headers: tokenHeaders() }); } catch (_) {}
        ["worklinkLoggedIn", "worklinkRole", "worklinkPhone", "worklinkName", "worklinkToken"].forEach(k => localStorage.removeItem(k));
        window.location.href = "login.html";
    };

    function tokenHeaders() {
        const h = { "Content-Type": "application/json" };
        const token = localStorage.getItem("worklinkToken");
        if (token) h.Authorization = "Bearer " + token;
        return h;
    }
})();
