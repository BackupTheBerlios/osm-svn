/*
 * @(#)StructuredPushSupplierServantManager.java
 *
 * Copyright 2000 OSM SARL. All Rights Reserved.
 * 
 * This software is the proprietary information of OSM SARL.  
 * Use is subject to license terms.
 * 
 * @author  Stephen McConnell
 * @version 1.0 26/03/2001
 */

package net.osm.sps;

import org.apache.avalon.framework.logger.Logger;
import org.apache.avalon.framework.logger.LogEnabled;
import org.apache.avalon.framework.context.Context;
import org.apache.avalon.framework.context.Contextualizable;
import org.apache.avalon.framework.context.ContextException;
import org.apache.avalon.framework.component.Component;
import org.apache.avalon.framework.component.ComponentManager;
import org.apache.avalon.framework.activity.Disposable;
import org.apache.avalon.framework.activity.Initializable;
import org.apache.avalon.framework.activity.Startable;
import org.apache.avalon.framework.logger.LogEnabled;
import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.Serviceable;
import org.apache.avalon.phoenix.BlockContext;
import org.apache.avalon.phoenix.Block;

import org.apache.orb.ORBContext;
import org.apache.pss.Connector;
import org.apache.pss.Session;
import org.apache.pss.DefaultServantManager;
import org.apache.pss.StorageContext;

import org.omg.CORBA_2_3.ORB;
import org.omg.CORBA.Any;
import org.omg.CORBA.LocalObject;
import org.omg.CORBA.Policy;
import org.omg.CosPersistentState.NotFound;
import org.omg.CosNotifyComm.StructuredPushSupplier;
import org.omg.CosNotifyComm.StructuredPushSupplierPOATie;
import org.omg.CosNotifyComm.StructuredPushSupplierHelper;
import org.omg.CosNotifyComm.StructuredPushConsumer;
import org.omg.CosNotification.EventType;
import org.omg.PortableServer.ServantLocator;
import org.omg.PortableServer.Servant;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.ServantLocatorPackage.CookieHolder ;
import org.omg.PortableServer.ForwardRequest;
import org.omg.PortableServer.POAHelper;
import org.omg.PortableServer.IdAssignmentPolicyValue;
import org.omg.PortableServer.RequestProcessingPolicyValue;
import org.omg.PortableServer.ImplicitActivationPolicyValue;
import org.omg.PortableServer.LifespanPolicyValue;
import org.omg.PortableServer.IdUniquenessPolicyValue;
import org.omg.PortableServer.ServantRetentionPolicyValue;
import org.omg.CosPersistentState.StorageObject;

import net.osm.event.EventStorageHome;

/**
 * The <code>StructuredPushSupplierServantManager</code> provides services supporting the 
 * creation of new <code>StructuredPushSupplier</code> instances that handle event 
 * propergation to registered consumers.
 *
 * @author <a href="mailto:mcconnell@osm.net">Stephen McConnell</a>
 */

public class StructuredPushSupplierServantManager extends DefaultServantManager 
implements StructuredPushSupplierService
{

    //=======================================================================
    // state
    //=======================================================================

    private SubscriberStorageHome m_home;
    private EventStorageHome m_event_home;
    private POA m_poa;
    
    //=======================================================================
    // Initializable
    //=======================================================================
    
    public void initialize()
    throws Exception
    {
	  super.initialize();
	  try
	  {
            if( getLogger().isDebugEnabled() ) getLogger().debug( "initialization (sps)" );
            m_home = (SubscriberStorageHome) super.getSession().find_storage_home( 
	        "PSDL:osm.net/sps/SubscriberStorageHomeBase:1.0" );
            m_event_home = (EventStorageHome) super.getSession().find_storage_home( 
	        "PSDL:osm.net/event/EventStorageHomeBase:1.0" );
	  }
	  catch( Throwable throwable )
	  {
	      String error = "unable to complete supplier initialization due to a unexpected error";
		throw new StructuredPushSupplierException( error, throwable );
	  }
    }

    //=======================================================================
    // Disposable
    //=======================================================================

    public void dispose() 
    {
        getLogger().debug("dispose");
        try
        {
            m_poa.destroy( true, true );
        }
        catch( Throwable e )
        {
            if( getLogger().isWarnEnabled() ) getLogger().warn( "ignoring POA related exception" );
        }
        getLogger().debug("dispose complete");
    }

    //=======================================================================
    // implementation
    //=======================================================================

   /**
    * Returns the storage home for EventStorage types.
    * @return the event storage home
    */
    protected EventStorageHome getEventHome()
    {
	  return m_event_home;
    }


   /**
    * Method by which derived types can override POA creation.
    * @return POA the portable object adapter
    */
    protected POA createPOA() throws Exception
    {
        m_poa = super.createPOA();
        return m_poa;
    }

   /**
    * Retuns the name to be assigned to the POA on creation.  This is an 
    * abstract method that must be overriden by derived types.
    * @return String the POA name
    */
    protected String getPoaName()
    {
        return "SPS";
    }


   /**
    * Returns a servant implementation.
    * @return Servant a managable servant
    */
    protected Servant createServant( 
       StorageContext context, Configuration config, 
       ServiceManager manager ) throws Exception
    {
        StructuredPushSupplierDelegate delegate = new StructuredPushSupplierDelegate();
        Servant servant = new StructuredPushSupplierPOATie( delegate );
        Logger log = getLogger().getChildLogger( "" + System.identityHashCode( delegate ) );
        LifecycleHelper.pipeline( delegate, log, context, config, manager );
        return servant;
    }

  
    //=======================================================================
    // StructuredPushSupplierService
    //=======================================================================
    
   /**
    * Creation of a new subscriber proxy object reference.
    * @param subscriber the subscriber persistent storage object
    * @return SubscriberProxy the proxy interface
    * @exception StructuredPushSupplierException if the criteria is incomplete
    */
    public SubscriberProxy createSubscriber( SubscriberStorage subscriber ) 
    throws StructuredPushSupplierException
    {
        if( getLogger().isDebugEnabled() ) getLogger().debug( "create proxy" );
	  if( subscriber == null ) throw new NullPointerException(
		"Cannot create proxy reference from a null subscriber argument");

        try
        {
            return SubscriberProxyHelper.narrow(
                m_poa.create_reference_with_id(
                    subscriber.get_pid(), SubscriberProxyHelper.id() ));
        }
        catch (Throwable e)
        {
	      String error = "failed to create subscriber proxy object reference";
            throw new StructuredPushSupplierException( error, e );
        }
    }

   /**
    * Creation of a new structured push supplier object reference.
    * @param subscriber the subscriber persistent storage object
    * @exception FactoryException if the criteria is incomplete
    */
    public StructuredPushSupplier createStructuredPushSupplier( SubscriberStorage subscriber ) 
    throws StructuredPushSupplierException
    {
        if( getLogger().isDebugEnabled() ) getLogger().debug( 
		"create StructuredPushSupplier" );
	  if( !initialized() ) throw new IllegalStateException("server has not been initialized");
	  if( disposed() ) throw new IllegalStateException("server is in disposed state");
	  if( subscriber == null ) throw new NullPointerException(
		"Cannot create supplier reference from a null subscriber argument");

        try
        {
            return StructuredPushSupplierHelper.narrow(
                m_poa.create_reference_with_id(
                    subscriber.get_pid(), StructuredPushSupplierHelper.id() ));
        }
        catch (Throwable e)
        {
	      String error = "failed to create structured push supplier object reference";
            throw new StructuredPushSupplierException( error, e );
        }
    }
}
