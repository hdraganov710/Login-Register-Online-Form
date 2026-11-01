import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

public class CaptchaTest
{
    @Test
    void generateText()
    {
        String text = Captcha.generateText();
        assertEquals(5,text.length());
        assertTrue(text.matches("[A-Z2-9]+"));
        assertNotEquals(Captcha.generateText(),Captcha.generateText());
    }

    @Test
    void createImageIsPng() throws Exception
    {
        byte[] image = Captcha.createImage("ABC23");
        assertTrue(image.length > 0);
        assertEquals((byte) 0x89,image[0]);
        assertEquals('P',image[1]);
        assertEquals('N',image[2]);
        assertEquals('G',image[3]);
    }
}
