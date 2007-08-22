/*
 */
package net.osm.pss;

import org.apache.avalon.framework.component.Component;

import org.omg.CosPersistentState.Connector;

/**
 * Service that provides access to a PSS connection object under which
 * storage object types and homes can  be registered.
 * @author Stephen McConnell <mcconnell@osm.net>
 */

public interface PSSConnectorService extends Component
{

   /**
    * Returns the PSS connector
    * @return Connector PSS storage connector
    */
    public Connector getPSSConnector( );

}



