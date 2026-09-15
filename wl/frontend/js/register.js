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


        if (password.length < 6) {

            alert(
                "Password must contain at least 6 characters."
            );

            return;
        }


        if (role === "worker" && (document.getElementById("skill").value.trim() === "" || document.getElementById("location").value.trim() === "")) {
            alert("Please enter your skill and location.");
            return;
        }

        if (role === "boss" && document.getElementById("company").value.trim() === "") {
            alert("Please enter your company name.");
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

            const skill = document.getElementById("skill").value.trim();
            const location = document.getElementById("location").value.trim();


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

                                location: location,
                                email: email

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
                document.getElementById("company").value.trim();


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

document.addEventListener("DOMContentLoaded", function () {
    const role = document.getElementById("role");
    const companyGroup = document.getElementById("companyGroup");
    const skill = document.getElementById("skill").closest(".input-group");
    const location = document.getElementById("location").closest(".input-group");
    function toggle(){
        const boss = role.value === "boss";
        companyGroup.style.display = boss ? "block" : "none";
        skill.style.display = boss ? "none" : "block";
        location.style.display = boss ? "none" : "block";
    }
    role.addEventListener("change", toggle); toggle();
});
