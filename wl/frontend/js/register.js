document
    .getElementById("registerForm")
    .addEventListener("submit", async function(event) {

        event.preventDefault();


        // =========================
        // GET FORM DATA
        // =========================

        const name =
            document.getElementById("name").value.trim();

        const email =
            document.getElementById("email").value.trim();

        const phone =
            document.getElementById("phone").value.trim();

        const password =
            document.getElementById("password").value;

        const role =
            document.getElementById("role").value;


        // =========================
        // VALIDATION
        // =========================

        if (
            name === "" ||
            email === "" ||
            phone === "" ||
            password === ""
        ) {

            alert("Please fill all fields.");

            return;
        }


        if (phone.length !== 10) {

            alert(
                "Please enter a valid 10 digit phone number."
            );

            return;
        }


        if (password.length < 4) {

            alert(
                "Password must contain at least 4 characters."
            );

            return;
        }


        // =========================
        // REGISTER WORKER
        // =========================

        if (role === "worker") {

            /*
             * Current backend needs skill
             * and location.
             *
             * These are not present in the
             * current registration form.
             *
             * Temporary default values are used.
             * We will make these fields proper
             * later.
             */

            const skill = "Not specified";

            const location = "Not specified";


            try {

                const response =
                    await fetch(
                        API_BASE + "/api/register-worker",
                        {
                            method: "POST",

                            headers: {
                                "Content-Type":
                                    "application/json"
                            },

                            body: JSON.stringify({

                                name: name,

                                password: password,

                                phone: phone,

                                skill: skill,

                                location: location

                            })
                        }
                    );


                const data =
                    await response.json();


                if (data.success) {

                    alert(
                        "Registration successful!\n\n" +
                        "Welcome to WorkLink."
                    );


                    window.location.href =
                        "login.html";

                } else {

                    alert(
                        data.message ||
                        "Registration failed."
                    );
                }


            } catch (error) {

                console.error(
                    "Backend Error:",
                    error
                );


                alert(
                    "Java backend is not running.\n\n" +
                    "Please start the WorkLink backend first."
                );
            }


            return;
        }


        // =========================
        // REGISTER BOSS
        // =========================

        if (role === "boss") {

            const company =
                "Not specified";


            try {

                const response =
                    await fetch(
                        API_BASE + "/api/register-boss",
                        {
                            method: "POST",

                            headers: {
                                "Content-Type":
                                    "application/json"
                            },

                            body: JSON.stringify({

                                name: name,

                                password: password,

                                phone: phone,

                                company: company

                            })
                        }
                    );


                const data =
                    await response.json();


                if (data.success) {

                    alert(
                        "Boss registration successful!"
                    );


                    window.location.href =
                        "login.html";

                } else {

                    alert(
                        data.message ||
                        "Registration failed."
                    );
                }


            } catch (error) {

                console.error(
                    "Backend Error:",
                    error
                );


                alert(
                    "Java backend is not running."
                );
            }

        }

    });