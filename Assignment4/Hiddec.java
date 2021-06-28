import java.io.*;
import static javax.crypto.Cipher.*;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import javax.crypto.SecretKey;
import javax.crypto.CipherInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import javax.crypto.spec.IvParameterSpec;

public class Hiddec
{
    private static byte [] data;

    public static void main(String args[])
    {
        // *** CHECK ARGUMENTS *** //

        HashMap<String,String> arguments = new HashMap<String,String>();
        String encryptionAlgorithm = "";

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

        if(args.length == 3)
        {
            encryptionAlgorithm = "AES/ECB/NoPadding";
            if (arguments.get("key") == null || arguments.get("input") == null || arguments.get("output") == null)
            {
                System.out.println("Incorrect arguments provided. Must include \"key\", \"input\", \"output\" and optionally \"ctr\"");
                System.exit(1);
            }
        }
        else if(args.length == 4)
        {
            encryptionAlgorithm = "AES/CTR/NoPadding";
            if (arguments.get("key") == null || arguments.get("input") == null || arguments.get("output") == null || arguments.get("ctr") == null)
            {
                System.out.println("Incorrect arguments provided. Must include \"key\", \"input\", \"output\" and optionally \"ctr\"");
                System.exit(1);
            }
        }
        else
        {
            System.out.println("Incorrect number of arguments provided to program. Should be either 3 or 4.");
            System.exit(1);
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
                cipher.init(DECRYPT_MODE,sk,IV);
            }
            else
            {
                cipher.init(DECRYPT_MODE,sk);
            }
        }
        catch(Exception e)
        {
            System.out.println("Error setting up decryption.");
            System.exit(1);
        }
        

        // *** FIND OFFSET (IF APPLICABLE) *** //    

        InputStream inputfilestream = null;
        CipherInputStream decryptionStream = null;
        int offset = 0;
        ArrayList<Byte> arr = new ArrayList<Byte>();

        if(arguments.get("ctr") != null)
        {
            try
            {          
                inputfilestream = new FileInputStream(arguments.get("input"));
            }
            catch(IOException e2)
            {
                System.out.println("IO exception occurred when setting up data file: " + arguments.get("input"));
                System.exit(1);
            }
        
            try
            {    
                int readable_data;
                while ((readable_data = inputfilestream.read()) != -1)
                {              
                    arr.add((byte)readable_data);
                }
                inputfilestream.close();
            }
            catch(IOException e3)
            {
                System.out.println("IO exception occurred when reading from input file: " + arguments.get("input"));
                System.exit(1);
            }

            offset = findOffset(arr, hashedKey, sk, IV);
            if(offset == -1)
            {
                System.out.println("No hashed key found in input file.");
                System.exit(1);
            }
        }
        
        // *** DECRYPT DATA FILE *** //   

        try
        {          
            // Skip past offset in inputfile.
            inputfilestream = new FileInputStream(arguments.get("input"));
            int readable_data;
            int pointer = 0;
            while (pointer < offset && (readable_data = inputfilestream.read()) != -1)
            {              
                pointer ++;
            }
            decryptionStream = new CipherInputStream(inputfilestream, cipher);
        }
        catch(IOException e2)
        {
            System.out.println("IO exception occurred when setting up data file: " + arguments.get("input"));
            System.exit(1);
        }

        arr = new ArrayList<Byte>();

        try
        {    
            int readable_data;
            while ((readable_data = decryptionStream.read()) != -1)
            {              
                arr.add((byte)readable_data);
            }
            decryptionStream.close();
        }
        catch(IOException e3)
        {
            System.out.println("IO exception occurred when reading from input file: " + arguments.get("input"));
            System.exit(1);
        }

        // *** SEARCH DECRYPTED DATA FILE *** //  

        int index1 = 0;
        int index2 = 0;
        boolean foundFirst = false;
        try
        {
            for(int i = 0; i < arr.size(); i ++)
            {
                if((arr.get(i) == hashedKey[0] && arr.get(i + 1) == hashedKey[1] && arr.get(i + 2) == hashedKey[2] 
                && arr.get(i + 3) == hashedKey[3] && arr.get(i + 4) == hashedKey[4] && arr.get(i + 5) == hashedKey[5]
                && arr.get(i + 6) == hashedKey[6] && arr.get(i + 7) == hashedKey[7] && arr.get(i + 8) == hashedKey[8]
                && arr.get(i + 9) == hashedKey[9] && arr.get(i + 10) == hashedKey[10] && arr.get(i + 11) == hashedKey[11]
                && arr.get(i + 12) == hashedKey[12] && arr.get(i + 13) == hashedKey[13] && arr.get(i + 14) == hashedKey[14]
                && arr.get(i + 15) == hashedKey[15]) == true)
                {
                    if(foundFirst == false)
                    {
                        foundFirst = true;
                        index1 = i + 16; // The first index of the data.
                    }
                    else
                    {
                        index2 = i - 1; // The last index of the data.
                        if(verifyHash(index1, index2, arr, md, keyBytes.length) == true)
                        {
                            // *** SETUP OUTPUT FILE *** // 
                            FileOutputStream outputfilestream = null;
                            try
                            {          
                                outputfilestream = new FileOutputStream(arguments.get("output"));
                            }
                            catch(IOException e2)
                            {
                                System.out.println("IO exception occurred when setting up output file: " + arguments.get("output"));
                                System.exit(1);
                            }
        
                            try
                            {    
                                for(int k = index1; k <= index2; k ++)
                                {
                                    outputfilestream.write((char)(arr.get(k) & 0xFF));
                                }
                                outputfilestream.close();
                            }
                            catch(IOException e3)
                            {
                                System.out.println("IO exception occurred when writing to output file: " + arguments.get("output"));
                                System.exit(1);
                            }
                            System.exit(0); // terminate program.
                        }
                        else
                        {
                            System.out.println("Data and trailing hashed data do not match.");
                            System.exit(1);
                        }
                    }

                }
            }

            // Things did not go as expected. Define problem:
            if(foundFirst == false)
            {
                System.out.println("No hashed key found in input file.");
                System.exit(1);
            }
            else if(foundFirst == true)
            {
                System.out.println("First hashed key found in input file, but no terminating hashed key.");
                System.exit(1);
            }
        }
        catch(IndexOutOfBoundsException e)
        {
            // Reached end of data file.
            if(foundFirst == false)
            {
                System.out.println("No hashed key found in input file.");
                System.exit(1);
            }
            else if(foundFirst == true)
            {
                System.out.println("First hashed key found in input file, but no terminating hashed key.");
                System.exit(1);
            }
        }
    }


    private static boolean verifyHash(int i1, int i2, ArrayList<Byte> a, MessageDigest m, int keyLength)
    {
        // *** HASH DATA FILE *** //  
        
        int length = i2 - i1 + 1;
        data = new byte[length];
        int p = 0;
        for(int i = i1; i <= i2; i ++)
        {
            data[p] = a.get(i); 
            p ++;
        }
        m.update(data);
        byte[] hashedData = m.digest();

        try
        {
            p = 0;
            for(int j = i2 + keyLength; j < hashedData.length; j ++)
            {
                if(a.get(j) == hashedData[p])
                {
                    p ++;
                }
                else
                {
                    return false;
                }
            }
        }
        catch(IndexOutOfBoundsException e)
        {
            System.out.println("Trailing hashed data exceeds end of input file.");
            System.exit(1);
        }

        return true;
    }


    private static int findOffset(ArrayList<Byte> arr, byte[] hashedKey, SecretKey sk, IvParameterSpec IV)
    {
        int index = -1;
        try
        {
            byte[] tempBlock = new byte[16];

            for(int i = 0; i < arr.size(); i ++)
            {
                Cipher tempCipher = Cipher.getInstance("AES/CTR/NoPadding");
                tempCipher.init(DECRYPT_MODE,sk,IV);
                int q = 0;
                for(int j = i; j < i + 16; j ++)
                {
                    tempBlock[q] = arr.get(j);
                    q++;
                }
                byte[] tempDecrypt = tempCipher.doFinal(tempBlock);

                if((tempDecrypt[0] == hashedKey[0] && tempDecrypt[1] == hashedKey[1] && tempDecrypt[2] == hashedKey[2] 
                && tempDecrypt[3] == hashedKey[3] && tempDecrypt[4] == hashedKey[4] && tempDecrypt[5] == hashedKey[5]
                && tempDecrypt[6] == hashedKey[6] && tempDecrypt[7] == hashedKey[7] && tempDecrypt[8] == hashedKey[8]
                && tempDecrypt[9] == hashedKey[9] && tempDecrypt[10] == hashedKey[10] && tempDecrypt[11] == hashedKey[11]
                && tempDecrypt[12] == hashedKey[12] && tempDecrypt[13] == hashedKey[13] && tempDecrypt[14] == hashedKey[14]
                && tempDecrypt[15] == hashedKey[15]) == true)
                {
                    index = i;
                    break;
                }
            }
        }
        catch(IndexOutOfBoundsException e)
        {
            return index;
        }
        catch(Exception e)
        {
            System.out.println("Error setting up decryption.");
            System.exit(1);
        }
        return index;
    }

}