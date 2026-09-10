document.addEventListener("DOMContentLoaded", function () {

    const API_BASE_URL = window.location.origin;

    const searchInput =
        document.getElementById("searchInput");

    const searchBtn =
        document.getElementById("searchBtn");

    const jobsContainer =
        document.getElementById("jobsContainer");

    const noResults =
        document.getElementById("noResults");

    const resultCount =
        document.getElementById("resultCount");


    // =====================================
    // LOAD WORKERS
    // =====================================

    async function searchWorkers() {

        const search =
            searchInput.value.trim();

        try {

            const response =
                await fetch(
                    API_BASE_URL +
                    "/api/workers/search?q=" +
                    encodeURIComponent(search)
                );

            if (!response.ok) {
                throw new Error(
                    "Server returned " + response.status
                );
            }

            const data =
                await response.json();

            if (!data.success) {

                showNoResults();

                return;
            }

            displayWorkers(
                data.workers
            );

        } catch (error) {

            console.error(
                "Worker Search Error:",
                error
            );

            jobsContainer.innerHTML = "";

            resultCount.textContent =
                "Unable to connect to backend";

            noResults.style.display =
                "block";
        }
    }


    // =====================================
    // DISPLAY WORKERS
    // =====================================

    function displayWorkers(workers) {

        jobsContainer.innerHTML = "";

        if (
            !workers ||
            workers.length === 0
        ) {

            showNoResults();

            return;
        }

        noResults.style.display =
            "none";

        resultCount.textContent =
            "Showing " +
            workers.length +
            " worker(s)";


        workers.forEach(function (worker) {

            const card =
                document.createElement("article");

            card.className =
                "job-card";


            card.innerHTML = `

                <div class="job-main">

                    <div class="job-icon">
                        👷
                    </div>

                    <div class="job-info">

                        <h3>
                            ${escapeHtml(worker.name)}
                        </h3>

                        <p class="company">
                            WorkLink Worker
                        </p>

                        <p class="location">
                            📍 ${escapeHtml(worker.location)}
                        </p>

                        <div class="tags">

                            <span>
                                ${escapeHtml(worker.skill)}
                            </span>

                            <span>
                                Available
                            </span>

                        </div>

                    </div>

                </div>


                <div class="job-side">

                    <strong>
                        👤
                    </strong>

                    <button
                        class="details-btn"
                        type="button"
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
                        "worker-profile.html?phone=" +
                        encodeURIComponent(
                            worker.phone
                        );

                }
            );


            jobsContainer.appendChild(card);

        });
    }


    // =====================================
    // NO RESULTS
    // =====================================

    function showNoResults() {

        jobsContainer.innerHTML = "";

        resultCount.textContent =
            "No workers found";

        noResults.style.display =
            "block";
    }


    // =====================================
    // SEARCH BUTTON
    // =====================================

    searchBtn.addEventListener(
        "click",
        searchWorkers
    );


    // =====================================
    // ENTER KEY
    // =====================================

    searchInput.addEventListener(
        "keydown",
        function (event) {

            if (event.key === "Enter") {

                searchWorkers();

            }

        }
    );


    // =====================================
    // EMPTY SEARCH
    // =====================================

    searchInput.addEventListener(
        "input",
        function () {

            if (
                searchInput.value.trim() === ""
            ) {

                searchWorkers();

            }

        }
    );


    // =====================================
    // ESCAPE HTML
    // =====================================

    function escapeHtml(value) {

        return String(value ?? "")
            .replaceAll("&", "&amp;")
            .replaceAll("<", "&lt;")
            .replaceAll(">", "&gt;")
            .replaceAll('"', "&quot;")
            .replaceAll("'", "&#039;");

    }


    // =====================================
    // INITIAL LOAD
    // =====================================

    searchWorkers();

});
