/*
 */
package net.osm.hub.user;

import org.omg.Session.User;
import org.omg.CosPersistentState.StorageObject;
import net.osm.hub.gateway.FactoryException;
import net.osm.hub.task.TaskService;
import net.osm.hub.pss.UserStorage;
import net.osm.realm.StandardPrincipal;

import org.apache.avalon.framework.component.Component;


/**
 * Factory interface through which a Workspace reference can be created.
 *
 * @author Stephen McConnell <mcconnell@osm.net>
 */

public interface UserAdministratorService extends Component
{

   /**
    * Creation of a User based on a supplied principal.
    * @param name the initial name to assign to the user
    * @return User a new User object reference
    */
    public UserStorage createUserStorage( StandardPrincipal principal ) 
    throws FactoryException;

   /**
    * Returns a reference to a User given a persistent storage object.
    * @param store the user persistent storage object
    * @return User the corresponding Uer reference
    */
    public User getUserReference( StorageObject store );

   /**
    * Callback through which a TaskService provider 
    * can register itself with the UserService.
    * @param service the task service
    */
    public void setTaskService( TaskService service );

}


