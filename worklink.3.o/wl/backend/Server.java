import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Server {
    private static final int PORT = Integer.parseInt(System.getenv().getOrDefault("PORT", "8080"));
    private static final int MAX_BODY = 32 * 1024;

    public static void main(String[] args) throws Exception {
        FileManager.initialize();
        HttpServer server = HttpServer.create(new InetSocketAddress("0.0.0.0", PORT), 0);
        server.createContext("/", Server::home);
        server.createContext("/api/test", Server::test);
        server.createContext("/api/register-worker", Server::registerWorker);
        server.createContext("/api/login-worker", Server::loginWorker);
        server.createContext("/api/register-boss", Server::registerBoss);
        server.createContext("/api/login-boss", Server::loginBoss);
        server.createContext("/api/logout", Server::logout);
        server.createContext("/api/worker/profile", Server::workerProfile);
        server.createContext("/api/worker/update", Server::updateWorkerProfile);
        server.createContext("/api/boss/profile", Server::bossProfile);
        server.createContext("/api/boss/update", Server::updateBossProfile);
        server.createContext("/api/workers/search", Server::searchWorkers);
        server.createContext("/api/bosses/search", Server::searchBosses);
        server.createContext("/api/rating", Server::rating);
        server.createContext("/api/jobs", Server::jobs);
        server.createContext("/api/chat", Server::chat);
        server.setExecutor(null);
        System.out.println("================================");
        System.out.println("       WORKLINK BACKEND v2");
        System.out.println("================================");
        System.out.println("Local: http://localhost:" + PORT);
        System.out.println("Test:  http://localhost:" + PORT + "/api/test");
        System.out.println("Security: PBKDF2 passwords + session tokens + validation");
        System.out.println("================================");
        server.start();
    }

    private static void home(HttpExchange e) throws IOException { serveStatic(e); }

    private static void serveStatic(HttpExchange e) throws IOException {
        if (handleOptions(e)) return;
        if (!"GET".equalsIgnoreCase(e.getRequestMethod())) { send(e,405,"Method Not Allowed","text/plain; charset=UTF-8"); return; }
        String path = e.getRequestURI().getPath();
        if (path == null || path.equals("/")) path = "/index.html";
        path = URLDecoder.decode(path, StandardCharsets.UTF_8);
        Path root = Paths.get("../frontend").toAbsolutePath().normalize();
        Path requested = root.resolve(path.substring(1)).normalize();
        if (!requested.startsWith(root)) { send(e,403,"Forbidden","text/plain; charset=UTF-8"); return; }
        if (!Files.exists(requested) || Files.isDirectory(requested)) { send(e,404,"Not Found","text/plain; charset=UTF-8"); return; }
        byte[] data = Files.readAllBytes(requested);
        e.getResponseHeaders().set("Content-Type", contentType(requested.getFileName().toString()));
        e.getResponseHeaders().set("Cache-Control", "no-store");
        addSecurityHeaders(e);
        e.sendResponseHeaders(200, data.length);
        try (OutputStream out=e.getResponseBody()) { out.write(data); }
    }

    private static String contentType(String n) {
        n=n.toLowerCase();
        if(n.endsWith(".html"))return"text/html; charset=UTF-8";
        if(n.endsWith(".css"))return"text/css; charset=UTF-8";
        if(n.endsWith(".js"))return"application/javascript; charset=UTF-8";
        if(n.endsWith(".json"))return"application/json; charset=UTF-8";
        if(n.endsWith(".png"))return"image/png";
        if(n.endsWith(".jpg")||n.endsWith(".jpeg"))return"image/jpeg";
        if(n.endsWith(".svg"))return"image/svg+xml";
        return"application/octet-stream";
    }

    private static void test(HttpExchange e)throws IOException{
        if(handleOptions(e))return;
        if(!"GET".equalsIgnoreCase(e.getRequestMethod())){sendJson(e,405,"{\"success\":false,\"message\":\"GET method required\"}");return;}
        sendJson(e,200,"{\"success\":true,\"message\":\"WorkLink Java Backend v2 is running\"}");
    }

    private static void registerWorker(HttpExchange e)throws IOException{
        if(!requireMethod(e,"POST"))return;
        String body=readBody(e);
        String name=getValue(body,"name"), password=getValue(body,"password"), phone=getValue(body,"phone"), skill=getValue(body,"skill"), location=getValue(body,"location"), email=getValue(body,"email");
        if(!phone.matches("\\d{10}")||name.isBlank()||password.length()<6||skill.isBlank()||location.isBlank()){
            sendJson(e,400,"{\"success\":false,\"message\":\"Enter valid details. Password must be at least 6 characters.\"}");return;
        }
        if(new WorkersService().phoneExists(phone)||new BossService().phoneExists(phone)){sendJson(e,409,"{\"success\":false,\"message\":\"Phone number already exists\"}");return;}
        boolean ok=new Login().workerRegister(name,password,phone,skill,location,email);
        sendJson(e,ok?200:400, ok?"{\"success\":true,\"message\":\"Worker registered successfully\"}":"{\"success\":false,\"message\":\"Worker registration failed\"}");
    }

    private static void registerBoss(HttpExchange e)throws IOException{
        if(!requireMethod(e,"POST"))return;
        String body=readBody(e);
        String name=getValue(body,"name"), password=getValue(body,"password"), phone=getValue(body,"phone"), company=getValue(body,"company");
        if(!phone.matches("\\d{10}")||name.isBlank()||password.length()<6||company.isBlank()){
            sendJson(e,400,"{\"success\":false,\"message\":\"Enter valid details. Password must be at least 6 characters.\"}");return;
        }
        if(new BossService().phoneExists(phone)||new WorkersService().phoneExists(phone)){sendJson(e,409,"{\"success\":false,\"message\":\"Phone number already exists\"}");return;}
        boolean ok=new Login().bossRegister(name,password,phone,company);
        sendJson(e,ok?200:400, ok?"{\"success\":true,\"message\":\"Boss registered successfully\"}":"{\"success\":false,\"message\":\"Boss registration failed\"}");
    }

    private static void loginWorker(HttpExchange e)throws IOException{ login(e,"worker"); }
    private static void loginBoss(HttpExchange e)throws IOException{ login(e,"boss"); }

    private static void login(HttpExchange e,String role)throws IOException{
        if(!requireMethod(e,"POST"))return;
        String body=readBody(e); String phone=getValue(body,"phone"), password=getValue(body,"password");
        boolean ok=role.equals("worker")?new Login().workerLogin(phone,password):new Login().bossLogin(phone,password);
        if(!ok){sendJson(e,401,"{\"success\":false,\"message\":\"Invalid phone or password\"}");return;}
        String row=role.equals("worker")?FileManager.getWorkerByPhone(phone):FileManager.getBossByPhone(phone);
        String[] d=FileManager.split(row); String name=d.length>0?d[0]:"User";
        String token=SessionManager.create(role,phone);
        sendJson(e,200,"{\"success\":true,\"message\":\"Login successful\",\"role\":\""+role+"\",\"phone\":\""+json(phone)+"\",\"name\":\""+json(name)+"\",\"token\":\""+json(token)+"\"}");
    }

    private static void logout(HttpExchange e)throws IOException{
        if(!requireMethod(e,"POST"))return;
        SessionManager.remove(token(e));
        sendJson(e,200,"{\"success\":true,\"message\":\"Logged out successfully\"}");
    }

    private static void workerProfile(HttpExchange e)throws IOException{
        if(!requireMethod(e,"GET"))return;
        String phone=query(e,"phone"); String row=FileManager.getWorkerByPhone(phone);
        if(row==null){sendJson(e,404,"{\"success\":false,\"message\":\"Worker not found\"}");return;}
        String[] d=FileManager.split(row); String email=d.length>5?d[5]:"";
        sendJson(e,200,"{\"success\":true,\"name\":\""+json(d[0])+"\",\"phone\":\""+json(d[2])+"\",\"skill\":\""+json(d[3])+"\",\"location\":\""+json(d[4])+"\",\"email\":\""+json(email)+"\"}");
    }

    private static void bossProfile(HttpExchange e)throws IOException{
        if(!requireMethod(e,"GET"))return;
        String phone=query(e,"phone"); String row=FileManager.getBossByPhone(phone);
        if(row==null){sendJson(e,404,"{\"success\":false,\"message\":\"Boss not found\"}");return;}
        String[] d=FileManager.split(row);
        sendJson(e,200,"{\"success\":true,\"name\":\""+json(d[0])+"\",\"phone\":\""+json(d[2])+"\",\"company\":\""+json(d[3])+"\"}");
    }

    private static void updateWorkerProfile(HttpExchange e)throws IOException{
        if(!requireMethod(e,"POST"))return;
        String body=readBody(e); String oldPhone=getValue(body,"oldPhone"), name=getValue(body,"name"), password=getValue(body,"password"), phone=getValue(body,"phone"), skill=getValue(body,"skill"), location=getValue(body,"location"), email=getValue(body,"email");
        if(!authorize(e,"worker",oldPhone)){sendJson(e,401,"{\"success\":false,\"message\":\"Authentication required\"}");return;}
        boolean ok=new WorkersService().updateWorkerProfile(oldPhone,name,password,phone,skill,location,email);
        if(ok && !oldPhone.equals(phone)){String t=token(e);SessionManager.replacePhone(t,"worker",phone);}
        sendJson(e,ok?200:400,ok?"{\"success\":true,\"message\":\"Profile updated successfully\"}":"{\"success\":false,\"message\":\"Profile update failed. Check your current password.\"}");
    }

    private static void updateBossProfile(HttpExchange e)throws IOException{
        if(!requireMethod(e,"POST"))return;
        String body=readBody(e); String oldPhone=getValue(body,"oldPhone"), name=getValue(body,"name"), password=getValue(body,"password"), phone=getValue(body,"phone"), company=getValue(body,"company");
        if(!authorize(e,"boss",oldPhone)){sendJson(e,401,"{\"success\":false,\"message\":\"Authentication required\"}");return;}
        boolean ok=new BossService().updateBossProfile(oldPhone,name,phone,company,password);
        if(ok && !oldPhone.equals(phone)) SessionManager.replacePhone(token(e),"boss",phone);
        sendJson(e,ok?200:400,ok?"{\"success\":true,\"message\":\"Boss profile updated successfully\"}":"{\"success\":false,\"message\":\"Profile update failed. Check your current password.\"}");
    }

    private static void searchWorkers(HttpExchange e)throws IOException{
        if(!requireMethod(e,"GET"))return;
        String q=query(e,"q").toLowerCase(), location=query(e,"location").toLowerCase(), category=query(e,"category").toLowerCase();
        List<String> rows=FileManager.getWorkers(); StringBuilder out=new StringBuilder("{\"success\":true,\"workers\":["); boolean first=true;
        for(String row:rows){String[] d=FileManager.split(row); if(d.length<5)continue; String name=d[0],phone=d[2],skill=d[3],loc=d[4];
            boolean match=q.isEmpty()||name.toLowerCase().contains(q)||skill.toLowerCase().contains(q)||loc.toLowerCase().contains(q);
            if(!location.isEmpty()&&!loc.toLowerCase().contains(location))match=false;
            if(!category.isEmpty()&&!category.equals("all")&&!skill.toLowerCase().contains(category))match=false;
            if(!match)continue;
            if(!first)out.append(','); first=false;
            out.append("{\"name\":\"").append(json(name)).append("\",\"phone\":\"").append(json(phone)).append("\",\"skill\":\"").append(json(skill)).append("\",\"location\":\"").append(json(loc)).append("\"}");
        }
        out.append("]}"); sendJson(e,200,out.toString());
    }

    private static void searchBosses(HttpExchange e)throws IOException{
        if(!requireMethod(e,"GET"))return;
        String q=query(e,"q").toLowerCase(); List<String> rows=FileManager.getBosses(); StringBuilder out=new StringBuilder("{\"success\":true,\"bosses\":["); boolean first=true;
        for(String row:rows){String[] d=FileManager.split(row); if(d.length<4)continue; if(!q.isEmpty()&&!d[0].toLowerCase().contains(q)&&!d[3].toLowerCase().contains(q))continue;
            if(!first)out.append(','); first=false; out.append("{\"name\":\"").append(json(d[0])).append("\",\"phone\":\"").append(json(d[2])).append("\",\"company\":\"").append(json(d[3])).append("\"}");
        }
        out.append("]}"); sendJson(e,200,out.toString());
    }

    private static void rating(HttpExchange e)throws IOException{
        if("POST".equalsIgnoreCase(e.getRequestMethod())){ saveRating(e); return; }
        if("GET".equalsIgnoreCase(e.getRequestMethod())){ loadRatings(e); return; }
        sendJson(e,405,"{\"success\":false,\"message\":\"GET or POST required\"}");
    }

    private static void saveRating(HttpExchange e)throws IOException{
        String body=readBody(e); String raterType=getValue(body,"raterType"), raterPhone=getValue(body,"raterPhone"), targetType=getValue(body,"targetType"), targetPhone=getValue(body,"targetPhone"), review=getValue(body,"review");
        int value; try{value=Integer.parseInt(getValue(body,"rating"));}catch(Exception ex){sendJson(e,400,"{\"success\":false,\"message\":\"Invalid rating\"}");return;}
        if(!authorize(e,raterType,raterPhone)){sendJson(e,401,"{\"success\":false,\"message\":\"Authentication required\"}");return;}
        if(!((raterType.equals("worker")&&targetType.equals("boss"))||(raterType.equals("boss")&&targetType.equals("worker")))){sendJson(e,400,"{\"success\":false,\"message\":\"Only opposite account types can be rated\"}");return;}
        if(review.trim().length()<5||review.length()>500||value<1||value>5){sendJson(e,400,"{\"success\":false,\"message\":\"Rating must be 1-5 and review 5-500 characters\"}");return;}
        String raterRow=raterType.equals("worker")?FileManager.getWorkerByPhone(raterPhone):FileManager.getBossByPhone(raterPhone);
        String targetRow=targetType.equals("worker")?FileManager.getWorkerByPhone(targetPhone):FileManager.getBossByPhone(targetPhone);
        if(raterRow==null||targetRow==null){sendJson(e,404,"{\"success\":false,\"message\":\"Rating account not found\"}");return;}
        if(raterPhone.equals(targetPhone)){sendJson(e,400,"{\"success\":false,\"message\":\"You cannot rate yourself\"}");return;}
        String raterName=FileManager.split(raterRow)[0], targetName=FileManager.split(targetRow)[0];
        boolean ok=new Rating().addRating(raterType,raterPhone,raterName,targetType,targetPhone,targetName,value,review);
        sendJson(e,ok?200:500,ok?"{\"success\":true,\"message\":\"Rating saved successfully\"}":"{\"success\":false,\"message\":\"Unable to save rating\"}");
    }

    private static void loadRatings(HttpExchange e)throws IOException{
        String phone=query(e,"phone"), type=query(e,"type");
        if(!((type.equals("worker")||type.equals("boss"))&&!phone.isBlank())){sendJson(e,400,"{\"success\":false,\"message\":\"Type and phone are required\"}");return;}
        String row=type.equals("worker")?FileManager.getWorkerByPhone(phone):FileManager.getBossByPhone(phone);
        if(row==null){sendJson(e,404,"{\"success\":false,\"message\":\"Profile not found\"}");return;}
        String name=FileManager.split(row)[0]; List<String> ratings=FileManager.getRatingsForTarget(type,phone,name); double avg=FileManager.getAverageRating(type,phone,name);
        StringBuilder out=new StringBuilder("{\"success\":true,\"average\":").append(String.format(java.util.Locale.US,"%.1f",avg)).append(",\"count\":").append(ratings.size()).append(",\"ratings\":["); boolean first=true;
        for(String r:ratings){String[] d=FileManager.split(r); if(d.length<8)continue; if(!first)out.append(','); first=false; out.append("{\"raterName\":\"").append(json(d[2])).append("\",\"raterType\":\"").append(json(d[0])).append("\",\"rating\":").append(d[6]).append(",\"review\":\"").append(json(d[7])).append("\"}");}
        out.append("]}"); sendJson(e,200,out.toString());
    }

    private static void chat(HttpExchange e)throws IOException{
        if("GET".equalsIgnoreCase(e.getRequestMethod())){ loadChat(e); return; }
        if("POST".equalsIgnoreCase(e.getRequestMethod())){ sendChat(e); return; }
        sendJson(e,405,"{\"success\":false,\"message\":\"GET or POST required\"}");
    }

    private static void loadChat(HttpExchange e)throws IOException{
        String myType="", myPhone="";
        SessionManager.Session session=SessionManager.get(token(e));
        if(session==null){sendJson(e,401,"{\"success\":false,\"message\":\"Authentication required\"}");return;}
        myType=session.role; myPhone=session.phone;
        String peerType=query(e,"peerType"), peerPhone=query(e,"peerPhone");
        if(!((peerType.equals("worker")||peerType.equals("boss"))&&!peerPhone.isBlank()) || (peerType.equals(myType)&&peerPhone.equals(myPhone))){
            sendJson(e,400,"{\"success\":false,\"message\":\"Invalid chat target\"}");return;
        }
        String peerRow=peerType.equals("worker")?FileManager.getWorkerByPhone(peerPhone):FileManager.getBossByPhone(peerPhone);
        if(peerRow==null){sendJson(e,404,"{\"success\":false,\"message\":\"Chat user not found\"}");return;}
        String peerName=FileManager.split(peerRow)[0];
        StringBuilder out=new StringBuilder("{\"success\":true,\"peerName\":\"").append(json(peerName)).append("\",\"messages\":[");
        boolean first=true;
        for(String row:FileManager.getMessages()){
            String[] d=FileManager.split(row); if(d.length<9)continue;
            boolean between=(d[1].equalsIgnoreCase(myType)&&d[2].equals(myPhone)&&d[4].equalsIgnoreCase(peerType)&&d[5].equals(peerPhone)) ||
                    (d[1].equalsIgnoreCase(peerType)&&d[2].equals(peerPhone)&&d[4].equalsIgnoreCase(myType)&&d[5].equals(myPhone));
            if(!between)continue;
            if(!first)out.append(','); first=false;
            out.append("{\"id\":\"").append(json(d[0])).append("\",\"senderType\":\"").append(json(d[1])).append("\",\"senderPhone\":\"").append(json(d[2])).append("\",\"senderName\":\"").append(json(d[3])).append("\",\"message\":\"").append(json(d[7])).append("\",\"createdAt\":\"").append(json(d[8])).append("\"}");
        }
        out.append("]}"); sendJson(e,200,out.toString());
    }

    private static void sendChat(HttpExchange e)throws IOException{
        SessionManager.Session session=SessionManager.get(token(e));
        if(session==null){sendJson(e,401,"{\"success\":false,\"message\":\"Authentication required\"}");return;}
        String body=readBody(e), peerType=getValue(body,"receiverType"), peerPhone=getValue(body,"receiverPhone"), message=getValue(body,"message").trim();
        if(!((peerType.equals("worker")||peerType.equals("boss"))&&!peerPhone.isBlank()) || (peerType.equals(session.role)&&peerPhone.equals(session.phone))){sendJson(e,400,"{\"success\":false,\"message\":\"Invalid chat target\"}");return;}
        if(message.isBlank()||message.length()>1000){sendJson(e,400,"{\"success\":false,\"message\":\"Message must be 1-1000 characters\"}");return;}
        String target=peerType.equals("worker")?FileManager.getWorkerByPhone(peerPhone):FileManager.getBossByPhone(peerPhone);
        if(target==null){sendJson(e,404,"{\"success\":false,\"message\":\"Chat user not found\"}");return;}
        String sender= session.role.equals("worker")?FileManager.getWorkerByPhone(session.phone):FileManager.getBossByPhone(session.phone);
        String senderName=FileManager.split(sender)[0], receiverName=FileManager.split(target)[0];
        String id=UUID.randomUUID().toString().substring(0,12);
        boolean ok=FileManager.saveMessage(id,session.role,session.phone,senderName,peerType,peerPhone,receiverName,message,Instant.now().toString());
        sendJson(e,ok?200:500,ok?"{\"success\":true,\"message\":\"Message sent\"}":"{\"success\":false,\"message\":\"Could not send message\"}");
    }

    private static void jobs(HttpExchange e)throws IOException{
        if("GET".equalsIgnoreCase(e.getRequestMethod())){listJobs(e);return;}
        if("POST".equalsIgnoreCase(e.getRequestMethod())){createJob(e);return;}
        sendJson(e,405,"{\"success\":false,\"message\":\"GET or POST required\"}");
    }

    private static void createJob(HttpExchange e)throws IOException{
        String body=readBody(e); String bossPhone=getValue(body,"bossPhone");
        if(!authorize(e,"boss",bossPhone)){sendJson(e,401,"{\"success\":false,\"message\":\"Only logged-in bosses can post work\"}");return;}
        String title=getValue(body,"title"),category=getValue(body,"category"),location=getValue(body,"location"),salary=getValue(body,"salary"),type=getValue(body,"type"),description=getValue(body,"description");
        if(title.isBlank()||category.isBlank()||location.isBlank()||salary.isBlank()||description.isBlank()||title.length()>100||description.length()>600){sendJson(e,400,"{\"success\":false,\"message\":\"Please complete all work details\"}");return;}
        String[] boss=FileManager.split(FileManager.getBossByPhone(bossPhone)); String id=UUID.randomUUID().toString().substring(0,8);
        boolean ok=FileManager.saveJob(id,bossPhone,boss[0],boss[3],title,category,location,salary,type.isBlank()?"Full Time":type,description,"OPEN",Instant.now().toString());
        sendJson(e,ok?200:500,ok?"{\"success\":true,\"message\":\"Work posted successfully\",\"id\":\""+id+"\"}":"{\"success\":false,\"message\":\"Could not post work\"}");
    }

    private static void listJobs(HttpExchange e)throws IOException{
        String q=query(e,"q").toLowerCase(), category=query(e,"category").toLowerCase(), location=query(e,"location").toLowerCase();
        List<String> rows=FileManager.getJobs(); StringBuilder out=new StringBuilder("{\"success\":true,\"jobs\":["); boolean first=true;
        for(String row:rows){String[] d=FileManager.split(row);if(d.length<12||!d[10].equalsIgnoreCase("OPEN"))continue;
            boolean match=q.isEmpty()||d[4].toLowerCase().contains(q)||d[3].toLowerCase().contains(q)||d[5].toLowerCase().contains(q)||d[6].toLowerCase().contains(q);
            if(!category.isEmpty()&&!category.equals("all")&&!d[5].toLowerCase().contains(category))match=false;
            if(!location.isEmpty()&&!d[6].toLowerCase().contains(location))match=false;
            if(!match)continue;
            if(!first)out.append(',');first=false;
            out.append("{\"id\":\"").append(json(d[0])).append("\",\"company\":\"").append(json(d[3])).append("\",\"bossName\":\"").append(json(d[2])).append("\",\"title\":\"").append(json(d[4])).append("\",\"category\":\"").append(json(d[5])).append("\",\"location\":\"").append(json(d[6])).append("\",\"salary\":\"").append(json(d[7])).append("\",\"type\":\"").append(json(d[8])).append("\",\"description\":\"").append(json(d[9])).append("\"}");
        }
        out.append("]}");sendJson(e,200,out.toString());
    }

    private static boolean authorize(HttpExchange e,String role,String phone){return SessionManager.matches(token(e),role,phone);}
    private static String token(HttpExchange e){String h=e.getRequestHeaders().getFirst("Authorization");if(h==null)return"";return h.startsWith("Bearer ")?h.substring(7).trim():"";}

    private static boolean requireMethod(HttpExchange e,String method)throws IOException{if(handleOptions(e))return false;if(!method.equalsIgnoreCase(e.getRequestMethod())){sendJson(e,405,"{\"success\":false,\"message\":\""+method+" method required\"}");return false;}return true;}

    private static String readBody(HttpExchange e)throws IOException{byte[] b=e.getRequestBody().readNBytes(MAX_BODY+1);if(b.length>MAX_BODY)throw new IOException("Request too large");return new String(b,StandardCharsets.UTF_8);}

    private static String getValue(String json,String key){
        if(json==null)return"";String needle="\""+key+"\"";int k=json.indexOf(needle);if(k<0)return"";int colon=json.indexOf(':',k+needle.length());if(colon<0)return"";int i=colon+1;while(i<json.length()&&Character.isWhitespace(json.charAt(i)))i++;if(i>=json.length()||json.charAt(i)!='\"')return"";i++;StringBuilder out=new StringBuilder();boolean esc=false;for(;i<json.length();i++){char c=json.charAt(i);if(esc){if(c=='n')out.append('\n');else if(c=='r')out.append('\r');else if(c=='t')out.append('\t');else out.append(c);esc=false;}else if(c=='\\')esc=true;else if(c=='\"')break;else out.append(c);}return out.toString();
    }

    private static String query(HttpExchange e,String key){String q=e.getRequestURI().getRawQuery();if(q==null)return"";for(String p:q.split("&")){String[] a=p.split("=",2);if(a.length==2&&a[0].equals(key))try{return URLDecoder.decode(a[1],StandardCharsets.UTF_8);}catch(Exception ignored){}}return"";}
    private static String json(String v){if(v==null)return"";return v.replace("\\","\\\\").replace("\"","\\\"").replace("\r","\\r").replace("\n","\\n").replace("\t","\\t");}

    private static boolean handleOptions(HttpExchange e)throws IOException{setHeaders(e);if("OPTIONS".equalsIgnoreCase(e.getRequestMethod())){e.sendResponseHeaders(204,-1);e.close();return true;}return false;}
    private static void setHeaders(HttpExchange e){e.getResponseHeaders().set("Access-Control-Allow-Origin","*");e.getResponseHeaders().set("Access-Control-Allow-Methods","GET, POST, OPTIONS");e.getResponseHeaders().set("Access-Control-Allow-Headers","Content-Type, Authorization");addSecurityHeaders(e);}
    private static void addSecurityHeaders(HttpExchange e){e.getResponseHeaders().set("X-Content-Type-Options","nosniff");e.getResponseHeaders().set("X-Frame-Options","SAMEORIGIN");e.getResponseHeaders().set("Referrer-Policy","strict-origin-when-cross-origin");}
    private static void sendJson(HttpExchange e,int status,String body)throws IOException{send(e,status,body,"application/json; charset=UTF-8");}
    private static void send(HttpExchange e,int status,String body,String type)throws IOException{setHeaders(e);e.getResponseHeaders().set("Content-Type",type);byte[] b=body.getBytes(StandardCharsets.UTF_8);e.sendResponseHeaders(status,b.length);try(OutputStream out=e.getResponseBody()){out.write(b);}}
}
