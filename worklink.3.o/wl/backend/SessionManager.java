import java.security.SecureRandom;
import java.util.Base64;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class SessionManager {
    private static final Map<String, Session> SESSIONS = new ConcurrentHashMap<>();
    private static final SecureRandom RANDOM = new SecureRandom();

    private SessionManager() {}

    public static String create(String role, String phone) {
        byte[] bytes = new byte[32];
        RANDOM.nextBytes(bytes);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        SESSIONS.put(token, new Session(role, phone));
        return token;
    }

    public static Session get(String token) { return token == null ? null : SESSIONS.get(token); }
    public static void remove(String token) { if (token != null) SESSIONS.remove(token); }
    public static void replacePhone(String token, String role, String phone) { if (token != null && SESSIONS.containsKey(token)) SESSIONS.put(token, new Session(role, phone)); }

    public static boolean matches(String token, String role, String phone) {
        Session s = get(token);
        return s != null && s.role.equalsIgnoreCase(role) && s.phone.equals(phone);
    }


    public static final class Session {
        public final String role;
        public final String phone;
        Session(String role, String phone) { this.role = role; this.phone = phone; }
    }
}
