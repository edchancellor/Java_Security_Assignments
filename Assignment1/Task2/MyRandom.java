import java.util.Random;

public class MyRandom extends java.util.Random
{
    private long a;
    private long b;
    public long x0;
    private long m;
    
    
    public MyRandom()
    {
        long nano = System.nanoTime();
        a = 56734547248L; // Primitive Factor of m
        m = 56734547381L; // Prime
        b = 0L;
        long seed = ((System.currentTimeMillis() * nano * a) % m);
        while (seed == 0)
        {
            // Make sure we do not have a zero seed.
            seed = ((System.currentTimeMillis() * nano * a) % m);
        }
        x0 = seed;
    }

    public MyRandom(long seed)
    {
        if(seed == 0)
        {
            System.out.println("Invalid seed: cannot == zero.");
            System.exit(1);
        }
        a = 56734547248L;
        m = 56734547381L;
        b = 0L;
        x0 = seed;
    }

    public int next(int bits)
    {
        // bits must be between 1 and 32 (inclusive)
        // Note that even if x0 = m - 1, the value of a*x0 + b will still be less than the maximum long value.
        long res = ((a*x0) + b) % m;
        x0 = res;
        return (int) ((res & ((1L << 32) - 1)) >>> (32 - bits)); // For example, if only 1 bit is required, then our result will shift 31 bits.
    }

    public void setSeed(long seed)
    {
        if(seed == 0)
        {
            System.out.println("Invalid seed: cannot == zero.");
            System.exit(1);
        }
        x0 = seed;
    }


    public static void main(String args[])
    {
        // For the purpose of testing
        MyRandom ran = new MyRandom();
        for (int i = 0; i < 10000; i++)
        {
            System.out.println(ran.nextInt(10000) + " ");
        }
    }
}