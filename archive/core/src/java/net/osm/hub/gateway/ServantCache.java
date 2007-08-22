/*
 */
package net.osm.hub.gateway;

import java.util.Hashtable;
import java.util.Iterator;
import org.apache.avalon.framework.activity.Disposable;
import org.omg.PortableServer.Servant;
import org.omg.CosPersistentState.NotFound;

/**
 * Utility class that provides support for the maintenance of a cache
 * servants instances.
 * @osm.warning cache limitation policy and enforcement not implemented
 * @author Stephen McConnell <mcconnell@osm.net>
 */

public class ServantCache implements Disposable
{

    //================================================================
    // state
    //================================================================

   /**
    * Name of the cache.
    */
    private String name;

   /**
    * Trace enablement.
    */
    private boolean trace = false;

   /**
    * table of cached servants.
    */
    private final Hashtable servants = new Hashtable();


    //================================================================
    // constructor
    //================================================================

    public ServantCache( String cacheName )
    {
        this.name = cacheName;
    }

    //================================================================
    // Disposable
    //================================================================

    public synchronized void dispose()
    {
        Iterator iterator = servants.values().iterator();
	  while( iterator.hasNext() )
	  {
		Object servant = iterator.next();
            iterator.remove();
	  }
    }

    //================================================================
    // Manager
    //================================================================
    
   /**
    * Operation invoked to removal a servant from the cache.
    * @param oid persistent object identifier
    */
    public void remove( final byte[] oid )
    {
        synchronized ( servants )
        {
            servants.remove( new String( oid ));
	      if( trace ) System.out.println(
		  "cache/" + name + ", size:  " + servants.size() );
        }
    }

   /**
    * Adds a servant to the cache.
    * @param oid persistent object identifier
    */
    public void add( final byte[] oid, Servant servant ) throws Exception
    {
        synchronized ( servants )
        {
		if( servants.get( new String( oid )) != null ) throw new Exception("duplicate key");
            servants.put( new String( oid ), servant );
	      if( trace ) System.out.println( 
		  "cache/" + name + ", " + servants.size() );
	  }
    }

   /**
    * Returns a servant from the cache. If no servant is located in the cache
    * this operation will throw the NotFound exception.
    * @param oid persistent object identifier
    * @exception NotFound if the supplied oid does not exist in the cache
    */
    public Servant locate( final byte[] oid ) throws NotFound
    {
        synchronized ( servants )
        {
            Object servant = servants.get( new String( oid ) );
            if (servant == null) throw new NotFound();
            return (Servant) servant;
        }
    }
}



