public class Validator
{
    public static String validateEmail(String email)
    {
        if(email == null || email.isEmpty())
        {
            return "Въведете имейл.";
        }
        if(email.length() > 50 || !email.matches("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"))
        {
            return "Невалиден имейл.";
        }
        return null;
    }

    public static String validateName(String name)
    {
        if(name == null || name.length() < 2 || name.length() > 50)
        {
            return "Имената трябва да са между 2 и 50 символа.";
        }
        if(!name.matches("^[A-Za-zА-Яа-я-]+$"))
        {
            return "Имената може да съдържат само букви и тире.";
        }
        return null;
    }

    public static String validatePassword(String password)
    {
        if(password == null || password.length() < 8)
        {
            return "Паролата трябва да е поне 8 символа.";
        }
        if(!password.matches(".*[0-9].*") || !password.matches(".*[A-Za-zА-Яа-я].*"))
        {
            return "Паролата трябва да съдържа поне една буква и една цифра.";
        }
        return null;
    }
}
