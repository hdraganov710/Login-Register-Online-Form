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
        try(Connection con = Database.getConnection();PreparedStatement ps = con.prepareStatement(sql))
        {
            ps.setString(1,email);
            try(ResultSet rs = ps.executeQuery())
            {
                if(rs.next())
                {
                    return new User(
                        rs.getString("email"),
                        rs.getString("password"),
                        rs.getString("first_name"),
                        rs.getString("last_name"),
                        rs.getString("salt")
                    );
                }
            }
            
        }
        return null;
    }
    public void update(User user) throws SQLException
    {
        String sql = "UPDATE users SET password = ?, salt = ?, first_name = ?, last_name = ? WHERE email = ?";
        try(Connection con = Database.getConnection();PreparedStatement ps = con.prepareStatement(sql))
        {
            ps.setString(1,user.getPasswordHash());
            ps.setString(2,user.getSalt());
            ps.setString(3,user.getFirstName());
            ps.setString(4,user.getLastName());
            ps.setString(5,user.getEmail());
            ps.executeUpdate();
        }
    }

}
