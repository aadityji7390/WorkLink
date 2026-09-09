import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.net.URLDecoder;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class Server {

    private static final int PORT = Integer.parseInt(System.getenv().getOrDefault("PORT", "8080"));

    public static void main(String[] args) throws Exception {

        // =========================================
        // INITIALIZE FILE SYSTEM
        // =========================================

        FileManager.initialize();


        // =========================================
        // CREATE SERVER
        // =========================================

        HttpServer server = HttpServer.create(
                new InetSocketAddress("0.0.0.0", PORT),
                0
        );


        // =========================================
        // ROUTES
        // =========================================

        server.createContext("/", Server::home);

        server.createContext(
                "/api/test",
                Server::test
        );

        // Worker Register
        server.createContext(
                "/api/register-worker",
                Server::registerWorker
        );

        // Worker Login
        server.createContext(
                "/api/login-worker",
                Server::loginWorker
        );

        // Boss Register
        server.createContext(
                "/api/register-boss",
                Server::registerBoss
        );

        // Boss Login
        server.createContext(
                "/api/login-boss",
                Server::loginBoss
        );


        // =========================================
        // WORKER PROFILE
        // =========================================

        server.createContext(
                "/api/worker/profile",
                Server::workerProfile
        );

        server.createContext(
                "/api/worker/update",
                Server::updateWorkerProfile
        );


        // =========================================
        // BOSS PROFILE
        // =========================================

        server.createContext(
                "/api/boss/profile",
                Server::bossProfile
        );

        server.createContext(
                "/api/boss/update",
                Server::updateBossProfile
        );


        // =========================================
        // SEARCH WORKERS
        // =========================================

        server.createContext(
                "/api/workers/search",
                Server::searchWorkers
        );
        // =========================================
// SEARCH BOSSES
// =========================================

server.createContext(
        "/api/bosses/search",
        Server::searchBosses
);

        // =========================================
        // RATING
        // =========================================

        server.createContext(
                "/api/rating",
                Server::rating
        );


        // =========================================
        // SERVER EXECUTOR
        // =========================================

        server.setExecutor(null);


        // =========================================
        // START SERVER
        // =========================================

        System.out.println();
        System.out.println("================================");
        System.out.println("       WORKLINK BACKEND");
        System.out.println("================================");

        System.out.println(
                "Local:   http://localhost:" + PORT
        );

        System.out.println(
                "Test:    http://localhost:" + PORT + "/api/test"
        );

        System.out.println(
                "Network: http://0.0.0.0:" + PORT
        );

        System.out.println("================================");
        System.out.println("Backend is READY.");
        System.out.println("Do not close this terminal.");
        System.out.println("================================");
        System.out.println();

        server.start();
    }


    // =========================================
    // HOME PAGE
    // =========================================

    private static void home(
            HttpExchange exchange
    ) throws IOException {
        serveStatic(exchange);
    }

    private static void serveStatic(HttpExchange exchange) throws IOException {
        if (handleOptions(exchange)) return;
        if (!exchange.getRequestMethod().equalsIgnoreCase("GET")) {
            sendResponse(exchange, 405, "Method Not Allowed");
            return;
        }

        String requestPath = exchange.getRequestURI().getPath();
        if (requestPath == null || requestPath.equals("/")) requestPath = "/index.html";
        requestPath = URLDecoder.decode(requestPath, StandardCharsets.UTF_8);

        Path frontendRoot = Paths.get("../frontend").toAbsolutePath().normalize();
        Path requested = frontendRoot.resolve(requestPath.substring(1)).normalize();

        if (!requested.startsWith(frontendRoot)) {
            sendResponse(exchange, 403, "Forbidden");
            return;
        }

        if (!Files.exists(requested) || Files.isDirectory(requested)) {
            sendResponse(exchange, 404, "Not Found");
            return;
        }

        byte[] data = Files.readAllBytes(requested);
        String contentType = contentType(requested.getFileName().toString());
        exchange.getResponseHeaders().set("Content-Type", contentType);
        exchange.getResponseHeaders().set("Cache-Control", "no-cache");
        exchange.sendResponseHeaders(200, data.length);
        try (OutputStream out = exchange.getResponseBody()) { out.write(data); }
    }

    private static String contentType(String name) {
        String n = name.toLowerCase();
        if (n.endsWith(".html")) return "text/html; charset=UTF-8";
        if (n.endsWith(".css")) return "text/css; charset=UTF-8";
        if (n.endsWith(".js")) return "application/javascript; charset=UTF-8";
        if (n.endsWith(".json")) return "application/json; charset=UTF-8";
        if (n.endsWith(".png")) return "image/png";
        if (n.endsWith(".jpg") || n.endsWith(".jpeg")) return "image/jpeg";
        if (n.endsWith(".svg")) return "image/svg+xml";
        return "application/octet-stream";
    }


    // =========================================
    // TEST API
    // =========================================

    private static void test(
            HttpExchange exchange
    ) throws IOException {

        if (handleOptions(exchange)) {
            return;
        }

        if (!exchange.getRequestMethod()
                .equalsIgnoreCase("GET")) {

            sendResponse(
                    exchange,
                    405,
                    "{\"success\":false,\"message\":\"GET method required\"}"
            );

            return;
        }

        sendResponse(
                exchange,
                200,
                "{\"success\":true,\"message\":\"WorkLink Java Backend is running\"}"
        );
    }


    // =========================================
    // WORKER REGISTER
    // =========================================

    private static void registerWorker(
            HttpExchange exchange
    ) throws IOException {

        if (handleOptions(exchange)) {
            return;
        }

        if (!exchange.getRequestMethod()
                .equalsIgnoreCase("POST")) {

            sendResponse(
                    exchange,
                    405,
                    "{\"success\":false,\"message\":\"POST method required\"}"
            );

            return;
        }

        String body =
                readBody(exchange);

        String name =
                getValue(body, "name");

        String password =
                getValue(body, "password");

        String phone =
                getValue(body, "phone");

        String skill =
                getValue(body, "skill");

        String location =
                getValue(body, "location");

        if (
                name.trim().isEmpty()
                ||
                password.trim().isEmpty()
                ||
                phone.trim().isEmpty()
                ||
                skill.trim().isEmpty()
                ||
                location.trim().isEmpty()
        ) {

            sendResponse(
                    exchange,
                    400,
                    "{\"success\":false,\"message\":\"All fields are required\"}"
            );

            return;
        }

        WorkersService workersService =
                new WorkersService();

        if (workersService.phoneExists(phone.trim())) {

            sendResponse(
                    exchange,
                    409,
                    "{\"success\":false,\"message\":\"Phone number already exists\"}"
            );

            return;
        }

        Login login =
                new Login();

        boolean result =
                login.workerRegister(
                        name.trim(),
                        password.trim(),
                        phone.trim(),
                        skill.trim(),
                        location.trim()
                );

        if (result) {

            sendResponse(
                    exchange,
                    200,
                    "{\"success\":true,\"message\":\"Worker registered successfully\"}"
            );

        } else {

            sendResponse(
                    exchange,
                    400,
                    "{\"success\":false,\"message\":\"Worker registration failed\"}"
            );
        }
    }


    // =========================================
    // WORKER LOGIN
    // =========================================

    private static void loginWorker(
            HttpExchange exchange
    ) throws IOException {

        if (handleOptions(exchange)) {
            return;
        }

        if (!exchange.getRequestMethod()
                .equalsIgnoreCase("POST")) {

            sendResponse(
                    exchange,
                    405,
                    "{\"success\":false,\"message\":\"POST method required\"}"
            );

            return;
        }

        String body =
                readBody(exchange);

        String phone =
                getValue(body, "phone");

        String password =
                getValue(body, "password");

        Login login =
                new Login();

        boolean result =
                login.workerLogin(
                        phone,
                        password
                );

        if (result) {

            sendResponse(
                    exchange,
                    200,
                    "{\"success\":true,\"message\":\"Worker login successful\"}"
            );

        } else {

            sendResponse(
                    exchange,
                    401,
                    "{\"success\":false,\"message\":\"Invalid phone or password\"}"
            );
        }
    }


    // =========================================
    // BOSS REGISTER
    // =========================================

    private static void registerBoss(
            HttpExchange exchange
    ) throws IOException {

        if (handleOptions(exchange)) {
            return;
        }

        if (!exchange.getRequestMethod()
                .equalsIgnoreCase("POST")) {

            sendResponse(
                    exchange,
                    405,
                    "{\"success\":false,\"message\":\"POST method required\"}"
            );

            return;
        }

        String body =
                readBody(exchange);

        String name =
                getValue(body, "name");

        String password =
                getValue(body, "password");

        String phone =
                getValue(body, "phone");

        String company =
                getValue(body, "company");

        Login login =
                new Login();

        boolean result =
                login.bossRegister(
                        name,
                        password,
                        phone,
                        company
                );

        if (result) {

            sendResponse(
                    exchange,
                    200,
                    "{\"success\":true,\"message\":\"Boss registered successfully\"}"
            );

        } else {

            sendResponse(
                    exchange,
                    400,
                    "{\"success\":false,\"message\":\"Boss registration failed\"}"
            );
        }
    }


    // =========================================
    // BOSS LOGIN
    // =========================================

    private static void loginBoss(
            HttpExchange exchange
    ) throws IOException {

        if (handleOptions(exchange)) {
            return;
        }

        if (!exchange.getRequestMethod()
                .equalsIgnoreCase("POST")) {

            sendResponse(
                    exchange,
                    405,
                    "{\"success\":false,\"message\":\"POST method required\"}"
            );

            return;
        }

        String body =
                readBody(exchange);

        String phone =
                getValue(body, "phone");

        String password =
                getValue(body, "password");

        Login login =
                new Login();

        boolean result =
                login.bossLogin(
                        phone,
                        password
                );

        if (result) {

            sendResponse(
                    exchange,
                    200,
                    "{\"success\":true,\"message\":\"Boss login successful\"}"
            );

        } else {

            sendResponse(
                    exchange,
                    401,
                    "{\"success\":false,\"message\":\"Invalid phone or password\"}"
            );
        }
    }


    // =========================================
    // GET WORKER PROFILE
    // =========================================

    private static void workerProfile(
            HttpExchange exchange
    ) throws IOException {

        if (handleOptions(exchange)) {
            return;
        }

        if (!exchange.getRequestMethod()
                .equalsIgnoreCase("GET")) {

            sendResponse(
                    exchange,
                    405,
                    "{\"success\":false,\"message\":\"GET method required\"}"
            );

            return;
        }

        String query =
                exchange.getRequestURI()
                        .getQuery();

        String phone =
                getQueryValue(
                        query,
                        "phone"
                );

        if (phone.isEmpty()) {

            sendResponse(
                    exchange,
                    400,
                    "{\"success\":false,\"message\":\"Phone is required\"}"
            );

            return;
        }

        WorkersService service =
                new WorkersService();

        String worker =
                service.getWorkerProfile(phone);

        if (worker == null) {

            sendResponse(
                    exchange,
                    404,
                    "{\"success\":false,\"message\":\"Worker not found\"}"
            );

            return;
        }

        String[] data =
                worker.split("\\|", -1);

        if (data.length < 5) {

            sendResponse(
                    exchange,
                    500,
                    "{\"success\":false,\"message\":\"Invalid worker data\"}"
            );

            return;
        }

        String email = "";

        if (data.length >= 6) {
            email = data[5].trim();
        }

        String response =
                "{"
                + "\"success\":true,"
                + "\"name\":\""
                + escapeJson(data[0])
                + "\","
                + "\"phone\":\""
                + escapeJson(data[2])
                + "\","
                + "\"skill\":\""
                + escapeJson(data[3])
                + "\","
                + "\"location\":\""
                + escapeJson(data[4])
                + "\","
                + "\"email\":\""
                + escapeJson(email)
                + "\""
                + "}";

        sendResponse(
                exchange,
                200,
                response
        );
    }


    // =========================================
    // GET BOSS PROFILE
    // =========================================

    private static void bossProfile(
            HttpExchange exchange
    ) throws IOException {

        if (handleOptions(exchange)) {
            return;
        }

        if (!exchange.getRequestMethod()
                .equalsIgnoreCase("GET")) {

            sendResponse(
                    exchange,
                    405,
                    "{\"success\":false,\"message\":\"GET method required\"}"
            );

            return;
        }

        String query =
                exchange.getRequestURI()
                        .getQuery();

        String phone =
                getQueryValue(
                        query,
                        "phone"
                );

        if (phone.isEmpty()) {

            sendResponse(
                    exchange,
                    400,
                    "{\"success\":false,\"message\":\"Phone is required\"}"
            );

            return;
        }

        BossService service =
                new BossService();

        String boss =
                service.getBossProfile(phone);

        if (boss == null) {

            sendResponse(
                    exchange,
                    404,
                    "{\"success\":false,\"message\":\"Boss not found\"}"
            );

            return;
        }

        String[] data =
                boss.split("\\|", -1);

        if (data.length < 4) {

            sendResponse(
                    exchange,
                    500,
                    "{\"success\":false,\"message\":\"Invalid boss data\"}"
            );

            return;
        }

        String response =
                "{"
                + "\"success\":true,"
                + "\"name\":\""
                + escapeJson(data[0])
                + "\","
                + "\"phone\":\""
                + escapeJson(data[2])
                + "\","
                + "\"company\":\""
                + escapeJson(data[3])
                + "\""
                + "}";

        sendResponse(
                exchange,
                200,
                response
        );
    }


    // =========================================
    // UPDATE BOSS PROFILE
    // =========================================

    private static void updateBossProfile(
            HttpExchange exchange
    ) throws IOException {

        if (handleOptions(exchange)) {
            return;
        }

        if (!exchange.getRequestMethod()
                .equalsIgnoreCase("POST")) {

            sendResponse(
                    exchange,
                    405,
                    "{\"success\":false,\"message\":\"POST method required\"}"
            );

            return;
        }

        String body =
                readBody(exchange);

        String oldPhone =
                getValue(
                        body,
                        "oldPhone"
                );

        String name =
                getValue(
                        body,
                        "name"
                );

        String password =
                getValue(
                        body,
                        "password"
                );

        String phone =
                getValue(
                        body,
                        "phone"
                );

        String company =
                getValue(
                        body,
                        "company"
                );
if (
        oldPhone.isEmpty()
        ||
        name.isEmpty()
        ||
        password.isEmpty()
        ||
        phone.isEmpty()
        ||
        company.isEmpty()
) {

    sendResponse(
            exchange,
            400,
            "{\"success\":false,\"message\":\"Name, password, phone and company are required\"}"
    );

    return;


        }

        BossService service =
                new BossService();

        boolean result =
                service.updateBossProfile(
                        oldPhone.trim(),
                        name.trim(),
                        phone.trim(),
                        company.trim(),
                        password.trim()
                );

        if (result) {

            sendResponse(
                    exchange,
                    200,
                    "{\"success\":true,\"message\":\"Boss profile updated successfully\"}"
            );

        } else {

            sendResponse(
                    exchange,
                    400,
                    "{\"success\":false,\"message\":\"Boss profile update failed\"}"
            );
        }
    }


    // =========================================
    // SEARCH WORKERS
    // =========================================

    private static void searchWorkers(
            HttpExchange exchange
    ) throws IOException {

        if (handleOptions(exchange)) {
            return;
        }

        if (!exchange.getRequestMethod()
                .equalsIgnoreCase("GET")) {

            sendResponse(
                    exchange,
                    405,
                    "{\"success\":false,\"message\":\"GET method required\"}"
            );

            return;
        }

        String query =
                exchange.getRequestURI()
                        .getQuery();

        String search =
                getQueryValue(
                        query,
                        "q"
                );

        if (search.isEmpty()) {

            search =
                    getQueryValue(
                            query,
                            "search"
                    );
        }

        search =
                search.toLowerCase().trim();

        String location =
                getQueryValue(
                        query,
                        "location"
                )
                .toLowerCase()
                .trim();

        List<String> workers =
                new WorkersService()
                        .getAllWorkers();

        StringBuilder response =
                new StringBuilder();

        response.append(
                "{\"success\":true,\"workers\":["
        );

        boolean first = true;

        for (String worker : workers) {

            if (
                    worker == null
                    ||
                    worker.trim().isEmpty()
            ) {
                continue;
            }

            String[] data =
                    worker.split("\\|", -1);

            if (data.length < 5) {
                continue;
            }

            String name =
                    data[0].trim();

            String phone =
                    data[2].trim();

            String skill =
                    data[3].trim();

            String workerLocation =
                    data[4].trim();

            String email = "";

            if (data.length >= 6) {
                email = data[5].trim();
            }

            boolean matchesSearch =
                    search.isEmpty()
                    ||
                    name.toLowerCase()
                            .contains(search)
                    ||
                    skill.toLowerCase()
                            .contains(search)
                    ||
                    workerLocation.toLowerCase()
                            .contains(search);

            boolean matchesLocation =
                    location.isEmpty()
                    ||
                    workerLocation
                            .toLowerCase()
                            .contains(location);

            if (
                    !matchesSearch
                    ||
                    !matchesLocation
            ) {
                continue;
            }

            if (!first) {
                response.append(",");
            }

            response.append("{");

            response.append(
                    "\"name\":\""
            );

            response.append(
                    escapeJson(name)
            );

            response.append("\",");

            response.append(
                    "\"phone\":\""
            );

            response.append(
                    escapeJson(phone)
            );

            response.append("\",");

            response.append(
                    "\"skill\":\""
            );

            response.append(
                    escapeJson(skill)
            );

            response.append("\",");

            response.append(
                    "\"location\":\""
            );

            response.append(
                    escapeJson(workerLocation)
            );

            response.append("\",");

            response.append(
                    "\"email\":\""
            );

            response.append(
                    escapeJson(email)
            );

            response.append("\"");

            response.append("}");

            first = false;
        }

        response.append("]}");

        sendResponse(
                exchange,
                200,
                response.toString()
        );
    }
    // =========================================
// SEARCH BOSSES
// =========================================

private static void searchBosses(
        HttpExchange exchange
) throws IOException {

    if (handleOptions(exchange)) {
        return;
    }


    if (
            !exchange.getRequestMethod()
                    .equalsIgnoreCase("GET")
    ) {

        sendResponse(
                exchange,
                405,
                "{\"success\":false,\"message\":\"GET method required\"}"
        );

        return;
    }


    String query =
            exchange.getRequestURI()
                    .getQuery();


    String search =
            getQueryValue(
                    query,
                    "q"
            );


    search =
            search.toLowerCase().trim();


    List<String> bosses =
            FileManager.getBosses();


    StringBuilder response =
            new StringBuilder();


    response.append(
            "{\"success\":true,\"bosses\":["
    );


    boolean first = true;


    for (String boss : bosses) {

        if (
                boss == null
                ||
                boss.trim().isEmpty()
        ) {
            continue;
        }


        String[] data =
                boss.split("\\|", -1);


        if (data.length < 4) {
            continue;
        }


        String name =
                data[0].trim();

        String phone =
                data[2].trim();

        String company =
                data[3].trim();


        boolean matches =
                search.isEmpty()
                ||
                name.toLowerCase()
                        .contains(search)
                ||
                company.toLowerCase()
                        .contains(search);


        if (!matches) {
            continue;
        }


        if (!first) {
            response.append(",");
        }


        response.append("{");


        response.append(
                "\"name\":\""
        );

        response.append(
                escapeJson(name)
        );

        response.append("\",");


        response.append(
                "\"phone\":\""
        );

        response.append(
                escapeJson(phone)
        );

        response.append("\",");


        response.append(
                "\"company\":\""
        );

        response.append(
                escapeJson(company)
        );


        response.append("\"");


        response.append("}");


        first = false;
    }


    response.append("]}");


    sendResponse(
            exchange,
            200,
            response.toString()
    );
}


 // =========================================
// RATING API
// =========================================

private static void rating(
        HttpExchange exchange
) throws IOException {

    if (handleOptions(exchange)) {
        return;
    }


    // =====================================
    // POST = SAVE RATING
    // =====================================

    if (
            exchange.getRequestMethod()
                    .equalsIgnoreCase("POST")
    ) {

        String body =
                readBody(exchange);


        String raterType =
                getValue(
                        body,
                        "raterType"
                );

        String raterPhone =
                getValue(
                        body,
                        "raterPhone"
                );

        String raterName =
                getValue(
                        body,
                        "raterName"
                );

        String targetType =
                getValue(
                        body,
                        "targetType"
                );

        String targetPhone =
                getValue(
                        body,
                        "targetPhone"
                );

        String targetName =
                getValue(
                        body,
                        "targetName"
                );

        String ratingText =
                getValue(
                        body,
                        "rating"
                );

        String review =
                getValue(
                        body,
                        "review"
                );


        // =====================================
        // BASIC VALIDATION
        // =====================================

        if (
                raterType.isEmpty()
                ||
                raterPhone.isEmpty()
                ||
                raterName.isEmpty()
                ||
                targetType.isEmpty()
                ||
                targetPhone.isEmpty()
                ||
                targetName.isEmpty()
                ||
                ratingText.isEmpty()
                ||
                review.trim().isEmpty()
        ) {

            sendResponse(
                    exchange,
                    400,
                    "{\"success\":false,\"message\":\"All rating fields are required\"}"
            );

            return;
        }


        // =====================================
        // VALID ROLE
        // =====================================

        if (
                !raterType.equalsIgnoreCase("worker")
                &&
                !raterType.equalsIgnoreCase("boss")
        ) {

            sendResponse(
                    exchange,
                    400,
                    "{\"success\":false,\"message\":\"Invalid rater type\"}"
            );

            return;
        }


        if (
                !targetType.equalsIgnoreCase("worker")
                &&
                !targetType.equalsIgnoreCase("boss")
        ) {

            sendResponse(
                    exchange,
                    400,
                    "{\"success\":false,\"message\":\"Invalid target type\"}"
            );

            return;
        }


        // =====================================
        // ONLY OPPOSITE ROLE CAN RATE
        // =====================================

        if (
                raterType.equalsIgnoreCase(
                        targetType
                )
        ) {

            sendResponse(
                    exchange,
                    400,
                    "{\"success\":false,\"message\":\"You can only rate the opposite account type\"}"
            );

            return;
        }


        // =====================================
        // RATING NUMBER
        // =====================================

        int ratingValue;

        try {

            ratingValue =
                    Integer.parseInt(
                            ratingText
                    );

        } catch (NumberFormatException e) {

            sendResponse(
                    exchange,
                    400,
                    "{\"success\":false,\"message\":\"Invalid rating\"}"
            );

            return;
        }


        if (
                ratingValue < 1
                ||
                ratingValue > 5
        ) {

            sendResponse(
                    exchange,
                    400,
                    "{\"success\":false,\"message\":\"Rating must be between 1 and 5\"}"
            );

            return;
        }


        // =====================================
        // CHECK RATER
        // =====================================

        boolean raterExists;


        if (
                raterType.equalsIgnoreCase(
                        "worker"
                )
        ) {

            raterExists =
                    FileManager.getWorkerByPhone(
                            raterPhone
                    ) != null;

        } else {

            raterExists =
                    FileManager.getBossByPhone(
                            raterPhone
                    ) != null;
        }


        if (!raterExists) {

            sendResponse(
                    exchange,
                    404,
                    "{\"success\":false,\"message\":\"Rater account not found\"}"
            );

            return;
        }


        // =====================================
        // CHECK TARGET
        // =====================================

        boolean targetExists;


        if (
                targetType.equalsIgnoreCase(
                        "worker"
                )
        ) {

            targetExists =
                    FileManager.getWorkerByPhone(
                            targetPhone
                    ) != null;

        } else {

            targetExists =
                    FileManager.getBossByPhone(
                            targetPhone
                    ) != null;
        }


        if (!targetExists) {

            sendResponse(
                    exchange,
                    404,
                    "{\"success\":false,\"message\":\"Rating target not found\"}"
            );

            return;
        }


        // =====================================
        // PREVENT SELF RATING
        // =====================================

        if (
                raterType.equalsIgnoreCase(
                        targetType
                )
                &&
                raterPhone.equals(
                        targetPhone
                )
        ) {

            sendResponse(
                    exchange,
                    400,
                    "{\"success\":false,\"message\":\"You cannot rate yourself\"}"
            );

            return;
        }


        // =====================================
        // SAVE
        // =====================================

        boolean saved =
                FileManager.saveRating(
                        raterType,
                        raterPhone,
                        raterName,
                        targetType,
                        targetPhone,
                        targetName,
                        ratingValue,
                        review
                );


        if (saved) {

            sendResponse(
                    exchange,
                    200,
                    "{\"success\":true,\"message\":\"Rating saved successfully\"}"
            );

        } else {

            sendResponse(
                    exchange,
                    500,
                    "{\"success\":false,\"message\":\"Unable to save rating\"}"
            );
        }

        return;
    }


    // =====================================
    // GET = LOAD RATINGS
    // =====================================

    if (
            exchange.getRequestMethod()
                    .equalsIgnoreCase("GET")
    ) {

        String query =
                exchange.getRequestURI()
                        .getQuery();


        String phone =
                getQueryValue(
                        query,
                        "phone"
                );

        String type =
                getQueryValue(
                        query,
                        "type"
                );


        if (
                phone.isEmpty()
                ||
                type.isEmpty()
        ) {

            sendResponse(
                    exchange,
                    400,
                    "{\"success\":false,\"message\":\"Type and phone are required\"}"
            );

            return;
        }


        String targetName = "";


        if (
                type.equalsIgnoreCase(
                        "worker"
                )
        ) {

            String worker =
                    FileManager.getWorkerByPhone(
                            phone
                    );

            if (worker == null) {

                sendResponse(
                        exchange,
                        404,
                        "{\"success\":false,\"message\":\"Worker not found\"}"
                );

                return;
            }


            String[] data =
                    worker.split("\\|", -1);

            targetName =
                    data[0].trim();


        } else if (
                type.equalsIgnoreCase(
                        "boss"
                )
        ) {

            String boss =
                    FileManager.getBossByPhone(
                            phone
                    );

            if (boss == null) {

                sendResponse(
                        exchange,
                        404,
                        "{\"success\":false,\"message\":\"Boss not found\"}"
                );

                return;
            }


            String[] data =
                    boss.split("\\|", -1);

            targetName =
                    data[0].trim();


        } else {

            sendResponse(
                    exchange,
                    400,
                    "{\"success\":false,\"message\":\"Invalid target type\"}"
            );

            return;
        }


        List<String> ratings =
                FileManager.getRatingsForTarget(
                        type,
                        phone,
                        targetName
                );


        double average =
                FileManager.getAverageRating(
                        type,
                        phone,
                        targetName
                );


        int count =
                ratings.size();


        StringBuilder json =
                new StringBuilder();


        json.append(
                "{\"success\":true,"
        );

        json.append(
                "\"average\":"
        );

        json.append(
                String.format(
                        java.util.Locale.US,
                        "%.1f",
                        average
                )
        );

        json.append(",");

        json.append(
                "\"count\":"
        );

        json.append(count);

        json.append(",");

        json.append(
                "\"ratings\":["
        );


        boolean first =
                true;


        for (String rating : ratings) {

            String[] data =
                    rating.split("\\|", -1);


            if (
                    data.length < 4
            ) {
                continue;
            }


            if (!first) {
                json.append(",");
            }

            first = false;


            json.append("{");


            if (data.length >= 8) {

                json.append(
                        "\"raterName\":\""
                );

                json.append(
                        escapeJson(data[2])
                );

                json.append("\",");


                json.append(
                        "\"raterType\":\""
                );

                json.append(
                        escapeJson(data[0])
                );

                json.append("\",");


                json.append(
                        "\"rating\":"
                );

                json.append(
                        data[6]
                );

                json.append(",");


                json.append(
                        "\"review\":\""
                );

                json.append(
                        escapeJson(data[7])
                );

                json.append("\"");


            } else {

                // OLD FORMAT

                json.append(
                        "\"raterName\":\""
                );

                json.append(
                        escapeJson(data[1])
                );

                json.append("\",");


                json.append(
                        "\"raterType\":\"boss\","
                );


                json.append(
                        "\"rating\":"
                );

                json.append(
                        data[2]
                );

                json.append(",");


                json.append(
                        "\"review\":\""
                );

                json.append(
                        escapeJson(data[3])
                );

                json.append("\"");
            }


            json.append("}");
        }


        json.append("]}");


        sendResponse(
                exchange,
                200,
                json.toString()
        );

        return;
    }


    sendResponse(
            exchange,
            405,
            "{\"success\":false,\"message\":\"GET or POST required\"}"
    );
}
    // =========================================
    // UPDATE WORKER PROFILE
    // =========================================

    private static void updateWorkerProfile(
            HttpExchange exchange
    ) throws IOException {

        if (handleOptions(exchange)) {
            return;
        }

        if (!exchange.getRequestMethod()
                .equalsIgnoreCase("POST")) {

            sendResponse(
                    exchange,
                    405,
                    "{\"success\":false,\"message\":\"POST method required\"}"
            );

            return;
        }

        String body =
                readBody(exchange);

        String oldPhone =
                getValue(
                        body,
                        "oldPhone"
                );

        String name =
                getValue(
                        body,
                        "name"
                );

        String password =
                getValue(
                        body,
                        "password"
                );

        String phone =
                getValue(
                        body,
                        "phone"
                );

        String skill =
                getValue(
                        body,
                        "skill"
                );

        String location =
                getValue(
                        body,
                        "location"
                );

        String email =
                getValue(
                        body,
                        "email"
                );

        System.out.println(
                "EMAIL RECEIVED: [" + email + "]"
        );

        if (
                oldPhone.isEmpty()
                ||
                name.isEmpty()
                ||
                password.isEmpty()
                ||
                phone.isEmpty()
                ||
                skill.isEmpty()
                ||
                location.isEmpty()
                ||
                email.isEmpty()
        ) {

            sendResponse(
                    exchange,
                    400,
                    "{\"success\":false,\"message\":\"All fields are required\"}"
            );

            return;
        }

        WorkersService service =
                new WorkersService();

boolean result =
        service.updateWorkerProfile(
                oldPhone,
                name,
                password,
                phone,
                skill,
                location,
                email
        );

        

        if (result) {

            sendResponse(
                    exchange,
                    200,
                    "{\"success\":true,\"message\":\"Profile updated successfully\"}"
            );

        } else {

            sendResponse(
                    exchange,
                    400,
                    "{\"success\":false,\"message\":\"Profile update failed\"}"
            );
        }
    }


    // =========================================
    // READ REQUEST BODY
    // =========================================

    private static String readBody(
            HttpExchange exchange
    ) throws IOException {

        byte[] data =
                exchange.getRequestBody()
                        .readAllBytes();

        return new String(
                data,
                StandardCharsets.UTF_8
        );
    }


    // =========================================
    // GET VALUE FROM JSON
    // =========================================

    private static String getValue(
            String json,
            String key
    ) {

        if (json == null) {
            return "";
        }

        String search =
                "\"" + key + "\"";

        int keyPosition =
                json.indexOf(search);

        if (keyPosition == -1) {
            return "";
        }

        int colon =
                json.indexOf(
                        ":",
                        keyPosition
                );

        if (colon == -1) {
            return "";
        }

        int firstQuote =
                json.indexOf(
                        "\"",
                        colon + 1
                );

        if (firstQuote == -1) {
            return "";
        }

        int secondQuote =
                json.indexOf(
                        "\"",
                        firstQuote + 1
                );

        if (secondQuote == -1) {
            return "";
        }

        return json.substring(
                firstQuote + 1,
                secondQuote
        );
    }


    // =========================================
    // GET QUERY VALUE
    // =========================================

    private static String getQueryValue(
            String query,
            String key
    ) {

        if (query == null) {
            return "";
        }

        String[] parts =
                query.split("&");

        for (String part : parts) {

            String[] pair =
                    part.split("=", 2);

            if (
                    pair.length == 2
                    &&
                    pair[0].equals(key)
            ) {

                return pair[1];
            }
        }

        return "";
    }


    // =========================================
    // ESCAPE JSON
    // =========================================

    private static String escapeJson(
            String value
    ) {

        if (value == null) {
            return "";
        }

        return value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"");
    }


    // =========================================
    // HANDLE OPTIONS / CORS
    // =========================================

    private static boolean handleOptions(
            HttpExchange exchange
    ) throws IOException {

        setCorsHeaders(exchange);

        if (exchange.getRequestMethod()
                .equalsIgnoreCase("OPTIONS")) {

            exchange.sendResponseHeaders(
                    204,
                    -1
            );

            exchange.close();

            return true;
        }

        return false;
    }


    // =========================================
    // CORS HEADERS
    // =========================================

    private static void setCorsHeaders(
            HttpExchange exchange
    ) {

        exchange.getResponseHeaders()
                .set(
                        "Content-Type",
                        "application/json; charset=UTF-8"
                );

        exchange.getResponseHeaders()
                .set(
                        "Access-Control-Allow-Origin",
                        "*"
                );

        exchange.getResponseHeaders()
                .set(
                        "Access-Control-Allow-Methods",
                        "GET, POST, OPTIONS"
                );

        exchange.getResponseHeaders()
                .set(
                        "Access-Control-Allow-Headers",
                        "Content-Type"
                );
    }


    // =========================================
    // SEND RESPONSE
    // =========================================

    private static void sendResponse(
            HttpExchange exchange,
            int status,
            String response
    ) throws IOException {

        setCorsHeaders(exchange);

        byte[] data =
                response.getBytes(
                        StandardCharsets.UTF_8
                );

        exchange.sendResponseHeaders(
                status,
                data.length
        );

        try (
                OutputStream output =
                        exchange.getResponseBody()
        ) {

            output.write(data);
        }
    }
}