There's a lot of terminology we use without thinking about the meaning of those words. 
When I started tinkering around with the insides of the IBM PC and configuring hard drives,
it was common to see references to the master and the slave drive in the BIOS and on the
jumper settings. Those same terms were used in textbooks and lectures about database systems
and highly available system designs in the 90s.

Those terms, along with others in computing and engineering, have racists meanings and
can make other people feel unwelcome. Considering how many other terms, like leader/follower
or primary/secondary, we have to describe the same concept without loss of meaning, we
need to stop using out of date terms. I want to work with a team that feels welcome to
participate in the construction of software and should never feel scared, excluded or
intimidated.

I decided to take a small step and rename the default branch in all my git repos
to `main`. [These instructions][rename-instructions], four steps, was all it took
for most of the repos. My Travis CI configuration ran a build on the new `main` branch
without a problem and retained the build history as well. For archived repos, I had to
first unarchive the repo before I could do the rename but GitHub makes that super easy
as well.

For any forked projects, git works perfectly fine with your local fork using the `main`
name and the upstream retaining the legacy name. You'll just have to use a few extra
keystrokes when referring to the upstream repo. Or better yet, you can ask that individual
to rename their default branch as well.

The math and physics that make computing possible don't discriminate. We shouldn't either.
#BlackLivesMatter

[rename-instructions]: https://gist.github.com/ccopsey/9866a0bcb0b39ade04fe
