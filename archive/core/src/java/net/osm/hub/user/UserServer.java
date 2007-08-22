/*
 * @(#)UserServer.java
 *
 * Copyright 2000 OSM SARL. All Rights Reserved.
 * 
 * This software is the proprietary information of OSM SARL.  
 * Use is subject to license terms.
 * 
 * @author  Stephen McConnell
 * @version 1.0 26/03/2001
 */

package net.osm.hub.user;

import java.security.Principal;
import javax.security.auth.x500.X500Principal;
import java.security.cert.CertificateFactory;
import java.io.ByteArrayInputStream;
import java.security.cert.CertPath;

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

import org.omg.CORBA.ORB;
import org.omg.CORBA.Any;
import org.omg.CORBA.LocalObject;
import org.omg.CORBA.Policy;
import org.omg.CORBA.TypeCode;
import org.omg.CosPersistentState.NotFound;
import org.omg.CosPersistentState.Connector;
import org.omg.CosPersistentState.Session;
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
import org.omg.CosPropertyService.PropertyDef;
import org.omg.Session.User;
import org.omg.Session.Desktop;
import org.omg.Session.UserHelper;
import org.omg.Session.ProducedBy;
import org.omg.Session.UserPOATie;
import org.omg.Session.AbstractResource;
import org.omg.Session.Administers;
import org.omg.Session.AdministeredBy;
import org.omg.CommunityFramework.Criteria;
import net.osm.hub.gateway.FactoryException;
import net.osm.hub.gateway.FactoryService;
import net.osm.hub.gateway.ServantContext;
import net.osm.hub.resource.AbstractResourceServer;
import net.osm.hub.desktop.DesktopAdministratorService;
import net.osm.hub.desktop.DesktopService;
import net.osm.hub.desktop.DesktopDelegate;
import net.osm.hub.task.TaskService;
import net.osm.hub.workspace.WorkspaceService;
import net.osm.hub.workspace.WorkspaceAdministratorService;
import net.osm.hub.pss.UserStorage;
import net.osm.hub.pss.UserStorageHome;
import net.osm.hub.pss.DesktopStorageRef;
import net.osm.hub.pss.DesktopStorageBaseRef;
import net.osm.hub.pss.LinkStorageHome;
import net.osm.time.TimeService;
import net.osm.time.TimeUtils;
import net.osm.realm.StandardPrincipal;
import net.osm.realm.StandardPrincipalBase;
import net.osm.util.X500Helper;
import net.osm.util.Incrementor;

/**
 * The <code>UserServer</code> block provides services supporting the 
 * creation of new <code>User</code> instances based on a current security
 * principal, and location of other user object references.
 *
 * @author <a href="mailto:mcconnell@osm.net">Stephen McConnell</a>
 */

public class UserServer extends AbstractResourceServer 
implements UserService, UserAdministratorService
{

    //=======================================================================
    // state
    //=======================================================================

    private UserStorageHome home;
    private DesktopService desktopService;
    private TaskService taskService;
    private DesktopAdministratorService desktopAdministratorService;
    private WorkspaceAdministratorService workspaceAdministratorService;
    private WorkspaceService workspaceService;
    private LinkStorageHome links;
    private Incrementor linkInc = Incrementor.create("LINK");

    //=================================================================
    // Composable
    //=================================================================
    
    /**
     * The compose operation handles the aggregation of dependent services:
     * <ul>
     * <li>DESKTOP: desktop service supporting creation of a new desktop
     * </ul>
     * <p>
     * @param manager The <code>ComponentManager</code> which this
     *                <code>Composer</code> uses.
     */
    public void compose( ComponentManager manager )
    throws ComponentException
    {
        if( getLogger().isDebugEnabled() ) getLogger().debug( "composition [user server]" );
	  super.compose( manager );
	  try
	  {
            desktopService = (DesktopService)manager.lookup("DESKTOP");
            desktopAdministratorService = (DesktopAdministratorService)
              manager.lookup("DESKTOP-ADMINISTRATOR");
            workspaceAdministratorService = (WorkspaceAdministratorService)
              manager.lookup("WORKSPACE-ADMINISTRATOR");
            workspaceService = (WorkspaceService)
              manager.lookup("WORKSPACE");

		super.manager.put( "DESKTOP", desktopService );
		super.manager.put( "WORKSPACE", workspaceService );
		super.manager.put( "FACTORY", manager.lookup("FACTORY") );

        }
        catch( Exception e )
	  {
		final String error = "composition phase failure";
		throw new ComponentException( error, e );
	  }
        if( getLogger().isDebugEnabled() ) getLogger().debug( "composition complete [user server]" );
    }

    
    //=======================================================================
    // Initializable
    //=======================================================================
    
    public void initialize()
    throws Exception
    {
        if( getLogger().isDebugEnabled() ) getLogger().debug( "initialization [user server]" );
	  super.initialize();

        home = (UserStorageHome) session.find_storage_home(
 		"PSDL:osm.net/hub/pss/UserStorageHomeBase:1.0" );
        links = (LinkStorageHome) session.find_storage_home( 	
	    "PSDL:osm.net/hub/pss/LinkStorageHomeBase:1.0" );

	  //
	  // callback inverse dependecies
        //

        desktopAdministratorService.setUserService( this );
        workspaceAdministratorService.setUserService( this );
        if( getLogger().isDebugEnabled() ) getLogger().debug( "initialization complete [user server]" );
    }

    //=======================================================================
    // FactoryService
    //=======================================================================

   /**
    * Creation of a new User.
    * @param name the user (not used)
    * @param criteria factory constraint (not used)
    * @exception FactoryException
    */
    public AbstractResource create( String name, Criteria criteria ) 
    throws FactoryException
    {
        return createUser( );
    }

    //=======================================================================
    // UserService
    //=======================================================================

   /**
    * Creation of a User based on the current principal security identity.
    * @param name the initial name to assign to the workspace
    * @return User a new User object reference
    */
    public User createUser( ) 
    throws FactoryException
    {
        try
	  {
            UserStorage store = createUserStorage( getCurrentPrincipal( ) );
		session.flush();
		return getUserReference( store );
	  }
	  catch( Throwable e )
	  {
		final String error = "unable to create new user";
		throw new FactoryException( error, e );
	  }
    }

   /**
    * Locates a User instance relative to the current principal identity.
    * 
    * @param log the log channel
    * @param services gateway context established by the server
    * @return User object reference to the user matching the pricipal identity
    * @exception NotFound if the current principal does not match a know user
    * @see net.osm.realm.StandardPrincipal
    */
    public User locateUser( ) throws NotFound
    {
	  // 
	  // locate the user by getting the encoded for of the pricipal and 
        // and using that as the key
	  //

        UserStorage store = null;
        StandardPrincipal principal = null;
        try
        {
		final byte[] array = getCurrentPrincipal().getEncoded();
            store = home.find_by_path( array );
            getLogger().info("located user reference");
		return getUserReference( store );
        }
        catch (NotFound e)
        {
		String info = "unknown principal: " + principal;
            throw new NotFound( info );
        }
        catch (Exception e)
        {
		String problem = "Unexpected exception while attempting to locate a user.";
	      if( getLogger().isErrorEnabled() ) getLogger().error( problem, e );
            throw new RuntimeException( problem, e );
        }
    }

    //=======================================================================
    // UserAdministratorService
    //=======================================================================
    
   /**
    * Callback through which a TaskService provider 
    * can register itself with the UserService.
    * @param service the task service
    */
    public void setTaskService( TaskService service )
    {
        if( service == null ) throw new NullPointerException("supplied service is null");
        if( taskService != null ) throw new IllegalArgumentException("task service already initialized");
        taskService = service;
        super.manager.put( "TASK", taskService );
    }

   /**
    * Returns a reference to a User given a persistent storage object.
    * @param store the user persistent storage object
    * @return User the corresponding Uer reference
    */
    public User getUserReference( StorageObject store )
    {
        return UserHelper.narrow(
          poa.create_reference_with_id
          (
              store.get_pid(), UserHelper.id()
          )
        );
    }

   /**
    * Creation of a User based on a supplied principal. If a User already exists 
    * under the supplied path, a User reference connected to that storage object is 
    * returned.
    * @param name the initial name to assign to the user
    * @return User a new User object reference
    */
    public UserStorage createUserStorage( StandardPrincipal standard ) 
    throws FactoryException
    {

        try
	  {
	      byte[] path = standard.getEncoded();
            return (UserStorage) home.find_by_path( path );
        }
	  catch( NotFound nf )
        {
	      Principal principal = standard.getPrincipal();
	      String name = principal.getName();
	      if( principal instanceof X500Principal )
	      {
	          X500Helper helper = new X500Helper( name );
                name = helper.getCommonName();
            }
	      return createUserStorage( name, standard );
	  }
	  catch( Throwable creationException )
	  {
	      String error = "Unexpected exception while creating user reference."; 
	      if( getLogger().isErrorEnabled() ) getLogger().error( error, creationException );
	      throw new org.omg.CORBA.INTERNAL();
	  }
    }

   /**
    * Creation of a UserStorage object.
    * @param name the initial name to assign to the workspace
    * @return User a new User object reference
    */
    private UserStorage createUserStorage( String name, StandardPrincipal principal ) 
    throws FactoryException
    {
        if( getLogger().isDebugEnabled() ) getLogger().debug( "create" );
	  if( !initialized() ) throw new IllegalStateException("server has not been initialized");
	  if( disposed() ) throw new IllegalStateException("server is in disposed state");
	  if( taskService == null ) throw new FactoryException("required task service is null");

	  // create a storage object for the user

        DesktopStorageRef desktopRef = new DesktopStorageBaseRef();
        UserStorage store = null;

	  boolean administrationMode = (principal != null);
        try
        {
	      if( principal == null ) principal = getCurrentPrincipal( );
		byte[] path = principal.getEncoded();
            long t = TimeUtils.resolveTime( clock );
            store = home.create(
			principal, path,
			domain.getDomainShortPID(), 
			random.getRandom(), t, t, t, name, 
			new ProducedBy(), desktopRef, false
	      );            
        }
        catch (Exception e)
        {
		String error = "Unexpected failure while creating user storage.";
            if( getLogger().isErrorEnabled() ) getLogger().error( error, e );
            throw new FactoryException( error, e );
        }

        Desktop desktop = null;
        try
        {
		desktopRef = desktopAdministratorService.createDesktopStorageRef( 
			"Desktop", store, principal );
		store.desktop( desktopRef );
		desktop = desktopService.getDesktopReference( desktopRef.deref() );
		Administers admin = new Administers( desktop );
		store.accesses().add( links.create( linkInc.increment(), admin ) );
        }
        catch (Exception e)
        {
		String error = "Desktop creation failure";
            if( getLogger().isErrorEnabled() ) getLogger().error( error, e );
            throw new FactoryException( error, e );
        }

	  //
	  // Initalize the property set storage object.
	  // Any constraints concerning types and properties should
	  // be handled here.
        // WARNING: PropertySet type and property constraints not
	  // implementated because the the spec is insufficient - need
	  // to be able to declare user properties in a more 
	  // comprehensive way.
        //

	  store.property_set().types( new TypeCode[0] );
	  store.property_set().definitions( new PropertyDef[0] );
	  store.property_set().values( new byte[0][] );
        return store;
    }


   /**
    * Creation of a servant based on a supplied delegate.
    * @param oid the persistent object identifier
    * @return Servant the servant
    */
    public Servant createServant( byte[] oid )
    {
        return new UserPOATie( createUserDelegate( oid ), poa );
    }

   /**
    * Creation of a desktop delegate based on a supplied persistent identifier.
    * @param oid the persistent object identifier
    * @return Servant the servant
    */
    public UserDelegate createUserDelegate( byte[] oid )
    {
	  return (UserDelegate) super.processLifecycle( new UserDelegate(), oid );
    }

   /**
    * Returns a reference to a User given a persistent storage object identifier.
    * @param pid storage object persistent identifier
    * @return User the corresponding Uer reference
    * @exception NotFound if the supplied pid does not match a known user
    */
    public User getUserReference( byte[] pid )
    throws NotFound
    {
	  StorageObject store = (StorageObject) home.get_catalog().find_by_pid( pid );
        return getUserReference( store );
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
	  StorageObject store = (StorageObject) home.find_by_short_pid( pid );
        return getUserReference( store );
    }

}
