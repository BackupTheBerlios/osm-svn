

package net.osm.session.user;

import java.util.Vector;

import org.apache.avalon.framework.context.Context;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.logger.Logger;

import org.omg.CORBA.Any;
import org.omg.CORBA.ORB;
import org.omg.CORBA.TypeCode;
import org.omg.CORBA.BooleanHolder;
import org.omg.PortableServer.POA;
import org.omg.CosNaming.NameComponent;
import org.omg.CosLifeCycle.NoFactory;
import org.omg.CosLifeCycle.NotRemovable;
import org.omg.CosCollection.AnySequenceHolder;
import org.omg.CosTime.TimeService;
import org.omg.CosPersistentState.NotFound;
import org.omg.CosNotification.StructuredEvent;
import org.omg.CosNotification.EventType;
import org.omg.CosNotification.EventHeader;
import org.omg.CosNotification.FixedEventHeader;
import org.omg.Session.AlreadyConnected;
import org.omg.Session.AbstractResource;
import org.omg.Session.AlreadyConnected;
import org.omg.Session.Administers;
import org.omg.Session.AdministersHelper;
import org.omg.Session.AdministeredBy;
import org.omg.Session.Accesses;
import org.omg.Session.AccessesHelper;
import org.omg.Session.AccessedBy;
import org.omg.Session.Access;
import org.omg.Session.AccessHelper;
import org.omg.Session.ComposedOf;
import org.omg.Session.Consumes;
import org.omg.Session.ConsumedBy;
import org.omg.Session.connect_state;
import org.omg.Session.Desktop;
import org.omg.Session.DesktopHelper;
import org.omg.Session.IsPartOf;
import org.omg.Session.Link;
import org.omg.Session.LinkIterator;
import org.omg.Session.LinkIteratorPOA;
import org.omg.Session.LinkIteratorPOATie;
import org.omg.Session.LinksHolder;
import org.omg.Session.MessageIteratorHolder;
import org.omg.Session.MessagesHolder;
import org.omg.Session.Message;
import org.omg.Session.MessageHelper;
import org.omg.Session.Owns;
import org.omg.Session.OwnsHelper;
import org.omg.Session.OwnedBy;
import org.omg.Session.Privilege;
import org.omg.Session.PrivilegeHelper;
import org.omg.Session.Produces;
import org.omg.Session.ProcessorConflict;
import org.omg.Session.ProducedBy;
import org.omg.Session.ResourceUnavailable;
import org.omg.Session.SemanticConflict;
import org.omg.Session.Task;
import org.omg.Session.TaskHelper;
import org.omg.Session.TasksHolder;
import org.omg.Session.TaskIteratorHolder;
import org.omg.Session.TaskIterator;
import org.omg.Session.TaskIteratorPOA;
import org.omg.Session.TaskIteratorPOATie;
import org.omg.Session.Workspace;
import org.omg.Session.WorkspaceHelper;
import org.omg.Session.WorkspacesHolder;
import org.omg.Session.WorkspaceIteratorHolder;
import org.omg.Session.WorkspaceIterator;
import org.omg.Session.WorkspaceIteratorPOA;
import org.omg.Session.WorkspaceIteratorPOATie;
import org.omg.CosPropertyService.PropertyDef;
import org.omg.CosPropertyService.PropertySetDef;
import org.omg.CosPropertyService.PropertySetDefOperations;
import org.omg.CosPropertyService.ConstraintNotSupported;
import org.omg.CosPropertyService.MultipleExceptions;
import org.omg.CosPropertyService.ConflictingProperty;
import org.omg.CosPropertyService.UnsupportedTypeCode;
import org.omg.CosPropertyService.UnsupportedProperty;
import org.omg.CosPropertyService.UnsupportedMode;
import org.omg.CosPropertyService.ReadOnlyProperty;
import org.omg.CosPropertyService.InvalidPropertyName;
import org.omg.CosPropertyService.PropertyNotFound;
import org.omg.CosPropertyService.FixedProperty;
import org.omg.CosPropertyService.PropertyException;
import org.omg.CosPropertyService.ExceptionReason;
import org.omg.CosPropertyService.Property;
import org.omg.CosPropertyService.PropertyMode;
import org.omg.CosPropertyService.PropertyModeType;
import org.omg.CosPropertyService.PropertyModesHolder;
import org.omg.CosPropertyService.PropertyTypesHolder;
import org.omg.CosPropertyService.PropertyNamesHolder;
import org.omg.CosPropertyService.PropertiesIterator;
import org.omg.CosPropertyService.PropertiesIteratorHolder;
import org.omg.CosPropertyService.PropertiesHolder;
import org.omg.CosPropertyService.PropertyNamesIteratorHolder;
import org.omg.CosPropertyService.PropertyNamesIterator;
import org.omg.CosPropertyService.PropertyDefsHolder;
import org.omg.CosPropertyService.PropertyDefHolder;

import org.apache.orb.util.LifecycleHelper;
import org.apache.pss.ActivatorService;
import org.apache.pss.util.Incrementor;
import org.apache.pss.DefaultStorageContext;
import org.apache.time.TimeUtils;

import net.osm.adapter.Adapter;
import net.osm.chooser.Chooser;
import net.osm.session.HomeService;
import net.osm.session.SessionSingleton;
import net.osm.session.CannotTerminate;
import net.osm.session.task.TaskService;
import net.osm.session.task.TaskIteratorDelegate;
import net.osm.session.user.UserService;
import net.osm.session.desktop.DesktopService;
import net.osm.session.workspace.WorkspaceService;
import net.osm.session.workspace.WorkspaceIteratorDelegate;
import net.osm.session.message.DefaultSystemMessage;
import net.osm.session.message.SystemMessage;
import net.osm.session.message.SystemMessageHelper;
import net.osm.session.message.SystemMessagesHolder;
import net.osm.session.message.SystemMessageIterator;
import net.osm.session.message.SystemMessageIteratorPOA;
import net.osm.session.message.SystemMessageIteratorPOATie;
import net.osm.session.message.SystemMessageIteratorDelegate;
import net.osm.session.message.MessageStorageHome;
import net.osm.session.message.MessageStorage;
import net.osm.session.resource.AbstractResourceDelegate;
import net.osm.session.linkage.LinkIteratorDelegate;
import net.osm.session.linkage.LinkStorage;
import net.osm.properties.PropertiesService;
import net.osm.properties.PropertySetDefDelegate;
import net.osm.properties.PropertySetDefStorage;
import net.osm.realm.StandardPrincipal;
import net.osm.sps.StructuredEventUtilities;
import net.osm.list.LinkedList;
import net.osm.list.Iterator;
import net.osm.list.NoEntry;

/**
 * Workspace defines private and shared places where resources, including
 * Task and Session objects, may be contained. Workspaces may contain
 * Workspaces.  The support for sharing and synchronizing the use of
 * objects available in Workspaces is provided by the objects and their
 * managers. Each Workspace may contain any collection of private and
 * shared objects that the objects and their managers provide access to,
 * and control use of.
 */

public class UserDelegate extends AbstractResourceDelegate implements UserOperations
{

    //======================================================================
    // static
    //======================================================================
    
   /**
    * The event type for a 'property' event.
    */
    public static final EventType propertyEventType = SessionSingleton.propertyEventType;
    
   /**
    * The event type for a 'connected' event.
    */
    public static final EventType connectedEventType = SessionSingleton.connectedEventType;

   /**
    * The event type for a 'enqueue' event.
    */
    public static final EventType enqueueEventType = SessionSingleton.enqueueEventType;

   /**
    * The event type for a 'dequeue' event.
    */
    public static final EventType dequeueEventType = SessionSingleton.dequeueEventType;

   /**
    * Incrementor for the creation of storage object identifiers for new 
    * message refernces.
    */
    private static final Incrementor messageIncrementor = Incrementor.create("MESSAGE");

    //======================================================================
    // state
    //======================================================================
    
   /**
    * SeriveManager reference.
    */
    private ServiceManager m_manager;
    
   /**
    * Storage object representing this User.
    */
    private UserStorage m_store;

   /**
    * Object reference to this User.
    */
    private User m_user;

   /**
    * Object reference to the User's Desktop.
    */
    private Desktop m_desktop;

   /**
    * Internal reference to the workspace service.
    */
    private WorkspaceService m_workspace_service;

   /**
    * Internal reference to the desktop service.
    */
    private DesktopService m_desktop_service;

   /**
    * Internal reference to the user service.
    */
    private UserService m_user_service;

   /**
    * Internal reference to the task service.
    */
    private TaskService m_task_service;

   /**
    * Internal reference to the properties service.
    */
    private PropertiesService m_properties_service;

   /**
    * Internal reference to the session service.
    */
    private HomeService m_sessionService;

   /**
    * The PropertySetDef delagate to which property operation as delagated.
    */
    private PropertySetDefDelegate m_psd_delegate;

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
        m_sessionService = (HomeService) manager.lookup(
          HomeService.SESSION_SERVICE_KEY );
        m_user_service = (UserService) manager.lookup(
          ActivatorService.ACTIVATOR_KEY );
        m_desktop_service = (DesktopService) manager.lookup( 
          DesktopService.DESKTOP_SERVICE_KEY );
        m_task_service = (TaskService) manager.lookup( 
          TaskService.TASK_SERVICE_KEY );
        m_properties_service = (PropertiesService) manager.lookup( 
          PropertiesService.PROPERTY_SERVICE_KEY );
    }
    
    //=======================================================================
    // Initializable
    //=======================================================================
    
   /**
    * Signal completion of contextualization phase and readiness to serve requests.
    * @exception Exception if the servant cannot complete normal initialization
    */
    public void initialize()
    throws Exception
    {
        super.initialize();

        m_store = (UserStorage) super.getStorageObject();
        
	  setUserReference( 
          UserHelper.narrow( 
            m_user_service.getUserReference( m_store ) ) );

        //
        // prepare the PropertySetDef delegate
	  //

        PropertySetDefStorage property_store = null;
	  try
	  {
            byte[] psd_pid = m_store.property_set();
            property_store = m_properties_service.getPropertySetDefStorage( psd_pid );
	      m_psd_delegate = new PropertySetDefDelegate(); 
            Logger log = getLogger().getChildLogger( "properties" );
            DefaultStorageContext context = new DefaultStorageContext( null, property_store );
            context.put("scope", m_store.get_pid() );
            context.makeReadOnly();
            LifecycleHelper.pipeline( m_psd_delegate, log, context, null, m_manager );
		if( getLogger().isDebugEnabled() ) getLogger().debug("property delegate established");
	  }
	  catch( Throwable t )
	  {
		String error = "failed to establish PropertySetDefDelegate";
		throw new UserException( error, t );
	  }
    }
    
    //==================================================
    // Vulnerable implementation
    //==================================================

   /**
    * Destroys the persistent identity of this object. An 
    * implementation is responsible for disposing of references 
    * to external resources on completion of termiation actions. 
    */
    public void terminate( ) throws CannotTerminate
    {
	  synchronized( m_store )
        {
		if( !expendable() ) throw new CannotTerminate(
               "resource is in use");

            if( getLogger().isDebugEnabled() ) getLogger().debug(
               "terminate ");

		//
		// remove the user's desktop
		//
            
		try
		{
		    get_desktop().remove();
		}
		catch( Exception e)
		{
		    String error = "failed to remove the user desktop";
		    if( getLogger().isWarnEnabled() ) getLogger().warn( error, e );
		    throw new CannotTerminate( error, e );
		}

		//
		// release the accesses associations
		//
		
            try
            {
                LinkedList accesses = m_store.accesses();
                retractEveryLink( 
			m_user, accesses, "accesses" );
            }
            catch (Exception e)
            {
                String error = "Unexpected error during retraction of access association";
                if( getLogger().isErrorEnabled() ) getLogger().error( error, e );
                throw new CannotTerminate( error, e );
            }

		//
		// destroy messages
		// WARNING: Implementation does not currently destroy folders
		//

            try
            {
                Iterator iterator = m_store.messages().iterator();
                while( iterator.has_next() )
                {
			  iterator.next().destroy_object();
			  iterator.remove();
                }
            }
            catch( Exception e )
            {
                String error = "unexpected error while destroying messages";
                if( getLogger().isErrorEnabled() ) getLogger().error( error, e );
            }

            //
	      // release owns associations
	      // (removes all Tasks owned by this User)
	      //

		boolean success = true;
	      try
            {
		    Vector ownsVector = new Vector();
                Iterator iterator = m_store.owns().iterator();
                while( iterator.has_next() )
                {
                    LinkStorage s = (LinkStorage) iterator.next();
		        ownsVector.addElement( s.link().resource() );
		    }

		    int j = ownsVector.size();
		    for( int i=0; i<j; i++ )
		    {
                    if( getLogger().isDebugEnabled() ) getLogger().debug( "removing task " + i+1 );
		        org.omg.CORBA.Object obj = (org.omg.CORBA.Object) ownsVector.elementAt( i );
		        Task task = TaskHelper.narrow( obj );
			  try
			  {
                        if( getLogger().isDebugEnabled() ) getLogger().debug( 
				  "removing task " + i );
		            task.remove();
		        }
                    catch( Throwable e )
	              {
		            String warning =  "failed to remove a task";
		            if( getLogger().isWarnEnabled() ) getLogger().warn( warning, e );
                        success = false;
                    }
		    }
	      }
            catch( Throwable e)
            {
                String s = "unexpected error while releasing tasks";
                e.printStackTrace();
                if( getLogger().isErrorEnabled() ) getLogger().error( s, e );
            }

		//
		// release any privilege associations
		//
		
            try
            {
                LinkedList privileges = m_store.privileges();
                retractEveryLink( 
			m_user, privileges, "priviliges" );
            }
            catch (Exception e)
            {
                String error = "Unexpected error during retraction of access association";
                if( getLogger().isErrorEnabled() ) getLogger().error( error, e );
                throw new CannotTerminate( error, e );
            }

		if( !success ) throw new CannotTerminate(
              "failed to remove one or more Tasks owned by this User");

	      super.terminate();
	  }
    }

   /**
    * Clean up state members.
    */
    public synchronized void dispose()
    {
	  this.m_psd_delegate.dispose();
        this.m_psd_delegate = null;
        this.m_store = null;
        this.m_user = null;
        this.m_desktop = null;
	  super.dispose();
    }

    // =====================================================================
    // Mailbox
    // =====================================================================

    /**
     * Returns the storage home for MessageStorage types.
     * @return MessageStorageHome the storage home for messages
     * @exception NotFound if the storage home cannot be found
     */
    public MessageStorageHome getMessageHome() throws NotFound
    {
        return ( MessageStorageHome ) m_store.get_storage_home().get_catalog().find_storage_home( 
			"PSDL:osm.net/session/message/MessageStorageHomeBase:1.0" );
    }

   /**
    * Place a message into the user's message INTRAY folder.
    * @param message the system message to enqueue
    */
    public void enqueue( SystemMessage message )
    {
 	  if( getLogger().isDebugEnabled() ) getLogger().debug("enqueue");
	  try
	  {
		long id = messageIncrementor.increment();
		message.header.identifier = id;
		m_store.messages().add( getMessageHome().create( id, message ) );
		post( newEnqueueEvent( message ) );
        }
        catch( Exception e )
        {
            final String error = "failed to enqueue a message";
            throw new UserRuntimeException( error, e );
        }
    }

   /**
    * Removes a message from the INTRAY and issues a dequeue event to 
    * attached subscribers.
    * @param identifier the system message identifier of the message to dequeue
    */
    public void dequeue( long identifier )
    {
 	  if( getLogger().isDebugEnabled() ) getLogger().debug("dequeue");
	  try
	  {
		MessageStorage s = getMessageHome().find_by_identifier( identifier );
		m_store.messages().remove( s );
		s.destroy_object();
        }
        catch( NotFound e )
        {
        }
        catch( Throwable e )
        {
            final String error = "failed to dequeue a message";
            throw new UserRuntimeException( error, e );
        }
        finally
        {
            post( newDequeueEvent( identifier ) );
        }
        
    }

   /**
    * The get_messages operation returns the set of messages in the user's inbasket.
    * @param max_number the maximum number of messages to pack into the messages
    *  holder
    * @param message a message sequence holder into which a sequence of messages will
    *   be packed depending on the max_number parameter
    * @return SystemMessageIterator a system message iterator holding the 
    *   remaining messages
    */
    public SystemMessageIterator get_messages( int max_number, SystemMessagesHolder messages )
    {
        LinkedList list = m_store.messages();

        synchronized( list )
        {
            
            getLogger().debug("get_messages");
            
            SystemMessage[] seq = null;
            int n = list.size();

            if( ( n == 0 ) || ( n < max_number ) )
            {
                seq = new SystemMessage[n];
            }
            else
            {
                seq = new SystemMessage[max_number];
            }

            Iterator iterator = list.iterator();
            try
            {
                for( int i=0; i<seq.length; i++ )
                {
                    MessageStorage ms = (MessageStorage) iterator.next();
			  seq[i] = ms.system_message();
                }
                messages.value = seq;
            }
            catch (Exception e)
            {
                String error = "Unexpected error while preparing message sequence.";
                throw new UserRuntimeException( error, e );
            }

		//
		// package and return the iterator
            //

            try
            {
                SystemMessageIteratorDelegate delegate = 
				new SystemMessageIteratorDelegate( iterator );
	          SystemMessageIteratorPOA servant = new SystemMessageIteratorPOATie( delegate );
		    return servant._this( super.getORB() );
		}
		catch( Exception e )
		{
		    final String error = "Message iterator construction failure.";
                throw new UserRuntimeException( error, e );
            }
        }
    }

    // =====================================================================
    // Structured Events
    // =====================================================================

   /**
    * Creation of a new 'connected' StructuredEvent.
    *
    * @param source User that the connected event is from
    * @param connected boolean connected state of the user
    * @return 'connected' structured event
    */
    public StructuredEvent newConnectedEvent( boolean connected )
    {
        org.omg.CosNotification.Property sourceProp = super.createSourceProperty( );
        Any b = ORB.init().create_any();
        b.insert_boolean( connected );
        org.omg.CosNotification.Property connectProp = 
          new org.omg.CosNotification.Property( "connected", b );
        return StructuredEventUtilities.createEvent( connectedEventType, 
		new org.omg.CosNotification.Property[]
		{ 
			StructuredEventUtilities.timestamp( ), 
			sourceProp, 
			connectProp 
		}
	  );
    }

   /**
    * Creation of a new 'enqueue' StructuredEvent.
    *
    * @param source User that the message has been enqueued against
    * @param message the system message that was added to the queue
    * @return StructuredEvent the 'message' structured event
    * @since 3.1
    */
    public StructuredEvent newEnqueueEvent( SystemMessage message )
    {
        org.omg.CosNotification.Property sourceProp = super.createSourceProperty();
        Any any = ORB.init().create_any();
        SystemMessageHelper.insert( any, message );
        org.omg.CosNotification.Property messageProp = 
          new org.omg.CosNotification.Property( "message", any );
        return StructuredEventUtilities.createEvent( enqueueEventType, 
		new org.omg.CosNotification.Property[]
		{ 
			StructuredEventUtilities.timestamp(), 
			sourceProp, 
			messageProp 
		}
	  );
    }

   /**
    * Creation of a new 'dequeue' StructuredEvent.
    *
    * @param source User that the message has been enqueued against
    * @param identifier the system message storage identifier
    * @return StructuredEvent the 'dequeue' structured event
    * @since 3.1
    */
    public StructuredEvent newDequeueEvent( long identifier )
    {
        org.omg.CosNotification.Property sourceProp = super.createSourceProperty();
        Any any = ORB.init().create_any();
	  any.insert_longlong( identifier );
        org.omg.CosNotification.Property id = 
          new org.omg.CosNotification.Property( "identifier", any );
        return StructuredEventUtilities.createEvent( dequeueEventType, 
		new org.omg.CosNotification.Property[]
		{ 
			StructuredEventUtilities.timestamp( ), 
			sourceProp, 
			id 
		}
	  );
    }


    // =====================================================================
    // UserOperations
    // =====================================================================

   /**
    * Returns the connected state of the User.
    */
    public connect_state connectstate()
    {
        touch( m_store );
        if( this.m_store.connected() ) return connect_state.connected;
        return connect_state.disconnected;
    }
    
   /**
    * Connect establishes the User session for clients, such as desktop
    * managers to present Workspaces and the computing environment.
    * Successful completion of this operation will result in the session
    * state being changed to connected.
    * @exception  <code>AlreadyConnected</code> if the User is already connected
    */ 
    public void connect()
    throws AlreadyConnected
    {
        synchronized( m_store )
        {
            touch( m_store );
            if( m_store.connected() ) throw new AlreadyConnected();
            if( getLogger().isDebugEnabled() )
            {
                getLogger().debug("connect");
            }
            m_store.connected( true );
		post( newConnectedEvent( true ));
            modify( m_store );
        }
    }
    
   /**
    * Clients disconnects with the User
    * and the computing environment to close the session. When complete
    * the session state is set to disconnected.
    * @exception  <code>NotConnected</code>
    *   if the User is already disconnected
    */
    public void disconnect()
    throws org.omg.Session.NotConnected
    {
        synchronized( m_store )
        {
            touch( m_store );
            if( !m_store.connected() ) throw new org.omg.Session.NotConnected();
            if( getLogger().isDebugEnabled() )
            {
                getLogger().debug("disconnect");
            }
            m_store.connected( false );
		post( newConnectedEvent( false ));
            modify( m_store );
        }
    }
    
   /**
    * The enqueue_message operation is used to notify a user of a
    * message.  Messages, passed as arguments to this operation, can be created
    * though a MessageFactory accessible under the respective user's 
    * find_factories operation.
    *
    * @param message Message to enqueue
    * @see net.osm.session.user.UserDelegate#enqueue( SystemMessage ) enqueue
    */
    public void enqueue_message( org.omg.Session.Message message )
    {
        throw new org.omg.CORBA.NO_IMPLEMENT();
    }
    
   /**
    * Removes a message from the queue of messages for this User. This method is 
    * not implemented due to security restrictions (CORBA 2 security requirement).  
    * A supplimentary interface can be provided by an implementation
    * to support message dequing and other message management functions.
    *
    * @param  message Message the Message to remove from the User message queue.
    * @exception org.omg.CORBA.NO_IMPLEMENT in all cases
    * @deprecated
    */
    public void dequeue_message(org.omg.Session.Message message)
    {
        throw new org.omg.CORBA.NO_IMPLEMENT();
    }
    
   /**
    * Returns an iterator and sequence of messages queued for this user.
    * @param  max_number long the maximum number of Message instances to
    * include in the returned Messages sequence.
    *
    * @param  tasks MessagesHolder containing a sequence of Message instances of
    * a length no greater than max_number.
    * @param  wsit MessageIterator an iterator of the Message instances
    * @exception org.omg.CORBA.NO_IMPLEMENT in all cases
    * @see net.osm.session.user.UserDelegate#get_messages
    * @deprecated
    */
    public void list_messages(int max_number, MessagesHolder messages, MessageIteratorHolder messageit)
    {
        throw new org.omg.CORBA.NO_IMPLEMENT();
    }

   /**
    * Task creation includes the initial specification of "who", "what", and
    * "how" for the Task. The User instance of this interface is "who", the
    * "what" is the AbstractResource data to update or produce, and the "how"
    * is the AbstractResource process (workflow, tool, etc.) used.
    *
    * This implementation assumes that the data argument (if not null) is bound 
    * both as a consumed and produced (i.e. updated) resource, otherwise the
    * client can add additional consumed and produced resources using the bind 
    * operation.
    *
    * @return  Task the new Task instance bound to the supplied processor and data
    * @param  process AbstractResource acting as a Processor
    * @param  data AbstractResource data to update or produce
    */
    public Task create_task(String name, AbstractResource process, AbstractResource data)
    {
	  touch( m_store );
	  try
	  {
            //####
            // ISSUE: The current implementation is independent of the processor package
            // however, given this interface (standard) we cannot resolve a task 
            // description.  One possibility to to treat the supplied name as a special 
            // string with a delimiter seperating the desired task name from the task 
            // description.
            //####
 
		Task task = m_task_service.createTask( name, process.name(), process );
	      if( data == null ) return task;

		ConsumedBy consumedBy = new net.osm.session.ConsumedBy( task );
		Consumes consumes = new net.osm.session.Consumes( data );
		try
		{
                data.bind( consumedBy );
		    task.bind( consumes );
		    return task;
		}
            catch (Exception e)
            {
                String s = "failed to establish consumption association";
                if( getLogger().isErrorEnabled() ) getLogger().error(s,e);
		    try
		    {
			  data.release( consumedBy );
		    }
		    catch( Exception x ){}
		    try
		    {
			  task.remove( );
		    }
		    catch( Exception x ){}
                throw new UserException( "unexpected error while creating task", e );
		}
	  }
        catch( Throwable t )
	  {
		String error = "Unable to create new task.";
		throw new UserRuntimeException( error, t );
	  }
    }
    
   /**
    * Returns an iterator and sequence of Tasks owned by this user.
    * @param  max_number long the maximum number of Task instances to
    * include in the returned Tasks sequence.
    * @param  tasks Session::Tasks a sequence of Task instances of
    * a length no greater than max_number.
    * @param  wsit TaskIterator an iterator of the Task instances
    */
    public void list_tasks(int max_number, TasksHolder tasks, TaskIteratorHolder taskit)
    {
	  TaskIterator iterator = null;
        LinkedList list = m_store.owns();
        synchronized( list )
        {
	      try
	      {
	          TaskIteratorDelegate delegate = new TaskIteratorDelegate( list.iterator() );
	          TaskIteratorPOA servant = new TaskIteratorPOATie( delegate );
		    iterator = servant._this( super.getORB() );

		    AnySequenceHolder anysHolder = new AnySequenceHolder();
		    BooleanHolder booleanHolder = new BooleanHolder();
		    if( iterator.retrieve_next_n_elements( max_number, anysHolder, booleanHolder ) )
		    {
			  int k = anysHolder.value.length;
			  Task[] sequence = new Task[k];
		        for( int i=0; i<k; i++ )
			  {
				sequence[i] = TaskHelper.extract( anysHolder.value[i]);
		        }
			  tasks.value = sequence;
		    }
		    else
		    {
		        tasks.value = new Task[0];
		    }
		}
		catch( Exception e )
		{
		    String error = "Unable to establish TaskIterator." ;
		    throw new UserRuntimeException( error, e );
		}
        }
	  taskit.value = iterator;
        touch( m_store );
    }
    
    
   /**
    * Returns the Desktop that links one User to many Workspaces. Workspaces
    * may have many Users linked via Desktop.
    * @return  Desktop the User's private Desktop.
    */
    public Desktop get_desktop()
    {
	  touch( m_store );
	  if( m_desktop != null ) return m_desktop;
	  try
	  {
            m_desktop = m_desktop_service.getDesktopReference( m_store.local_desktop() );
            return m_desktop;
	  }
	  catch( Throwable e )
	  {
		String error = "Unable to resolve desktop from internal reference.";
		throw new UserRuntimeException( error, e );
	  }
    }
    
   /**
    * Users may create Workspaces with this operation. As Workspace may be shared
    * implementations must set access control lists to the Users sequence specified
    * with the create operation. On creation of a new workspace the principal User
    * creating the new instance is implicitly associated with the workspace as
    * administrator.  As administrator, the User holds rights enabling the modification
    * of the access control list through bind, replace and release operations.
    *
    * @return  Workspace administrated by this User.
    * @param  accesslist Users
    * a sequence of user instances to associate in the Workspace ACL.
    */
    public Workspace create_workspace(String name, org.omg.Session.User[] accesslist)
    {

        Workspace workspace = null;
        try
        {
            workspace = m_workspace_service.createWorkspace( name );
	  }
	  catch( Exception e )
	  {
		String error = "Unable to create workspace.";
            throw new UserRuntimeException( error, e );
	  }

        IsPartOf isPartOf = new net.osm.session.IsPartOf( get_desktop() );
        ComposedOf composedOf = new net.osm.session.ComposedOf( workspace );
	  Administers administers = new net.osm.session.Administers( workspace );
        AdministeredBy administeredBy = new net.osm.session.AdministeredBy( m_user );
        try
        {
		workspace.bind( isPartOf );
	      get_desktop().bind( composedOf );
	      bind( administers );
	      workspace.bind( administeredBy );
		return workspace;
        }
	  catch( Throwable e )
	  {
		String error = "failed to configure workspace";
            if( getLogger().isErrorEnabled() ) getLogger().error( error, e );
		try
	      {
		    get_desktop().release( composedOf );
		}
		catch( Throwable t ){}
		try
	      {

		    release( administers );
		}
		catch( Throwable t ){}
		try
	      {
		    workspace.remove();
		}
		catch( Throwable t ){}
            throw new UserRuntimeException( error, e );
	  }
    }
    
   /**
    * Returns an interator and limited length sequence of Workspaces created by
    * this User.
    *
    * @param  max_number long the maximum number of Workspace instances to
    * include in the returned Workspace sequence.
    * @param  tasks Session::Workspace a sequence of Workspace instances of
    * a length no greater than max_number that are administered by this User.
    * @param  wsit WorkspaceIterator an iterator of the Workspace instances
    */
    public void list_workspaces(int max_number, WorkspacesHolder workspaces, WorkspaceIteratorHolder wsit)
    {
	  WorkspaceIterator iterator = null;
        LinkedList list = m_store.accesses();
        synchronized( list )
        {
	      try
	      {
	          WorkspaceIteratorDelegate delegate = 
                  new WorkspaceIteratorDelegate( list.iterator() );
	          WorkspaceIteratorPOA servant = new WorkspaceIteratorPOATie( delegate );
		    iterator = servant._this( super.getORB() );

		    AnySequenceHolder anysHolder = new AnySequenceHolder();
		    BooleanHolder booleanHolder = new BooleanHolder();
		    if( iterator.retrieve_next_n_elements( max_number, anysHolder, booleanHolder ) )
		    {
			  int k = anysHolder.value.length;
			  Workspace[] sequence = new Workspace[k];
		        for( int i=0; i<k; i++ )
			  {
				sequence[i] = WorkspaceHelper.extract( anysHolder.value[i]);
		        }
			  workspaces.value = sequence;
		    }
		    else
		    {
		        workspaces.value = new Workspace[0];
		    }
		}
		catch( Exception e )
		{
		    String error = "failed to establish WorkspaceIterator" ;
		    throw new UserRuntimeException( error, e );
		}
        }
	  wsit.value = iterator;
        touch( m_store );
    }
        
    // ==========================================================================
    // AbstractPerson implementation
    // ==========================================================================
    
    //
    // PropertySet implementation
    //
    
   /**
    * Will modify or add a property to the PropertySet. If the property already exists, then
    * the property type is checked before the value is overwritten. If the property does not
    * exist, then the property is added to the PropertySet.<p>
    * To change the any TypeCode portion of the property_value of a property, a client
    * must first delete_property, then invoke the define_property.<p>
    */
    public void define_property( String property_name, Any property_value)
      throws InvalidPropertyName, ConflictingProperty, UnsupportedTypeCode,
        UnsupportedProperty, ReadOnlyProperty
    {
        m_psd_delegate.define_property( property_name, property_value );
	  modify( m_store );
    }
    
   /**
    * Will modify or add each of the properties in Properties parameter to the
    * PropertySet. For each property in the list, if the property already exists, then the
    * property type is checked before overwriting the value. If the property does not exist,
    * then the property is added to the PropertySet.<p>
    * This is a batch operation that returns the MultipleExceptions exception if any define
    * operation failed.
    */
    public void define_properties( Property[] nproperties)
    throws MultipleExceptions
    {
        m_psd_delegate.define_properties( nproperties );
	  modify( m_store );
    }
    
   /**
    * Returns the current number of properties associated with this PropertySet.
    */
    public int get_number_of_properties()
    {
	  touch( m_store );
        return m_psd_delegate.get_number_of_properties( );
    }
    
   /**
    * Returns all of the property names currently defined in the PropertySet. If the
    * PropertySet contains more than how_many property names, then the remaining
    * property names are put into the PropertyNamesIterator.
    */
    public void get_all_property_names(int how_many, 
      PropertyNamesHolder property_names, PropertyNamesIteratorHolder rest)
    {
	  touch( m_store );
        m_psd_delegate.get_all_property_names( how_many, property_names, rest );
    }
    
   /**
    * Returns the value of a property in the PropertySet.
    */
    public org.omg.CORBA.Any get_property_value( String property_name )
    throws PropertyNotFound, InvalidPropertyName
    {
	  touch( m_store );
        return m_psd_delegate.get_property_value( property_name );
    }
    
   /**
    * Returns the values of the properties listed in property_names.
    * When the boolean flag is true, the Properties parameter contains valid values for all
    * requested property names. If false, then all properties with a value of type tk_void
    * may have failed due to PropertyNotFound or InvalidPropertyName.<p>
    * A separate invocation of get_property for each such property name is necessary to
    * determine the specific exception or to verify that tk_void is the correct any
    * TypeCode for that property name.<p>
    * This approach was taken to avoid a complex, hard to program structure to carry mixed
    * results.
    */
    public boolean get_properties( String[] property_names, PropertiesHolder nproperties)
    {
	  touch( m_store );
        return m_psd_delegate.get_properties( property_names, nproperties );
    }
    
   /**
    * Returns all of the properties defined in the PropertySet. If more than how_many
    * properties are found, then the remaining properties are returned in an iterator.
    */
    public void get_all_properties(
      int how_many, PropertiesHolder nproperties, PropertiesIteratorHolder rest)
    {
        m_psd_delegate.get_all_properties( how_many, nproperties, rest);
	  touch( m_store );
    }
    
   /**
    * Deletes the specified property if it exists from a PropertySet.
    */
    public void delete_property( String property_name )
    throws PropertyNotFound, InvalidPropertyName, FixedProperty
    {
        m_psd_delegate.delete_property( property_name );
	  modify( m_store );
    }
    
   /**
    * Deletes the properties defined in the property_names parameter.
    * This is a batch operation that returns the MultipleExceptions exception
    * if any delete failed.
    */
    public void delete_properties( String[] property_names )
    throws MultipleExceptions
    {
        m_psd_delegate.delete_properties( property_names );
	  modify( m_store );
    }
    
   /**
    * Variation of delete_properties. Applies to all properties.
    * Since some properties may be defined as fixed property types, it may be that not all
    * properties are deleted. The boolean flag is set to false to indicate that not all properties
    * were deleted.<p>
    * A client could invoke get_number_of_properties to determine how many
    * properties remain. Then invoke get_all_property_names to extract the property
    * names remaining. A separate invocation of delete_property for each such property
    * name is necessary to determine the specific exception.<p>
    * Note ? If the property is in a PropertySetDef, then the set_mode operation could be
    * invoked to attempt to change the property mode to something other than fixed before
    * using delete_property.
    * This approach was taken to avoid the use of an iterator to return an indeterminate
    * number of exceptions.
    */
    public boolean delete_all_properties()
    {
	  modify( m_store );
        return m_psd_delegate.delete_all_properties();
    }
    
   /**
    * The is_property_defined operation returns true if the property is defined in the
    * PropertySet, and returns false otherwise.
    */
    public boolean is_property_defined( String property_name )
    throws InvalidPropertyName
    {
	  touch( m_store );
        return m_psd_delegate.is_property_defined( property_name );
    }
    
    
    //
    // PropertySetDef implementation
    //
    
   /**
    * Indicates which types of properties are supported by this PropertySet. If the output
    * sequence is empty, then there is no restrictions on the any TypeCode portion of the
    * property_value field of a Property in this PropertySet, unless the
    * get_allowed_properties output sequence is not empty.
    * For example, a PropertySet implementation could decide to only accept properties that
    * had any TypeCodes of tk_string and tk_ushort to simplify storage processing and
    * retrieval.
    */
    public void get_allowed_property_types( PropertyTypesHolder property_types )
    {
	  touch( m_store );
        m_psd_delegate.get_allowed_property_types( property_types );
    }
    
    
   /**
    * Indicates which properties are supported by this PropertySet. If the output sequence is
    * empty, then there is no restrictions on the properties that can be in this PropertySet,
    * unless the get_allowed_property_types output sequence is not empty.
    */
    public void get_allowed_properties( PropertyDefsHolder property_defs )
    {
 	  touch( m_store );
        m_psd_delegate.get_allowed_properties( property_defs );
    }
    
    
   /**
    * This operation will modify or add a property to the PropertySet. If the property already
    * exists, then the property type is checked before the value is overwritten. The property
    * mode is also checked to be sure a new value may be written. If the property does not
    * exist, then the property is added to the PropertySet.
    * To change the any TypeCode portion of the property_value of a property, a client
    * must first delete_property, then invoke the define_property_with_mode.
    */
    public void define_property_with_mode( 
      String property_name, Any property_value, PropertyModeType property_mode)
      throws InvalidPropertyName, ConflictingProperty, UnsupportedTypeCode, 
        UnsupportedProperty, UnsupportedMode, ReadOnlyProperty
    {
 	  touch( m_store );
        m_psd_delegate.define_property_with_mode( property_name, property_value, property_mode );
 	  modify( m_store );
    }
    
    
   /**
    * This operation will modify or add each of the properties in the Properties parameter
    * to the PropertySet. For each property in the list, if the property already exists, then the
    * property type is checked before overwriting the value. The property mode is also
    * checked to be sure a new value may be written. If the property does not exist, then the
    * property is added to the PropertySet.
    * This is a batch operation that returns the MultipleExceptions exception if any define
    * operation failed.
    */
    public void define_properties_with_modes(PropertyDef[] property_defs)
    throws MultipleExceptions
    {
 	  touch( m_store );
        m_psd_delegate.define_properties_with_modes( property_defs );
 	  modify( m_store );
    }
    
    
   /**
    * Returns the mode of the property in the PropertySet.
    */
    public org.omg.CosPropertyService.PropertyModeType get_property_mode(String property_name)
    throws PropertyNotFound, InvalidPropertyName
    {
 	  touch( m_store );
        return m_psd_delegate.get_property_mode( property_name );
    }
    
    
   /**
    * Returns the modes of the properties listed in property_names.
    * When the boolean flag is true, the property_modes parameter contains valid values
    * for all requested property names. If false, then all properties with a
    * property_mode_type of undefined failed due to PropertyNotFound or
    * InvalidPropertyName. A separate invocation of get_property_mode for each such
    * property name is necessary to determine the specific exception for that property name.
    * This approach was taken to avoid a complex, hard to program structure to carry mixed
    * results.
    */
    public boolean get_property_modes(String[] property_names, PropertyModesHolder property_modes)
    {
 	  touch( m_store );
        return m_psd_delegate.get_property_modes( property_names, property_modes );
    }
    
    
   /**
    * Sets the mode of a property in the PropertySet.
    * Protection of the mode of a property is considered an implementation issue. For
    * example, an implementation could raise the UnsupportedMode when a client attempts
    * to change a fixed_normal property to normal.
    */
    public void set_property_mode( 
	String property_name, PropertyModeType property_mode)
    throws InvalidPropertyName, PropertyNotFound, UnsupportedMode
    {
 	  touch( m_store );
        m_psd_delegate.set_property_mode( property_name, property_mode );
 	  modify( m_store );
    }
    
   /**
    * Sets the mode for each property in the property_modes parameter. This is a batch
    * operation that returns the MultipleExceptions exception if any set failed.
    */
    public void set_property_modes( PropertyMode[] property_modes)
    throws MultipleExceptions
    {
 	  touch( m_store );
        m_psd_delegate.set_property_modes( property_modes );
 	  modify( m_store );
    }

    // ==========================================================================
    // FactoryFinder implementation
    // ==========================================================================
        
   /**
    * The find_factories operation is passed a key used to identify the desired factory.
    * The key is a name, as defined by the naming service. More than one factory may
    * match the key. As such, the factory finder returns a sequence of factories. If there are
    * no matches, the NoFactory exception is raised.<p>
    * The scope of the key is the factory finder. The factory finder assigns no semantics to
    * the key. It simply matches keys. It makes no guarantees about the interface or
    * implementation of the returned factories or objects they create.
    * @param key the factory key
    * @return org.omg.CORBA.Object[] an array of objects
    */
    public org.omg.CORBA.Object[] find_factories( NameComponent[] key )
    throws NoFactory
    {
	  // Return the default chooser.
        // Note 1: need to add a default locator.
        // Note 2: need to add a personal favorites list

        return new org.omg.CORBA.Object[]{ m_sessionService.getChooser() };
    }
    
    //====================================================================================
    // AbstractResource override
    //====================================================================================

   /**
    *  The most derived type that this <code>AbstractResource</code> represents.
    */
    public TypeCode resourceKind()
    {
        touch( m_store );
        return UserHelper.type();
    }

   /**
    * Extension of the AbstractResource bind operation to support reception of the
    * notification of association dependencies from external resources on this
    * User.
    *
    * @param link Link notification of an association dependency
    */
    public synchronized void bind(Link link)
    throws ResourceUnavailable, ProcessorConflict, SemanticConflict
    {
        synchronized( m_store )
        {
            touch( m_store );
            if( link instanceof org.omg.Session.Accesses )
            {
                
                // Notification to this User that is is being included in an
		    // Workspace access control list.
                
                LinkedList list = m_store.accesses();
                synchronized( list )
                {
                    if( !containsLink( list, link ))
                    {
                        addLink( m_user, list, link );
                    }
                    else
                    {
                        throw new SemanticConflict();
                    }
                }
            }
            else if( link instanceof org.omg.Session.Owns )
            {
                
                // Notification to this User that a Task is being associated
		    // to this user.
                
                LinkedList list = m_store.owns();
                synchronized( list )
                {
                    if( !containsLink( list, link ))
                    {
                        addLink( m_user, list, link );
                    }
                    else
                    {
                        throw new SemanticConflict();
                    }
                }
            }
            else if( link instanceof org.omg.Session.Privilege )
            {
                // Notification to this User that a supplimentary
                // privilege has been granted.

                LinkedList list = m_store.privileges();
                synchronized( list )
                {
                    if( !containsLink( list, link ))
                    {
                        addLink( m_user, list, link );
                    }
                    else
                    {
                        throw new SemanticConflict();
                    }
                }
            }
            else
            {
                super.bind( link );
            }
        }
    }

   /**
    * Extension of the AbstractResource replace operation to support reception of the
    * notification of the replacement of association dependencies from external 
    * resources on this User.
    *
    * Replaces an existing User dependecy with another.
    * @param  old the Link to replace
    * @param  new the replacement Link
    * @exception  <code>ResourceUnavailable</code>
    * if the resource cannot accept the new link binding
    * @exception  <code>ProcessorConflict</code>
    * if a processor is unable or unwilling to provide processing services to a Task.
    * @exception  <code>SemanticConflict</code>
    * if the resource cannot accept the link binding due to a cardinality constraint.
    */
    public synchronized void replace(Link old, Link _new)
    throws ResourceUnavailable, ProcessorConflict, SemanticConflict
    {
        synchronized( m_store )
        {
            if( getLogger().isDebugEnabled() ) getLogger().debug("[USR]replace");

            touch( m_store );

            if (( old instanceof org.omg.Session.Accesses ) 
              && ( _new instanceof org.omg.Session.Accesses ))
            {
                // client is requesting the replacement of an exiting Accesses
                // association from a Workspace
                try
                {
                    replaceLink( m_user, m_store.accesses(), old, _new );
                }
                catch (Exception e)
                {
                    String s = "unexpected exception while resolving 'Accesses' references";
                    throw new UserRuntimeException( s, e );
                }
            }
            else if (( old instanceof org.omg.Session.Owns) 
              && ( _new instanceof org.omg.Session.Owns ))
            {
                // client is requesting the replacement of an exiting Owns
                // association from a Task
                try
                {
                    replaceLink( m_user, m_store.owns(), old, _new );
                }
                catch (Exception e)
                {
                    String s = "Unexpected exception while resolving 'Owns' references.";
                    throw new UserRuntimeException( s, e );
                }
            }
            else if (( old instanceof org.omg.Session.Privilege) 
              && ( _new instanceof org.omg.Session.Privilege ))
            {
                try
                {
                    replaceLink( m_user, m_store.privileges(), old, _new );
                }
                catch (Exception e)
                {
                    String s = "Unexpected exception while resolving privilege.";
                    throw new UserRuntimeException( s, e );
                }
            }
            else
            {
                super.replace( old, _new );
            }
            modify( m_store );
        }
    }
    
   /**
    * Extension of the AbstractResource replace operation to support reception of the
    * notification of the release of association dependencies from external 
    * resources on this User.  This operation has lifecycle implications on owned Tasks.
    * The release of an owns association on a Task will result in the destruction of 
    * the Task instance.
    *
    * Releases an existing dependecy.
    * @param link Link to retract
    */
    public synchronized void release(Link link)
    {
        synchronized( m_store )
        {
            touch( m_store );
            
            if( link instanceof org.omg.Session.Accesses )
            {
                
                // A Workspace is notifying us of the release of an Accesses 
		    // association.
                
                try
                {
                    LinkedList list = m_store.accesses();
                    releaseLink( m_user, list, link );
                }
                catch( Exception e)
                {
                    String s = "failed to release Accesses association";
                    throw new UserRuntimeException( s, e );
                }
            }
            else if( link instanceof org.omg.Session.Owns )
            {
                
                // an Task is notifying this resource of the retraction of
                // an Owns association
                
                try
                {
                    LinkedList list = m_store.owns();
                    releaseLink( m_user, list, link );
                }
                catch( Exception e)
                {
                    String s = "failed to release Owns association";
                    throw new UserRuntimeException( s, e );
                }
            }
            else if( link instanceof org.omg.Session.Privilege )
            {
                try
                {
                    LinkedList list = m_store.privileges();
                    releaseLink( m_user, list, link );
                }
                catch (Exception e)
                {
                    String s = "Unexpected exception while releasing privilege.";
                    throw new UserRuntimeException( s, e );
                }
            }
            else
            {
                super.release( link );
            }
        }
    }

   /**
    * Returns the number of Links held by an User corresponding to a
    * given TypeCode filter criteria.  Filter criteria is expressed as a TypeCode
    * desribing a type of Link.  User is aware of the link types
    * presented in the table below together with links declared under the 
    * AbstractResource count operation.  Types derived from User may
    * override the count method to return counts based on more specific link
    * associations.
    *
    * <table>
    * <tr>
    *   <td>TypeCode</td><td>Description</td>
    * <tr>
    *   <td valign="top">Privalege</td>
    *   <td>
    *     Super type of the Ownership and Access abstract links. Both
    *     Ownership and Access are abstract link kinds exposed by
    *     User, Workspace and Task.  User will return the count of all
    *     Accesses and Owns association in response to Privalege typecode.
    *   </td>
    * <tr>
    *   <td valign="top">Access</td>
    *   <td>
    *     Super type of Accesses.
    *   </td>
    * <tr>
    *   <td valign="top">Accesses</td>
    *   <td>
    *     Returns the number of workspaces accessed by this user.
    *   </td>
    * <tr>
    *   <td valign="top">Administers</td>
    *   <td>
    *     Returns the number of workspaces administered by this user.
    *   </td>
    * <tr>
    *   <td valign="top">Owns</td>
    *   <td>
    *     Returns the number of tasks owned by this user.
    *   </td>
    * </table>
    * <br>
    *
    * @return  int number of links corresponding to the supplied type.
    * @param  type CORBA::TypeCode a type code of a valuetype derived from Link
    */
    public short count(org.omg.CORBA.TypeCode type)
    {
        getLogger().debug("count");
        int count = 0;

        // abstract link types
        
        if( type.equal( PrivilegeHelper.type() ) ) 
	  {
		count = m_store.accesses().size() 
              + m_store.owns().size() 
              + m_store.privileges().size();
	  }
        else if( type.equal( AccessHelper.type() ) ) 
	  {
	      count = m_store.accesses().size();
	  }
        else if( type.equal( AccessesHelper.type() ) ) 
	  {
		count = m_store.accesses().size();
	  }
        else if( type.equal( AdministersHelper.type() ) ) 
	  {
		int i = 0;
		LinkedList accesses = m_store.accesses();
		synchronized( accesses )
		{
		    Iterator iterator = accesses.iterator();
		    while( iterator.has_next() )
		    {
			  try
			  {
			      if( iterator.next() instanceof Administers ) i++;
			  }
			  catch( NoEntry ne )
			  {
				// should not happen if the list is synchronized
				String error = "Iterator throw a NoEntry while has_next is true inside User.count.";
				throw new UserRuntimeException( error, ne );
			  }
		    }
	      }
		count = i;
	  }
	  else
	  {
	      count = super.count( type );
	  }
        touch( m_store );
        Integer v = new Integer( count );
        return v.shortValue();
    }

   /**
    * Returns a set of resources linked to this user by a specific relationship.
    * This operation may be used by desktop managers to present object relationship 
    * graphs. The User expand implmentation suppliments the AbstractResource expand 
    * implementation with the addition of support for Accesses and Owns link types.
    * Support for abstract link expansion and LinkIterator is not available at this time.
    *
    * @return  LinkIterator an iterator of Link instances
    * @param  max_number maximum number of Link instance to include in the
    * seq value.
    * @param  seq Links a sequence of Links matching the type filter
    */
    public LinkIterator expand(org.omg.CORBA.TypeCode type, int max_number, LinksHolder links)
    {
        getLogger().debug("expand");
             
        if( type.equivalent( AccessesHelper.type() ))
        {
            LinkedList accesses = m_store.accesses();
            synchronized( accesses )
            {
                // prepare resource sequence
                links.value = create_link_sequence( accesses, max_number ); 
		    touch( m_store );
                LinkIteratorDelegate delegate = new LinkIteratorDelegate( super.getORB(), accesses, type );
	          LinkIteratorPOA servant = new LinkIteratorPOATie( delegate );
		    return servant._this( super.getORB() );
            }
        }
        else if( type.equivalent( OwnsHelper.type() ))
        {
            LinkedList owns = m_store.owns();
            synchronized( owns )
            {
                // prepare resource sequence
                links.value = create_link_sequence( owns, max_number ); 
		    touch( m_store );
                LinkIteratorDelegate delegate = new LinkIteratorDelegate( super.getORB(), owns, type );
	          LinkIteratorPOA servant = new LinkIteratorPOATie( delegate );
		    return servant._this( super.getORB() );
            }
        }
        else if( type.equivalent( PrivilegeHelper.type() ))
        {
            LinkedList list = m_store.privileges();
            synchronized( list )
            {
                // prepare resource sequence
                links.value = create_link_sequence( list, max_number ); 
		    touch( m_store );
                LinkIteratorDelegate delegate = new LinkIteratorDelegate( super.getORB(), list, type );
	          LinkIteratorPOA servant = new LinkIteratorPOATie( delegate );
		    return servant._this( super.getORB() );
            }
        }
        else
        {
            return super.expand( type, max_number, links );
        }
    }


    // ==========================================================================
    // BaseBusinessObject operations
    // ==========================================================================
    
   /**
    * UserDelegate extends the remove operation through retraction of
    * privalige (ownership and access) links towards Task and Workspaces.
    */
    public synchronized void remove()
    throws NotRemovable
    {
        synchronized( m_store )
        {
            if( getLogger().isDebugEnabled() ) getLogger().debug("[USR] remove");
		try
		{
                terminate();
		}
		catch(CannotTerminate e)
		{
		    String msg = "unexpected exception while terminating User";
		    if( getLogger().isErrorEnabled() ) getLogger().error( msg, e );
		    throw new NotRemovable( msg );
		}
		try
		{
 		    dispose();
		}
		catch(Exception e)
		{
		    System.err.println("unexpected error while removing resource");
		    e.printStackTrace();
		}
        }
    }

    // ==========================================================================
    // internals
    // ==========================================================================

   /**
    * Set the object reference to be returned for this delegate.
    * @param workspace the object reference for the workspace
    */
    protected void setUserReference( User user )
    {
        m_user = user;
        setReference( user );
    }

    public Adapter get_adapter()
    {
        if( isOwner() ) 
        {
            return new PrincipalValue( m_user );
        }
        else
        {
            return new UserValue( m_user );
        }
    }

}
