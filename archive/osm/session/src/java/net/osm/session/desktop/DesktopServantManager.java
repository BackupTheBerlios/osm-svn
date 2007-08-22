/*
 * Copyright 2000 OSM SARL. All Rights Reserved.
 * 
 * This software is the proprietary information of OSM SARL.  
 * Use is subject to license terms.
 * 
 * @author  Stephen McConnell
 * @version 1.0 26/03/2001
 */

package net.osm.session.desktop;

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

import org.apache.orb.ORB;
import org.apache.orb.util.LifecycleHelper;
import org.apache.orb.ORBContext;
import org.apache.pss.Connector;
import org.apache.pss.Session;
import org.apache.pss.ConnectorContext;
import org.apache.pss.StorageContext;
import org.apache.time.TimeUtils;

import org.omg.CORBA.Any;
import org.omg.CosTime.TimeService;
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

import net.osm.realm.StandardPrincipal;
import net.osm.session.resource.AbstractResourceServantManager;
import net.osm.session.SessionException;
import net.osm.session.ProducedBy;
import net.osm.session.AdministeredBy;
import net.osm.session.resource.AbstractResource;
import net.osm.session.workspace.WorkspaceServantManager;
import net.osm.session.user.UserService;
import net.osm.session.linkage.LinkStorageHome;
import net.osm.session.user.UserStorage;

/**
 * <p>The <code>AbstractResourceServantManager</code> provides services supporting the 
 * creation of new <code>AbstractResource</code> instances and a servant activation
 * model for handling the redirection of incomming requests.</p>
 *
 * @author <a href="mailto:mcconnell@osm.net">Stephen McConnell</a>
 */
public class DesktopServantManager extends WorkspaceServantManager
implements DesktopService
{

    //=======================================================================
    // static
    //=======================================================================

    private static final String c_key = "desktop";

    //=======================================================================
    // state
    //=======================================================================

    private UserService m_user_service;
    private LinkStorageHome m_link_home;
    private DesktopStorageHome m_home;
    private POA m_poa;

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
	  m_user_service = (UserService) manager.lookup( UserService.USER_SERVICE_KEY );
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
            m_home = (DesktopStorageHome) super.getSession().find_storage_home( 
	        "PSDL:osm.net/session/desktop/DesktopStorageHomeBase:1.0" );
            m_link_home = (LinkStorageHome) super.getSession().find_storage_home(
 		"PSDL:osm.net/session/linkage/LinkStorageHomeBase:1.0" );
	  }
	  catch( Throwable throwable )
	  {
	      String error = "unable to complete initialization due to a unexpected error";
		throw new DesktopException( error, throwable );
	  }
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
        return c_key;
    }

   /**
    * Returns a servant implmentation.
    * @return Servant a managable servant
    */
    protected Servant createServant( 
      StorageContext context, Configuration config, ServiceManager manager 
    ) throws Exception
    {
        DesktopDelegate delegate = new DesktopDelegate();
        Servant servant = new DesktopPOATie( delegate );
        Logger log = getLogger().getChildLogger( "" + System.identityHashCode( delegate ) );
        LifecycleHelper.pipeline( delegate, log, context, config, manager );
        return servant;
    }

   /**
    * Returns the storage home managed by the manager.
    * @return StorageHomeBase the storage home
    */
    protected StorageHomeBase getStorageHome()
    {
        return m_home;
    }
    
   /**
    * Returns an object reference for a storage object.
    * @param store the storage object
    */
    protected AbstractResource getObjectReference( StorageObject store )
    {
        try
        {
            return getDesktopReference( (DesktopStorage) store );
        }
        catch( Throwable e )
        {
            throw new DesktopRuntimeException("Invalid storage type argument.", e );
        }
    }

    //=======================================================================
    // DesktopService
    //=======================================================================
    

   /**
    * Creations of a Desktop object reference.
    * @param name the initial name to asign to the desktop
    * @param user_short_pid storage object short pid representing the owner
    * @return Desktop desktop object reference
    * @exception DesktopException
    */
    public Desktop createDesktop( String name, byte[] user_short_pid ) 
    throws DesktopException
    {
        DesktopStorage store = createDesktopStorage( name, user_short_pid );
        return getDesktopReference( store );
    }

   /**
    * Creations of a Desktop storage instance.
    * @param name the initial name to asign to the desktop
    * @param user_short_pid storage object short pid representing the owner
    * @return DesktopStorage desktop storage
    * @exception DesktopException
    */
    public DesktopStorage createDesktopStorage( String name, byte[] user_short_pid ) 
    throws DesktopException
    {
        if( getLogger().isDebugEnabled() ) getLogger().debug("creating desktop storage");

        DesktopStorage store = null;
        try
        {
            store = (DesktopStorage) 
              super.createWorkspaceStorage( m_home, name );
            store.owner_short_pid( user_short_pid );
            return store;
        }
        catch (Throwable e)
        {
		String error = "Internal failure while creating desktop storage.";
            throw new DesktopException( error, e );
        }
    }

   /**
    * Creation of a Desktop object reference based on a supplied storage object.
    * @param store a desktop storage object
    * @return Desktop a Desktop object reference
    * @exception FactoryException
    */
    public Desktop getDesktopReference( DesktopStorage store ) 
    throws DesktopException
    {
        return DesktopHelper.narrow( 
          m_poa.create_reference_with_id( 
            store.get_pid(), 
            DesktopHelper.id() ) );
    }

   /**
    * Returns a reference to a Desktop given a persistent storage object identifier.
    * @param pid desktop short persistent identifier
    * @return Desktop the corresponding PID
    * @exception NotFound if the supplied pid does not match a know desktop
    */
    public Desktop getDesktopReference( byte[] pid )
    throws NotFound
    {
        try
        {
            DesktopStorage store = (DesktopStorage) m_home.find_by_short_pid( pid );
            return getDesktopReference( store );
        }
        catch( DesktopException de )
        {
            throw new DesktopRuntimeException(
              "Could not create desktop reference.", de );
        } 
    }
}
