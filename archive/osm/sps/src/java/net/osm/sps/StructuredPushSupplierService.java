/*
 * Copyright 2002 OSM SARL. All Rights Reserved.
 * 
 * This software is the proprietary information of OSM SARL.  
 * Use is subject to license terms.
 * 
 * @author  Stephen McConnell
 * @version 1.0 03/03/2002
 */
package net.osm.sps;

import org.apache.avalon.framework.component.Component;
import org.apache.avalon.framework.logger.Logger;
import org.omg.CosNotifyComm.StructuredPushSupplier;
import org.omg.CosNotifyComm.StructuredPushConsumer;
import org.omg.CosPersistentState.StorageObject;

/**
 * Defines the set of operations supporting <code>SubscriberProxy</code>
 * and <code>StructuredPushSupplier</code> creation.
 * @author Stephen McConnell <mcconnell@osm.net>
 */

public interface StructuredPushSupplierService extends Component
{

    public static final String SERVICE_KEY = "sps_service_key";

   /**
    * Creation of a new subscriber proxy object reference.
    * @param subscriber the subscriber persistent storage object
    * @return SubscriberProxy the proxy interface
    * @exception FactoryException if the criteria is incomplete
    */
    public SubscriberProxy createSubscriber( SubscriberStorage subscriber ) 
    throws StructuredPushSupplierException;

   /**
    * Creation of a new structured push supplier object reference.
    * @param subscriber the subscriber persistent storage object
    * @exception FactoryException
    */
    public StructuredPushSupplier createStructuredPushSupplier( SubscriberStorage subscriber ) 
    throws StructuredPushSupplierException;


}



