{:title "QThru: Warp Pipe POS Integration"
 :layout :post
 :tags ["retrospective" "qthru" "startup"]}

> A lot of the Google inventions came from engineers just screwing around with
> ideas. And then management would see them, and we'd say, 'Boy, that's
> interesting. Let's add some more engineers.'
>
> -Eric Schmidt

One of the largest challenges the engineering team faced at QThru was the initial
setup with a retail location. Our proof of concept code required a CSV file
with product and pricing data that we would then import into our database.
We thought that the CSV format would be consistent between point of sale (POS)
systems and that we could just use a copy of that data file. This was not true.
We also assumed that the pricing managers and staff at the grocery stores would
comfortable setting up the import program. This was also not true.

 <div class="alert alert-info">
 This is the fourth in a series of retrospective thoughts on QThru, a mobile
 self checkout startup that went bust. See my
 "[QThru Series](/2016/06/03/the-qthru-series.html)" post for a list of all the
 topics.
 </div>

Our assumptions generated some friction with potential customers during the sales
cycle. We also had a skew of additional problems with data consistency. We learned
that even though the prices were set in the POS with a CSV import, the managers
were also going into the POS throughout the day to update pricing info. We were
now trying to maintain consistency between two separate systems. Many customers
also used specific pricing rules supported by the existing lane POS system. The
engineering staff was looking at a huge backlog of work just to replicate the
logic for all these rules in our own platform.

In December 2013, Aaron Roberts and I were discussing these frustrations in
regards to a sales lead that was experiencing problems integrating their store.
This particular store used the same POS as our pilot store but
they also used a workflow of updating data in the POS and using the POS to track
discounting for loyal customers. In a moment of frustration I jokingly suggested
that it would be easier to telnet to the POS terminal and ask for prices than
replicating all of the logic. I had seen a number of banks that
added a new GUI interface to their old mainframe systems by simply having those
GUIs manipulate the text screens through a terminal connection. If we would just
do that hacky solution, we wouldn't have to replicate all the logic. Little did I
know where this statement would lead.

The rough design was fairly simple. Our API server would need to lock a particular
POS terminal session for use to prevent any concurrency issues. Then we would
send a series of keystrokes to the POS that would perform a price check operation
on the terminal. After the API server had the result, it could release the POS terminal
session back to the available pool. Checking out was very similar but instead of
performing the price check action on the POS, our keystrokes would add items to
the session, total them up and then close out the transaction with a QThru payment
type.

The problem seemed to map fairly consistently with the [XMPP protocol][3]. We could
install a client program on a POS terminal in the store. This client would login
with a store specific username while the resource id would represent the specific
POS terminal in use. As an example, the Ridge IGA store POS terminal would be
identified as:

```
ridgeiga@warppipe.qthru.com/pos1
```

We could then use the [XMPP presence][4] functionality to perform lane reservation
and acknowledgment. When a lane was going to be reserved, we
would send a command asking the lane to reserve itself for a particular user GUID.
The client program would then set its presence to "Away" ("xa" for in the XMPP protocol) and
include the user GUID that the terminal is reserved for. We would wait for a presense
update with the user GUID acknowledgment before sending keystroke commands. When
we needed to release a POS terminal, the client would set its presence back to "Available". Using
XMPP in this way gave us an instant design for how to handle long lived connections,
retry logic and status communication.

The next step was generating a command syntax to send to each POS client. This
boiled down to only three commands - assign a terminal to a user, emit some
keystrokes, and release a terminal. In keeping with the XML nature of the
[XMPP protocol][3], we came up with the following scheme:

```
<!-- Assign a Terminal to a User -->
<assign xmlns="q:warppipe:1.0">
  UserGuid
</assign>

<!-- Emit keystrokes; <t> elements are literal text, <c> are special control keys -->
<parrot xmlns="q:warppipe:1.0">
   <t>1234567890</t>
   <c>ENTER</c>
</parrot>

<!-- Release a Terminal -->
<release xmlns="q:warppipe:1.0">
  UserGuid
</release>
```

The initial intent was to introduce three new stanza messages in the XML stream
to segregate the traffic. For demo purposes, I ended up just sending the XML in
the message stanza. This opened a whole slew of tools to us since we could use
any jabber server/clients to test various components in isolation. That turned
out to be huge and so we decided to keep the message stanza approach for the
duration of QThru's life.

The first working demo was a huge success. From one side of the conference room I was able
to use Adium to reserve the POS, watch the status change, send price lookup keystrokes,
and release the POS. It was fairly rough without any major components but proved
that we had something to work from. The next step would be to figure out how to
capture the pricing and cart data from the POS terminal. That was when Aaron
mentioned he found  a PDF document describing a UDP broadcast function that our
POS implemented for integrating with a CCTV security systems.

Within half a day we had updated our POS client with an async UDP server that would
listen on port 3456 for the POS CCTV XML messages. The more difficult part was
coming up with a schema for the XML without any developer documentation. We ended
up gathering 4 different XML schemas that we would need to check for:

```
<!-- Scan Item -->
<ISS45-WinposEvent>
	<EventHeader Terminal="1" Operator="1" Authorizer="1" TimeStamp="20130122161319" TransactionType="4092" Status="Success" />
	<Params>
		<Param Name="Till Num" Value="115" />
	</Params>
</ISS45-WinposEvent>
<ISS45-WinposEvent>
	<EventHeader Terminal="1" Operator="1" Authorizer="1" TimeStamp="20130122161319" TransactionType="4002" Status="Success" />
	<Params>
		<Param Name="Till Num" Value="115" />
		<Param Name="Item Code" Value="00007695041533" />
		<Param Name="Description" Value="Yogi Chai Rooibos   " />
		<Param Name="Entry Method" Value="MANUAL" />
		<Param Name="STD Price" Value="2.99" />
		<Param Name="Actual Price" Value="2.99" />
		<Param Name="Quantity" Value="1" />
		<Param Name="Sales Amount" Value="2.99" />
		<Param Name="Required Age" Value="0" />
		<Param Name="Actual Age" Value="0" />
	</Params>
</ISS45-WinposEvent>

<!-- Enter Tender Mode -->
<ISS45-WinposEvent>
	<EventHeader Terminal="1" Operator="1" Authorizer="1" TimeStamp="20130122161332" TransactionType="4065" Status="Success" />
	<Params>
		<Param Name="Till Num" Value="115" />
		<Param Name="Gross Amount" Value="2.99" />
		<Param Name="Net Amount" Value="2.99" />
		<Param Name="Elements" Value="0" />
		<Param Name="Tax Total" Value="" />
		<Param Name="Grand Total" Value="2.99" />
	</Params>
</ISS45-WinposEvent>

<!-- Complete Sale -->
<ISS45-WinposEvent>
	<EventHeader Terminal="1" Operator="1" Authorizer="1" TimeStamp="20130122161340" TransactionType="4003" Status="Success" />
	<Params>
		<Param Name="Till Num" Value="115" />
		<Param Name="Tender Type" Value="1" />
		<Param Name="Amount" Value="2.99" />
	</Params>
</ISS45-WinposEvent>
<ISS45-WinposEvent>
	<EventHeader Terminal="1" Operator="1" Authorizer="1" TimeStamp="20130122161340" TransactionType="4082" Status="Success" />
	<Params>
		<Param Name="Till Num" Value="115" />
	</Params>
</ISS45-WinposEvent>

<!-- Price Inquiry with No Option -->
<ISS45-WinposEvent>
	<EventHeader Terminal="1" Operator="1" Authorizer="1" TimeStamp="20130122161437" TransactionType="4047" Status="Success" />
	<Params>
		<Param Name="Till Num" Value="116" />
		<Param Name="Item Code" Value="00007695041533" />
		<Param Name="Description" Value="Yogi Chai Rooibos   " />
		<Param Name="Entry Method" Value="MANUAL" />
		<Param Name="STD Price" Value="2.99" />
		<Param Name="Actual Price" Value="2.99" />
		<Param Name="Quantity" Value="1" />
		<Param Name="Sales Amount" Value="2.99" />
		<Param Name="Required Age" Value="0" />
		<Param Name="Actual Age" Value="0" />
	</Params>
</ISS45-WinposEvent>
```

With the XML in place and some crude parsing code, we needed to return values
from the client to the other side of the XMPP connection. Again it was time
to come up with a data format. We had started playing around with the idea of
parsing the POS XML in the client but that could lead us to needing to roll out
client upgrades if the format should happen to change. Instead we decided that
we would wrap the raw UDP response values in a simple type:

```
<resp xmlns="q:warppipe:1.0">
  <!-- raw XML values -->
</resp>
```

This led to the second demo where we showcased grabbing a response to the keystroke
commands with a running POS. With the client program proof of concept and the
protocol defined, we were ready to design a server component with all the necessary
logic. We decided that we should build a new component, dubbed Warp Pipe, to
encapsulate a lot of the logic.

The first priority was building out a XMPP server that we could extend with a
ReST interface. This would make it easier to use from our mobile application
API as well as dynamic JS pages. It also provided us with an abstraction to
hide many of the details on how the XMPP interface worked. In the end we found
that the [node-xmpp][1] project was small and was currently being supported by the
library authors. After a few small patches to the authentication logic and a fix
to the TLS support, we had a server. We then used [restify][2] as our ReST framework.

Some of our early node.js code was pretty bad. It was clear that I was a Java
developer trying to write in a new language. After a few weeks, things became
more comfortable and I found myself using OO constructs less and relying more
on higher order functions and the underscore library to make the code expressive.
[Restify][2] allowed us to split the request logic into small functions that would be
composed together to define each API endpoint. This made each piece easier to
understand and test. After two months, we were ready to start testing the solution
end to end.

The initial testing was horrible. We had a number of problems with the lane
reservation code not freeing claimed lane resources. We also had parsing errors
on the UDP strings and random things that we struggled to reproduce. After an
hour of testing, Jon Wang had found at least 20 bugs with the code. That weekend
I went home and rewrote the lane reservation and parsing code using a TDD approach
after months of becoming familiar with node.js. On Monday, we tried all of the
failed tests and every single one passed. In fact, we actually had better speeds
too.

After 2 months generating proof of concepts and 3 months of coding/testing, we
were ready to show our new solution to our sales lead. During the demo, we scanned
an item with the QThru app, saw the POS next to me grab the price and then saw
the result on the phone all within a fraction of a second (looking at the logs
later, it was 380ms). Our data request left the mobile phone, went to our API
server in us-east-1, back through the XMPP channel to the POS and returned with
all the logic in less than half a second. This had the potential to change how
we would deploy our service.

That demo was the last time Warp Pipe ever saw use. Shortly after the demo in May,
Aaron started to devote his attention to a skunkworks project he hoped would be
his next startup, Cloud and Mortar. No one bothered to work on the sales agreement
with the customer and eventually the engineering team was laid off. I still think
the idea of using XMPP for IPC where you want to punt on connection and retry
logic is interesting. The XMPP standard has elements of IoT thinking well before
IoT was even a term. For future startups in the retail space, you need to figure
out your Warp Pipe idea. Replacing the existing POS or in store tech is a hard
sell and trying to duplicate functionality is an impossible goal.

[1]: https://github.com/qthru/node-xmpp
[2]: http://restify.com/
[3]: https://datatracker.ietf.org/doc/rfc6120/
[4]: https://datatracker.ietf.org/doc/rfc6121/
