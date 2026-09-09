import java.util.List;

public class BossService {

    // =========================================
    // REGISTER BOSS
    // =========================================

    public boolean registerBoss(
            String name,
            String password,
            String phone,
            String company
    ) {

        if (name == null ||
                name.trim().isEmpty()) {
            return false;
        }

        if (password == null ||
                password.trim().isEmpty()) {
            return false;
        }

        if (phone == null ||
                phone.trim().isEmpty()) {
            return false;
        }

        if (company == null ||
                company.trim().isEmpty()) {
            return false;
        }

        if (phoneExists(phone)) {
            return false;
        }

        return FileManager.saveBoss(
                name.trim(),
                password,
                phone.trim(),
                company.trim()
        );
    }


    // =========================================
    // LOGIN BOSS
    // =========================================

    public boolean loginBoss(
            String phone,
            String password
    ) {

        if (phone == null ||
                password == null) {
            return false;
        }

        List<String> bosses =
                FileManager.getBosses();

        if (bosses == null) {
            return false;
        }

        for (String boss : bosses) {

            if (boss == null ||
                    boss.trim().isEmpty()) {
                continue;
            }

            String[] data =
                    boss.split("\\|", -1);

            if (data.length >= 3
                    && data[2].trim()
                            .equals(phone.trim())
                    && data.length >= 2
                    && data[1].equals(password)) {

                return true;
            }
        }

        return false;
    }


    // =========================================
    // PHONE EXISTS
    // =========================================

    public boolean phoneExists(String phone) {

        if (phone == null ||
                phone.trim().isEmpty()) {
            return false;
        }

        return FileManager.getBossByPhone(
                phone.trim()
        ) != null;
    }


    // =========================================
    // GET ALL BOSSES
    // =========================================

    public List<String> getAllBosses() {

        return FileManager.getBosses();
    }


    // =========================================
    // GET BOSS PROFILE
    // =========================================

    public String getBossProfile(
            String phone
    ) {

        return FileManager.getBossByPhone(
                phone
        );
    }


    // =========================================
    // UPDATE BOSS PROFILE
    // =========================================

    public boolean updateBossProfile(
            String oldPhone,
            String name,
            String phone,
            String company,
            String currentPassword
    ) {

        if (oldPhone == null ||
                oldPhone.trim().isEmpty()) {
            return false;
        }

        if (name == null ||
                name.trim().isEmpty()) {
            return false;
        }

        if (phone == null ||
                phone.trim().isEmpty()) {
            return false;
        }

        if (company == null ||
                company.trim().isEmpty()) {
            return false;
        }

        if (currentPassword == null ||
                currentPassword.isEmpty()) {
            return false;
        }

        oldPhone = oldPhone.trim();
        phone = phone.trim();

        String existing =
                FileManager.getBossByPhone(
                        oldPhone
                );

        if (existing == null) {
            return false;
        }

        String[] data =
                existing.split("\\|", -1);

        if (data.length < 2) {
            return false;
        }

        // Current password verify
        if (!data[1].equals(currentPassword)) {
            return false;
        }

        // New phone kisi aur boss ka nahi hona chahiye
        if (!oldPhone.equals(phone)
                && phoneExists(phone)) {

            return false;
        }

        /*
         * Current FileManager me direct updateBoss()
         * method nahi hai.
         *
         * Isliye existing boss list ko modify karke
         * saveAllBosses() se save karenge.
         */

        List<String> bosses =
                FileManager.getBosses();

        if (bosses == null) {
            return false;
        }

        boolean found = false;

        for (int i = 0;
             i < bosses.size();
             i++) {

            String boss = bosses.get(i);

            if (boss == null ||
                    boss.trim().isEmpty()) {
                continue;
            }

            String[] bossData =
                    boss.split("\\|", -1);

            if (bossData.length >= 4
                    && bossData[2].trim()
                            .equals(oldPhone)) {

                String newData =
                        name.trim()
                        + "|"
                        + currentPassword
                        + "|"
                        + phone
                        + "|"
                        + company.trim();

                bosses.set(i, newData);

                found = true;
                break;
            }
        }

        if (!found) {
            return false;
        }

        return FileManager.saveAllBosses(
                bosses
        );
    }
}