/*
 * @(#)TaskServer.java
 *
 * Copyright 2000 OSM SARL. All Rights Reserved.
 * 
 * This software is the proprietary information of OSM SARL.  
 * Use is subject to license terms.
 * 
 * @author  Stephen McConnell
 * @version 1.0 26/03/2001
 */

package net.osm.hub.task;

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
import org.omg.CosNotification.EventType;
import org.omg.CosNotifyComm.StructuredPushSupplier;
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
import org.omg.Session.User;
import org.omg.Session.ProducedBy;
import org.omg.Session.OwnedBy;
import org.omg.Session.Owns;
import org.omg.Session.task_state;
import org.omg.CollaborationFramework.Coordinates;
import org.omg.CollaborationFramework.CoordinatedBy;
import org.omg.CommunityFramework.Criteria;
import org.omg.CollaborationFramework.Processor;

import net.osm.session.Task;
import net.osm.session.TaskHelper;
import net.osm.session.TaskPOATie;

import net.osm.hub.gateway.FactoryException;
import net.osm.hub.resource.AbstractResourceServer;
import net.osm.hub.user.UserService;
import net.osm.hub.user.UserAdministratorService;
import net.osm.hub.pss.TaskStorage;
import net.osm.hub.pss.TaskStorageHome;
import net.osm.time.TimeService;
import net.osm.time.TimeUtils;
import net.osm.realm.StandardPrincipal;

/**
 * The <code>TaskServer</code> block services supporting the 
 * creation of new <code>Task</code> instances.
 *
 * @author <a href="mailto:mcconnell@osm.net">Stephen McConnell</a>
 */

public class TaskServer extends AbstractResourceServer 
implements TaskService
{

    //==================================================
    // static
    //==================================================

    private static final EventType[] removals = new EventType[0];
    private static final EventType[] additions = new EventType[]
    { 
	  new EventType("org.omg.collaboration","state")
    };

    //==================================================
    // state
    //==================================================

    private TaskStorageHome home;
    private UserService userService;
    private UserAdministratorService userAdministratorService;

    //=================================================================
    // Composable
    //=================================================================
    
    /**
     * The compose operation handles the aggregation of dependent services:
     * <ul>
     * <li>USER: user service supporting location of a User representing the current principal
     * </ul>
     * <p>
     * @param manager The <code>ComponentManager</code> which this
     *                <code>Composer</code> uses.
     */
    public void compose( ComponentManager manager )
    throws ComponentException
    {
        if( getLogger().isDebugEnabled() ) getLogger().debug( "composition [task server]" );
	  super.compose( manager );
	  try
	  {
            userService = (UserService)manager.lookup("USER");
            userAdministratorService = (UserAdministratorService)
              manager.lookup("USER-ADMINISTRATOR");
		super.manager.put( "USER-ADMINSTRATOR", userAdministratorService );
        }
        catch( Exception e )
	  {
		final String error = "composition phase failure";
		throw new ComponentException( error, e );
	  }
        if( getLogger().isDebugEnabled() ) getLogger().debug( "composition complete [task server]" );
    }
    
    //=======================================================================
    // Initializable
    //=======================================================================
    
    public void initialize()
    throws Exception
    {
        if( getLogger().isDebugEnabled() ) getLogger().debug( "initialization [task server]" );
	  super.initialize();
        home = (TaskStorageHome) session.find_storage_home(
 		"PSDL:osm.net/hub/pss/TaskStorageHomeBase:1.0" );
        userAdministratorService.setTaskService( this );
        if( getLogger().isDebugEnabled() ) getLogger().debug( "initialization complete [task server]" );
    }
     
    //=======================================================================
    // FactoryService
    //=======================================================================

   /**
    * Creation of a task reference.
    * @param name the name of the task
    * @param criteria factory constraint (currently ignored)
    * @exception FactoryException
    * @osm.warning this method will always throw a FactoryException
    * @see #createTask
    */
    public AbstractResource create( String name, Criteria criteria ) 
    throws FactoryException
    {
	  if( getLogger().isWarnEnabled() ) getLogger().warn("attempting to create a task using criteria");
        throw new FactoryException("use createTask(String,String,Processor)");
    }
    
    //=======================================================================
    // TaskService
    //=======================================================================

   /**
    * Creation of a Task.
    * @param name the initial name to assign to the task
    * @return Task a new Task object reference
    */
    public Task createTask( String name, String description, Processor processor ) 
    throws FactoryException
    {

        if( getLogger().isDebugEnabled() ) getLogger().debug( "create" );
	  if( !initialized() ) throw new IllegalStateException("server has not been initialized");
	  if( disposed() ) throw new IllegalStateException("server is in disposed state");

	  // locate principal user to bind as task owner

	  User user = null;
	  try
	  {
	      user = userService.locateUser();
	  }
	  catch( Exception e )
	  {
		String error = "unable to resolve a User matching the principal";
		if( getLogger().isErrorEnabled() ) getLogger().error( error, e );
		throw new FactoryException( error, e );
	  }

	  // create a storage object for the task

        TaskStorage store = null;
        try
        {
		StandardPrincipal principal = getCurrentPrincipal( );
            long t = TimeUtils.resolveTime( clock );
            store = home.create(
			principal,
			domain.getDomainShortPID(), 
			random.getRandom(), t, t, t, name, 
			new ProducedBy(),
			new OwnedBy(  ), 
			new Coordinates(  ), 
			task_state.notstarted, 
			description
	      );
        }
        catch (Exception e)
        {
		String error = "Unexpected failure while creating task storage.";
            if( getLogger().isErrorEnabled() ) getLogger().error( error, e );
            throw new FactoryException( error, e );
        }
        
	  // create an object reference to return to the client

	  Task task = null;
        try
        {
            task = TaskHelper.narrow(
              poa.create_reference_with_id
              (
                store.get_pid(), org.omg.Session.TaskHelper.id()
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

	  // create an object reference to pass to the processor

	  Task callback = null;
        try
        {
            callback = TaskHelper.narrow(
              poa.create_reference_with_id
              (
                store.get_pid(), TaskHelper.id()
              )
            );
            if( getLogger().isDebugEnabled() ) getLogger().debug("callback reference creation");
        }
        catch (Exception e)
        {
		String error = "failed to allocate a new reference";
            if( getLogger().isErrorEnabled() ) getLogger().error( error, e );
            throw new FactoryException( error, e );
        }

        // establish relationships between user, task and processor

	  CoordinatedBy coordinatedBy = new CoordinatedBy( callback );
        Coordinates coordinates = new Coordinates( processor );
        Owns owns = new Owns( task );
        OwnedBy ownedBy = new OwnedBy( user );
        try
        {
		task.bind( coordinates );
		task.bind( ownedBy );
		user.bind( owns );
		processor.bind( coordinatedBy );

            StructuredPushSupplier supplier = processor.add_consumer( task );
		supplier.subscription_change( additions, removals );

		if( getLogger().isDebugEnabled() ) getLogger().debug( "task creation complete" );
		return task;
        }
        catch (Exception e)
        {
		String error = "failed to establish coordinatedBy relationship";
		if( getLogger().isErrorEnabled() ) getLogger().error( error, e );
		try
		{
		    user.release( owns );
		}
		catch( Throwable x ){}
		try
		{
		    processor.release( coordinatedBy );
		}
		catch( Throwable x ){}
		finally
		{
		    store.destroy_object();
		}
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
        return new TaskPOATie( 
          (TaskDelegate) super.processLifecycle( new TaskDelegate(), oid ), poa );
    }

   /**
    * Returns a reference to a Task given a persistent storage object identifier.
    * @param pid Task persistent identifier
    * @return Task corresponding to the PID
    * @exception NotFound if the supplied pid does not matach a know Task
    */
    public Task getTaskReference( byte[] pid )
    throws NotFound
    {
	  try
	  {
            StorageObject store = (StorageObject) home.get_catalog().find_by_pid( pid );
            return TaskHelper.narrow(
              poa.create_reference_with_id
              (
                store.get_pid(), TaskHelper.id()
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
}
