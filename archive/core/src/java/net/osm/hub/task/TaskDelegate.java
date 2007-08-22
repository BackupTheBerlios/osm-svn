

package net.osm.hub.task;

import org.apache.avalon.framework.context.Context;

import org.omg.CollaborationFramework.Processor;
import org.omg.CollaborationFramework.ProcessorHelper;
import org.omg.CollaborationFramework.Coordinates;
import org.omg.CollaborationFramework.CoordinatesHelper;
import org.omg.CollaborationFramework.CoordinatedBy;
import org.omg.CORBA.ORB;
import org.omg.CORBA.Any;
import org.omg.CORBA.TypeCode;
import org.omg.CORBA.NO_IMPLEMENT;
import org.omg.CORBA.BooleanHolder;
import org.omg.CosNotification.EventType;
import org.omg.CosCollection.AnySequenceHolder;
import org.omg.CosObjectIdentity.IdentifiableObject;
import org.omg.CosTime.TimeService;
import org.omg.CosNaming.NameComponent;
import org.omg.CosLifeCycle.NoFactory;
import org.omg.CosLifeCycle.NotRemovable;
import org.omg.CosNotification.StructuredEvent;
import org.omg.CosNotification.EventType;
import org.omg.CosNotification.EventHeader;
import org.omg.CosNotification.FixedEventHeader;
import org.omg.CosNotification.Property;
import org.omg.CosNotifyComm.InvalidEventType;
import org.omg.CosEventComm.Disconnected;
import org.omg.NamingAuthority.AuthorityId;
import org.omg.PortableServer.POA;
import org.omg.Session.LinkIterator;
import org.omg.Session.LinkIteratorPOA;
import org.omg.Session.LinkIteratorPOATie;
import org.omg.Session.OwnedBy;
import org.omg.Session.OwnedByHelper;
import org.omg.Session.Owns;
import org.omg.Session.User;
import org.omg.Session.UserHelper;
import org.omg.Session.task_state;
import org.omg.Session.ConsumedBy;
import org.omg.Session.Produces;
import org.omg.Session.ProducesHelper;
import org.omg.Session.ProducedBy;
import org.omg.Session.task_stateHelper;
import org.omg.Session.AbstractResource;
import org.omg.Session.AbstractResourceHelper;
import org.omg.Session.AbstractResourcesHelper;
import org.omg.Session.AbstractResourcesHolder;
import org.omg.Session.AbstractResourceIterator;
import org.omg.Session.AbstractResourceIteratorPOA;
import org.omg.Session.AbstractResourceIteratorHolder;
import org.omg.Session.AbstractResourceIteratorPOATie;
import org.omg.Session.ProcessorConflict;
import org.omg.Session.CannotStart;
import org.omg.Session.CannotStop;
import org.omg.Session.CannotSuspend;
import org.omg.Session.AlreadyRunning;
import org.omg.Session.CurrentlySuspended;
import org.omg.Session.NotRunning;
import org.omg.Session.SemanticConflict;
import org.omg.Session.ResourceUnavailable;
import org.omg.Session.Link;
import org.omg.Session.LinksHolder;
import org.omg.Session.LinkIteratorHolder;
import org.omg.Session.Consumes;
import org.omg.Session.ConsumesHelper;
import org.omg.Session.SessionSingleton;
import org.omg.Session.TaskHelper;
import org.omg.Session.BaseBusinessObjectKey;
import org.omg.CollaborationFramework.CoordinatedBy;
import org.omg.CollaborationFramework.ProcessorStateHelper;
import org.omg.CollaborationFramework.StateDescriptor;
import org.omg.CollaborationFramework.StateDescriptorHelper;

import net.osm.hub.pss.*;
import net.osm.hub.resource.AbstractResourceDelegate;
import net.osm.hub.resource.AbstractResourceIteratorDelegate;
import net.osm.hub.resource.LinkIteratorDelegate;
import net.osm.hub.resource.StructuredEventUtilities;
import net.osm.hub.gateway.CannotTerminate;
import net.osm.list.LinkedList;
import net.osm.list.Iterator;
import net.osm.list.NoEntry;
import net.osm.util.Incrementor;
//import net.osm.session.Task;

/**
 * Workspace defines private and shared places where resources, including
 * Task and Session objects, may be contained. Workspaces may contain
 * Workspaces.  The support for sharing and synchronizing the use of
 * objects available in Workspaces is provided by the objects and their
 * managers. Each Workspace may contain any collection of private and
 * shared objects that the objects and their managers provide access to,
 * and control use of.
 */

public class TaskDelegate extends AbstractResourceDelegate implements net.osm.session.TaskOperations
{
    // =====================================================================
    // static
    // =====================================================================
    
   /**
    * Event type event containing changes in task state.
    */
    public static final EventType taskStateEventType = SessionSingleton.taskStateEventType;

   /**
    * Event type event containing changes to Task ownership.
    */
    public static final EventType ownershipEventType = SessionSingleton.ownershipEventType;

    // =====================================================================
    // state
    // =====================================================================

   /**
    * Storage object representing this Task.
    */
    private TaskStorage store;
    
   /**
    * Object reference to this Task.
    */
    private org.omg.Session.Task m_task;    

    //=======================================================================
    // Initializable
    //=======================================================================
    
    public void initialize()
    throws Exception
    {
	  super.initialize();
        if( getLogger().isDebugEnabled() ) getLogger().debug( "initialization (task)" );
        this.store = (TaskStorage) super.getContext().getStorageObject();
	  setTaskReference( org.omg.Session.TaskHelper.narrow( getManager().getReference( 
          store.get_pid(), TaskHelper.id() ) ) );
    }

    // ===========================================================
    // NotifyPublishOperations
    // ===========================================================

   /**
    * The NotifyPublish interface supports an operation which allows a supplier of
    * Notifications to announce, or publish, the names of the types of events it will be
    * supplying, It is intended to be an abstract interface which is inherited by all
    * Notification Service consumer interfaces, and enables suppliers to inform consumers
    * supporting this interface of the types of events they intend to supply.
    */
    public void offer_change(EventType[] added, EventType[] removed)
    throws InvalidEventType
    {
    }

    // ===========================================================
    // StructuredPushConsumer
    // ===========================================================
    
   /**
    * TaskDelegate is implememted as a structure push consumed in order
    * to listen to structured events from the processor it is coordinating.
    * Task state is resolved based on incomming process state events.
    * @param event the processor state event
    * @exception Disconnected
    */
    public void push_structured_event(StructuredEvent event )
    throws Disconnected
    {
	  EventType type = event.header.fixed_header.event_type;
	  if( type.domain_name.equals("org.omg.collaboration") && type.type_name.equals("state") )
	  {
		// a process state event contains three filterable properties
	      // 1. timestamp
		// 2. source
		// 3. value
		// where value contains the processor state descriptor

		Property p = StructuredEventUtilities.getFilterableProperty( event, "value" );
		if( p == null )
		{
		    final String badContent = "missing state property in structured event";
		    if( getLogger().isWarnEnabled() ) getLogger().warn( badContent );
		    return;
		}

		StateDescriptor descriptor = StateDescriptorHelper.extract( p.value );
		task_state state = descriptor.processor_state;

		if( state == task_state.running )
		{
		    setStateRunning();
		}
		else if( state == task_state.suspended )
		{
		    setStateSuspended();
		}
		else if( state == task_state.terminated )
		{
		    setStateTerminated();
		}
		else if( state == task_state.completed )
		{
		    setStateCompleted();
		}
		else
		{
		    final String badContent = "bad content in structured event: ";
		    if( getLogger().isWarnEnabled() ) getLogger().warn( badContent + state );
		    System.out.println( badContent + state );
		    return;
            }
		if( descriptor.problems != null ) if( descriptor.problems.length > 0 )
		{
		    for( int i=0; i<descriptor.problems.length; i++ )
	          {
			  descriptor.problems[i].print();
		    }
		}
	  }
    }
    
   /**
    * The disconnect_structured_push_consumer operation is invoked to terminate a
    * connection between the target StructuredPushConsumer, and its associated supplier.
    * This operation takes no input parameters and returns no values. The result of this
    * operation is that the target StructuredPushConsumer will release all resources it had
    * allocated to support the connection, and dispose its own object reference.
    */
    public void disconnect_structured_push_consumer()
    {
    }

    private void setStateRunning()
    {
        if( getLogger().isDebugEnabled() ) getLogger().debug("setting task to runnning state");
        store.task_state( task_state.running );
	  post( newTaskStateEvent( task_state.running ));
    }

    public void setStateSuspended()
    {
        if( getLogger().isDebugEnabled() ) getLogger().debug("setting task to suspended state");
        store.task_state( task_state.suspended );
	  post( newTaskStateEvent( task_state.suspended ));
    }

    public void setStateTerminated()
    {
        if( getLogger().isDebugEnabled() ) getLogger().debug("setting task to terminated state");
        retractUsages();
        store.task_state( task_state.terminated );
	  post( newTaskStateEvent( task_state.terminated ));
    }

    public void setStateCompleted()
    {
        if( getLogger().isDebugEnabled() ) getLogger().debug("setting task to compeleted state");
        retractUsages();
        store.task_state( task_state.completed );
	  post( newTaskStateEvent( task_state.completed ));
    }

    //==================================================
    // Vulnerable implementation
    //==================================================

   /**
    * Test is this instance can be terminated or not.
    * @return boolean true if the persistent identity of this 
    * instance can be destroyed.
    */
    public boolean expendable( )
    {
        synchronized( store )
        {
            return super.expendable();
        }
    }

   /**
    * Destroys the persistent identity of this object. An 
    * implementation is responsible for disposing of references 
    * to external resources on completion of termiation actions. 
    * @exception CannotTerminate if the resource cannot be terminated
    */
    public void terminate( ) throws CannotTerminate
    {
	  synchronized( store )
        {
		if( !expendable() ) throw new CannotTerminate("resource is in use");
            if( getLogger().isDebugEnabled() ) getLogger().debug("terminate task");

            try
	      {
                if( store.coordinates().resource() != null ) try
		    {
			  store.coordinates().resource().release( new CoordinatedBy( getTaskReference() ));
		    }
		    catch( Throwable remote )
		    {
		        String warning = "ignoring failure to retract a CoordinatedBy association on a processor";
		  	  if( getLogger().isWarnEnabled() ) getLogger().warn( warning, remote );
		    }
                finally
                {
                    store.coordinates( new Coordinates() );
                }
                
                //
                // remove the OwnedBy link entry
                //
                
                if( getLogger().isDebugEnabled() ) getLogger().debug(
			"retracting owner association");
                OwnedBy ownership = store.owned_by();
		    if( ownership.resource() != null ) try
                {
                    ownership.resource().release( new Owns( getTaskReference() ));
			  release( ownership );
                }
                catch( Exception e)
                {
                    if( getLogger().isWarnEnabled() ) getLogger().warn(
				"failed to release Ownership from the User",e);
                }

		    //
		    // notify the consumed and produced resources of the retraction of 
		    // respective links
		    //


            }
            catch (Throwable e)
            {
                if( getLogger().isErrorEnabled() ) getLogger().error(
			"Unexpected termination exception", e );
                e.printStackTrace();
            }

		super.terminate();
        }
    }

    //==========================================================================
    // Structured Events
    //==========================================================================

   /**
    * Creation of a new 'process_state' StructuredEvent.
    * @param state process_state of the task
    * @return 'process_state' structured event
    */
    
    public StructuredEvent newTaskStateEvent( task_state state )
    {
        Property sourceProp = super.createSourceProperty( );
        Any stateHolder = orb.create_any();
        task_stateHelper.insert( stateHolder, state );
        Property stateProp = new Property( "task_state", stateHolder );
        return StructuredEventUtilities.createEvent( orb, taskStateEventType, 
		new Property[]
		{ 
			StructuredEventUtilities.timestamp( orb ), 
			sourceProp, 
			stateProp 
		}
	  );
    }
    
   /**
    * Creation of a new 'ownership' StructuredEvent.
    * @param user User that has assumed ownership
    * @return 'ownership' structured event
    */
    
    public StructuredEvent newOwnershipEvent( User user )
    {
        Property sourceProp = super.createSourceProperty( );
        Any userHolder = orb.create_any();
        UserHelper.insert( userHolder, user );
        Property ownerProp = new Property( "owner", userHolder );
        return StructuredEventUtilities.createEvent( orb, ownershipEventType, 
		new Property[]
		{ 
			StructuredEventUtilities.timestamp( orb ), 
			sourceProp, 
			ownerProp
		}
	  );
    }
    
    // ==========================================================================
    // Task
    // ==========================================================================
    
   /**
    * Task description.
    */
    public String description()
    {
	  touch( store );
        return store.description();
    }
    
   /**
    * Task description.
    */
    public void description( String value )
    {
        synchronized( store )
        {
		String old = new String(store.description());
		Any oldValue = orb.create_any();
		oldValue.insert_string( old );

		Any newValue = orb.create_any();
		newValue.insert_string( value );
            store.description( value );

		post( newUpdateEvent("description", oldValue, newValue ));
            modify( store );
        }
    }
    
   /**
    * The task state is determined by the state of its execution and the state of the
    * data content being processed. The task state and data state are related but
    * independent. The data state contains information about the application or
    * system object. The task state contains information about the task. For example,
    * when a fault simulator completes execution it is not necessarily true that the
    * fault simulation task has completed ? the completeness depends on the value
    * of the fault coverage. The value of the fault coverage is based on the data, what
    * has been called the "data state". The execution of the fault simulator is independent
    * of the results of the execution. Also the fault coverage may be changed, independent
    * of the fault simulator, if the parameters of the design are changed.
    * <P>
    * <table cellpadding=0 cellspacing=3 border=0>
    * <tr bgcolor="lightslategray" ><td width="30%">Value</td><td width="70%">Description</td></TR>
    * <tr><td>open</td><td>Task is not finished and active.</td></TR>
    * <tr><td>closed</td><td>Task is finished and inactive.</td></TR>
    * <tr><td>not_running</td><td>Task is active and quiescent, but ready to execute.</td></TR>
    * <tr><td>running</td><td>Task is active and executing.</td></TR>
    * <tr><td>notstarted</td><td>Task is active and ready to be initialized and started.</td></TR>
    * <tr><td>suspended</td><td>Task is active, has been started and suspended.</td></TR>
    * <tr><td>completed</td><td>Task is finished and completed normally.    </td></TR>
    * <tr><td>terminated</td><td>Task finished and stopped before normal completion.</td></TR>
    * </table>
    * <p>
    *
    * @return  task_state current state of the Task
    */
    
    public task_state get_state()
    {
	  return store.task_state();
    }
    
    
   /**
    * Returns the User that a Task is owned by.  This operation is
    * equivilient to expansion of the resource using the <code>OwnedBy</code>
    * Link typecode.
    *
    * @return  User owning this Task
    */
    public User owned_by()
    {
        touch( store );
        return UserHelper.narrow( store.owned_by().resource() );
    }
    
   /**
    * Sets the owner of the Task to the User referenced under the input
    * argument new_task_owner.
    *
    * @param  new_task_owner User - the User to assign as Task owner
    */
    public void set_owned_by(org.omg.Session.User new_task_owner)
    {
        synchronized( store )
        {
            store.owned_by( new OwnedBy( new_task_owner ));
            modify( store );
        }
    }
    
   /**
    * Adds a resource under a consumption relationship to this Task.
    *
    * @param  resource AbstractResource to be added as a consumed resource
    * @param  tag String role of the resource relative to the Task
    */
    public void add_consumed(AbstractResource r, String tag)
    {
        LinkedList list = store.consumes();
        synchronized( list )
        {
            try
            {
                Consumes link = new Consumes( r, tag );
                if( !containsLink( list, link ))
                {
			  ConsumedBy inverseLink = new ConsumedBy( getTaskReference(), tag );
                    r.bind( inverseLink );
                    addLink( getTaskReference(), list, link );
                }
                else
                {
                    // don't do anything because the resource is already here
                }
            }
            catch (Exception e)
            {
                String s = "failed to add resource to the Task's set of consumed resources";
                if( getLogger().isWarnEnabled() ) getLogger().warn(s,e);
		    e.printStackTrace();
                throw new org.omg.CORBA.INTERNAL(s);
            }
        }
        modify( store );
    }
    
   /**
    * Removes a resource currently assigned under a consuption relationship.
    *
    * @param  resource AbstractResource to be removed from the set of consumption relationships.
    */
    public void remove_consumed( String tag )
    {
        Consumes link = null;
        try
	  {
            link = get_consumed( tag );
	  }
	  catch( ResourceUnavailable ru )
        {
		final String warning = "ignoring attempt to remove an unknown tag: ";
		if( getLogger().isWarnEnabled() ) getLogger().warn( warning + tag , ru );
        }

        LinkedList list = store.consumes();
        synchronized( list )
        {
            try
            {
                releaseLink( getTaskReference(), list, link );
                
                // notify the contained resource that it is no
                // longer consumed
                
                try
                {
                    link.resource().release( new ConsumedBy( getTaskReference(), link.tag() ));
                }
                catch( Exception e )
                {
                    String s = "Remote resource raised an exception when notifying it " +
                    "of the release of a ConsumedBy association";
                    if( getLogger().isWarnEnabled() ) getLogger().warn(s, e);
                }
            }
            catch( NoEntry noEntry )
            {
                String problem = "Cannot remove a resource that isn't consumed.";
                if( getLogger().isErrorEnabled() ) getLogger().error( problem, noEntry );
                throw new org.omg.CORBA.INTERNAL( problem );
            }
        }
        modify( store );
    }
    
   /**
    * The list_consumed operation will return a list of all AbstractResource instances
    * consumed by this Task.
    *
    * @param  max_number long the maximum number of AbstractResources instances to
    * include in the returned AbstractResources sequence.
    * @param  resources Session::AbstractResources a sequence of AbstractResources instances of
    * a length no greater than max_number that are consumed by this Task.
    * @param  resourceit AbstractResourceIterator an iterator of the consumed AbstractResource instances
    * @param  linkit LinkIterator an iterator of the Link instances
    * @osm.warning support for linkit iterator not currently implemented
    */
    public void list_consumed(int max_number, AbstractResourcesHolder resources, AbstractResourceIteratorHolder resourceit, LinkIteratorHolder linkit)
    {        

	  AbstractResourceIterator iterator = null;
        LinkedList list = store.consumes();
        synchronized( list )
        {
	      try
	      {
	          AbstractResourceIteratorDelegate delegate = new AbstractResourceIteratorDelegate( orb, list.iterator() );
	          AbstractResourceIteratorPOA servant = new AbstractResourceIteratorPOATie( delegate );
		    iterator = servant._this( orb );

		    AnySequenceHolder anysHolder = new AnySequenceHolder();
		    BooleanHolder booleanHolder = new BooleanHolder();
		    if( iterator.retrieve_next_n_elements( max_number, anysHolder, booleanHolder ) )
		    {
			  int k = anysHolder.value.length;
			  AbstractResource[] sequence = new AbstractResource[k];
		        for( int i=0; i<k; i++ )
			  {
				sequence[i] = AbstractResourceHelper.extract( anysHolder.value[i]);
		        }
			  resources.value = sequence;
		    }
		    else
		    {
		        resources.value = new AbstractResource[0];
		    }
		}
		catch( Exception e )
		{
		    String error = "failed to establish AbstractResourceIterator" ;
	          if( getLogger().isErrorEnabled() ) getLogger().error( error, e );
		    throw new org.omg.CORBA.INTERNAL( error );
		}
        }
	  resourceit.value = iterator;
        touch( store );
    }

   /**
    * The get_consumed operation returns a single Consumed link instances
    * referenced by the Task under the supplied usage role.
    * @osm.note this is a supplimentary OSM operation 
    * @param role the name of the role identifying the consumption link
    * @exception ResourceUnavailable if the request role is not bound to a resource
    */
    public Consumes get_consumed( String role ) throws ResourceUnavailable
    {
        LinkedList list = store.consumes();
        synchronized( list )
        {
		Iterator iterator = list.iterator();
		while( iterator.has_next() )
	      {
		    try
		    {
			  LinkStorage linkStore = (LinkStorage) iterator.next();
		        Consumes link = (Consumes) linkStore.link();
		        if( link.tag().equals( role ) ) return link;
		    }
	          catch( Exception e )
		    {		  
			  if( getLogger().isWarnEnabled() ) getLogger().warn("get_consumed/exception: " + e );
	          }
	      }
		throw new ResourceUnavailable();
        }
    }
    
    
   /**
    * Add an AbstractResource under a production relationship.
    *
    * @param  resource Session::AbstractResources - the resource to add
    * @param  tag String - the role of the resource towards the Task
    */
    public void add_produced(AbstractResource r, String tag)
    {
        LinkedList list = store.produces();
        synchronized( list )
        {
            try
            {
                Produces link = new Produces( r, tag );
                if( !containsLink( list, link ))
                {
                    r.bind( new ProducedBy( getTaskReference(), tag ));
                    addLink( getTaskReference(), list, link );
                }
                else
                {
                    // don't do anything because the resource is already here
                }
            }
            catch (Exception e)
            {
                String s = "failed to add resource to the Task's set of produced resources";
                if( getLogger().isWarnEnabled() ) getLogger().warn(s,e);
                throw new org.omg.CORBA.INTERNAL(s);
            }
        }
    }
    
   /**
    * Removes a resource currently assigned under a production relationship.
    *
    * @param  resource AbstractResource to be removed from the set of production relationships.
    */
    public void remove_produced(AbstractResource r)
    {
        LinkedList list = store.produces();
        synchronized( list )
        {
            try
            {
                Produces link = new Produces ( r );
                releaseLink( getTaskReference(), list, link );
                
                // notify the contained resource that it is no
                // longer produced by this Task
                
                try
                {
                    r.release( new ProducedBy( getTaskReference(), "*" ));
                }
                catch( Exception e )
                {
                    String s = "Remote resource raised an exception when notifying it " +
                    "of the release of a ProducedBy association";
                    if( getLogger().isWarnEnabled() ) getLogger().warn(s, e);
                }
            }
            catch( NoEntry noEntry )
            {
                String problem = "Cannot remove a resource that isn't produced by this Task.";
                if( getLogger().isWarnEnabled() ) getLogger().warn( problem, noEntry );
                throw new org.omg.CORBA.INTERNAL( problem );
            }
        }
    }
    
    
   /**
    * The list_produced operation will return a list of all AbstractResource instances
    * produced by this Task.
    * @param  max_number long the maximum number of AbstractResources instances to
    * include in the returned AbstractResources sequence.
    * @param  resources Session::AbstractResources a sequence of AbstractResources instances of
    * a length no greater than max_number that are produced by this Task.
    * @param  resourceit AbstractResourceIterator an iterator of the produced AbstractResolurce instances
    * @param  linkit LinkIterator - an iterator of the Link instances corresponding to
    * the class <code>Produces</code>
    */
    public void list_produced(int max_number, AbstractResourcesHolder resources, AbstractResourceIteratorHolder resourceit, LinkIteratorHolder linkit)
    {

	  AbstractResourceIterator iterator = null;
        LinkedList list = store.produces();
        synchronized( list )
        {
	      try
	      {
	          AbstractResourceIteratorDelegate delegate = new AbstractResourceIteratorDelegate( orb, list.iterator() );
	          AbstractResourceIteratorPOA servant = new AbstractResourceIteratorPOATie( delegate );
		    iterator = servant._this( orb );

		    AnySequenceHolder anysHolder = new AnySequenceHolder();
		    BooleanHolder booleanHolder = new BooleanHolder();
		    if( iterator.retrieve_next_n_elements( max_number, anysHolder, booleanHolder ) )
		    {
			  int k = anysHolder.value.length;
			  AbstractResource[] sequence = new AbstractResource[k];
		        for( int i=0; i<k; i++ )
			  {
				sequence[i] = AbstractResourceHelper.extract( anysHolder.value[i]);
		        }
			  resources.value = sequence;
		    }
		    else
		    {
		        resources.value = new AbstractResource[0];
		    }
		}
		catch( Exception e )
		{
		    String error = "failed to establish AbstractResourceIterator" ;
	          if( getLogger().isErrorEnabled() ) getLogger().error( error, e );
		    throw new org.omg.CORBA.INTERNAL( error );
		}
        }
	  resourceit.value = iterator;
        touch( store );
    }
    
    
   /**
    * Sets the processor for this Task.
    * An implementation of set_process must ensure that appropriate bind
    * and release operations are invoked on the processor resources in order to
    * ensure referential integrity.  When a task is initially created, the implementation
    * is responsible for invocation of the bind operation on the abstract resource that
    * is serving as the processor, using the ProcessedBy link kind.  Subsequent
    * invocations of set_processor are responsible for the releasing and
    * re-establishing processed_by links on the old and new process resource using
    * the release and bind operation on the respective process resources.
    *
    * @param  processor Session::AbstractResource
    * the processor running this Task
    * @exception  ProcessorConflict
    * if the processor cannot accept processing responsibility
    */
    public void set_processor( AbstractResource r ) throws ProcessorConflict
    {
        // make sure the processor argument is a type of Processor
        Processor processor = null;
        try
        {
            processor = ProcessorHelper.narrow( r );
        }
        catch (Exception e)
        {
            String s = "supplied processor argument is not derived org.omg.CollaborationFramework.Processor";
            if( getLogger().isErrorEnabled() ) getLogger().error( s, e );
        }
        
        // bind to the processor
        try
        {
            synchronized( store )
            {
                CoordinatedBy link = new CoordinatedBy( getTaskReference() );
		    if( get_processor() == null )
		    {
                    processor.bind( link );
                    bind( new Coordinates( processor ) );
		    }
		    else
		    {
                    processor.bind( link );
			  Processor currentProcessor = ProcessorHelper.narrow( get_processor());
                    replace( new Coordinates( currentProcessor ), new Coordinates( processor ) );
		    }
            }
        }
        catch (Exception e)
        {
            String s = "failed to bind/replace processor";
            if( getLogger().isErrorEnabled() ) getLogger().error( s, e );
        }
    }
    
   /**
    * Returns a Processor associated to this Task.
    *
    * @return  AbstractResource processor of the Task.
    */
    public AbstractResource get_processor()
    {
        return store.coordinates().resource();
    }
    
   /**
    * Starts a Task.
    * @exception  CannotStart if the processor cannot be started.
    * @exception  AlreadyRunning if the processor is already in a running state.
    */
    public void start()
    throws CannotStart, AlreadyRunning
    {
        synchronized( store.task_state() )
        {      

            // make sure we have an attached processor and that the current state is valid
            // for this action

            if( store.coordinates().resource() == null ) throw new CannotStart();            
            task_state current = store.task_state();
            if( current == task_state.running ) throw new AlreadyRunning();
            if( current != task_state.notstarted ) if( current != task_state.suspended ) throw new CannotStart();
            
            // start the processor

            Processor processor = ProcessorHelper.narrow( store.coordinates().resource() );
            processor.start();
        }
    }
    
   /**
    * Suspends a Task.
    * @exception  CannotSuspend if the processor cannot be suspended.
    * @exception  CurrentlySuspended if the processor is already in a suspended state.
    */
    public void suspend() throws CannotSuspend, CurrentlySuspended
    {
        synchronized( store.task_state() )
        {
            if( store.coordinates().resource() == null ) throw new CannotSuspend();
            
            task_state current = store.task_state();
            if( current != task_state.running ) throw new CannotSuspend();
            if( current == task_state.suspended ) throw new CurrentlySuspended();
            
            try
            {
		    //System.out.println("[TSK] suspending");
                Processor p = ProcessorHelper.narrow( store.coordinates().resource());
                p.suspend();
            }
            catch( CurrentlySuspended e )
            {
                // processor is already suspended so update our state
                store.task_state( task_state.suspended );
		    post( newTaskStateEvent( store.task_state() ));
            }
            catch( Exception e )
            {
                if( getLogger().isWarnEnabled() ) getLogger().warn("cannot suspend the processor", e );
                throw new CannotSuspend();
            }
        }
    }
    
   /**
    * Stops a Task resulting in the release of all consumption and production
    * usage association, the destuction of the associated Processor.
    *
    * @exception  CannotStop if the processor cannot be stopped.
    * @exception  NotRunning if the processor is not in a running state.
    */
    public void stop() throws CannotStop, NotRunning
    {
        synchronized( store )
        {            
            task_state current = store.task_state();
            if( current != task_state.running ) throw new NotRunning();
            
            try
            {
                Processor p = ProcessorHelper.narrow( store.coordinates().resource());
                if( p != null )
		    {
                    if( getLogger().isDebugEnabled() ) getLogger().debug("stopping the processor" );
			  p.stop();
		    }
            }
            catch( NotRunning e )
            {
                if( getLogger().isWarnEnabled() ) getLogger().warn("processor is not running", e );
                throw new NotRunning();
            }
            catch( Exception e )
            {
                if( getLogger().isWarnEnabled() ) getLogger().warn("cannot stop the processor", e );
                throw new CannotStop();
            }
        }
    }

    // ==========================================================================
    // AbstractResource operation override
    // ==========================================================================
    
   /**
    *  The most derived type that this <code>Task</code> represents.
    */
    
    public TypeCode resourceKind()
    {
        getLogger().info("resourceKind");
        touch( store );
        return TaskHelper.type();
    }
    
   /**
    * Extension of the AbstractResource bind operation to support reception of the
    * notification of association dependencies from external resources on this
    * Task.
    *
    * @param link Link notification of an association dependency
    */
    
    public synchronized void bind(Link link)
    throws ResourceUnavailable, ProcessorConflict, SemanticConflict
    {
        synchronized( store )
        {
            
            if( getLogger().isDebugEnabled() ) getLogger().debug("bind: " + link.getClass().getName());
            touch( store );
            
            if( link instanceof OwnedBy )
            {
                // Notification to this Task that its ownership has been bound
                // to a particular user.
                
                if( store.owned_by().resource() != null ) throw new SemanticConflict();
                store.owned_by( (OwnedBy) link );
                if( getLogger().isDebugEnabled() ) getLogger().debug("[TSK] bind "
                + "\n\tSOURCE: " + store.name()
                + "\n\tLINK: " + link.getClass().getName()
                + "\n\tTARGET: " + link.resource().name() );

		    // ISSUE:
		    // The T/S specification calls for two events
		    // Ownership event should be dropped

                post( newBindEvent( link ));
                post( newOwnershipEvent( UserHelper.narrow( link.resource() ) ) );
            }
            else if( link instanceof Coordinates )
            {
                // Notification to this Task that it is being bound as Coordinator
                // of a Processor
                
                if( store.coordinates().resource() != null ) throw new SemanticConflict();
                store.coordinates( (Coordinates) link );
                if( getLogger().isDebugEnabled() ) getLogger().debug("[TSK] bind "
                + "\n\tSOURCE: " + store.name()
                + "\n\tLINK: " + link.getClass().getName()
                + "\n\tTARGET: " + link.resource().name() );
                post( newBindEvent( link ));
            }
            else if( link instanceof Consumes )
            {
                
                // Notification to this Task that an AbstractResource is being added to
                // to the set of resources that it is consuming.
                
                LinkedList list = store.consumes();
                synchronized( list )
                {
                    if( !containsLink( list, link ))
                    {
                        addLink( getTaskReference(), list, link );
                    }
                    else
                    {
                        throw new SemanticConflict();
                    }
                }
            }
            else if( link instanceof Produces )
            {
                
                // Notification to this Task that an AbstractResource is being added to
                // to the set of resources that it is producing.
                
                LinkedList list = store.produces();
                synchronized( list )
                {
                    if( !containsLink( list, link ))
                    {
                        addLink( getTaskReference(), list, link );
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
    * Replaces an existing Task dependecy with another.
    *
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
            if( getLogger().isDebugEnabled() ) getLogger().debug("[TSK] replace");
            touch( store );
            if(( old instanceof OwnedBy ) && ( _new instanceof OwnedBy ))
            {
                
                // client is requesting the replacement of an exiting OwnedBy
                // association from a User to another User
                
                if( store.owned_by().resource() != null )
                {
                    if( store.owned_by().resource().get_key().equal( old.resource().get_key() ))
                    {
                        store.owned_by( (OwnedBy) _new );
                		if( getLogger().isDebugEnabled() ) getLogger().debug("[TSK] replace "
                			+ "\n\tSOURCE: " + store.name()
                			+ "\n\tLINK: " + old.getClass().getName()
                			+ "\n\tOLD: " + old.resource().name()
                			+ "\n\tNEW: " + _new.resource().name() );
                        post( newReplaceEvent( old, _new ));
                        modify( store );
                        return;
                    }
                    else
                    {
                        if( getLogger().isDebugEnabled() ) getLogger().debug(
				  "supplied 'old' value does not match current 'OwnedBy' link");
                        throw new SemanticConflict();
                    }
                }
                else if( old.resource() == null )
                {
                    store.owned_by( (OwnedBy) _new );
                	  if( getLogger().isDebugEnabled() ) getLogger().debug("[TSK] replace "
                			+ "\n\tSOURCE: " + store.name()
                			+ "\n\tLINK: " + old.getClass().getName()
                			+ "\n\tNEW: " + _new.resource().name() );
                    post( newReplaceEvent( old, _new ));
                    modify(store);
                    return;
                }
                else
                {
                    if( getLogger().isDebugEnabled() ) getLogger().debug(
			    "old argument cannot replace a non assigned link");
                    throw new SemanticConflict();
                }
            }
            else if(( old instanceof Coordinates ) && ( _new instanceof Coordinates ))
            {
                
                // client is requesting a change in the processor this task is
                // coordinating
                
                if( store.coordinates().resource() != null )
                {
                    if( store.coordinates().resource().get_key().equal( old.resource().get_key() ))
                    {
                        store.coordinates( (Coordinates) _new );
                		if( getLogger().isDebugEnabled() ) getLogger().debug("[TSK] replace "
                			+ "\n\tSOURCE: " + store.name()
                			+ "\n\tLINK: " + old.getClass().getName()
                			+ "\n\tOLD: " + old.resource().name()
                			+ "\n\tNEW: " + _new.resource().name() );
                        post( newReplaceEvent( old, _new ));
                        modify( store );
                        return;
                    }
                    else
                    {
                        if( getLogger().isDebugEnabled() ) getLogger().debug(
				  "supplied 'old' value does not match current 'Coordinates' link");
                        throw new SemanticConflict();
                    }
                }
                else if( old.resource() == null )
                {
                    store.coordinates( (Coordinates) _new );
                		if( getLogger().isDebugEnabled() ) getLogger().debug("[TSK] replace "
                			+ "\n\tSOURCE: " + store.name()
                			+ "\n\tLINK: " + old.getClass().getName()
                			+ "\n\tOLD: " + old.resource().name()
                			+ "\n\tNEW: " + _new.resource().name() );
                    post( newReplaceEvent( old, _new ));
                    modify(store);
                    return;
                }
                else
                {
                    if( getLogger().isDebugEnabled() ) getLogger().debug(
			    "old argument cannot replace a non assigned link");
                    throw new SemanticConflict();
                }
            }
            else if (( old instanceof Consumes) && ( _new instanceof Consumes))
            {
                // client is requesting the replacement of an exiting Consumes
                // association
                
                try
                {
                    replaceLink( getTaskReference(), store.consumes(), old, _new );
                }
                catch (Exception e)
                {
                    String s = "unexpected exception while resolving 'Consumes' references";
                    if( getLogger().isErrorEnabled() ) getLogger().error(s,e);
                    throw new org.omg.CORBA.INTERNAL(s);
                }
            }
            else if (( old instanceof Produces) && ( _new instanceof Produces ))
            {
                // client is requesting the replacement of an exiting Produces
                // association
                
                try
                {
                    replaceLink( getTaskReference(), store.produces(), old, _new );
                }
                catch (Exception e)
                {
                    String s = "unexpected exception while resolving 'Produces' references";
                    if( getLogger().isErrorEnabled() ) getLogger().error(s,e);
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
    * Releases an existing dependecy where the dependency is one of OwnedBy, 
    * or a usage dependency coresponding to a Produces or Consumes association.
    *
    * @param link Link to retract
    */
    public synchronized void release(Link link)
    {
        synchronized( store )
        {
            
            getLogger().info("[TSK] release " + link.getClass().getName());
            touch( store );
            
            if( link instanceof OwnedBy )
            {
                
                // a User is notifying this resource of the retraction of an OwnedBy
                // association - which implies that this Task has no further reason to
                // exist because a Task is bound to exactly one owning User.
                
                if( store.owned_by().resource() != null )
                {
                    try
                    {
                        BaseBusinessObjectKey thisKey = store.owned_by().resource().get_key();
                        if( thisKey.equal( link.resource().get_key() ) )
                        {

				    // retraction of a OwnedBy association triggers the removal of 
				    // this Task

                            store.owned_by( new OwnedBy() );
                		    if( getLogger().isDebugEnabled() ) getLogger().debug("[TSK] release "
                			+ "\n\tSOURCE: " + store.name()
                			+ "\n\tLINK: " + link.getClass().getName()
                			+ "\n\tTARGET: " + link.resource().name() );
                            post( newReleaseEvent( link ));
                        }
                    }
                    catch( Exception e)
                    {
                        String s = "failed to release OwnedBy association on Task";
                        if( getLogger().isErrorEnabled() ) getLogger().error( s, e );
                        throw new org.omg.CORBA.INTERNAL( s );
                    }
                }
            }
            else if( link instanceof Coordinates )
            {
                
                // a client is notifying this task of the retraction of an Coordinates
                // association
                
                if( store.coordinates().resource() != null )
                {
                    try
                    {
                        BaseBusinessObjectKey thisKey = store.coordinates().resource().get_key();
                        if( thisKey.equal( link.resource().get_key() ) )
                        {
                            store.coordinates( new Coordinates() );
                		    if( getLogger().isDebugEnabled() ) getLogger().debug("[TSK] release "
                			+ "\n\tSOURCE: " + store.name()
                			+ "\n\tLINK: " + link.getClass().getName()
                			+ "\n\tTARGET: " + link.resource().name() );
                            post( newReleaseEvent( link ));
                            modify( store );
                        }
                    }
                    catch( Exception e)
                    {
                        String s = "failed to release Coordinates association on Task";
                        if( getLogger().isErrorEnabled() ) getLogger().error( s, e );
                        throw new org.omg.CORBA.INTERNAL( s );
                    }
                }
            }
            else if( link instanceof Consumes )
            {
                
                // Notification of the retract of a Consumption depndency.
                
                try
                {
                    LinkedList list = store.consumes();
                    releaseLink( getTaskReference(), list, link );
                }
                catch( Exception e)
                {
                    String s = "failed to release Consumes association";
                    if( getLogger().isErrorEnabled() ) getLogger().error( s, e );
                    throw new org.omg.CORBA.INTERNAL( s );
                }
            }
            else if( link instanceof Produces )
            {
                
                // an AbstractResource is notifying this resource of the retraction of
                // an Produces association
                
                try
                {
                    LinkedList list = store.produces();
                    releaseLink( getTaskReference(), list, link );
                }
                catch( Exception e)
                {
                    String s = "failed to release Produces association";
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
    * Expansion of links maintained by this Task.
    * If the supplied typecode corresponds to OwnedBy or Coordinates, the iterator return
    * will be null and the single link instance will be returned in the supplied link holder.
    * If the typecode corresponds to the Consumes or Produces link class, this implementation
    * will handle the response otherwise the expand operation will be delegated to the 
    * supertype.  Support for abstract link expansion is not available at this stage.  
    *
    * @param link Link to retract
    */
    public LinkIterator expand(org.omg.CORBA.TypeCode type, int max_number, LinksHolder links)
    {
        getLogger().info("[TSK] expand");
        LinkIterator linkIterator = null; 

	  if( type.equivalent( OwnedByHelper.type() ))
	  {
		links.value = new Link[]{ store.owned_by() };
		touch( store );
		return null;
	  }
	  else if( type.equivalent( CoordinatesHelper.type() ))
	  {
		links.value = new Link[]{ store.coordinates() };
		touch( store );
		return null;
	  }
        else if( type.equivalent( ConsumesHelper.type() ))
        {
            LinkedList consumes = store.consumes();
            synchronized( consumes )
            {
                // prepare resource sequence
                links.value = create_link_sequence( consumes, max_number ); 
		    touch( store );
                LinkIteratorDelegate delegate = new LinkIteratorDelegate( orb, consumes, type );
	          LinkIteratorPOA servant = new LinkIteratorPOATie( delegate );
		    return servant._this( orb );
            }
        }
        else if( type.equivalent( ProducesHelper.type() ))
        {
            LinkedList produces = store.produces();
            synchronized( produces )
            {
                // prepare resource sequence
                links.value = create_link_sequence( produces, max_number ); 
		    touch( store );
                LinkIteratorDelegate delegate = new LinkIteratorDelegate( orb, produces, type );
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
    // BaseBusinessObject operation override
    // ==========================================================================
    
   /**
    * TaskDelegate provides support for the removal of a Task resource that has no
    * standing usage associations. As such, a Task cannot be removed if it is 
    * consuming or producing one or more resources.  A client application is required 
    * to stop the Task (or wait for normal Task completion).  In the case of a stop,
    * and normal completion the Task will destroy it associated processor,
    * release usage dependecies and destroy itself by internally invoking the remove 
    * operation.
    */
    
    public synchronized void remove()
    throws NotRemovable
    {
        synchronized( store )
        {
            if( getLogger().isDebugEnabled() ) getLogger().debug("[TSK] remove");
		try
		{
                terminate();
		}
		catch(CannotTerminate e)
		{
		    if( getLogger().isErrorEnabled() ) getLogger().error( e.getMessage(), e );
		    throw new NotRemovable( e.getMessage() ); // should not happen
		}
		try
		{
 		    dispose();
		}
		catch(Exception e)
		{
		    System.err.println("[TSK] unexpected error while removing Task");
		    e.printStackTrace();
		}
        }
    }
    
    // ==========================================================================
    // utilities
    // ==========================================================================
    
   /**
    * Set the object reference to be returned for this delegate.
    * @param workspace the object reference for the workspace
    */
    protected void setTaskReference( org.omg.Session.Task task )
    {
        m_task = task;
        setReference( task );
    }

   /**
    * Returns the object reference for this delegate.
    * @return the object referenced for the delegate
    */
    protected org.omg.Session.Task getTaskReference( )
    {
        return m_task;
    }

   /**
    * Internal implementation of the remove operation supporting
    * retraction of Consumes, Produces association.
    */
    protected synchronized void retractUsages()
    {
        
        synchronized( store )
        {
            
            getLogger().info("[TSK] retracting usage associations");
            
            try
            {
                //
                // remove the Produces link entries
                //
                
                LinkedList produces = store.produces();
                Iterator iterator = produces.iterator();
                while( iterator.has_next() )
                {                
                    LinkStorage s = (LinkStorage) iterator.next();
			  Produces p = (Produces) s.link();
			  try
		        {
			      p.resource().release( new ProducedBy( getTaskReference(), p.tag() ));
			  }
			  catch( Throwable e )
			  {
				final String warning = "ignoring remote exeception while notifying a resource of" 
				  + " the retraction of a produced by association";
				if( getLogger().isWarnEnabled() ) getLogger().warn( warning, e );
		        }
		    }
                
                //
                // remove the Consumes link entries
                //
                
                LinkedList consumers = store.consumes();
                Iterator iterator2 = consumers.iterator();
                while( iterator2.has_next() )
                {                
                    LinkStorage s = (LinkStorage) iterator2.next();
			  Consumes c = (Consumes) s.link();
			  try
		        {
			      c.resource().release( new ConsumedBy( getTaskReference(), c.tag() ));
			  }
			  catch( Throwable e )
			  {
				final String warning = "ignoring remote exeception while notifying a resource of" 
				  + " the retraction of a consumption association";
				if( getLogger().isWarnEnabled() ) getLogger().warn( warning, e );
		        }
		    }
            }
            catch (Exception e)
            {
                if( getLogger().isErrorEnabled() ) getLogger().error(
			"Unexpected error during retractual of usage dependecies",e);
            }
        }
    }

    //==========================================================================
    // Disposable
    //==========================================================================

   /**
    * Clean up state members.
    */ 
    public synchronized void dispose()
    {
 	  if( getLogger().isDebugEnabled() ) getLogger().debug("[TSK] dispose");
        this.store = null;
        this.m_task = null;
	  super.dispose();
    }
}
