/*
 */
package net.osm.hub.gateway;

import org.apache.avalon.framework.context.Context;
import org.apache.avalon.framework.context.DefaultContext;
import org.omg.CosPersistentState.StorageObject;


/**
 * ServantContext is an object passed as an argument during the 
 * contextualize phase of a servant.  ServantContext suppliments 
 * the default context interface with accessors to the servant
 * storage objet.
 *
 * @author Stephen McConnell <mcconnell@osm.net>
 */

public class ServantContext extends DefaultContext
{

    //============================================================
    // static
    //============================================================
 
    public static final String STORE = "STORE";

    //============================================================
    // state
    //============================================================

    private StorageObject store;

    //============================================================
    // constructor
    //============================================================

    public ServantContext( Context parent, StorageObject storage )
    {
	  super( parent );
	  final String nullStorage = "null storage object supplied to constructor";
        if( storage == null ) throw new NullPointerException( nullStorage );
        this.store = storage;
        put( STORE, storage );
    }

    //============================================================
    // implementation
    //============================================================

   /**
    * The storage object for the servant.
    * @return StorageObject PSS storage object
    */
    public StorageObject getStorageObject()
    {
        return this.store;
    }

}



