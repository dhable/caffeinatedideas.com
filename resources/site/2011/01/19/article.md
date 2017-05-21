In my last entry, I mentioned that we need to look at more than correct execution
to determine if the software our dev teams produce is actually high quality
software and this requires us to determine what traits we want the code to have.
When asked, most people will probably include something about scalability, whereby
they want the most efficient memory footprint or to maximize the concurrent number
of jobs in process at any given time. This is a noble goal to have but let's be
real - most of the software we write won't suffer from the scale issues of eBay,
Google or Facebook. Even in that list, scalability as we've defined it wasn't an
issue at first so it's probably not a good quality to include up front.

If technical scalability isn't the most important trait, then what is? I believe
that there are three major axes of quality in a large majority of code bases out
there:

1. Extendability

	This is the desirable quality of the design of the system, components and code
	such that they can be extended to handle new requirements in the future. There
	are numerous companies that eventually met a decline in their given markets because
	they weren't able to iterate the feature set to keep up with new demands from new
	players, usually with new and clean designs. Good separation of code boundaries,
	well designed APIs, constant refactoring to clean up previous mistakes and reduction
	or cyclomatic complexity are key here. This is so important that the agile camp
	has even come up with a new name for discussing what happens when software isn't
	extendable - technical debt.

2. Maintainability

	Unlike extendability, this isn't focused on the code base but more focused on the
	efforts required by an operations group or a system engineering group to deploy and
	keep the software presented by the developers running. This entails making sure that
	configuration options are documented and communicated, that monitoring points are not
	slapped on afterwards but thought through and included up front and that the system
	helps the operations people avoid costly configuration or management mistakes. The new
	world of devops is starting to address these issues by making sure that we take the
	best practices developed to address extendability and bring them into the operations
	side.

3. Reliability

	Cloud computing and mobile access are hot these days are truly represent how most
	software will be delivered and consumed in the future. This environment isn't without
	issues so systems need to be ready to handle extra latency on the connections, auto
	discovery of services, automatic recovery from failures and ensuring that failures of
	individual nodes won't cause cascading issues and bring the whole system down.


<div class="alert alert-info">
__Disclaimer__: Security was in a close race for the #3 spot on my list but there are
plenty of products that are wildly successful even though they suffer from numerous
security exploits. I don't encourage it, but with extendability and maintainability
you should be able to add new security features in.
</div>

If we can add on to our software, it's easy to deploy new versions and the
existing software can handle some uncertainty with execution then it's easy to
address other concerns as they become important to our organization and our
customers. Simply put, we'll only worry about traits if they'll be of value. This
becomes a hard pill to swallow, especially for the engineering mind, since we tend
to believe that we need to be prepared for everything in the future. This was my
mindset not too long ago when I was the sole developer working on a simple web
based ticketing system. I ended up creating this overly complex method for dynamically
re-branding the application in case we wanted to have multiple variations running.
I missed the larger picture that this was an internal tool only and that branding
isn't what mattered. Instead, I should have focused on making it easier to iterate
over user features and worked with the internal customer to deliver value. It was
simply that the dynamic branding was more fun from a technical side.

How do we measure these traits? As I hinted to above, the industry has put in a lot of
effort on measuring the extendability aspects of software and there are a lot of excellent
tools. My favorite at the moment would be [Sonar][sonar]. It provides an excellent
dashboard of metrics collected from other tools via it's plugin mechanism and even produces
trend reports over periods of time. Using the open source plugins for tools like PMD,
FindBugs, unit test tracking and the code coverage reports, you should be able to spot
issues where the code base needs some TLC. If you have some additional resources, the
[SonarJ][sonarj] tool set is a very convenient way to spot the beginnings of spaghetti
code before it becomes a huge cleanup project.

Measuring maintainability and reliability isn't as mature of a field. I've worked with
a number of configuration management frameworks and they all seem to be too complex for
their own good. The recent movement in the field of devops with systems like [Puppet][puppet]
and [Chef][chef] are making this easier but it's still not as simple as it could be. Hopefully
time will help simplify these offerings and also bring additional tools that help with the
deployment of complex systems. There seem to be even less tools that concentrate on this and
it's a tough problem since right now it's highly dependent on the application that's been
built and and how it's deployed. Yet, figuring out how to apply automated tests and metrics
around maintainability and reliability will help move software development further into
engineering and away from art.

[sonar]: http://www.sonarsource.org/
[sonarj]: http://www.hello2morrow.com/products/sonarj
[puppet]: http://www.puppetlabs.com
[chef]: http://wiki.opscode.com/display/chef/Home
