package net.osm.session.desktop;

import net.osm.session.user.UserAdapter;
import net.osm.session.workspace.WorkspaceAdapter;

/**
 * An adapter providing EJB style access to a <code>Desktop</code>.
 */
public interface DesktopAdapter extends WorkspaceAdapter
{
    /**
     * Returns a <code>UserAdapter</code> that owns the desktop.
     * @return UserAdapter the user that owns the desktop
     */
    public UserAdapter getUser();

}

