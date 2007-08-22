

package net.osm.session.processor;

import java.util.Vector;

import org.apache.avalon.framework.context.Context;
import org.apache.avalon.framework.context.DefaultContext;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.ServiceException;

import org.apache.excalibur.merlin.UnitInfo;

import org.omg.CORBA.ORB;
import org.omg.CORBA.Any;
import org.omg.CORBA.TypeCode;
import org.omg.CORBA.NO_IMPLEMENT;
import org.omg.CORBA.BooleanHolder;
import org.omg.CosCollection.AnySequenceHolder;
import org.omg.CosObjectIdentity.IdentifiableObject;
import org.omg.CosTime.TimeService;
import org.omg.CosNaming.NameComponent;
import org.omg.CosLifeCycle.NoFactory;
import org.omg.CosLifeCycle.NotRemovable;
import org.omg.NamingAuthority.AuthorityId;
import org.omg.CosNotification.EventType;
import org.omg.CosNotification.Property;
import org.omg.CosNotification.StructuredEvent;
import org.omg.PortableServer.POA;
import org.omg.Session.LinkIterator;
import org.omg.Session.AbstractResource;
import org.omg.Session.AbstractResourceHelper;
import org.omg.Session.AbstractResourceIteratorPOA;
import org.omg.Session.AbstractResourceIteratorPOATie;
import org.omg.Session.AbstractResourceIteratorHelper;
import org.omg.Session.LinkIterator;
import org.omg.Session.LinkIteratorPOA;
import org.omg.Session.LinkIteratorPOATie;
import org.omg.Session.AbstractResourcesHolder;
import org.omg.Session.AbstractResourceIterator;
import org.omg.Session.AbstractResourceIteratorHolder;
import org.omg.Session.task_state;
import org.omg.Session.task_stateHelper;
import org.omg.Session.User;
import org.omg.Session.Link;
import org.omg.Session.LinksHolder;
import org.omg.Session.ResourceUnavailable;
import org.omg.Session.ProcessorConflict;
import org.omg.Session.SemanticConflict;
import org.omg.Session.AccessedByHelper;
import org.omg.Session.AccessedBy;
import org.omg.Session.CollectedBy;
import org.omg.Session.ComposedOf;
import org.omg.Session.ComposedOfHelper;
import org.omg.Session.Collects;
import org.omg.Session.CollectsHelper;
import org.omg.Session.AdministeredBy;
import org.omg.Session.Administers;
import org.omg.Session.IsPartOf;
import org.omg.Session.ContainmentHelper;
import org.omg.Session.CannotStart;
import org.omg.Session.CannotStop;
import org.omg.Session.AlreadyRunning;
import org.omg.Session.CannotSuspend;
import org.omg.Session.CurrentlySuspended;
import org.omg.Session.NotRunning;

import org.apache.orb.util.LifecycleHelper;
import org.apache.pss.ActivatorService;
import org.apache.pss.util.Incrementor;
import org.apache.time.TimeUtils;

import net.osm.adapter.Adapter;
import net.osm.session.CannotTerminate;
import net.osm.session.SessionSingleton;
import net.osm.session.Executes;
import net.osm.session.ExecutedBy;
import net.osm.session.ExecutesHelper;
import net.osm.session.DefaultExecutes;
import net.osm.session.DefaultExecutedBy;
import net.osm.session.user.UserService;
import net.osm.session.resource.AbstractResourceDelegate;
import net.osm.session.linkage.LinkIteratorDelegate;
import net.osm.session.linkage.LinkStorage;
import net.osm.list.LinkedList;
import net.osm.list.Iterator;
import net.osm.list.NoEntry;
import net.osm.sps.StructuredEventUtilities;

/**
 * Implementation of a generic processor framework that uses pluggable adapters
 * supporting process specfic execution behaviour.
 */
public class ProcessorDelegate extends AbstractResourceDelegate 
implements ProcessorOperations, ApplianceListener
{

    //======================================================================
    // static
    //======================================================================

    public static final String PROCESSOR_KEY = "processor";

    public static final String PROCESSOR_STATE_KEY = "processor.state";

   /**
    * Event type event containing changes in process state.  The 
    * event type used is the Task state event event value because
    * the TS specifiation does not define a processor event.
    */
    public static final EventType processorStateEventType = SessionSingleton.taskStateEventType;

    //======================================================================
    // state
    //======================================================================
    
   /**
    * Storage object representing this Processor.
    */
    private ProcessorStorage m_store;
    
   /**
    * Object reference to this Processor.
    */
    private Processor m_processor;

   /**
    * Internal reference to the processor service.
    */
    private ProcessorService m_processor_service;

   /**
    * Internal reference to the user service.
    */
    private UserService m_user_service;

   /**
    * Internal reference to the processor appliance.
    */
    private Appliance m_appliance;

   /**
    * Internal reference to the thread within which the appliance will execute.
    */
    private Thread m_thread;

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
        m_processor_service = (ProcessorService) manager.lookup(
          ActivatorService.ACTIVATOR_KEY );
        m_user_service = (UserService) manager.lookup( 
          UserService.USER_SERVICE_KEY );
    }

    protected UserService getUserService()
    {
        return m_user_service;
    }

    protected ProcessorService getProcessorService()
    {
        return m_processor_service;
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

        m_store = (ProcessorStorage) super.getStorageObject();
        
	  setProcessorReference( 
          ProcessorHelper.narrow( 
            m_processor_service.getProcessorReference( m_store ) ) );

        //
        // load the appliance
        //

        try
        {
            UnitInfo info = new UnitInfo( m_store.appliance().replace('.','/') );
            getLogger().debug("appliance:\n " + info.toString() );

            ClassLoader loader = Thread.currentThread().getContextClassLoader();
            m_appliance = (Appliance) info.getBaseClass().newInstance();

            Configuration conf = info.getConfiguration();
            m_store.description( 
              conf.getChild("description").getValue("No description available."));

            DefaultContext context = new DefaultContext();
            context.put( PROCESSOR_KEY, this );

            int state = 0;
            switch( m_store.processor_state() )
            {
              case task_state._not_running:
                state = Appliance.IDLE;
                break;
              case task_state._running:
                state = Appliance.RUNNING;
                break;
              case task_state._suspended:
                state = Appliance.SUSPENDED;
                break;
              case task_state._terminated:
                state = Appliance.TERMINATED;
                break;
              case task_state._completed:
                state = Appliance.COMPLETED;
                break;
            }

            context.put( PROCESSOR_STATE_KEY, new Integer( state ));
            
            LifecycleHelper.pipeline( 
              m_appliance, 
              getLogger().getChildLogger("appliance"),
              context,
              info.getConfiguration(),
              null
            );

            m_appliance.addApplianceListener( this );
		m_thread = new Thread( (Runnable) m_appliance );
		m_thread.start();

        }
        catch( Throwable e )
        {
            final String error = "Unexpected exception while attempting to establish appliance.";
            throw new ProcessorException( error, e );
        }

    }
    
    // ==========================================================================
    // Disposable
    // ==========================================================================

   /**
    * Clean up state members.
    */ 
    public synchronized void dispose()
    {
        this.m_store = null;
        this.m_processor = null;
	  super.dispose();
    }

    // ==========================================================================
    // ApplianceListener
    // ==========================================================================

   /**
    * Method invoked when the state of an appliance changes.  
    */
    public void stateChanged( ApplianceEvent event )
    {
        synchronized( m_store )
        {
            int before = m_store.processor_state();
            switch( event.getState() )
            {
              case Appliance.IDLE:
                m_store.processor_state( task_state._notstarted );
                break;
              case Appliance.RUNNING:
                m_store.processor_state( task_state._running );
                break;
              case Appliance.SUSPENDED:
                m_store.processor_state( task_state._suspended );
                break;
              case Appliance.TERMINATED:
                m_store.processor_state( task_state._terminated );
                break;
              case Appliance.COMPLETED:
                m_store.processor_state( task_state._completed );
                break;
            }
            if( before != m_store.processor_state() )
            {
	          super.post( newProcessorStateEvent( get_process_state() ));
            }
        }
    }

    // ==========================================================================
    // Processor
    // ==========================================================================
    
    /**
     * The processor description.
     */
    public String description()
    {
        return m_store.description();
    }

    /**
     * The processor description.
     */
    public void description( String value )
    {
        m_store.description( value );
    }

    /**
     * Returns a reference to the task coordinating this process.
     * @return  Task the coordinating task
     */
    public net.osm.session.task.Task get_task()
    {
        return net.osm.session.task.TaskHelper.narrow( m_store.executes().resource() );
    }

    /**
     * Starts a processor.
     * @exception  CannotStart if the processor cannot be started.
     * @exception  AlreadyRunning if the processor is already in a running state.
     */
    public void start()
    throws CannotStart, AlreadyRunning
    {
        getLogger().debug("start");
        switch( m_appliance.getApplianceState() )
        {
            case Appliance.IDLE:
              try
              {
                  m_appliance.start();
                  break;
              }
              catch( Throwable e )
              {
                  final String error = "Internal error while starting appliance.";
                  throw new CannotStart( error + " Cause: " + e.toString() );
              }
            case Appliance.RUNNING:
              throw new AlreadyRunning("Processor is already running.");
            case Appliance.SUSPENDED:
              m_appliance.resume();
              break;
            case Appliance.TERMINATED:
              throw new CannotStart("Processor has terminated.");
            case Appliance.COMPLETED:
              throw new CannotStart("Processor has completed.");
        }
    }

    /**
     * Suspends a processor.
     * @exception  CannotSuspend if the processor cannot be suspended.
     * @exception  CurrentlySuspended if the processor is already in a suspended state.
     */
    public void suspend()
        throws CannotSuspend, CurrentlySuspended
    {
        getLogger().debug("suspend");
        switch( m_appliance.getApplianceState() )
        {
            case Appliance.IDLE:
              throw new CannotSuspend("Processor has not been started.");
            case Appliance.RUNNING:
              m_appliance.suspend();
              break;
            case Appliance.SUSPENDED:
              throw new CurrentlySuspended("Processor is alreeady suspended.");
            case Appliance.TERMINATED:
              throw new CannotSuspend("Processor has terminated.");
            case Appliance.COMPLETED:
              throw new CannotSuspend("Processor has completed.");
        }
    }

    /**
     * Stops a Processor.
     * @exception  CannotStop if the processor cannot be stopped.
     * @exception  NotRunning if the processor is not in a running state.
     */
    public void stop()
        throws CannotStop, NotRunning
    {
        getLogger().debug("stop");
        switch( m_appliance.getApplianceState() )
        {
            case Appliance.IDLE:
              throw new NotRunning("Processor is not running.");
            case Appliance.RUNNING:
              try
              {
                  m_appliance.stop();
                  break;
              }
              catch( Throwable e )
              {
                  final String error = "Internal error while stopping appliance.";
                  throw new CannotStop( error + " Cause: " + e.toString() );
              }
            case Appliance.SUSPENDED:
              try
              {
                  m_appliance.stop();
                  break;
              }
              catch( Throwable e )
              {
                  final String error = "Internal error while stopping suspended appliance.";
                  throw new CannotStop( error + " Cause: " + e.toString() );
              }
            case Appliance.TERMINATED:
              break;
            case Appliance.COMPLETED:
              throw new CannotStop("Processor has completed.");
        }
    }

    /**
     * The processor state is determined by the state of its execution and the state of the 
     * data content being processed. The processor state and data state are related but 
     * independent. The data state contains information about the application or 
     * system object. The processor state contains information about the execution
     * context. 
     * @return  task_state current state of the processor
     */
    public task_state get_process_state()
    {
        return task_state.from_int( m_store.processor_state() );
    }

    // ==========================================================================
    // AbstractResource operation override
    // ==========================================================================
    
   /**
    *  The most derived type that this <code>AbstractResource</code> represents.
    */
    public TypeCode resourceKind()
    {
        return ProcessorHelper.type();
    }
    
   /**
    * Extension of the AbstractResource bind operation to support reception of the
    * notification of association dependencies from external resources on this
    * Processor.
    *
    * @param link Link notification of an association dependency
    */
    public synchronized void bind(Link link)
    throws ResourceUnavailable, ProcessorConflict, SemanticConflict
    {
        synchronized( m_store )
        {
            if( getLogger().isDebugEnabled() ) getLogger().debug("bind " 
		  + link.getClass().getName() );
            touch( m_store );

            if( link instanceof Executes )
		{    
                // Notification to this Processor that an Task has been
                // assigned as coordinator.
                
                m_store.executes( (Executes) link );
                if( getLogger().isDebugEnabled() ) getLogger().debug("bind "
                + "\n\tSOURCE: " + m_store.name()
                + "\n\tLINK: " + link.getClass().getName()
                + "\n\tTARGET: " + link.resource().name() );
                post( newBindEvent( link ));
            }
            else
            {
                super.bind( link );
            }
        }
    }
    
   /**
    * Replaces an existing Processor dependecy with another.
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
            if( getLogger().isDebugEnabled() ) getLogger().debug("replace");
            touch( m_store );

            if (( old instanceof Executes) 
              && ( _new instanceof Executes ) )
            {

                // Notification to this Processor that an Task has been
                // assigned as coordinator, replaciong the current coordinator.
                
                if( m_store.executes().resource() != null )
                {
                    if( m_store.executes().resource()._is_equivalent( old.resource() ))
                    {

                        m_store.executes( (Executes) _new );
                        if( getLogger().isDebugEnabled() ) getLogger().debug("replace "
                			+ "\n\tSOURCE: " + m_store.name()
                			+ "\n\tLINK: " + old.getClass().getName()
                			+ "\n\tOLD: " + old.resource().name()
                			+ "\n\tNEW: " + _new.resource().name() );
                        post( newReplaceEvent( old, _new ));
                        modify( m_store );
                        return;
                    }
                    else
                    {
                        if( getLogger().isDebugEnabled() ) getLogger().debug(
				  "supplied 'old' value does not match current 'Executes' link");
                        throw new SemanticConflict();
                    }
                }
                else if( old.resource() == null )
                {
                    m_store.executes( (Executes) _new );
                	  if( getLogger().isDebugEnabled() ) getLogger().debug("replace "
                			+ "\n\tSOURCE: " + m_store.name()
                			+ "\n\tLINK: " + old.getClass().getName()
                			+ "\n\tNEW: " + _new.resource().name() );
                    post( newReplaceEvent( old, _new ));
                    modify(m_store);
                    return;
                }
                else
                {
                    if( getLogger().isDebugEnabled() ) getLogger().debug(
			    "old argument cannot replace a non assigned link");
                    throw new SemanticConflict();
                }
            }
            else
            {
                super.replace( old, _new );
            }
        }
    }
    
   /**
    * Releases an existing dependecy.
    * @param link Link to retract
    */
    public synchronized void release(Link link)
    {
        synchronized( m_store )
        {
            getLogger().debug("release");
            touch( m_store );
            
            if( link instanceof Executes )
            {
                
                // an Task is notifying this processor of the retraction of
                // an ExecutedBy association

                if( m_store.executes().resource() != null )
                {
                    try
                    {
                        if( m_store.executes().resource()._is_equivalent( link.resource() ))
                        {
				    // retraction of a Executes association

                            m_store.executes( new DefaultExecutes() );
                		    if( getLogger().isDebugEnabled() ) getLogger().debug("release "
                			+ "\n\tSOURCE: " + m_store.name()
                			+ "\n\tLINK: " + link.getClass().getName()
                			+ "\n\tTARGET: " + link.resource().name() );
                            post( newReleaseEvent( link ));
                        }
                    }
                    catch( Exception e)
                    {
                        String s = "failed to release Executes association on Processor";
                        throw new ProcessorRuntimeException( s, e );
                    }
                }
            }
            else
            {
                super.release( link );
            }
        }
    }

   /**
    * Returns an integer corresponding to the number of links of the 
    * link type declared by the type argument.
    */
    public short count(org.omg.CORBA.TypeCode type)
    {
        getLogger().debug("count");
        int count = 0;

        if( type.equal( ExecutesHelper.type() ) ) 
	  {
            if( m_store.executes().resource() != null ) count = 1;
            touch( m_store );
	  }
	  else
	  {
	      count = super.count( type );
	  }
        Integer v = new Integer( count );
        return v.shortValue();
    }
  
   /**
    * Returns a set of resources linked to this processor by a specific relationship.
    * @return  LinkIterator an iterator of Link instances
    * @param  max_number maximum number of Link instance to include in the
    * seq value.
    * @param  seq Links a sequence of Links matching the type filter
    *
    * @param link Link to retract
    */
    public LinkIterator expand(org.omg.CORBA.TypeCode type, int max_number, LinksHolder links)
    {
        getLogger().debug("expand");
	  if( type == null ) throw new org.omg.CORBA.BAD_PARAM(
	    "Illegal null type argument supplied to expand.");
	  if( links == null ) throw new org.omg.CORBA.BAD_PARAM(
	    "Illegal null links argument supplied to expand.");
             
        if( type.equivalent( ExecutesHelper.type() ))
        {
		links.value = new Link[]{ m_store.executes() };
		touch( m_store );
		return null;
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
    * 
    */
    
    public synchronized void remove()
    throws NotRemovable
    {
        synchronized( m_store )
        {
            if( getLogger().isDebugEnabled() ) getLogger().debug("remove");
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
		    if( getLogger().isWarnEnabled() ) getLogger().warn( 
                 "Ignoring disposal exception during processor removal.", e );
		}
        }
    }
    
    // ==========================================================================
    // utilities
    // ==========================================================================
    
    protected StructuredEvent newProcessorStateEvent( task_state state )
    {
        Property sourceProp = super.createSourceProperty( );
        Any stateHolder = ORB.init().create_any();
        task_stateHelper.insert( stateHolder, state );
        Property stateProp = new Property( "task_state", stateHolder );
        return StructuredEventUtilities.createEvent( processorStateEventType, 
		new Property[]
		{ 
			StructuredEventUtilities.timestamp(), 
			sourceProp, 
			stateProp 
		}
	  );
    }

   /**
    * Set the object reference to be returned for this delegate.
    * @param processor the object reference for the processor
    */
    protected void setProcessorReference( Processor processor )
    {
        m_processor = processor;
        super.setReference( processor );
    }

   /**
    * Returns the object reference for this delegate.
    * @return Processor the object referenced for the delegate
    */
    protected Processor getProcessorReference( )
    {
        return m_processor;
    }

   /**
    * Returns a <code>ProcessorAdapter</code>.
    * @return Adapter an instance of <code>ProcessorAdapter</code>.
    */
    public Adapter get_adapter()
    {
        return new ProcessorValue( m_processor );
    }

}
