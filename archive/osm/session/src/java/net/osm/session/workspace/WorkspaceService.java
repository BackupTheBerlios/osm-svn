/*
 */
package net.osm.session.workspace;

import net.osm.session.resource.AbstractResourceService;

/**
 * Factory interface through which a Workspace reference can be created.
 *
 * @author Stephen McConnell <mcconnell@osm.net>
 */

public interface WorkspaceService extends AbstractResourceService
{

    public static final String WORKSPACE_SERVICE_KEY = "WORKSPACE_SERVICE_KEY";

   /**
    * Return a reference to an object as an Workspace.
    * @param StorageObject storage object
    * @return Workspace object reference to the workspace
    */
    public Workspace getWorkspaceReference( WorkspaceStorage store );

   /**
    * Creation of a Workspace.
    * @param name the initial name to assign to the workspace
    * @return Workspace a new Workspace object reference
    */
    public Workspace createWorkspace( String name ) 
    throws WorkspaceException;

   /**
    * Creation of a new <code>StorageObject</code> representing the 
    * state of a new <code>AbstractResource</code> instance.
    * @param name the name to apply to the new resource
    * @return StorageObject storage object encapsulating the resource state
    */
    public WorkspaceStorage createWorkspaceStorage( String name ) throws WorkspaceException;

}



