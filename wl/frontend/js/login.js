document.addEventListener("DOMContentLoaded", function () {

    const API_BASE = window.location.origin;

    const loginForm = document.getElementById("loginForm");
    const loginBtn = document.getElementById("loginBtn");
    const loginMessage = document.getElementById("loginMessage");

    loginForm.addEventListener("submit", async function (event) {

        event.preventDefault();

        const phone = document.getElementById("phone").value.trim();
        const password = document.getElementById("password").value;

        const roleInput = document.querySelector(
            'input[name="role"]:checked'
        );

        const role = roleInput ? roleInput.value : "";

        // ==============================
        // VALIDATION
        // ==============================

        if (!/^[0-9]{10}$/.test(phone)) {
            loginMessage.textContent =
                "Please enter a valid 10-digit phone number.";
            loginMessage.style.color = "red";
            return;
        }

        if (!password) {
            loginMessage.textContent =
                "Please enter your password.";
            loginMessage.style.color = "red";
            return;
        }

        if (role !== "worker" && role !== "boss") {
            loginMessage.textContent =
                "Please select Worker or Boss.";
            loginMessage.style.color = "red";
            return;
        }

        // ==============================
        // LOGIN BUTTON
        // ==============================

        loginBtn.disabled = true;
        loginBtn.textContent = "Logging in...";
        loginMessage.textContent = "";

        try {

            // ==============================
            // LOGIN API
            // ==============================

            const response = await fetch(
                API_BASE + "/api/login-" + role,
                {
                    method: "POST",

                    headers: {
                        "Content-Type": "application/json"
                    },

                    body: JSON.stringify({
                        phone: phone,
                        password: password
                    })
                }
            );

            const data = await response.json();

            // ==============================
            // LOGIN FAILED
            // ==============================

            if (!data.success) {

                loginMessage.textContent =
                    data.message ||
                    "Invalid phone number or password.";

                loginMessage.style.color = "red";

                loginBtn.disabled = false;
                loginBtn.textContent = "Login";

                return;
            }

            // ==============================
            // SAVE LOGIN SESSION
            // ==============================

            localStorage.setItem(
                "worklinkLoggedIn",
                "true"
            );

            localStorage.setItem(
                "worklinkRole",
                role
            );

            localStorage.setItem(
                "worklinkPhone",
                phone
            );

            localStorage.removeItem(
                "worklinkName"
            );

            // ==============================
            // GET USER NAME
            // ==============================

            try {

                let profileUrl;

                if (role === "worker") {

                    profileUrl =
                        API_BASE +
                        "/api/worker/profile?phone=" +
                        encodeURIComponent(phone);

                } else {

                    profileUrl =
                        API_BASE +
                        "/api/boss/profile?phone=" +
                        encodeURIComponent(phone);
                }

                const profileResponse =
                    await fetch(profileUrl);

                const profileData =
                    await profileResponse.json();

                if (
                    profileData.success &&
                    profileData.name
                ) {

                    localStorage.setItem(
                        "worklinkName",
                        profileData.name
                    );
                }

            } catch (profileError) {

                console.warn(
                    "Could not load profile name:",
                    profileError
                );
            }

            // ==============================
            // SUCCESS
            // ==============================

            loginMessage.textContent =
                "Login successful!";

            loginMessage.style.color =
                "green";

            // ==============================
            // REDIRECT
            // ==============================

            setTimeout(function () {

                if (role === "worker") {

                    window.location.href =
                        "worker-dashboard.html";

                } else {

                    window.location.href =
                        "boss-dashboard.html";
                }

            }, 400);

        } catch (error) {

            console.error(
                "Login error:",
                error
            );

            loginMessage.textContent =
                "Server is not responding. Please make sure the Java server is running.";

            loginMessage.style.color =
                "red";

            loginBtn.disabled = false;
            loginBtn.textContent = "Login";
        }

    });

});