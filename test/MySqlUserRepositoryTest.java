import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;
import java.sql.Connection;
import java.sql.PreparedStatement;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class MySqlUserRepositoryTest
{
    private MySqlUserRepository repo = new MySqlUserRepository();
    private String email = "test" + System.currentTimeMillis() + "@test.bg";

    @BeforeEach
    void checkDatabase()
    {
        assumeTrue(System.getenv("DB_PASS") != null,"DB_PASS is not set");
    }

    @AfterEach
    void deleteTestUser() throws Exception
    {
        if(System.getenv("DB_PASS") == null)
        {
            return;
        }
        try(Connection con = Database.getConnection();PreparedStatement ps = con.prepareStatement("DELETE FROM users WHERE email = ?"))
        {
            ps.setString(1,email);
            ps.executeUpdate();
        }
    }

    @Test
    void saveFindAndUpdate() throws Exception
    {
        repo.save(new User(email,"hash","Ivan","Petrov","salt"));

        User user = repo.findByEmail(email);
        assertEquals(email,user.getEmail());
        assertEquals("hash",user.getPasswordHash());
        assertEquals("Ivan",user.getFirstName());
        assertEquals("Petrov",user.getLastName());
        assertEquals("salt",user.getSalt());

        repo.update(new User(email,"newhash","Georgi","Ivanov","newsalt"));
        user = repo.findByEmail(email);
        assertEquals("newhash",user.getPasswordHash());
        assertEquals("Georgi",user.getFirstName());
        assertEquals("Ivanov",user.getLastName());
        assertEquals("newsalt",user.getSalt());
    }

    @Test
    void findMissingUser() throws Exception
    {
        assertNull(repo.findByEmail(email));
    }
}
