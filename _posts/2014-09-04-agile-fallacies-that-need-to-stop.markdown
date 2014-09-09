---
layout: post
title: Software Engineering Fallacies That Need To Stop
status: publish
type: post
published: true
comments: true
---
The other morning I was discussing my recent discovery of [#NoEstimates][NoEstimates] with our company\'s agile champion. 
\"The business needs certaintly about their software projects\" is a key themes that he\'s heard repeated as a counter to avoiding 
estimates. We ended up coming up with a list of our favorite software engineering fallacies that we seem to just keep propogating.

<!--EndExcerpt-->

## Life Is Uncertain 

At some point in our lives, we\'re given this misconception that with effort we can have some level of certianty about the future. On a small 
enough scale with very fixed boundries, I suppose that\'s true - it\'s probably pretty certian that I\'ll take another breathe of air. Even
then there is still a small level of uncertianly that I won\'t and the outcome of that action will make for a pretty wild day. The harsh reality
is that nothing in life is certian.

There's no profession that truely understands the uncertaninty of life like the medical profession. Ask any doctor to comment about a prognosis 
and they will __never__ speak in definiative terms. 

Believing the life if certian doesn\'t just affect estimates, it also lulls developers into ignoring the edge cases in their code. The cloud
is a chaotic environment where you can never be certian that resources, APIs or services are available. Mature software is code that has been used
in the real world long enough that the development team has accounted for enough uncertain cases. It does not mean that a chunk of code has no
more risk of breaking. There is no way to know whether we\'ve covered all the corner cases. The sooner we embrase the chaos instead of punishing
teams for their failures to forsee the uncertainty, the sooner we can get back to deliverying real value.


## Life Is Not Synchronous

Next time you go to a store, take a look around at how business is being conducted. In most stores, you\'ll notice a fair amount of interactions
happening asychrnously. Each person involved does not simply wait for the first task to finish before starting another task. In fact, we very
rarely perform tasks in a synchrnoous way. In [Programming Erlang][erlang-book], Joe Armstrong opens right away acknowlding the world as an
asynchrnous place.

	Let\'s forget about computers for a moment; I\'m going to look out of my window
	and tell you what I see.
	
	I see a woman taking a dog for a walk. I see a car trying to find a parking
	space. I see a plane flying overhead and a boat sailing by. All these things
	happen in parallel.

The synchronous mindset isn\'t just limited to the code we write. We also need to remember that our processes and people can also waste a lot
of time if they communicate synchronously. While agile favors personal communication over process, calling a meeting forces everyone on the
team into a synchronous mode where we loose velocity. Just like a mutex lock, a meeting is something that should only be called when you\'re
in the critical section of the work flow and you need to make sure everyone is on the same page. The cost to obtaining that mutex is much
higher than you think.


## Nothing Is Ever Done

Once a city builds a road, tunnel or bridge, we often say that the project is done. Is it? Let's assume that we account for the periodic 
maintenance and operating costs in the original estimate, we're still not done. In the decades that follow, we need to be ready and willing
to revisit our original design of the structure to ensure that it meets current safety standards. We\'ll also need to account for changes over
time in how those roads get used. We need to be ready to rebuild small sections if we can not longer find suitable replacement components 
with modern components that we can actually purchase. We all realize that once a road or bridge is constructed, we'll be paying for it 
for the next 50 to 60 years. This is why many communities have become more selective in how much infrastructure they build.

Software is no different. Granted, it won't wear out as we make more copies or have it handle more users. Instead by building or using a piece
of software, we've committed to the long term expenses of keeping that software running - porting it to new machines, keeping current on
dependent libraries and ensuring that the tools can be obtained. In the article 
[\"Quality Software Costs Money – Heartbleed Was Free\"][acm-article] (Communications of the ACM, Vol. 57 No. 8, Pages 49-51) Poul-Henning 
Kamp wrote:

	Earlier this year the OpenSSL Heartbleed bug laid waste to Internet security, and there are still hundreds of thousands of embedded
	devices of all kinds—probably your television among them—that have not and will not ever be software-upgraded to fix it. The best way 
	to prevent that from happening again is to avoid having bugs of that kind go undiscovered for several years, and the only way to avoid 
	that is to have competent people paying attention to the software.

This doesn\'t just apply to open source software, but to commercial and in-house software as well. What happens when we finally discover the 
bug years later but there\'s no one around to maintain the code? It happens more than you think. In 1999,
a life insurance company I worked for did need to maintain an old system responsible for a class of product that hadn\'t been sold in years. The
problem was that all the data and the programs were specific to [PL/1][pl1]. Unfortunatly, no one in the company knew [PL/1][pl1]. Instead the
company was forced to hire from a small number of contractors to keep the system running. Had the company decided to build in maintenance costs
earlier, they wouldn\'t be in a situation where they needed to depend on a small pool of talent.

We need to have competent people who are given time to keep the software created up-to-date and free of defects. This means spending time to 
peroidicly update the queueing infrastructure to new versions or upgrading the build scripts. This is more than just refactoring code smells; 
it's ensuring that the code base remains relevant. Thinking that once software ships that it's done is a dangerous mindset that leads to 
increasingly difficult to fix code.


## What Can You Do?

As a practicioner of the craft of software developement, it's upon all of us to tell the people around us that these are myths. Move your
company towards [#NoEstimates][NoEstimates] . Build your code like everything will go wrong and to avoid blocking. Peel of time to keep your 
tools and dependencies up-to-date. Most importantly, stay connected with others in your community and keep discussing better ways to build 
and support software.


[NoEstimates]: http://noestimates.org/blog/
[erlang-book]: https://pragprog.com/book/jaerlang2/programming-erlang
[acm-article]: http://cacm.acm.org/magazines/2014/8/177007-quality-software-costs-money-heartbleed-was-free/fulltext
[pl1]: https://en.wikipedia.org/wiki/PL/I