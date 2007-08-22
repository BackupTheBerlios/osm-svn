
package net.osm.agent.util;

import java.util.List;
import java.util.Iterator;
import java.util.LinkedList;
import org.omg.CORBA.ORB;

import net.osm.shell.Entity;
import net.osm.audit.RemoteEventListener;
import net.osm.audit.RemoteEvent;
import  net.osm.entity.EntityService;


/**
 * The <code>Collection</code> class maintains a list of entities 
 * based on a collection establised by a romote invocation using 
 * returning a CosCollection iterator, and maintained by notification
 * events from the source object.
 * @osm.warning maintaince of the list is not implemented
 * @osm.warning period verification of list integrity not implemented
 */
public class Collection extends LinkedList
{

    //=========================================================================
    // constructor
    //=========================================================================

   /**
    * Constructor of a new <code>Collection</code> based on supplied CosCollection 
    * iterator as the source for initial population of the list. 
    * @param orb object request broker
    * @param iterator a CosCollection iterator to be used to populate the list
    */
    public Collection( ORB orb, EntityService resolver, org.omg.CosCollection.Iterator iterator )
    {
        this( orb, resolver, iterator, false );
    }

   /**
    * Constructor of a new <code>Collection</code> based on supplied CosCollection 
    * iterator as the source for initial population of the list. 
    * @param orb object request broker
    * @param iterator a CosCollection iterator
    * @param isValue true if objects exposed by the CosCollection iterator should be handled 
    *    as valuetypes (as distinct from object references).
    */
    public Collection( ORB orb, EntityService resolver, org.omg.CosCollection.Iterator iterator, boolean isValue )
    {
	  populate( new CollectionIterator( orb, resolver, iterator, isValue ));
    }

    //=========================================================================
    // Collection
    //=========================================================================

   /**
    * Populates the collection based on a supplied iterator.
    */
    public void populate( Iterator iterator )
    {
	  try
	  {
	      while( iterator.hasNext() )
            {
		    add( (Entity)iterator.next() );
            }
        }
        catch( Exception e )
        {
		e.printStackTrace();
            throw new RuntimeException("Failed to populate the collection.", e );
        }
    }
}
