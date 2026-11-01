public class User{
    private String email;
    private String passwordHash;
    private String firstName;
    private String lastName;
    private String salt;
    public User(String email, String passwordHash,String firstName,String lastName,String salt){
        this.email = email;
        this.passwordHash = passwordHash;
        this.firstName = firstName;
        this.lastName = lastName;
        this.salt = salt;
    }
    public String getEmail()
    {
        return this.email;
    }
    public String getPasswordHash()
    {
        return this.passwordHash;
    }
    public String getFirstName()
    {
        return this.firstName;
    }
    public String getLastName()
    {
        return this.lastName;
    }
    public String getSalt()
    {
        return this.salt;
    }
    
}