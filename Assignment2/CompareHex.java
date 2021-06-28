public class CompareHex
{
    public static void main (String args[])
    {
        if(args.length != 2)
        {
            System.out.println("Usage: 2 arguments required: CompareHex <hex1> <hex2>");
            System.exit(1);
        }
        String one = args[0];
        String two = args[1];
        int length = one.length();
        if(length != two.length())
        {
            System.out.println("Input strings must be the same length.");
            System.exit(1);
        }

        char [] onearr = null;
        char [] twoarr = null;

        try
        {
            onearr = hex2bin(one);
            twoarr = hex2bin(two);
        }
        catch(Exception e)
        {
            System.out.println("Input strings must be in hexadecimal.");
            System.exit(1);
        }
        
        int length2 = onearr.length;
        int count = 0;
        for(int i = 0; i < length2; i ++)
        {
            char one1 = onearr[i];
            char two2 = twoarr[i];
            if (one1 == two2)
            {
                count ++;
            }
        }
        System.out.println(count + " bits are the same.");
    }


    private static char[] hex2bin(String hexAddr)
    {
        char[] arr = new char[hexAddr.length()*4];
        String [] stringy = hexAddr.split("");
        int p = 0;
        for(int j = 0; j < hexAddr.length(); j ++)
        {
            String temp = stringy[j];
            String bin1 = Integer.toBinaryString(Integer.parseInt(temp, 16));
            if(bin1.length() == 4)
            {
                arr[p] = bin1.charAt(0);
                arr[p + 1] = bin1.charAt(1);
                arr[p + 2] = bin1.charAt(2);
                arr[p + 3] = bin1.charAt(3);
            }
            else if(bin1.length() == 3)
            {
                arr[p] = '0';
                arr[p + 1] = bin1.charAt(0);
                arr[p + 2] = bin1.charAt(1);
                arr[p + 3] = bin1.charAt(2);
            }
            else if(bin1.length() == 2)
            {
                arr[p] = '0';
                arr[p + 1] = '0';
                arr[p + 2] = bin1.charAt(0);
                arr[p + 3] = bin1.charAt(1);
            }
            else if(bin1.length() == 1)
            {
                arr[p] = '0';
                arr[p + 1] = '0';
                arr[p + 2] = '0';
                arr[p + 3] = bin1.charAt(0);
            }
            else
            {
                System.exit(1);
            }
            p = p + 4;
        }

        return arr;
    }

}