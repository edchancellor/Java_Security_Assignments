import java.io.*;
import java.util.Random;

public class StreamCipher
{
    public static void main(String args[])
    {
        if (args.length != 3)
        { 
            System.out.println("Usage: 3 arguments required: StreamCipher <key> <input> <output>");
            System.exit(1);
        }

        String key = args[0];
        String inputfile = args[1];
        String outputfile = args[2];

        if(inputfile.equals(outputfile))
        {
            System.out.println("Input file name cannot be the same as output file name.");
            System.exit(1);
        }

        long seed = 0L;
        InputStream inputfilestream = null;
        OutputStream outputfilestream = null;

        try
        {
            seed = Long.parseLong(key);
        }
        catch(NumberFormatException e1)
        {
            System.out.println("Number format exception occurred. Make sure the key can be parsed as a long.");
            System.exit(1);
        }


        try
        {          
            inputfilestream = new FileInputStream(inputfile);
        }
        catch(IOException e2)
        {
            System.out.println("IO exception occurred when setting up input file: " + inputfile);
            System.exit(1);
        }

        // USE FOR TASK 1:
        //Random prng = new Random(seed);

        // USE FOR TASK 2:
        //MyRandom prng = new MyRandom(seed);

        // USE FOR TASK 3:
        MyRandom prng = new MyRandom(seed);

        try
        {
            outputfilestream = new FileOutputStream(outputfile);       
            int readable_data;
            while ((readable_data = inputfilestream.read()) != -1)
            {
                // USE FOR TASK 1 and 2:    
                //outputfilestream.write((byte)prng.nextInt(256) ^ readable_data);

                // USE FOR TASK 3:
                outputfilestream.write((byte)prng.next(8) ^ readable_data);
            }
            inputfilestream.close();
            outputfilestream.close();
        }
        catch(IOException e3)
        {
            System.out.println("IO exception occurred when writing to output file: " + outputfile);
            System.exit(1);
        }

    }
}