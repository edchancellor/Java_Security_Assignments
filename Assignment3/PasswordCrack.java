import java.io.*;
import java.util.*;

public class PasswordCrack
{
    final static String[] CHARACTERS = {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "a", "b", "c", "d", "e", "f", "g", "h", "i", 
    "j", "k", "l", "m", "n", "o", "p", "q", "r", "s", "t", "u", "v", "w", "x", "y", "z", "A", "B", "C", "D", "E", "F", "G", "H", "I",
    "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z"};
    private static ArrayList<String> words;
    private static HashMap<String,String> salts_enc;

    public static void main (String args[]) throws FileNotFoundException
    {
        if (args.length != 2)
        { 
            System.out.println("Usage: 2 arguments required: PasswordCrack <dictionary> <passwd>");
            System.exit(1);
        }

        // Get encrypted passwords, salts and names
        String passwd = args[1];
        salts_enc = new HashMap<String,String>();
        words = new ArrayList<String>();

        try
        {
            Scanner sc = new Scanner(new File(passwd));
            while(sc.hasNext())
            {
                String line = sc.nextLine();
                String[] params = line.split(":");
                salts_enc.put(params[1].substring(0,2), params[1]);
                String [] usernames = params[4].split(" ");
                for(int j = 0; j < usernames.length; j ++)
                {
                    words.add(usernames[j]);
                }
            }
            sc.close();
        }
        catch(FileNotFoundException e1)
        {
            System.out.println("Password file " + passwd + " cannot be opened.");
            System.exit(1);
        }
        catch(Exception e2)
        {
            System.out.println("Error when loading from password file: " + passwd);
            System.out.println("Ensure that password file has correct format.");
            System.exit(1);
        }
        

        // Add common passwords
        words.add("123456");
	    words.add("123456789");
	    words.add("picture1");
	    words.add("password");
	    words.add("12345678");
	    words.add("111111");
	    words.add("123123");
	    words.add("12345");
	    words.add("1234567890");
	    words.add("senha");
	    words.add("1234567");
	    words.add("qwerty");
	    words.add("abc123");
	    words.add("Million2");
	    words.add("000000");
	    words.add("1234");
	    words.add("iloveyou");
	    words.add("aaron431");
	    words.add("password1");
	    words.add("qqww1122");
        words.add("letmein");
        words.add("qwertyuiop");
        words.add("login");
        words.add("passw0rd");

        // Add dictionary words
        String dictionary = args[0];

        try
        {
            Scanner sc2 = new Scanner(new File(dictionary));
            while(sc2.hasNext())
            {
                String line = sc2.nextLine();
                words.add(line);
            }
            sc2.close();
        }
        catch(FileNotFoundException e3)
        {
            System.out.println("Dictionary file " + dictionary + " cannot be opened.");
            System.exit(1);
        }
        catch(Exception e4)
        {
            System.out.println("Error when loading from dictionary file: " + dictionary);
            System.out.println("Ensure that dictionary file has correct format.");
            System.exit(1);
        }

        // Check list unmangled:
        for (int i = 0; i < words.size(); i++) 
        {
            String word = words.get(i);
            check(word);
        }

        // Check list with one mangle
        mangle();

        // Check list with two mangles
        mangle2();

    }

    private static String check(String word)
    {
        Iterator<Map.Entry<String,String>> iter = salts_enc.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry<String,String> entry = iter.next();
            String salt = entry.getKey();
            String pass = entry.getValue();
            String test = jcrypt.crypt(salt, word);
            if(test.equals(pass))
            {
                System.out.println(word);
                iter.remove(); 
            }
        }
        return word;
    }

    // Mangled results are saved, in order to mangle them a second time.
    private static void mangle()
    {
        ArrayList<String> new_mangle = new ArrayList<String>();
        for (int i = 0; i < words.size(); i++) 
        {
            String word = words.get(i);

            // These only produce different hash if word is less than 8
            if(word.length() < 8)
            {
                new_mangle.add(check(duplicate(word)));
                for(int j = 0; j < CHARACTERS.length; j ++)
                {
                    new_mangle.add(check(append(word, CHARACTERS[j])));
                }
            }

            // This only produces different hash if word is less than or equal to 8
            if(word.length() <= 8)
            {
                new_mangle.add(check(deleteLast(word)));
            }

            new_mangle.add(check(deleteFirst(word)));
            new_mangle.add(check(reverse(word)));    
            new_mangle.add(check(reflect1(word)));
            new_mangle.add(check(reflect2(word)));
            new_mangle.add(check(uppercase(word)));
            new_mangle.add(check(lowercase(word)));
            new_mangle.add(check(capitalise(word)));
            new_mangle.add(check(nCapitalise(word)));
            new_mangle.add(check(toggleCase1(word)));
            new_mangle.add(check(toggleCase2(word)));
            for(int j = 0; j < CHARACTERS.length; j ++)
            {
                new_mangle.add(check(prepend(word, CHARACTERS[j])));
            }
        }
        words = new_mangle;
    }

    // mangle2 is almost identical to mangle, but doesn't save the words into a list array as it takes up too much memory
    private static void mangle2()
    {
        ArrayList<String> new_mangle = new ArrayList<String>();
        for (int i = 0; i < words.size(); i++) 
        {
            String word = words.get(i);

            // These only produce different hash if word is less than 8
            if(word.length() < 8)
            {
                check(duplicate(word));
                for(int j = 0; j < CHARACTERS.length; j ++)
                {
                    check(append(word, CHARACTERS[j]));
                }
            }

            // This only produces different hash if word is less than or equal to 8
            if(word.length() <= 8)
            {
                check(deleteLast(word));
            }

            check(deleteFirst(word));
            check(reverse(word));    
            check(reflect1(word));
            check(reflect2(word));
            check(uppercase(word));
            check(lowercase(word));
            check(capitalise(word));
            check(nCapitalise(word));
            check(toggleCase1(word));
            check(toggleCase2(word));
            for(int j = 0; j < CHARACTERS.length; j ++)
            {
                check(prepend(word, CHARACTERS[j]));
            }
        }
    }

    // Mangle functions
    
    private static String prepend(String str, String X)
    {
        return X+str;
    }

    private static String append(String str, String X)
    {
        return str+X;
    }

    private static String deleteFirst(String str)
    {
        if(str == "")
        {
            return str;
        }
        else
        {
            return str.substring(1);
        }
    }

    private static String deleteLast(String str)
    {
        if(str == "")
        {
            return str;
        }
        else
        {
            return str.substring(0,str.length() - 1);
        }
    }

    private static String reverse(String str)
    {
        if(str == "")
        {
            return str;
        }
        else
        {
            byte[] normal = str.getBytes();
            byte[] reverse = new byte[normal.length];
            for (int i = 0; i < normal.length; i++)
            {
                reverse[i] = normal[normal.length - i - 1];
            }
            return new String(reverse);
        }
    }

    private static String duplicate(String str)
    {
        return str+str;
    }

    private static String reflect1(String str)
    {
        return str+reverse(str);
    }

    private static String reflect2(String str)
    {
        return reverse(str)+str;
    }

    private static String uppercase(String str)
    {
        return str.toUpperCase();
    }

    private static String lowercase(String str)
    {
        return str.toLowerCase();
    }

    private static String capitalise(String str)
    {
        if(str == "")
        {
            return str;
        }
        else
        {
            return str.substring(0, 1).toUpperCase()+str.substring(1).toLowerCase();
        }
    }

    private static String nCapitalise(String str)
    {
        if(str == "")
        {
            return str;
        }
        else
        {
            return str.substring(0, 1).toLowerCase()+str.substring(1).toUpperCase();
        }
    }

    private static String toggleCase1(String str)
    {
        String result = "";
        int length = str.length();
        int index = 0;
        while(index < length)
        {
            result = result + str.substring(index, index + 1).toUpperCase();
            index ++;
            if(index < length)
            {
                result = result + str.substring(index, index + 1).toLowerCase();
                index ++;
            }
            else
            {
                break;
            }
        }
        return result;
    }

    private static String toggleCase2(String str)
    {
        String result = "";
        int length = str.length();
        int index = 0;
        while(index < length)
        {
            result = result + str.substring(index, index + 1).toLowerCase();
            index ++;
            if(index < length)
            {
                result = result + str.substring(index, index + 1).toUpperCase();
                index ++;
            }
            else
            {
                break;
            }
        }
        return result;
    }
}