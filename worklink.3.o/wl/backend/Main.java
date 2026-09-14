public class Main {

    public static void main(String[] args) {

        System.out.println(
                "================================"
        );

        System.out.println(
                "       WORKLINK SYSTEM"
        );

        System.out.println(
                "================================"
        );


        // ==============================
        // START FILE SYSTEM
        // ==============================

        FileManager.initialize();


        // ==============================
        // WORKER SERVICE
        // ==============================

        WorkersService workersService =
                new WorkersService();


        // ==============================
        // TEST REGISTRATION
        // ==============================

        boolean registered =
                workersService.registerWorker(
                        "Aaditya",
                        "12345",
                        "9876543210",
                        "Electrician",
                        "Lucknow"
                );


        if (registered) {

            System.out.println(
                    "Registration successful."
            );

        }


        // ==============================
        // TEST LOGIN
        // ==============================

        boolean login =
                workersService.loginWorker(
                        "9876543210",
                        "12345"
                );


        if (login) {

            System.out.println(
                    "Login successful."
            );

        }


        // ==============================
        // SEARCH WORKER
        // ==============================

        System.out.println(
                "\nSearching workers...\n"
        );

        workersService.searchBySkill(
                "Electrician"
        );

    }
}