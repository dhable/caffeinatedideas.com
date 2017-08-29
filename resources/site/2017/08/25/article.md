I've been pretty quiet on this blog for the last year as I worked on my own static site generator. 
This wasn't my original intention I wouldn't recommend building from scratch for people who just
want to publish content. This story starts back in 2015 when my site was built using 
[jekyll][jekyll-project] and hosted on S3.
 

## Toward a Clojure solution

Sometime around 2012 or 2013, I started learning more about static site hosting using AWS S3. The cost
would be pennies compared to the $10/mo I was paying for hosting a WordPress blog. That was a significant
savings for a site that I assume gets very little traffic. I started the process of exporting the old posts
from WordPress and building out a site with [jekyll][jekyll-project], the most popular static site generator 
at the time. I was a happy [jekyll][jekyll-project] user until I read [The Pragmatic Programmer][prag-prog-book].

One of the central concepts in [The Pragmatic Programmer][prag-prog-book] is that developers should 
understand how their tools are built, to the point where you feel comfortable enough to extend them.
This opinion is often used by emacs users who remind you that it's not a text editor but a lisp
platform that they can willingly extend. This had me thinking - could I debug or extend [jekyll][jekyll-project] 
if I needed or wanted to do something interesting with it? [jekyll][jekyll-project] is written in Ruby and I had 
no interest in picking up Ruby when I was already focused on functional programming languages like Clojure.

After a bit of searching, I ended up finding the [cryogen][cryogen-project] project - a static site generator 
written in Clojure. After working with a small demo site for a number of weeks, I felt comfortable enough
to move my entire site over. I started porting templates and reformating content. Along the way I ended up 
finding a number of minor hiccups but punted those as I was making good progress with the larger conversion effort.


## ...and then problems surfaced

[cryogen][cryogen-project], like most static site generators, provides a solution for non-technical
people to use. My first problem was with how [cryogen][cryogen-project] grouped posts for generating an
overview/index page. I don't generate enough content to warrant groupings by year and month and just wanted
to use the year. I put together a small change that exposed this as a config option and submitted a PR back
to the main project. Once that was accepted, I was back to using the standard release instead of my patched
version.

The next roadblock was tougher. [cryogen][cryogen-project] used its own directory structure for the generated
files and this structure was different from the one used by [jekyll][jekyll-project]. I didn't want to break 
all of the existing URLs on my content. With a fork in hand, I ended up hacking in some code that would output 
the same directory structure as [jekyll][jekyll-project] did. The change wasn't pretty and I was struggling to 
see how I could make a clean PR for this change. So instead of being on the main branch of [cryogen][cryogen-project], 
I was now using my custom fork. 

I had accomplished the goal of being able to extend my tools but I didn't like the idea of having a fork that I
would need to maintain with new fixes and features. The effort to turn the hack into a clean PR for the main
project was pretty significant. I decided that I'd put my change aside for the time and just release the site.

Sometime in 2015, I ended up pushing my first version of content generated with [cryogen][cryogen-project].


## Twelve months later 

I had been publishing content and happy with [cryogen][cryogen-project], even though I was using a forked
version of the code. I made some posts about QThru and didn't have any problems that I couldn't overcome.
I was also becoming more comfortable with Clojure every day and started to think back to my hack. It should
be possible that decisions like what directory structure to use for the output are simple to define in the
small Clojure bootstrap file. I should be able to compose a site from the small pieces. This then lead to
ideas about using [boot][boot-project] as it's just Clojure code composed together into a build system.

I started to take a shot at moving my site over to [boot][boot-project] and refactoring the [cryogen][cryogen-project]
code to make this idea of composable wrappers possible. Making major changes in someone else's code is 
never as straight forward as you might think it is. I simply did not have the time to dedicate to either 
of these efforts. In these moments I started to ask myself dangerous questions. If Clojure was all about
manipulating data, why do I need a framework to work in? I started playing around with that idea of using
plain old Clojure with [boot][boot-project] and not making much progress.


## Inspiration from Clojure Remote

I ended up putting aside the idea of using plain ol Clojure driven by boot as deadlines at work started
to pick up. I had pretty much left it aside until a few days after attending Eric Normand's Clojure Remote 2017 
[workshop on building composable abstractions][cr17-normand]. After the conference I was looking for something
to practice with. That's when I remembered my static site generator idea. I ended up deleting all the code
I had written to that point and started in a namespace docstring to list a physical abstraction, a book, and
then write the function signatures from that. After a few days of off and on work, I had defined a set of
records, functions to work on them and had an idea on how to structure the solution.

Proud with the extra experience gained using Normand's techniques, I then decided to start writing unit tests
for all this code. I had no intention of publishing this code for others to use but writing the unit tests
helped me verify in small chunks that what I was building was correct and also pointed out design issues that
I wouldn't have noticed until I started to use the various functions I defined. Working tests also gave me some 
momentum to keep going. Seeing the number of tests increase and the successful test runs is wonderful motivation 
to not ditch an idea.

It was still more work than I would have expected. Working with files and paths on the JVM isn't an easy thing.
That lead to issues about naming context variables in the templates, figuring out how posted dates will be
handles, where to output content and finally how to tie it all together with boot. It wasn't until Aug 2017 that
I had enough of a solution done that I could publish a new site given the current state. The small, focused
libraries in Clojure made it possible to tie together various elements into a coherent solution but did require
more research and work than a turnkey solution.


## My Solution (for now)

The final solution maps a set of EDN files into a set of HTML files, maintaining the directory structure of
the source files for the output. These EDN files contain a hashmap with three required keywords:

* `:template-name` is a string that contains the name of the template file that is going to be used to
  render the page in question, minus the '.html' part. 
* `:resources` is a vector of files, relative to the EDN page, that will be copied alongside the compiled
  output. This is used to include pictures within the blog posts but could be used for any file.
* `:data` is a hashmap that can contain anything that might be required for the template. As far as the
  site generation code is concerned, it's opaque and the schema depends on template and use.

So what did I store in the `:data` hashmap for blog posts in the first version?

* `:is-blog-post` is a simple boolean hack to let the index page filter out pages that it shouldn't
  include in the main index. A more robust solution would have been a filter based on path info and that
  is something for the future.
* `:title` is a string for the blog post title.
* `:tags` is a vector of strings to use as tags. I currently don't used them in the new template and
  might drop it in the future. It currently remains as a hold over from the prior site source.
* `:posted` is a java.util.Date on when the page was posted. The idea of a posted date isn't tied to a
  source filename or source path anymore. This makes it code sufficiently generic to handle other types of 
  content without tons of special cases.
* `:content` is a string with the content.

Writing content in an EDN file is painful and breaks preview and authoring tools. This is where 
the power of EDN and Clojure help. I was able to define my own custom reader macro, `#include`, that
takes a filename and executes a method to get the content string. Using a multimethod, I'm able to then implement
that function in such a way that it can use a markdown specific parser when the extension is '.md' and default to
using `slurp` for all other cases.

Using simple EDN data and pushing the schema to the template means that putting together a resume page for the
site just means figuring out the data I want to store in `:data` and building the template for the output.
Another page could define a set of current projects with timelines or a tech radar or even photo album. Going
through the process of eliminating the special cases and minimizing the code over and over led me to a more flexible
solution.

This isn't for everyone. Some users need sane defaults to guide their decisions - the path of posts was
a decision [jekyll][jekyll-project] made for me. If I was starting from scratch, I'm not sure that I would actually 
care about the directory structure or would even know enough to get started. I also need to figure out
how to solve problems, like RSS feeds or what to do about tag indexes. For now, they're not part of the site
because I simply haven't had the time to get them fixed.


## Going Forward

What I ended up with is something that is more than a static blog generator but more of a generic site generator.
Blog posts aren't handled any different by Clojure code and the only thing that makes them special is the data
contained in the EDN page. This makes it easy to apply the same code to data driven static sites, like my personal
profile that I maintain at [danhable.com][danhable-com]. In 2018, I'm planning on merging this blog site and that
profile into a single domain to cut down on costs. Although I might continue to maintain both and just extend my
code solution to generate one to N number of static sites with different templates.

In hindsight, I wouldn't recommend this as a solution unless you really want to go through the pain of scraping
designs, writing lots of boring utility code and end up in a very similar spot. The biggest gain from the whole
effort was in practicing the methods of breaking down an abstraction into Clojure code, writing Clojure and using 
libs and tools I normally wouldn't use in my day job. 


[jekyll-project]: https://jekyllrb.com/
[prag-prog-book]: https://pragprog.com/book/tpp/the-pragmatic-programmer
[cryogen-project]: https://github.com/cryogen-project/cryogen
[boot-project]: http://boot-clj.com/
[cr17-normand]: https://clojureremote.com/speakers/#normand
[danhable-com]: http://danhable.com