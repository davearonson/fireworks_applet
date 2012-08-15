/*

firework.java -- fireworks definitions
Copyright 1997-2003, David J. Aronson
Based on stuff in projectile.java

*/


import java.awt.Color;
import java.util.Vector;

// firework classes
// (should eventually be divided into carrier and charge,
// so as to enable "wiggly" lines up, fading, etc.,
// without having to multiply entities unnecessarily).
abstract class firework extends exploder
{

    protected double  maxSubSpeed;  // max speed of fragments
    protected int     numSubs;      // number of fragments (subparts)


    public firework (double myX, double myY, double xs, double ys, Color c,
                     int f, Vector v, double mss)
    {
        // note relatively low ballistic coefficient, key to keeping going
        super (myX, myY, xs, ys, .001, c, f, v);
        maxSubSpeed = mss;
        calcNumSubs();
    }


    // calculate the number of fragments this will explode into.
    // this is one of the main things that will get overridden in subclasses.
    protected void calcNumSubs()
    { numSubs = (int) (maxSubSpeed * 20); }


    // add a single "sparkle" (defined later) to our fragment vector,
    // with default fuse length
    protected void addSparkle (double speed, double angle, Color c)
    { addSparkle (speed, angle, c, 100 + dja.roll (2, 50)); }


    // add a single "sparkle" (defined later) to our fragment vector,
    // with a KNOWN fuse length; call the defaulter if zero, which calls this
    // with default parameters.  (klugey, maybe i'll fix it later.)
    protected void addSparkle (double speed, double angle, Color c, int f)
    {
        if (f == 0)
	{
            addSparkle (speed, angle, c); // let other one handle default
	}
	else
	{
	    if (c == Color.black)
	    {
	        System.out.println ("Added a black sparkle!");
	    }
            fragments.addElement (new sparkle (x, y,
                                               speed * Math.cos (angle),
                                               speed * Math.sin (angle),
                                               c, f));
        }
    }

} // firework


//----------------------------------------------------------------------

// first, the basic shape extensions....

//----------------------------------------------------------------------



// simple ball firework; also usable as "flash", with short fuse
class ballFW extends firework
{

    protected int  subFuse;  // fuse length on subparts, for flash


    // note TWO constructors, one that sets subFuse to 0 (which becomes
    // default), and the other to a specific length....

    public ballFW (double myX, double myY, double xs, double ys, Color c,
                   int f, Vector v, double mss)
    {
        super (myX, myY, xs, ys, c, f, v, mss);
        subFuse = 0; // later gets turned into default -- yeah, klugey....
    }


    public ballFW (double myX, double myY, double xs, double ys, Color c,
                   int f, Vector v, double mss, int sf)
    {
        super (myX, myY, xs, ys, c, f, v, mss);
	subFuse = sf;
    }


    protected void calcNumSubs()
    { numSubs = (int) (maxSubSpeed * maxSubSpeed * 4); }


    protected void end()
    {
        int  i;
        for (i = 0; i < numSubs; i++)
        {
            addSparkle (dja.roll (maxSubSpeed), dja.roll (dja.TWOPI),
                        color, subFuse);
        }
    }

} // ballFW


//----------------------------------------------------------------------


// simple ring firework -- uses 20% variance to achieve ring thickness
class ringFW extends firework
{
    public ringFW (double myX, double myY, double xs, double ys, Color c,
                   int f, Vector v, double mss)
    { super (myX, myY, xs, ys, c, f, v, mss); }


    protected void end()
    {
        int    i;
        for (i = 0; i < numSubs; i++)
        {
            addSparkle (maxSubSpeed * dja.variance (0.2),
                        dja.roll (dja.TWOPI), color);
        }

    }
}


//----------------------------------------------------------------------


// simple star firework -- also laying groundwork for spirals, double
// starts, etc.
class starFW extends firework
{

    int     arms;
    double  offset;

    public starFW (double myX, double myY, double xs, double ys, Color c,
                   int f, Vector v, double mss)
    {
        super (myX, myY, xs, ys, c, f, v, mss);
	calcArms();
	offset = dja.roll (dja.TWOPI);
    }


    public starFW (double myX, double myY, double xs, double ys, Color c,
		   int f, Vector v, double mss, int a, double o)
    {
        super (myX, myY, xs, ys, c, f, v, mss);
	arms = a;
	offset = o;
    }


    protected void calcArms() { arms = dja.roll (3, 3); }


    protected void end()
    {
	int  a;
	int  ns = numSubs / arms;  // fragments PER ARM

        for (a = 0; a < arms; a++)
        {
            int  i;
            for (i = 0; i < ns; i++)
            {
		addSparkle (dja.roll (maxSubSpeed),
			    offset * dja.variance (0.2 / arms), color);
            }
	    offset += (dja.TWOPI / arms);
        }

    }

} // starFW


//----------------------------------------------------------------------
// now some slight extensions....
//----------------------------------------------------------------------


// spiral firework, simply a twisted star
// (may eventually adapt to make arms that don't do full 360 degrees)
class spiralFW extends starFW
{

    boolean  reverse;  // whether to reverse usual direction
    int      layers;   // how many times each arm rotates


    public spiralFW (double myX, double myY, double xs, double ys, Color c,
                     int f, Vector v, double mss)
    {
	super (myX, myY, xs, ys, c, f, v, mss);
	if (arms == 1)
	{
	    layers = 2;
	}
	else if (arms == 2)
	{
	    layers = dja.roll (2);
	}
	else
	{
	    layers = 1;
	}
        reverse = dja.roll (2) == 1;
    }


    public spiralFW (double myX, double myY, double xs, double ys, Color c,
		     int f, Vector v, double mss, int a, double o, int l,
		     boolean r)
    {
	super (myX, myY, xs, ys, c, f, v, mss, a, o);
	layers = l;
        reverse = r;
    }


    protected void calcArms() { arms = dja.roll (2, 3) - 1; }


    protected void calcNumSubs()
    { numSubs = (int) (maxSubSpeed * 30); }


    protected void end()
    {
	int	a;
	double	mss = maxSubSpeed / layers;
        int     ns = numSubs / (arms * layers); // frags per arm per layer

        addSparkle (0, 0, color); // leave one at "home" to emphasize it
        for (a = 0; a < arms; a++)
        {
            int  l;
            for (l = 0; l < layers; l++)
            {
                double  baseSpeed = mss * l;
                int     i;
                for (i = 0; i < ns; i++)
                {
                    double  angle = dja.upBiasRoll (dja.TWOPI);
                    double  speed = baseSpeed + (mss * angle / dja.TWOPI);

		    if (reverse)
		    {
                        angle = dja.TWOPI - angle;
		    }
                    addSparkle (speed * dja.variance (.2), offset + angle,
                                color);
                }
            }
            offset += (dja.TWOPI / arms);
        }

    }
} // spiralFW


//----------------------------------------------------------------------

// now some more interesting shapes....

//----------------------------------------------------------------------


// heart-shaped firework
class heartFW extends firework
{
    public heartFW (double myX, double myY, double xs, double ys, Color c,
                    int f, Vector v, double mss)
    {   super (myX, myY, xs, ys, c, f, v, mss); }


    protected void calcNumSubs()
    { numSubs = (int) (maxSubSpeed * 10); }


    protected void end()
    {
        int     i;
        addSparkle (0, 0, color); // leave one at "home" to emphasize it
	for (i = 0; i < numSubs; i++)
        {
            double  angle;
            double  speed;
            // bias angle to around pi to compensate for stretch.
            // if it's over, use same speed as for horizontal opposite.
            angle = dja.roll (2, Math.PI);
            speed = maxSubSpeed * 2 * ((angle < Math.PI) ? angle : dja.TWOPI - angle) / dja.TWOPI;
            addSparkle (speed * dja.variance (.2), angle + dja.HALFPI, color);
        }
    }
}


//----------------------------------------------------------------------

// and now the ones built up from those....
// GOTCHA ALERT:  Since Java uses GC for memory management,
// the technique of creating new ones and bursting them immediately is OK.
// If translated to C++ or some such, however, we must clean up!

//----------------------------------------------------------------------


// two stars with the same orgin and # of arms, each one having its
// arms exactly between those of the other.
class doubleStarFW extends firework
{
    public doubleStarFW (double myX, double myY, double xs, double ys, Color c,
                  int f, Vector v, double mss)
    { super (myX, myY, xs, ys, c, f, v, mss); }

    protected void end()
    {
	int	a = 2 * dja.roll (2, 3);  // even numbers look better
	double	o = dja.roll (dja.TWOPI);

	new starFW (x, y, 0, 0, dja.randomBrightColor (3), 0, fragments, maxSubSpeed, a, o).end();
        new starFW (x, y, 0, 0, dja.randomBrightColor (3), 0, fragments, maxSubSpeed * 0.75, a, o + Math.PI/a).end();
    }
}


//----------------------------------------------------------------------


// "eye"-like firework -- a ring with a smaller ball in the middle
// (later, may make the ring an oval, maybe even w/ pointy ends....)
class eyeFW extends firework
{
    public eyeFW (double myX, double myY, double xs, double ys, Color c,
                  int f, Vector v, double mss)
    { super (myX, myY, xs, ys, c, f, v, mss); }

    protected void end()
    {
	new ballFW (x, y, 0, 0, dja.randomBrightColor (3), 0, fragments, maxSubSpeed/2).end();
	new ringFW (x, y, 0, 0, dja.randomBrightColor (3), 0, fragments, maxSubSpeed).end();
    }
}


//----------------------------------------------------------------------


// "web"-like firework -- concentric rings latered on a star
// (see concentrics below)
class webFW extends firework
{
    public webFW (double myX, double myY, double xs, double ys, Color c,
                    int f, Vector v, double mss)
    { super (myX, myY, xs, ys, c, f, v, mss); }

    protected void end()
    {
	new starFW (x, y, 0, 0, dja.randomBrightColor (3), 0, fragments, maxSubSpeed).end();
	new concRingFW (x, y, 0, 0, dja.randomBrightColor (3), 0, fragments, maxSubSpeed).end();
    }
}


//----------------------------------------------------------------------


// like webFW, but with a spiral instead of concentric rings
class spWebFW extends firework
{
    public spWebFW (double myX, double myY, double xs, double ys, Color c,
                    int f, Vector v, double mss)
    { super (myX, myY, xs, ys, c, f, v, mss); }

    protected void end()
    {
	new starFW (x, y, 0, 0, dja.randomBrightColor (3), 0, fragments, maxSubSpeed).end();
	new spiralFW (x, y, 0, 0, dja.randomBrightColor (3), 0, fragments, maxSubSpeed).end();
    }
}


//----------------------------------------------------------------------


// like spWebFW, but with a circle around the edge, like a spiral on a wheel
class spWebWheelFW extends firework
{
    public spWebWheelFW (double myX, double myY, double xs, double ys, Color c,
                    int f, Vector v, double mss)
    { super (myX, myY, xs, ys, c, f, v, mss); }

    protected void end()
    {
        // reduce size to reduce CPU load!
	new starFW (x, y, 0, 0, dja.randomBrightColor (3), 0, fragments, maxSubSpeed/2).end();
	new ringFW (x, y, 0, 0, dja.randomBrightColor (3), 0, fragments, maxSubSpeed/2).end();
	new spiralFW (x, y, 0, 0, dja.randomBrightColor (3), 0, fragments, maxSubSpeed/2).end();
    }
}


//----------------------------------------------------------------------


// spiral-spoked wheel
class spWheelFW extends firework
{
    public spWheelFW (double myX, double myY, double xs, double ys, Color c,
                      int f, Vector v, double mss)
    { super (myX, myY, xs, ys, c, f, v, mss); }

    protected void end()
    {
	new spiralFW (x, y, 0, 0, dja.randomBrightColor (3), 0, fragments, maxSubSpeed).end();
	new ringFW (x, y, 0, 0, dja.randomBrightColor (3), 0, fragments, maxSubSpeed).end();
    }
}


//----------------------------------------------------------------------


// wheel-like firework
class wheelFW extends firework
{
    public wheelFW (double myX, double myY, double xs, double ys, Color c,
                    int f, Vector v, double mss)
    { super (myX, myY, xs, ys, c, f, v, mss); }

    protected void end()
    {
	new starFW (x, y, 0, 0, dja.randomBrightColor (3), 0, fragments, maxSubSpeed).end();
	new ringFW (x, y, 0, 0, dja.randomBrightColor (3), 0, fragments, maxSubSpeed).end();
    }
}


//----------------------------------------------------------------------

// and now some derivations that do something interesting: launch more!

//----------------------------------------------------------------------


// how to do multiple of something; hinges on using makeSubFW() in end()
abstract class multiFW extends firework
{
    public multiFW (double myX, double myY, double xs, double ys, Color c,
                      int f, Vector v, double mss)
    { super (myX, myY, xs, ys, c, f, v, mss); }

    protected void end()
    {
	Color  oldColor = Color.black;
	int    i;
	for (i = 0; i < numSubs; i++)
        {
	    Color  c;
	    do { c = dja.randomBrightColor (3); } while (c == oldColor);
	    fragments.addElement (makeSubFW (i, numSubs, c));
	    oldColor = c;
        }
    }

    abstract protected firework makeSubFW (int i, int outOf, Color c);

}


//----------------------------------------------------------------------

// first concentric arrangements of things....

//----------------------------------------------------------------------


// how to do concentric somethings
abstract class concFW extends multiFW
{
    public concFW (double myX, double myY, double xs, double ys, Color c,
                       int f, Vector v, double mss)
    { super (myX, myY, xs, ys, c, f, v, mss); }

    protected void calcNumSubs()
    { numSubs = dja.roll (2, 2); }  // 2 to 4 things, usually 3

}


//---------------------------------------------------------------------


// concentric balls;
// kinda hard to see the effect, actually, so may remove
class concBallFW extends concFW
{
    public concBallFW (double myX, double myY, double xs, double ys, Color c,
                       int f, Vector v, double mss)
    { super (myX, myY, xs, ys, c, f, v, mss); }

    protected void calcNumSubs()
    { numSubs = 2; }

    protected firework makeSubFW (int i, int outOf, Color c)
    {
	return new ballFW (x, y, 0, 0, c, 0, fragments,
                           maxSubSpeed * ((i + 1.0) / outOf));
    }

}


//----------------------------------------------------------------------


// concentric rings
class concRingFW extends concFW
{
    public concRingFW (double myX, double myY, double xs, double ys, Color c,
                       int f, Vector v, double mss)
    { super (myX, myY, xs, ys, c, f, v, mss); }

    protected firework makeSubFW (int i, int outOf, Color c)
    {
	return new ringFW (x, y, 0, 0, c, 0, fragments,
                           maxSubSpeed * ((i + 1.0) / outOf));
    }

}


//----------------------------------------------------------------------


// concentric hearts -- this one was kinda tricky, since the "origin" is
// at the upper "point", rather than the visual center.  had to delay the
// outer ones to make it all *look* "centered".
class concHeartFW extends concFW
{
    public concHeartFW (double myX, double myY, double xs, double ys, Color c,
                       int f, Vector v, double mss)
    { super (myX, myY, xs, ys, c, f, v, mss); }

    protected firework makeSubFW (int i, int outOf, Color c)
    {
        // adjust for off-centered appearance -- can't just pop 'em all here,
	// have to push the bigger ones up (or drop the smaller ones)
	double  mss = maxSubSpeed * ((i + 1.0) / outOf);
	return new heartFW (x, y, 0, maxSubSpeed, c, i, fragments, mss);
    }

}


//----------------------------------------------------------------------

// now scatterings of things....

//----------------------------------------------------------------------


// how to do multiple somethings, *scattered* instead of concentric.
// hinges on use of makeScatterSubFW(), to "throw" it
abstract class scatterFW extends multiFW
{
    public scatterFW (double myX, double myY, double xs, double ys, Color c,
                      int f, Vector v, double mss)
    { super (myX, myY, xs, ys, c, f, v, mss); }

    protected void calcNumSubs()
    { numSubs = dja.roll (4, 2); }  // 4 to 8 things, usually 6

    protected firework makeSubFW (int i, int outOf, Color c)
    {
        double  angle = dja.roll (Math.PI);
	double	speed = dja.roll (2, maxSubSpeed / 4);
	return makeScatterSubFW (i, outOf, angle, speed, c);
    }

    abstract protected firework makeScatterSubFW (int i, int outOf,
                                                  double angle, double speed,
                                                  Color c);

}


//----------------------------------------------------------------------


// scattered balls
class scatterBallFW extends scatterFW
{
    public scatterBallFW (double myX, double myY, double xs, double ys,
                          Color c, int f, Vector v, double mss)
    { super (myX, myY, xs, ys, c, f, v, mss); }

    protected firework makeScatterSubFW (int i, int outOf, double angle,
                                         double speed, Color c)
    {
	return new ballFW
	(
	    x, y, speed * Math.cos (angle), speed * Math.sin (angle),
	    c, 20 + i * 5, fragments, maxSubSpeed / 2 * dja.variance (.5)
	);
    }

}


//----------------------------------------------------------------------


// scattered balls, with short fuses, like a bunch of flashes
class scatterFlashFW extends scatterFW
{
    public scatterFlashFW (double myX, double myY, double xs, double ys,
                           Color c, int f, Vector v, double mss)
    { super (myX, myY, xs, ys, c, f, v, mss); }

    protected firework makeScatterSubFW (int i, int outOf, double angle,
                                         double speed, Color c)
    {
	return new ballFW
	(
	    x, y, speed * Math.cos (angle), speed * Math.sin (angle),
	    c, 20 + i * 5, fragments, maxSubSpeed * dja.variance (.2), 5
        );
    }
}


//----------------------------------------------------------------------


// scattered hearts... awwwwwww....  B-)
class scatterHeartFW extends scatterFW
{
    public scatterHeartFW (double myX, double myY, double xs, double ys,
                           Color c, int f, Vector v, double mss)
    { super (myX, myY, xs, ys, c, f, v, mss); }

    protected firework makeScatterSubFW (int i, int outOf, double angle,
                                         double speed, Color c)
    {
	return new heartFW
	(
	    x, y, speed * Math.cos (angle), speed * Math.sin (angle),
	    c, 20 + i * 5, fragments, maxSubSpeed * dja.variance (.5)
        );
    }

}


//----------------------------------------------------------------------

// and lastly, the stuff that makes it all work together....

//----------------------------------------------------------------------


// the sparklies that all fireworks ultimately become, and that float down.
// note the relatively high ballistic coefficient -- that's the key to
// their behavior as opposed to the various shells.
class sparkle extends fusedProjectile
{
    public sparkle (double myX, double myY, double xs, double ys,
                    Color c, int f)
    { super (myX, myY, xs, ys, .1, c, f); }  // note high bc
}


// a factory (a la Factory pattern) to make a random one
// (should be tweaked to have a more realistic probability curve,
// but this will do fine for now).
class fireworkFactory
{
    public static firework makeFirework (double myX, double myY,
                                         double xs, double ys,
                                         Color c, int f, Vector v, double mss)
    {
        firework  fw = null;
        int       i = dja.roll (18);
        switch (i)
        {
            default:
            case  1:
                fw = new ballFW (myX, myY, xs, ys, c, f, v, mss);
            break;
            case  2:
                fw = new ringFW (myX, myY, xs, ys, c, f, v, mss);
            break;
            case  3:
                fw = new spiralFW (myX, myY, xs, ys, c, f, v, mss);
            break;
            case  4:
                fw = new starFW (myX, myY, xs, ys, c, f, v, mss);
            break;
            case  5:
                fw = new heartFW (myX, myY, xs, ys, c, f, v, mss);
            break;
            case  6:
                fw = new eyeFW (myX, myY, xs, ys, c, f, v, mss);
            break;
            case  7:
                fw = new spWheelFW (myX, myY, xs, ys, c, f, v, mss);
            break;
            case  8:
                fw = new wheelFW (myX, myY, xs, ys, c, f, v, mss);
            break;
            case  9:
                fw = new concHeartFW (myX, myY, xs, ys, c, f, v, mss);
            break;
            case 10:
                fw = new concRingFW (myX, myY, xs, ys, c, f, v, mss);
            break;
            case 11:
                fw = new scatterBallFW (myX, myY, xs, ys, c, f, v, mss);
            break;
            case 12:
                fw = new scatterFlashFW (myX, myY, xs, ys, c, f, v, mss);
            break;
            case 13:
                fw = new scatterHeartFW (myX, myY, xs, ys, c, f, v, mss);
            break;
            case 14:
                fw = new doubleStarFW (myX, myY, xs, ys, c, f, v, mss);
            break;
            case 15:
                fw = new webFW (myX, myY, xs, ys, c, f, v, mss);
            break;
            case 16:
                fw = new spWebFW (myX, myY, xs, ys, c, f, v, mss);
            break;
            case 17:
                fw = new spWebWheelFW (myX, myY, xs, ys, c, f, v, mss);
            break;
            case 18:
                fw = new concBallFW (myX, myY, xs, ys, c, f, v, mss);
            break;
         }         
	return fw;
    }

} // fireworkFactory


// END OF FILE
