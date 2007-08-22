/*
 * @(#)StructuredPushSupplierServer.java
 *
 * Copyright 2000 OSM SARL. All Rights Reserved.
 * 
 * This software is the proprietary information of OSM SARL.  
 * Use is subject to license terms.
 * 
 * @author  Stephen McConnell
 * @version 1.0 26/03/2001
 */

package net.osm.hub.resource;

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
import org.apache.avalon.framework.component.ComponentManager;
import org.apache.avalon.framework.component.ComponentException;
import org.apache.avalon.framework.component.Composable;
import org.apache.avalon.phoenix.BlockContext;
import org.apache.avalon.phoenix.Block;

import org.omg.CORBA_2_3.ORB;
import org.omg.CORBA.Any;
import org.omg.CORBA.LocalObject;
import org.omg.CORBA.Policy;
import org.omg.CosPersistentState.NotFound;
import org.omg.CosPersistentState.Connector;
import org.omg.CosPersistentState.Session;
import org.omg.CosNotifyComm.StructuredPushSupplier;
import org.omg.CosNotifyComm.StructuredPushSupplierPOATie;
import org.omg.CosNotifyComm.StructuredPushSupplierHelper;
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
import org.omg.CosNotification.EventType;
import org.omg.Session.IdentifiableDomainConsumer;

import net.osm.hub.gateway.FactoryException;
import net.osm.hub.gateway.ServantContext;
import net.osm.hub.gateway.FactoryService;
import net.osm.hub.gateway.DomainService;
import net.osm.hub.gateway.RandomService;
import net.osm.hub.gateway.DefaultServer;
import net.osm.hub.pss.AbstractResourceStorage;
import net.osm.hub.pss.SubscriberStorageHomeBase;
import net.osm.hub.pss.SubscriberStorageHome;
import net.osm.hub.pss.SubscriberStorage;
import net.osm.orb.ORBService;
import net.osm.pss.PersistanceHandler;
import net.osm.pss.PSSConnectorService;
import net.osm.pss.PSSSessionService;
import net.osm.realm.StandardPrincipal;
import net.osm.session.SubscriberProxy;
import net.osm.session.SubscriberProxyPOATie;
import net.osm.session.SubscriberProxyHelper;


/**
 * The <code>StructuredPushSupplierServer</code> block provides services supporting the 
 * creation of new <code>StructuredPushSupplier</code> instances that handle event 
 * propergation to registered consumers.
 *
 * @author <a href="mailto:mcconnell@osm.net">Stephen McConnell</a>
 */

public class StructuredPushSupplierServer extends DefaultServer 
implements StructuredPushSupplierService
{

    //=======================================================================
    // state
    //=======================================================================

    private SubscriberStorageHome home;
    private DomainService domain;
    private RandomService random;
    
    //=================================================================
    // Composable
    //=================================================================
    
    /**
     * Pass the <code>ComponentManager</code> to the <code>Composable</code>.
     * The <code>Composable</code> implementation should use the specified
     * <code>ComponentManager</code> to acquire the components it needs for
     * execution.
     *
     * @param manager The <code>ComponentManager</code> which this
     *                <code>Composer</code> uses.
     */
    public void compose( ComponentManager manager )
    throws ComponentException
    {
        if( getLogger().isDebugEnabled() ) getLogger().debug( 
	    "supplied composition" );

	  super.compose( manager );
	  try
	  {
	      super.compose( manager );
		domain = (DomainService)manager.lookup("DOMAIN");
		random = (RandomService)manager.lookup("RANDOM");
		super.manager.put( "DOMAIN", domain );
	      super.manager.put( "RANDOM", random );
        }
        catch( Exception e )
	  {
		final String error = "composition phase failure";
		throw new ComponentException( error, e );
	  }
        if( getLogger().isDebugEnabled() ) getLogger().debug( "composition complete" );
    }

    //=======================================================================
    // Initializable
    //=======================================================================
    
    public void initialize()
    throws Exception
    {
	  try
	  {
            if( getLogger().isDebugEnabled() ) getLogger().debug( 
		  "supplier initialization" );
	      super.initialize();
            Configuration persistence = configuration.getChild("persistence");
	      PersistanceHandler.register( connector, persistence );
            home = (SubscriberStorageHome) session.find_storage_home( 
	        "PSDL:osm.net/hub/pss/SubscriberStorageHomeBase:1.0" );
	  }
	  catch( Throwable throwable )
	  {
	      String error = "unable to complete supplier initialization due to a unexpected error";
		if( getLogger().isErrorEnabled() ) getLogger().error( error, throwable );
		throw new Exception( error, throwable );
	  }
    }
  
    //=======================================================================
    // StructuredPushSupplierService
    //=======================================================================
    
   /**
    * Notification to the manager to release a supplier reference.
    * @param subscription the subscription persistent storage object
    */
    public void releaseSupplier( SubscriberStorage subscriber )
    {
        super.remove( subscriber.get_pid() );
    }

   /**
    * Creation of a new subscriber storage object.
    * @param resource the persistent identifier of a AbstractResourceStorage object
    * @param idc the IdentifiableDomainConsumer that will receive events
    * @return SubscriberStorage storage object for the subscriber proxy
    * @exception FactoryException
    */
    public SubscriberStorage createSubscriberStorage( AbstractResourceStorage resource, 
	IdentifiableDomainConsumer idc )
    throws FactoryException
    {
	  if( resource == null ) throw new NullPointerException(
	    "Null resource argument supplied to subscriber storage factory.");
	  if( idc == null ) throw new NullPointerException(
	    "Null consumer argument supplied to subscriber storage factory.");

        return home.create( resource.get_pid(), idc, new EventType[0], false );
    }

   /**
    * Creation of a new subscriber proxy object reference.
    * @param subscriber the subscriber persistent storage object
    * @return SubscriberProxy the proxy interface
    * @exception FactoryException if the criteria is incomplete
    */
    public SubscriberProxy createSubscriber( SubscriberStorage subscriber ) 
    throws FactoryException
    {
        if( getLogger().isDebugEnabled() ) getLogger().debug( "create proxy" );
	  if( subscriber == null ) throw new NullPointerException(
		"Cannot create proxy reference from a null subscriber argument");

        try
        {
            return SubscriberProxyHelper.narrow(
                poa.create_reference_with_id(
                    subscriber.get_pid(), SubscriberProxyHelper.id() ));
        }
        catch (Throwable e)
        {
	      String error = "failed to create subscriber proxy object reference";
            if( getLogger().isErrorEnabled() ) getLogger().error( error, e );
            throw new FactoryException( error, e );
        }
    }

   /**
    * Creation of a new structured push supplier object reference.
    * @param subscriber the subscriber persistent storage object
    * @exception FactoryException if the criteria is incomplete
    */
    public StructuredPushSupplier createStructuredPushSupplier( SubscriberStorage subscriber ) 
    throws FactoryException
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
                poa.create_reference_with_id(
                    subscriber.get_pid(), StructuredPushSupplierHelper.id() ));
        }
        catch (Throwable e)
        {
	      String error = "failed to create structured push supplier object reference";
            if( getLogger().isErrorEnabled() ) getLogger().error("[BBO] " + error, e );
            throw new FactoryException( error, e );
        }
    }

   /**
    * Creation of a new structured push supplier delegate.
    * @param subscriber the subscriber persistent storage object
    * @exception FactoryException
    */
    public StructuredPushSupplierDelegate createDelegate( SubscriberStorage subscriber ) 
    throws FactoryException
    {
	  if( subscriber == null ) throw new NullPointerException(
		"Cannot create delegate from a null subscriber argument");

	  try
	  {
            return (StructuredPushSupplierDelegate) super.processLifecycle( 
		  new StructuredPushSupplierDelegate(), subscriber.get_pid() );
	  }
	  catch( Throwable e )
	  {
		final String error = "unable to create a structured push supplier delegate";
		throw new FactoryException( error, e );
	  }
    }


   /**
    * Creation of a servant based on a supplied delegate.
    * @param oid the persistent object identifier
    * @return Servant the servant
    */
    public Servant createServant( byte[] oid )
    {
        return new SubscriberProxyPOATie( 
          (StructuredPushSupplierDelegate) super.processLifecycle( 
		new StructuredPushSupplierDelegate(), oid ), poa );
    }
}
