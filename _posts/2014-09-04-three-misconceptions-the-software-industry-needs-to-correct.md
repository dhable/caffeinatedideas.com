---
layout: post
title: Three Misconceptions The Software Industry Needs To Correct
status: publish
type: post
published: true
comments: true
---
The other morning I was discussing my recent discovery of [#NoEstimates][NoEstimates] with our company\'s agile champion. 
\"The business needs certaintly about their software projects\" is a key themes that he\'s heard repeated as a counter to avoiding 
estimates.

<!--EndExcerpt-->

## There Is No Certinanty In Life

At some point in our lives, we\'re given this misconception that with effort we can have some level of certianty about the future. On a small 
enough scale with very fixed boundries, I suppose that\'s true - it\'s probably pretty certian that I\'ll take another breathe of air. Even
then there is still a small level of uncertianly that I won\'t and the outcome of that action will make for a pretty wild day. In fact, there
is no industry that understand that nothing in life is certian like the medical profession. Ask any doctor to comment about a prognosis and
they will ___never___ discount the very slim possibilities in life.

Believing the life if certian doesn\'t just affect estimates, it also lulls developers into ignoring the edge cases in their code. The cloud
is a chaotic environment where you can never be certian that resources, APIs or data is ever where you left it. Good, mature software is code
that has seen the real world for a long enough period of time that the development team has added enough code around uncertian cases. That does
not mean that the code or the problem domain is certian and free of risk. Instead of expecting that software is perfect and free of uncertainty,
try to think of software as a patient in a hospital - even when low risk is involved, the uncertain can happen.

## Life Is Not Synchronous

Next time you go to a store, take a look around at how business is being conducted. In most stores, you\'ll notice a fair amount of interactions
happening asychrnously. Each person involved does not simply wait for the first task to finish before starting another task. In fact, we very
rarely perform tasks in a synchrnoous way. In [Programming Erlang][erlang-book], Joe Armstrong opens right away acknowlding the world as an
asynchrnous place.

	Let’s forget about computers for a moment; I’m going to look out of my window
	and tell you what I see.
	
	I see a woman taking a dog for a walk. I see a car trying to find a parking
	space. I see a plane flying overhead and a boat sailing by. All these things
	happen in parallel.

The synchnous mindset isn\'t just limited to the code we write. We also need to remember that our processes and people can also waste a lot
of time if they communicate synchronously. While agile favors personal communication over process, calling a meeting forces everyone on the
team into a synchronous mode where we loose velocity. Just like a mutex lock, a meeting is something that should only be called when you\'re
in the critical section of the work flow and you need to make sure everyone is on the same page. The cost to obtaining that mutex is much
higher than you think.

## Nothing Is Ever Done




[NoEstimates]: http://noestimates.org/blog/
[erlang-book]: https://pragprog.com/book/jaerlang2/programming-erlang