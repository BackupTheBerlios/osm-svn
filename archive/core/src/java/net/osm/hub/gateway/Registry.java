/*
 */
package net.osm.hub.gateway;

import org.apache.avalon.framework.component.Component;

import org.omg.CORBA.ORB;
import org.omg.CommunityFramework.Criteria;

/**
 * Service the supports the registration of of factories enabling 
 * aggregation of a set of factory abilities based on supplied 
 * criteria.
 *
 * @author Stephen McConnell <mcconnell@osm.net>
 */

public interface Registry extends Component
{

   /**
    * Register a criteria and the supporting factory.
    * @param criteria the <code>Criteria</code> supported by a server
    * @param source the factory supporting resource creation for the criteria
    *   identified by the label
    */
    public void register( Criteria criteria, FactoryService source );

}



