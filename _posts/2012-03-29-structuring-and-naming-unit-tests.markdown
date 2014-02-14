---
layout: post
title: Structuring and Naming Unit Tests
tags: development
status: publish
type: post
published: true
comments: true
---
Unit testing and automated unit testing is part of all conversations around 
software development these days. It\'s used by the agile groups to point out how 
software gets developed without huge, monolithic testing ground and by quality 
groups to prove that early testing reduces the overall cost on an organization. 
Despite the topic being so wildly discussed and required in modern development, 
we don\'t really talk a lot about the nuts and bolts of development, specifically 
how to structure and name the test code that we write.

<!--EndExcerpt-->

I, like many people, started out with a test directory and set of classes that mirrored 
the source code. For the sake of this article, I\'m going to stub out an object that 
could be part of any business system - a purchase order record.

{% prism java %}
package bigsys.purchase;

public class PurchaseOrder
{
    public PurchaseOrderRecord() { 
        // creates a new purchase record
    }

    public void addItem( PurchasedItem anItem) { 
        // adds a single item to this order
    }

    public void addItems( List itemList) { 
        // adds all the items in the list to this order
    }

    public void approve( User approver, String note ) { 
        // records that the purchase is approved for next step
    }

    public void deny( User denier, String note ) { 
        // records the purchase is denied and why
    }

    protected boolean isValid() { 
        // makes sure the object is in a valid state after update
    }
}
{% endprism %}

I haven\'t provided the implementation details for this object just to reduce 
the noise for the purposes of this entry. We want to focus on the unit tests. 
Most of us have been in this camp and now try to write a unit test for this 
class using our favorite xUnit framework and probably create something that 
mirrors the production code in structure and naming.

{% prism java %}
package bigsys.purchase;

public class PurchaseOrderTest
{
    @Test
    public void testCreatePurchaseOrder() { 
        // ....
    }

    @Test
    public void testAddNullItem() { 
        // ....
    }

    @Test
    public void testAddSingleItem() { 
        // ....
    }

    // ... more tests to follow ...
}
{% endprism %}

This works but there a lot of down sides to this kind of testing. First, I still 
don\'t have a good idea what the tests will do and what they consider success vs. 
failure without reading through all the code. Second, how do I know that I covered 
all the critical possibilities of what my code should be doing? Third, as the 
number of test cases grow my test class will continue to grow and become more 
difficult to update and maintain. This last point is one that you simply can\'t 
avoid - every method will tend to have more than a single test that needs to run so 
my single test class will always be larger than the class it models.

Let\'s fix the maintainability of this test by breaking up the test code into a 
number of smaller files. To start, we\'ll build a new package in the test space that 
is the same as the class we\'re testing. Inside this new package, we\'ll place 
individual test classes.

{% prism java %}
package bigsys.purchase.PurchaseOrder;

public class AddItemTests
{
    @Test
    public void testAddNullItem() { 
        // ....
    }

    @Test
    public void testAddSingleItem() { 
        // ....
    }

    // ... more tests to follow ...
}
{% endprism %}

and

{% prism java %}
package bigsys.purchase.PurchaseOrder;

public class CreateTests
{
    @Test
    public void testCreatePurchaseOrder() { 
        // ....
    }
}
{% endprism %}

Breaking up the tests in this way provides a couple of different benefits. 
First, each of these test groups will be smaller. Second, we now know without 
hunting through the test run report which functionality might be broken. More 
often than not, developers should be committing code based around behavioral 
boundaries so it becomes easier to figure out if the unit test failure in 
question might be caused by a recent commit you may have made.

The last bit of refactoring we should consider would be to change the name of 
the classes and methods to become more human friendly. Years ago a peer suggested 
naming the unit test classes after the pattern "WhenYaddaYaddaYadda" and the 
methods after the pattern "ShouldBlahBlahBlah". This way, a new developer could 
read through the classes in the test package and see under what conditions the 
object is designed to be used (the when) and what should happen given various 
states (the should). This now serves as usage documentation for the code that 
isn\'t bound to get stale since we\'re always maintaining the tests as we develop 
the code. Reworking the example classes above using this new naming convention 
we end up with

{% prism java %}
package bigsys.purchase.PurchaseOrder;

public class WhenAddingPurchaseItem
{
    @Test
    public void shouldNotAllowNull() // was testAddNullItem 
    { 
        // ....
    }

    @Test
    public void shouldAllowASingleItem() // was testAddNullItem
    { 
        // ....
    }

    // ... more tests to follow ...
}
{% endprism %}

and

{% prism java %}
package bigsys.purchase.PurchaseOrder;

public class WhenCreatingPurchaseOrder
{
    @Test
    public void shouldBuildNewPOWhenNoKeyDefined() // was testCreatePurchaseOrder
    { 
        // ....
    }
}
{% endprism %}

As you can see, the new names contain a bit more information about what the 
expectations are and what we\'re really checking in the test. Older versions of xUnit 
frameworks may not be able to take advantage of the newer naming convention since they 
relied on the names of methods and classes to know which classes and methods to look 
for in the build path. Today, most frameworks and build scripts should have the 
capability to give you greater freedom in naming and structuring your tests.

I\'ve used these techniques to structure my tests for some time and I find that the 
overall maintenance and clarity of my tests is now on par with the production code.
