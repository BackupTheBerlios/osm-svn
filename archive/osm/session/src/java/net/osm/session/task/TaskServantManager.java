/*
 * Copyright 2000 OSM SARL. All Rights Reserved.
 * 
 * This software is the proprietary information of OSM SARL.  
 * Use is subject to license terms.
 * 
 * @author  Stephen McConnell
 * @version 1.0 26/03/2001
 */

package net.osm.session.task;

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
import org.omg.Session.Workspace;

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
import org.omg.CosPersistentState.StorageHomeBase;
import org.omg.CosPersistentState.NotFound;
import org.omg.CosNotification.Property;
import org.omg.CosNotification.EventType;
import org.omg.CosNotifyComm.StructuredPushSupplier;
import org.omg.Session.Owns;
import org.omg.Session.OwnedBy;
import org.omg.Session.task_state;
import org.omg.Session.User;
import org.omg.Session.IdentifiableDomainConsumerHelper;

import net.osm.session.Executes;
import net.osm.session.ExecutedBy;
import net.osm.session.DefaultExecutes;
import net.osm.session.DefaultExecutedBy;
import net.osm.session.resource.AbstractResource;
import net.osm.session.resource.AbstractResourceServantManager;
import net.osm.session.SessionException;
import net.osm.session.ProducedBy;
import net.osm.session.user.UserService;

/**
 * <p>The <code>AbstractResourceServantManager</code> provides services supporting the 
 * creation of new <code>AbstractResource</code> instances and a servant activation
 * model for handling the redirection of incomming requests.</p>
 *
 * @author <a href="mailto:mcconnell@osm.net">Stephen McConnell</a>
 */
public class TaskServantManager extends AbstractResourceServantManager
implements TaskService
{
    //==================================================
    // static
    //==================================================

    private static final EventType[] removals = new EventType[0];
    private static final EventType[] additions = new EventType[]
    { 
	  new EventType("org.omg.session","process_state")
    };

    private static final String c_key = "task";

    //=======================================================================
    // state
    //=======================================================================

    private ServiceManager m_manager;
    private UserService m_user_service;
    private TaskStorageHome m_home;
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
            m_home = (TaskStorageHome) super.getSession().find_storage_home( 
	        "PSDL:osm.net/session/task/TaskStorageHomeBase:1.0" );
            m_user_service = (UserService) m_manager.lookup( 
              UserService.USER_SERVICE_KEY );
	  }
	  catch( Throwable throwable )
	  {
	      String error = "unable to complete supplier initialization due to a unexpected error";
		throw new TaskException( error, throwable );
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
        TaskDelegate delegate = new TaskDelegate();
        Servant servant = new TaskPOATie( delegate );
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
            return getTaskReference( (TaskStorage) store );
        }
        catch( Throwable e )
        {
            throw new TaskRuntimeException("Invalid storage type argument.", e );
        }
    }

    //=======================================================================
    // TaskService
    //=======================================================================
    
   /**
    * Creation of a Task.
    * @param name the initial name to assign to the task
    * @param decription the task description
    * @param processor the task processor
    * @return Task a new Task object reference
    */
    public Task createTask( String name, String description, org.omg.Session.AbstractResource processor ) 
    throws TaskException
    {

        //
        // get the object reference of the User corresponding to the principal
        //

	  User user = null;
	  try
	  {
	      user = m_user_service.locateUser();
	  }
	  catch( Throwable e )
	  {
		String error = "Unable to resolve implicit task owner.";
		throw new TaskException( error, e );
	  }

        //
        // create the storage object for the Task
        //

        TaskStorage store = null;
        try
        {
            store = (TaskStorage) super.createAbstractResourceStorage( m_home, name );
            store.description( description );
            store.task_state( task_state.notstarted );
            store.owned_by( new net.osm.session.OwnedBy( user ) );
            store.coordinates( new DefaultExecutedBy( processor ) );
        }
        catch( Throwable e )
        {
            final String error = "Unexpected problem while creating task.";
            if( getLogger().isErrorEnabled() ) getLogger().error( error, e );
            throw new TaskException( error, e );
        }

        //
        // associate the user, task and processing resource
        //

        Task task = getTaskReference( store );
        Executes executes = new DefaultExecutes( task );
        Owns owns = new net.osm.session.Owns( task );

        try
        {
		processor.bind( executes );
		user.bind( owns );

            StructuredPushSupplier supplier = processor.add_consumer( 
              IdentifiableDomainConsumerHelper.narrow( task )  );
		supplier.subscription_change( additions, removals );

		if( getLogger().isDebugEnabled() ) getLogger().debug( "task creation complete" );
		return task;
        }
        catch (Exception e)
        {
		String error = "failed to establish user/task/processor association";
		if( getLogger().isErrorEnabled() ) getLogger().error( error );
		try
		{
		    user.release( owns );
		}
		catch( Throwable x ){}
		try
		{
		    processor.release( executes );
		}
		catch( Throwable x ){}
		finally
		{
		    store.destroy_object();
		}
            throw new TaskException( error, e );
        }
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
        TaskStorage store = (TaskStorage) m_home.get_catalog().find_by_pid( pid );
        return getTaskReference( store );
    }

   /**
    * Return a reference to an object as an Task.
    * @param TaskStorage storage object
    * @return Task object reference
    */
    public Task getTaskReference( TaskStorage store ) 
    {
        try
        {
            return TaskHelper.narrow( 
              m_poa.create_reference_with_id( 
                store.get_pid(), 
                TaskHelper.id() ) );
        }
        catch( Throwable e )
        {
            throw new TaskRuntimeException("Could not resolve task reference.", e );
        }
    }

}
