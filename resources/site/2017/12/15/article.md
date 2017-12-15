This year I managed to finally read Nate Silver's book ["The Signal and the Noise: Why So Many Predictions Fail -- but Some Don't"][SignalAndNoise]. 
The concepts in the book will feel similar to any developer who's read up on or attended 
any agile session on estimating. In Chapter 11, Silver turns to looking at stock
markets and why the modern market behavior has changed when compared to the market
behavior of the past.

> This furious velocity of trading is something fairly new. In the
> 1950s, the average share of common stock in an American company was
> held for about six years before being traded -- consistent with the
> idea that stocks are a long-term investment. By the 2000s, the
> velocity of trading had increased roughly twelvefold. Instead of being
> held for six years, the same share of stock was traded after just six months.

Silver takes this argument through the stock trading process and proposes that the modern
investor, who uses someone else's money, is more likely to bet on companies and investments
not based on the fundamentals but rather on what aligns their moves with the rest of their
peers in the financial industry.

> These statistics represent a potential complication for efficient-market hypothesis:
> when it's not your own money on the line but someone else's, your incentives may change.
> Under some circumstances, in fact, in may be quite rational for traders to take positions
> that lose money for their firms and their investors if it allows them to stay with the
> herd and reduces their chances of getting fired.
>
> There is significant theoretical and empirical evidence for herding behavior among mutual
> funds and other institutional investors. "The answer as to why bubbles form", Blodget told me,
> "is that it's in everybody's interest to keep markets going up." 

Herd behavior is something we're all familiar with outside of investing and stock markets.
At some point in life, you made a decision to go along with a group, whether that was on where
to go for dinner or what look would be "cool" this year in school. I'm sure everyone who's sat
through a sprint planning session has also participated in herd behavior when it comes time to
estimate each story or unit of work in the sprint.

Many agile practitioners would argue that their estimation techniques produce better results,
be it planning poker or t-shirt sizing. While each technique uses a different scale for rating
work against other work, they all share a common practice - public discussion and defense of
everyone's estimate. In a recent project, estimates were taken and the lowest estimated value
and highest estimated value were then called out to argue for why they thought their estimate
was correct. Notice that we've now singled out two individuals and separated them from the herd
of their peers. This discussion goes on until a consensus (e.g. people change their estimate) 
is reached among the team and the common estimate is then used.

In principal this sounds like an open discussion of those with the greatest discrepancy should
start a conversation where the collective knowledge of group exposes the unknowns and clarifies
the problem so a true estimate can be achieved. After sitting through a number of different agile
implementations and estimating approaches, I don't think that goal is achieved. Instead, the
team follows a herding behavior. This can manifest itself in a few different ways:

1. The outliers seek to rejoin the herd. Even though they may have had honest and valid reasons
for their estimate, they quickly abandon those opinions and resign to the majority estimate value.
The reasons for concession may be mental fatigue from attending long planning meetings or it could
be a lack of confidence in their own self to participate in the process.

2. The outliers, even after a lengthy discussion, refuse to change their opinion. At this point
the lead then needs to make a decision to either ignore their opinion as an outlier or some other
means of compromise - like picking the middle value. Even with valid points, the remaining members
of the team might not change their estimates as they don't want to break away from the herd.

3. Based on who one of the outliers is, the middle majority will quickly rejoin that individual.
This mainly happens when one of the outliers is a senior engineer, lead or architect. There must be
a reason why that individual picked their estimate simply based on their position within the company.

In each of these cases, the estimating principals that the group wisdom knows best breaks down into
individuals steering and controlling the whole estimating process.

There might be other reasons for the herding behavior in estimating as well. Take a project that involves 
using some new, hot technology to build a product. Management is already skeptical but the team sold them 
on how easy it is to learn or how simple the work actually is. This means we start with a low estimate 
to gain project approval. As work proceeds, problems come up and deliverable slip. Management wants to 
revisit those estimates and work breakdown. The team is likely to herd together and keep the message to
management upbeat - major bugs are "easy" to fix with low estimates. Why does the team do this? Well,
it's in the best interest of everyone to keep the project going. If we had the honest conversation that
we're using the wrong tech or that the problem is much harder, we risk project cancellation and missing
out on that work.

There might not be an easy solution to the bias that herding introduces to estimating. As facilitators
of these planning meetings, we need to be aware that the process for many will be more comfortable if
they ignore real facts that would impact the work and simply align with their peers. Making the whole
process more inclusive of these outlining opinions should be the goal. We also need to identify when titles
may be driving the process and address that issue through smaller group sessions without those individuals
at first, adding their opinion in later. Simple async web systems that allow offline estimating and present
anonymous interaction could be an interesting thing to study.  


[SignalAndNoise]: https://www.amazon.com/Signal-Noise-Many-Predictions-Fail-but/dp/0143125087/ref=sr_1_1?ie=UTF8&qid=1513356807&sr=8-1&keywords=the+signal+and+the+noise+nate+silver
