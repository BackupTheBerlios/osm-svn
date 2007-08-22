/* 
 * AgentService.java
 */

package net.osm.agent;

import org.omg.CORBA_2_3.ORB;
import net.osm.hub.home.Finder;

import net.osm.shell.Service;
import  net.osm.entity.EntityService;

/**
 * The <code>AgentService</code> interface specifies a set of services to be provided 
 * by an agent server including access to a root <code>Agent</code> instance and a 
 * method through which objects (CORBA references and valuetypes) can be wrapped
 * within a new agent instance.
 *
 * @author Stephen McConnell
 */

public interface AgentService extends EntityService
{

   /**
    * Returns a factory finder reference.
    */
    public Finder getFinder();

   /**
    * Returns a <code>UserAgent</code> instance representing the 
    * principal user of the agent service.  This agent is typically 
    * used by the framework for task and message management.
    */
    public UserAgent getUserAgent();

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
    * Returns the current ORB
    */
    public ORB getOrb();

   

}
