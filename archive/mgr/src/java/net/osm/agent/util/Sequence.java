
package net.osm.agent.util;

import java.util.List;
import java.util.LinkedList;
import java.util.Iterator;
import net.osm.shell.Entity;
import  net.osm.entity.EntityService;

/**
 * The <code>Collection</code> class maintains a list of entities 
 * based on a collection establised by a romote invocation using 
 * returning a CosCollection iterator, and maintained by notification
 * events from the source object.
 * @osm.warning maintaince of the list is not implemented
 * @osm.warning period verification of list integrity not implemented
 */
public class Sequence extends LinkedList
{

    //=========================================================================
    // Constructor
    //=========================================================================

   /**
    */
    public Sequence( EntityService resolver, Object[] array )
    {
        SequenceIterator iterator = new SequenceIterator( resolver, array );
        populate( iterator );
    }

    //=========================================================================
    // Operations
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
