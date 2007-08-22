/*
 */
package net.osm.hub.community;

import org.omg.CosPersistentState.StorageObject;
import org.omg.CommunityFramework.Community;
import org.omg.CommunityFramework.CommunityCriteria;
import net.osm.hub.gateway.FactoryException;
import net.osm.hub.pss.CommunityStorage;
import net.osm.hub.pss.UserStorage;
import net.osm.realm.StandardPrincipal;


/**
 * Factory interface through which Community object reference can be created
 * and public communities can be resolved.
 *
 * @author Stephen McConnell <mcconnell@osm.net>
 */

public interface CommunityAdministratorService
{

   /**
    * Creation of a new Community object reference bound to a supplied administrator.
    * @param name the initial name to assign to the community
    * @param criteria community creation DPML criteria
    * @param administrator the storage object representing the administrator
    * @param principal the principal to assign a community storage owner
    * @return CommunityStorage a new Community storage object
    */
    public CommunityStorage createCommunityStorage( String name, CommunityCriteria criteria, 
	UserStorage administrator, StandardPrincipal principal ) 
    throws FactoryException;

   /**
    * Returns a reference to a Community given a persistent storage object.
    * @param pid Community  persistent identifier
    * @return Community  corresponding to the PID
    * @exception NotFound if the supplied pid does not matach a know Community 
    */
    public Community getCommunityReference( StorageObject store );

}


