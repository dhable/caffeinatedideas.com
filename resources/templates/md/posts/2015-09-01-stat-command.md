{:title "stat command"
 :layout :post
 :tags ["linux" "reminder"]}

I was working on a shell script today that required determining the user that
owned a particular directory. A quick search online led me to [this][so-1] Stack
Overflow post. This led me to the following quick command that I could use in
a script

```bash
CASSANDRA_USER=ls -l /var/lib | grep cassandra | awk '{print $3}'
USER=${CASSANDRA_USER|-"cassandra"}
```

The second line sets the var USER to either the value in CASSANDRA_USER or to the
default string "cassandra" if the previous command did not result in a value. The
grep was the weak point and it actually started to fail when another component
added a directory to /var/lib that was owned by a user named cassandra.

At this point I was not looking forward to a more complex shell command with awk
and then I ran across a [better answer][better-answer] to the original question.
This involved a command that I had not used before - stat. The solution then
became

```bash
stat -c %U /var/lib/cassandra
```

It's simple, to the point and doesn't require more logic to handle other situations.
I'll make sure I use the stat command in the future instead of trying to parse
the output of the ls command.

[so-1]: http://stackoverflow.com/questions/7331651/find-the-owner-of-a-file-in-unix
[better-answer]: http://www.unix.com/unix-for-advanced-and-expert-users/81006-there-command-get-owner-file.html
