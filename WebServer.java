import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class WebServer implements HttpHandler
{
    private UserService service;
    private HttpServer server;
    private Map<String, String> sessions = new HashMap<>();
    private Map<String, String> captchas = new HashMap<>();

    public WebServer(UserService service)
    {
        this.service = service;
    }

    public int start(int port) throws IOException
    {
        server = HttpServer.create(new InetSocketAddress("localhost",port),0);
        server.createContext("/",this);
        server.start();
        return server.getAddress().getPort();
    }

    public void stop()
    {
        server.stop(0);
    }

    public String getCaptchaText(String captchaId)
    {
        return captchas.get(captchaId);
    }

    public void handle(HttpExchange ex) throws IOException
    {
        try
        {
            String path = ex.getRequestURI().getPath();
            if(path.equals("/")) home(ex);
            else if(path.equals("/register")) register(ex);
            else if(path.equals("/login")) login(ex);
            else if(path.equals("/logout")) logout(ex);
            else if(path.equals("/profile")) profile(ex);
            else if(path.equals("/captcha")) captcha(ex);
            else sendHtml(ex,404,page("Грешка 404","<p>Страницата не е намерена.</p>"));
        }
        catch(Exception e)
        {
            e.printStackTrace();
            sendHtml(ex,500,page("Грешка","<p>Възникна грешка в сървъра.</p>"));
        }
    }

    private void home(HttpExchange ex) throws IOException
    {
        if(getLoggedEmail(ex) != null)
        {
            redirect(ex,"/profile");
        }
        else
        {
            redirect(ex,"/login");
        }
    }

    private void register(HttpExchange ex) throws Exception
    {
        if(ex.getRequestMethod().equals("GET"))
        {
            showRegisterPage(ex,200,null,"","","");
            return;
        }
        Map<String, String> form = readForm(ex);
        String email = form.getOrDefault("email","").trim().toLowerCase();
        String firstName = form.getOrDefault("firstName","").trim();
        String lastName = form.getOrDefault("lastName","").trim();
        String password = form.getOrDefault("password","");
        String answer = form.getOrDefault("captcha","").trim();

        String expected = captchas.remove(getCookie(ex,"captcha"));
        if(expected == null || !expected.equalsIgnoreCase(answer))
        {
            showRegisterPage(ex,400,"Грешен код от картинката.",email,firstName,lastName);
            return;
        }
        String error = service.register(email,firstName,lastName,password);
        if(error != null)
        {
            showRegisterPage(ex,400,error,email,firstName,lastName);
            return;
        }
        redirect(ex,"/login?registered");
    }

    private void showRegisterPage(HttpExchange ex, int status, String error, String email, String firstName, String lastName) throws IOException
    {
        String captchaId = UUID.randomUUID().toString();
        captchas.put(captchaId,Captcha.generateText());
        ex.getResponseHeaders().add("Set-Cookie","captcha=" + captchaId + "; Path=/; HttpOnly; SameSite=Strict");
        String html = errorHtml(error)
            + "<form method='post' action='/register'>"
            + "<label>Имейл <input name='email' value='" + escape(email) + "'></label>"
            + "<label>Име <input name='firstName' value='" + escape(firstName) + "'></label>"
            + "<label>Фамилия <input name='lastName' value='" + escape(lastName) + "'></label>"
            + "<label>Парола <input type='password' name='password'></label>"
            + "<img src='/captcha?" + System.nanoTime() + "' alt='captcha'>"
            + "<label>Код от картинката <input name='captcha' autocomplete='off'></label>"
            + "<button>Регистрация</button>"
            + "</form>"
            + "<p>Имаш акаунт? <a href='/login'>Вход</a></p>";
        sendHtml(ex,status,page("Регистрация",html));
    }

    private void captcha(HttpExchange ex) throws IOException
    {
        String text = captchas.get(getCookie(ex,"captcha"));
        if(text == null)
        {
            sendHtml(ex,404,page("Грешка 404","<p>Няма CAPTCHA.</p>"));
            return;
        }
        byte[] image = Captcha.createImage(text);
        ex.getResponseHeaders().set("Content-Type","image/png");
        ex.getResponseHeaders().set("Cache-Control","no-store");
        ex.sendResponseHeaders(200,image.length);
        try(OutputStream out = ex.getResponseBody())
        {
            out.write(image);
        }
    }

    private void login(HttpExchange ex) throws Exception
    {
        if(ex.getRequestMethod().equals("GET"))
        {
            if(getLoggedEmail(ex) != null)
            {
                redirect(ex,"/profile");
                return;
            }
            String message = "";
            String query = ex.getRequestURI().getQuery();
            if(query != null && query.contains("registered"))
            {
                message = "<p class='success'>Регистрацията е успешна. Влезте в профила си.</p>";
            }
            showLoginPage(ex,200,message,"");
            return;
        }
        Map<String, String> form = readForm(ex);
        String email = form.getOrDefault("email","").trim().toLowerCase();
        String password = form.getOrDefault("password","");
        User user = service.login(email,password);
        if(user == null)
        {
            showLoginPage(ex,401,errorHtml("Грешен имейл или парола."),email);
            return;
        }
        String sessionId = UUID.randomUUID().toString();
        sessions.put(sessionId,user.getEmail());
        ex.getResponseHeaders().add("Set-Cookie","session=" + sessionId + "; Path=/; HttpOnly; SameSite=Strict");
        redirect(ex,"/profile");
    }

    private void showLoginPage(HttpExchange ex, int status, String message, String email) throws IOException
    {
        String html = message
            + "<form method='post' action='/login'>"
            + "<label>Имейл <input name='email' value='" + escape(email) + "'></label>"
            + "<label>Парола <input type='password' name='password'></label>"
            + "<button>Вход</button>"
            + "</form>"
            + "<p>Нямаш акаунт? <a href='/register'>Регистрация</a></p>";
        sendHtml(ex,status,page("Вход",html));
    }

    private void logout(HttpExchange ex) throws IOException
    {
        if(ex.getRequestMethod().equals("POST"))
        {
            sessions.remove(getCookie(ex,"session"));
            ex.getResponseHeaders().add("Set-Cookie","session=; Path=/; Max-Age=0; HttpOnly; SameSite=Strict");
        }
        redirect(ex,"/login");
    }

    private void profile(HttpExchange ex) throws Exception
    {
        String email = getLoggedEmail(ex);
        if(email == null)
        {
            redirect(ex,"/login");
            return;
        }
        if(ex.getRequestMethod().equals("GET"))
        {
            User user = service.getUser(email);
            showProfilePage(ex,200,"",email,user.getFirstName(),user.getLastName());
            return;
        }
        Map<String, String> form = readForm(ex);
        String firstName = form.getOrDefault("firstName","").trim();
        String lastName = form.getOrDefault("lastName","").trim();
        String oldPassword = form.getOrDefault("oldPassword","");
        String newPassword = form.getOrDefault("newPassword","");
        String error = service.updateProfile(email,firstName,lastName,oldPassword,newPassword);
        if(error != null)
        {
            showProfilePage(ex,400,errorHtml(error),email,firstName,lastName);
            return;
        }
        showProfilePage(ex,200,"<p class='success'>Промените са запазени.</p>",email,firstName,lastName);
    }

    private void showProfilePage(HttpExchange ex, int status, String message, String email, String firstName, String lastName) throws IOException
    {
        String html = message
            + "<p>Имейл: <b>" + escape(email) + "</b></p>"
            + "<form method='post' action='/profile'>"
            + "<label>Име <input name='firstName' value='" + escape(firstName) + "'></label>"
            + "<label>Фамилия <input name='lastName' value='" + escape(lastName) + "'></label>"
            + "<p>Смяна на парола (остави празно, ако не искаш да я сменяш):</p>"
            + "<label>Стара парола <input type='password' name='oldPassword'></label>"
            + "<label>Нова парола <input type='password' name='newPassword'></label>"
            + "<button>Запази</button>"
            + "</form>"
            + "<form method='post' action='/logout'><button>Изход</button></form>";
        sendHtml(ex,status,page("Профил",html));
    }

    private String getLoggedEmail(HttpExchange ex)
    {
        return sessions.get(getCookie(ex,"session"));
    }

    private String getCookie(HttpExchange ex, String name)
    {
        String header = ex.getRequestHeaders().getFirst("Cookie");
        if(header == null)
        {
            return null;
        }
        for(String cookie : header.split(";"))
        {
            String[] parts = cookie.trim().split("=",2);
            if(parts.length == 2 && parts[0].equals(name))
            {
                return parts[1];
            }
        }
        return null;
    }

    private Map<String, String> readForm(HttpExchange ex) throws IOException
    {
        String body = new String(ex.getRequestBody().readAllBytes(),StandardCharsets.UTF_8);
        Map<String, String> form = new HashMap<>();
        for(String pair : body.split("&"))
        {
            String[] parts = pair.split("=",2);
            if(parts.length == 2)
            {
                form.put(URLDecoder.decode(parts[0],StandardCharsets.UTF_8),URLDecoder.decode(parts[1],StandardCharsets.UTF_8));
            }
        }
        return form;
    }

    private void sendHtml(HttpExchange ex, int status, String html) throws IOException
    {
        byte[] bytes = html.getBytes(StandardCharsets.UTF_8);
        ex.getResponseHeaders().set("Content-Type","text/html; charset=UTF-8");
        ex.sendResponseHeaders(status,bytes.length);
        try(OutputStream out = ex.getResponseBody())
        {
            out.write(bytes);
        }
    }

    private void redirect(HttpExchange ex, String url) throws IOException
    {
        ex.getResponseHeaders().set("Location",url);
        ex.sendResponseHeaders(303,-1);
        ex.close();
    }

    private String page(String title, String content)
    {
        return "<!DOCTYPE html><html lang='bg'><head><meta charset='UTF-8'>"
            + "<meta name='viewport' content='width=device-width, initial-scale=1'>"
            + "<title>" + title + "</title>"
            + "<style>"
            + "body{font-family:Arial,sans-serif;max-width:400px;margin:40px auto;padding:0 16px}"
            + "label{display:block;margin:10px 0}"
            + "input{width:100%;padding:6px;box-sizing:border-box}"
            + "button{padding:8px 16px;margin-top:10px}"
            + ".error{color:#c00}.success{color:#080}"
            + "</style></head><body>"
            + "<h1>" + title + "</h1>" + content
            + "</body></html>";
    }

    private String errorHtml(String error)
    {
        if(error == null)
        {
            return "";
        }
        return "<p class='error'>" + escape(error) + "</p>";
    }

    private String escape(String text)
    {
        if(text == null)
        {
            return "";
        }
        return text.replace("&","&amp;").replace("<","&lt;").replace(">","&gt;").replace("\"","&quot;").replace("'","&#39;");
    }
}
