/*
 */
package net.osm.hub.workspace;

import org.omg.Session.Workspace;
import net.osm.hub.gateway.FactoryException;
import org.omg.CosPersistentState.NotFound;
import org.apache.avalon.framework.component.Component;

/**
 * Factory interface through which a Workspace reference can be created.
 *
 * @author Stephen McConnell <mcconnell@osm.net>
 */

public interface WorkspaceService extends Component
{

   /**
    * Creation of a Workspace.
    * @param name the initial name to assign to the workspace
    * @return Workspace a new Workspace object reference
    */
    public Workspace createWorkspace( String name ) 
    throws FactoryException;

   /**
    * Returns a reference to a Workspace given a persistent storage object identifier.
    * @param pid Workspace persistent identifier
    * @return Workspace corresponding to the PID
    * @exception NotFound if the supplied pid does not matach a know workspace
    */
    public Workspace getWorkspaceReference( byte[] pid )
    throws NotFound;


}



