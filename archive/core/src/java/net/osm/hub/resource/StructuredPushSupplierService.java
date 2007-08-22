/*
 */
package net.osm.hub.resource;

import org.apache.avalon.framework.component.Component;
import org.omg.CosNotifyComm.StructuredPushSupplier;
import net.osm.hub.pss.SubscriberStorage;
import net.osm.hub.pss.AbstractResourceStorage;
import net.osm.hub.gateway.FactoryException;
import org.omg.Session.IdentifiableDomainConsumer;
import net.osm.session.SubscriberProxy;

/**
 * 
 *
 * @author Stephen McConnell <mcconnell@osm.net>
 */

public interface StructuredPushSupplierService extends Component
{

   /**
    * Creation of a new subscriber storage object.
    * @param resource the persistent identifier of a AbstractResourceStorage object
    * @param idc the IdentifiableDomainConsumer that will receive events
    * @return SubscriberStorage storage object for the subscriber proxy
    * @exception FactoryException
    */
    public SubscriberStorage createSubscriberStorage( AbstractResourceStorage resource, 
	IdentifiableDomainConsumer idc ) 
    throws FactoryException;

   /**
    * Creation of a new subscriber proxy object reference.
    * @param subscriber the subscriber persistent storage object
    * @return SubscriberProxy the proxy interface
    * @exception FactoryException if the criteria is incomplete
    */
    public SubscriberProxy createSubscriber( SubscriberStorage subscriber ) 
    throws FactoryException;

   /**
    * Creation of a new structured push supplier object reference.
    * @param subscriber the subscriber persistent storage object
    * @exception FactoryException
    */
    public StructuredPushSupplier createStructuredPushSupplier( SubscriberStorage subscriber ) 
    throws FactoryException;

   /**
    * Creation of a new structured push supplier delegate.
    * @param subscriber the subscriber persistent storage object
    * @exception FactoryException
    */
    public StructuredPushSupplierDelegate createDelegate( SubscriberStorage subscriber ) 
    throws FactoryException;

}



