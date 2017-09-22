> "Data is a precious thing and will last longer than the systems themselves."
>
> – Tim Berners-Lee

The mobile platform we developed at QThru started as a simple mobile application
with a ReST API to perform CRUD operations. The types of data that we stored was
one of the more valuable assets that our platform was generating. Over the 2 years 
of operation, the data model was transformed and enriched as we better understood
the domain. In this post, I'm going to point out some of the decisions we made 
about our data model and whether they were helpful or not.  

<div class="alert alert-info">
This is the fourth in a series of retrospective thoughts on QThru, a mobile
self checkout startup that went bust. See my
"[QThru Series](/2016/06/03/the-qthru-series.html)" post for a list of all the
topics.
</div>


## Your Schema Is Code

On my first day working at QThru, I started an effort to document the current
schema in SQL files and store this schema alongside the Java code. Up to this point, the only
up-to-date schema for the company was the production MySQL database. This made it tough for
developers to coordinate their local environments with the expectations of the platform, not to
mention that deployments were quite manual. In the first two days, I generated the schema and
implemented [Flyway][flyway] in our code base. [Flyway][flyway] provides a Rails-like schema 
management. This would ensure and migrate the schema of any database over time to match the code.

I'm not the first person to recommend this kind of database schema versioning but I firmly believe
you need this. Since putting in [Flyway][flyway], we added four new developers to contribute to the 
codebase and deployed multiple times to many different environments. Each time we knew that the DB was 
migrated correctly to a schema that the code expected. We knew that this was the same migration that
we tested prior to deployments. We never had a developer waste time after pulling code from github
because they didn't get the schema update.

The other remarkable effect from [Flyway][flyway] - we were comfortable changing our schema. Over 
the life of the API code, we actually changed the schema 75 times. This included major refactors, like 
our migration to a multi-tenant schema, and data migrations. Our migrations tended to be simple ALTER 
statements with one or two fields being adjusted. The agility of being able to evolve the data alongside 
the code is an accomplishments I'm proud of implementing at QThru.


## Item Entity

One of the largest tables in our schema was the Item table. 

```
﻿CREATE TABLE `Item` (
  `storeId` int(11) NOT NULL,
  `sku` varchar(40) NOT NULL,
  `description` varchar(255) NOT NULL,
  `size` varchar(255) DEFAULT NULL,
  `superseded_by` varchar(40) DEFAULT NULL,
  `posDescription` varchar(255) DEFAULT NULL,
  `aisle` varchar(10) DEFAULT NULL,
  `shelf` varchar(10) DEFAULT NULL,
  `departmentCode` varchar(15) DEFAULT NULL,
  `restricted` tinyint(1) NOT NULL DEFAULT '0',
  `linkedItem` bit(1) NOT NULL DEFAULT b'0',
  PRIMARY KEY (`storeId`,`sku`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
```

A row in the Item table represented anything that was in a Store for sale. A few of the interesting
columns included:

* `description` and `posDescription` both describe the item but `posDescription` was designed to
  match the text used by other POS systems the store may have. Often times those systems impose
  length and allowed character values that are more restrictive than our `description`. Since we
  tried to make the receipts issued by QThry match the POS, we often used the `posDescription` for
  the receipt but used the more verbose `description` on displays to the user. I don't think maintaining
  the parity with the paper receipts was all that useful and did add complexity to the code and DB.
* `superseded_by` field was a reference to another `sku` in the same `storeId` of an item that
  was a valid replacement for the item in question but not the same `sku`. Very often a supplier will
  change the UPC code of an item when they change the artwork on the package. This means a shelf can
  have a mix of both sets of items and both could be sold in the same cart. We wanted to collapse those
  items for users as well as run analytics knowing the two different `sku` values were the same item.
* `restricted` was an indicator that the checkout process needed an override from a store employee before
  continuing. This was used primarily to support the sale of alcohol, which was possible under WA law.
* `linkedItem` was used to sell a pair of items together through the scan of either item. This came up when
  we started to think about things like bottle deposits. Scanning the `sku` for the bulk good should also
  trigger a small fee for the packaging associated with it.


## What is a SKU?

In business terms a [SKU][SKU] is a unique id
for something that can be sold - a product or a service. [SKU][SKU]s are often times assigned by a business
for every unique item that is used by POS and backend systems. As we thought about the grocery item problem,
we decided that a [SKU][SKU] is really a generic term that includes UPC codes, PLU codes and custom POS codes
for anything not labeled with an industry standard code. Thus we decided to track `sku` values for items in
the database.

Additionally we wanted to support our own set coding scheme as well for retailers that want to sell an item
without a current code. We ended up coming up with the qCode for this purpose. qCodes were designed to encode
49 characters - 9 checksum characters, 40 identifier characters. The qCode was capable of being encoded in
a Version 4 QR code, which was the max our QR code scanner could work with within the time we wanted to wait
for a scan. An example Version 4 QR looks like

<a title="By Autopilot (Own work) [CC BY-SA 3.0 (http://creativecommons.org/licenses/by-sa/3.0)], via Wikimedia Commons" href="https://commons.wikimedia.org/wiki/File%3AQr-4.png"><img width="128" alt="Qr-4" src="https://upload.wikimedia.org/wikipedia/commons/8/8f/Qr-4.png"/></a> 

qCodes were used to identify our checkout kiosks and were also being developed for use in our
self service scales. This would let us encode more information about the item on the scale than just
the PLU and the weight. For instance, we could encode the total count entered for items that were priced
per unit or the weight taken as well as the price computed by the scale.

Values in the `sku` field were zero padded on the right if shorter than 40 characters. This means that `0`
has a special meaning in the leading position. It also means that any code that manipulates `sku` values needs
to ensure to use a string data type of a custom numeric value that doesn't drop the leading `0`, as most int
and long implementations do. 


## Pricing Entity
 
Pricing was kept in a separate table for every item. 

```
CREATE TABLE `ItemPrice` (
  `storeId` int(11) NOT NULL,
  `sku` varchar(40) NOT NULL,
  `priceStartDate` date DEFAULT NULL,
  `priceEndDate` date DEFAULT NULL,
  `priceType` tinyint(2) unsigned NOT NULL DEFAULT '0',
  `taxable` tinyint(1) DEFAULT NULL,
  `price` decimal(10,2) DEFAULT NULL,
  `bogo` tinyint(1) DEFAULT NULL,
  `quantity` int(11) NOT NULL DEFAULT '1',
  KEY `tmp_ItemPrice_key` (`storeId`,`sku`) USING BTREE,
  CONSTRAINT `ItemPrice_ibfk_1` FOREIGN KEY (`storeId`, `sku`) REFERENCES `Item` (`storeId`, `sku`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
```

Our pricing table was structured such that an item might have multiple price records in the `ItemPrice` table. 
The price to charge a consumer was calculated using the row with the highest `priceType` value where the current
date and time were within `priceStartDate` and `priceEndDate`. A `NULL` value for a date was used for records
that continued into the future or past forever.

The `quantity` field looks odd on the surface but we learned that a common retail price might be 3 for $1.
Doing the basic division is going to result in $0.333333.... and a problem. What price do you store and
what do you charge? After many conversations with retail managers we discovered that a price that doesn't
divide evenly rounds up on the first items and then only discounts the last. So in our 3 for $1 instance,
the first and second item should cost $0.34 and the third item costs $0.32. This lets the consumer buy a single
item but rewards them for buying in lots of 3. Storing the `quantity` and the `price` was necessary to support
that use case. 

This scheme of layering multiple price records was inspired from some work the team had done with ERP
systems. With this scheme in an ERP, it's possible to lay out a base price and plan out pricing exceptions
into the future. For a point of sale, this is overkill. Most point of sale systems only need to know what
to charge at this exact moment in time. Users will do their planning in an ERP and then have that system
push updates to the point of sale for every event. We could have simplified our code if we would have
adopted the point of sale model.


## Member Entity

Shoppers in our data model were referred to as members and had one of the weakest entities in the data
model.

```
CREATE TABLE `Member` (
  `guid` char(255) NOT NULL,
  `facebookId` varchar(255) DEFAULT NULL,
  `googleId` varchar(255) DEFAULT NULL,
  `favoriteStoreIds` varchar(255) DEFAULT NULL,
  `firstName` varchar(120) DEFAULT NULL,
  `lastName` varchar(120) DEFAULT NULL,
  `emailAddress` varchar(120) DEFAULT NULL,
  `mobilePhoneNumber` varchar(20) DEFAULT NULL,
  `active` bit(1) NOT NULL DEFAULT b'1',
  `created` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `modified` timestamp NULL DEFAULT NULL,
  `usedPromotion` bit(1) NOT NULL DEFAULT b'0',
  PRIMARY KEY (`guid`),
  UNIQUE KEY `facebookId` (`facebookId`),
  UNIQUE KEY `googleId` (`googleId`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
```

Aaron wanted to make sure that someone could install the QThru app and start shopping in a store without
going through a painful account creation process. This resulted in the mobile app asking the API if started
without a `guid` stored locally on the device. The API was very primitive and would generate a new GUID and
`Member` entity record. This could result in a number of `Member` records that all refer to the same individual
but no means of tying them together. In hindsight we should have collected a device fingerprint record as well
to help tie issued `guid` ids together.

The `facebookId` and `googleId` stored the unique identifiers used by those platforms and were obtained when
the user decided they want to link their social account to QThru. Only then were we able to obtain the other
identifying information, such as `firstName` and `emailAddress`. Later versions of the app started to require
those fields when shopping at stores that only did email receipts but we already had too much dirty data to
fix it at that point.

In hindsight, the `Member` table needed an denormalized counterpart to store all of the events that a user
took within our app. This includes things like adding and removing items from their cart, where users were
when the opened the app, every revision of the shopping lists, etc. This event stream would have been important
to use when trying to build marketing models for the users. We tried to rely on [TestFlight][TestFlight] for 
that data but the metrics were all aimed at sections of the mobile app and didn't provide the insight into
the backend service that we needed. This work would have been required had we shipped a recommendation and
intelligent promotion service.


## Storing Historic Transactions

Stores are always stocking new items and dropping old items that don't sell. This means that the `Item` table
is always changing and doesn't contain a historic view of the store. It also means that when a customer buys
an item, we don't want to simply link to the `Item` if you want to know what they bought. Instead, we created
a denormalized `Transaction` table to hold all the information we wanted to save when the checkout process
was completed.

```
CREATE TABLE `Transaction` (
  `id` int(11) unsigned NOT NULL AUTO_INCREMENT,
  `memberGuid` char(255) NOT NULL,
  `storeId` int(11) NOT NULL,
  `scannedInput` varchar(49) DEFAULT NULL,
  `sku` varchar(40) NOT NULL,
  `description` varchar(255) NOT NULL,
  `size` varchar(255) DEFAULT NULL,
  `unitPrice` decimal(10,2) NOT NULL,
  `quantity` int(10) NOT NULL,
  `appliedDiscounts` decimal(10,2) NOT NULL DEFAULT '0.00',
  `preTaxTotal` decimal(10,2) NOT NULL,
  `taxable` tinyint(1) NOT NULL DEFAULT '0',
  `departmentCode` varchar(15) DEFAULT NULL,
  `purchaseDate` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `spiritLiterSize` float(10,4) DEFAULT NULL,
  `bogo` tinyint(1) NOT NULL DEFAULT '0',
  `braintreeTransactionId` varchar(255) DEFAULT NULL,
  `unitQuantity` int(11) NOT NULL DEFAULT '1',
  `usedLoyaltyProgramId` int(11) unsigned DEFAULT NULL,
  `usedLoyaltyCode` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `memberGuid` (`memberGuid`),
  KEY `storeId` (`storeId`),
  KEY `idx_Transaction_Date` (`purchaseDate`) USING BTREE,
  CONSTRAINT `Transaction_ibfk_1` FOREIGN KEY (`memberGuid`) REFERENCES `Member` (`guid`),
  CONSTRAINT `Transaction_ibfk_2` FOREIGN KEY (`storeId`) REFERENCES `Store` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
```

In addition to storing the `sku` we also stored something called `scannedInput`, which was the 
unprocessed value that was read by the phone. Recall that UPC codes that start with a 2 indicate 
an item with a variable price. Since the price is encoded in the UPC code, there is value in keeping
that data value around for auditing later on. Additionally we also stored the `unitPrice`, `quantity`,
`appliedDiscounts` and `preTaxTotal` all separate. This again let us debug and support issues where
customers would claim that the calculations being made did not match their expectation. Storing the
tax calculate for the item would have also been beneficial but that value was easy enough using this
table and the BrainTree merchant report.

Some of the fields, like `bogo` and `spiritLiterSize` were quickly added to support buy one, get one
promotions or the WA liquor tax. Using [Flyway][flyway] allowed us to make these quick changes without
a full abstraction and continue to evolve the data model as we proceeded with development or supported
more use cases. It was often the difference between delivering features in a single sprint and having
a month of meetings to try and nail down the requirements.


## In The End

If you haven't figured it out, I highly endorse [Flyway][flyway] or some other form of schema as code
system to manage your database schema. That 2-3 day effort in the beginning of the platform's development
made scaling up the dev team and focus on new features easy to cope with. We never had to worry about
the schema for an environment or on a local dev machine. 

  

[flyway]: https://flywaydb.org/
[SKU]: https://en.wikipedia.org/wiki/Stock_keeping_unit
[TestFlight]: https://developer.apple.com/testflight/
