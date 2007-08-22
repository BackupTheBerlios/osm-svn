/*
 */
package net.osm.hub.gateway;

import org.apache.avalon.framework.component.Component;

import org.omg.CORBA.ORB;
import org.omg.CommunityFramework.Criteria;
import org.omg.Session.AbstractResource;

/**
 * Service the provides access to a <code>AbstractResource</code>
 * factory.
 *
 * @author Stephen McConnell <mcconnell@osm.net>
 */

public interface FactoryService extends Component
{

   /**
    * Creation of a new AbstractResource based on supplied creation Criteria.
    * @param name the name of the resource to be created
    * @param criteria the <code>Criteria</code> defining creation constraints and/or parameters
    * @exception FactoryException if the criteria is incomplete or unsupported
    */
    public AbstractResource create( String name, Criteria criteria ) 
    throws FactoryException;
}



