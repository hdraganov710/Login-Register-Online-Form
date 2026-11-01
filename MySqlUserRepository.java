import java.sql.*;

public class MySqlUserRepository implements UserRepository {
    public void save(User user) throws SQLException
    {
        String sql = "INSERT INTO users(email,password, salt,first_name,last_name) VALUES(?,?,?,?,?)";
        try(Connection con = Database.getConnection();PreparedStatement ps = con.prepareStatement(sql))
        {
            ps.setString(1, user.getEmail());
            ps.setString(2,user.getPasswordHash());
            ps.setString(3,user.getSalt());
            ps.setString(4,user.getFirstName());
            ps.setString(5,user.getLastName());
            ps.executeUpdate();
        }
    }
    public User findByEmail(String email) throws SQLException{
        String sql = "SELECT * FROM users WHERE email = ?";
        return null;
    }

}
