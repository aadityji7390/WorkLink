import java.util.ArrayList;
import java.util.List;

public class WorkersService {

    // =========================================
    // REGISTER WORKER
    // =========================================

    public boolean registerWorker(
            String name,
            String password,
            String phone,
            String skill,
            String location
    ) {

        if (name == null || name.trim().isEmpty()) {
            return false;
        }

        if (password == null || password.trim().isEmpty()) {
            return false;
        }

        if (phone == null || phone.trim().isEmpty()) {
            return false;
        }

        if (skill == null || skill.trim().isEmpty()) {
            return false;
        }

        if (location == null || location.trim().isEmpty()) {
            return false;
        }

        if (phoneExists(phone)) {
            return false;
        }

        return FileManager.saveWorker(
                name.trim(),
                password.trim(),
                phone.trim(),
                skill.trim(),
                location.trim()
        );
    }


    // =========================================
    // LOGIN WORKER
    // =========================================

    public boolean loginWorker(
            String phone,
            String password
    ) {

        if (
                phone == null
                ||
                password == null
        ) {
            return false;
        }

        List<String> workers =
                FileManager.getWorkers();

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

            if (
                    data.length >= 3
                    &&
                    data[2].trim()
                            .equals(phone.trim())
                    &&
                    data[1].equals(password)
            ) {

                return true;
            }
        }

        return false;
    }


    // =========================================
    // PHONE EXISTS
    // =========================================

    public boolean phoneExists(
            String phone
    ) {

        if (
                phone == null
                ||
                phone.trim().isEmpty()
        ) {
            return false;
        }

        return FileManager.getWorkerByPhone(
                phone.trim()
        ) != null;
    }


    // =========================================
    // GET ALL WORKERS
    // =========================================

    public List<String> getAllWorkers() {

        return FileManager.getWorkers();
    }


    // =========================================
    // SEARCH BY SKILL
    // =========================================

    public List<String> searchBySkill(
            String skill
    ) {

        List<String> workers =
                FileManager.getWorkers();

        List<String> result =
                new ArrayList<>();

        if (
                skill == null
                ||
                skill.trim().isEmpty()
        ) {
            return workers;
        }

        String query =
                skill.trim()
                        .toLowerCase();

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

            if (data.length < 4) {
                continue;
            }

            String workerSkill =
                    data[3];

            if (
                    workerSkill
                            .toLowerCase()
                            .contains(query)
            ) {

                result.add(worker);
            }
        }

        return result;
    }


    // =========================================
    // GET WORKER PROFILE
    // =========================================

    public String getWorkerProfile(
            String phone
    ) {

        return FileManager.getWorkerByPhone(
                phone
        );
    }


    // =========================================
    // UPDATE WORKER PROFILE
    //
    // IMPORTANT ORDER:
    // oldPhone
    // name
    // password
    // phone
    // skill
    // location
    // email
    // =========================================

    public boolean updateWorkerProfile(
            String oldPhone,
            String name,
            String password,
            String phone,
            String skill,
            String location,
            String email
    ) {

        // =====================================
        // VALIDATION
        // =====================================

        if (
                oldPhone == null
                ||
                oldPhone.trim().isEmpty()
        ) {
            return false;
        }

        if (
                name == null
                ||
                name.trim().isEmpty()
        ) {
            return false;
        }

        if (
                password == null
                ||
                password.trim().isEmpty()
        ) {
            return false;
        }

        if (
                phone == null
                ||
                phone.trim().isEmpty()
        ) {
            return false;
        }

        if (
                skill == null
                ||
                skill.trim().isEmpty()
        ) {
            return false;
        }

        if (
                location == null
                ||
                location.trim().isEmpty()
        ) {
            return false;
        }


        oldPhone =
                oldPhone.trim();

        name =
                name.trim();

        phone =
                phone.trim();

        skill =
                skill.trim();

        location =
                location.trim();

        if (email == null) {
            email = "";
        }

        email =
                email.trim();


        // =====================================
        // GET EXISTING WORKER
        // =====================================

        String existingWorker =
                FileManager.getWorkerByPhone(
                        oldPhone
                );

        if (existingWorker == null) {

            System.out.println(
                    "Worker not found for update: "
                    + oldPhone
            );

            return false;
        }


        String[] data =
                existingWorker.split(
                        "\\|",
                        -1
                );


        if (data.length < 2) {

            System.out.println(
                    "Invalid worker data."
            );

            return false;
        }


        // =====================================
        // VERIFY PASSWORD
        // =====================================

        String savedPassword =
                data[1];

        if (
                !savedPassword.equals(
                        password
                )
        ) {

            System.out.println(
                    "Wrong password while updating profile."
            );

            return false;
        }


        // =====================================
        // CHECK NEW PHONE
        // =====================================

        if (!oldPhone.equals(phone)) {

            String anotherWorker =
                    FileManager.getWorkerByPhone(
                            phone
                    );

            if (anotherWorker != null) {

                System.out.println(
                        "Phone already belongs to another worker."
                );

                return false;
            }
        }


        // =====================================
        // UPDATE FILE
        // =====================================

        return FileManager.updateWorker(
                oldPhone,
                name,
                password,
                phone,
                skill,
                location,
                email
        );
    }
}