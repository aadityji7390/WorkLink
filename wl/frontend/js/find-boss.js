document.addEventListener("DOMContentLoaded", function () {

    const searchInput =
        document.getElementById("searchInput");

    const searchBtn =
        document.getElementById("searchBtn");

    const bossesContainer =
        document.getElementById("bossesContainer");

    const noResults =
        document.getElementById("noResults");

    const resultCount =
        document.getElementById("resultCount");


    // =====================================
    // SEARCH BOSSES
    // =====================================

    async function searchBosses() {

        const search =
            searchInput.value.trim();


        try {

            const response =
                await fetch(
                    API_BASE + "/api/bosses/search?q="
                    +
                    encodeURIComponent(search)
                );


            const data =
                await response.json();


            if (!data.success) {

                showNoResults();

                return;
            }


            displayBosses(
                data.bosses
            );


        } catch (error) {

            console.error(
                "Boss Search Error:",
                error
            );


            bossesContainer.innerHTML = "";

            resultCount.textContent =
                "Unable to connect to backend";

            noResults.style.display =
                "block";
        }
    }


    // =====================================
    // DISPLAY
    // =====================================

    function displayBosses(bosses) {

        bossesContainer.innerHTML = "";


        if (
            !bosses
            ||
            bosses.length === 0
        ) {

            showNoResults();

            return;
        }


        noResults.style.display =
            "none";


        resultCount.textContent =
            "Showing "
            +
            bosses.length
            +
            " employer(s)";


        bosses.forEach(
            function (boss) {

                const card =
                    document.createElement(
                        "article"
                    );


                card.className =
                    "job-card";


                card.innerHTML = `

                    <div class="job-main">

                        <div class="job-icon">
                            💼
                        </div>

                        <div class="job-info">

                            <h3>
                                ${escapeHtml(boss.name)}
                            </h3>

                            <p class="company">
                                ${escapeHtml(boss.company)}
                            </p>

                            <p class="location">
                                📱 ${escapeHtml(boss.phone)}
                            </p>

                            <div class="tags">

                                <span>
                                    Employer
                                </span>

                                <span>
                                    WorkLink
                                </span>

                            </div>

                        </div>

                    </div>


                    <div class="job-side">

                        <strong>
                            💼
                        </strong>

                        <button
                            type="button"
                            class="details-btn"
                        >
                            View Profile
                        </button>

                    </div>

                `;


                const detailsBtn =
                    card.querySelector(
                        ".details-btn"
                    );


                detailsBtn.addEventListener(
                    "click",
                    function () {

                        window.location.href =
                            "boss-profile.html?phone="
                            +
                            encodeURIComponent(
                                boss.phone
                            );
                    }
                );


                bossesContainer.appendChild(
                    card
                );
            }
        );
    }


    // =====================================
    // NO RESULTS
    // =====================================

    function showNoResults() {

        bossesContainer.innerHTML = "";

        resultCount.textContent =
            "No employers found";

        noResults.style.display =
            "block";
    }


    // =====================================
    // EVENTS
    // =====================================

    searchBtn.addEventListener(
        "click",
        searchBosses
    );


    searchInput.addEventListener(
        "keydown",
        function (event) {

            if (
                event.key === "Enter"
            ) {

                searchBosses();
            }
        }
    );


    searchInput.addEventListener(
        "input",
        function () {

            if (
                searchInput.value.trim()
                ===
                ""
            ) {

                searchBosses();
            }
        }
    );


    // =====================================
    // ESCAPE
    // =====================================

    function escapeHtml(value) {

        return String(value)
            .replaceAll("&", "&amp;")
            .replaceAll("<", "&lt;")
            .replaceAll(">", "&gt;")
            .replaceAll('"', "&quot;")
            .replaceAll("'", "&#039;");
    }


    searchBosses();

});