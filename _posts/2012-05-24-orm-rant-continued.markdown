---
layout: post
title: ORM Rant, Continued
tags: database, architecture
status: publish
type: post
published: true
comments: true
---
Back in January, I wrote an entry about not being a fan of ORM frameworks in 
software development. At the time, we were struggling to increase the performance 
of the native mail, calendar and contact clients on the PlayBook. Our use of 
SQLAlchemy hindered us by trying to abstract out database calls, sometimes in 
ways that made the performance worse. Truly getting the performance numbers we 
wanted often involved solving the data access problem and then spending just as 
much time to figure out how to solve the problem again with SQLAlchemy. It would 
have been easier to just write the SQL by hand and only used on level of abstraction.

This topic of ORM frameworks sprung to my mind last night while I was listening 
to Dmitriry Setrakyan talk about GridGain at the Seattle Scalability Meetup. On 
one of the slides, Dmitriry mentions that GridGain has built a native SQL engine 
into the product, allowing developers to use a subset of the SQL specification to 
manipulate the data from their data grid. Just think about that feature for a 
minute - GridGain abstracted their data access method to support SQL. They\'re not 
the only vendor using SQL as an abstraction of a non-relational data store either. 
Cassandra also added an [SQL subset syntax][cql] to make querying the data store 
easier as well.

I\'m not trying to defend the strict definition of SQL or even advocate that these 
products implement every feature that you have available to you in MySQL. That\'s 
just adapting and morphing the SQL language over time and not necessarily a bad 
thing. Yet, every single one of them has tried to innovate some kind of new method 
for querying and over time settled back on things that are very close to SQL. I 
don\'t think this is caused by developer\'s unwillingness to change. It\'s because the 
world has vetted and refined what SQL is and come up with a very good abstraction 
to the problem of data manipulation.

Since SQL is a very well tested way to describe how we want to query and work with 
data, I can\'t see what value adding an ORM tool can provide. Any gains you seem to 
make in the very beginning of your project will quickly be lost when you start trying 
to boost performance or add a real world use case. Chances are you\'ll be able to 
quickly come up with some form of SQL that gets you unblocked.

[cql]: http://www.slideshare.net/jericevans/cql-sql-in-cassandra
