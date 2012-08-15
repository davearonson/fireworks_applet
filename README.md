This is an applet called fireshow, a simple fireworks display.  I wrote it long
ago, when applets were what everybody was using Java for... and when I didn't
have all that strong a handle on OO and design patterns and so on, so forgive
the newbie-ish code!

The code is in these files:

- fireshow.java: the applet itself
- firework.java: various classes of fireworks
- projectile.java: various basic classes of projectiles
- dja.java: various utility methods and constants

Then of course there's html.html, which is nothing but a holder.

In addition to demonstrating inheritance, this applet also demonstrates
threading.  I initially had each projectile run as its own thread, but that was
very slow.  Now it has one thread occasionally launch a rocket, while another
thread handles all the existing projectiles (erasing, moving, and if not
expired, redrawing), which are stored in a Vector.  Admittedly, this
application does not really *require* threading, and would be simpler without
it, plus it doesn't do proper synchronization, but that's not the point!
