/* 
 * Controls.java
 */

package net.osm.xweb;

import org.omg.CORBA.ORB;
import org.omg.CommunityFramework.Community;

import net.osm.agent.Agent;
import net.osm.agent.AgentService;
import net.osm.hub.home.Finder;

/**
 * The <code>Controls</code> interface specifies a set of operations providing access 
 * to the current ORB, the root community on a business object engine, security real 
 * management and agent management tools.
 *
 * @author Stephen McConnell
 */

public interface Controls
{
   /**
    * Returns the current ORB.
    */
    public ORB getOrb();

   /**
    * Returns the agent manager.
    * @return AgentService
    */
    public AgentService getAgentService();

   /**
    * Returns a factory finder reference.
    */
    public Finder getFinder();

   /**
    * Returns the root agent.
    */
    public Agent getRoot();

}
