/*
 */
package net.osm.session.user;

import net.osm.session.resource.AbstractResourceService;

import org.omg.CosPersistentState.NotFound;

import net.osm.realm.StandardPrincipal;

/**
 * Factory interface through which a User reference can be created.
 *
 * @author Stephen McConnell <mcconnell@osm.net>
 */

public interface UserService extends AbstractResourceService
{

    public static final String USER_SERVICE_KEY = "USER_SERVICE_KEY";


   /**
    * Creation of a User.
    * @return User a new User object reference
    */
    public User createUser( ) 
    throws UserException;

   /**
    * Creation of a User based on a supplied principal.
    * @param name the initial name to assign to the user
    * @return UserStorage a new User object reference
    */
    public UserStorage createUserStorage( StandardPrincipal principal ) 
    throws UserException;

   /**
    * Creation of a User based on a supplied principal.
    * @param name the initial name to assign to the user
    * @param path encoded principal
    * @return UserStorage a new storage object
    */
    public UserStorage createUserStorage( String name, byte[] path ) 
    throws UserException;

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
    * @return User the corresponding User reference
    * @exception NotFound if the supplied pid does not match a known user
    */
    public User getUserReference( byte[] pid )
    throws NotFound;

   /**
    * Returns a reference to a User given a persistent storage object.
    * @param store user storage object
    * @return User the corresponding User reference
    */
    public User getUserReference( UserStorage store );

   /**
    * Returns a reference to a User given a short user persistent storage object identifier.
    * @param pid short user persistent identifier
    * @return User the corresponding Uer reference
    * @exception NotFound if the supplied pid does not match a known user
    */
    public User getShortUserReference( byte[] pid )
    throws NotFound;

}


