import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

public class FileManager {

    private static final String DATA_FOLDER = "data";
    private static final String WORKERS_FILE = "data/worker.txt";
    private static final String BOSSES_FILE = "data/bosses.txt";
    private static final String RATINGS_FILE = "data/ratings.txt";


    public FileManager() {
    }


    // =========================================
    // INITIALIZE
    // =========================================

    public static void initialize() {

        try {

            Files.createDirectories(
                    Paths.get(DATA_FOLDER)
            );

            createFileIfNotExists(WORKERS_FILE);
            createFileIfNotExists(BOSSES_FILE);
            createFileIfNotExists(RATINGS_FILE);

            System.out.println(
                    "Data system initialized."
            );

            System.out.println(
                    "Worker file: "
                    + Paths.get(WORKERS_FILE)
                            .toAbsolutePath()
            );

        } catch (IOException e) {

            System.out.println(
                    "Error initializing data system: "
                    + e.getMessage()
            );
        }
    }


    private static void createFileIfNotExists(
            String file
    ) throws IOException {

        Path path =
                Paths.get(file);

        if (!Files.exists(
                path,
                new LinkOption[0]
        )) {

            Files.createFile(path);
        }
    }


    // =========================================
    // WORKER
    // =========================================

    public static boolean saveWorker(
            String name,
            String password,
            String phone,
            String skill,
            String location
    ) {

        String data =
                name + "|"
                + password + "|"
                + phone + "|"
                + skill + "|"
                + location + "|";

        return appendToFile(
                WORKERS_FILE,
                data
        );
    }


    public static List<String> getWorkers() {

        return readFile(
                WORKERS_FILE
        );
    }


    public static String getWorkerByPhone(
            String phone
    ) {

        if (phone == null) {
            return null;
        }

        for (String worker : getWorkers()) {

            if (
                    worker == null
                    ||
                    worker.trim().isEmpty()
            ) {
                continue;
            }

            String[] data =
                    worker.split("\\|", -1);

            if (
                    data.length >= 5
                    &&
                    data[2].trim()
                            .equals(phone.trim())
            ) {

                return worker;
            }
        }

        return null;
    }


    // =========================================
    // UPDATE WORKER
    // =========================================

    public static boolean updateWorker(
            String oldPhone,
            String name,
            String password,
            String phone,
            String skill,
            String location,
            String email
    ) {

        try {

            List<String> workers =
                    getWorkers();

            boolean found = false;


            for (
                    int i = 0;
                    i < workers.size();
                    i++
            ) {

                String worker =
                        workers.get(i);

                if (
                        worker == null
                        ||
                        worker.trim().isEmpty()
                ) {
                    continue;
                }

                String[] data =
                        worker.split("\\|", -1);


                if (
                        data.length >= 5
                        &&
                        data[2].trim()
                                .equals(oldPhone.trim())
                ) {

                    workers.set(
                            i,
                            name.trim()
                            + "|"
                            + password.trim()
                            + "|"
                            + phone.trim()
                            + "|"
                            + skill.trim()
                            + "|"
                            + location.trim()
                            + "|"
                            + email.trim()
                    );

                    found = true;
                    break;
                }
            }


            if (!found) {

                System.out.println(
                        "Worker not found."
                );

                return false;
            }


            Files.write(
                    Paths.get(WORKERS_FILE),
                    workers,
                    StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING
            );

            return true;


        } catch (IOException e) {

            System.out.println(
                    "Error updating worker: "
                    + e.getMessage()
            );

            return false;
        }
    }


    // =========================================
    // BOSS
    // =========================================

    public static boolean saveBoss(
            String name,
            String password,
            String phone,
            String company
    ) {

        String data =
                name + "|"
                + password + "|"
                + phone + "|"
                + company;

        return appendToFile(
                BOSSES_FILE,
                data
        );
    }


    public static List<String> getBosses() {

        return readFile(
                BOSSES_FILE
        );
    }


    public static String getBossByPhone(
            String phone
    ) {

        if (phone == null) {
            return null;
        }

        for (String boss : getBosses()) {

            if (
                    boss == null
                    ||
                    boss.trim().isEmpty()
            ) {
                continue;
            }

            String[] data =
                    boss.split("\\|", -1);

            if (
                    data.length >= 4
                    &&
                    data[2].trim()
                            .equals(phone.trim())
            ) {

                return boss;
            }
        }

        return null;
    }


    public static boolean saveAllBosses(
            List<String> bosses
    ) {

        try {

            Files.write(
                    Paths.get(BOSSES_FILE),
                    bosses,
                    StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING
            );

            return true;

        } catch (IOException e) {

            System.out.println(
                    "Error saving bosses: "
                    + e.getMessage()
            );

            return false;
        }
    }


    // =========================================
    // RATING
    // =========================================

    /*
     * New rating format:
     *
     * raterType|raterPhone|raterName|
     * targetType|targetPhone|targetName|
     * rating|review
     *
     * Example:
     *
     * worker|1111111111|Aaditya|boss|2222222222|Rahul|5|Good employer
     */


    public static boolean saveRating(
            String raterType,
            String raterPhone,
            String raterName,
            String targetType,
            String targetPhone,
            String targetName,
            int rating,
            String review
    ) {

        if (
                raterType == null
                ||
                raterPhone == null
                ||
                raterName == null
                ||
                targetType == null
                ||
                targetPhone == null
                ||
                targetName == null
        ) {

            return false;
        }


        // Pipe separator ko review/name me allow nahi karenge
        raterType =
                cleanRatingValue(raterType);

        raterPhone =
                cleanRatingValue(raterPhone);

        raterName =
                cleanRatingValue(raterName);

        targetType =
                cleanRatingValue(targetType);

        targetPhone =
                cleanRatingValue(targetPhone);

        targetName =
                cleanRatingValue(targetName);

        review =
                cleanRatingValue(review);


        String data =
                raterType + "|"
                + raterPhone + "|"
                + raterName + "|"
                + targetType + "|"
                + targetPhone + "|"
                + targetName + "|"
                + rating + "|"
                + review;


        return appendToFile(
                RATINGS_FILE,
                data
        );
    }


    /*
     * Old method kept so existing code does not break.
     *
     * Old format:
     * workerName|bossName|rating|review
     */

    public static boolean saveRating(
            String workerName,
            String bossName,
            int rating,
            String review
    ) {

        String data =
                cleanRatingValue(workerName)
                + "|"
                + cleanRatingValue(bossName)
                + "|"
                + rating
                + "|"
                + cleanRatingValue(review);

        return appendToFile(
                RATINGS_FILE,
                data
        );
    }


    public static List<String> getRatings() {

        return readFile(
                RATINGS_FILE
        );
    }


    // =========================================
    // GET RATINGS FOR WORKER
    // =========================================

    public static List<String> getRatingsForWorker(
            String workerName
    ) {

        List<String> ratings =
                getRatings();

        List<String> result =
                new ArrayList<>();


        if (workerName == null) {
            return result;
        }


        for (String rating : ratings) {

            if (
                    rating == null
                    ||
                    rating.trim().isEmpty()
            ) {
                continue;
            }


            String[] data =
                    rating.split("\\|", -1);


            // OLD FORMAT
            if (data.length >= 4) {

                if (data.length < 8) {

                    if (
                            data[0]
                                    .trim()
                                    .equalsIgnoreCase(
                                            workerName.trim()
                                    )
                    ) {

                        result.add(rating);
                    }

                    continue;
                }


                // NEW FORMAT
                if (
                        data[3]
                                .trim()
                                .equalsIgnoreCase("worker")
                        &&
                        data[5]
                                .trim()
                                .equalsIgnoreCase(
                                        workerName.trim()
                                )
                ) {

                    result.add(rating);
                }
            }
        }

        return result;
    }


    // =========================================
    // GET RATINGS FOR TARGET PHONE
    // =========================================

    public static List<String> getRatingsForTarget(
            String targetType,
            String targetPhone,
            String targetName
    ) {

        List<String> ratings =
                getRatings();

        List<String> result =
                new ArrayList<>();


        if (
                targetType == null
                ||
                targetPhone == null
        ) {

            return result;
        }


        for (String rating : ratings) {

            if (
                    rating == null
                    ||
                    rating.trim().isEmpty()
            ) {
                continue;
            }


            String[] data =
                    rating.split("\\|", -1);


            // NEW FORMAT
            if (data.length >= 8) {

                if (
                        data[3]
                                .trim()
                                .equalsIgnoreCase(
                                        targetType.trim()
                                )
                        &&
                        data[4]
                                .trim()
                                .equals(
                                        targetPhone.trim()
                                )
                ) {

                    result.add(rating);
                }

                continue;
            }


            // OLD FORMAT
            // Old records only supported workers.
            if (
                    targetType
                            .equalsIgnoreCase("worker")
                    &&
                    targetName != null
                    &&
                    data.length >= 4
                    &&
                    data[0]
                            .trim()
                            .equalsIgnoreCase(
                                    targetName.trim()
                            )
            ) {

                result.add(rating);
            }
        }


        return result;
    }


    // =========================================
    // AVERAGE RATING
    // =========================================

    public static double getAverageRating(
            String targetType,
            String targetPhone,
            String targetName
    ) {

        List<String> ratings =
                getRatingsForTarget(
                        targetType,
                        targetPhone,
                        targetName
                );


        int total = 0;
        int count = 0;


        for (String rating : ratings) {

            String[] data =
                    rating.split("\\|", -1);


            try {

                int value;


                if (data.length >= 8) {

                    value =
                            Integer.parseInt(
                                    data[6]
                            );

                } else if (data.length >= 4) {

                    value =
                            Integer.parseInt(
                                    data[2]
                            );

                } else {

                    continue;
                }


                if (
                        value >= 1
                        &&
                        value <= 5
                ) {

                    total += value;
                    count++;
                }


            } catch (NumberFormatException e) {

                // Ignore invalid rating
            }
        }


        if (count == 0) {
            return 0.0;
        }


        return (double) total / count;
    }


    // =========================================
    // RATING COUNT
    // =========================================

    public static int getRatingCount(
            String targetType,
            String targetPhone,
            String targetName
    ) {

        return getRatingsForTarget(
                targetType,
                targetPhone,
                targetName
        ).size();
    }


    // =========================================
    // OLD WORKER AVERAGE
    // =========================================

    public static double getWorkerAverageRating(
            String workerName
    ) {

        return getAverageRating(
                "worker",
                "",
                workerName
        );
    }


    // =========================================
    // FILE HELPERS
    // =========================================

    private static boolean appendToFile(
            String file,
            String data
    ) {

        try {

            Path parent =
                    Paths.get(file)
                            .getParent();

            if (parent != null) {

                Files.createDirectories(
                        parent
                );
            }


            Files.write(
                    Paths.get(file),
                    (
                            data
                            + System.lineSeparator()
                    ).getBytes(
                            StandardCharsets.UTF_8
                    ),
                    StandardOpenOption.CREATE,
                    StandardOpenOption.APPEND
            );

            return true;


        } catch (IOException e) {

            System.out.println(
                    "Error writing file: "
                    + e.getMessage()
            );

            return false;
        }
    }


    private static List<String> readFile(
            String file
    ) {

        try {

            Path path =
                    Paths.get(file);

            Path parent =
                    path.getParent();


            if (parent != null) {

                Files.createDirectories(
                        parent
                );
            }


            if (
                    !Files.exists(
                            path,
                            new LinkOption[0]
                    )
            ) {

                Files.createFile(path);
            }


            return Files.readAllLines(
                    path,
                    StandardCharsets.UTF_8
            );


        } catch (IOException e) {

            System.out.println(
                    "Error reading file: "
                    + e.getMessage()
            );

            return new ArrayList<>();
        }
    }


    private static String cleanRatingValue(
            String value
    ) {

        if (value == null) {
            return "";
        }

        return value
                .replace("|", "/")
                .replace("\r", " ")
                .replace("\n", " ")
                .trim();
    }


    // =========================================
    // CLEAR
    // =========================================

    public static void clearWorkers() {

        clearFile(WORKERS_FILE);
    }


    public static void clearBosses() {

        clearFile(BOSSES_FILE);
    }


    public static void clearRatings() {

        clearFile(RATINGS_FILE);
    }


    private static void clearFile(
            String file
    ) {

        try {

            Files.write(
                    Paths.get(file),
                    new byte[0],
                    StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING
            );

        } catch (IOException e) {

            System.out.println(
                    "Error clearing file: "
                    + e.getMessage()
            );
        }
    }
}