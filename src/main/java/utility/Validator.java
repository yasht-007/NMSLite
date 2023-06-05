package utility;

public class Validator
{
   public static boolean checkNull(String input)
    {
        return input == null || input.equalsIgnoreCase("");
    }
}
