/*
 */ 

package net.osm.security.realm;

import java.security.Principal;
import java.util.Arrays;
import java.util.List;
import org.apache.catalina.Realm;


/**
 * Generic implementation of <strong>java.security.Principal</strong> that
 * is available for use by <code>net.osm.security.BasicRealm</code> implementations.
 *
 * @author Stephen McConnell, OSM
 * @version 1.0
 */

final class BasicPrincipal implements Principal 
{

    /**
     * The username of the user represented by this Principal.
     */

    protected String name = null;

    /**
     * The Realm with which this Principal is associated.
     */
    protected Realm realm = null;

    /**
     * Construct a new Principal, associated with the specified Realm, for the
     * specified username and password.
     *
     * @param realm The Realm that owns this Principal
     * @param name The username of the user represented by this Principal
     * @param password Credentials used to authenticate this user
     */
    BasicPrincipal(Realm realm, String name ) {
        this.realm = realm;
        this.name = name;
    }


    /**
     * The username of the user represented by this Principal.
     */

    public String getName() {
        return (this.name);
    }


    /**
     * The Realm with which this Principal is associated.
     */

    Realm getRealm() {
        return (this.realm);
    }

    /**
     * Return a String representation of this object.
     */
    public String toString() {

        StringBuffer sb = new StringBuffer("BasicPrincipal[");
        sb.append(this.name);
        sb.append("]");
        return (sb.toString());
    }
}
