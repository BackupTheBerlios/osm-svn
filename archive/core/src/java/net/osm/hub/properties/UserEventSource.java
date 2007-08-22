/*
 */
package net.osm.hub.properties;

import org.omg.CosNotification.StructuredEvent;
import org.omg.Session.User;


/**
 * Iterface used by the property service to access the user object reference and 
 * methods enabling the posting of structured events.
 *
 * @author Stephen McConnell <mcconnell@osm.net>
 */

public interface UserEventSource 
{

   /**
    * Returns the object reference for the user.
    */

    public User getUserReference();


   /**
    * Post a structured event.
    */

    public void post( StructuredEvent event );

}



