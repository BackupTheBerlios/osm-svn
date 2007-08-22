
package net.osm.agent;

import java.util.LinkedList;
import java.util.ListIterator;
import org.apache.avalon.framework.activity.Disposable;
import org.apache.avalon.framework.CascadingRuntimeException;
import org.omg.Session.AbstractResource;


/**
 * The <code>Cache</code> class maintains a stack of Agent instances that are created 
 * using the resolve operations and asserted to the top of the stack on each occurance 
 * of a request for the particular agent.  The implementation ensures that frequently 
 * accessed agents are always available (to the extent that the size of the list allows).  
 * When the stack reaches a predefined size, the agent at the bottom of the stack is 
 * destroyed.  Subsequent attempts to the agent using resolve will result in the 
 * re-creation of the agent and assertion of the agent onto the top of the stack.
 */

public class Cache extends LinkedList
{

   /**
    * Maximum size of the cache.
    */
    protected int max;

   /**
    * Creates a new instance of cache with a maximum size.
    * @param max the maximum number of objects to hold in the cache
    */

    public Cache( int max )
    {
        super();
        this.max = max;
    }


   /**
    * The <code>add</code> operation overrides the classic linked list method
    * by pushing the added object onto position zero of the list (the top of 
    * the list) and checks for any entries exceeding the cache size limit.
    */
    public boolean add( Object object )
    {
        super.addFirst( object );
	  if( super.size() >= max )
        {
 	      try
            {
                Object last = super.removeLast();
		    if( last instanceof Disposable ) ((Disposable)last).dispose();
            }
            catch( Exception e )
            {
                throw new CascadingRuntimeException("Unexpected exception while disposing of an agent.", e );
            }
        }
	  return true;
    }

   /**
    * Returns an agent from the stack by invoking the <code>equals</code> method on 
    * each entry in the stack (top=to-bottom). If no agent is found, the method returns
    * a null reference.  If an agent is found, a side-effect is the promotion of the 
    * agent to the top of the stack.
    */

    public ActiveAgent locate( AbstractResource resource )
    {
	  int n = 0;
        ListIterator iterator = super.listIterator();
        while( iterator.hasNext() )
        {
		ActiveAgent next = (ActiveAgent) iterator.next();
            if( next.equals( resource )) 
		{
		    //
		    // remove the agent from its current position 
		    // and put in onto the top of the list
		    //
		    super.remove( next );
		    add( next );
		    return next;
		}
		n++;
        }
        return null;
    }
}
