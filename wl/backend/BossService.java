import java.util.List;

public class BossService {
    public boolean registerBoss(String name, String password, String phone, String company) {
        if (!validText(name) || !validText(password) || !validPhone(phone) || !validText(company)) return false;
        if (FileManager.getBossByPhone(phone.trim()) != null || FileManager.getWorkerByPhone(phone.trim()) != null) return false;
        return FileManager.saveBoss(name.trim(), PasswordUtil.hash(password.trim()), phone.trim(), company.trim());
    }

    public boolean loginBoss(String phone, String password) {
        if (!validPhone(phone) || password == null) return false;
        String row = FileManager.getBossByPhone(phone.trim());
        if (row == null) return false;
        String[] d = FileManager.split(row);
        if (d.length < 4 || !PasswordUtil.verify(password, d[1])) return false;
        if (!PasswordUtil.isHashed(d[1])) FileManager.replaceBossPassword(phone.trim(), PasswordUtil.hash(password));
        return true;
    }

    public boolean phoneExists(String phone) { return FileManager.getBossByPhone(phone) != null; }
    public List<String> getAllBosses() { return FileManager.getBosses(); }
    public String getBossProfile(String phone) { return FileManager.getBossByPhone(phone); }

    public boolean updateBossProfile(String oldPhone, String name, String phone, String company, String currentPassword) {
        if (!validPhone(oldPhone) || !validText(name) || !validPhone(phone) || !validText(company) || currentPassword == null) return false;
        String existing = FileManager.getBossByPhone(oldPhone);
        if (existing == null) return false;
        String[] d = FileManager.split(existing);
        if (d.length < 4 || !PasswordUtil.verify(currentPassword, d[1])) return false;
        if (!oldPhone.equals(phone) && (FileManager.getBossByPhone(phone) != null || FileManager.getWorkerByPhone(phone) != null)) return false;
        List<String> bosses = FileManager.getBosses();
        for (int i = 0; i < bosses.size(); i++) {
            String[] b = FileManager.split(bosses.get(i));
            if (b.length >= 4 && b[2].equals(oldPhone)) {
                bosses.set(i, name.trim() + "|" + d[1] + "|" + phone + "|" + company.trim());
                return FileManager.saveAllBosses(bosses);
            }
        }
        return false;
    }

    private boolean validText(String s) { return s != null && !s.trim().isEmpty() && s.trim().length() <= 120; }
    private boolean validPhone(String s) { return s != null && s.matches("\\d{10}"); }
}
