---
layout: post
title: 'State of the Union: Application Logging'
tags: architecture, brainstorm
status: publish
type: post
published: true
comments: true
---
Over the last week, I\'ve been asked to help investigate some issues with a new 
obfuscation process that our builds will now go through before deployment (we 
can save the rants about obfuscating code for some other time). Per the norm 
for this activity, the test group ran into issues in a section of code and needed 
help investigating. As I dug into the issue, I turned to the log files and found 
that they were absolutely worthless. While we suspected that the obfuscation 
processor renamed or removed a class, we couldn\'t find the telltale signs of 
such an activity - no exceptions at all. 

<!--EndExcerpt-->

After an hour tracing the source code, I found what seems to be a common pattern 
in the code:

{% prism java %}
try
{
    // do something potentially dangerous
}
catch( Exception e )
{
    jobDone( 567, &quot;bad request&quot; )
}
{% endprism %}

What\'s wrong here? The code does prevent an exception from killing the long 
running server process and it does fulfill the component API by returning an 
error code. But what was the root cause of the error? This is a simple oversight 
that I see way too much. This code was written with the assumption that a message 
will be logged before throwing an exception but the code encountering an error 
isn\'t something written by us. It\'s the spring framework and that code doesn\'t 
know anything about our logging configuration. Instead of being able to point at 
the problem and fix it, I needed to add some logging code, spin another build 
and then rerun the test. We eventually saw the error (turns out the bean definitions 
no longer matched the byte code), but this highlighted some very real problems 
with logging in server side application development.

After years of trying to work with applications there seem to be some consistent 
problems that always occur. Here\'s a quick a dirty laundry list that I\'ve seen 
over the years and I\'m sure that I\'ve contributed to this list more that I\'d like 
to admit.

* Never Contains What You Need

	No matter how much we try, it seems that the one critical piece of information 
	about the memory state is never logged out in enough detail. A lot of good 
	developers go 95% of the way, but until we get those crystal balls delivered 
	we\'re never going to know what would be useful when fighting fires.

* Contain Too Much Of What You Don\'t Need

	The knee jerk reaction to first point becomes that we just dump more and more 
	information into the log files since we can\'t figure out what\'s going on. Things 
	like \"Entering method doSomething()\", \"Returning from method doSomething()\" or 
	\"Read 7612 bytes of data from socket connection, haveMore = true\" aren\'t very 
	useful the vast majority of the time. Instead they provide a lot of clutter, 
	which brings up the next point.

* Needle In A Hay Stack

	Once you migrate the code from a single dev desktop machine to a production 
	server handling 100,000 concurrently, the verbosity of the logging system becomes 
	your friememy. It\'s going to be useful but how the heck are you going to notice 
	those really important messages that are keys to solving issues or, worse yet, 
	knowing that you have a user who is having a bad product experience. I\'ve recently 
	read about some people using [Hadoop][hadoop] to process their log messages due to 
	the volume of data that\'s being produced.

* Coarse Grained Controls

	To help cope with the problems of too much data, we assign some coarse grained log 
	levels to each message in the hope that we can suppress some things we want to see 
	on low volume systems but not on high volume production boxes. To me, this doesn\'t 
	make sense. The additional information about state is a debug level message because 
	when I\'m debugging an error I\'d like to see it. So do I log this at error level or 
	at debug level? And if I\'m logging at error level, what about the debug messages 
	that occurred before? Were they generated on the way to getting into an error state 
	or for some other request? Simple levels like debug, info and error aren\'t expressive 
	enough to help.

* Still Prone To Human Mistakes

	Finally, the whole process is still very human driven. Each framework requires a 
	developer to come up with the log message, provide the relevant information and 
	then invoke the logging process when it\'s necessary. If the developer forgets or 
	some additional logging is needed to troubleshoot a problem? You\'re out of luck 
	and you\'ll need to change the code and go through another deployment.

In the last 10 years, the industry has made great strides improving web frameworks, scalable 
data access solutions and large volume data processing but we haven\'t moved very far in terms 
of helping ourselves with better logging. What we need is a logging framework that provides 
a rich declarative structure for defining some more complex logging rules and isn\'t dependent 
on developers to remember to invoke it throughout their code. Stay tuned for some random 
thoughts on what I think that might look like in practice.

[hadoop]: http://hadoop.apache.org/
