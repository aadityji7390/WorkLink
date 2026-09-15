document.addEventListener("DOMContentLoaded", function () {
    const searchInput = document.getElementById("searchInput");
    const searchBtn = document.getElementById("searchBtn");
    const bossesContainer = document.getElementById("bossesContainer");
    const noResults = document.getElementById("noResults");
    const resultCount = document.getElementById("resultCount");
    const filters = document.querySelectorAll(".filter");
    let activeFilter = "all";

    async function searchBosses() {
        const search = searchInput.value.trim();
        try {
            const response = await fetch(
                API_BASE + "/api/bosses/search?q=" + encodeURIComponent(search) +
                "&category=" + encodeURIComponent(activeFilter)
            );
            const data = await response.json();
            if (!data.success) {
                showNoResults("No employers found");
                return;
            }
            displayBosses(data.bosses || []);
        } catch (error) {
            console.error("Boss Search Error:", error);
            bossesContainer.innerHTML = "";
            resultCount.textContent = "Unable to connect to backend";
            noResults.style.display = "block";
        }
    }

    function displayBosses(bosses) {
        bossesContainer.innerHTML = "";
        if (!bosses.length) {
            showNoResults(activeFilter === "all" ? "No employers found" : "No employers found for this work type");
            return;
        }
        noResults.style.display = "none";
        resultCount.textContent = "Showing " + bosses.length + " employer(s)";

        bosses.forEach(function (boss) {
            const card = document.createElement("article");
            card.className = "job-card";
            const categories = (boss.categories || []).filter(Boolean);
            const locations = (boss.locations || []).filter(Boolean);
            const categoryText = categories.length ? categories.join(" • ") : "Open to workers";
            const locationText = locations.length ? locations.join(" • ") : "Location available in work posts";

            card.innerHTML = `
                <div class="job-main">
                    <div class="job-icon">💼</div>
                    <div class="job-info">
                        <h3>${escapeHtml(boss.name)}</h3>
                        <p class="company">🏢 ${escapeHtml(boss.company)}</p>
                        <p class="location">📍 ${escapeHtml(locationText)}</p>
                        <div class="availability-line ${boss.available === false ? "unavailable" : ""}">${boss.available === false ? "○ Currently unavailable" : "● Available for work"}</div>
                        <div class="tags">
                            <span>Employer</span>
                            <span>${escapeHtml(categoryText)}</span>
                        </div>
                    </div>
                </div>
                <div class="job-side">
                    <strong>💼</strong>
                    <button type="button" class="details-btn">View Details</button>
                    <button type="button" class="chat-btn">💬 Chat</button>
                </div>
            `;

            card.querySelector(".details-btn").addEventListener("click", function () {
                window.location.href = "boss-profile.html?phone=" + encodeURIComponent(boss.phone);
            });
            card.querySelector(".chat-btn").addEventListener("click", function () {
                window.location.href = "chat.html?type=boss&phone=" + encodeURIComponent(boss.phone) + "&name=" + encodeURIComponent(boss.name);
            });
            bossesContainer.appendChild(card);
        });
    }

    function showNoResults(message) {
        bossesContainer.innerHTML = "";
        resultCount.textContent = message;
        noResults.style.display = "block";
    }

    function escapeHtml(value) {
        return String(value ?? "")
            .replaceAll("&", "&amp;")
            .replaceAll("<", "&lt;")
            .replaceAll(">", "&gt;")
            .replaceAll('"', "&quot;")
            .replaceAll("'", "&#039;");
    }

    searchBtn.addEventListener("click", searchBosses);
    searchInput.addEventListener("keydown", function (event) {
        if (event.key === "Enter") searchBosses();
    });
    searchInput.addEventListener("input", function () {
        if (!searchInput.value.trim()) searchBosses();
    });
    filters.forEach(function (button) {
        button.addEventListener("click", function () {
            filters.forEach(function (x) { x.classList.remove("active"); });
            button.classList.add("active");
            activeFilter = button.dataset.filter || "all";
            searchBosses();
        });
    });

    searchBosses();
});
