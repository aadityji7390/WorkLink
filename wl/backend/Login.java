public class Login {

    private WorkersService workersService;
    private BossService bossService;


    public Login() {

        workersService = new WorkersService();
        bossService = new BossService();

    }


    // ==============================
    // WORKER LOGIN
    // ==============================

    public boolean workerLogin(
            String phone,
            String password
    ) {

        return workersService.loginWorker(
                phone,
                password
        );
    }


    // ==============================
    // BOSS LOGIN
    // ==============================

    public boolean bossLogin(
            String phone,
            String password
    ) {

        return bossService.loginBoss(
                phone,
                password
        );
    }


    // ==============================
    // WORKER REGISTER
    // ==============================

    public boolean workerRegister(
            String name,
            String password,
            String phone,
            String skill,
            String location
    ) {

        return workersService.registerWorker(
                name,
                password,
                phone,
                skill,
                location
        );
    }


    // ==============================
    // BOSS REGISTER
    // ==============================

    public boolean bossRegister(
            String name,
            String password,
            String phone,
            String company
    ) {

        return bossService.registerBoss(
                name,
                password,
                phone,
                company
        );
    }

}