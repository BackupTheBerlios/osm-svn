/* 
 * AgentService.java
 */

package net.osm.agent;

import org.omg.CORBA.ORB;
import org.omg.CommunityFramework.Community;

/**
 * The <code>AgentService</code> interface specifies a set of services to be provided 
 * by an agent server including access to a root <code>Agent</code> instance and a 
 * method through which objects (CORBA references and valuetypes) can be wrapped
 * within a new agent instance.
 *
 * @author Stephen McConnell
 */

public interface AgentService 
{

   /**
    * Return the current ORB.
    */
    public ORB getOrb();

   /**
    * Method used following server startup to establish the reference to the 
    * root community.
    */

    public Agent setRoot( Object value );
    
   /**
    * Returns the root agent.
    */
    public Agent getRoot();

   /**
    * Creation of a new Agent instance based on a supplied primary object.
    */
    public Agent resolve( Object object );

}
