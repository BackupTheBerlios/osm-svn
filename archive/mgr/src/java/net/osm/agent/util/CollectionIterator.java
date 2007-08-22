
package net.osm.agent.util;

import java.util.List;
import java.util.Arrays;
import java.util.Iterator;

import org.omg.CORBA.Any;
import org.omg.CORBA.AnyHolder;
import org.omg.CORBA.BooleanHolder;
import org.omg.CORBA.TypeCode;
import org.omg.CORBA.ORB;
import org.omg.Session.LinksHolder;

import net.osm.agent.Agent;
import net.osm.agent.ActiveAgent;
import net.osm.agent.AgentIterator;
import net.osm.agent.AgentServer;
import  net.osm.entity.EntityService;

/**
 * The <code>CollectionIterator</code> class provides support for simple forward iteration 
 * backed by a org.omg.CosCollection.Iterator where object references and/or valuetypes
 * returned from the CosCollection iterator are wrapped within Agent instances.
 */

public class CollectionIterator implements AgentIterator
{
    //=========================================================================
    // static
    //=========================================================================

    private static final boolean trace = true;

    //=========================================================================
    // state
    //=========================================================================

   /**
    * The current ORB.
    */
    private ORB orb;

   /**
    * True if the iterator is returning packed valuetypes.
    */
    private boolean isValue = false;

   /**
    * The object reference to the org.omg.CosCollection.Iterator that this object 
    * represents.
    */
    private org.omg.CosCollection.Iterator iterator;

   /**
    * True if a call to next will return a value.
    */
    private boolean more = false;

   /**
    * True if a call to next will return a value.
    */
    private EntityService resolver;


    //=========================================================================
    // constructor
    //=========================================================================

   /**
    * Constructor of a new <code>CollectionIterator</code> based on supplied CosCollection 
    * iterator where the agents exposed by the iterator are agents backed by reference
    * objects (as distict from valuetypes).
    * @param orb object request broker
    * @param iterator a CosCollection iterator
    */
    public CollectionIterator( ORB orb, EntityService resolver, org.omg.CosCollection.Iterator iterator )
    {
        this( orb, resolver, iterator, false );
    }

   /**
    * Constructor of a new <code>CollectionIterator</code> based on supplied CosCollection 
    * iterator.
    * @param orb object request broker
    * @param iterator a CosCollection iterator
    * @param isValue true if objects exposed by the CosCollection iterator should be handled 
    *    as valuetypes (as distinct from object references).
    */

    public CollectionIterator( ORB orb, EntityService resolver, org.omg.CosCollection.Iterator iterator, boolean isValue )
    {
        this.orb = orb;
        this.iterator = iterator;
	  this.isValue = isValue;
        this.resolver = resolver;
	  try
	  {
	      more = iterator.retrieve_element( new AnyHolder() );
		if( trace ) System.out.println("ITERATOR: Established iterator, has content: " + more );
	  }
	  catch( Throwable e )
        {
		if( trace ) System.out.println("ITERATOR: Unexpected exception while creating CollectionIterator." + e ); 
		more = false;
        }
    }

    //=========================================================================
    // Operations
    //=========================================================================

   /**
    * Returns true is a subsequent call to <code>next</code> will return a valid
    * agent.
    */
    public boolean hasNext()
    {
	  return more;
    }

   /**
    * Returns the next agent in the iteration.  The type of agent returned from the 
    * <code>next</code> method will be dynamically selected based on the type of 
    * underlying object and agent implementations available to the framework.
    */
    public Object next()
    {
        boolean ok = true;
	  AnyHolder anyHolder = new AnyHolder();
	  BooleanHolder moreHolder = new BooleanHolder();
        try
        {
		ok = iterator.retrieve_element_set_to_next( anyHolder, moreHolder );
        }
        catch( Exception e )
        {
		throw new RuntimeException(
               "Remote exception while attempting to retrieve the next element.", e );
        }

        try
        {
		if( ok )
		{
		    more = moreHolder.value;
		    Object object = null;
		    if( isValue )
		    {
			  object = anyHolder.value.extract_Value();
		    }
		    else
		    {
			  object = anyHolder.value.extract_Object();
		    }
		    return (Agent) resolver.resolve( object );
		}
		else
		{
		    more = false;
		    this.dispose();
		    return null;
		}
        }
        catch( Exception e )
        {
		throw new RuntimeException(
		  "CollectionIterator. " +
		  "Local exception while attempting to retrieve the next element.", e );
        }
    }

   /**
    * The <code>remove</code> method will normally provide support for the 
    * removal of the last agent returned from the iterator - this functionality is
    * not implemented at this time.
    * @exception RuntimeException if invoked
    */
    public void remove()
    {
        throw new RuntimeException("Remove operation is not supported.");
    }

   /**
    * The dispose operation will be called on destruction of the instance prior to 
    * the invocation of the finalize operation.  The implementation destroys the underlying
    * CosCollection iterator and releases state members.
    */

    public void dispose()
    {
        this.iterator.destroy();
        this.iterator = null;
        this.resolver = null;
        this.orb = null;
    }

}
