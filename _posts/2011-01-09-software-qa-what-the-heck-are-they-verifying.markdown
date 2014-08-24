---
layout: post
title: 'Software QA: What the heck are they verifying?'
tags: qa
status: publish
type: post
published: true
comments: true
---
Ask layperson what a quality assurance tester does and, as you can guess, the 
answer won\'t clarify the situation much - they test the software to make sure 
it has some level of quality. That answer didn\'t really clear up the situation 
any and it all revolves around the fact that we haven\'t defined what quality 
is. It\'s a very interesting question to pose and one that I\'m not sure has a 
straight forward answer.

<!--EndExcerpt-->

If you\'ve been working in software industry for any amount of time, you probably 
have made a change to an existing system and then passed the change on to a tester. 
Depending on your company, these testers probably looked at the change you made 
and verified that it fulfilled the functional requirements, that is, given a set 
of inputs by a user of the software, the software does expected and desirable 
effects. These expected and desirable effects are documented and discussed to 
death by the requirements team with the input of the customers. If this doesn\'t 
describe the norm for your company, you are one of the lucky individuals working 
at a more through forward company.

Now as the developers approach the agile development process where we only design 
the code up front, we can see that there is a possibility for two very different 
systems to emerge. First, we could write only as much code as we need at the time 
but end up with one huge main method that does everything - no methods, no classes, 
no modules. The second system we could end up with would be a thing of pure beauty 
with minimal and proper abstractions everywhere and no line of logic is repeated 
twice. Which system would the QA department sign off on and let us ship to production?

The answer is that either system, based on the limited definition of quality from above, 
could meet the functional requirements and thus become our released software. While this 
might be ok if we never have to touch the system again, that\'s probably not the case. 
We\'re going to need a follow up release with additional features, fixes because some 
requirements weren\'t correct and so forth. This is the state of most systems and it\'s 
after the pain grows so much that we end up with the inevitable project of the re-write. 
We try to start the re-write but often the projects loose momentum and traction and users 
will undoubtedly leave. Perl 6, the next major version of Perl that was intended as a 
re-write of Perl, is a classic example. After years of uncertainty and slow progress,
developers (including myself) left for other languages that had a cleaner future and some 
even kept the Perl 5 fork alive.

This is why when we talk about the quality of our software, we\'re really talking about 
something that\'s fundamentally flawed. Instead we need to expand our definition of quality 
to include not only correctness from an end user\'s perspective but also:

* within our bounds for performance
* easy to adapt to new features
* easy to understand
* etc.

There are a number of good sources out there that talk about the \"-ilities\" of software 
development and each of these represents a quality we think good software should have. While 
I tend to stick with a smaller list I\'ve heard discussed at a Construx course, which handful 
of \"-ilities\" you care about really depends on the purpose of your system. If you\'re building 
a C++ compiler, portability over scalability. Large social sites, like Twitter or Facebook, 
might favor scalability over portability. The first thing you need to think about with your 
product owners is, \"What is the reason we\'re building this software and what are the most 
important -ilities that it should possess for the next couple releases?\"

After you formulate an answer, this question isn\'t going to go away. It needs to be revisited 
every so often. While Twitter favored the extensibility aspect of their system early on, they 
eventually needed to change gears and focus on scalability - the key was holding off until 
they needed to without upsetting their users. Even now they\'re not done as they\'ve recently 
switched one more time and started to look at usability (e.g. site redesign) and security 
(e.g. OAuth for APIs). It\'s a never ending question that should guide your agile project 
efforts on a macro level.
