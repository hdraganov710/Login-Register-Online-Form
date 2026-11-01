import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.SecureRandom;
import javax.imageio.ImageIO;

public class Captcha
{
    private static final String chars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private static SecureRandom random = new SecureRandom();

    public static String generateText()
    {
        String text = "";
        for(int i = 0; i < 5; i++)
        {
            text += chars.charAt(random.nextInt(chars.length()));
        }
        return text;
    }

    public static byte[] createImage(String text) throws IOException
    {
        BufferedImage image = new BufferedImage(150,50,BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();
        g.setColor(Color.WHITE);
        g.fillRect(0,0,150,50);
        for(int i = 0; i < 10; i++)
        {
            g.setColor(new Color(random.nextInt(200),random.nextInt(200),random.nextInt(200)));
            g.drawLine(random.nextInt(150),random.nextInt(50),random.nextInt(150),random.nextInt(50));
        }
        g.setFont(new Font("Arial",Font.BOLD,30));
        for(int i = 0; i < text.length(); i++)
        {
            g.setColor(new Color(random.nextInt(150),random.nextInt(150),random.nextInt(150)));
            g.drawString(String.valueOf(text.charAt(i)),10 + i * 27,30 + random.nextInt(15));
        }
        g.dispose();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ImageIO.write(image,"png",out);
        return out.toByteArray();
    }
}
