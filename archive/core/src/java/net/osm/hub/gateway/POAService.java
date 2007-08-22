/*
 */
package net.osm.hub.gateway;

import org.omg.PortableServer.POA;

import org.apache.avalon.framework.component.Component;

/**
 * Service through which the current POA can be accessed.
 * @author Stephen McConnell <mcconnell@osm.net>
 */

public interface POAService extends Component
{

   /**
    * Accessor to the current POA (Portable Object Adapter).
    * @return POA current POA
    */
    public POA getPoa( );

}



