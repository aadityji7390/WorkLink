document.addEventListener("DOMContentLoaded", async function () {

    const loggedIn = localStorage.getItem("worklinkLoggedIn");
    const role = localStorage.getItem("worklinkRole");
    const phone = localStorage.getItem("worklinkPhone");

    // ==============================
    // LOGIN PROTECTION
    // ==============================

    if (
        loggedIn !== "true" ||
        role !== "worker" ||
        !phone
    ) {
        window.location.href = "login.html";
        return;
    }


    // ==============================
    // LOAD WORKER PROFILE
    // ==============================

    try {

        const response = await fetch(
            "/api/worker/profile?phone=" +
            encodeURIComponent(phone)
        );

        const data = await response.json();

        if (data.success) {

            const name = data.name || "Worker";

            document.getElementById("dashboardName").textContent = name;
            document.getElementById("welcomeName").textContent = name;

            const avatar =
                name.trim().charAt(0).toUpperCase() || "W";

            document.getElementById("dashboardAvatar").textContent =
                avatar;

            localStorage.setItem("worklinkName", name);
        }

    } catch (error) {

        console.error(
            "Worker profile loading error:",
            error
        );

    }


    // ==============================
    // LOAD MY RATING
    // ==============================

    try {

        const response = await fetch(
            "/api/rating?type=worker&phone=" +
            encodeURIComponent(phone)
        );

        const data = await response.json();

        if (data.success) {

            const average =
                Number(data.average || 0).toFixed(1);

            document.getElementById("myRating").textContent =
                average;
        }

    } catch (error) {

        console.error(
            "Rating loading error:",
            error
        );

    }


    // ==============================
    // NOTIFICATION
    // ==============================

    const notificationBtn =
        document.getElementById("notificationBtn");

    if (notificationBtn) {

        notificationBtn.addEventListener(
            "click",
            function () {

                alert(
                    "No new notifications right now."
                );

            }
        );
    }


    // ==============================
    // VIEW DETAILS
    // ==============================

    document.querySelectorAll(
        ".job-right button"
    ).forEach(function (button) {

        button.addEventListener(
            "click",
            function () {

                alert(
                    "Job details will be available soon."
                );

            }
        );

    });


    // ==============================
    // LOGOUT
    // ==============================

    const logoutBtn =
        document.getElementById("logoutBtn");

    if (logoutBtn) {

        logoutBtn.addEventListener(
            "click",
            function () {

                localStorage.removeItem(
                    "worklinkLoggedIn"
                );

                localStorage.removeItem(
                    "worklinkRole"
                );

                localStorage.removeItem(
                    "worklinkPhone"
                );

                localStorage.removeItem(
                    "worklinkName"
                );

                window.location.href = "login.html";
            }
        );
    }

});