
package net.osm.agent.util;

import org.apache.avalon.framework.CascadingRuntimeException;

import net.osm.agent.Agent;
import net.osm.agent.AgentIterator;
import net.osm.agent.AgentServer;
import  net.osm.entity.EntityService;

/**
 * The <code>SequenceIterator</code> class provides support for simple forward iteration 
 * backed by an array value.
 */

public class SequenceIterator implements AgentIterator
{

   /**
    * The array backing the iterator.
    */
    private Object[] array;

   /**
    * The position of the next element in the array to be returned. The value of 
    * pos is initially set at 0 and is incremented on each call to next.
    */
    private int pos = 0;

    private EntityService resolver;


    //=========================================================================
    // Constructor
    //=========================================================================

   /**
    * Constructor of a new <code>SequenceIterator</code> based on supplied array.
    * @param array 
    */
    public SequenceIterator( EntityService resolver, Object[] array )
    {
	  if( array == null ) throw new RuntimeException("Null array value passed to constructor.");
        this.array = array;
        this.resolver = resolver;
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
	  return pos < array.length;
    }

   /**
    * Returns the next agent in the iteration.  The type of agent returned from the 
    * <code>next</code> method will be dynamically selected based on the type of 
    * underlying object and agent implementations available to the framework.
    */
    public Object next()
    {
        try
        {
		if( hasNext() )
            {
		    Object object = array[this.pos];
		    pos++;
		    if( object instanceof Agent ) return object;
                return resolver.resolve( object );
            }
        }
        catch( Exception e )
        {
		throw new RuntimeException("Unexpected exception while getting next array value.", e );
        }
        throw new RuntimeException("Unexpected exception while resolving next array value.");
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
        this.array = null;
    }

}
