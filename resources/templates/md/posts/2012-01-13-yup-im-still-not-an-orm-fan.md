{:title "Yup, I'm Still Not An ORM Fan"
 :layout :post
 :tags ["database" "rants"]}

Object-Relational Mappers (ORM) frameworks have taken off in the agile development
community in the last decade under the prose that relational databases are too
complex for developers to deal with and we should instead deal with objects in our
code. After working with a few different ORM frameworks, I'm still not sold. All the
perceived productivity gains are quickly a wash once the team starts to encounter the
common ORM problems. I see three large problems cropping up with code bases that
incorporate an ORM framework:

* lack of ability to tune for performance or specific usage
* lack of a standardized grammar or API
* difficulties integrating with existing schemas

I've tried hard to like a variety of ORM tools over the last 7 years. It started when
I introduced Hibernate into a Java web application to replace a home-grown DAO setup.
I didn't like the idea of maintaining not only the objects in Java, but also making
sure that those matched the XML configuration file. Add to this the fact I had to learn
a whole new query language to get items and it took what was a fairly simple facade
around JDBC and increased the expertise required to support this piece of code. I then
revisited the ORM concept when I was working on a Rails application. In this case, we
we're given a partial DB schema to work with. We spent more time trying to fit the tool
into the requirement than we gained. Now, I'm finishing up a project where we leveraged
SQLAlchemy and trying to tune the ORM library to match our specifications has become
more difficult. Again, I see the team spending more time trying to fit the tool into
the requirement than any savings we gained by using the tool.

All three of the problems I've mentioned before don't stem from the use of any particular
tool or implementation. They are instead symptoms of adding too many layers of abstraction
into a problem space. Every ORM tool is trying to build for a very generic set of situations
and often but that's not what every application needs. For instance, SQLAlchemy maintains
references to all the objects loaded through a Session for when you happen to change those
values and then call the commit() method. If I'm working on an embedded space with limited
RAM, I would rather for the developers to write much more explicit code with a rigid structure
to save 10MB here and there. The skeptic is screaming at the monitor that I shouldn't have
selected SQLAlchemy but should have picked xyz instead. Maybe, but what assumption does xyz
make that would impact that environment? Parts of SQLAlchemy were actually useful for us,
like the trigger mechanism. The problem is just too nebulous to have a clear definition that
fits.

It's also this nebulous definition of what services and tools a ORM framework needs to supply
that leads to another major problem - the lack of a standard grammar and API. Just as there's
a million ways to describe the sunrise to a blind man, there's a million different ways
to approach how to communicate and query a bucket of bits sitting on a storage device. It's
not that software engineers aren't trying, it's just a very difficult and poorly defined problem.
This leads to each camp trying different things using various language features that don't
transfer. SQLAlchemy uses a syntax with method chaining and objects, Hibernate has the HQL
string syntax that looks a lot like SQL. Plus each of these frameworks also give up at some
point and expose a means to break out into SQL. This means when I sit down to work on a code
base, I need to know multiple ways of accessing the underlying data depending on which section
of the application I'm in.

Just like there's a million different ways to describe the data retrieval problem, there's
also a million ways to structure storage of the data. This added number of conditions that the
ORM framework needs to deal with further forces the framework to provide more grammar to
compensate for the various schemas. By the time you code not only the object, define the schema
and code around any odd mapping rules that might exist, you've saved nothing in terms of speed
and efficiency. I've talked with a number of developers that won't even bother hooking a new Rails
app up to an existing data source. They would rather create a new schema using the Rails framework
and then migrate the data from the old database. This approach does offer the ability to fix
previous sins, it's not always possible given the constraints of the project.

Given these problems with ORM frameworks, what's the best solution? Honestly, there isn't one.
For quick demos or code that just needs to work and not evolve, a ORM framework might be fine.
Don't underestimate the power of regular, old SQL either. It's a data storage specification
that's been built over 20 years with a fairly large work force that should be able to understand
it. This doesn't mean you shouldn't try to wrap and abstract the data access bit into a well
contained unit but SQL isn't that evil. And sometimes you just need a text file with some data
too. Sequentially accessed data that can be read a line at a time and broken into pieces quickly
can outperform any database under the right conditions.

For myself, I'll still use a ORM framework for demo code that I have no intention of promoting
to a production code base. From my experiences though, I don't see ORM frameworks being flexible
enough to support the constant changing constraints of a production code base.
