public class Main {
    public static void main(String[] args) throws Exception{
        UserService service = new UserService(new MySqlUserRepository());
        WebServer server = new WebServer(service);
        server.start(8080);
        System.out.println("Server started: http://localhost:8080");
    }
}
