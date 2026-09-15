import java.util.List;
import java.util.ArrayList;

public class WorkersService {
    public boolean registerWorker(String name, String password, String phone, String skill, String location) {
        return registerWorker(name, password, phone, skill, location, "");
    }

    public boolean registerWorker(String name, String password, String phone, String skill, String location, String email) {
        if (!validText(name) || !validText(password) || !validPhone(phone) || !validText(skill) || !validText(location)) return false;
        if (FileManager.getWorkerByPhone(phone.trim()) != null || FileManager.getBossByPhone(phone.trim()) != null) return false;
        return FileManager.saveWorker(name.trim(), PasswordUtil.hash(password.trim()), phone.trim(), skill.trim(), location.trim(), email == null ? "" : email.trim());
    }

    public boolean loginWorker(String phone, String password) {
        if (!validPhone(phone) || password == null) return false;
        String row = FileManager.getWorkerByPhone(phone.trim());
        if (row == null) return false;
        String[] d = FileManager.split(row);
        if (d.length < 3 || !PasswordUtil.verify(password, d[1])) return false;
        if (!PasswordUtil.isHashed(d[1])) FileManager.replaceWorkerPassword(phone.trim(), PasswordUtil.hash(password));
        return true;
    }

    public boolean phoneExists(String phone) { return FileManager.getWorkerByPhone(phone) != null; }
    public List<String> getAllWorkers() { return FileManager.getWorkers(); }
    public String getWorkerProfile(String phone) { return FileManager.getWorkerByPhone(phone); }

    public boolean updateWorkerProfile(String oldPhone, String name, String currentPassword, String phone,
                                       String skill, String location, String email) {
        if (!validPhone(oldPhone) || !validText(name) || currentPassword == null || !validPhone(phone) ||
                !validText(skill) || !validText(location)) return false;
        String existing = FileManager.getWorkerByPhone(oldPhone);
        if (existing == null) return false;
        String[] d = FileManager.split(existing);
        if (d.length < 5 || !PasswordUtil.verify(currentPassword, d[1])) return false;
        if (!oldPhone.equals(phone) && (FileManager.getWorkerByPhone(phone) != null || FileManager.getBossByPhone(phone) != null)) return false;
        return FileManager.updateWorker(oldPhone, name, d[1], phone, skill, location, email == null ? "" : email);
    }

    public List<String> searchBySkill(String skill) {
        if (skill == null || skill.trim().isEmpty()) return getAllWorkers();
        List<String> out = new ArrayList<>();
        for (String row : getAllWorkers()) {
            String[] d = FileManager.split(row);
            if (d.length >= 4 && d[3].toLowerCase().contains(skill.trim().toLowerCase())) out.add(row);
        }
        return out;
    }

    private boolean validText(String s) { return s != null && !s.trim().isEmpty() && s.trim().length() <= 120; }
    private boolean validPhone(String s) { return s != null && s.matches("\\d{10}"); }
}
