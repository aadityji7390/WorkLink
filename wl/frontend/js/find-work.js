document.addEventListener("DOMContentLoaded", () => {

    const input = document.getElementById("searchInput");
    const btn = document.getElementById("searchBtn");
    const container = document.getElementById("jobsContainer");
    const empty = document.getElementById("noResults");
    const count = document.getElementById("resultCount");

    let category = "all";
    let cache = [];

    const API_BASE =
        window.API_BASE ||
        (location.hostname === "localhost" && location.port !== "8080"
            ? "http://localhost:8080"
            : location.origin);


    async function loadBosses() {

        try {

            const search = input.value.trim();

            const url =
                API_BASE +
                "/api/bosses/search?q=" +
                encodeURIComponent(search);

            const response = await fetch(url);

            if (!response.ok) {
                throw new Error("Server error");
            }

            const data = await response.json();

            cache = Array.isArray(data.bosses)
                ? data.bosses
                : Array.isArray(data)
                    ? data
                    : [];

            render();

        } catch (error) {

            console.error("Employer search error:", error);

            container.innerHTML = "";

            count.textContent = "Unable to connect to backend";

            empty.style.display = "block";

        }
    }


    function render() {

        let bosses = [...cache];

        /*
         * Category filter
         */
        if (category !== "all") {

            bosses = bosses.filter(boss => {

                const text = (
                    String(boss.name || "") +
                    " " +
                    String(boss.company || "") +
                    " " +
                    String(boss.skill || "") +
                    " " +
                    String(boss.category || "") +
                    " " +
                    String(boss.workType || "")
                ).toLowerCase();

                return text.includes(category.toLowerCase());

            });

        }


        container.innerHTML = "";

        if (bosses.length === 0) {

            empty.style.display = "block";
            count.textContent = "No employers found";
            return;

        }

        empty.style.display = "none";

        count.textContent =
            "Showing " + bosses.length + " employer(s)";


        bosses.forEach(boss => {

            const card = document.createElement("article");

            card.className = "job-card";

            const name =
                boss.name ||
                boss.fullName ||
                boss.username ||
                "Employer";

            const company =
                boss.company ||
                boss.companyName ||
                boss.business ||
                "WorkLink Employer";

            const skill =
                boss.skill ||
                boss.category ||
                boss.workType ||
                "General Work";

            const location =
                boss.location ||
                boss.address ||
                "Location not available";

            const phone =
                boss.phone ||
                boss.mobile ||
                "";

            const description =
                boss.description ||
                boss.about ||
                "Employer profile available on WorkLink.";


            card.innerHTML = `

                <div class="job-main">

                    <div class="job-icon">
                        ${getIcon(skill)}
                    </div>

                    <div class="job-info">

                        <h3>${esc(name)}</h3>

                        <p class="company">
                            🏢 ${esc(company)}
                        </p>

                        <p class="location">
                            📍 ${esc(location)}
                        </p>

                        <div class="tags">

                            <span>
                                ${esc(skill)}
                            </span>

                        </div>

                    </div>

                </div>


                <div class="job-side">

                    <button
                        class="details-btn"
                        type="button">
                        View Details
                    </button>

                </div>
            `;


            const detailsButton =
                card.querySelector(".details-btn");


            detailsButton.onclick = () => {

                /*
                 * Boss profile is opened using phone number.
                 * This matches the existing WorkLink profile system.
                 */

                if (phone) {

                    window.location.href =
                        "boss-profile.html?phone=" +
                        encodeURIComponent(phone);

                } else {

                    /*
                     * Fallback if backend returns an ID instead of phone.
                     */

                    const id =
                        boss.id ||
                        boss.bossId ||
                        boss.username;

                    if (id) {

                        window.location.href =
                            "boss-profile.html?id=" +
                            encodeURIComponent(id);

                    } else {

                        alert(
                            "Employer profile information is not available."
                        );

                    }

                }

            };


            container.appendChild(card);

        });

    }


    function getIcon(skill) {

        const value =
            String(skill || "").toLowerCase();

        if (value.includes("electric")) {
            return "🔧";
        }

        if (value.includes("plumb")) {
            return "🔧";
        }

        if (value.includes("driver")) {
            return "🚗";
        }

        if (value.includes("construction")) {
            return "🏗️";
        }

        return "💼";
    }


    function esc(value) {

        return String(value ?? "")
            .replace(/&/g, "&amp;")
            .replace(/</g, "&lt;")
            .replace(/>/g, "&gt;")
            .replace(/"/g, "&quot;")
            .replace(/'/g, "&#039;");

    }


    /*
     * Search button
     */

    btn.addEventListener("click", loadBosses);


    /*
     * Enter key search
     */

    input.addEventListener("keydown", event => {

        if (event.key === "Enter") {
            loadBosses();
        }

    });


    /*
     * Filters
     */

    document.querySelectorAll(".filter").forEach(button => {

        button.addEventListener("click", () => {

            document
                .querySelectorAll(".filter")
                .forEach(item =>
                    item.classList.remove("active")
                );

            button.classList.add("active");

            category =
                button.dataset.filter || "all";

            render();

        });

    });


    /*
     * Initial load
     */

    loadBosses();

});
