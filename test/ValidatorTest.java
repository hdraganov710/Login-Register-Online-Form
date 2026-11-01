import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

public class ValidatorTest
{
    @Test
    void validEmail()
    {
        assertNull(Validator.validateEmail("ivan@abv.bg"));
        assertNull(Validator.validateEmail("ivan.petrov+test@gmail.com"));
    }

    @Test
    void invalidEmail()
    {
        assertNotNull(Validator.validateEmail(null));
        assertNotNull(Validator.validateEmail(""));
        assertNotNull(Validator.validateEmail("ivan"));
        assertNotNull(Validator.validateEmail("ivan@abv"));
        assertNotNull(Validator.validateEmail("ivan @abv.bg"));
        assertNotNull(Validator.validateEmail("a".repeat(45) + "@abv.bg"));
    }

    @Test
    void validName()
    {
        assertNull(Validator.validateName("Ivan"));
        assertNull(Validator.validateName("Иван"));
        assertNull(Validator.validateName("Ivanova-Petrova"));
    }

    @Test
    void invalidName()
    {
        assertNotNull(Validator.validateName(null));
        assertNotNull(Validator.validateName("I"));
        assertNotNull(Validator.validateName("Ivan1"));
        assertNotNull(Validator.validateName("<script>"));
        assertNotNull(Validator.validateName("a".repeat(51)));
    }

    @Test
    void validPassword()
    {
        assertNull(Validator.validatePassword("parola123"));
        assertNull(Validator.validatePassword("парола123"));
    }

    @Test
    void invalidPassword()
    {
        assertNotNull(Validator.validatePassword(null));
        assertNotNull(Validator.validatePassword("abc12"));
        assertNotNull(Validator.validatePassword("parolaparola"));
        assertNotNull(Validator.validatePassword("12345678"));
    }
}
