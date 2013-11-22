---
layout: post
title: What Navy SEALs Can Teach Us About Software Development
tags: agile
status: publish
type: post
published: true
comments: true
---
I was having a conversation with a co-worker last week about various bad scrum 
practices that seem to creep into acceptance (often times called scrum-but). We 
started to think about why we've seen scrum teams with these problems and what 
the solution is. That's when I asked, "how come the Navy SEALs don't seem to have 
as many problems even though they face great uncertainty and challenges?"" After 
thinking about it, I came up with three key lessons that we could learn from the 
SEALs.

<!--EndExcerpt-->

First, I had to look at  the size of the teams or squads that seem to exist in 
larger companies. Since most larger companies try to convert their existing silo-ed 
models into scrum teams the typical teams end up with a lot of members in very 
specialized roles. These often times include:

* 6 developers (3 pairs)
* 3 functional testers
* 1 project managers
* 1 performance tester
* 1 tool developer

For a grand total of 12 members who actively participate in the sprints or missions. 
Now compare this to the size of a Navy SEAL team that contains only 4-5 men. Why is 
it that the SEAL team is 1/3 the size of the software development team? Simply put, 
the team of 4-5 people can move quicker and with more precision than larger teams. 
The smaller team doesn't need as much effort to coordinate the communication and 
come to a common understanding. If you graphed out the effort required to build 
consensus vs the number of people involved, you would end up with an exponential 
growth curve instead of a linear graph. If you don't believe me, try organizing a 
group lunch outing and watch how the decision on where to becomes increasingly more 
complex as more people are invited.

Lesson #1: Keep your scrum team sizes between 4-5 members just like the Navy SEALs.

Now looking at the team composition above, you'll see a lot of people that are involved 
in the sprints that are playing some very specific roles, like performance tester. We 
need and will continue to need members with special skills but they aren't needed on 
every individual scrum team. The Navy still relies on surgeons to treat wounded SEALs 
but you can't afford to put a surgeon in every SEAL squad nor would that be the most 
effective use of your resources. Yet, every SEAL member knows basic first aid and some 
squads may have even more knowledge but nowhere near what a surgeon would have.

Similarly, it's time that the developers start becoming more generalists in their 
skills and learn how to test their own code, how to setup environments and how to 
perform some architectural design decisions. The development teams need to have the 
ability and autonomy to exercise these skills when they're in the thick of the sprint. 
This doesn't mean you can fire all the architects. You'll still want a group with 
strong architecture talent but that might be limited to one or two scrum teams instead 
of every scrum team, just like SEAL teams may be known for being snipers or medical 
units. If you find those specialized scrum teams skewed with more work, then you might 
want to consider cross training and developing people on other scrum teams (keeping 
the number the same) or designing the software so not every change needs some specialized 
skill to ship.

Lesson #2: Scrum teams need generalists with a few teams that specialize in a skill so a 
department can cover everything that's needed.

Scrum is also very popular with the managers because they get a well defined level of 
visibility and control into the work that the scrum team is doing. More often than not, 
I've seen this turn into a hindrance for the teams as the product owners start attending 
the stand up meetings or asking for scope changes in the middle of a sprint. These 
changes to missions probably aren't the norm for a SEAL team that's moving into combat. 
Once the mission starts, the commanders don't ask the teams to pause momentarily while 
additional information is considered nor is a SEAL team that's currently deployed asked 
to pause for some planning on the next mission. Instead, the SEAL commanders do a 
great job in making sure the team currently engaged in a mission stays focused and 
is not inundated with too much information. Questions are answered quickly, with 
certainty and the decisions made in the field are reviewed later to grow the team's 
skills as well as identifying new situations that might need more research.

This filtering of information and keeping a team working on the current mission at hand 
is often compromised in response to communication issues. Instead of working on making 
the teams smaller with higher skilled, generalized developers, the focus turns towards 
copying more people on the emails, having more conference calls and setting up more meetings 
where everyone's input is requested. This is not only destructive but is also the same 
as asking a special ops team to discuss next weeks mission for 30 minutes right as they 
reach the outside of the enemy compound. No one would expect the SEALs to do that so why 
do we expect our software development teams to do this?

Additionally, the objectives for the mission can't change. SEAL teams don't change the 
entire mission goal once they've started moving out nor will they pile on additional 
secondary targets because they're already in the neighborhood. Instead, the goals are 
clear and defined prior to deploying the team. In fact, the top commander's only real 
options once the team has been engaged is to abort the mission and regroup. The same 
should be true of software development. We shouldn't make changes that aren't part of 
the sprint goals just because we're working in a module already and we really shouldn't 
change the goals of the sprint in a week because we think of something different. If we 
need to make changes, we should abort the current sprint and re-group.

Lesson #3: Start sprints with clear objectives and keep the team focused on executing 
these objectives without distraction. If a direction change is needed, abort and re-group 
instead of floundering and possibly causing confusion within the team.
