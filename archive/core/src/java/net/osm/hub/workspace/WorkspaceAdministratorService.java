/*
 */
package net.osm.hub.workspace;

import net.osm.hub.gateway.FactoryException;
import net.osm.hub.user.UserService;
import org.apache.avalon.framework.component.Component;


/**
 * Factory interface through which a Workspace reference can be created.
 *
 * @author Stephen McConnell <mcconnell@osm.net>
 */

public interface WorkspaceAdministratorService extends Component
{

   /**
    * Declare the UserService to the WorkspaceService.  This 
    * operation is provided as means to bypass circular dependencies
    * between user and workspace delegates.
    * @param service the user service manager
    */
    public void setUserService( UserService service ); 

}



