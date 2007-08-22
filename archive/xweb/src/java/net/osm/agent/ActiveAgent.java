
package net.osm.agent;

import net.osm.audit.home.Adapter;
import org.omg.Session.AbstractResource;
import org.omg.CORBA.ORB;

/**
 * The <code>ActiveAgent</code> interface suppliments the <code>Agent</code>
 * interface with support for operations dealing with event subscriptions 
 * between the agent and its remotely located primary object.
 */

public interface ActiveAgent extends Agent
{

   /**
    * The <code>setOrb</code> operations set the current ORB.
    * @param orb the current Object Request Broker
    */
    public void setOrb( ORB orb );

   /**
    * Test that returns true if the agents principal object is equal 
    * to the supplied object.
    * @param object object to compare against this agents primary object
    * @see #setReference
    */
    public boolean equals( AbstractResource object );


   /**
    * The <code>setAdapter</code> operation is used to declare the Adapter 
    * instance to be assigned to the agent.  The supplied adapter provides
    * the implementation of the mechanisms enabling event subscription, 
    * subscription modification, event reception, and forwarding of local 
    * events to associated agents.  An implementation of setAdapter is 
    * responsible for establishing the agents internal reference to the primary
    * resource. 
    * @param value the adapter supporting event subscription and incomming event
    * @see net.osm.agent.Agent#setReference
    */
    public void setAdapter( Adapter value );

   /**
    * The <code>connect</code> operation requests the establishment of a 
    * a connection between the agent adapter and the remote primary event producer.
    * The <code>connect</code> operation must be explicity invoked and an initial 
    * subscription declared before event reception will occur.
    */
    public void connect();

   /**
    * The <code>disconnect</code> operation requests the termination of 
    * event reception by the agent (or more strictly speaking, disconnection of 
    * the adapter's asociuation to the event producer).
    */
    public void disconnect();

}
