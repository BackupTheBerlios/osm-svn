/*
 */
package net.osm.hub.desktop;

import org.omg.Session.Desktop;
import net.osm.hub.gateway.FactoryException;
import net.osm.hub.user.UserService;
import net.osm.hub.pss.DesktopStorageRef;
import org.omg.CosPersistentState.NotFound;
import org.omg.CosPersistentState.StorageObject;
import net.osm.realm.StandardPrincipal;
import org.apache.avalon.framework.component.Component;


/**
 * Desktop support services.
 *
 * @author Stephen McConnell <mcconnell@osm.net>
 */

public interface DesktopAdministratorService extends Component
{
   /**
    * Callback through which a UserService provider 
    * can register itself with the DesktopService.
    * @param service the task service
    */
    public void setUserService( UserService service );

   /**
    * Creations of a Desktop storage instance.
    * @param name the initial name to asign to the desktop
    * @param owner the storage object representing the owner
    * @param principal the principal to assign as owner
    * @return DesktopStorageRef desktop storage reference
    * @exception FactoryException
    */
    public DesktopStorageRef createDesktopStorageRef( String name, StorageObject owner, 
      StandardPrincipal principal ) 
    throws FactoryException;

   /**
    * Creation of a desktop delegate based on a supplied persistent identifier.
    * @param oid the persistent object identifier
    * @return Servant the servant
    */
    public DesktopDelegate createDesktopDelegate( byte[] oid );

}



