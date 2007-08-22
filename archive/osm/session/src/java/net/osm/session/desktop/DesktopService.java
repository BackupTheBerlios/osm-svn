/*
 */
package net.osm.session.desktop;

//import org.omg.Session.Desktop;
import org.omg.CosPersistentState.StorageObject;
import org.omg.CosPersistentState.NotFound;

import net.osm.session.workspace.WorkspaceService;
import net.osm.session.user.UserStorage;

/**
 * Factory interface through which a Desktop reference can be created.
 *
 * @author Stephen McConnell <mcconnell@osm.net>
 */

public interface DesktopService extends WorkspaceService
{

    public static final String DESKTOP_SERVICE_KEY = "DESKTOP_SERVICE_KEY";

   /**
    * Creations of a Desktop object reference.
    * @param name the initial name to asign to the desktop
    * @param user_short_pid storage object short pid representing the owner
    * @return Desktop desktop object reference
    * @exception DesktopException
    */
    public Desktop createDesktop( String name, byte[] user_short_pid ) 
    throws DesktopException;

   /**
    * Creations of a Desktop storage instance.
    * @param name the initial name to asign to the desktop
    * @param user_short_pid storage object short pid representing the owner
    * @return DesktopStorage desktop storage
    * @exception DesktopException
    */
    public DesktopStorage createDesktopStorage( String name, byte[] user_short_pid ) 
    throws DesktopException;

   /**
    * Creation of a Desktop object reference based on a supplied storage object.
    * @param store a desktop storage object
    * @return Desktop a Desktop object reference
    * @exception DesktopException
    */
    public Desktop getDesktopReference( DesktopStorage store ) 
    throws DesktopException;

   /**
    * Returns a reference to a Desktop given a persistent storage object identifier.
    * @param pid dektop short persistent identifier
    * @return Desktop the corresponding PID
    * @exception NotFound if the supplied pid does not matach a know desktop
    */
    public Desktop getDesktopReference( byte[] pid )
    throws NotFound;


}



