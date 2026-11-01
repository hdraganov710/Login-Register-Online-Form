import java.security.SecureRandom;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.SecretKeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.MessageDigest;

public class PasswordHasher {
    private static final int iterations = 210000;
    private static final int keyLength = 256;
    public static byte[] generateSalt()
    {
        byte[] salt = new byte[16];
        SecureRandom sr = new SecureRandom();
        sr.nextBytes(salt);
        return salt;
    }
    public static byte[] hash(String password, byte[] salt) throws NoSuchAlgorithmException, InvalidKeySpecException
    {
        PBEKeySpec spec = new PBEKeySpec(password.toCharArray(),salt,iterations,keyLength);
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        return factory.generateSecret(spec).getEncoded();
    }
    public static boolean verify(String password, byte[] salt, byte[] expectedHash) throws InvalidKeySpecException,NoSuchAlgorithmException
    {
        byte[] hash = hash(password,salt);
        return MessageDigest.isEqual(expectedHash,hash);
    }
}
