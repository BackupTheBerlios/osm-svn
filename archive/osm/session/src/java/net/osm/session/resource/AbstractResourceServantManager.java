/*
 * Copyright 2000 OSM SARL. All Rights Reserved.
 * 
 * This software is the proprietary information of OSM SARL.  
 * Use is subject to license terms.
 * 
 * @author  Stephen McConnell
 * @version 1.0 26/03/2001
 */

package net.osm.session.resource;

import java.util.Random;

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
import org.apache.avalon.framework.service.DefaultServiceManager;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.Serviceable;

import org.omg.CORBA.Any;
import org.omg.CosTime.TimeService;
import org.apache.orb.ORB;
import org.apache.orb.ORBContext;
import org.apache.orb.util.LifecycleHelper;
import org.apache.pss.Connector;
import org.apache.pss.Session;
import org.apache.pss.ConnectorContext;
import org.apache.pss.StorageContext;
import org.apache.time.TimeUtils;

import org.omg.CORBA.Any;
import org.omg.CORBA.LocalObject;
import org.omg.CORBA.Policy;
import org.omg.CORBA.TypeCode;
import org.omg.CosPersistentState.NotFound;
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
import org.omg.CosPersistentState.NotFound;
import org.omg.CosPersistentState.StorageHomeBase;
import org.omg.CosPropertyService.PropertySet;
import org.omg.CosPropertyService.PropertySetDef;
import org.omg.CosPropertyService.PropertySetDefHelper;
import org.omg.CosPropertyService.PropertySetDefPOATie;
import org.omg.CosPropertyService.PropertyModeType ;
import org.omg.CosPropertyService.PropertyDef;
import org.omg.CosPropertyService.PropertyHolder;
import org.omg.CosPropertyService.PropertiesHolder;
import org.omg.CosPropertyService.PropertiesIteratorHolder;

import net.osm.domain.DomainService;
import net.osm.sps.StructuredPushSupplierService;
import net.osm.realm.StandardPrincipal;
import net.osm.realm.PrincipalManagerHelper;
import net.osm.realm.PrincipalManager;
import net.osm.session.ProducedBy;
import net.osm.session.linkage.LinkStorageHome;

/**
 * <p>The <code>AbstractResourceServantManager</code> provides services supporting the 
 * creation of new <code>AbstractResource</code> instances and a servant activation
 * model for handling the redirection of incomming requests.</p>
 *
 * @author <a href="mailto:mcconnell@osm.net">Stephen McConnell</a>
 */
public abstract class AbstractResourceServantManager extends DefaultServantManager 
implements AbstractResourceService, Serviceable, Initializable, Disposable
{

    //=======================================================================
    // state
    //=======================================================================

    private ServiceManager m_manager;
    private AbstractResourceStorageHome m_home;
    private LinkStorageHome m_link_home;
    private POA m_poa;
    private TimeService m_clock;
    private DomainService m_domain_service;
    private StructuredPushSupplierService m_sps_service;
    private PrincipalManager m_principal_manager;
    private ORB m_orb;

   /**
    * Random seed against which constant random identifiers are generated.
    */
    private final Random m_seed = new Random();

    //=======================================================================
    // Serviceable
    //=======================================================================

    /**
     * Pass the <code>ServiceManager</code> to the <code>Serviceable</code>.
     * The <code>Serviceable</code> implementation uses the supplied
     * <code>ServiceManager</code> to acquire the components it needs for
     * execution.
     * @param manager the <code>ServiceManager</code> for this delegate
     * @exception ServiceException
     */
    public void service( ServiceManager manager )
    throws ServiceException
    {
        super.service( manager );

        m_manager = manager;

	  try
	  {
	      m_orb = (ORB) manager.lookup( ORBContext.ORB_KEY );
            m_clock = (TimeService) manager.lookup("time");
            m_domain_service = (DomainService) manager.lookup("domain");
            m_sps_service = (StructuredPushSupplierService) manager.lookup( 
              StructuredPushSupplierService.SERVICE_KEY );
	  }
	  catch( Exception e )
	  {
		final String error = "failed to resolve connection context";
		throw new ServiceException( error, e);
	  }
    }


    //=======================================================================
    // Initializable
    //=======================================================================
    
   /**
    * Initialization of the manager during which the container storage home is 
    * resolved following supertype initialization.
    * @exception Exception if an error occurs during initalization
    */
    public void initialize()
    throws Exception
    {
	  super.initialize();
	  try
	  {
            m_home = (AbstractResourceStorageHome) super.getSession().find_storage_home( 
	        "PSDL:osm.net/session/resource/AbstractResourceStorageHomeBase:1.0" );
            m_principal_manager = PrincipalManagerHelper.narrow( 
              m_orb.resolve_initial_references( "PrincipalManager" ) );
	  }
	  catch( Throwable throwable )
	  {
	      String error = "unable to complete supplier initialization due to a unexpected error";
		throw new AbstractResourceException( error, throwable );
	  }
    }

    //=======================================================================
    // Disposable
    //=======================================================================

   /**
    * Disposal of the servant manager and cleanup of state members.
    */       
    public void dispose() 
    {
        m_home = null;
        m_link_home = null;
        m_poa = null;
        m_manager.release( m_clock );
        m_manager.release( m_domain_service );
        m_manager.release( m_sps_service );
        m_principal_manager = null;
        m_orb = null;

        super.dispose();
    }

    //=======================================================================
    // DefaultServantManager
    //=======================================================================

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
    * Returns the name to be assigned to the POA on creation.
    * @return String the POA name
    */
    protected String getPoaName()
    {
        return "ABSTRACT_RESOURCE_MANAGER";
    }

    protected ServiceManager getServiceManager( ServiceManager parent )
    {
        ServiceManager super_manager = super.getServiceManager( parent );
        DefaultServiceManager manager = new DefaultServiceManager( super_manager );
        manager.put( "poa", m_poa );
        manager.put( "domain", m_domain_service );
        manager.put( StructuredPushSupplierService.SERVICE_KEY, m_sps_service );
        manager.makeReadOnly();
        return manager;
    }

   /**
    * Returns a servant implementation.
    * @return Servant a managable servant
    */
    protected Servant createServant( 
      StorageContext context, Configuration config, ServiceManager manager 
    ) throws Exception
    {
        AbstractResourceDelegate delegate = new AbstractResourceDelegate();
        Servant servant = new AbstractResourcePOATie( delegate );
        Logger log = getLogger().getChildLogger( "" + System.identityHashCode( delegate ) );
        LifecycleHelper.pipeline( delegate, log, context, config, manager );
        return servant;
    }

    //=======================================================================
    // Manager
    //=======================================================================

   /**
    * Returns an object reference to the server.
    * @return the object reference
    */
    public org.omg.CORBA.Object getReference()
    {
       try
       {
           return m_poa.create_reference_with_id( 
               getPoaName().getBytes(), org.apache.orb.corbaloc.ChooserHelper.id() );
           //return ManagerHelper.narrow( 
           //  m_poa.create_reference_with_id( 
           //    getPoaName().getBytes(), ManagerHelper.id() ));
       }
       catch( Throwable e )
       {
           // should not happen
           throw new AbstractResourceRuntimeException(
             "Unexpected failure while resolving manager object reference.", e );
       }
    }

    //=======================================================================
    // corbaloc::Chooser
    //=======================================================================

    /**
     * Process a query based on a URL ref syntax. A url of the 
     * form corbaloc::home.osm.net/gateway#12345A will have 
     * a ref element corresponding to <code>12345A</code>.
     * 
     * @param  ref the ref string
     * @return  the result of the reference selection
     * @exception  UnknownReference if the supplied ref cannot be resolved
     * @exception  ServiceRedirection if the ref resolution is being redirected
     */
    public org.omg.CORBA.Any select( String ref )
        throws org.apache.orb.corbaloc.UnknownReference, org.apache.orb.corbaloc.InvalidReference, org.apache.orb.corbaloc.ServiceRedirection
    {
        getLogger().debug("select: " + ref );
        int id;

        try
        {
            id = Integer.parseInt( ref );
        }
        catch( Throwable e )
        {
            final String error = e.toString();
            throw new org.apache.orb.corbaloc.InvalidReference( ref, e.toString() ); 
        }

        try
        {
            AbstractResource r = resolve( id );
            Any any = m_orb.create_any();
            AbstractResourceHelper.insert( any, r );
            return any;
        }
        catch( NotFound e )
        {
            throw new org.apache.orb.corbaloc.UnknownReference( ref ); 
        }
        catch( Throwable e )
        {
            throw new org.omg.CORBA.INTERNAL( e.getMessage() ); 
        }
    }

    //=======================================================================
    // AbstractResourceService
    //=======================================================================
    
   /**
    * Returns an object reference matching a supplied identifier.
    * @param int identifiable domain object identifier
    * @return AbstractResource object reference
    */
    public AbstractResource resolve( int identifier ) throws NotFound
    {
        return getObjectReference( 
           ((AbstractResourceStorageHome)getStorageHome()).find_by_identifier( identifier ) );
    }

   /**
    * Returns the storage home managed by the manager.
    * @return StorageHomeBase the storage home
    */
    protected abstract StorageHomeBase getStorageHome();
    
   /**
    * Returns an object reference for a storage object.
    * @param store the storage object
    */
    protected abstract AbstractResource getObjectReference( StorageObject store );
    
   /**
    * Returns a new AbstractResource instance.
    * @param name the name of the resource to be created
    * @return AbstractResource object reference to the new resource 
    */
    public AbstractResource createAbstractResource( String name )
    throws AbstractResourceException
    {
        StorageObject store = createAbstractResourceStorage( name );
        return getAbstractResourceReference( store );
    }

   /**
    * Creation of a new <code>StorageObject</code> representing the 
    * state of a new <code>AbstractResource</code> instance.
    * @param name the name to apply to the new resource
    * @return StorageObject storage object encapsulating the resource state
    */
    public AbstractResourceStorage createAbstractResourceStorage( String name )
    throws AbstractResourceException
    {
        return createAbstractResourceStorage( m_home, name );
    }

   /**
    * Creation of a new <code>StorageObject</code> representing the 
    * state of a new <code>AbstractResource</code> instance.
    * @param name the name to apply to the new resource
    * @return StorageObject storage object encapsulating the resource state
    */
    public AbstractResourceStorage createAbstractResourceStorage( 
       AbstractResourceStorageHome home, String name )
    throws AbstractResourceException
    {
        try
        {
            final long t = TimeUtils.resolveTime( m_clock );
            AbstractResourceStorage store = home.create(
              getPrincipal(), 
              getDefaultDomain(), 
              getRandom(), 
              t, 
              t, 
              t, 
              name, 
              new net.osm.session.ProducedBy() );
            store.publisher().subscribers( new byte[][]{} );
            return store;
        }
        catch( Throwable e )
        {
            final String error = "Unexpected problem while creating resource storage.";
            if( getLogger().isErrorEnabled() ) getLogger().error( error, e );
            throw new AbstractResourceException( error, e );
        }
    }

   /**
    * Return a reference to an object as an AbstractResource.
    * @param StorageObject storage object
    * @return AbstractResource object reference
    */
    public AbstractResource getAbstractResourceReference( StorageObject store )
    {
        return AbstractResourceHelper.narrow( 
          m_poa.create_reference_with_id( 
            store.get_pid(), 
            AbstractResourceHelper.id() ) );
    }

    //=============================================================================
    // utilities
    //=============================================================================

    /**
     * Returns the storage home for LinkStorage types.
     * @osm.warning this operation will be moved to a service under the component manager
     */
    protected LinkStorageHome getLinkHome() throws NotFound 
    {
        if( m_link_home != null ) return m_link_home;
        m_link_home = ( LinkStorageHome ) m_home.get_catalog().find_storage_home( 
			"PSDL:osm.net/session/linkage/LinkStorageHomeBase:1.0" );
        return m_link_home;
    }

   /**
    * Returns a client principal invoking the operation.
    * @return StandardPrincipal the client principal
    */
    public StandardPrincipal getPrincipal() throws Exception
    {
	  return m_principal_manager.getPrincipal();
    }

   /**
    * Return a random seed used by factory implmentation during the creation of 
    * constant random identifiers.
    */
    public int getRandom( )
    {
	  return m_seed.nextInt( Integer.MAX_VALUE );
    }

   /**
    * Returns the short PID to the default domain.
    */
    public byte[] getDefaultDomain( )
    {
	  return m_domain_service.getDefaultDomain( );
    }

   /**
    * Returns the current ORB instance.
    */
    public ORB getORB( )
    {
	  return m_orb;
    }

   /**
    * Returns the time service.
    */
    protected TimeService getTimeService()
    {
        return m_clock;
    }

}
