/*
 */
package net.osm.pss;

import org.apache.avalon.framework.component.Component;

import org.omg.CosPersistentState.Session;

/**
 * Service that providdes access to the current PSS persistence session
 * enabling access to storage object homes, storage object retrival and 
 * storage object creation.
 * @author Stephen McConnell <mcconnell@osm.net>
 */

public interface PSSSessionService extends Component
{

   /**
    * Returns the PSS session
    * @return Session PSS session
    */
    public Session getPSSSession( );

}



