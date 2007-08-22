/*
 */
package net.osm.session.resource;

import org.apache.orb.ORB;
import org.apache.pss.ActivatorService;

import org.omg.CosPersistentState.NotFound;
import org.omg.CosPersistentState.StorageObject;
//import org.omg.Session.AbstractResource;

import net.osm.session.SessionException;
import net.osm.realm.StandardPrincipal;

/**
 * Interface supporting AbstractResource management actions.
 *
 * @author Stephen McConnell <mcconnell@osm.net>
 */

public interface AbstractResourceService extends ActivatorService
{

    public static final String SERVICE_KEY = "ABSTRACT_RESOURCE_SERVICE_KEY";

   /**
    * Returns a new AbstractResource instance.
    * @param name the name of the resource to be created
    * @return AbstractResource object reference to the new resource 
    */
    public AbstractResource createAbstractResource( String name )
    throws AbstractResourceException;

   /**
    * Creation of a new <code>StorageObject</code> representing the 
    * state of a new <code>AbstractResource</code> instance.
    * @param name the name to apply to the new resource
    * @return StorageObject storage object encapsulating the resource state
    */
    public AbstractResourceStorage createAbstractResourceStorage( String name )
    throws AbstractResourceException;

   /**
    * Return a reference to an object as an AbstractResource.
    * @param StorageObject storage object
    * @return AbstractResource object reference
    */
    public AbstractResource getAbstractResourceReference( StorageObject store );

   /**
    * Returns an object reference matching a supplied identifier.
    * @param int identifiable domain object identifier
    * @return AbstractResource object reference
    */
    public AbstractResource resolve( int identifier ) throws NotFound;

   /**
    * Return a random seed used by factory implmentation during the creation of 
    * constant random identifiers.
    */
    public int getRandom( );

   /**
    * Returns a client principal invoking the operation.
    * @return StandardPrincipal the client principal
    */
    public StandardPrincipal getPrincipal() throws Exception;

   /**
    * Returns the current ORB instance.
    */
    public ORB getORB( );

}



