document.addEventListener("DOMContentLoaded", async function () {

    // =========================================
    // API
    // =========================================

    const API_BASE = window.API_BASE || (location.hostname === "localhost" || location.hostname === "127.0.0.1" ? "http://localhost:8080" : window.location.origin);


    // =========================================
    // LOGIN
    // =========================================

    const loggedIn =
        localStorage.getItem("worklinkLoggedIn");

    const role =
        localStorage.getItem("worklinkRole");

    let loggedInPhone =
        localStorage.getItem("worklinkPhone");


    if (
        loggedIn !== "true" ||
        !role ||
        !loggedInPhone
    ) {

        alert("Please login first.");

        window.location.href = "login.html";

        return;
    }


    // =========================================
    // URL PHONE
    // =========================================

    const params =
        new URLSearchParams(
            window.location.search
        );

    const urlPhone =
        params.get("phone");


    let viewedPhone;
    let isOwnProfile = false;


    // =========================================
    // WORKER
    // =========================================

    if (role === "worker") {

        if (
            urlPhone &&
            urlPhone.trim() !== ""
        ) {

            viewedPhone =
                urlPhone.trim();

            isOwnProfile =
                viewedPhone === loggedInPhone;

        } else {

            viewedPhone =
                loggedInPhone;

            isOwnProfile = true;
        }

    }


    // =========================================
    // BOSS
    // =========================================

    else if (role === "boss") {

        if (
            !urlPhone ||
            urlPhone.trim() === ""
        ) {

            window.location.href =
                "boss-dashboard.html";

            return;
        }

        viewedPhone =
            urlPhone.trim();

        isOwnProfile = false;
    }


    // =========================================
    // INVALID ROLE
    // =========================================

    else {

        window.location.href =
            "login.html";

        return;
    }


    // =========================================
    // ELEMENTS
    // =========================================

    const editBtn =
        document.getElementById("editBtn");

    const saveBtn =
        document.getElementById("saveBtn");

    const cancelBtn =
        document.getElementById("cancelBtn");

    const viewMode =
        document.getElementById("viewMode");

    const editMode =
        document.getElementById("editMode");


    const nameElement =
        document.getElementById("name");

    const emailElement =
        document.getElementById("email");

    const phoneElement =
        document.getElementById("phone");

    const locationElement =
        document.getElementById("location");

    const skillsElement =
        document.getElementById("skills");


    const headerName =
        document.getElementById("headerName");

    const avatar =
        document.getElementById("avatar");


    const editName =
        document.getElementById("editName");

    const editEmail =
        document.getElementById("editEmail");

    const editPhone =
        document.getElementById("editPhone");

    const editLocation =
        document.getElementById("editLocation");

    const editSkill =
        document.getElementById("editSkill");

    const editPassword =
        document.getElementById("editPassword");


    const rateSection =
        document.getElementById("rateSection");

    const rateBtn =
        document.getElementById("rateBtn");

    const chatBtn = document.getElementById("chatBtn");
    if (chatBtn && !isOwnProfile) {
        chatBtn.style.display = "inline-block";
        chatBtn.href = "chat.html?type=worker&phone=" + encodeURIComponent(viewedPhone) + "&name=" + encodeURIComponent((document.getElementById("headerName") || {}).textContent || "WorkLink User");
    }


    const availabilityBtn =
        document.getElementById(
            "availabilityBtn"
        );


    // =========================================
    // CURRENT DATA
    // =========================================

    let currentSkill = "";


    // =========================================
    // AVAILABILITY
    // =========================================

    function availabilityKey() {

        return (
            "worklinkAvailability_" +
            viewedPhone
        );
    }


    function getAvailability() {

        const saved =
            localStorage.getItem(
                availabilityKey()
            );

        if (
            saved === "available" ||
            saved === "unavailable"
        ) {

            return saved;
        }

        return "available";
    }


    function updateAvailabilityButton() {

        if (!availabilityBtn) {
            return;
        }


        const status =
            getAvailability();


        if (status === "available") {

            availabilityBtn.textContent =
                "Available";

            availabilityBtn.style.background =
                "#16a34a";

            availabilityBtn.style.color =
                "#ffffff";

        } else {

            availabilityBtn.textContent =
                "Not Available";

            availabilityBtn.style.background =
                "#dc2626";

            availabilityBtn.style.color =
                "#ffffff";
        }
    }


    function toggleAvailability() {

        if (!isOwnProfile) {
            return;
        }


        const current =
            getAvailability();


        const next =
            current === "available"
                ? "unavailable"
                : "available";


        localStorage.setItem(
            availabilityKey(),
            next
        );


        updateAvailabilityButton();
    }


    // =========================================
    // AVAILABILITY BUTTON
    // =========================================

    if (
        availabilityBtn &&
        isOwnProfile &&
        role === "worker"
    ) {

        availabilityBtn.style.display =
            "inline-block";

        availabilityBtn.style.marginTop =
            "10px";

        availabilityBtn.style.padding =
            "10px 20px";

        availabilityBtn.style.border =
            "none";

        availabilityBtn.style.borderRadius =
            "8px";

        availabilityBtn.style.cursor =
            "pointer";

        availabilityBtn.style.fontSize =
            "15px";

        availabilityBtn.style.fontWeight =
            "600";

        availabilityBtn.addEventListener(
            "click",
            toggleAvailability
        );

        updateAvailabilityButton();

    } else {

        if (availabilityBtn) {

            availabilityBtn.style.display =
                "none";
        }
    }


    // =========================================
    // LOAD PROFILE
    // =========================================

    async function loadProfile() {

        try {

            const response =
                await fetch(
                    API_BASE +
                    "/api/worker/profile?phone=" +
                    encodeURIComponent(
                        viewedPhone
                    )
                );


            if (!response.ok) {

                throw new Error(
                    "Server error: " +
                    response.status
                );
            }


            const data =
                await response.json();


            if (!data.success) {

                alert(
                    data.message ||
                    "Worker profile not found."
                );

                return;
            }


            // =====================================
            // NAME
            // =====================================

            const workerName =
                data.name || "Worker";


            if (nameElement) {

                nameElement.textContent =
                    workerName;
            }


            if (headerName) {

                headerName.textContent =
                    workerName;
            }


            // =====================================
            // AVATAR
            // =====================================

            if (avatar) {

                avatar.textContent =
                    workerName
                        .trim()
                        .charAt(0)
                        .toUpperCase() || "W";
            }


            // =====================================
            // EMAIL
            // =====================================

            if (emailElement) {

                emailElement.textContent =
                    data.email &&
                    String(data.email).trim()
                        ? String(data.email).trim()
                        : "Email not available";
            }


            // =====================================
            // PHONE
            // =====================================

            if (phoneElement) {

                phoneElement.textContent =
                    data.phone ||
                    "Not available";
            }


            // =====================================
            // LOCATION
            // =====================================

            if (locationElement) {

                locationElement.textContent =
                    data.location ||
                    "Not available";
            }


            // =====================================
            // SKILL
            // =====================================

            currentSkill =
                data.skill || "Worker";


            displaySkills(
                currentSkill
            );


            // =====================================
            // EDIT VALUES
            // =====================================

            if (isOwnProfile) {

                if (editName) {

                    editName.value =
                        data.name || "";
                }


                if (editEmail) {

                    editEmail.value =
                        data.email || "";
                }


                if (editPhone) {

                    editPhone.value =
                        data.phone || "";
                }


                if (editLocation) {

                    editLocation.value =
                        data.location || "";
                }


                if (editSkill) {

                    editSkill.value =
                        data.skill || "";
                }


                if (editPassword) {

                    editPassword.value = "";
                }
            }


        } catch (error) {

            console.error(
                "Worker profile error:",
                error
            );


            if (nameElement) {

                nameElement.textContent =
                    "Unable to load profile";
            }


            alert(
                "Unable to connect to WorkLink backend. Check Java server on port 8080."
            );
        }
    }


    // =========================================
    // DISPLAY SKILLS
    // =========================================

    function displaySkills(skillText) {

        if (!skillsElement) {
            return;
        }


        if (
            !skillText ||
            skillText.trim() === ""
        ) {

            skillsElement.innerHTML =
                "<span>Worker</span>";

            return;
        }


        const skillArray =
            skillText
                .split(",")
                .map(function (skill) {

                    return skill.trim();

                })
                .filter(function (skill) {

                    return skill !== "";

                });


        if (skillArray.length === 0) {

            skillsElement.innerHTML =
                "<span>Worker</span>";

            return;
        }


        skillsElement.innerHTML =
            skillArray
                .map(function (skill) {

                    return (
                        "<span>" +
                        escapeHtml(skill) +
                        "</span>"
                    );

                })
                .join("");
    }


    // =========================================
    // HTML ESCAPE
    // =========================================

    function escapeHtml(value) {

        return String(value)
            .replace(/&/g, "&amp;")
            .replace(/</g, "&lt;")
            .replace(/>/g, "&gt;")
            .replace(/"/g, "&quot;")
            .replace(/'/g, "&#039;");
    }


    // =========================================
    // OWN / OTHER PROFILE
    // =========================================

    if (isOwnProfile) {

        if (editBtn) {

            editBtn.style.display =
                "inline-block";
        }


        if (rateSection) {

            rateSection.style.display =
                "none";
        }

    } else {

        if (editBtn) {

            editBtn.style.display =
                "none";
        }


        if (
            rateSection &&
            role === "boss"
        ) {

            rateSection.style.display =
                "block";
        }
    }


    // =========================================
    // EDIT PROFILE
    // =========================================

    if (editBtn) {

        editBtn.addEventListener(
            "click",
            function () {

                if (!isOwnProfile) {
                    return;
                }


                if (viewMode) {

                    viewMode.style.display =
                        "none";
                }


                if (editMode) {

                    editMode.classList.remove(
                        "hidden"
                    );

                    editMode.style.display =
                        "block";
                }


                editBtn.style.display =
                    "none";


                // Put current values again

                if (editName) {

                    editName.value =
                        nameElement.textContent;
                }


                if (editEmail) {

                    const value =
                        emailElement.textContent;

                    editEmail.value =
                        value ===
                        "Email not available"
                            ? ""
                            : value;
                }


                if (editPhone) {

                    editPhone.value =
                        phoneElement.textContent;
                }


                if (editLocation) {

                    editLocation.value =
                        locationElement.textContent;
                }


                if (editSkill) {

                    editSkill.value =
                        currentSkill;
                }


                if (editPassword) {

                    editPassword.value = "";
                }
            }
        );
    }


    // =========================================
    // CANCEL
    // =========================================

    if (cancelBtn) {

        cancelBtn.addEventListener(
            "click",
            function () {

                if (editMode) {

                    editMode.classList.add(
                        "hidden"
                    );

                    editMode.style.display =
                        "none";
                }


                if (viewMode) {

                    viewMode.style.display =
                        "block";
                }


                if (editBtn) {

                    editBtn.style.display =
                        "inline-block";
                }


                if (editPassword) {

                    editPassword.value = "";
                }
            }
        );
    }


    // =========================================
    // SAVE PROFILE
    // =========================================

    if (saveBtn) {

        saveBtn.addEventListener(
            "click",
            async function () {

                if (!isOwnProfile) {
                    return;
                }


                const name =
                    editName.value.trim();

                const email =
                    editEmail.value.trim();

                const phone =
                    editPhone.value.trim();

                const skill =
                    editSkill.value.trim();

                const location =
                    editLocation.value.trim();

                const password =
                    editPassword.value.trim();


                // =====================================
                // VALIDATION
                // =====================================

                if (!name) {

                    alert(
                        "Please enter your name."
                    );

                    editName.focus();

                    return;
                }


                if (!email) {

                    alert(
                        "Please enter your email."
                    );

                    editEmail.focus();

                    return;
                }


                if (
                    !/^[0-9]{10}$/.test(phone)
                ) {

                    alert(
                        "Phone number must contain exactly 10 digits."
                    );

                    editPhone.focus();

                    return;
                }


                if (!skill) {

                    alert(
                        "Please enter at least one skill."
                    );

                    editSkill.focus();

                    return;
                }


                if (!location) {

                    alert(
                        "Please enter your location."
                    );

                    editLocation.focus();

                    return;
                }


                if (!password) {

                    alert(
                        "Please enter your current password."
                    );

                    editPassword.focus();

                    return;
                }


                saveBtn.disabled = true;

                saveBtn.textContent =
                    "Saving...";


                try {

                    const response =
                        await fetch(
                            API_BASE +
                            "/api/worker/update",
                            {
                                method: "POST",

                                headers: {
                                    "Content-Type":
                                        "application/json"
                                },

                                body:
                                    JSON.stringify({

                                        oldPhone:
                                            loggedInPhone,

                                        name:
                                            name,

                                        password:
                                            password,

                                        phone:
                                            phone,

                                        skill:
                                            skill,

                                        location:
                                            location,

                                        email:
                                            email
                                    })
                            }
                        );


                    const data =
                        await response.json();


                    if (!data.success) {

                        alert(
                            data.message ||
                            "Profile update failed."
                        );

                        return;
                    }


                    // =====================================
                    // UPDATE LOCAL STORAGE
                    // =====================================

                    localStorage.setItem(
                        "worklinkPhone",
                        phone
                    );

                    localStorage.setItem(
                        "worklinkName",
                        name
                    );


                    loggedInPhone =
                        phone;

                    viewedPhone =
                        phone;

                    currentSkill =
                        skill;


                    // =====================================
                    // UPDATE SCREEN
                    // =====================================

                    nameElement.textContent =
                        name;

                    emailElement.textContent =
                        email;

                    phoneElement.textContent =
                        phone;

                    locationElement.textContent =
                        location;

                    headerName.textContent =
                        name;

                    avatar.textContent =
                        name
                            .charAt(0)
                            .toUpperCase();


                    displaySkills(
                        skill
                    );


                    // =====================================
                    // CLOSE EDIT MODE
                    // =====================================

                    if (editMode) {

                        editMode.classList.add(
                            "hidden"
                        );

                        editMode.style.display =
                            "none";
                    }


                    if (viewMode) {

                        viewMode.style.display =
                            "block";
                    }


                    if (editBtn) {

                        editBtn.style.display =
                            "inline-block";
                    }


                    editPassword.value = "";


                    // Availability key update
                    // if phone changed

                    updateAvailabilityButton();


                    alert(
                        "Profile updated successfully!"
                    );


                } catch (error) {

                    console.error(
                        "Profile update error:",
                        error
                    );


                    alert(
                        "Unable to update profile."
                    );

                } finally {

                    saveBtn.disabled =
                        false;

                    saveBtn.textContent =
                        "💾 Save Changes";
                }
            }
        );
    }


    // =========================================
    // RATE WORKER
    // =========================================

    if (rateBtn) {

        rateBtn.addEventListener(
            "click",
            function () {

                if (role !== "boss") {

                    alert(
                        "Only a Boss can rate a Worker."
                    );

                    return;
                }


                window.location.href =
                    "rating.html?type=worker&phone=" +
                    encodeURIComponent(
                        viewedPhone
                    ) +
                    "&name=" +
                    encodeURIComponent(
                        headerName
                            ? headerName.textContent
                            : "Worker"
                    );
            }
        );
    }


    // =========================================
    // LOAD RATINGS
    // =========================================

    async function loadRatings() {

        try {

            const response =
                await fetch(
                    API_BASE +
                    "/api/rating?type=worker&phone=" +
                    encodeURIComponent(
                        viewedPhone
                    )
                );


            if (!response.ok) {
                return;
            }


            const data =
                await response.json();


            if (!data.success) {
                return;
            }


            const average =
                Number(
                    data.average || 0
                ).toFixed(1);


            const count =
                Number(
                    data.count || 0
                );


            const averageRating =
                document.getElementById(
                    "averageRating"
                );


            const ratingCount =
                document.getElementById(
                    "ratingCount"
                );


            if (averageRating) {

                averageRating.textContent =
                    average;
            }


            if (ratingCount) {

                ratingCount.textContent =
                    count === 1
                        ? "1 rating"
                        : count + " ratings";
            }


            const reviewsList =
                document.getElementById(
                    "reviewsList"
                );


            if (!reviewsList) {
                return;
            }


            const ratings =
                Array.isArray(data.ratings)
                    ? data.ratings
                    : [];


            if (ratings.length === 0) {

                reviewsList.innerHTML =
                    "<p>No reviews yet.</p>";

                return;
            }


            reviewsList.innerHTML = "";


            ratings.forEach(
                function (rating) {

                    const reviewer =
                        rating.raterName ||
                        "User";


                    const value =
                        Number(
                            rating.rating
                        ) || 0;


                    const review =
                        rating.review ||
                        "No review";


                    const stars =
                        "★".repeat(
                            Math.max(
                                0,
                                Math.min(
                                    5,
                                    value
                                )
                            )
                        );


                    const div =
                        document.createElement(
                            "div"
                        );


                    div.style.padding =
                        "15px";

                    div.style.marginBottom =
                        "12px";

                    div.style.border =
                        "1px solid #e5e7eb";

                    div.style.borderRadius =
                        "10px";

                    div.style.background =
                        "#fafafa";


                    div.innerHTML =

                        "<div style=\"" +
                        "display:flex;" +
                        "justify-content:space-between;" +
                        "gap:10px;" +
                        "margin-bottom:7px;" +
                        "\">" +

                        "<strong>" +
                        escapeHtml(
                            reviewer
                        ) +
                        "</strong>" +

                        "<span>" +
                        escapeHtml(
                            stars || "—"
                        ) +
                        "</span>" +

                        "</div>" +

                        "<p style=\"" +
                        "margin:0;" +
                        "line-height:1.5;" +
                        "color:#444;" +
                        "\">" +

                        escapeHtml(
                            review
                        ) +

                        "</p>";


                    reviewsList.appendChild(
                        div
                    );
                }
            );


        } catch (error) {

            console.error(
                "Rating loading error:",
                error
            );
        }
    }


    // =========================================
    // INITIAL LOAD
    // =========================================

    await loadProfile();

    updateAvailabilityButton();

    await loadRatings();

});