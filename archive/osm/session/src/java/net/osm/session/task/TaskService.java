/*
 */
package net.osm.session.task;

import org.omg.CosPersistentState.NotFound;
import org.omg.Session.AbstractResource;

import net.osm.session.resource.AbstractResourceService;
import net.osm.session.task.Task;

/**
 * Factory interface through which a Workspace reference can be created.
 *
 * @author Stephen McConnell <mcconnell@osm.net>
 */

public interface TaskService extends AbstractResourceService
{

    public static final String TASK_SERVICE_KEY = "TASK_SERVICE_KEY";

   /**
    * Creation of a Task.
    * @param name the initial name to assign to the task
    * @param decription the task description
    * @param processor the task processor
    * @return Task a new Task object reference
    */
    public Task createTask( String name, String description, AbstractResource processor ) 
    throws TaskException;

   /**
    * Returns a reference to a Task given a persistent storage object identifier.
    * @param pid Task persistent identifier
    * @return Task corresponding to the PID
    * @exception NotFound if the supplied pid does not matach a know Task
    */
    public Task getTaskReference( byte[] pid )
    throws NotFound;

   /**
    * Returns a reference to a Task given a persistent storage object.
    * @param store TaskStorage persistent storage object
    * @return Task corresponding to the storage object
    */
    public Task getTaskReference( TaskStorage store );



}



