/*
 * @(#)AbstractResourceServer.java
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
import org.omg.CosPersistentState.StorageObject;
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
import org.omg.CommunityFramework.Criteria;
import org.omg.Session.AbstractResource;
import org.omg.Session.AbstractResourceHelper;
import net.osm.hub.gateway.FactoryException;
import net.osm.hub.gateway.FactoryService;
import net.osm.hub.gateway.DomainService;
import net.osm.hub.gateway.RandomService;
import net.osm.hub.gateway.DefaultServer;
import net.osm.pss.PersistanceHandler;
import net.osm.hub.gateway.Registry;
import net.osm.hub.gateway.ServantContext;
import net.osm.hub.resource.StructuredPushSupplierService;
import net.osm.hub.pss.SubscriptionStorageHomeBase;
import net.osm.hub.pss.SubscriptionStorage;
import net.osm.time.TimeService;
import net.osm.time.TimeUtils;
import net.osm.realm.StandardPrincipal;
import net.osm.dpml.DPML;
import net.osm.orb.ORBService;

/**
 * The abstract <code>AbstractResourceServer</code> block declares services supporting the 
 * creation of new <code>AbstractResource</code> references and the handling incomming 
 * resource requests.
 *
 * @author <a href="mailto:mcconnell@osm.net">Stephen McConnell</a>
 */

public abstract class AbstractResourceServer extends DefaultServer 
implements FactoryService, AbstractResourceService
{
    //==================================================
    // state
    //==================================================

    protected DomainService domain;
    protected RandomService random;
    protected TimeService clock;
    protected Registry registry;
    protected StructuredPushSupplierService supplier;
    
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
        super.compose( manager );
	  try
	  {
            if( getLogger().isDebugEnabled() ) getLogger().debug( "composition (AbstractResource)" );
	      super.compose( manager );
		domain = (DomainService)manager.lookup("DOMAIN");
		random = (RandomService)manager.lookup("RANDOM");
            registry = ((Registry)manager.lookup("REGISTRY"));
		clock = (TimeService)manager.lookup("CLOCK");
		supplier = (StructuredPushSupplierService)manager.lookup("STRUCTURED-PUSH-SUPPLIER");

		super.manager.put( "DOMAIN", (DomainService)manager.lookup("DOMAIN"));
		super.manager.put( "RANDOM", (RandomService)manager.lookup("RANDOM"));
            super.manager.put( "REGISTRY", (Registry)manager.lookup("REGISTRY"));
		super.manager.put( "CLOCK", (TimeService)manager.lookup("CLOCK"));
		super.manager.put( "STRUCTURED-PUSH-SUPPLIER", 
		  (StructuredPushSupplierService)manager.lookup("STRUCTURED-PUSH-SUPPLIER"));
        }
        catch( Exception e )
	  {
		final String error = "composition phase failure";
		throw new ComponentException( error, e );
	  }
        if( getLogger().isDebugEnabled() ) getLogger().debug( "composition complete [resource server]" );
    }

    //=======================================================================
    // Initializable
    //=======================================================================
    
    public void initialize()
    throws Exception
    {
        if( getLogger().isDebugEnabled() ) getLogger().debug( "initialization [resource server]" );
	  super.initialize();

        //
	  // register the criteria supported by this home
	  //

	  try
	  {
		Configuration[] array = configuration.getChild("dpml").getChildren();
		for( int i=0; i<array.length; i++ )
	      {
		    try
		    {
		        Criteria cx = DPML.buildCriteriaElement( array[i] );
			  registry.register( cx, this );
		    }
	          catch( Exception e )
		    {
			  if( getLogger().isWarnEnabled() ) getLogger().warn(
			    "ignoring error while internalizing a DPML element");
                }
            }
        }
        catch (Exception e)
        {
		final String error = "dpml block initialization failure";
            if( getLogger().isErrorEnabled() ) getLogger().error( error, e );
            throw new Exception( error, e );
        }
        if( getLogger().isDebugEnabled() ) getLogger().debug( 
		"initialization complete [resource server]" );
    }


    //=======================================================================
    // FactoryService
    //=======================================================================
    
   /**
    * Creation of a new resource.
    * @param name the name of the resource
    * @param value the <code>Criteria</code> defining the resource
    * @exception FactoryException if the criteria is incomplete or unsupported
    */
    public abstract AbstractResource create( String name, Criteria criteria ) 
    throws FactoryException;


    //=======================================================================
    // utilities
    //=======================================================================

   /**
    * Return a reference to an object as an AbstractResource.
    * @param StorageObject storage object
    * @return AbstractResource object reference
    */
    public AbstractResource getAbstractResourceReference( StorageObject store )
    {
        return AbstractResourceHelper.narrow( getPoa().create_reference_with_id( store.get_pid(), AbstractResourceHelper.id() ) );
    }


}
