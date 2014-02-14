---
layout: post
title: My Journey with Seven Languages
tags: career, development, languages
status: publish
type: post
published: true
comments: true
---
Being nearly a year behind on my reading list, I finally broke out my copy of 
[Bruce Tate\'s Seven Languages in Seven Weeks][seven-lang-book] book for a little 
mind expanding exercise. At first I was a little disappointed that I\'ve already 
worked with most of the languages back from my days in the CS program at UWM. Even 
so, I decided to keep my mind open and I\'m glad that I did.

<!--EndExcerpt-->

Bruce starts out with Ruby, a language that feels very familiar for anyone who\'s 
programmed in Perl, Python, Java, C# or VB. It was a nice warm up to the style of 
the book and let me figure out what I wanted to take away from all the work. The 
programming problems were easily solvable with a bit of research on the web and 
reading through the API documentation. After a week again with Ruby, I found myself 
feeling fairly indifferent about the language. I know lots of people love it, but 
the blocking syntax with the 'End' keyword drives me nuts and I\'ve never liked that 
the name of the variable affects it\'s scope.

Next up was Io, which I was excited to get to. My only experience with Io was a brief 
lunchtime conversation I had with other RIM engineers while we discussed ways to build 
a rapid development SDK for the PlayBook. After working through the first two parts 
with Io, I started to see how Io\'s prototype model would be powerful for building a game 
engine where a designer could introduce special items into a game without needing to 
change the game engine. I was a bit let down in the last part as Bruce\'s explanation of 
Io\'s message system didn\'t contain the depth that I wanted. For anyone working through 
the Io chapter, there\'s something funny that happens with the OperatorTable when it\'s 
manipulated in the same file that defines the operator. To work around this, I needed to 
keep the solution code in a file and then load it into the REPL and check the code by hand.

Third on the list was Prolog. Ugh! The last time I used Prolog it was in my CS programming 
language course and I was turned off of Prolog after that semester. In the interest of 
keeping an open mind, I calmly took a few days to relax and then started through the third 
week. To my surprise, I ended up getting quite a bit out of this chapter. Maybe it\'s the 
wisdom gained after years of working in the industry, but the language made sense. Revisiting 
the pattern matching was truly elegant, designing recursive algorithms by pattern seemed 
natural and there was no shortage of problems that Prolog makes easy. I can only think that 
my Prolog experience in college was the product of attempting to quickly learn the concepts 
without really understanding the motivation as to why the world needed Prolog. I don\'t think 
I\'ll integrate Prolog anytime soon, but I\'m really glad that I gave it a second chance.

Next up was Scala. I\'ve been fascinated with Scala since I learned about it in 2008. Since 
then, I\'ve started to cool on the language and Bruce\'s chapter continued that trend. The book 
seems to make quite a jump in terms of time required here as the Scala chapter is the largest 
and the problems tend to require the most work to solve. Bruce starts the chapter by introducing 
Scala as a better Java and the problems in the first section tend to follow a very traditional 
OO, Java-ish feel. One thing people new to Scala will notice is that the type system and be a 
real PITA! During the development of my tic-tac-toe solution, I ran into a situation where the 
type system didn\'t like the null and string returns. After a frustrating time getting to the hear 
of the problem, I ended up using an empty string instead of null. The chapter wraps up with an 
overview of Scala\'s threading model and a problem that\'s tailored to showcase why Scala threading 
is better than Java.

Next was Erlang and this was another brand new language for me. I\'ve heard lots of people in the 
server scalability circles talking about it but never had the time to get hands on with it. After 
the refresher with Prolog, Erlang didn\'t seem so strange and was probably one of the easiest 
chapters to work through. I quickly plowed through the nuts and bolts of Erlang and then slowed 
down when Bruce introduced process monitoring. Erlang doesn\'t really make it easy to keep track 
of who\'s monitoring who in source code unless you have discipline and experience. Despite that, 
I was able to work through most of the problems in the chapter. The final problem, and one I wish 
Bruce spent more time on, related to understanding the Erlang standard library. It\'s pretty 
critical for any language to understand the library and how to use it. Too bad that Erlang\'s 
website isn\'t quite so nice for finding information. I\'d keep Erlang on a list of technologies 
to watch but I don\'t currently have a problem that matches it.

Finally, I got to the chapter on Clojure. Again, I had some preconceived notions about lisp style 
languages from the days in CS classes but put those aside. I\'d have to say that Clojure is one 
language I\'m pretty excited about. Bruce starts by covering the basic functional aspect and why 
the lisp style syntax isn\'t so bad. This was a fairly good overview of the language and some of 
the library facilities. Then we get into the real benefits to Clojure - the concurrency model and 
Java interop facilities. The Scala chapter talks a lot about why the Thread paradigm needs to be 
replaced but Clojure introduces a model that seems more complete than Scala\'s actor model. This 
chapter seems to be the second largest and is fairly dense but well worth the time to complete.

Yes, there is one more chapter on Haskell but it was at this point that I decided to put down the 
Seven Languages book. After getting ramped up on Clojure\'s concurrency model and some of the 
interop features, I started off to continue to deep dive into the Clojure language. At some point, 
I\'ll return to the chapter on Haskell simply to brush up on monad\'s and how they impact Haskell. 
It\'s one of those concepts that you end up discussing in the CS coursework. If my change of heart 
with Prolog and lisp are any indication, I think it will be easier to see why monads are important 
with more experience. 

This book is great for giving you a bit of a guided overview of a number of languages, whether 
they\'re new or old to you. Even if you\'ve worked with one in the past, it\'s best to keep an open 
mind and see if you can appreciate what each language has to offer. Who knows, you might find 
yourself becoming an advocate for one in the future.

[seven-lang-book]: http://www.amazon.com/Seven-Languages-Weeks-Programming-Programmers/dp/193435659X/ref=sr_1_1?ie=UTF8&qid=1348097921&sr=8-1&keywords=seven+languages+in+seven+weeks
