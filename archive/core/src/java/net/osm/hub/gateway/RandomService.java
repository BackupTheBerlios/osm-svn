/*
 */
package net.osm.hub.gateway;

import org.apache.avalon.framework.component.Component;

/**
 * Service that provides the generation of random numbers.
 *
 * @author Stephen McConnell <mcconnell@osm.net>
 */

public interface RandomService extends Component
{

   /**
    * Return a random number used by factory implmentation during the creation of 
    * constant random identifiers.
    */
    public int getRandom( );
}



