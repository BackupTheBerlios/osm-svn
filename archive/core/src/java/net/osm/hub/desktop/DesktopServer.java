/*
 * @(#)DesktopServer.java
 *
 * Copyright 2000 OSM SARL. All Rights Reserved.
 * 
 * This software is the proprietary information of OSM SARL.  
 * Use is subject to license terms.
 * 
 * @author  Stephen McConnell
 * @version 1.0 26/03/2001
 */

package net.osm.hub.desktop;

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
import org.omg.CosPersistentState.StorageObject;
import org.omg.CosPersistentState.NotFound;
import org.omg.CosPersistentState.Connector;
import org.omg.CosPersistentState.Session;
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
import org.omg.Session.AbstractResource;
import org.omg.Session.Desktop;
import org.omg.Session.User;
import org.omg.Session.DesktopHelper;
import org.omg.Session.DesktopPOATie;
import org.omg.Session.ProducedBy;
import org.omg.Session.AdministeredBy;
import org.omg.CommunityFramework.Criteria;

import net.osm.hub.user.UserService;
import net.osm.hub.gateway.FactoryException;
import net.osm.hub.resource.AbstractResourceServer;
import net.osm.hub.workspace.WorkspaceServer;
import net.osm.hub.pss.DesktopStorage;
import net.osm.hub.pss.DesktopStorageRef;
import net.osm.hub.pss.DesktopStorageBaseRef;
import net.osm.hub.pss.DesktopStorageHome;
import net.osm.hub.pss.LinkStorageHome;
import net.osm.time.TimeService;
import net.osm.time.TimeUtils;
import net.osm.realm.StandardPrincipal;
import net.osm.util.Incrementor;

/**
 * The <code>DesktopServer</code> block services supporting the 
 * creation of new <code>Desktop</code> instances.
 *
 * @author <a href="mailto:mcconnell@osm.net">Stephen McConnell</a>
 */

public class DesktopServer extends AbstractResourceServer 
implements DesktopService, DesktopAdministratorService
{

    //=======================================================================
    // state
    //=======================================================================

    private DesktopStorageHome home;
    private UserService userService;
    private LinkStorageHome links;
    private Incrementor linkInc = Incrementor.create("LINK");

    //=================================================================
    // Composable
    //=================================================================
    
   /**
    * Pass the <code>ComponentManager</code> to the <code>Composable</code>.
    * The <code>Composable</code> implementation should use the specified
    * <code>ComponentManager</code> to acquire the components it needs for
    * execution.
    * @param manager The <code>ComponentManager</code> which this
    *                <code>Composer</code> uses.
    */
    public void compose( ComponentManager manager )
    throws ComponentException
    {
        if( getLogger().isDebugEnabled() ) getLogger().debug( 
	    "composition [community server]" );
	  super.compose( manager );
	  try
	  {
	      super.manager.put( "WORKSPACE", manager.lookup("WORKSPACE") );
	      super.manager.put( "FACTORY", manager.lookup("FACTORY") );
	  }
	  catch( Exception e )
	  {
		final String error = "manager failed to provide UserService";
		throw new ComponentException( error, e);
	  }
        if( getLogger().isDebugEnabled() ) getLogger().debug( 
	    "composition [community server] complete" );
    }

    //=======================================================================
    // Initializable
    //=======================================================================
    
    public void initialize()
    throws Exception
    {
        if( getLogger().isDebugEnabled()) getLogger().debug( 
		"initialization [desktop server]" );
	  super.initialize();
        home = (DesktopStorageHome) session.find_storage_home(
 		"PSDL:osm.net/hub/pss/DesktopStorageHomeBase:1.0" );
        links = (LinkStorageHome) session.find_storage_home(
 		"PSDL:osm.net/hub/pss/LinkStorageHomeBase:1.0" );
        
        if( getLogger().isDebugEnabled()) getLogger().debug( 
		"initialization complete [desktop server]" );
    }
        
    //=======================================================================
    // FactoryService
    //=======================================================================

   /**
    * Creation of a workspace reference.
    * @param name the name of the workspace
    * @param criteria factory constraint (currently ignored)
    * @exception FactoryException
    */
    public AbstractResource create( String name, Criteria criteria ) 
    throws FactoryException
    {
        throw new FactoryException("not-supported, use DesktopService interface");
    }

    //=======================================================================
    // DesktopAdministratorService
    //=======================================================================

   /**
    * Callback through which a UserService provider 
    * can register itself with the DesktopService.
    * @param service the task service
    */
    public void setUserService( UserService service )
    {
        if( service == null ) throw new NullPointerException("supplied service is null");
        if( userService != null ) throw new IllegalArgumentException("user service already initialized");
        userService = service;
        super.manager.put("USER", userService );
    }

   /**
    * Creations of a Desktop storage instance bound to the supplied owner as administrator.
    * 
    * @param name the initial name to asign to the desktop
    * @param owner the persistent storage object representing the owner
    * @return DesktopStorageRef desktop storage reference
    * @exception FactoryException
    */
    public DesktopStorageRef createDesktopStorageRef( 
      String name, StorageObject owner, StandardPrincipal principal ) 
    throws FactoryException
    {
        if( getLogger().isDebugEnabled() ) getLogger().debug( "create" );
	  if( !initialized() ) throw new IllegalStateException("server has not been initialized");
	  if( disposed() ) throw new IllegalStateException("server is in disposed state");

	  // create a storage object for the Desktop

        DesktopStorage store = null;
        try
        {
		if( principal == null ) principal = getCurrentPrincipal( );
            long t = TimeUtils.resolveTime( clock );
            store = home.create(
			principal,
			domain.getDomainShortPID(), 
			random.getRandom(), t, t, t, name, 
			new ProducedBy(), owner.get_short_pid()
	      );

            AdministeredBy admin = new AdministeredBy( userService.getUserReference( owner.get_pid() ) );
            store.accessed_by().add( links.create( linkInc.increment(), admin ) );
            return new DesktopStorageBaseRef( store.get_short_pid(), home );
        }
        catch (Exception e)
        {
		String error = "Unexpected failure while creating desktop storage.";
            if( getLogger().isErrorEnabled() ) getLogger().error( error, e );
            throw new FactoryException( error, e );
        }
    }

   /**
    * Creation of a desktop delegate based on a supplied persistent identifier.
    * @param oid the persistent object identifier
    * @return Servant the servant
    */
    public DesktopDelegate createDesktopDelegate( byte[] oid )
    {
	  return (DesktopDelegate) super.processLifecycle( new DesktopDelegate(), oid );
    }


    //=======================================================================
    // DesktopService
    //=======================================================================

   /**
    * Creations of a Desktop storage instance.
    * @param name the initial name to asign to the desktop
    * @param owner the storage object representing the owner
    * @return DesktopStorageRef desktop storage reference
    * @exception FactoryException
    */
    public DesktopStorageRef createDesktopStorageRef( String name, StorageObject owner ) 
    throws FactoryException
    {
        return createDesktopStorageRef( name, owner, null );
    }

   /**
    * Creation of a Desktop object reference based on a supplied storage object.
    * @param store a desktop storage object
    * @return Desktop a Desktop object reference
    * @exception FactoryException
    */
    public Desktop getDesktopReference( StorageObject store ) 
    throws FactoryException
    {
        if( !( store instanceof DesktopStorage )) throw new FactoryException(
          "supplied storage type '" + store.getClass().getName() + "' not supported");

        try
	  {
            return DesktopHelper.narrow(
              poa.create_reference_with_id
              (
                store.get_pid(), DesktopHelper.id()
              )
            );
        }
        catch(Throwable e)
        {
		String info = "failed to create desktop objecxt reference";
            throw new FactoryException( info, e );
        }
    }

   /**
    * Returns a reference to a Desktop given a persistent storage object identifier.
    * @param pid sektop persistent identifier
    * @return Desktop the corresponding PID
    * @exception NotFound if the supplied pid does not matach a know desktop
    */
    public Desktop getDesktopReference( byte[] pid )
    throws NotFound
    {
	  try
	  {
            StorageObject store = (StorageObject) home.get_catalog().find_by_pid( pid );
            return DesktopHelper.narrow(
              poa.create_reference_with_id
              (
                store.get_pid(), DesktopHelper.id()
              )
            );
	  }
	  catch( NotFound e )
	  {
	      throw new NotFound();
	  }
	  catch( Throwable e )
	  {
		String error = "unexpected exception while resolving reference";
		if( getLogger().isErrorEnabled() ) getLogger().error( error, e );
	      throw new RuntimeException( error, e );
	  }
    }

    //=======================================================================
    // implementation of DefaultServer abstract methods
    //=======================================================================

   /**
    * Creation of a servant based on a supplied persistent identifier.
    * @param oid the persistent object identifier
    * @return Servant the servant
    */
    public Servant createServant( byte[] oid )
    {
        return new DesktopPOATie( createDesktopDelegate( oid ), poa );
    }


}
