import java.io.*;
import static javax.crypto.Cipher.*;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import javax.crypto.SecretKey;
import javax.crypto.CipherOutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import javax.crypto.spec.IvParameterSpec;
import java.util.Random;

public class Hidenc
{
    private static byte [] data;

    public static void main(String args[])
    {
        // *** CHECK ARGUMENTS *** //

        if(args.length < 4 || args.length > 6)
        {
            System.out.println("Incorrect number of arguments provided to program.");
            System.exit(1);
        }

        HashMap<String,String> arguments = new HashMap<String,String>();
        String encryptionAlgorithm = "";
        Random rand = new Random();

        for(int i = 0; i < args.length; i ++)
        {
            if(!args[i].startsWith("--")) {
                System.out.println("Arguments must start with \"--\".");
                System.exit(1);
            }

            String[] parameters = args[i].substring(2).split("=", 2);
			
            if(parameters.length != 2 || parameters[1].length() < 1) {
                System.out.println("Invalid argument format detected.");
                System.exit(1);
            }

            arguments.put(parameters[0], parameters[1]);
        }

        if(arguments.get("ctr") == null)
        {
            encryptionAlgorithm = "AES/ECB/NoPadding";         
        }
        else
        {
            encryptionAlgorithm = "AES/CTR/NoPadding";
        }

        if (arguments.get("key") == null || arguments.get("input") == null || arguments.get("output") == null)
        {
            System.out.println("Incorrect arguments provided. Must include \"key\", \"input\", \"output\", \"template\" or \"size\", and optionally \"ctr\" and \"offset\"");
            System.exit(1);
        }

        if(!(arguments.get("template") == null ^ arguments.get("size") == null))
        {
            System.out.println("Must provide either \"template\" or \"size\", but not both.");
            System.exit(1);
        }

        if(arguments.get("offset") == null)
        {
            if(arguments.get("template") != null)
            {
                File temFile = new File(arguments.get("template"));
                Long sizeOfTemplate = temFile.length();

                File inFile = new File(arguments.get("input"));
                Long sizeOfInput = inFile.length();

                if(sizeOfTemplate - sizeOfInput - 48 > 0)
                {
                    // create offset
                    int offset = rand.nextInt((int)(sizeOfTemplate - sizeOfInput - 48));
                    arguments.put("offset", Integer.toString(offset));
                }
                else
                {
                    System.out.println("Template file too small to fit input.");
                    System.exit(1);
                }
            }
            else
            {
                Long size = Long.parseLong(arguments.get("size"));

                File inFile = new File(arguments.get("input"));
                Long sizeOfInput = inFile.length();

                if((size - sizeOfInput - 48 > 0) && size > 0)
                {
                    // create offset
                    int offset = rand.nextInt((int)(size - sizeOfInput - 48));
                    arguments.put("offset", Integer.toString(offset));
                }
                else
                {
                    System.out.println("Size parameter too small to fit input.");
                    System.exit(1);
                }
            }
        }
        else
        {
            Long offset = Long.parseLong(arguments.get("offset"));

            File inFile = new File(arguments.get("input"));
            Long sizeOfInput = inFile.length();
            if(arguments.get("template") != null)
            {
                File temFile = new File(arguments.get("template"));
                Long sizeOfTemplate = temFile.length();

                if((offset + sizeOfInput + 48 > sizeOfTemplate) || offset < 0)
                {
                    System.out.println("Offset parameter not able to fit input in container.");
                    System.exit(1);
                }
            }
            else
            {
                Long size = Long.parseLong(arguments.get("size"));

                if(size < 0)
                {
                    System.out.println("Size parameter too small to fit input.");
                    System.exit(1);
                }

                if((offset + sizeOfInput + 48 > size) || offset < 0)
                {
                    System.out.println("Offset parameter not able to fit input in container.");
                    System.exit(1);
                }
            }
        }

        // *** GET KEY *** //

        byte[] keyBytes = null;
    
        try
        {
            keyBytes = new byte[arguments.get("key").length()/2];
            int j = 0;
            for(int i = 0; i < arguments.get("key").length(); i= i + 2)
            {
                keyBytes[j] = (byte)Integer.parseInt(arguments.get("key").substring(i, i + 2), 16);
                j ++;
            }
        }
        catch(Exception e)
        {
            System.out.println("Error parsing key: " + arguments.get("key"));
            System.exit(1);
        }

        // *** HASH KEY *** //

        MessageDigest md = null;
        byte[] hashedKey = null;
        String hashAlgorithm = "MD5";

        try
        {
            md = MessageDigest.getInstance(hashAlgorithm);
            md.update(keyBytes);
            hashedKey = md.digest();
        }
        catch(NoSuchAlgorithmException e)
        {
            System.out.println("Invalid hashing algorithm.");
            System.exit(1);
        }
        catch(Exception e)
        {
            System.out.println("Error hashing key.");
            System.exit(1);
        }

        // *** GET CTR IV (IF APPLICABLE) *** //

        byte[] IVBytes = null;
        if(arguments.get("ctr") != null)
        {
            try
            {
                IVBytes = new byte[arguments.get("ctr").length()/2];
                int j = 0;
                for(int i = 0; i < arguments.get("ctr").length(); i= i + 2)
                {
                    IVBytes[j] = (byte)Integer.parseInt(arguments.get("ctr").substring(i, i + 2), 16);
                    j ++;
                }
            }
            catch(Exception e)
            {
                System.out.println("Error parsing ctr: " + arguments.get("ctr"));
                System.exit(1);
            }
        }

        // *** SET UP CIPHER *** //

        Cipher cipher = null;
        SecretKey sk = null;
        IvParameterSpec IV = null;

        try
        {
            cipher = Cipher.getInstance(encryptionAlgorithm);
            sk = new SecretKeySpec(keyBytes, "AES");
            
            if(arguments.get("ctr") != null)
            {
                IV = new IvParameterSpec(IVBytes);
                cipher.init(ENCRYPT_MODE,sk,IV);
            }
            else
            {
                cipher.init(ENCRYPT_MODE,sk);
            }
        }
        catch(Exception e)
        {
            System.out.println("Error setting up decryption.");
            System.exit(1);
        }
        
        // *** START READING IN FILE *** //   

        InputStream inputfilestream = null;
        OutputStream outputfilestream = null;
        InputStream templatestream = null;
        try
        {          
            inputfilestream = new FileInputStream(arguments.get("input"));
            outputfilestream = new FileOutputStream(arguments.get("output"));
        }
        catch(IOException e2)
        {
            System.out.println("IO exception occurred when setting up file streams.");
            System.exit(1);
        }

        CipherOutputStream encryptionStream = null;
        ArrayList<Byte> arr = new ArrayList<Byte>();
        try
        {
            if(arguments.get("template") == null)
            {
                 // Output the random bytes until offset.
                int offset = Integer.parseInt(arguments.get("offset"));
                byte [] random_bytes = new byte[offset];
                rand.nextBytes(random_bytes);
                for(int i = 0; i < offset; i ++)
                {
                    outputfilestream.write(random_bytes[i]);
                }
            }
            else
            {
                // read in template file.
                templatestream = new FileInputStream(arguments.get("template"));
                int offset = Integer.parseInt(arguments.get("offset"));
                int readable_data;
                int pointer = 0;
                while (pointer < offset && ((readable_data = templatestream.read()) != -1))
                {
                    outputfilestream.write((byte)readable_data);
                    pointer ++;
                }
            }


            encryptionStream = new CipherOutputStream(outputfilestream, cipher);

            // add hashed key
            for(int i = 0; i < hashedKey.length; i ++)
            {
                if(templatestream != null)
                {
                    templatestream.read();
                }
                encryptionStream.write(hashedKey[i]);
            }

            // add data
            int readable_data;
            while ((readable_data = inputfilestream.read()) != -1)
            {
                arr.add((byte)readable_data);
                encryptionStream.write((byte)readable_data);
                if(templatestream != null)
                {
                    templatestream.read();
                }
            }
            inputfilestream.close();

            // add hashed key again
            for(int i = 0; i < hashedKey.length; i ++)
            {
                if(templatestream != null)
                {
                    templatestream.read();
                }
                encryptionStream.write(hashedKey[i]);
            }

            // add hashed data
            byte [] data = new byte [arr.size()];
            byte [] hashedData;
            for(int i = 0; i < arr.size(); i ++)
            {
                data[i] = arr.get(i); 
            }
            md.update(data);
            hashedData = md.digest();
            for(int i = 0; i < hashedData.length; i ++)
            {
                if(templatestream != null)
                {
                    templatestream.read();
                }
                encryptionStream.write(hashedData[i]);
            }


            if(arguments.get("template") == null)
            {
                 // Output the random bytes until offset.
                int offset = Integer.parseInt(arguments.get("offset"));
                int size = Integer.parseInt(arguments.get("size"));
                byte [] random_bytes = new byte[size - offset - arr.size() - 48];
                rand.nextBytes(random_bytes);
                for(int i = 0; i < random_bytes.length; i ++)
                {
                    encryptionStream.write(random_bytes[i]);
                }
            }
            else
            {
                // read in template file.
                int readable_data2;
                while ((readable_data2 = templatestream.read()) != -1)
                {
                    outputfilestream.write((byte)readable_data2);
                }
            }

            encryptionStream.close();

        }
        catch(IOException e2)
        {
            System.out.println("IO exception occurred with encryption stream.");
            System.exit(1);
        }
    }

}