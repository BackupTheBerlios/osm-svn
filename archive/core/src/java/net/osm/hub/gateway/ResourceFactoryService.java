/*
 */
package net.osm.hub.gateway;

import org.apache.avalon.framework.component.Component;

import net.osm.hub.home.ResourceFactory;

/**
 * Service the provides access to a <code>ResourceFactory</code>.
 *
 * @author Stephen McConnell <mcconnell@osm.net>
 */

public interface ResourceFactoryService extends Component
{

   /**
    * Accessor to a resource factory.
    * @return ResourceFactory
    */
    public ResourceFactory getResourceFactory( );

}



