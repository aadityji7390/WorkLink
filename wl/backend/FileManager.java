import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
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
    private static final String JOBS_FILE = "data/jobs.txt";
    private static final String BOSS_AVAILABILITY_FILE = "data/boss_availability.txt";

    public static synchronized void initialize() {
        try {
            Files.createDirectories(Paths.get(DATA_FOLDER));
            createFileIfMissing(WORKERS_FILE);
            createFileIfMissing(BOSSES_FILE);
            createFileIfMissing(RATINGS_FILE);
            createFileIfMissing(JOBS_FILE);
            createFileIfMissing(BOSS_AVAILABILITY_FILE);
            createFileIfMissing("data/messages.txt");
            System.out.println("WorkLink data system initialized.");
        } catch (IOException e) {
            System.out.println("Data initialization error: " + e.getMessage());
        }
    }

    private static void createFileIfMissing(String file) throws IOException {
        Path p = Paths.get(file);
        if (!Files.exists(p)) Files.createFile(p);
    }

    public static synchronized boolean saveWorker(String name, String password, String phone, String skill, String location) {
        return saveWorker(name, password, phone, skill, location, "");
    }

    public static synchronized boolean saveWorker(String name, String password, String phone, String skill, String location, String email) {
        String data = safe(name) + "|" + safe(password) + "|" + safe(phone) + "|" + safe(skill) + "|" + safe(location) + "|" + safe(email);
        return appendToFile(WORKERS_FILE, data);
    }

    public static List<String> getWorkers() { return readFile(WORKERS_FILE); }

    public static String getWorkerByPhone(String phone) {
        if (phone == null) return null;
        for (String row : getWorkers()) {
            String[] d = split(row);
            if (d.length >= 5 && d[2].trim().equals(phone.trim())) return row;
        }
        return null;
    }

    public static synchronized boolean updateWorker(String oldPhone, String name, String password, String phone, String skill, String location, String email) {
        List<String> rows = getWorkers();
        boolean found = false;
        for (int i = 0; i < rows.size(); i++) {
            String[] d = split(rows.get(i));
            if (d.length >= 5 && d[2].trim().equals(oldPhone.trim())) {
                rows.set(i, safe(name) + "|" + safe(password) + "|" + safe(phone) + "|" + safe(skill) + "|" + safe(location) + "|" + safe(email));
                found = true;
                break;
            }
        }
        return found && writeFile(WORKERS_FILE, rows);
    }

    public static synchronized boolean replaceWorkerPassword(String phone, String newPassword) {
        List<String> rows = getWorkers();
        for (int i = 0; i < rows.size(); i++) {
            String[] d = split(rows.get(i));
            if (d.length >= 5 && d[2].trim().equals(phone.trim())) {
                String email = d.length >= 6 ? d[5] : "";
                rows.set(i, safe(d[0]) + "|" + safe(newPassword) + "|" + safe(d[2]) + "|" + safe(d[3]) + "|" + safe(d[4]) + "|" + safe(email));
                return writeFile(WORKERS_FILE, rows);
            }
        }
        return false;
    }

    public static synchronized boolean saveBoss(String name, String password, String phone, String company) {
        return appendToFile(BOSSES_FILE, safe(name) + "|" + safe(password) + "|" + safe(phone) + "|" + safe(company));
    }

    public static List<String> getBosses() { return readFile(BOSSES_FILE); }

    public static String getBossByPhone(String phone) {
        if (phone == null) return null;
        for (String row : getBosses()) {
            String[] d = split(row);
            if (d.length >= 4 && d[2].trim().equals(phone.trim())) return row;
        }
        return null;
    }

    public static synchronized boolean saveAllBosses(List<String> bosses) { return writeFile(BOSSES_FILE, bosses); }

    public static synchronized boolean replaceBossPassword(String phone, String newPassword) {
        List<String> rows = getBosses();
        for (int i = 0; i < rows.size(); i++) {
            String[] d = split(rows.get(i));
            if (d.length >= 4 && d[2].trim().equals(phone.trim())) {
                rows.set(i, safe(d[0]) + "|" + safe(newPassword) + "|" + safe(d[2]) + "|" + safe(d[3]));
                return writeFile(BOSSES_FILE, rows);
            }
        }
        return false;
    }

    public static synchronized boolean saveRating(String raterType, String raterPhone, String raterName,
                                                   String targetType, String targetPhone, String targetName,
                                                   int rating, String review) {
        if (rating < 1 || rating > 5) return false;
        String data = safe(raterType) + "|" + safe(raterPhone) + "|" + safe(raterName) + "|" +
                safe(targetType) + "|" + safe(targetPhone) + "|" + safe(targetName) + "|" + rating + "|" + safe(review);
        return appendToFile(RATINGS_FILE, data);
    }

    public static boolean saveRating(String workerName, String bossName, int rating, String review) {
        return appendToFile(RATINGS_FILE, safe(workerName) + "|" + safe(bossName) + "|" + rating + "|" + safe(review));
    }

    public static List<String> getRatings() { return readFile(RATINGS_FILE); }

    public static List<String> getRatingsForTarget(String type, String phone, String name) {
        List<String> out = new ArrayList<>();
        for (String row : getRatings()) {
            String[] d = split(row);
            if (d.length >= 8 && d[3].equalsIgnoreCase(type) && d[4].equals(phone)) out.add(row);
            else if (d.length == 4 && type.equalsIgnoreCase("worker") && d[0].equalsIgnoreCase(name)) out.add(row);
        }
        return out;
    }

    public static double getAverageRating(String type, String phone, String name) {
        List<String> rows = getRatingsForTarget(type, phone, name);
        double total = 0; int count = 0;
        for (String row : rows) {
            String[] d = split(row);
            try {
                int r = d.length >= 8 ? Integer.parseInt(d[6]) : Integer.parseInt(d[2]);
                if (r >= 1 && r <= 5) { total += r; count++; }
            } catch (Exception ignored) {}
        }
        return count == 0 ? 0 : total / count;
    }

    public static double getWorkerAverageRating(String workerName) { return getAverageRating("worker", "", workerName); }

    // Job format: id|bossPhone|bossName|company|title|category|location|salary|type|description|status|createdAt
    public static synchronized boolean saveJob(String id, String bossPhone, String bossName, String company,
                                                String title, String category, String location, String salary,
                                                String type, String description, String status, String createdAt) {
        String row = String.join("|", safe(id), safe(bossPhone), safe(bossName), safe(company), safe(title), safe(category),
                safe(location), safe(salary), safe(type), safe(description), safe(status), safe(createdAt));
        return appendToFile(JOBS_FILE, row);
    }

    public static List<String> getJobs() { return readFile(JOBS_FILE); }

    // Boss availability is stored separately so the existing bosses.txt format stays unchanged.
    public static boolean isBossAvailable(String phone) {
        if (phone == null || phone.isBlank()) return true;
        for (String row : readFile(BOSS_AVAILABILITY_FILE)) {
            String[] d = split(row);
            if (d.length >= 2 && d[0].trim().equals(phone.trim())) return !d[1].equalsIgnoreCase("unavailable");
        }
        return true;
    }

    public static synchronized boolean setBossAvailability(String phone, boolean available) {
        if (phone == null || phone.isBlank()) return false;
        List<String> rows = readFile(BOSS_AVAILABILITY_FILE);
        String value = available ? "available" : "unavailable";
        boolean found = false;
        for (int i = 0; i < rows.size(); i++) {
            String[] d = split(rows.get(i));
            if (d.length >= 2 && d[0].trim().equals(phone.trim())) {
                rows.set(i, safe(phone) + "|" + value);
                found = true;
                break;
            }
        }
        if (!found) rows.add(safe(phone) + "|" + value);
        return writeFile(BOSS_AVAILABILITY_FILE, rows);
    }

    public static String getJobById(String id) {
        for (String row : getJobs()) {
            String[] d = split(row);
            if (d.length >= 12 && d[0].equals(id)) return row;
        }
        return null;
    }

    public static synchronized boolean updateJobStatus(String id, String status) {
        List<String> rows = getJobs();
        for (int i = 0; i < rows.size(); i++) {
            String[] d = split(rows.get(i));
            if (d.length >= 12 && d[0].equals(id)) {
                d[10] = safe(status);
                rows.set(i, String.join("|", d));
                return writeFile(JOBS_FILE, rows);
            }
        }
        return false;
    }

    public static synchronized boolean saveMessage(String id, String senderType, String senderPhone, String senderName,
                                                     String receiverType, String receiverPhone, String receiverName,
                                                     String message, String createdAt) {
        String row = String.join("|", safe(id), safe(senderType), safe(senderPhone), safe(senderName),
                safe(receiverType), safe(receiverPhone), safe(receiverName), safe(message), safe(createdAt));
        return appendToFile("data/messages.txt", row);
    }

    public static List<String> getMessages() { return readFile("data/messages.txt"); }

    private static String safe(String value) {
        if (value == null) return "";
        return value.replace("|", "/").replace("\r", " ").replace("\n", " ").trim();
    }

    public static String[] split(String row) { return row == null ? new String[0] : row.split("\\|", -1); }

    private static synchronized boolean appendToFile(String file, String data) {
        try {
            Files.write(Paths.get(file), List.of(data), StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
            return true;
        } catch (IOException e) {
            System.out.println("File write error: " + e.getMessage());
            return false;
        }
    }

    private static synchronized boolean writeFile(String file, List<String> rows) {
        try {
            Files.write(Paths.get(file), rows, StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            return true;
        } catch (IOException e) {
            System.out.println("File save error: " + e.getMessage());
            return false;
        }
    }

    private static List<String> readFile(String file) {
        try { return new ArrayList<>(Files.readAllLines(Paths.get(file), StandardCharsets.UTF_8)); }
        catch (IOException e) { return new ArrayList<>(); }
    }
}
