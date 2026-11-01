import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class FakeUserRepository implements UserRepository
{
    private Map<String, User> users = new HashMap<>();

    public User findByEmail(String email) throws SQLException
    {
        return users.get(email);
    }

    public void save(User user) throws SQLException
    {
        users.put(user.getEmail(),user);
    }

    public void update(User user) throws SQLException
    {
        users.put(user.getEmail(),user);
    }
}
