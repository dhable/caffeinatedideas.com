---
layout: post
title: How Certain Are You?
tags: agile process
status: publish
type: post
published: true
comments: true
---
At some point in our lives, we\'re given this misconception that with effort and training we can have 
some level of certainty about the future.  On a small enough scale with very fixed boundaries, I suppose 
that\'s true - it\'s probably pretty certain that I\'ll take another breathe of air.  Even then there is 
still a small level of uncertainty that I won\'t and the outcome of that action will make for a pretty 
wild day. The harsh reality is that nothing in life is certain.

Developers have started to acknowledge that our modern app-centric solutions are really distributed systems 
that suffer from all of the uncertainty expressed so nicely in the 
[fallacies of distributed systems][dist-sys-fallacies]. We have come to understand and accept the the fact 
that our spiffy Ember.js front end might fail when it calls our Flask API server that currently experienced 
a 25 second network glitch trying to log into the MySQL database hosted in AWS. The business expects that 
when we launch our app that the developers have thought enough about these issues. Organizations like Netflix 
have gone as far as building Chaos Monkey to constantly introduce the small edge cases.

Just like there\'s a lack of certainty in a distributed system, we also need to acknowledge that there\'s a 
lack of certainty in project management. While we may try techniques to manage the uncertainty, we need to 
accept the fallacies of project management:

1. Every task contains a complete understanding.
2. Meetings have zero impact to productivity.
3. The project can support an infinite number of employees.
4. The requirements never change.
5. There is one stakeholder.
6. The bug rate with TDD will be zero.
7. Every team is homogeneous.

This list shouldn\'t come as a surprise to anyone who\'s recently participated on a scrum team. Unfortunately, 
may scrum practitioners have worked to establish a process that sets one or more of these fallacies in stone. 
The effects of ignoring the fallacies is most often seen in the simple act of estimating. Instead of trying to 
acknowledge all of the uncertainty in a project, we end up subjecting teams to games of 
[planning poker][planning-poker] or apply [mathematically operations][velocity] to numbers until we get an 
answer that we like better. Why not simply accept the uncertainty and [avoid spending time estimating][NoEstimates] 
something we simply do not understand.

Lack of estimates and embracing the uncertainty is likely to leave many project managers feeling a bit uncomfortable. 
One of the key bullet points in their job description is to help the business predict when it\'s going to ship 
product. Doesn\'t that require estimates? It does only if you also believe that you need to set a date for calling 
something done. Instead, try shipping code continuously to production and stop worrying about trying to estimate 
the uncertain.

Viva [#NoEstimates][NoEstimates-Hashtag]!

[NoEstimates]: http://noestimates.org/blog/
[NoEstimates-Hashtag]: https://twitter.com/search?q=%23NoEstimates
[dist-sys-fallacies]: http://en.wikipedia.org/wiki/Fallacies_of_distributed_computing
[planning-poker]: http://www.planningpoker.com/
[velocity]: http://en.wikipedia.org/wiki/Velocity_(software_development)

