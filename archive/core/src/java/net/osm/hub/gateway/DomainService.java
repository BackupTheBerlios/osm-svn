/*
 */
package net.osm.hub.gateway;

import org.apache.avalon.framework.component.Component;

/**
 * Service that provides access to a domain storage object identifier.
 *
 * @author Stephen McConnell <mcconnell@osm.net>
 */

public interface DomainService extends Component
{

   /**
    * Returns the short PID of the root gateway domain.
    * @return byte[] the PSS short persistent identifier
    */
    public byte[] getDomainShortPID( );

}



