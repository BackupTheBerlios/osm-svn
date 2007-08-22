/*
 */
package net.osm.hub.task;

import org.apache.avalon.framework.component.Component;

//import org.omg.Session.Task;
import net.osm.session.Task;
import net.osm.hub.gateway.FactoryException;
import org.omg.CosPersistentState.NotFound;
import org.omg.CollaborationFramework.Processor;


/**
 * Factory interface through which a Workspace reference can be created.
 *
 * @author Stephen McConnell <mcconnell@osm.net>
 */

public interface TaskService extends Component
{

   /**
    * Creation of a Task.
    * @param name the initial name to assign to the task
    * @param decription the task description
    * @return Task a new Task object reference
    */
    public Task createTask( String name, String description, Processor processor ) 
    throws FactoryException;

   /**
    * Returns a reference to a Task given a persistent storage object identifier.
    * @param pid Task persistent identifier
    * @return Task corresponding to the PID
    * @exception NotFound if the supplied pid does not matach a know Task
    */
    public Task getTaskReference( byte[] pid )
    throws NotFound;


}



