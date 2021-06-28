import java.util.Random;

public class MyRandom extends java.util.Random
{
    private int [] S = new int[256];
    private int [] T = new int[256];
    private int [] K;
    private int k_len;

    private int I = 0;
    private int J = 0;
    
    public MyRandom()
    {
        long nano = System.nanoTime();
        long a = 56734547248L; // Primitive Factor of m
        long m = 56734547381L; // Prime
        long seed = ((System.currentTimeMillis() * nano * a) % m); // Random seed based on time.
        while (seed == 0)
        {
            // Make sure we do not have a zero seed.
            seed = ((System.currentTimeMillis() * nano * a) % m);
        }
        extractKey(seed);
        RC4_init();
    }

    public MyRandom(long seed)
    {
        extractKey(seed);
        RC4_init();
    }

    private void extractKey(long seed)
    {
        // Extract bytes from the long seed
        K = new int [Long.BYTES];
        for(int i = 0; i < Long.BYTES; i ++)
        {
            K[i] = (int)(seed & 255);
            seed = seed >>> 8;
        }

        // Set key length
        k_len = K.length;
    }

    private void RC4_init()
    {
        for (int i = 0; i < S.length; i ++)
        {
            S[i] = i;
        }

        int j = 0;

        for (int i = 0; i < S.length; i++)
        {
            j = (j + S[i] + K[i%k_len])%256;
            // swap S[i] and S[j]
            int temp = S[j];
            S[j] = S[i];
            S[i] = temp;
        }
    }

    public void setSeed(long seed)
    {
        S = new int[256];
        T = new int[256];
        extractKey(seed);
        RC4_init();
    }

    public int next(int bits)
    {
        if (bits != 8)
        {
            System.out.println("Method MyRandom.next(int bits) requires that bits is set to 8. You used bits == " + bits);
            System.exit(1);
        }
        I = (I + 1)%256;
        J = (J + S[I])%256;
        // swap S[I] and S[J]
        int temp = S[J];
        S[J] = S[I];
        S[I] = temp;
        return S[(S[I] + S[J])%256];
    }


    public static void main(String args[])
    {
        // For the purposes of testing
        MyRandom rand = new MyRandom();
        for(int i = 0; i < 256; i ++)
        {
            System.out.println(rand.next(8) + " ");
        }
    }
}