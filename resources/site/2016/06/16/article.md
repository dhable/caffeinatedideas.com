> I'm talking about liquid. Rich enough to have your own jet. Rich enough not to
> waste time. Fifty, a hundred million dollars, buddy. A player. Or nothing.
>
> -- Gordon Gekko

<div class="alert alert-info">
This is the second in a series of retrospective thoughts on QThru, a mobile
self checkout startup that went bust. See my
"[QThru Series](/2016/06/03/the-qthru-series.html)" post for a list of all the
topics.
</div>

In 2009, the smartphone app market was on fire. Apple's decision to release a
native app SDK that developers actually wanted to use unleashed the creativity
of the world and new businesses started to emerge. In particular, startups
were looking at how an Internet connected computer could be used to process
payments. In February that year, Square launched their mobile payment platform
for iOS.

QThru initially started in the same digital wallet space. The founding
engineers were already working on an Android wallet solution when Aaron Roberts,
the soon to be CEO of QThru, discussed changing from a wallet into a mobile
point of sale solution. Instead of processing the payments, QThru would maintain
the product catalog and shopping cart. When the user checked out, payments would
be handled through a third party - BrainTree. The whole platform worked much
like any eCommerce platform at the time but the real innovation was the user
experience.

Instead of searching for products by name or with a text entry, the QThru app
would use the iPhone's camera to scan the barcode. This made finding and adding
products to the user's cart far simpler than other web based solutions at the
time. To expedite the development, Aaron found and partnered with Scandit for
the barcode scanner. Early days on iOS didn't have a native barcode scanning
library in the SDK.

![Checkout Lines](/posts/2016-06-16-qthru-business-model/lines.jpg)

To monetize the platform, QThru's initial business model was to charge a
transaction fee (I believe of 2.5%) to the retailer. Right away the financial
leadership of QThru highlighted that the transaction fee didn't cover the fee
BrainTree was charging us to process the payment. This led to a fairly steep
fee being pitched to retailers. Under the final pricing plan a retailer was:

* charged a flat monthly fee for having the service active. This ranged between
  $100 to $500 depending on how many SKUs you wanted to load into the system.
* charged an installation fee, which included us shipping produce scales and
  checkout kiosks. While the hardware provided through partners was expensive,
  QThru also added a markup for the software we loaded.
* charged a 4% per transaction, $0.25 minimum.

The breakdown here really was a lack of empathy for the retail customer. Our
target segment wasn't premium brand retailers, like Apple. Our customers were
grocery stores that couldn't justify such a steep cost for letting a customer
who just wanted a bottle of Coke and didn't want to wait in line. To make the
situation worse, retailers didn't have an option of using their existing payment
processor or electing to not by a kiosk.

To make the sales arrangements more contentious with retailers, we insisted that
the mobile app remain a QThru branded app and that they simply add their retail
locations to our map. Basically, we wanted to hijack their brands in order to
build the QThru brand. More than once the VP of engineering asked if this was
a wise idea and the idea of providing a SDK or whitelabeled apps was always
dismissed without further discussion.

Without any value other than getting a small subset of customers through the
line faster, we never did manage to sell this plan to a single retailer. The
few contracts we had made a lot of concessions on the price of the service, in
some cases giving it away for free.

Seeing that the per transaction/monthly fee model wasn't going to work, our CMO
suggested we look at building out a promotion engine so we can sell customer
segmentation and targeted ads. The idea here is that Coke could make offers and
recommendations to you while shopping in an attempt to convert a Pepsi purchase
to a Coke purchase. This is a vastly different platform that what we had
developed and would take six months to a year to beta. With this divide and the
continued commitment to selling the transaction/monthly fee model, we ran out
of money with no progress.

In hindsight, the transaction/monthly fee model would work if we could offer
something to the retailer that they didn't have before - like insight into their
customer's behaviors. The advertising strategy would have also worked but our
product needed to be vastly different, in particular, we'd need to let the
retailers build the mobile apps and just provide the SDK. Our business wouldn't
be consumer facing but rather retailer facing. I've also come to realize that
grocery stores are already in a low margin business and tacking on any fee
won't work.
