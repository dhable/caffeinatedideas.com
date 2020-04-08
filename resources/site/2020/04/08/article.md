Hard to believe but we're already three months into 2020 and it's time for a quick
retrospective on my [2020 Tech Radar goals][q1-2020-radar].

* Exploring: [TLA+][tla] <br/>
  No significant progress to update on this. I've read through the first chapter of
  [Practical TLA+][tla-book] but didn't go beyond that.
  <br/><br/>
  
* Deep Dive: [Rust][rust] <br/>
  Started reading [The Rust Programming Language][rust-book] book. I'm two chapters 
  in and so far it's still pretty standard language stuff.
  <br/><br/>
  
* Deep Dive: [Reason][reason] <br/>
  No progress.
  <br/><br/>
  
* Adopting: Linux <br/>
  This goal had the most significant progress so far. In December, I had switched
  to Ubuntu running on a Dell 5520 laptop for work and adjusted to a new tool chain.
  For years, I've leaned on Mac specific tools, like Alfred, Tower and Dash, out of
  habit. While comparable tools do exist for Linux, I've found that I don't really
  use Dash, most OS versions offer launcher functionality like Alfred and it was well
  past the time to actually learn how to use git from the command line. I would have
  to say that the most difficult part of the switch has been the device driver support.
  The Broadcom wifi chipset on my 5520 has a pretty flaky driver that I've been troubleshooting
  for some time. I had success using an unofficial firmware release I've found through
  some discussion forums but the firmware change is always undone when an official update
  comes through.
  <br/><br/>
  
* Adopting: [Generative Testing][gen-testing] <br/>
  No progress.
  <br/><br/>
  
Not bad progress but not a ton. I'm pretty happy with the results given that I also switched
employers during this time. New job also means that I'll be making some changes to these
goals.

* Exploring: [TLA+][tla] <br/>
  Verification of complex algorithms and systems is always going to be necessary and so I'm
  still interested in putting in the exploring effort to see how this might impact my
  development process.
  <br/><br/>

* Deep Dive: [Rust][rust] <br/>
  After having done some AWS lambda work in node.js, I can see the need for predictable
  performance, small overhead and fast execution. This is one I'm still very interested
  in.
  <br/><br/>

* Deep Dive: [Generative Testing][gen-testing] <br/>
  This is going to change to learning the generative testing landscape in [scala][scala]
  and how to apply it.
  <br/><br/>
  
* Adopting: [Scala][scala] <br/>
  My new job will be building out back end systems with [scala][scala] and I'm excited to
  actually have a type system again. This is going to be a large focus for me over the next
  year. I'm finding that the functional approaches and design patterns are not a huge change
  from Clojure but there are some interesting constructs, like traits and implicits, that
  I haven't given much thought about recently.
  <br/><br/>

The notable changes here are dropping Linux and [Reason][reason] and 
moving [generative testing][gen-testing] to deep dive. The focus for the remainder of this
year are really going to be getting up to speed on [scala][scala] and its ecosystem. Adding
another programming language will spread me too thin. Additioanlly, I'm not sure how my new
team and company feel about [generative testing][gen-testing] or Linux on the desktop. I need
some time to navigate the organization and feel out those before committing to adopting them.
  
[q1-2020-radar]: /2020/01/15/my-2020-tech-radar.html
[tla]: https://lamport.azurewebsites.net/tla/tla.html
[tla-book]: https://www.apress.com/gp/book/9781484238288
[rust]: https://www.rust-lang.org/
[rust-book]: https://nostarch.com/Rust2018
[reason]: https://reasonml.github.io/
[gen-testing]: https://8thlight.com/blog/connor-mendenhall/2013/10/31/check-your-work.html
[scala]: https://www.scala-lang.org/