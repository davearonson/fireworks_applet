/*

projectile.java -- basic data/behavior of general projectiles
Copyright 1997-2003, David J. Aronson

should eventually refactor air resistance, gravity, etc. out into some
sort of "environment" or "atmosphere" class.

for now, also ignores effect of altitude on resistance and gravity.

also, this is not quite as "basic" as could be; could have a base class
w/o knowledge of color and previous position -- i.e., pure theory.  the
one with such knowledge would also be a logical place to start adding
drawing stuff, like knowledge of maximum y parameter, so as to enable
reversing that, so as to be able to encapsulate the need for that into
that class, rather than having the drawing parts of the application take
care of that inconvenient fact.  also should not have end() and expire()
type stuff quite so soon, but this was developed initially for a
fireworks applet....

*/


import java.awt.*;
import java.util.Vector;


class projectile
{
    protected double  ballCoef;  // ballistic coefficient; multiplies airRes
    protected Color   color;     // what color it shows up as
    protected double  oldX;      // previous x; used for drawing motion
    protected double  oldY;      // previous y; used for drawing motion
    protected double  x;         // current horizontal position
    protected double  xSpeed;    // current horizontal speed in pixels/loop
    protected double  y;         // current vertical position
    protected double  ySpeed;    // current vertical speed in pixels/loop


    protected void init (double iX, double iY, double bc, Color c)
    {
        oldX = x = iX;
        oldY = y = iY;
        ballCoef = bc;
        color = c;
    }


    public projectile (double iX, double iY, double bc, Color c)
    {
        init (iX, iY, bc, c);
    }


    public projectile (double iX, double iY, double iXS, double iYS,
                       double bc, Color c)
    {
        xSpeed = iXS;
        ySpeed = iYS;
        init (iX, iY, bc, c);
    }


    // by default, they do nothing when they "end"
    protected void end() { }


    // by default, projectiles NEVER expire
    protected boolean expire() { return false; }


    // move it one "turn", and return whether it's still "alive"
    public boolean move()
    {
        if (expire())
        {
            end();
            return false;
        }
        // add gravity, at .1 pixel per loop per loop
        // (this should be refactored out to an environment class)
        ySpeed -= .1;

        // air resistance
        // (this should be refactored out to an environment class)
        xSpeed *= 1 - ballCoef;
        ySpeed *= 1 - ballCoef;

        // move
        oldX = x;
        oldY = y;
        x += xSpeed;
        y += ySpeed;
	return true;
    }


    // note that this will appear UPSIDE-DOWN.  Need to find some way to
    // correct.  Eventual intent is to get height of graphics content and
    // subtract....
    public void paint (Graphics g)
    {
        g.setColor (color);
        g.drawLine ((int) oldX, (int) oldY, (int) x, (int) y);
    }

    public double getX() { return x; }

    public double getY() { return y; }

} // projectile


//----------------------------------------------------------------------


// projectiles that have a fuse

class fusedProjectile extends projectile
{
    protected int  fuse;  // how many loops this thing should be going

    public fusedProjectile (double iX, double iY, double bc, Color c, int f)
    {
        super (iX, iY, bc, c);
        fuse = f;
    }


    public fusedProjectile (double iX, double iY, double iXS, double iYS,
                            double bc, Color c, int f)
    {
        super (iX, iY, iXS, iYS, bc, c);
        fuse = f;
    }


    protected boolean expire()
    {
        if (fuse > 0)
	{
	    fuse--;
	}
        return fuse <= 0;
    }

}


//----------------------------------------------------------------------


// fused projectiles that accept a Vector to put fragments into --
// note that this still doesn't define what fragments to put there.
// that's why this is marked abstract.

abstract class exploder extends fusedProjectile
{
    protected Vector  fragments;

    public exploder (double iX, double iY, double bc, Color c, int f, Vector v)
    {
        super (iX, iY, bc, c, f);
        fragments = v;
    }


    public exploder (double iX, double iY, double iXS, double iYS,
                     double bc, Color c, int f, Vector v)
    {
        super (iX, iY, iXS, iYS, bc, c, f);
        fragments = v;
    }
}


// END OF FILE
