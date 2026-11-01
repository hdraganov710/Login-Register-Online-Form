import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

public class PasswordHasherTest
{
    @Test
    void saltIsRandom()
    {
        byte[] salt1 = PasswordHasher.generateSalt();
        byte[] salt2 = PasswordHasher.generateSalt();
        assertEquals(16,salt1.length);
        assertFalse(java.util.Arrays.equals(salt1,salt2));
    }

    @Test
    void sameSaltGivesSameHash() throws Exception
    {
        byte[] salt = PasswordHasher.generateSalt();
        assertArrayEquals(PasswordHasher.hash("parola123",salt),PasswordHasher.hash("parola123",salt));
    }

    @Test
    void differentSaltGivesDifferentHash() throws Exception
    {
        byte[] hash1 = PasswordHasher.hash("parola123",PasswordHasher.generateSalt());
        byte[] hash2 = PasswordHasher.hash("parola123",PasswordHasher.generateSalt());
        assertFalse(java.util.Arrays.equals(hash1,hash2));
    }

    @Test
    void verify() throws Exception
    {
        byte[] salt = PasswordHasher.generateSalt();
        byte[] hash = PasswordHasher.hash("parola123",salt);
        assertTrue(PasswordHasher.verify("parola123",salt,hash));
        assertFalse(PasswordHasher.verify("greshna123",salt,hash));
    }
}
