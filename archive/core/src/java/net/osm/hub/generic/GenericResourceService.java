/*
 */
package net.osm.hub.generic;

import org.omg.CommunityFramework.GenericResource;
import org.omg.CommunityFramework.GenericCriteria;
import net.osm.hub.gateway.FactoryException;
import org.omg.CosPersistentState.NotFound;
import org.apache.avalon.framework.component.Component;


/**
 * Desktop support services.
 *
 * @author Stephen McConnell <mcconnell@osm.net>
 */

public interface GenericResourceService extends Component
{

   /**
    * Creation of a GenericResource object reference based on a supplied name 
    * and criteria
    * @param name a initial name
    * @param criteria factory criteria
    * @return GenericResource a GenericResource object reference
    * @exception FactoryException
    */
    public GenericResource createGenericResource( String name, GenericCriteria criteria ) 
    throws FactoryException;

   /**
    * Returns a reference to a Desktop given a persistent storage object identifier.
    * @param pid GenericResourceStorage persistent identifier
    * @return GenericResource the corresponding PID
    * @exception NotFound if the supplied pid does not matach a know generic resource
    */
    public GenericResource getGenericResourceReference( byte[] pid )
    throws NotFound;


}



