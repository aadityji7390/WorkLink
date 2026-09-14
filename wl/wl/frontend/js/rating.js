document.addEventListener("DOMContentLoaded", function () {

    // ==========================================
    // LOGIN CHECK
    // ==========================================

    const loggedIn =
        localStorage.getItem("worklinkLoggedIn");

    const myRole =
        localStorage.getItem("worklinkRole");

    const myPhone =
        localStorage.getItem("worklinkPhone");


    if (
        loggedIn !== "true" ||
        !myRole ||
        !myPhone
    ) {

        window.location.href = "login.html";
        return;

    }


    // ==========================================
    // GET TARGET FROM PROFILE
    // ==========================================

    const params =
        new URLSearchParams(window.location.search);

    const targetType =
        params.get("type");

    const targetPhone =
        params.get("phone");

    const targetName =
        params.get("name") || "User";


    // ==========================================
    // TARGET VALIDATION
    // ==========================================

    if (
        !targetType ||
        !targetPhone ||
        (
            targetType !== "worker" &&
            targetType !== "boss"
        )
    ) {

        document.getElementById(
            "message"
        ).textContent =
            "Please open a Worker or Boss profile first to give a rating.";

        document.getElementById(
            "submitRating"
        ).disabled = true;

        return;

    }


    // ==========================================
    // ONLY OPPOSITE ROLE CAN RATE
    // ==========================================

    if (
        (myRole === "worker" && targetType !== "boss") ||
        (myRole === "boss" && targetType !== "worker")
    ) {

        document.getElementById(
            "message"
        ).textContent =
            "You can only rate the opposite role.";

        document.getElementById(
            "submitRating"
        ).disabled = true;

        return;

    }


    // ==========================================
    // SELF RATING BLOCK
    // ==========================================

    if (
        String(myPhone) === String(targetPhone)
    ) {

        document.getElementById(
            "message"
        ).textContent =
            "You cannot rate your own profile.";

        document.getElementById(
            "submitRating"
        ).disabled = true;

        return;

    }


    // ==========================================
    // TARGET TEXT
    // ==========================================

    const targetText =
        document.getElementById(
            "targetText"
        );


    if (targetType === "worker") {

        targetText.textContent =
            "Rate and review " +
            targetName +
            "'s work.";

    } else {

        targetText.textContent =
            "Rate and review " +
            targetName +
            " as an employer.";

    }



    // ==========================================
    // STAR SYSTEM
    // ==========================================

    const stars =
        document.querySelectorAll(
            "#stars button"
        );

    const ratingText =
        document.getElementById(
            "ratingText"
        );

    let selectedRating = 0;


    stars.forEach(function (star) {

        star.addEventListener(
            "mouseenter",
            function () {

                const rating =
                    Number(
                        star.dataset.rating
                    );

                highlightStars(rating);

            }
        );


        star.addEventListener(
            "mouseleave",
            function () {

                highlightStars(
                    selectedRating
                );

            }
        );


        star.addEventListener(
            "click",
            function () {

                selectedRating =
                    Number(
                        star.dataset.rating
                    );


                highlightStars(
                    selectedRating
                );


                ratingText.textContent =
                    getRatingText(
                        selectedRating
                    );

            }
        );

    });



    function highlightStars(rating) {

        stars.forEach(
            function (star) {

                const starRating =
                    Number(
                        star.dataset.rating
                    );


                if (
                    starRating <= rating
                ) {

                    star.classList.add(
                        "selected"
                    );

                } else {

                    star.classList.remove(
                        "selected"
                    );

                }

            }
        );

    }



    function getRatingText(rating) {

        switch (rating) {

            case 1:
                return "⭐ Very Bad";

            case 2:
                return "⭐⭐ Bad";

            case 3:
                return "⭐⭐⭐ Average";

            case 4:
                return "⭐⭐⭐⭐ Good";

            case 5:
                return "⭐⭐⭐⭐⭐ Excellent";

            default:
                return "Select a rating";

        }

    }



    // ==========================================
    // REVIEW CHARACTER COUNT
    // ==========================================

    const review =
        document.getElementById(
            "review"
        );

    const charCount =
        document.getElementById(
            "charCount"
        );


    review.addEventListener(
        "input",
        function () {

            charCount.textContent =
                review.value.length;

        }
    );



    // ==========================================
    // SUBMIT RATING
    // ==========================================

    const submitBtn =
        document.getElementById(
            "submitRating"
        );

    const message =
        document.getElementById(
            "message"
        );


    submitBtn.addEventListener(
        "click",
        async function () {

            // ------------------------------
            // RATING CHECK
            // ------------------------------

            if (
                selectedRating < 1 ||
                selectedRating > 5
            ) {

                message.textContent =
                    "Please select a rating from 1 to 5 stars.";

                return;

            }


            // ------------------------------
            // REVIEW CHECK
            // ------------------------------

            const reviewText =
                review.value.trim();


            if (reviewText.length < 5) {

                message.textContent =
                    "Please write at least 5 characters in your review.";

                return;

            }


            // ------------------------------
            // OPPOSITE ROLE CHECK
            // ------------------------------

            if (
                (myRole === "worker" && targetType !== "boss") ||
                (myRole === "boss" && targetType !== "worker")
            ) {

                message.textContent =
                    "You can only rate the opposite role.";

                return;

            }


            // ------------------------------
            // SELF CHECK
            // ------------------------------

            if (
                String(myPhone) === String(targetPhone)
            ) {

                message.textContent =
                    "You cannot rate your own profile.";

                return;

            }



            // ------------------------------
            // BUTTON
            // ------------------------------

            submitBtn.disabled = true;

            submitBtn.textContent =
                "Submitting...";



            try {

                const response =
                    await fetch(
                        "/api/rating",
                        {
                            method: "POST",

                            headers: {
                                "Content-Type":
                                    "application/json"
                            },

                            body: JSON.stringify({

                                // Current logged-in user
                                raterType:
                                    myRole,

                                raterPhone:
                                    myPhone,

                                // Server will verify the
                                // actual name from account.
                                raterName:
                                    localStorage.getItem(
                                        "worklinkName"
                                    ) || "User",


                                // Profile that is being rated
                                targetType:
                                    targetType,

                                targetPhone:
                                    targetPhone,

                                targetName:
                                    targetName,


                                // IMPORTANT:
                                // Keep rating as STRING
                                rating:
                                    String(
                                        selectedRating
                                    ),


                                review:
                                    reviewText

                            })

                        }
                    );


                const data =
                    await response.json();



                // ------------------------------
                // SUCCESS
                // ------------------------------

                if (data.success) {

                    message.textContent =
                        "Rating submitted successfully!";


                    message.style.color =
                        "green";


                    submitBtn.textContent =
                        "Rating Submitted ✓";


                    // Return to the profile
                    // after successful rating.

                    setTimeout(
                        function () {

                            if (
                                targetType === "worker"
                            ) {

                                window.location.href =
                                    "worker-profile.html?phone=" +
                                    encodeURIComponent(
                                        targetPhone
                                    );

                            } else {

                                window.location.href =
                                    "boss-profile.html?phone=" +
                                    encodeURIComponent(
                                        targetPhone
                                    );

                            }

                        },
                        1000
                    );


                } else {

                    message.textContent =
                        data.message ||
                        "Unable to submit rating.";


                    message.style.color =
                        "red";


                    submitBtn.disabled =
                        false;


                    submitBtn.textContent =
                        "Submit Rating";

                }


            } catch (error) {

                console.error(
                    "Rating submit error:",
                    error
                );


                message.textContent =
                    "Server error. Please try again.";


                message.style.color =
                    "red";


                submitBtn.disabled =
                    false;


                submitBtn.textContent =
                    "Submit Rating";

            }

        }
    );

});