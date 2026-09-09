document.addEventListener("DOMContentLoaded", async function () {

    // ==========================================
    // LOGIN INFORMATION
    // ==========================================

    const loggedIn =
        localStorage.getItem("worklinkLoggedIn");

    const role =
        localStorage.getItem("worklinkRole");

    const loggedInPhone =
        localStorage.getItem("worklinkPhone");


    // ==========================================
    // LOGIN PROTECTION
    // ==========================================

    if (
        loggedIn !== "true" ||
        !role ||
        !loggedInPhone
    ) {

        window.location.href = "login.html";
        return;

    }


    // ==========================================
    // URL PHONE
    // ==========================================

    const params =
        new URLSearchParams(window.location.search);

    const urlPhone =
        params.get("phone");


    const hasTargetPhone =
        urlPhone &&
        urlPhone.trim() !== "";


    let viewedPhone;
    let isOwnProfile = false;


    // ==========================================
    // BOSS VIEWING BOSS PROFILE
    // ==========================================

    if (role === "boss") {

        if (hasTargetPhone) {

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


    // ==========================================
    // WORKER VIEWING BOSS PROFILE
    // ==========================================

    else if (role === "worker") {

        // Worker MUST have boss phone
        // to view a boss profile.

        if (!hasTargetPhone) {

            window.location.href =
                "worker-dashboard.html";

            return;

        }


        viewedPhone =
            urlPhone.trim();

        isOwnProfile = false;

    }


    // ==========================================
    // INVALID ROLE
    // ==========================================

    else {

        window.location.href =
            "login.html";

        return;

    }



    // ==========================================
    // PROFILE BUTTONS
    // ==========================================

    const editBtn =
        document.getElementById("editBtn");

    const rateSection =
        document.getElementById("rateSection");

    const rateBtn =
        document.getElementById("rateBtn");



    // ==========================================
    // OWN BOSS PROFILE
    // ==========================================

    if (isOwnProfile) {

        // Boss can edit own profile.

        editBtn.style.display =
            "inline-block";


        // Boss cannot rate himself.

        rateSection.style.display =
            "none";

    }


    // ==========================================
    // OTHER BOSS PROFILE
    // ==========================================

    else {

        // Nobody can edit another boss.

        editBtn.style.display =
            "none";


        // Only Worker can rate Boss.

        if (role === "worker") {

            rateSection.style.display =
                "block";

        } else {

            rateSection.style.display =
                "none";

        }

    }



    // ==========================================
    // LOAD BOSS PROFILE
    // ==========================================

    async function loadProfile() {

        try {

            const response =
                await fetch(
                    "/api/boss/profile?phone=" +
                    encodeURIComponent(viewedPhone)
                );


            const data =
                await response.json();


            if (!data.success) {

                alert(
                    data.message ||
                    "Boss profile not found."
                );


                if (role === "worker") {

                    window.location.href =
                        "worker-dashboard.html";

                } else {

                    window.location.href =
                        "boss-dashboard.html";

                }

                return;

            }



            // ==================================
            // NAME
            // ==================================

            const name =
                data.name || "Boss";


            document.getElementById(
                "headerName"
            ).textContent =
                name;


            document.getElementById(
                "name"
            ).textContent =
                name;


            document.getElementById(
                "avatar"
            ).textContent =
                name
                    .trim()
                    .charAt(0)
                    .toUpperCase() || "B";



            // ==================================
            // COMPANY
            // ==================================

            const company =
                data.company ||
                "Company not available";


            document.getElementById(
                "company"
            ).textContent =
                company;


            document.getElementById(
                "companyDisplay"
            ).innerHTML =
                `<span>${escapeHtml(company)}</span>`;



            // ==================================
            // PHONE
            // ==================================

            document.getElementById(
                "phone"
            ).textContent =
                data.phone ||
                viewedPhone;



            // ==================================
            // EDIT FORM
            // ==================================

            if (isOwnProfile) {

                document.getElementById(
                    "editName"
                ).value =
                    data.name || "";


                document.getElementById(
                    "editCompany"
                ).value =
                    data.company || "";


                document.getElementById(
                    "editPhone"
                ).value =
                    data.phone || "";

            }


        } catch (error) {

            console.error(
                "Boss profile error:",
                error
            );


            alert(
                "Unable to load boss profile."
            );

        }

    }



    // ==========================================
    // LOAD RATINGS + REVIEWS
    // ==========================================

    async function loadRatings() {

        try {

            const response =
                await fetch(
                    "/api/rating?type=boss&phone=" +
                    encodeURIComponent(viewedPhone)
                );


            const data =
                await response.json();


            if (!data.success) {
                return;
            }



            // ==================================
            // AVERAGE
            // ==================================

            const average =
                Number(
                    data.average || 0
                ).toFixed(1);


            document.getElementById(
                "averageRating"
            ).textContent =
                average;



            // ==================================
            // COUNT
            // ==================================

            const count =
                Number(
                    data.count || 0
                );


            document.getElementById(
                "ratingCount"
            ).textContent =
                count === 1
                    ? "1 rating"
                    : `${count} ratings`;



            // ==================================
            // REVIEWS
            // ==================================

            const reviewsList =
                document.getElementById(
                    "reviewsList"
                );


            const ratings =
                Array.isArray(data.ratings)
                    ? data.ratings
                    : [];


            if (ratings.length === 0) {

                reviewsList.innerHTML =
                    `
                    <p>
                        No reviews yet.
                    </p>
                    `;

                return;

            }



            reviewsList.innerHTML =
                ratings
                    .map(
                        rating => {

                            const ratingNumber =
                                Math.max(
                                    0,
                                    Math.min(
                                        5,
                                        Number(
                                            rating.rating
                                        ) || 0
                                    )
                                );


                            const stars =
                                "★".repeat(
                                    ratingNumber
                                );


                            const raterName =
                                rating.raterName ||
                                "Worker";


                            const review =
                                rating.review ||
                                "No review";


                            return `
                                <div
                                    style="
                                        padding:15px;
                                        margin-bottom:12px;
                                        border:1px solid #e5e7eb;
                                        border-radius:10px;
                                        background:#fafafa;
                                    ">

                                    <div
                                        style="
                                            display:flex;
                                            justify-content:space-between;
                                            gap:10px;
                                            margin-bottom:7px;
                                        ">

                                        <strong>
                                            ${escapeHtml(raterName)}
                                        </strong>

                                        <span>
                                            ${stars || "—"}
                                        </span>

                                    </div>

                                    <p
                                        style="
                                            margin:0;
                                            line-height:1.5;
                                            color:#444;
                                        ">
                                        ${escapeHtml(review)}
                                    </p>

                                </div>
                            `;

                        }
                    )
                    .join("");


        } catch (error) {

            console.error(
                "Boss rating loading error:",
                error
            );

        }

    }



    // ==========================================
    // EDIT PROFILE
    // ==========================================

    if (editBtn) {

        editBtn.addEventListener(
            "click",
            function () {

                if (!isOwnProfile) {
                    return;
                }


                document.getElementById(
                    "viewMode"
                ).style.display =
                    "none";


                document.getElementById(
                    "editMode"
                ).classList.remove(
                    "hidden"
                );


                editBtn.style.display =
                    "none";

            }
        );

    }



    // ==========================================
    // CANCEL EDIT
    // ==========================================

    const cancelBtn =
        document.getElementById(
            "cancelBtn"
        );


    if (cancelBtn) {

        cancelBtn.addEventListener(
            "click",
            function () {

                document.getElementById(
                    "editMode"
                ).classList.add(
                    "hidden"
                );


                document.getElementById(
                    "viewMode"
                ).style.display =
                    "block";


                editBtn.style.display =
                    "inline-block";

            }
        );

    }



    // ==========================================
    // SAVE BOSS PROFILE
    // ==========================================

    const saveBtn =
        document.getElementById(
            "saveBtn"
        );


    if (saveBtn) {

        saveBtn.addEventListener(
            "click",
            async function () {

                if (!isOwnProfile) {
                    return;
                }


                const name =
                    document.getElementById(
                        "editName"
                    ).value.trim();


                const company =
                    document.getElementById(
                        "editCompany"
                    ).value.trim();


                const phone =
                    document.getElementById(
                        "editPhone"
                    ).value.trim();


                const password =
                    document.getElementById(
                        "editPassword"
                    ).value.trim();



                // ==================================
                // VALIDATION
                // ==================================

                if (!name) {

                    alert(
                        "Please enter your name."
                    );

                    return;

                }


                if (!company) {

                    alert(
                        "Please enter your company name."
                    );

                    return;

                }


                if (!phone) {

                    alert(
                        "Please enter your phone number."
                    );

                    return;

                }


                if (!/^[0-9]{10}$/.test(phone)) {

                    alert(
                        "Phone number must contain exactly 10 digits."
                    );

                    return;

                }


                if (!password) {

                    alert(
                        "Please enter your current password."
                    );

                    return;

                }



                saveBtn.disabled = true;

                saveBtn.textContent =
                    "Saving...";



                try {

                    const response =
                        await fetch(
                            "/api/boss/update",
                            {
                                method: "POST",

                                headers: {
                                    "Content-Type":
                                        "application/json"
                                },

                                body: JSON.stringify({

                                    oldPhone:
                                        loggedInPhone,

                                    name:
                                        name,

                                    phone:
                                        phone,

                                    company:
                                        company,

                                    password:
                                        password

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


                        saveBtn.disabled = false;

                        saveBtn.textContent =
                            "💾 Save Changes";

                        return;

                    }



                    // ==================================
                    // UPDATE LOCAL STORAGE
                    // ==================================

                    localStorage.setItem(
                        "worklinkPhone",
                        phone
                    );


                    localStorage.setItem(
                        "worklinkName",
                        name
                    );


                    alert(
                        "Profile updated successfully."
                    );


                    window.location.href =
                        "boss-profile.html?phone=" +
                        encodeURIComponent(phone);

                } catch (error) {

                    console.error(
                        "Boss profile update error:",
                        error
                    );


                    alert(
                        "Unable to update profile."
                    );


                    saveBtn.disabled = false;

                    saveBtn.textContent =
                        "💾 Save Changes";

                }

            }
        );

    }



    // ==========================================
    // RATE BOSS
    // ==========================================

    if (rateBtn) {

        rateBtn.addEventListener(
            "click",
            function () {

                // Only Worker can rate Boss.

                if (role !== "worker") {

                    alert(
                        "Only a Worker can rate a Boss."
                    );

                    return;

                }


                window.location.href =
                    "rating.html?type=boss&phone=" +
                    encodeURIComponent(viewedPhone) +
                    "&name=" +
                    encodeURIComponent(
                        document.getElementById(
                            "headerName"
                        ).textContent
                    );

            }
        );

    }



    // ==========================================
    // HTML ESCAPE
    // ==========================================

    function escapeHtml(value) {

        return String(value)
            .replace(/&/g, "&amp;")
            .replace(/</g, "&lt;")
            .replace(/>/g, "&gt;")
            .replace(/"/g, "&quot;")
            .replace(/'/g, "&#039;");

    }



    // ==========================================
    // INITIAL LOAD
    // ==========================================

    await loadProfile();

    await loadRatings();

});