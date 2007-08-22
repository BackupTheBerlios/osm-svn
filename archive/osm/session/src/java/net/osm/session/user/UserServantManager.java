/*
 * Copyright 2000 OSM SARL. All Rights Reserved.
 * 
 * This software is the proprietary information of OSM SARL.  
 * Use is subject to license terms.
 * 
 * @author  Stephen McConnell
 * @version 1.0 26/03/2001
 */

package net.osm.session.user;

import java.security.Principal;
import javax.security.auth.x500.X500Principal;

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
import org.apache.orb.ORBContext;
import org.apache.orb.util.LifecycleHelper;
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
import org.omg.CosPersistentState.StorageHomeBase;
import org.omg.CosPersistentState.NotFound;
import org.omg.CosPropertyService.PropertyDef;
import org.omg.Session.Desktop;
import org.omg.Session.Workspace;
import org.omg.Session.Administers;

import net.osm.session.X500Helper;
import net.osm.session.SessionException;
import net.osm.session.ProducedBy;
import net.osm.session.resource.AbstractResourceServantManager;
import net.osm.session.desktop.DesktopService;
import net.osm.session.desktop.DesktopStorage;
import net.osm.session.desktop.DesktopStorageRef;
import net.osm.session.desktop.DesktopStorageBaseRef;
import net.osm.session.workspace.WorkspaceStorage;
import net.osm.session.resource.AbstractResource;
import net.osm.realm.StandardPrincipal;
import net.osm.properties.PropertiesService;

/**
 * <p>The <code>AbstractResourceServantManager</code> provides services supporting the 
 * creation of new <code>AbstractResource</code> instances and a servant activation
 * model for handling the redirection of incomming requests.</p>
 *
 * @author <a href="mailto:mcconnell@osm.net">Stephen McConnell</a>
 */
public class UserServantManager extends AbstractResourceServantManager
implements UserService
{
    //=======================================================================
    // static
    //=======================================================================

    private static final String c_key = "user";

    //=======================================================================
    // state
    //=======================================================================

    private ServiceManager m_manager;
    private DesktopService m_desktop_service;
    private UserStorageHome m_home;
    private PropertiesService m_properties_service;
    private POA m_poa;

    //=======================================================================
    // Serviceable
    //=======================================================================

    /**
     * Pass the <code>ServiceManager</code> to the <code>Composable</code>.
     * The <code>Serviceable</code> implementation uses the supplied
     * <code>ServiceManager</code> to acquire the components it needs for
     * execution.
     * @param controller the <code>ServiceManager</code> for this delegate
     * @exception ServiceException
     */
    public void service( ServiceManager manager )
    throws ServiceException
    {
	  super.service( manager );
        m_manager = manager;
        m_desktop_service = (DesktopService) manager.lookup( 
          DesktopService.DESKTOP_SERVICE_KEY );
        m_properties_service = (PropertiesService) manager.lookup( 
          PropertiesService.PROPERTY_SERVICE_KEY );
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
            m_home = (UserStorageHome) super.getSession().find_storage_home( 
	        "PSDL:osm.net/session/user/UserStorageHomeBase:1.0" );
	  }
	  catch( Throwable throwable )
	  {
	      String error = "unable to complete supplier initialization due to a unexpected error";
		throw new UserException( error, throwable );
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
        UserDelegate delegate = new UserDelegate();
        Servant servant = new UserPOATie( delegate );
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
            return getUserReference( (UserStorage) store );
        }
        catch( Throwable e )
        {
            throw new UserRuntimeException("Invalid storage type argument.", e );
        }
    }

    //=======================================================================
    // UserService
    //=======================================================================
    
   /**
    * Creation of a User.
    * @return User a new User object reference
    */
    public User createUser( ) 
    throws UserException
    {
        try
	  {
            UserStorage store = createUserStorage( getPrincipal( ) );
		return getUserReference( store );
	  }
	  catch( Throwable e )
	  {
		final String error = "unable to create new user";
		throw new UserException( error, e );
	  }
    }

   /**
    * Creation of a User based on a supplied principal.
    * @param name the initial name to assign to the user
    * @return User a new User object reference
    */
    public UserStorage createUserStorage( StandardPrincipal principal ) 
    throws UserException
    {
        try
	  {
	      byte[] path = principal .getEncoded();
            return (UserStorage) m_home.find_by_path( path );
        }
	  catch( NotFound nf )
        {
	      Principal p = principal.getPrincipal();
	      String name = p.getName();
	      if( p instanceof X500Principal )
	      {
	          X500Helper helper = new X500Helper( name );
                name = helper.getCommonName();
            }
	      return createUserStorage( name, principal );
	  }
	  catch( Throwable creationException )
	  {
	      String error = "Unexpected exception while creating user reference."; 
	      throw new UserException( error, creationException );
	  }
    }

   /**
    * Creation of a User based on a supplied principal and name.
    * @param name the initial name to assign to the user
    * @return User a new User object reference
    */
    private UserStorage createUserStorage( String name, StandardPrincipal principal ) 
    throws UserException
    {
        if( name == null )
        {
            throw new NullPointerException(
              "Null name argument during user storage creation.");
        }

        if( principal == null )
        {
            throw new NullPointerException(
              "Null principal argument during user storage creation.");
        }

        if( getLogger().isDebugEnabled() ) getLogger().debug("creating user storage");

        UserStorage store = createUserStorage( name, principal.getEncoded() );
        return store;
    }

   /**
    * Creation of a User based on a supplied principal.
    * @param name the initial name to assign to the user
    * @param path encoded principal
    * @return UserStorage a new storage object
    */
    public UserStorage createUserStorage( String name, byte[] path ) 
    throws UserException
    {
        if( getLogger().isDebugEnabled() ) getLogger().debug("creating user storage");


        UserStorage store = null;
        try
        {
            store = (UserStorage) super.createAbstractResourceStorage( m_home, name );
            store.path( path );
            store.connected( false );

            byte[] ps_pid = m_properties_service.createPropertySetDefStorage().get_short_pid();
            store.property_set( ps_pid );

            DesktopStorage desktop = m_desktop_service.createDesktopStorage( 
			"Desktop", store.get_short_pid() );
            byte[] desktop_pid = desktop.get_short_pid();
            store.local_desktop( desktop_pid );

		Administers admin = new net.osm.session.Administers( 
              m_desktop_service.getDesktopReference( desktop ));
		store.accesses().add( getLinkHome().create( admin ) );

            return store;
        }
        catch( Throwable e )
        {
            final String error = "Unexpected problem while creating user storage.";
            if( getLogger().isErrorEnabled() ) getLogger().error( error, e );
            throw new UserException( error, e );
        }
    }

   /**
    * Locates a User instance relative to the current principal identity.
    * @param name the initial name to assign to the user
    * @return User a new User object reference
    */
    public User locateUser( ) 
    throws NotFound
    {
	  // 
	  // locate the user by getting the encoded for of the pricipal and 
        // and using that as the key
	  //

        UserStorage store = null;
        StandardPrincipal principal = null;
        try
        {
		final byte[] array = getPrincipal().getEncoded();
            store = m_home.find_by_path( array );
            getLogger().debug("located user reference");
		return getUserReference( store );
        }
        catch (NotFound e)
        {
		String info = "unknown principal: " + principal;
            throw new NotFound( info );
        }
        catch (Throwable e)
        {
		String problem = "Unexpected exception while attempting to locate a user.";
	      if( getLogger().isErrorEnabled() ) getLogger().error( problem, e );
            throw new UserRuntimeException( problem, e );
        }
    }

   /**
    * Returns a reference to a User given a persistent storage object identifier.
    * @param pid user persistent identifier
    * @return User the corresponding User reference
    * @exception NotFound if the supplied pid does not match a known user
    */
    public User getUserReference( byte[] pid )
    throws NotFound
    {
        final UserStorage store = (UserStorage) m_home.get_catalog().find_by_pid( pid );
        return getUserReference( store );
    }

   /**
    * Returns a reference to a User given a persistent storage object.
    * @param store user storage object
    * @return User the corresponding User reference
    */
    public User getUserReference( UserStorage store )
    {
        return UserHelper.narrow( 
          m_poa.create_reference_with_id( 
            store.get_pid(), 
            UserHelper.id() ) );
    }

   /**
    * Returns a reference to a User given a short user persistent storage object identifier.
    * @param pid short user persistent identifier
    * @return User the corresponding Uer reference
    * @exception NotFound if the supplied pid does not match a known user
    */
    public User getShortUserReference( byte[] pid )
    throws NotFound
    {
        final UserStorage store = (UserStorage) m_home.find_by_short_pid( pid );
        return getUserReference( store );
    }
}
