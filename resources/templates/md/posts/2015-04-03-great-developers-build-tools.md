{:title "Great developers build tools"
 :layout :post
 :tags ["craftsmanship" "tools"]}

> It was important, his father said, to craft the backs of cabinets and
> fences properly, even though they were hidden. "He loved doing things right.
> He even cared about the look of the parts you couldn't see."
>
> Excerpt From: Walter Isaacson. "Steve Jobs."" iBooks. [https://itun.es/us/QyFUz.l][link]

Today I had to investigate a ticket that failed the end to end verification step.
The comment in the ticket contained a good amount of details about the environment
that the test was run in, what was performed and a little bit about the failure.
It was more details than a customer would provide but didn't include log files,
memory dumps or live debugging.

Yet, it only took me a few minutes to recreate the test and then less than a
minute to get the information I needed.

The magical answer wasn't a great new process where test engineering and product
engineering undergo a mind meld testing session. It was simply that the engineers
before me had either found or built all the tools I would need to track down the
problem. With a handful of script commands, I was able to rebuild a test env in
AWS and push a private branch to it. This tool also made is painless to SSH into
the box and look at the log files.

Not only did the bug get fixed fast, I was much happier while working on the ticket.

In this world of lean start ups and scrum, we need to remember that the quality of
the tools we use are just as important as the final product. The end user of our
software will probably never see that code so it's easy to be fooled into thinking
it's not as important as the other work. Yet a comprehensive toolbox will speed up
our daily work, help catch errors before they're released and give us a greater
sense of pride in our product. Next time you're looking to eliminate something
from the current sprint, don't pick the tooling. It's more important than you
think.

[link]: https://itun.es/us/QyFUz.l
