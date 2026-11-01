import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class UserServiceTest
{
    private FakeUserRepository repo;
    private UserService service;

    @BeforeEach
    void setUp()
    {
        repo = new FakeUserRepository();
        service = new UserService(repo);
    }

    @Test
    void registerSavesUserWithHashedPassword() throws Exception
    {
        assertNull(service.register("ivan@abv.bg","Ivan","Petrov","parola123"));
        User user = repo.findByEmail("ivan@abv.bg");
        assertNotNull(user);
        assertEquals("Ivan",user.getFirstName());
        assertEquals("Petrov",user.getLastName());
        assertNotEquals("parola123",user.getPasswordHash());
        assertNotNull(user.getSalt());
    }

    @Test
    void registerWithInvalidData() throws Exception
    {
        assertNotNull(service.register("ivan","Ivan","Petrov","parola123"));
        assertNotNull(service.register("ivan@abv.bg","I","Petrov","parola123"));
        assertNotNull(service.register("ivan@abv.bg","Ivan","P","parola123"));
        assertNotNull(service.register("ivan@abv.bg","Ivan","Petrov","123"));
        assertNull(repo.findByEmail("ivan@abv.bg"));
    }

    @Test
    void registerSameEmailTwice() throws Exception
    {
        assertNull(service.register("ivan@abv.bg","Ivan","Petrov","parola123"));
        assertNotNull(service.register("ivan@abv.bg","Georgi","Ivanov","parola456"));
    }

    @Test
    void login() throws Exception
    {
        service.register("ivan@abv.bg","Ivan","Petrov","parola123");
        assertNotNull(service.login("ivan@abv.bg","parola123"));
        assertNull(service.login("ivan@abv.bg","greshna123"));
        assertNull(service.login("nqma@abv.bg","parola123"));
        assertNull(service.login("ivan@abv.bg",null));
    }

    @Test
    void getUser() throws Exception
    {
        service.register("ivan@abv.bg","Ivan","Petrov","parola123");
        assertEquals("ivan@abv.bg",service.getUser("ivan@abv.bg").getEmail());
        assertNull(service.getUser("nqma@abv.bg"));
    }

    @Test
    void updateNamesOnly() throws Exception
    {
        service.register("ivan@abv.bg","Ivan","Petrov","parola123");
        assertNull(service.updateProfile("ivan@abv.bg","Georgi","Ivanov","",""));
        User user = repo.findByEmail("ivan@abv.bg");
        assertEquals("Georgi",user.getFirstName());
        assertEquals("Ivanov",user.getLastName());
        assertNotNull(service.login("ivan@abv.bg","parola123"));
    }

    @Test
    void updatePassword() throws Exception
    {
        service.register("ivan@abv.bg","Ivan","Petrov","parola123");
        assertNull(service.updateProfile("ivan@abv.bg","Ivan","Petrov","parola123","novaparola1"));
        assertNull(service.login("ivan@abv.bg","parola123"));
        assertNotNull(service.login("ivan@abv.bg","novaparola1"));
    }

    @Test
    void updateWithErrors() throws Exception
    {
        service.register("ivan@abv.bg","Ivan","Petrov","parola123");
        assertNotNull(service.updateProfile("nqma@abv.bg","Ivan","Petrov","",""));
        assertNotNull(service.updateProfile("ivan@abv.bg","I","Petrov","",""));
        assertNotNull(service.updateProfile("ivan@abv.bg","Ivan","P","",""));
        assertNotNull(service.updateProfile("ivan@abv.bg","Ivan","Petrov","greshna123","novaparola1"));
        assertNotNull(service.updateProfile("ivan@abv.bg","Ivan","Petrov","parola123","123"));
        assertNotNull(service.login("ivan@abv.bg","parola123"));
    }
}
