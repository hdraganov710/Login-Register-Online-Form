import java.sql.*;

public interface UserRepository{
    User findByEmail(String email) throws SQLException;
    void save(User user) throws SQLException;
    void update(User user) throws SQLException;
}