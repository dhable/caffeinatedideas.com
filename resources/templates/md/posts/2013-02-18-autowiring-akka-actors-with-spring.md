{:title "Autowiring Akka Actors with Spring"
 :layout :post
 :tags ["java" "concurrency"]}

We consume a lot of data in various formats to deliver our mobile shopping 
experience and we need to process these data feeds quickly so we can offer
up-to-date pricing and inventory within our application. This means that we need
to avoid blocking operations as much as possible during our calculations and run
multiple elements through our import process concurrently. Instead of rewriting
tons of basic threading code, I wanted to leverage Akka\'s actor based model so we
could focus our code on the actual business logic of importing the data and less
on threading.

This solution seems awesome on paper but became a bit complicated when I looked at
the two constraints I had to work with. First, the component needed to remain in Java.
We\'ve talked a bit among the team in making the move to Scala but this wasn\'t the right
time to make the change. We needed to be done quickly and with the minimum amount of
variance in schedule so the team could move on to some important backlog items. Second,
the data model is already defined in POJOs that are annotated with Hibernate and wired
together with Spring. Reusing these POJOs was a must to ensure that additional data model
changes would be picked up in all components without repeating ourselves and causing
duplicate work. Even with these constrains, I considered Akka a solid direction.

For those not familiar with Akka, actors are instances of objects that can run as separate
threads and communicate by sending messages to each other. This gives you a lot of power to
build modular actors that perform a single action and then assemble those actors into work
flows that represent your process. This ability to setup our import logic via composition
and then tweak how each of the steps is threaded via Akka configuration is very powerful
and agile.

As I started to craft our first couple of actors, I ran into problems creating new actors
within the Spring framework. If we didn\'t use spring to autowire the dependencies, we would
also need to rethink how the POJOs we wanted to inject would also have their dependencies
wired. Since this code is shared across a number of components, I couldn\'t change the POJOs
or drop Spring from the picture. To complicate matters, Akka has its own way of creating actors
and this is through the actorOf() method on the ActorSystem object. Attempting to create an
instance of an UntypedActor outside of this factory method results in an exception.

After spending time looking through Google and some trial-and-error, I found the first part of
the solution in Akka\'s UntypedActorFactory. This allows you to control exactly how the instance
would be created and became the key to bridging the instance creation to the spring context.
The second part of the solution was the ApplicationContextAware interface. This solved the
problem of how does a bean get a reference to the context it is a part of. Using these two
concepts, I was able to present a way for Spring developers to create actors using a familiar
paradigm.

First, let\'s define a class that can build actors for us with a Spring context given what we
learned above..

{% prism java %}
public class ActorBuilder implements  ApplicationContextAware, UntypedActorFactory
{
   private ApplicationContext applicationContext;
   private String actorBeanId;

   public ActorBuilder(final String actorBeanId)
   {
      this.actorBeanId = actorBeanId;
   }

   @Override
   public final void setApplicationContext(ApplicationContext applicationContext) throws BeansException
   {
      this.applicationContext = applicationContext;
   }

   @Override
   public final Actor create() throws Exception
   {
      return (Actor)applicationContext.getBean(actorBeanName);
   }
}
{% endprism %}

Next, let\'s build an actor that uses @Autowired to obtain a reference to our
POJO data access objects. Note that the data access object also contains
dependencies and these will be injected as part of the autowiring process.

{% prism java %}
public class XmlDataFeedActor extends UntypedActor
{
   @Autowired
   private ItemDao itemDataHandler;

   public void onReceive(Object msg) throws Exception
   {
      // This is the logic of my actor
   }
}
{% endprism %}

Finally, let\'s update our spring XML definitions with a bean for the actor and a
bean  for the builder that can create this new actor.

{% prism markup %}
<!--  ItemDao bean is defined in a base XML conf file imported -->
<bean id="actorSystem" class="akka.actor.ActorSystem" factory-method="create" scope="singleton">
   <constructor-arg value="myapp"/>
</bean>

<bean id="xmlFeedActorBuilder" class="ActorBuilder" scope="singleton">
   <constructor-arg value="xmlFeedActor"/>
</bean>

<bean id="xmlFeedActor" class="XmlDataFeedActor" scope="prototype"/>
{% endprism %}

It\'s important to note that the bean definition for the actor needs to be scoped as
a prototype bean. Since prototype instances are unique, Spring will defer the creation
of the bean until the getBean() call for that particular definition. If you don\'t,
you\'ll get the Akka creation error while Spring is pre-creating beans in the
application context init.

To use the definition in an class we can now write:

{% prism java %}
public class SomeOtherClass
{
   @Autowired
   @Qualifier("xmlFeedActorBuilder")
   ActorBuilder xmlDataFeedBuilder;

   @Autowired
   ActorSystem actorSystem;

   public void someMethod()
   {
      //...
      ActorRef xmlFeedActorRef = actorSystem.actorOf(new Props(xmlDataFeedBuilder));
      xmlFeedActorRef.tell("some message");
      //...
   }
}
{% endprism %}

Using this template, we were able to continue to leverage the existing structure of our
Spring code but pull in new techniques to help us gain more control over our multithreading.
