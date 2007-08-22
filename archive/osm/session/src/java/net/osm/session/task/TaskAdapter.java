
package net.osm.session.task;

import java.util.Iterator;

import org.apache.avalon.framework.activity.Startable;
import org.apache.avalon.framework.activity.Suspendable;

import org.omg.Session.task_state;
import org.omg.Session.ResourceUnavailable;

import net.osm.session.resource.AbstractResourceAdapter;
import net.osm.session.user.UserAdapter;

/**
 * An adapter providing EJB style access to a <code>Task</code>.
 */
public interface TaskAdapter extends AbstractResourceAdapter, Startable, Suspendable
{
    /**
     * Returns the <code>UserAdapter</code> of the user that owns the task.
     * @return  UserAdapter the owner of the task
     */
    UserAdapter getOwner();

    /**
     * Adds a resource to the set of resources consumed by this task.
     * @param  resource the rersource to add to the set of consumed task
     * @param  role the role of the resource within the scope of the task
     */
    void addConsumed( AbstractResourceAdapter resource, String role );

    /**
     * Removes a resource to the set of resources consumed by this task.
     * @param  role the resource role
     */
    void removeConsumed( String role );

    /**
     * Returns an consumed resource by name.
     * @param role the consumption role
     * @return  AbstractResourceAdapter the consumed resources matching the supplied role
     */
    AbstractResourceAdapter getConsumedByRole( String role ) throws ResourceUnavailable;

    /**
     * Returns an iterator of resources consumed by the task.
     * @return  Iterator of consumed resources
     */
    Iterator getConsumed();

    /**
     * Adds a resource to the set of resources produced by this task.
     * @param  resource the rersource to add to the set of produced resources
     * @param  role the role of the resource within the scope of the task
     */
    void addProduced( AbstractResourceAdapter resource, String role );

    /**
     * Removes a resource to the set of resources produced by this task.
     * @param  role the resource role
     */
    void removeProduced( AbstractResourceAdapter resource );

    /**
     * Returns an iterator of resources produced by the task.
     * @return  Iterator of produced resources
     */
    Iterator getProduced();

    /**
     * Returns the task state
     * @return  task_state the task state
     */
    task_state getState();

   /**
    * Returns the task description
    * @return String the task description
    */
    public String getDescription();
}
