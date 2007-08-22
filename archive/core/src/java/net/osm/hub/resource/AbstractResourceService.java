/*
 */
package net.osm.hub.resource;

import org.omg.CosPersistentState.NotFound;
import org.omg.CosPersistentState.StorageObject;
import org.omg.Session.AbstractResource;
import org.apache.avalon.framework.component.Component;


/**
 * Factory interface through which an AbstractResource reference can be created.
 *
 * @author Stephen McConnell <mcconnell@osm.net>
 */

public interface AbstractResourceService extends Component
{
   /**
    * Return a reference to an object as an AbstractResource.
    * @param StorageObject storage object
    * @return AbstractResource object reference
    */
    public AbstractResource getAbstractResourceReference( StorageObject store );

}



