import java.sql.*;
import java.util.*;
public class Main {
    public static void main(String[] args) throws Exception{
       try(Connection con = Database.getConnection())
       {
        System.out.println("Connected!");
       }
       catch(SQLException e)
       {
        System.out.println(e.getMessage());
       }
       byte[] salt1 = PasswordHasher.generateSalt();
       byte[] salt2 = PasswordHasher.generateSalt();
       byte[] hash1 = PasswordHasher.hash("parola123", salt1);
       byte[] hash2 = PasswordHasher.hash("parola123", salt1);
       byte[] hash3 = PasswordHasher.hash("parola123",salt2);
       System.out.println("Hash1: " + Base64.getEncoder().encodeToString(hash1));
       System.out.println("Hash2: " + Base64.getEncoder().encodeToString(hash2));
       System.out.println("Hash3: " + Base64.getEncoder().encodeToString(hash3));
       System.out.println(PasswordHasher.verify("parola123",salt1,hash1));
       System.out.println(PasswordHasher.verify("greshna",salt1,hash1));
    }
}
