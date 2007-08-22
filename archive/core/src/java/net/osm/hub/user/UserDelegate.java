

package net.osm.hub.user;

import java.util.Vector;

import org.apache.avalon.framework.context.Context;
import org.apache.avalon.framework.component.ComponentManager;
import org.apache.avalon.framework.component.ComponentException;

import org.omg.CORBA.Any;
import org.omg.CORBA.ORB;
import org.omg.CORBA.TypeCode;
import org.omg.CORBA.BooleanHolder;
import org.omg.CosCollection.AnySequenceHolder;
import org.omg.PortableServer.POA;
import org.omg.CosTime.TimeService;
import org.omg.CosLifeCycle.NotRemovable;
import org.omg.CosPersistentState.NotFound;
import org.omg.CosNotification.StructuredEvent;
import org.omg.CosNotification.EventType;
import org.omg.CosNotification.EventHeader;
import org.omg.CosNotification.FixedEventHeader;
import org.omg.CosNotification.Property;
import org.omg.CollaborationFramework.Processor;
import org.omg.CollaborationFramework.ProcessorHelper;
import org.omg.CollaborationFramework.ProcessorModel;
import org.omg.CommunityFramework.Member;
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
import org.omg.Session.SystemMessage;
import org.omg.Session.ResourceUnavailable;
import org.omg.Session.SemanticConflict;
import org.omg.Session.SystemMessageBase;
import org.omg.Session.SystemMessageHelper;
import org.omg.Session.SystemMessagesHolder;
import org.omg.Session.SystemMessageIterator;
import org.omg.Session.SystemMessageIteratorPOA;
import org.omg.Session.SystemMessageIteratorPOATie;
import org.omg.Session.SessionSingleton;
import org.omg.Session.Task;
import org.omg.Session.TaskHelper;
import org.omg.Session.TasksHolder;
import org.omg.Session.TaskIteratorHolder;
import org.omg.Session.TaskIterator;
import org.omg.Session.TaskIteratorPOA;
import org.omg.Session.TaskIteratorPOATie;
import org.omg.Session.User;
import org.omg.Session.UserOperations;
import org.omg.Session.UserHelper;
import org.omg.Session.Workspace;
import org.omg.Session.WorkspaceHelper;
import org.omg.Session.WorkspacesHolder;
import org.omg.Session.WorkspaceIteratorHolder;
import org.omg.Session.WorkspaceIterator;
import org.omg.Session.WorkspaceIteratorPOA;
import org.omg.Session.WorkspaceIteratorPOATie;

import net.osm.hub.desktop.DesktopService;
import net.osm.hub.gateway.FactoryException;
import net.osm.hub.gateway.CannotTerminate;
import net.osm.hub.gateway.ResourceFactoryService;
import net.osm.hub.home.ResourceFactory;
import net.osm.hub.resource.SystemMessageIteratorDelegate;
import net.osm.hub.properties.PropertySetDef;
import net.osm.hub.properties.PropertySetDefDelegate;
import net.osm.hub.pss.LinkStorage;
import net.osm.hub.pss.UserStorage;
import net.osm.hub.pss.WorkspaceStorage;
import net.osm.hub.pss.WorkspaceStorageHome;
import net.osm.hub.pss.FolderStorage;
import net.osm.hub.pss.DomainStorage;
import net.osm.hub.pss.MessageStorage;
import net.osm.hub.pss.MessageStorageHome;
import net.osm.hub.properties.UserEventSource;
import net.osm.hub.resource.LinkIteratorDelegate;
import net.osm.hub.resource.StructuredEventUtilities;
import net.osm.hub.resource.AbstractResourceDelegate;
import net.osm.hub.task.TaskIteratorDelegate;
import net.osm.hub.task.TaskService;
import net.osm.hub.workspace.WorkspaceIteratorDelegate;
import net.osm.hub.workspace.WorkspaceService;
import net.osm.list.LinkedList;
import net.osm.list.Iterator;
import net.osm.list.NoEntry;
import net.osm.time.TimeUtils;
import net.osm.util.Incrementor;

/**
 * UserDelegate is the implementation delegate for the Task and Session User type
 * and as such supports the role of a person in a distributed computing environment. 
 * Information  about the person is inherited by User. In this specification Users have   
 * tasksand resources located in workspaces on a desktop, as well as a message queue  
 * and a connection state. A specialization of User can add things like  
 * preferences.
 */

public class UserDelegate extends AbstractResourceDelegate implements UserOperations, UserEventSource
{

    // ============================================================
    // static
    // ============================================================
    
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


    // ============================================================
    // state
    // ============================================================
    
   /**
    * PSS storage object for this instance.
    */
    private UserStorage store;
    
   /**
    * Object reference to this user instance.
    */
    private User m_user;
    
   /**
    * Object reference to this user's desktop.
    */
    protected Desktop desktop;

   /**
    * POA reponsible for the PropertySetDef implementation.
    */
    private PropertySetDefDelegate properties;

    private ResourceFactoryService resourceFactoryService;
    private WorkspaceService workspaceService;
    private TaskService taskService;
    private DesktopService desktopService;

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
	  super.compose( manager );
        if( getLogger().isDebugEnabled() ) getLogger().debug("composition (user)");
	  resourceFactoryService = (ResourceFactoryService)manager.lookup("FACTORY");
	  desktopService = (DesktopService) manager.lookup("DESKTOP");
	  workspaceService = (WorkspaceService) manager.lookup("WORKSPACE");
	  taskService = (TaskService) manager.lookup("TASK");
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
        if( getLogger().isDebugEnabled() ) getLogger().debug("initialization (user)");
        this.store = (UserStorage) super.getContext().getStorageObject();
	  setUserReference( UserHelper.narrow( getManager().getReference( 
          store.get_pid(), UserHelper.id() ) ) );

        //
        // Set the PropertySetDef delegate
	  //

	  try
	  {
	      properties = new PropertySetDefDelegate( 
			orb, store.property_set(), store.get_short_pid(), this
	      );
		properties.enableLogging( getLogger().getChildLogger( "PSD" )); 
		if( getLogger().isDebugEnabled() ) getLogger().debug("property delegate established");
	  }
	  catch( Throwable t )
	  {
		String error = "failed to establish PropertySetDefDelegate";
		if( getLogger().isErrorEnabled() ) getLogger().error( error );
		throw new Exception( error, t );
	  }
    }

    //==================================================
    // UserEventSource
    //==================================================


   /**
    * Post a structured event.
    */
    public void post( StructuredEvent event )
    {
        super.post( event );
    }

    //==================================================
    // Vulnerable
    //==================================================

   /**
    * Destroys the persistent identity of this object. An 
    * implementation is responsible for disposing of references 
    * to external resources on completion of termiation actions. 
    */
    public void terminate( ) throws CannotTerminate
    {
	  synchronized( store )
        {
		if( !expendable() ) throw new CannotTerminate("resource is in use");
            if( getLogger().isDebugEnabled() ) getLogger().debug("[USR] terminate ");

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
                LinkedList accesses = store.accesses();
                retractEveryLink( 
			getUserReference(), accesses, "accesses", 
			new AccessedBy( getUserReference() ));
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
                Iterator iterator = store.messages().iterator();
                while( iterator.has_next() )
                {
			  iterator.next().destroy_object();
			  iterator.remove();
                }
            }
            catch( Exception e)
            {
                String s = "unexpected error while destroying messages";
                e.printStackTrace();
                if( getLogger().isErrorEnabled() ) getLogger().error( "[USR] " + s, e );
            }


            //
	      // release owns associations
	      // (removes all Tasks owned by this User)
	      //

		boolean success = true;
	      try
            {
                if( getLogger().isDebugEnabled() ) getLogger().debug( 
			"[USR] preparing sequence of Tasks owned by the user" );
		    Vector ownsVector = new Vector();
                Iterator iterator = store.owns().iterator();
                while( iterator.has_next() )
                {
                    LinkStorage s = (LinkStorage) iterator.next();
		        ownsVector.addElement( s.link().resource() );
		    }

		    int j = ownsVector.size();
                if( getLogger().isDebugEnabled() ) getLogger().debug( "[USR] removing Tasks " + j+1);
		    for( int i=0; i<j; i++ )
		    {
		        org.omg.CORBA.Object obj = (org.omg.CORBA.Object) ownsVector.elementAt( i );
		        Task task = TaskHelper.narrow( obj );
			  try
			  {
                        if( getLogger().isDebugEnabled() ) getLogger().debug( 
				  "[USR] removing task " + i );
		            task.remove();
		        }
                    catch( Throwable e )
	              {
		            String warning =  "[USR] failed to remove a task";
		            if( getLogger().isWarnEnabled() ) getLogger().warn( warning, e );
		            System.err.println( warning );
		            e.printStackTrace();
                        success = false;
                    }
		    }
	      }
            catch( Throwable e)
            {
                String s = "unexpected error while releasing tasks";
                e.printStackTrace();
                if( getLogger().isErrorEnabled() ) getLogger().error( "[USR] " + s, e );
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
 	  if( getLogger().isDebugEnabled() ) getLogger().debug("dispose (user)");
	  this.properties.dispose();
        this.properties = null;
        this.store = null;
        this.m_user = null;
        this.desktop = null;
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
        return ( MessageStorageHome ) getSession().find_storage_home( 
			"PSDL:osm.net/hub/pss/MessageStorageHomeBase:1.0" );
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
		store.messages().add( getMessageHome().create( id, message ) );
		post( newEnqueueEvent( message ) );
        }
        catch( Exception e )
        {
            final String error = "failed to enqueue a message";
	      if( getLogger().isErrorEnabled() ) getLogger().error( error, e );
            throw new org.omg.CORBA.INTERNAL();
        }
    }

   /**
    * Removes a message from the INTRAY
    * @param identifier the system message identifier of the message to dequeue
    */
    public void dequeue( long identifier )
    {
 	  if( getLogger().isDebugEnabled() ) getLogger().debug("dequeue");
	  try
	  {
		MessageStorage s = getMessageHome().find_by_identifier( identifier );
		store.messages().remove( s );
		s.destroy_object();
		post( newDequeueEvent( identifier ) );
        }
        catch( Exception e )
        {
            final String error = "failed to dequeue a message";
	      if( getLogger().isErrorEnabled() ) getLogger().error( error, e );
            throw new org.omg.CORBA.INTERNAL();
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
        LinkedList list = store.messages();
        synchronized( list )
        {
            
            getLogger().info("get_messages");
            
            SystemMessage[] seq = null;
            int n = list.size();

		System.out.println("MESSAGES/SIZE: " + n );
		System.out.println("MESSAGES/MAX: " + max_number );

            if( ( n == 0 ) || ( n < max_number ) )
            {
                seq = new SystemMessage[n];
            }
            else
            {
                seq = new SystemMessage[max_number];
            }

		System.out.println("MESSAGES/SEQ: " + seq.length );
            Iterator iterator = list.iterator();
            try
            {
                for( int i=0; i<seq.length; i++ )
                {
                    MessageStorage ms = (MessageStorage) iterator.next();
			  seq[i] = ms.message();
                }
                messages.value = seq;
            }
            catch (Exception e)
            {
                String error = "Unexpected error while preparing message sequence.";
                if( getLogger().isErrorEnabled() ) getLogger().error( error ,e);
                throw new org.omg.CORBA.INTERNAL( error );
            }

		//
		// package and return the iterator
            //

            try
            {
                SystemMessageIteratorDelegate delegate = 
				new SystemMessageIteratorDelegate( orb, iterator );
	          SystemMessageIteratorPOA servant = new SystemMessageIteratorPOATie( delegate );
		    return servant._this( orb );
		}
		catch( Exception e )
		{
		    final String error = "Message iterator construction failure.";
                if( getLogger().isErrorEnabled() ) getLogger().error( error, e );
                throw new org.omg.CORBA.INTERNAL( error );
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
        Property sourceProp = super.createSourceProperty( );
        Any b = orb.create_any();
        b.insert_boolean( connected );
        Property connectProp = new Property( "connected", b );
        return StructuredEventUtilities.createEvent( orb, connectedEventType, 
		new Property[]
		{ 
			StructuredEventUtilities.timestamp( orb ), 
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
        Property sourceProp = super.createSourceProperty();
        Any any = orb.create_any();
        SystemMessageHelper.insert( any, message );
        Property messageProp = new Property( "message", any );
        return StructuredEventUtilities.createEvent( orb, enqueueEventType, 
		new Property[]
		{ 
			StructuredEventUtilities.timestamp( orb ), 
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
        Property sourceProp = super.createSourceProperty();
        Any any = orb.create_any();
	  any.insert_longlong( identifier );
        Property id = new Property( "identifier", any );
        return StructuredEventUtilities.createEvent( orb, dequeueEventType, 
		new Property[]
		{ 
			StructuredEventUtilities.timestamp( orb ), 
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
        touch( store );
        if( this.store.connected() ) return connect_state.connected;
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
        synchronized( store )
        {
            touch( store );
            if( store.connected() ) throw new AlreadyConnected();
            store.connected( true );
		post( newConnectedEvent( true ));
            modify( store );
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
        synchronized( store )
        {
            touch( store );
            if( !store.connected() ) throw new org.omg.Session.NotConnected();
            store.connected( false );
		post( newConnectedEvent( false ));
            modify( store );
        }
    }
    
   /**
    * The enqueue_message operation is used to notify a user of a
    * message.  Messages, passed as arguments to this operation, can be created
    * though a MessageFactory accessible under the respective user's 
    * find_factories operation.
    *
    * @param message Message to enqueue
    * @see net.osm.hub.user.UserDelegate#enqueue( SystemMessage ) enqueue
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
    * @see net.osm.hub.user.UserDelegate#get_messages
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
	  touch( store );
	  try
	  {
	      Processor processor = ProcessorHelper.narrow( process );
		ProcessorModel model = (ProcessorModel)processor.model();
		String description = model.note;
		Task task = getTaskService().createTask( name, description, processor );
	      if( data == null ) return task;

		ConsumedBy consumedBy = new ConsumedBy( task );
		Consumes consumes = new Consumes( data );
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
                throw new Exception( "unexpected error while creating task", e );
		}
	  }
        catch( Throwable t )
	  {
		String error = "unable to create new task";
		if( getLogger().isErrorEnabled() ) getLogger().error( error, t );
		throw new org.omg.CORBA.INTERNAL();
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
        LinkedList list = store.owns();
        synchronized( list )
        {
	      try
	      {
	          TaskIteratorDelegate delegate = new TaskIteratorDelegate( orb, list.iterator() );
	          TaskIteratorPOA servant = new TaskIteratorPOATie( delegate );
		    iterator = servant._this( orb );

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
		    String error = "failed to establish TaskIterator" ;
	          if( getLogger().isErrorEnabled() ) getLogger().error( error, e );
		    throw new org.omg.CORBA.INTERNAL( error );
		}
        }
	  taskit.value = iterator;
        touch( store );
    }
    
    
   /**
    * Returns the Desktop that links one User to many Workspaces. Workspaces
    * may have many Users linked via Desktop.
    * @return  Desktop the User's private Desktop.
    */
    public Desktop get_desktop()
    {
	  touch( store );
	  if( desktop != null ) return desktop;
	  try
	  {
            desktop = getDesktopService().getDesktopReference( 
              store.desktop().get_pid() );
	  }
	  catch( Throwable e )
	  {
		String error = "unable to resolve desktop from internal reference";
		if( getLogger().isErrorEnabled() ) getLogger().error( error, e );
		throw new org.omg.CORBA.INTERNAL();
	  }
        return desktop;
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
    public Workspace create_workspace(String name, User[] accesslist)
    {

        Workspace workspace = null;
        try
        {
            workspace = getWorkspaceService().createWorkspace( name );
	  }
	  catch( Exception e )
	  {
		String error = "failed to create workspace";
            if( getLogger().isErrorEnabled() ) getLogger().error( error, e );
            throw new org.omg.CORBA.INTERNAL( error );
	  }

        IsPartOf isPartOf = new IsPartOf( get_desktop() );
        ComposedOf composedOf = new ComposedOf( workspace );
	  Administers administers = new Administers( workspace );
        AdministeredBy administeredBy = new AdministeredBy( getUserReference() );
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
            throw new org.omg.CORBA.INTERNAL( error );
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
        LinkedList list = store.accesses();
        synchronized( list )
        {
	      try
	      {
	          WorkspaceIteratorDelegate delegate = new WorkspaceIteratorDelegate( 
			orb, list.iterator() );
	          WorkspaceIteratorPOA servant = new WorkspaceIteratorPOATie( delegate );
		    iterator = servant._this( orb );

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
	          if( getLogger().isErrorEnabled() ) getLogger().error( error, e );
		    throw new org.omg.CORBA.INTERNAL( error );
		}
        }
	  wsit.value = iterator;
        touch( store );
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
    * @osm.warning suport for storing the factories assigned to a particular user is not implemented
    *   at this time
    */
    public org.omg.CORBA.Object[] find_factories(org.omg.CosNaming.NameComponent[] factory_key)
    throws org.omg.CosLifeCycle.NoFactory
    {
        return new org.omg.CORBA.Object[]{ getResourceFactoryService().getResourceFactory() };
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
    public void define_property( String property_name, org.omg.CORBA.Any property_value)
    throws org.omg.CosPropertyService.InvalidPropertyName,
    org.omg.CosPropertyService.ConflictingProperty,
    org.omg.CosPropertyService.UnsupportedTypeCode,
    org.omg.CosPropertyService.UnsupportedProperty,
    org.omg.CosPropertyService.ReadOnlyProperty
    {
        properties.define_property( property_name, property_value );
	  modify( store );
    }
    
   /**
    * Will modify or add each of the properties in Properties parameter to the
    * PropertySet. For each property in the list, if the property already exists, then the
    * property type is checked before overwriting the value. If the property does not exist,
    * then the property is added to the PropertySet.<p>
    * This is a batch operation that returns the MultipleExceptions exception if any define
    * operation failed.
    */
    public void define_properties(org.omg.CosPropertyService.Property[] nproperties)
    throws org.omg.CosPropertyService.MultipleExceptions
    {
        properties.define_properties( nproperties );
	  modify( store );
    }
    
   /**
    * Returns the current number of properties associated with this PropertySet.
    */
    public int get_number_of_properties()
    {
	  touch( store );
        return properties.get_number_of_properties( );
    }
    
   /**
    * Returns all of the property names currently defined in the PropertySet. If the
    * PropertySet contains more than how_many property names, then the remaining
    * property names are put into the PropertyNamesIterator.
    */
    public void get_all_property_names(int how_many, org.omg.CosPropertyService.PropertyNamesHolder property_names, org.omg.CosPropertyService.PropertyNamesIteratorHolder rest)
    {
	  touch( store );
        properties.get_all_property_names( how_many, property_names, rest );
    }
    
   /**
    * Returns the value of a property in the PropertySet.
    */
    public org.omg.CORBA.Any get_property_value(java.lang.String property_name)
    throws org.omg.CosPropertyService.PropertyNotFound,
    org.omg.CosPropertyService.InvalidPropertyName
    {
	  touch( store );
        return properties.get_property_value( property_name );
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
    public boolean get_properties(java.lang.String[] property_names, org.omg.CosPropertyService.PropertiesHolder nproperties)
    {
	  touch( store );
        return properties.get_properties( property_names, nproperties );
    }
    
   /**
    * Returns all of the properties defined in the PropertySet. If more than how_many
    * properties are found, then the remaining properties are returned in an iterator.
    */
    public void get_all_properties(int how_many, org.omg.CosPropertyService.PropertiesHolder nproperties, org.omg.CosPropertyService.PropertiesIteratorHolder rest)
    {
        properties.get_all_properties( how_many, nproperties, rest);
	  touch( store );
    }
    
   /**
    * Deletes the specified property if it exists from a PropertySet.
    */
    public void delete_property(java.lang.String property_name)
    throws org.omg.CosPropertyService.PropertyNotFound, org.omg.CosPropertyService.InvalidPropertyName, org.omg.CosPropertyService.FixedProperty
    {
        properties.delete_property( property_name );
	  modify( store );
    }
    
   /**
    * Deletes the properties defined in the property_names parameter.
    * This is a batch operation that returns the MultipleExceptions exception
    * if any delete failed.
    */
    public void delete_properties(java.lang.String[] property_names)
    throws org.omg.CosPropertyService.MultipleExceptions
    {
        properties.delete_properties( property_names );
	  modify( store );
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
	  modify( store );
        return properties.delete_all_properties();
    }
    
   /**
    * The is_property_defined operation returns true if the property is defined in the
    * PropertySet, and returns false otherwise.
    */
    public boolean is_property_defined(java.lang.String property_name)
    throws org.omg.CosPropertyService.InvalidPropertyName
    {
	  touch( store );
        return properties.is_property_defined( property_name );
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
    public void get_allowed_property_types(org.omg.CosPropertyService.PropertyTypesHolder property_types)
    {
	  touch( store );
        properties.get_allowed_property_types( property_types );
    }
    
    
   /**
    * Indicates which properties are supported by this PropertySet. If the output sequence is
    * empty, then there is no restrictions on the properties that can be in this PropertySet,
    * unless the get_allowed_property_types output sequence is not empty.
    */
    public void get_allowed_properties(org.omg.CosPropertyService.PropertyDefsHolder property_defs)
    {
 	  touch( store );
        properties.get_allowed_properties( property_defs );
    }
    
    
   /**
    * This operation will modify or add a property to the PropertySet. If the property already
    * exists, then the property type is checked before the value is overwritten. The property
    * mode is also checked to be sure a new value may be written. If the property does not
    * exist, then the property is added to the PropertySet.
    * To change the any TypeCode portion of the property_value of a property, a client
    * must first delete_property, then invoke the define_property_with_mode.
    */
    public void define_property_with_mode(String property_name, org.omg.CORBA.Any property_value, org.omg.CosPropertyService.PropertyModeType property_mode)
    throws org.omg.CosPropertyService.InvalidPropertyName,
    org.omg.CosPropertyService.ConflictingProperty,
    org.omg.CosPropertyService.UnsupportedTypeCode,
    org.omg.CosPropertyService.UnsupportedProperty,
    org.omg.CosPropertyService.UnsupportedMode,
    org.omg.CosPropertyService.ReadOnlyProperty
    {
 	  touch( store );
        properties.define_property_with_mode( property_name, property_value, property_mode );
 	  modify( store );
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
    public void define_properties_with_modes(org.omg.CosPropertyService.PropertyDef[] property_defs)
    throws org.omg.CosPropertyService.MultipleExceptions
    {
 	  touch( store );
        properties.define_properties_with_modes( property_defs );
 	  modify( store );
    }
    
    
   /**
    * Returns the mode of the property in the PropertySet.
    */
    public org.omg.CosPropertyService.PropertyModeType get_property_mode(java.lang.String property_name)
    throws org.omg.CosPropertyService.PropertyNotFound, org.omg.CosPropertyService.InvalidPropertyName
    {
 	  touch( store );
        return properties.get_property_mode( property_name );
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
    public boolean get_property_modes(java.lang.String[] property_names, org.omg.CosPropertyService.PropertyModesHolder property_modes)
    {
 	  touch( store );
        return properties.get_property_modes( property_names, property_modes );
    }
    
    
   /**
    * Sets the mode of a property in the PropertySet.
    * Protection of the mode of a property is considered an implementation issue. For
    * example, an implementation could raise the UnsupportedMode when a client attempts
    * to change a fixed_normal property to normal.
    */
    public void set_property_mode( 
	String property_name, org.omg.CosPropertyService.PropertyModeType property_mode)
    throws org.omg.CosPropertyService.InvalidPropertyName,
    org.omg.CosPropertyService.PropertyNotFound,
    org.omg.CosPropertyService.UnsupportedMode
    {
 	  touch( store );
        properties.set_property_mode( property_name, property_mode );
 	  modify( store );
    }
    
   /**
    * Sets the mode for each property in the property_modes parameter. This is a batch
    * operation that returns the MultipleExceptions exception if any set failed.
    */
    public void set_property_modes(org.omg.CosPropertyService.PropertyMode[] property_modes)
    throws org.omg.CosPropertyService.MultipleExceptions
    {
 	  touch( store );
        properties.set_property_modes( property_modes );
 	  modify( store );
    }

    // ==========================================================================
    // AbstractResource operation override
    // ==========================================================================

   /**
    *  The most derived type that this <code>AbstractResource</code> represents.
    */
    
    public TypeCode resourceKind()
    {
        touch( store );
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
        synchronized( store )
        {
            touch( store );
            if( link instanceof Accesses )
            {
                
                // Notification to this User that is is being included in an
		    // Workspace access control list.
                
                LinkedList list = store.accesses();
                synchronized( list )
                {
                    if( !containsLink( list, link ))
                    {
                        addLink( getUserReference(), list, link );
                    }
                    else
                    {
                        throw new SemanticConflict();
                    }
                }
            }
            else if( link instanceof Owns )
            {
                
                // Notification to this User that a Task is being associated
		    // to this user.
                
                LinkedList list = store.owns();
                synchronized( list )
                {
                    if( !containsLink( list, link ))
                    {
                        addLink( getUserReference(), list, link );
                    }
                    else
                    {
                        throw new SemanticConflict();
                    }
                }
            }
            else if( link instanceof Member )
            {
                // Notification to this User that a Membership has granted
		    // recognition of the user as a Member.

                LinkedList list = store.memberships();
                synchronized( list )
                {
                    if( !containsLink( list, link ))
                    {
                        addLink( getUserReference(), list, link );
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
        synchronized( store )
        {
            if( getLogger().isDebugEnabled() ) getLogger().debug("[USR]replace");
            touch( store );
            if (( old instanceof Accesses ) && ( _new instanceof Accesses ))
            {
                // client is requesting the replacement of an exiting Accesses
                // association from a Workspace
                try
                {
                    replaceLink( getUserReference(), store.accesses(), old, _new );
                }
                catch (Exception e)
                {
                    String s = "unexpected exception while resolving 'Accesses' references";
                    if( getLogger().isErrorEnabled() ) getLogger().error(s,e);
                    throw new org.omg.CORBA.INTERNAL(s);
                }
            }
            else if (( old instanceof Owns) && ( _new instanceof Owns ))
            {
                // client is requesting the replacement of an exiting Owns
                // association from a Task
                try
                {
                    replaceLink( getUserReference(), store.owns(), old, _new );
                }
                catch (Exception e)
                {
                    String s = "unexpected exception while resolving 'Owns' references";
                    if( getLogger().isErrorEnabled() ) getLogger().error( s, e );
                    throw new org.omg.CORBA.INTERNAL(s);
                }
            }
            else if (( old instanceof Member) && ( _new instanceof Member ))
            {
                // client is requesting the replacement of an exiting Member
                // association from a Membership

                try
                {
                    replaceLink( getUserReference(), store.memberships(), old, _new );
                }
                catch (Exception e)
                {
                    String s = "unexpected exception while resolving 'Member' references";
                    if( getLogger().isErrorEnabled() ) getLogger().error( s, e );
                    throw new org.omg.CORBA.INTERNAL(s);
                }
            }
            else
            {
                super.replace( old, _new );
            }
            modify( store );
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
        synchronized( store )
        {
            getLogger().info("[USR] release");
            touch( store );
            
            if( link instanceof Accesses )
            {
                
                // A Workspace is notifying us of the release of an Accesses 
		    // association.
                
                try
                {
                    LinkedList list = store.accesses();
                    releaseLink( getUserReference(), list, link );
                }
                catch( Exception e)
                {
                    String s = "failed to release Accesses association";
                    if( getLogger().isErrorEnabled() ) getLogger().error( s, e );
                    throw new org.omg.CORBA.INTERNAL( s );
                }
            }
            else if( link instanceof Owns )
            {
                
                // an Task is notifying this resource of the retraction of
                // an Owns association
                
                try
                {
                    LinkedList list = store.owns();
                    releaseLink( getUserReference(), list, link );
                }
                catch( Exception e)
                {
                    String s = "failed to release Owns association";
                    if( getLogger().isErrorEnabled() ) getLogger().error( s, e );
                    throw new org.omg.CORBA.INTERNAL( s );
                }
            }
            else if( link instanceof Member )
            {
                
                // an Membership is notifying this resource of the retraction of
                // an Member association
                
                try
                {
                    LinkedList list = store.memberships();
                    releaseLink( getUserReference(), list, link );
                }
                catch( Exception e)
                {
                    String s = "failed to release Member association";
                    if( getLogger().isErrorEnabled() ) getLogger().error( s, e );
                    throw new org.omg.CORBA.INTERNAL( s );
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
        getLogger().info("[USR] count");
        int count = 0;

        // abstract link types
        
        if( type.equal( PrivilegeHelper.type() ) ) 
	  {
		count = store.accesses().size() + store.owns().size();
	  }
        else if( type.equal( AccessHelper.type() ) ) 
	  {
	      count = store.accesses().size();
	  }
        else if( type.equal( AccessesHelper.type() ) ) 
	  {
		count = store.accesses().size();
	  }
        else if( type.equal( AdministersHelper.type() ) ) 
	  {
		int i = 0;
		LinkedList accesses = store.accesses();
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
				String error = "iterator throw a NoEntry while has_next is true inside User.count";
				if( getLogger().isErrorEnabled() ) getLogger().error( error, ne );
				throw new org.omg.CORBA.INTERNAL( error );
			  }
		    }
	      }
		count = i;
	  }
	  else
	  {
	      count = super.count( type );
	  }
        touch( store );
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
        getLogger().info("[USR] expand");
             
        if( type.equivalent( AccessesHelper.type() ))
        {
            LinkedList accesses = store.accesses();
            synchronized( accesses )
            {
                // prepare resource sequence
                links.value = create_link_sequence( accesses, max_number ); 
		    touch( store );
                LinkIteratorDelegate delegate = new LinkIteratorDelegate( orb, accesses, type );
	          LinkIteratorPOA servant = new LinkIteratorPOATie( delegate );
		    return servant._this( orb );
            }
        }
        else if( type.equivalent( OwnsHelper.type() ))
        {
            LinkedList owns = store.owns();
            synchronized( owns )
            {
                // prepare resource sequence
                links.value = create_link_sequence( owns, max_number ); 
		    touch( store );
                LinkIteratorDelegate delegate = new LinkIteratorDelegate( orb, owns, type );
	          LinkIteratorPOA servant = new LinkIteratorPOATie( delegate );
		    return servant._this( orb );
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
        synchronized( store )
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

   /**
    * Returns the object reference for this delegate.
    * @return User the object referenced for the delegate
    */
    public User getUserReference( )
    {
        return m_user;
    }

    protected ResourceFactoryService getResourceFactoryService()
    {
        return resourceFactoryService;
    }

    protected TaskService getTaskService()
    {
        return taskService;
    }

    protected WorkspaceService getWorkspaceService()
    {
        return workspaceService;
    }

    protected DesktopService getDesktopService()
    {
        return desktopService;
    }
    

}
