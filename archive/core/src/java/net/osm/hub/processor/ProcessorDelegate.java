

package net.osm.hub.processor;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.JarURLConnection;
import java.util.jar.Attributes;

import org.apache.avalon.framework.logger.Logger;
import org.apache.avalon.framework.logger.LogEnabled;
import org.apache.avalon.framework.activity.Initializable;
import org.apache.avalon.framework.context.Context;
import org.apache.avalon.framework.context.ContextException;
import org.apache.avalon.framework.context.Contextualizable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.component.Composable;
import org.apache.avalon.framework.component.ComponentManager;
import org.apache.avalon.framework.component.ComponentException;

import org.omg.CORBA.ORB;
import org.omg.CORBA.Any;
import org.omg.CORBA.TypeCode;
import org.omg.CORBA.portable.ValueFactory;
import org.omg.CORBA.NO_IMPLEMENT;
import org.omg.CORBA.BooleanHolder;
import org.omg.CosCollection.AnySequenceHolder;
import org.omg.CosLifeCycle.NotRemovable;
import org.omg.CosTime.TimeService;
import org.omg.CollaborationFramework.Controls;
import org.omg.CollaborationFramework.ControlsHelper;
import org.omg.CollaborationFramework.ControlledBy;
import org.omg.CollaborationFramework.ControlledByHelper;
import org.omg.CollaborationFramework.Coordinates;
import org.omg.CollaborationFramework.CoordinatedBy;
import org.omg.CollaborationFramework.CoordinatedByHelper;
import org.omg.CollaborationFramework.ProcessorOperations;
import org.omg.CollaborationFramework.Processor;
import org.omg.CollaborationFramework.ProcessorModel;
import org.omg.CollaborationFramework.ProcessorHelper;
import org.omg.CollaborationFramework.StateDescriptor;
import org.omg.CollaborationFramework.Master;
import org.omg.CollaborationFramework.MasterHelper;
import org.omg.CollaborationFramework.UsageDescriptor;
import org.omg.CollaborationFramework.InputDescriptor;
import org.omg.CollaborationFramework.OutputDescriptor;
import org.omg.CollaborationFramework.Slave;
import org.omg.CollaborationFramework.SlaveHelper;
import org.omg.CollaborationFramework.SlaveIterator;
import org.omg.CollaborationFramework.SlaveIteratorPOA;
import org.omg.CollaborationFramework.SlaveIteratorPOATie;
import org.omg.CollaborationFramework.SlavesHolder;
import org.omg.CommunityFramework.Model;
import org.omg.CommunityFramework.Problem;
import org.omg.CommunityFramework.GenericResource;
import org.omg.CommunityFramework.GenericResourceHelper;
import org.omg.PortableServer.POA;
import org.omg.Session.AbstractResource;
import org.omg.Session.BaseBusinessObjectKey;
import org.omg.Session.Link;
import org.omg.Session.LinkIterator;
import org.omg.Session.LinkIteratorPOA;
import org.omg.Session.LinkIteratorPOATie;
import org.omg.Session.LinksHolder;
import org.omg.Session.LinkHolder;
import org.omg.Session.LinkIteratorHolder;
import org.omg.Session.ResourceUnavailable;
import org.omg.Session.Task;
import org.omg.Session.TaskHelper;
import org.omg.Session.ProcessorConflict;
import org.omg.Session.SemanticConflict;
import org.omg.Session.Consumes;
import org.omg.Session.ConsumesHelper;
import org.omg.Session.CannotStart; 
import org.omg.Session.AlreadyRunning;
import org.omg.Session.CurrentlySuspended;
import org.omg.Session.CannotSuspend;
import org.omg.Session.NotRunning;
import org.omg.Session.CannotStop;
import org.omg.Session.task_state;
import org.omg.Session.SystemMessage;
import org.omg.Session.User;

import net.osm.hub.gateway.FactoryException;
import net.osm.hub.gateway.Manager;
import net.osm.hub.resource.AbstractResourceDelegate;
import net.osm.hub.resource.LinkIteratorDelegate;
import net.osm.hub.pss.LinkStorage;
import net.osm.hub.pss.ProcessorStorage;
import net.osm.hub.gateway.CannotTerminate;
import net.osm.list.LinkedList;
import net.osm.list.Iterator;
import net.osm.list.NoEntry;
import net.osm.realm.StandardPrincipal;

/**
 * <p>The <code>ProcessorDelegate</code> class manages the bridge between the external view of
 * a enterprise business process and the internal implementation of that service.   To the 
 * external client the Processor interface presents a view of formal connections of a processor
 * to a user's task, the task associated consumption and production relationships, the owner
 * of the Task and so forth.  Internally, a Processor implementation maintains persistent 
 * state execution state and acts as the coordinator of an internal enterprise service in its
 * delivery and execution of the published process lifecycle and process policy.</>
 * 
 * <p>Processors are described in terms of a lifecycle and public execution policy expressed 
 * in DPML or equivilent <code>ProcessorCriteria</code> construct. A ProcessorCritera contains 
 * declarations of the behaviour that a processor gaurantees towards the external client.  It 
 * declares the required input associations and output of the process.  More specialized 
 * variants of the ProcessorDelegate implememntation support dynamic lifecycle declaration 
 * (customization of the process state transition model and role based contraints enabling 
 * the defintion of arbitary collaborative processes).</p>
 *
 * <p>Processes are constructed based on a supplied criteria.  Criteria instances available 
 * within a particular site are published under a ProcessorFactory, located through the 
 * Finder interface.</p>
 */

public class ProcessorDelegate 
extends AbstractProcessorDelegate 
implements Contextualizable, Composable, Initializable, ProcessorCallback
{
        
   /**
    * Number of miliseconds for the appliance to startup, suspension 
    * and termination.
    */
    private int startup;
    private int suspension;
    private int termination;

   /**
    * Internal reference to the Thread that appliance will run within.
    */
    private Thread thread;
   
   /**
    * The appliance.
    */
    private Appliance appliance;

    private ProcessorContext context;

    private ComponentManager manager;

    //=======================================================================
    // Composable
    //=======================================================================
    
    /**
     * Pass the <code>ComponentManager</code> to the <code>Composable</code>.
     * The implementation aquires a reference to the registration authority (RA)
     * that will be used to handle the certification request.
     * @param manager the <code>ComponentManager</code> holding a reference to
     *  a PKI registration authority
     */
    public void compose( ComponentManager manager )
    throws ComponentException
    {
	  super.compose( manager );
	  this.manager = manager;
    }

    //=================================================================
    // Contextualizable
    //=================================================================

   /**
    * Set the block context.
    * @param context the block context
    * @exception ContextException if the block cannot be narrowed to BlockContext
    */
    public void contextualize( Context context ) throws ContextException
    {
        super.contextualize( context );
	  if( !(context instanceof ProcessorContext ) )
	  {
		final String error = "supplied context does not implement ProcessorContext.";
		throw new ContextException( error );
        }
	  this.context = (ProcessorContext) context;
    }
     
    //=======================================================================
    // Initializable
    //=======================================================================
    
    public void initialize()
    throws Exception
    {       
	  super.initialize();

	  Configuration policy = context.getAppliancePolicy();
	  this.startup = policy.getAttributeAsInteger("startup",10000);
        this.suspension = policy.getAttributeAsInteger("suspension",10000);
	  this.termination = policy.getAttributeAsInteger("termination",10000);

	  // get the appliance

	  try
	  {
	      appliance = (Appliance) context.getApplianceClass().newInstance();
		if( appliance instanceof LogEnabled ) 
		  ((LogEnabled)appliance).enableLogging( getLogger().getChildLogger( "appliance" ) );
		if( appliance instanceof Contextualizable ) 
	        ((Contextualizable)appliance).contextualize( new ApplianceContext( context, this ) );
	      if( appliance instanceof Composable )
		  ((Composable)appliance).compose( manager );
	      if( appliance instanceof Configurable )
		  ((Configurable)appliance).configure( context.getApplianceConfiguration() );
	      if( appliance instanceof Initializable )
		  ((Initializable)appliance).initialize();
        }
	  catch(Exception e)
	  {
		String error =  "failed to instantiate the appliance" ;
		throw new Exception( error, e );
	  }
    }

    // ==========================================================================
    // Processor
    // ==========================================================================

   /**
    * The Processor start operation is invoked by a client to initate the execution of 
    * a process thread.  A Processor may be started if it is in either the 'notstarted' 
    * or 'suspended' state.  In either case, validation of the processor will be 
    * undertaken.  If the validation fails a CannotStart exception will be thrown.
    * A client can access supplementary information concerning a CannotStart condition 
    * by invoking the <code>verify</code> operation against the Processor interface.
    *
    * @exception  org.omg.Session.CannotStart
    * @exception  org.omg.Session.AlreadyRunning
    */
    public void start()
    throws CannotStart, AlreadyRunning
    {
	  task_state current = null;
	  synchronized( store )
	  {
		current = store.processor_state().processor_state;
	      if(( current == task_state.completed ) || 
		  (current == task_state.terminated)) throw new CannotStart();
	      if( current == task_state.running ) throw new AlreadyRunning();
            if( verify().length > 0 ) throw new CannotStart();
		if( getLogger().isDebugEnabled() ) getLogger().debug( "start" );
            store.processor_state().processor_state = task_state.running;
	      post( newStateEvent( new StateDescriptor( task_state.running )));
		thread = new Thread( appliance );
		thread.start();
	  }
    }

   /**
    * Moves a processor into a suspended state.  The suspend operation
    * is responsible for handling registration of any intermidiate state 
    * established by process execution followed by termination of the 
    * process execution thread.  Process execution may be resumed by 
    * re-applying the start operation. This implementation handles 
    * thread termination and related event generation.
    *
    * @exception  org.omg.Session.CannotSuspend
    * @exception  org.omg.Session.CurrentlySuspended
    */
    public void suspend()
    throws CannotSuspend, CurrentlySuspended
    {
	  task_state current = null;
	  synchronized( store )
	  {
            current = store.processor_state().processor_state;
	      if(( current == task_state.completed ) || 
		  (current == task_state.terminated)) throw new CannotSuspend();
	      if( current == task_state.suspended ) throw new CurrentlySuspended();
		if( getLogger().isDebugEnabled() ) getLogger().debug( "suspend" );

		try
		{
		    if( Thread.currentThread() != thread )
		    {
		        appliance.requestSuspension();
		        thread.join( 10000 );
		    }
		}
		catch( InterruptedException ie )
		{
		}
		finally
		{
                if( getLogger().isDebugEnabled() ) getLogger().debug("suspending");
                store.processor_state().processor_state = task_state.suspended;
	          post( newStateEvent( new StateDescriptor( task_state.suspended )));
		}
	  }
    }

   /**
    * Operation invoked by an appliance to request suspension of the process
    * for reasons detailed under the supplied message.  The implementation 
    * directs the message to the owner of the coordinating task.
    * @param message the message to be directed to the owner of the coordinating task
    */
    public void suspend( SystemMessage message )
    throws CannotSuspend, CurrentlySuspended
    {
        suspend();
	  try
	  {
            Task task = coordinator();
	      User user = task.owned_by();
	      user.enqueue( message );
	  }
	  catch( Throwable e )
	  {
	      final String warning = "unable to queue a message against the processor owner";
	      if( getLogger().isWarnEnabled() ) getLogger().warn( warning, e );
	  }
    }

   /**
    * Stops a processor. Semantically equivalent to the Task stop operation 
    * (refer Task and Session, Task specification).
    * @exception  org.omg.Session.CannotStop
    * @exception  org.omg.Session.NotRunning
    */
    public void stop()
    throws CannotStop, NotRunning
    {
        stop( null );
    }
    
   /**
    * Stops a processor as a result of an error condition.  This method is 
    * invoked by an appliance to signal termination of the process as a result
    * of an internal exception.
    * @param throwable the exception causing the appliance to terminate or null
    *   if the stop operation is invoked by a remote client
    * @exception  org.omg.Session.CannotStop
    * @exception  org.omg.Session.NotRunning
    */
    public void stop( Throwable throwable )
    throws CannotStop, NotRunning
    {
	  task_state current = null;
	  synchronized( store )
	  {
            current = store.processor_state().processor_state;
	      if(( current == task_state.completed ) || 
		  (current == task_state.terminated)) throw new CannotStop();

		try
		{
		    if( Thread.currentThread() != thread )
		    {
		        appliance.requestTermination();
		        thread.join( 10000 );
		    }
		}
		catch( InterruptedException ie )
		{
		}
		finally
		{
                store.processor_state().processor_state = task_state.terminated;
		    if( throwable == null )
		    {
                    if( getLogger().isDebugEnabled() ) getLogger().debug("terminating" );
	              post( newStateEvent( new StateDescriptor( task_state.terminated )));
		    }
		    else
	          {
                    if( getLogger().isDebugEnabled() ) getLogger().debug(
		          "terminating", throwable );

	              post( 
			    newStateEvent( 
			      new StateDescriptor( 
			        task_state.terminated, new Problem( throwable )
			      )
			    )
                    );
	          }
		}
	  }
    }

   /**
    * Method invoked by an appliance to signal normal completion of 
    * of the execution thread.
    */
    public void completion()
    {
        completion( null );
    }

   /**
    * Method invoked by an appliance to signal normal completion of 
    * of the execution thread with a supplied completion message.
    */
    public void completion( SystemMessage message )
    {
	  try
	  {
            if( getLogger().isDebugEnabled() ) getLogger().debug("completing");
            store.processor_state().processor_state = task_state.completed;
	      post( newStateEvent( new StateDescriptor( task_state.completed )));
	  }
	  catch( Throwable e )
	  {
		if( getLogger().isWarnEnabled() ) getLogger().warn(
	            "failed to notify task that the processor has completed", e );
	  }

        if( message != null ) try
	  {
            Task task = coordinator();
	      User user = task.owned_by();
	      user.enqueue( message );
	  }
	  catch( Throwable e )
	  {
	      final String warning = "unable to queue a message against the processor owner";
	      if( getLogger().isWarnEnabled() ) getLogger().warn( warning, e );
	  }
    }

    // ==========================================================================
    // BaseBusinessObject
    // ==========================================================================
    
   /**
    * ProcessorDelegate suppliments the BaseBusienssObject remove implementation
    * through retraction of the coordination links between it and its coordinating 
    * Task, and the removal of any subsidary processors.
    */
    public synchronized void remove()
    throws NotRemovable
    {
	  super.remove();
    }

    //=======================================================================
    // Disposable
    //=======================================================================

   /**
    * Clean up state members.
    */ 
    public synchronized void dispose()
    {
 	  if( getLogger().isDebugEnabled() ) getLogger().debug("dispose");
	  //if( thread != null ) thread.destroy();
        this.thread = null;
        this.appliance = null;
	  super.dispose();
    }
}
