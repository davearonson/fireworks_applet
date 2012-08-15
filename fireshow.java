/*

fireshow.java -- fireworks show
Copyright 1997-2003, David J. Aronson
Based on stuff in firework.java

*/


import java.applet.Applet;
import java.awt.*;
import java.util.*;


// fireworks show applet
// see also firework.java, which in turn uses projectile.java and dja.java
public class fireshow extends Applet
{


    // worker threads -- see their classes below
    private launcher  myLauncher;
    private updater   myUpdater;


    // get parameter, or use default if not specified
    // (should be refactored out to general utility class eventually)
    private int getParm (String parmName, int def)
    {
        int  retVal;
        String s = getParameter (parmName);
	if (s == null)
	{
	    retVal = def;
	}
	else
	{
	    retVal = Integer.parseInt (s);
	}
	return retVal;
    }


    // upon startup, get parameters and start threads
    public void start()
    {
        int     delay;  // delay between launches
	int     fps;    // frames per second
        int     maxX;   // applet width
        int     maxY;   // applet height
        Vector  v;      // where to put burst fragments

	delay = getParm ("delay", 5);
	fps = getParm ("framesPerSec", 25);

	maxX = getSize().width;
	maxY = getSize().height;
        setBackground (Color.black);

	v = new Vector (100, 10);  // init capacity 100, add 10 as needed
	myLauncher = new launcher (delay, maxX, maxY, v);
        myUpdater = new updater (maxY, getGraphics(), v, fps);
        myLauncher.start();
        myUpdater.start();
    }


    // when applet is stopped, tell worker threads to stop.
    // (usually stopped anyway, but apparently some browser
    // configs do NOT stop the worker threads, and they
    // continue to mess up the screen!)
    public void stop()
    {
        myLauncher.askToStop();
        myUpdater.askToStop();
    }


} // fireshow


// helper thread classes
// (initially inner classes, but $ in name messed things up under unix)


// threads that can be told to stop by use of a flag.
// the run() method should be mainly a loop that
// exits when keepRunning becomes false.
abstract class StoppableThread extends Thread
{
    boolean keepRunning;

    public StoppableThread() { keepRunning = true; }

    public void askToStop() { keepRunning = false; }

    public void start()
    {
        super.start();
	keepRunning = true;
    }

}


// worker thread to handle launching projectile
// NOTE: maxYS and maxFS calculations assumes 0.1 gravity;
// gravity (and air resistance) should eventually
// be factored out into an environment class.
// see projectile class for more details.
class launcher extends StoppableThread
{
    int      delay;        // approximate average delay between launches
    int      maxFuse;      // maximum fuse length on a launched projectile
    int      maxX;         // max X *location* (i.e., screen width)
    double   maxYS;        // max Y *launch speed*
    Vector   projectiles;  // vector to add launched shells to


    public launcher (int d, int mX, int mY, Vector v)
    {
        delay = d * 1000;   // passed in in secs, but stored in millisecs
        maxX = mX;
        // physics 101: v = sqrt (2dg), to achieve height d.
	// but note that this ignores air resistance.
	// result: usually bursts at about 3/4 height.  perfect!
        maxYS = Math.sqrt (2 * mY * .1);
	maxFuse = (int) (maxYS * 10);
        projectiles = v;
    }


    public void run()
    {
        while (keepRunning)
	{
            int  snooze;

            // if we've already got 100 projectiles, hold off a bit!
	    // that way we don't wind up with TOO too many at once.
            if (projectiles.size() >= 100)
	    {
	        snooze = (int) dja.roll (delay / 2);
	    }
            else
	    {
	        double mss = 5 + dja.roll (5, 1.0); // maximum subpart speed
		if (projectiles.size() == 0)
		{
                    System.gc(); // gc while we can do so w/o visible effect!
		}
                projectiles.addElement
                (
                    fireworkFactory.makeFirework
                    (
                        maxX / 4.0 + dja.roll (2, maxX/4), 0.0,  // x, y
                        maxYS/10 - dja.roll (2, maxYS/10),       // xs
                        maxYS * .8 * dja.variance (.2),          // ys
                        dja.randomBrightColor (3),               // c
                        maxFuse/2 + dja.roll (2, maxFuse/4),     // f
                        projectiles,                             // v
                        mss
                    )
                );
                snooze = (int) dja.roll (2, delay);
            }

            try { Thread.sleep (snooze); }
            catch (InterruptedException e) { break; }
        }
    }
} // launcher



// worker thread to handle
// moving existing projectiles, removing dead ones, and updating screen
class updater extends StoppableThread
{

    Graphics  graphics;     // graphics context
    int       msPerFrame;   // milliseconds between frames
    int       maxY;         // screen height
    Vector    projectiles;  // all the projectiles we are handling


    public updater (int mY, Graphics g, Vector v, int f)
    {
	graphics = g;
	maxY = mY;
        msPerFrame = 1000 / f;  // passed as frames/sec; need other way round
        projectiles = v;
    }


    public void run()
    {
        while (keepRunning)
	{
            long  timer = System.currentTimeMillis();
            MoveProjs();

	    // see how much time we can sleep for; if any, do so
            timer = msPerFrame - (System.currentTimeMillis() - timer);
            if (timer > 0)
            {
                try { Thread.sleep (timer); }
                catch (InterruptedException e) { break; }
            }
        }
    }


    // move all the projectiles, both in memory and on screen
    // (note: currently assumes black background!)
    protected void MoveProjs()
    {
        int  i;
      
        for (i = 0; i < projectiles.size(); i++)
        {
            projectile  p = (projectile) projectiles.elementAt (i);
            Color       oldColor = p.color;

	    // erase, move, and if still "alive", paint, else forget
            p.color = Color.black;
            PaintProj (p);
            p.color = oldColor;  // do this here so fragments inherit color!
            if (p.move())
            {
                PaintProj (p);
            }
       	    else
	    {
	        projectiles.removeElementAt (i--);
	    }
        }
    }


    // draw line from previous to current position, as sort of streak or blur.
    // should use p.draw(), but need to reverse y dimension!
    protected void PaintProj (projectile p)
    {
        graphics.setColor (p.color);
        graphics.drawLine ((int) p.oldX, maxY - (int) p.oldY,
	                   (int) p.x, maxY - (int) p.y);
    }

} // updater


// END OF FILE
