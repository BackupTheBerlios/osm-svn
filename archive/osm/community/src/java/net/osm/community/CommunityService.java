/*
 */
package net.osm.community;

import org.omg.CosPersistentState.StorageObject;
import org.omg.CosPersistentState.NotFound;

import net.osm.session.workspace.WorkspaceService;

/**
 * Factory interface through which a Community reference can be created.
 *
 * @author Stephen McConnell <mcconnell@osm.net>
 */

public interface CommunityService extends WorkspaceService
{

    static final String COMMUNITY_SERVICE_KEY = "COMMUNITY_SERVICE_KEY";

   /**
    * Creations of a Community object reference.
    * @param name the initial name to asign to the community
    * @param user_short_pid storage object short pid representing the owner
    * @return Community object reference
    * @exception CommunityException
    */
    Community createCommunity( String name, byte[] user_short_pid ) 
    throws CommunityException;

   /**
    * Creations of a Community storage instance.
    * @param name the initial name to asign to the community
    * @param user_short_pid storage object short pid representing the owner
    * @return CommunityStorage community storage
    * @exception CommunityException
    */
    CommunityStorage createCommunityStorage( String name, byte[] user_short_pid ) 
    throws CommunityException;

   /**
    * Creation of a Community object reference based on a supplied storage object.
    * @param store a community storage object
    * @return Community a Community object reference
    * @exception CommunityException
    */
    Community getCommunityReference( CommunityStorage store ) 
    throws CommunityException;

   /**
    * Returns a reference to a Community given a persistent storage object identifier.
    * @param pid community short persistent identifier
    * @return Desktop the corresponding PID
    * @exception NotFound if the supplied pid does not matach a know desktop
    */
    Community getCommunityReference( byte[] pid )
    throws NotFound;

}



