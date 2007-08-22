

package net.osm.pki.process;

import java.io.Serializable;

import org.apache.avalon.framework.context.Context;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.component.ComponentManager;
import org.apache.avalon.framework.component.ComponentException;
import org.apache.avalon.framework.component.Composable;

import org.omg.CORBA.ORB;
import org.omg.CORBA.Any;
import org.omg.CORBA.TypeCode;
import org.omg.CORBA.portable.ValueFactory;
import org.omg.PortableServer.POA;
import org.omg.CosLifeCycle.NotRemovable;
import org.omg.CollaborationFramework.Processor;
import org.omg.CollaborationFramework.ProcessorHelper;
import org.omg.CollaborationFramework.ProcessorOperations;
import org.omg.CollaborationFramework.StateDescriptor;
import org.omg.CommunityFramework.Problem;
import org.omg.Session.CannotStart; 
import org.omg.Session.AlreadyRunning;
import org.omg.Session.CurrentlySuspended;
import org.omg.Session.CannotStop;
import org.omg.Session.NotRunning;
import org.omg.Session.CannotSuspend;
import org.omg.Session.task_state;
import org.omg.Session.Task;
import org.omg.Session.User;
import org.omg.Session.SystemMessage;

import net.osm.hub.pss.ProcessorStorage;
import net.osm.hub.gateway.CannotTerminate;
import net.osm.hub.processor.AbstractProcessorDelegate;
import net.osm.hub.processor.ApplianceContext;
import net.osm.hub.processor.ProcessorCallback;
import net.osm.hub.resource.AbstractResourceDelegate;
import net.osm.hub.generic.GenericResourceService;
import net.osm.pki.authority.RegistrationAuthorityService;


/**
 * CertificationDelegate handles PKI requests for digital identity certification.
 */

public class CertificationDelegate extends AbstractProcessorDelegate 
implements ProcessorCallback, Composable
{

    //=================================================================
    // state
    //=================================================================

   /**
    * The runnable appliance.  
    */
    private CertificationAppliance appliance;
     
   /**
    * The thread to run the certification request within.  
    */
    private Thread thread;

   /**
    * The RA against which certification requests will be lodged.
    */
    private RegistrationAuthorityService authority;

   /**
    * The GenericResurce service against which request for creation
    * of produced resources will be directed.
    */
    GenericResourceService generic;

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
        if( getLogger().isDebugEnabled() ) getLogger().debug( "compose (CertificationDelegate)" );
	  this.manager = manager;
        authority = ((RegistrationAuthorityService) manager.lookup("AUTHORITY"));
        generic = ((GenericResourceService)manager.lookup("GENERIC"));
    }

    //=======================================================================
    // Initializable
    //=======================================================================
    
   /**
    * Signal completion of contextualization phase and readiness to serve requests.
    * @exception Exception indicating an internal error during the initialization process
    */
    public void initialize()
    throws Exception
    {
	  super.initialize();
        if( getLogger().isDebugEnabled() ) getLogger().debug( "initialize (CertificationDelegate)" );
	  appliance = new CertificationAppliance( );
	  appliance.enableLogging( getLogger().getChildLogger("appliance") );
	  appliance.contextualize( new ApplianceContext( this ) );
	  appliance.compose( manager );
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


    //=================================================================
    // Disposable 
    //=================================================================

   /**
    * Clean up state members.
    */ 
    public synchronized void dispose()
    {
	  if( getLogger().isDebugEnabled() ) getLogger().debug("dispose (CertificationDelegate)");
	  thread = null;
	  appliance = null;
	  authority = null;
	  generic = null;
	  super.dispose();
    }
}
