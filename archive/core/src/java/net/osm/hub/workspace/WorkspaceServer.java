/*
 * @(#)WorkspaceServer.java
 *
 * Copyright 2000 OSM SARL. All Rights Reserved.
 * 
 * This software is the proprietary information of OSM SARL.  
 * Use is subject to license terms.
 * 
 * @author  Stephen McConnell
 * @version 1.0 26/03/2001
 */

package net.osm.hub.workspace;

import org.apache.avalon.framework.logger.Logger;
import org.apache.avalon.framework.logger.LogEnabled;
import org.apache.avalon.framework.context.Context;
import org.apache.avalon.framework.context.Contextualizable;
import org.apache.avalon.framework.context.ContextException;
import org.apache.avalon.framework.component.Component;
import org.apache.avalon.framework.component.ComponentManager;
import org.apache.avalon.framework.component.ComponentException;
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
import org.omg.Session.AbstractResource;
import org.omg.Session.Workspace;
import org.omg.Session.WorkspaceHelper;
import org.omg.Session.WorkspacePOATie;
import org.omg.Session.ProducedBy;
import org.omg.CommunityFramework.Criteria;

import net.osm.hub.gateway.FactoryException;
import net.osm.hub.resource.AbstractResourceServer;
import net.osm.hub.pss.WorkspaceStorage;
import net.osm.hub.pss.WorkspaceStorageHome;
import net.osm.hub.user.UserService;
import net.osm.time.TimeService;
import net.osm.time.TimeUtils;
import net.osm.realm.StandardPrincipal;

/**
 * The <code>WorkspaceServer</code> block services supporting the 
 * creation of new <code>Workspace</code> instances.
 *
 * @author <a href="mailto:mcconnell@osm.net">Stephen McConnell</a>
 */

public class WorkspaceServer extends AbstractResourceServer 
implements WorkspaceService, WorkspaceAdministratorService
{

    //=======================================================================
    // state
    //=======================================================================

    private WorkspaceStorageHome home;
    private UserService userService;

    //=================================================================
    // Composable
    //=================================================================
    
    /**
     * The compose operation handles the aggregation of dependent services:
     * @param manager The <code>ComponentManager</code> which this
     *                <code>Composer</code> uses.
     */
    public void compose( ComponentManager manager )
    throws ComponentException
    {
        if( getLogger().isDebugEnabled() ) getLogger().debug( "composition [workspace server]" );
	  super.compose( manager );
	  try
	  {
		super.manager.put( "FACTORY", manager.lookup("FACTORY") );
        }
        catch( Exception e )
	  {
		final String error = "composition phase failure";
		throw new ComponentException( error, e );
	  }
        if( getLogger().isDebugEnabled() ) getLogger().debug( "composition complete [workspace server]" );
    }

    
    //=======================================================================
    // Initializable
    //=======================================================================
    
    public void initialize()
    throws Exception
    {
        if( getLogger().isDebugEnabled() ) getLogger().debug( "initialization [workspace server]" );
	  super.initialize();
        home = (WorkspaceStorageHome) session.find_storage_home(
 		"PSDL:osm.net/hub/pss/WorkspaceStorageHomeBase:1.0" );
        if( getLogger().isDebugEnabled() ) getLogger().debug( "initialization complete [workspace server]" );
	  super.manager.put("WORKSPACE", this );
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
        return createWorkspace( name );
    }
    
    //=======================================================================
    // WorkspaceAdministratorService
    //=======================================================================

   /**
    * Declare the UserService to the WorkspaceService.  This 
    * operation is provided as means to bypass circular dependencies
    * between user and workspace delegates.
    * @param service the user service manager
    */
    public void setUserService( UserService service )
    {
        if( service == null ) throw new NullPointerException("supplied service is null");
        if( userService != null ) throw new IllegalArgumentException("user service already initialized");
	  if( getLogger().isDebugEnabled() ) getLogger().debug("setting user service reference");
        userService = service;
        super.manager.put("USER", userService );
    }

    //=======================================================================
    // WorkspaceService
    //=======================================================================

   /**
    * Creation of a Workspace.
    * @param name the initial name to assign to the workspace
    * @return Workspace a new Workspace object reference
    */
    public Workspace createWorkspace( String name ) 
    throws FactoryException
    {

        if( getLogger().isDebugEnabled() ) getLogger().debug( "create" );
	  if( !initialized() ) throw new IllegalStateException("server has not been initialized");
	  if( disposed() ) throw new IllegalStateException("server is in disposed state");

	  // create a storage object for the workspace

        WorkspaceStorage store = null;
        try
        {
		StandardPrincipal principal = getCurrentPrincipal( );
            long t = TimeUtils.resolveTime( clock );
            store = home.create(
			principal,
			domain.getDomainShortPID(), 
			random.getRandom(), t, t, t, name, 
			new ProducedBy()
	      );
        }
        catch (Exception e)
        {
		String error = "Unexpected failure while creating workspace storage.";
            if( getLogger().isErrorEnabled() ) getLogger().error( error, e );
            throw new FactoryException( error, e );
        }
        
	  // create an object reference to return to the client

	  Workspace workspace = null;
        try
        {
            workspace = WorkspaceHelper.narrow(
              poa.create_reference_with_id
              (
                store.get_pid(), WorkspaceHelper.id()
              )
            );
            if( getLogger().isDebugEnabled() ) getLogger().debug("reference creation");
        }
        catch (Exception e)
        {
		String error = "failed to allocate a new reference";
            if( getLogger().isErrorEnabled() ) getLogger().error( error, e );
            throw new FactoryException( error, e );
        }
        return workspace;
    }

   /**
    * Returns a reference to a Workspace given a persistent storage object identifier.
    * @param pid Workspace persistent identifier
    * @return Workspace corresponding to the PID
    * @exception NotFound if the supplied pid does not matach a know workspace
    */
    public Workspace getWorkspaceReference( byte[] pid )
    throws NotFound
    {
	  try
	  {
            StorageObject store = (StorageObject) home.get_catalog().find_by_pid( pid );
            return WorkspaceHelper.narrow(
              poa.create_reference_with_id
              (
                store.get_pid(), WorkspaceHelper.id()
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

   /**
    * Creation of a servant based on a supplied delegate.
    * @param oid the persistent object identifier
    * @return Servant the servant
    */
    public Servant createServant( byte[] oid )
    {
        return new WorkspacePOATie( 
          (WorkspaceDelegate) super.processLifecycle( new WorkspaceDelegate(), oid ), poa );
    }

   /**
    * Creation of a workspace servant.
    * @param oid the object identifier
    */
/*
    public Servant createServant( byte[] oid )
    {
	  if( !initialized() ) throw new IllegalStateException("server has not been initialized");
	  if( disposed() ) throw new IllegalStateException("server is in disposed state");
        try
        {
	      WorkspaceStorage store = (WorkspaceStorage) home.get_catalog().find_by_pid( oid );
            WorkspaceDelegate delegate = new WorkspaceDelegate( );
		Logger logger = getLogger().getChildLogger( ""+store.random() );
            delegate.enableLogging( logger );
            delegate.contextualize( createServantContext( poa, store, userService ) );
            delegate.initialize();
		return new WorkspacePOATie( delegate, poa );
        }
        catch( Exception e )
	  {
		final String error = "delegate instantiation failure.";
            if( getLogger().isFatalErrorEnabled() ) getLogger().fatalError( error, e );
            throw new org.omg.CORBA.INTERNAL( error + ", " + e );
	  }
    }

    public WorkspaceContext createServantContext( POA poa, StorageObject store, UserService us )
    {
        AbstractResourceContext rc = super.createServantContext( poa, store );
        return new DefaultWorkspaceContext( rc, us, this ); 
    }
*/

}
