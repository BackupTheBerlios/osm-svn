/*
 */
package net.osm.hub.desktop;

import org.omg.Session.Desktop;
import net.osm.hub.gateway.FactoryException;
import net.osm.hub.pss.DesktopStorageRef;
import org.omg.CosPersistentState.NotFound;
import org.omg.CosPersistentState.StorageObject;
import org.apache.avalon.framework.component.Component;


/**
 * Desktop support services.
 *
 * @author Stephen McConnell <mcconnell@osm.net>
 */

public interface DesktopService extends Component
{

   /**
    * Creations of a Desktop storage instance.
    * @param name the initial name to asign to the desktop
    * @param owner storage object representing the owner
    * @return DesktopStorageRef desktop storage reference
    * @exception FactoryException
    */
    public DesktopStorageRef createDesktopStorageRef( String name, StorageObject owner ) 
    throws FactoryException;

   /**
    * Creation of a Desktop object reference based on a supplied storage object.
    * @param store a desktop storage object
    * @return Desktop a Desktop object reference
    * @exception FactoryException
    */
    public Desktop getDesktopReference( StorageObject store ) 
    throws FactoryException;

   /**
    * Returns a reference to a Desktop given a persistent storage object identifier.
    * @param pid sektop persistent identifier
    * @return Desktop the corresponding PID
    * @exception NotFound if the supplied pid does not matach a know desktop
    */
    public Desktop getDesktopReference( byte[] pid )
    throws NotFound;


}



