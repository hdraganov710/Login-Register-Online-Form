import java.util.Base64;

public class UserService
{
    private UserRepository repo;

    public UserService(UserRepository repo)
    {
        this.repo = repo;
    }

    public String register(String email, String firstName, String lastName, String password) throws Exception
    {
        String error = Validator.validateEmail(email);
        if(error == null) error = Validator.validateName(firstName);
        if(error == null) error = Validator.validateName(lastName);
        if(error == null) error = Validator.validatePassword(password);
        if(error != null)
        {
            return error;
        }
        if(repo.findByEmail(email) != null)
        {
            return "Вече има потребител с този имейл.";
        }
        byte[] salt = PasswordHasher.generateSalt();
        byte[] hash = PasswordHasher.hash(password,salt);
        User user = new User(email,Base64.getEncoder().encodeToString(hash),firstName,lastName,Base64.getEncoder().encodeToString(salt));
        repo.save(user);
        return null;
    }

    public User login(String email, String password) throws Exception
    {
        User user = repo.findByEmail(email);
        if(user == null || password == null)
        {
            return null;
        }
        byte[] salt = Base64.getDecoder().decode(user.getSalt());
        byte[] hash = Base64.getDecoder().decode(user.getPasswordHash());
        if(PasswordHasher.verify(password,salt,hash))
        {
            return user;
        }
        return null;
    }

    public User getUser(String email) throws Exception
    {
        return repo.findByEmail(email);
    }

    public String updateProfile(String email, String firstName, String lastName, String oldPassword, String newPassword) throws Exception
    {
        User user = repo.findByEmail(email);
        if(user == null)
        {
            return "Потребителят не съществува.";
        }
        String error = Validator.validateName(firstName);
        if(error == null) error = Validator.validateName(lastName);
        if(error != null)
        {
            return error;
        }
        String passwordHash = user.getPasswordHash();
        String salt = user.getSalt();
        if(newPassword != null && !newPassword.isEmpty())
        {
            if(login(email,oldPassword) == null)
            {
                return "Старата парола е грешна.";
            }
            error = Validator.validatePassword(newPassword);
            if(error != null)
            {
                return error;
            }
            byte[] newSalt = PasswordHasher.generateSalt();
            passwordHash = Base64.getEncoder().encodeToString(PasswordHasher.hash(newPassword,newSalt));
            salt = Base64.getEncoder().encodeToString(newSalt);
        }
        repo.update(new User(email,passwordHash,firstName,lastName,salt));
        return null;
    }
}
