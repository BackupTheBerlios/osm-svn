/*
 */
package net.osm.hub.user;

import org.omg.Session.User;
import net.osm.hub.gateway.FactoryException;
import org.omg.CosPersistentState.NotFound;
import org.apache.avalon.framework.component.Component;


/**
 * Factory interface through which a Workspace reference can be created.
 *
 * @author Stephen McConnell <mcconnell@osm.net>
 */

public interface UserService extends Component
{

   /**
    * Creation of a User.
    * @return User a new User object reference
    */
    public User createUser( ) 
    throws FactoryException;

   /**
    * Locates a User instance relative to the current principal identity.
    * @param name the initial name to assign to the user
    * @return User a new User object reference
    */
    public User locateUser( ) 
    throws NotFound;

   /**
    * Returns a reference to a User given a persistent storage object identifier.
    * @param pid user persistent identifier
    * @return User the corresponding Uer reference
    * @exception NotFound if the supplied pid does not match a known user
    */
    public User getUserReference( byte[] pid )
    throws NotFound;

   /**
    * Returns a reference to a User given a short user persistent storage object identifier.
    * @param pid short user persistent identifier
    * @return User the corresponding Uer reference
    * @exception NotFound if the supplied pid does not match a known user
    */
    public User getShortUserReference( byte[] pid )
    throws NotFound;

}


