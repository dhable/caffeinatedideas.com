Every year, I like to set goals for myself to help me focus on new things that I want to learn or adopt throughout 
the year. I typically model my goals on a layout similar to the [ThoughWorks Radar][thoughtworks-tech-radar] that
is published every quarter. The three main categories that I've used in my tech radar are:

* Exploring <br/>
  Topics listed in the _"exploring"_ section are items that I've run into either in conference talks or papers
  and want to know more about them. This is usually limited to either reading a book, paper or watching conference
  talks on the topic. Cursory knowledge is the goal with these topics.
  <br/><br/>
  
* Deep Dive <br/>
  Topics listed in the _"deep dive"_ section are items that I have a strong desire to know more about
  and am interested in adopting into my regular practice in the future. In addition to reading up on these,
  I will be using the concepts and ideas in personal projects and write about in the future.
  <br/><br/>
  
* Adopting <br/>
  Topics listed in the _"adopting"_ section are items that influence my every day approach to designing
  and writing software. In addition to researching each of these topics, I also plan to mentor those on projects around
  me in these topics. I do find it interesting the topics I've selected for adopting this year are concepts instead of 
  individual programming languages or frameworks. 
  <br/><br/>

With that general framework in mind, the goals I've come up with for 2020 are: 

* Exploring: [TLA+][tla] <br/>
  The idea of being able to model concurrent, distributed systems with a prover to probe for potential errors
  and bugs is intriguing. As backends move towards microservices, everything is becoming a concurrent, distributed
  system. [TLA+][tla] may never become a day to day tool but it's one I'm interested in exploring. 
  <br/><br/>

* Deep Dive: [Rust][rust] <br/>
  I found myself writing a bit of [go][go-lang] in 2019 on various cloud platform projects. While I enjoyed the return
  to more native and performant applications, [go][go-lang] mutable by default decisions felt strange to me coming from
  Clojure. [Rust][rust] takes the more familiar approach of immutable by default and adds compiler verifiable ownership
  of data that eliminates the need for a garbage collector. My gut is telling me that [Rust][rust] could displace C as
  the preferred stack for writing apps in the future as it makes reasoning about large code bases easier.
  <br/><br/>
  
* Deep Dive: [Reason][reason] <br/>
  [Reason][reason] is an OCaml dialect that the creator of React created while working on the React project for Facebook. I
  find the syntax easier to read than Javascript and brings along the well understood and strong type system from OCaml.
  I worry that moving it into adopting would be a hard sell just like [Elm][elm], [PureScript][purescript] 
  and [ClojureScript][cljs] are. 
  <br/><br/>
  
* Adopting: Linux <br/>
  In December I took the plunge and gave up my Macbook for an Ubuntu desktop machine. It's been an interesting
  switch and one that's highlighted how little I know about the architecture and troubleshooting of a Linux system.
  I got into this pattern of ignoring the OS since I started deploying applications into cloud containers and focused 
  more on web APIs. It was just easier to leverage some PaaS or throw away a borked image and start over. I really want
  to gain a better understanding of the platform that powers my desktop and the Internet as a whole.
  <br/><br/>
  
* Adopting: [Generative Testing][gen-testing] <br/>
  This is a topic that has been working up my radar over the years. The idea behind [Generative Testing][gen-testing]
  is that if you can define the relationship between the inputs and outputs of a program, you can let the computer
  generate way more input test cases than you can think of. A set of well defined relationships, aka properties, and
  you can now probe your code logic for corner cases you never thought of before. Now that we have a much smaller team,
  I think it's time to start introducing [Generative Testing][gen-testing] and get the computer to do more work for us.
  <br/><br/>

The overall theme for this year is exploring new ways of working. I just felt frustrated in 2019 when it came to
supporting large, complex code based with a team of distributed employees. The focus on tooling with stronger type
systems along with verification is a step in that direction. The other big theme is a return to systems programming,
a topic I was fascinated with during my undergrad time. My focus on web technologies has been a solid career move but
one I feel less passion for.


[go-lang]: https://golang.org/
[tla]: https://lamport.azurewebsites.net/tla/tla.html
[thoughtworks-tech-radar]: https://www.thoughtworks.com/radar
[rust]: https://www.rust-lang.org/
[reason]: https://reasonml.github.io/
[elm]: http://elm-lang.org/
[purescript]: http://www.purescript.org/
[cljs]: https://clojurescript.org/
[gen-testing]: https://8thlight.com/blog/connor-mendenhall/2013/10/31/check-your-work.html
