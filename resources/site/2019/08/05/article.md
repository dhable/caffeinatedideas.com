This weekend I had to replace the top deck boards on the steps leading to
our front porch. The work seemed easy enough - buy some wood, rough cut to
length, unscrew the old boards and screw in the new boards. It's only eight
steps so it shouldn't take more than a day if I'm slow. After spending a
whole day on the project, I only managed to replace three out of the eight
steps. 

So what went wrong? The first snag was when I went looking for the saw. It
seems to have gone missing from the garage and probably occurred a few years
ago when someone broke into the garage door. So an hour and hundred dollars
later, I had my saw and was on my way to cutting the boards. When I took my
first board to the front step, it wasn't wide enough. Turns out my ability 
to tell a 2x10 from a 2x12 wasn't up to par. After another hour and trip to
the hardware store, things were looking positive until the stop on my tape
measure broke off and now I couldn't measure. The final straw came when the
battery on my power driver gave out and I didn't have the spare charged up.

As I was cleaning up from the day, I started to think about why the day went
so bad. I understood the scope of work, what steps were required and basically
how to perform the work. Where I ran into trouble was with my tools - some 
were missing; some not ready for the task at hand. I clearly didn't have 
a backup when I broke a tool. Had I used the tools more frequently, I would
have known and addressed the issues over time. I would have been more
comfortable with them and they would have been ready for me. 

The same thing has happened to me before in code bases despite being much more
proficient with software than power tools. If a code base is considered "complete"
then we tend to not attempt to use the the build scripts, IDEs, tests or even
setup a local environment. When we eventually do, we tend to find that either
versions of the tools are no longer available, the configuration on the CI/CD 
server isn't the same or that libraries are just not available anymore. Simple
changes turn into huge time sinks. If you support an old branch, version of
code base, you need to pull it out every now and then and make sure you can
still work with it. That means building in time to upgrade libraries and 
platforms.  

In both the cases, carpentry and software, all of the tooling problems would
have still existed but frequently using those tools means that you don't need
to worry about a huge tooling upgrade effort when you want to make a simple
change. You'll be happier making changes if you keep the tools fresh.
 