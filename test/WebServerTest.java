import static org.junit.jupiter.api.Assertions.*;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class WebServerTest
{
    private WebServer server;
    private String url;
    private HttpClient client = HttpClient.newHttpClient();
    private Map<String, String> cookies = new HashMap<>();

    @BeforeEach
    void start() throws Exception
    {
        startServer(new FakeUserRepository());
    }

    @AfterEach
    void stop()
    {
        server.stop();
    }

    private void startServer(UserRepository repo) throws Exception
    {
        server = new WebServer(new UserService(repo));
        url = "http://localhost:" + server.start(0);
    }

    private HttpResponse<String> get(String path) throws Exception
    {
        HttpRequest.Builder request = HttpRequest.newBuilder(URI.create(url + path)).GET();
        return send(request);
    }

    private HttpResponse<String> post(String path, String... fields) throws Exception
    {
        String body = "";
        for(int i = 0; i < fields.length; i += 2)
        {
            if(!body.isEmpty()) body += "&";
            body += fields[i] + "=" + URLEncoder.encode(fields[i + 1],StandardCharsets.UTF_8);
        }
        HttpRequest.Builder request = HttpRequest.newBuilder(URI.create(url + path))
            .header("Content-Type","application/x-www-form-urlencoded")
            .POST(HttpRequest.BodyPublishers.ofString(body));
        return send(request);
    }

    private HttpResponse<String> send(HttpRequest.Builder request) throws Exception
    {
        String cookieHeader = "";
        for(String name : cookies.keySet())
        {
            if(!cookieHeader.isEmpty()) cookieHeader += "; ";
            cookieHeader += name + "=" + cookies.get(name);
        }
        if(!cookieHeader.isEmpty())
        {
            request.header("Cookie",cookieHeader);
        }
        HttpResponse<String> response = client.send(request.build(),HttpResponse.BodyHandlers.ofString());
        for(String setCookie : response.headers().allValues("Set-Cookie"))
        {
            String[] parts = setCookie.split(";")[0].split("=",2);
            if(parts[1].isEmpty()) cookies.remove(parts[0]);
            else cookies.put(parts[0],parts[1]);
        }
        return response;
    }

    private String location(HttpResponse<String> response)
    {
        return response.headers().firstValue("Location").orElse("");
    }

    private HttpResponse<String> register(String email, String password) throws Exception
    {
        get("/register");
        String answer = server.getCaptchaText(cookies.get("captcha"));
        return post("/register","email",email,"firstName","Ivan","lastName","Petrov","password",password,"captcha",answer.toLowerCase());
    }

    @Test
    void homeRedirectsToLogin() throws Exception
    {
        HttpResponse<String> response = get("/");
        assertEquals(303,response.statusCode());
        assertEquals("/login",location(response));
    }

    @Test
    void unknownPage() throws Exception
    {
        assertEquals(404,get("/nqma").statusCode());
    }

    @Test
    void registerPageHasForm() throws Exception
    {
        HttpResponse<String> response = get("/register");
        assertEquals(200,response.statusCode());
        assertTrue(response.body().contains("name='email'"));
        assertTrue(cookies.containsKey("captcha"));
    }

    @Test
    void captchaImage() throws Exception
    {
        assertEquals(404,get("/captcha").statusCode());
        get("/register");
        HttpResponse<String> response = get("/captcha");
        assertEquals(200,response.statusCode());
        assertEquals("image/png",response.headers().firstValue("Content-Type").orElse(""));
    }

    @Test
    void wrongCaptcha() throws Exception
    {
        get("/register");
        HttpResponse<String> response = post("/register","email","ivan@abv.bg","firstName","Ivan","lastName","Petrov","password","parola123","captcha","XXXXX");
        assertEquals(400,response.statusCode());
        assertTrue(response.body().contains("Грешен код"));
    }

    @Test
    void registerWithInvalidData() throws Exception
    {
        HttpResponse<String> response = register("ivan","parola123");
        assertEquals(400,response.statusCode());
        assertTrue(response.body().contains("Невалиден имейл"));
    }

    @Test
    void registerLoginProfileLogout() throws Exception
    {
        HttpResponse<String> response = register("Ivan@abv.bg","parola123");
        assertEquals(303,response.statusCode());
        assertEquals("/login?registered",location(response));

        assertTrue(get("/login?registered").body().contains("Регистрацията е успешна"));

        response = post("/login","email","ivan@abv.bg","password","parola123");
        assertEquals(303,response.statusCode());
        assertEquals("/profile",location(response));

        assertEquals("/profile",location(get("/")));
        assertEquals("/profile",location(get("/login")));

        response = get("/profile");
        assertEquals(200,response.statusCode());
        assertTrue(response.body().contains("Ivan"));

        response = post("/profile","firstName","Georgi","lastName","Ivanov","oldPassword","","newPassword","");
        assertEquals(200,response.statusCode());
        assertTrue(response.body().contains("Промените са запазени"));
        assertTrue(response.body().contains("Georgi"));

        response = post("/profile","firstName","G","lastName","Ivanov","oldPassword","","newPassword","");
        assertEquals(400,response.statusCode());

        response = post("/logout");
        assertEquals(303,response.statusCode());
        assertEquals("/login",location(get("/profile")));
    }

    @Test
    void wrongPassword() throws Exception
    {
        register("ivan@abv.bg","parola123");
        HttpResponse<String> response = post("/login","email","ivan@abv.bg","password","greshna123");
        assertEquals(401,response.statusCode());
        assertTrue(response.body().contains("Грешен имейл или парола"));
    }

    @Test
    void profileNeedsLogin() throws Exception
    {
        assertEquals("/login",location(get("/profile")));
    }

    @Test
    void logoutWithGet() throws Exception
    {
        assertEquals("/login",location(get("/logout")));
    }

    @Test
    void escapesHtml() throws Exception
    {
        get("/register");
        HttpResponse<String> response = post("/register","email","<script>","firstName","Ivan","lastName","Petrov","password","parola123","captcha","XXXXX");
        assertFalse(response.body().contains("<script>"));
        assertTrue(response.body().contains("&lt;script&gt;"));
    }

    @Test
    void serverError() throws Exception
    {
        server.stop();
        startServer(new FakeUserRepository()
        {
            public User findByEmail(String email) throws SQLException
            {
                throw new SQLException("DB is down");
            }
        });
        HttpResponse<String> response = post("/login","email","ivan@abv.bg","password","parola123");
        assertEquals(500,response.statusCode());
    }
}
