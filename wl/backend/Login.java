public class Login {
    private final WorkersService workersService = new WorkersService();
    private final BossService bossService = new BossService();
    public boolean workerLogin(String phone, String password) { return workersService.loginWorker(phone, password); }
    public boolean bossLogin(String phone, String password) { return bossService.loginBoss(phone, password); }
    public boolean workerRegister(String name, String password, String phone, String skill, String location) { return workersService.registerWorker(name,password,phone,skill,location); }
    public boolean workerRegister(String name, String password, String phone, String skill, String location, String email) { return workersService.registerWorker(name,password,phone,skill,location,email); }
    public boolean bossRegister(String name, String password, String phone, String company) { return bossService.registerBoss(name,password,phone,company); }
}
