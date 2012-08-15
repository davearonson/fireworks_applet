/*

dja.java -- assorted utility routines
Copyright 1997-2003, David J. Aronson

*/

import java.applet.Applet;
import java.awt.Color;
import java.util.Random;

// assorted little utility things I might use over
abstract class dja
{

    // private stuff
    private static boolean initted = false;
    private static Random myRandomizer = null;
    private static void init()
    {
        if (! initted)
        {
            myRandomizer = new Random (System.currentTimeMillis());
            initted = true;
        }
    }

    // assorted useful constants
    public static final double HALFPI = Math.PI / 2;
    public static final double TWOPI = Math.PI * 2;


    // make a random BROWSER-SAFE color, with at least minimum total brightness
    public static Color randomBrightColor (int min)
    {
        int colorNum;  // color represented as an integer
        int total;     // total brightness, taking R/G/B equally

        do
        {
            int  i;

            for (colorNum = total = i = 0; i < 3; i++)
            {
                // decide each RGB component -- random # from 0 to 5, times 51
                int tmp = roll (6) - 1;
                colorNum = colorNum * 256 + tmp * 51;
                total += tmp;
            }
        } while (total < min); // keep trying until bright enough
        return new Color (colorNum);
    }


    // make a random BROWSER-SAFE color, with any brightness
    // (yeah, this way is kinda klugey, but will do for now)
    public static Color randomColor() { return randomBrightColor (0); }


    // roll a die with "size" faces
    public static int roll (int size)
    {
        init();
        // note: nextDouble returns 0 <= d < size, so 0 <= ceil < size
        return (int) Math.ceil (myRandomizer.nextDouble() * size) + 1;
    }


    // roll "dice" dice with "size" faces each;
    // note overloading of function name
    public static int roll (int dice, int size)
    {
        int  i;
        int  total = 0;

        for (i = 0; i < dice; i++)
        {
            total += roll (size);
        }
        return total;
    }
    

    // generate a random *double* such that 0 <= result < parameter
    // (and even more overloading!)
    public static double roll (double size)
    {
        init();
        return myRandomizer.nextDouble() * size;
    }


    // generate sum of random *doubles* such that 0 <= each double < parameter
    public static double roll (int dice, double size)
    {
        int     i;
        double  total = 0;

        for (i = 0; i < dice; i++)
        {
            total += roll (size);
        }
        return total;
    }


    // return random floating point number within some range centered on 1.0,
    // biased towards the middle
    // (e.g., .1 returns within a range .1 wide, i.e., 1.0 +/- .05)
    public static double variance (double part)
    { return 1.0 - part/2 + roll (2, part/2); }


    // generate a random double between 0 and max, inclusive,
    // but biased towards the upper range.
    // (note: can actually achieve full "max", unlike roll(double)!)
    public static double upBiasRoll (double max)
    {
        double tmp = 2 * max - roll (2, max);
        if (tmp > max)
        {
            tmp = (2 * max) - tmp;
        }
        return tmp;
    }

} // dja


// END OF FILE
