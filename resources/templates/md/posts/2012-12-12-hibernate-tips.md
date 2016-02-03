{:title "Hibernate Tips"
 :layout :post
 :tags ["java" "database"]}

I\'ve decided to assemble a list of some cool features I found in the last week 
working with Hibernate. If you\'re going to use an ORM, you might as well learn to
use it effectively.

## Tip #1: Scan for Entity Classes

If you\'re using Spring ORM and Hibernate like we are at QThru, you\'ve probably
started off with a session factory that listed each of the entity objects in the
application context XML file. To avoid repeating what you\'ve already declared with
annotations, use the packagesToScan property to have Hibernate discover those entity
objects for you:

{% prism markup %}
<property name="packagesToScan">
    <array>
        <value>com.qthru.entity</value>
    </array>
</property>
{% endprism %}

Now adding, renaming or changing the relationship won\'t require touching the application
context file. The naysayers are going to mention how this isn\'t as fast as manually
declaring them. Is that really a problem for most of us? The session factory is going to
be wired and constructed when you bootstrap your application, a small price to pay for
developer sanity in the age of fast servers.

## Tip #2: Use Flyway for Schema Management

If you haven\'t hear of [Flyway][flyway-home], you need to click the link and get up to
speed. One of Rails best features was helping developers manage their database changes in
an agile way. After a few attempts, the Java community has finally caught up with our Ruby
peers. If you\'re also using Spring ORM and Hibernate, you should take it a step further and
have flyway check your schema when the server instance boots to apply changes. To do so,
you\'ll need to add a flyway bean definition:

{% prism markup %}
<bean id="dataSource" class="org.springframework.jdbc.datasource.DriverManagerDataSource">
    <!-- your data source definition for your environment -->
</bean>

<bean id="flyway" class="com.googlecode.flyway.core.Flyway" init-method="migrate">
    <property name="dataSource" ref="dataSource"/>
</bean>
{% endprism %}

When the flyway bean is created, the migrate method will be run and your schema changes
will be applied. To apply the changes, just setup a faux dependency on the session factory
bean.

{% prism markup %}
<bean id="mySessionFactory"
      class="org.springframework.orm.hibernate3.annotation.AnnotationSessionFactoryBean" depends-on="flyway">
    <!-- rest of session factory definition -->
</bean>
{% endprism %}

Now simply deploying the WAR file will also deploy any migrations that you have to the DB
before the application even gets bootstrapped. This has saved the development team at QThru
countless hours and taken the pain out of schema changes. Since bad code happens, you\'ll want
to make sure you have good database backups or snapshots before deploying new application
versions. Of course you have those already, right?

## Tip #3: Don\'t Fear UserTypes

Most people use Hibernate to map their entity objects into tables, but this still leaves custom
types on their own. For instance, QThru stores and uses [UPC][upc-wikipedia] data throughout our
application and it would be a shame to not have Hibernate convert between a VARCHAR field and a
UPC object that we\'ve defined. That\'s where UserTypes some into play. To get started, you\'ll need
to create a new class that extends UserType. In our UPC example I ended up with

{% prism java %}
public class UPCType implements UserType {
    @Override
    public int[] sqlTypes() {
        return new int[] {Types.VARCHAR };
    }

    @Override
    public Class returnedClass() {
        return UPC.class;
    }

    @Override
    public boolean equals(Object x, Object y) throws HibernateException {
        return x.equals(y);
    }

    @Override
    public int hashCode(Object x) throws HibernateException {
        return x.hashCode();
    }

    @Override
    public Object nullSafeGet(ResultSet rs, String[] names, Object owner) throws HibernateException, SQLException {
        final String upcAsString = (String) StringType.INSTANCE.nullSafeGet(rs, names[0]);
        return (upcAsString == null ? null : new UPC(upcAsString));
    }

    @Override
    public void nullSafeSet(PreparedStatement stmt, Object value, int index) throws HibernateException, SQLException {
        if(value == null)
            StringType.INSTANCE.nullSafeSet(stmt, null, index);
        else
            StringType.INSTANCE.nullSafeSet(stmt, ((UPC)value).toString(), index);
    }

    @Override
    public Object deepCopy(Object value) throws HibernateException {
        if(UPC.class.isAssignableFrom( value.getClass() ))
        {
            UPC upcValue = (UPC)value;
            return new UPC(upcValue.toString());
        }
        else
        {
            return value;
        }
    }

    @Override
    public boolean isMutable() {
        return false;
    }

    @Override
    public Serializable disassemble(Object value) throws HibernateException {
        return (Serializable)value;
    }

    @Override
    public Object assemble(Serializable cached, Object owner) throws HibernateException {
        return cached;
    }

    @Override
    public Object replace(Object original, Object target, Object owner) throws HibernateException {
        return original;
    }
}
{% endprism %}

The UPCType class just tells Hibernate how to get from JDBC objects to a UPC object
and vice versa. You can see in the nullSafeSet and nullSafeGet that we simply delegate
the details to the VARCHAR type and the UPC constructor. I imagine that you\'ll end up
doing very similar things in your implementation.

With our UserType in place, we now need to let Hibernate know that UPC instances should
use the new UPCType class to perform the actual CRUD operation. If you\'re using the JPA / Hibernate
annotation library, this is kind of annoying. You need to the use @TypeDef annotation
but you don\'t want to repeat this everywhere. Fortunately, you can avoid repeating the
TypeDef everywhere if you [annotate the package][java-package-annotate]. Unfortunately,
you\'ll need to annotate the package.

To annotate the package, you\'ll need to create a file called package-info.java in the
package you want to annotate. Within the package-info.java file you can annotate the
package declaration.

{% prism java %}
@TypeDefs({
    @TypeDef(defaultForType = UPC.class, typeClass = UPCType.class)
})
package com.qthru.entity;

import com.qthru.util.UPC;
import com.qthru.util.UPCType;

import org.hibernate.annotations.TypeDef;
import org.hibernate.annotations.TypeDefs;
{% endprism %}

You\'ll notice that the package statement still needs to be the first statement in the file
so all the imports will come after. Ugly, huh? The payoff is that we can now add a line to
our Spring ORM definition that tells Hibernate to scan the package for annotations.

{% prism markup %}
<property name="annotatedPackages">
    <!-- look for package level annotations -->
    <array>
        <value>com.qthru.entity</value>
    </array>
</property>
{% endprism %}

With this in place the rest of the team can just add a @Column annotation and use the UPC
type in any entity objects without worrying about the internal representation or any rules
associated with the UPC class. Those are all kept inside UPC and Hibernate continues to be
in charge of saving and loading data fields, no matter what type they are.

[flyway-home]: http://flywaydb.org
[upc-wikipedia]: http://en.wikipedia.org/wiki/Universal_Product_Code
[java-package-annotate]: http://docs.oracle.com/javase/specs/jls/se5.0/html/packages.html#7.4.1.1
