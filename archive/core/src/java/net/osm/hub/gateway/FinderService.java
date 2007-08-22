/*
 */
package net.osm.hub.gateway;

import org.apache.avalon.framework.component.Component;
import org.omg.CommunityFramework.ResourceFactoryProblem;
import org.omg.Session.AbstractResource;
import net.osm.hub.home.UnknownName;


/**
 * Bootstrap service that establishes the root community public access point.
 *
 * @author Stephen McConnell <mcconnell@osm.net>
 */

public interface FinderService extends Component
{

   /**
    * Returns the root bootstrap community.
    * @return Community the root bootstrap community
    * @exception Exception
    */
    public AbstractResource lookup( String name ) 
    throws UnknownName, ResourceFactoryProblem;

}



