---
layout: post
title: Apple's Mountain Lion Up-to-Date Program
tags: general, ux
status: publish
type: post
published: true
comments: true
---
This year I made the decision to buy a new laptop. My MacBook Core Duo just was 
struggling to keep up with all the programs that I use on a daily basis for 
development. Running VMs with Linux and Windows didn\'t help so I jumped on board 
the new 15\" MacBook with Retina display because I could get 16GB of RAM in it. 
The added bonus was a free copy of Mountain Lion when it was released. I\'m still 
waiting on my download code from [Apple\'s up-to-date][apple-uptodate] program and 
that\'s a bit surprising.

<!--EndExcerpt-->

If you haven\'t experienced the up-to-date program, it\'s really baffling why Apple 
didn\'t spend a few more days to make the whole experience better. The forms start 
off by asking for some very basic information - name, address, date of purchase 
and email. From there, they then ask you to enter the serial number for the qualifying 
hardware that you purchased. When it\'s all said and done, you get a request 
confirmation code and the note that a promo code for the AppStore will be emailed 
to you.

This seems backwards. First, Apple obviously tracks the purchase order for every 
piece of hardware and ties the serial number to the purchase order. ERP systems 
like SAP and Oracle have been doing this for years so simply knowing the serial 
number would enable them to know when and who purchased the machine. This would tell 
them which machines qualify for free upgrades to Mountain Lion and which don\'t. I\'m 
not sure what\'s taking so long to verify my purchase but given the speed of database 
queries, I\'m guessing they dropped the ball or implemented some process where every 
request needs to be manually confirmed.

Second, the AppStore should make this whole web request process obsolete. The machine\'s 
serial number that I input on the up-to-date form came from the System Info utility. 
The AppStore program should have done the same lookup on my machine, send a query to 
the backend to determine if I qualify for the upgrade and then tie the upgrade to my 
machine and Apple ID. This was the biggest reason to own all the pieces of the product 
pipeline and why people love Apple products. This type of web form and wait process 
feels like something Dell or HP would do. This is seriously the best we can expect 
from Tim Cook?

There is a positive in all this - I have to wait a day or two before I can upgrade and 
thus watch to see if there are any major bugs that would change my mind.

__Update__: Two days later, I finally got a response from Apple. Turns out that they 
generate a PDF document with a redemption code protected by a password and email that 
to you. The password comes in a second PDF document and then you need to open AppStore 
and redeem it. For a company that enables checkout with smartphones in their brick and 
mortar stores, this seems very backwards and way to complicated. Hopefully the AppStore 
team is already streamlining this process for the next Mac OS release.

[apple-uptodate]: http://www.apple.com/osx/uptodate/