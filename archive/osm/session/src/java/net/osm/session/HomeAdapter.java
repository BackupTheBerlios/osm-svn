package net.osm.session;

import org.omg.CORBA.portable.ValueBase;
import net.osm.chooser.ChooserAdapter;
import net.osm.finder.FinderAdapter;

import net.osm.session.user.PrincipalAdapter;

/**
 * Client-side support for interaction with the session framework.
 */
public interface HomeAdapter extends ValueBase, ChooserAdapter, FinderAdapter
{
    /**
     * Returns a user relative to the undelying principal.
     * @param  policy TRUE if a new should be created if the principal is unknown
     * otherwise, the UnknownPrincipal exception will be thrown if the principal
     * cannot be resolved to a user reference
     * @return  UserAdapter an adapter wrapping a user object reference
     * @exception  UnknownPrincipal if the underlying principal does not
     * match a registered user.
     */
     PrincipalAdapter resolve_user(boolean policy) throws UnknownPrincipal;

}

