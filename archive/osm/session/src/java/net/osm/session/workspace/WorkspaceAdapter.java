package net.osm.session.workspace;

import java.util.Iterator;

import net.osm.session.resource.AbstractResourceAdapter;

/**
 * An adapter providing EJB style access to a <code>Workspace</code>.
 */
public interface WorkspaceAdapter extends AbstractResourceAdapter
{
    /**
     * Creation of a new sub-workspace within this workspace.
     * @return the sub-workspace
     */
    public WorkspaceAdapter createSubWorkspace();

    /**
     * Creation of a new sub-workspace within this workspace.
     * @param  name the name to assign to the sub-workspace
     * @return the sub-workspace
     */
    public WorkspaceAdapter createSubWorkspace(String name);

    /**
     * Add a resource to this workspace.
     * @param  resource the <code>AbstractResourceAdapter</code> to add to the workspace
     */
    public void addResource( AbstractResourceAdapter resource );

    /**
     * Removes a resource from this workspace.
     * @param  resource the <code>AbstractResourceAdapter</code> to remove from the workspace
     */
    public void removeResource( AbstractResourceAdapter resource );

    /**
     * Returns an iterator of resources contained by workspace.
     * @return Iterator of resources contained within the workspace
     */
    public Iterator getContained();

}

