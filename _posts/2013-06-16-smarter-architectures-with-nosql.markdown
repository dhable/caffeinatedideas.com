---
layout: post
title: Smarter Architectures with NoSQL
tags: idea
status: publish
type: post
published: true
comments: true
---
The pace of software development and the demands for teams to produce features 
has been steadily increasing. This increasing pace has driven the industry to 
innovate how we create software, like automating our tests and deployments. Another 
tools that can help development teams deliver features faster are NoSQL databases.

It\'s unfortunate that a lot of conference talks about NoSQL end up focusing on the 
technical scalability of the products. Most shops aren\'t going to need to worry about 
building web scale solutions initially, and may never need to build to web scale 
standards. This doesn\'t mean that we should then write off NoSQL databases. In addition 
to handling higher loads, many NoSQL databases already implement a lot of functionality 
modern apps use.

For example, let\'s build a mobile application for managing your shopping list. For the 
first iteration we\'ll use Java/Spring/MySQL stack for the service that the mobile 
application will call. 

![Spring Web Architecture](/assets/springmvc.png)

So where\'s the problem with this very common architecture? The dev team needs to write 
all the code to handle something as mundane as reading and writing information to a 
database. If you follow the recommended Java patterns, you end up writing a lot of 
code - ORM entity objects, DAOs, service objects, JSON view objects, controller objects 
and all the code to serialize/deserialize the requests on the mobile side. In fact, a lot 
of work is being done in the middle layer that doesn\'t add any value to the process.

You could streamline this code a bit by selecting a different language or some other libraries 
or even ignoring the recommended design pattern. At the end of the day, you still have 
this middle component that doesn\'t add any value to the process. Let\'s take some sage advise 
from Scrooge McDuck and work smarter, not harder. Let\'s rebuild our solution but this time 
use a document database, like CouchDB.

![NoSQL Alternative Web Architecture](/assets/nosql.png)

Turns out that CouchDB exposes all of it\'s database operations through a restful API and 
the documents are all stored as JSON. We can exploit that fact to eliminate all of the 
backend service code we had to maintain originally and have the mobile app work directly 
with CouchDB. Now the team can focus all their efforts on building out the client and if 
they need to adjust what type of information is being stored, they can just change the 
document format.

Leaving a database exposed works fine for development but not a good practice for production 
services. We can use a proxy, like nginx, as the exposed endpoint in front of CouchDB. The 
proxy approach also makes it easy to plug in security modules and URL rewriting to give 
your middleware a more polished feel without the need to write any code until you truly 
need a service that\'s going to offer more than simple data storage.

Teams shouldn\'t just look at NoSQL as solutions to web scaling problems. If teams are 
willing to think outside of the box, the use of NoSQL can cut down on how much work is 
required to deliver features. 
