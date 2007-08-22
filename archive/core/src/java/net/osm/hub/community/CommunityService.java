/*
 */
package net.osm.hub.community;

import org.omg.CosPersistentState.NotFound;
import org.omg.CommunityFramework.Community;
import org.omg.CommunityFramework.CommunityCriteria;
import net.osm.hub.gateway.FactoryException;
import org.apache.avalon.framework.component.Component;


/**
 * Factory interface through which Community object reference can be created
 * and public communities can be resolved.
 *
 * @author Stephen McConnell <mcconnell@osm.net>
 */

public interface CommunityService extends Component
{

   /**
    * Creation of a new Community object reference bound to the principal user
    * as community administrator.
    * @param name the initial name to assign to the community
    * @return Community a new Community object reference
    */
    public Community createCommunity( String name, CommunityCriteria criteria ) 
    throws FactoryException;

   /**
    * Returns a reference to a Community given a persistent storage object identifier.
    * @param pid Community persistent identifier
    * @return Community corresponding to the PID
    * @exception NotFound if the supplied pid does not matach a know Community
    */
    public Community getCommunityReference( byte[] pid )
    throws NotFound;

}


