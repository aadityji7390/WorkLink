import java.util.List;

public class Search {

    // ==============================
    // SEARCH WORKERS
    // ==============================

    public void searchWorkers(
            String keyword
    ) {

        List<String> workers =
                FileManager.getWorkers();


        boolean found = false;


        for (String worker : workers) {

            if (worker.trim().isEmpty()) {
                continue;
            }


            String[] data =
                    worker.split("\\|");


            if (data.length >= 5) {

                String name = data[0];
                String skill = data[3];
                String location = data[4];


                if (
                        name.toLowerCase()
                                .contains(
                                        keyword.toLowerCase()
                                )
                        ||
                        skill.toLowerCase()
                                .contains(
                                        keyword.toLowerCase()
                                )
                        ||
                        location.toLowerCase()
                                .contains(
                                        keyword.toLowerCase()
                                )
                ) {

                    System.out.println(
                            "Name: " + name
                    );

                    System.out.println(
                            "Skill: " + skill
                    );

                    System.out.println(
                            "Location: " + location
                    );

                    System.out.println(
                            "Phone: " + data[2]
                    );

                    System.out.println(
                            "----------------------"
                    );


                    found = true;
                }
            }
        }


        if (!found) {

            System.out.println(
                    "No worker found."
            );
        }
    }

}