
package net.osm.agent;

import org.apache.avalon.framework.activity.Disposable;
import net.osm.shell.Entity;

/**
 * The <code>Agent</code> interface specifies the minimum set of operations
 * shared by all agents - covering setter operations for the pricipal object 
 * and current Object Request Broker.  Implementations of the Agent interface
 * wrap remotely located objects and provide a convinient programming model 
 * supporting rapid application development and deployment consitent with the 
 * J2EE model.
 *
 * @see AgentService
 * @author Stephen McConnell
 */

public interface Agent extends Disposable, Entity
{

   /**
    * The <code>setPrimary</code> operation set the princial object that the agent 
    * will represent.  Typically, the object passed to this argument will be a CORBA
    * object reference of a business object, or valuetype related to the business 
    * object.
    * @param value the pricipal object
    */
    public void setPrimary( Object value );


}
