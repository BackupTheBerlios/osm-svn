
package net.osm.agent;

import net.osm.audit.RemoteEventListener;
import net.osm.audit.home.Adapter;
import org.omg.Session.AbstractResource;
import org.omg.CORBA.ORB;

/**
 * The <code>ActiveAgent</code> interface suppliments the <code>Agent</code>
 * interface with support for operations dealing with event subscriptions 
 * between the agent and its remotely located primary object.
 *
 * @author Stephen McConnell
 */

public interface ActiveAgent extends Agent, RemoteEventListener
{

   /**
    * Test that returns true if the agents principal object is equal 
    * to the supplied object.
    * @param object object to compare against this agents primary object
    * @see #setPrimary
    */
    public boolean equivalent( AbstractResource object );

    public boolean getActive();

}
