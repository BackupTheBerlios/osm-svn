

package net.osm.hub.processor;

import java.io.Serializable;

import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.avalon.framework.logger.Logger;
import org.apache.avalon.framework.activity.Disposable;
import org.apache.avalon.framework.context.Context;
import org.apache.avalon.framework.context.Contextualizable;
import org.apache.avalon.framework.context.ContextException;
import org.apache.avalon.framework.component.ComponentManager;
import org.apache.avalon.framework.component.ComponentException;
import org.apache.avalon.framework.activity.Initializable;

import org.omg.Session.SystemMessage;
import org.omg.Session.SystemMessageBase;
import org.omg.Session.MessageClassification;
import org.omg.Session.MessageHeader;
import org.omg.Session.MessageHeaderBase;
import org.omg.Session.MessagePriority;
import org.omg.Session.MessageBody;
import org.omg.Session.MessageBodyBase;

/**
 * AbstractAppliance is an abstract runnable object that handles the 
 * processors main background execution thread.
 */

public abstract class AbstractAppliance extends AbstractLogEnabled 
implements Appliance, Contextualizable, Disposable
{

    //=================================================================
    // static
    //=================================================================

    public static final int PENDING = 0;
    public static final int RUNNING = 1;
    public static final int SUSPENDED = 2;
    public static final int TERMINATED = 3;
    public static final int COMPLETED = 4;

    //=================================================================
    // state
    //=================================================================

   /**
    * Internal thread state.
    */
    private int state = PENDING;

   /**
    * The appliance context.
    */
    private Context context;

    private ProcessorCallback callback;

    //=======================================================================
    // Contextualizable
    //=======================================================================

   /**
    * Establish the appliance context.
    * @param context the servant context
    */
    public void contextualize( Context context ) throws ContextException
    {
	  this.context = context;
        callback = getProcessorCallback();
    }

    //=================================================================
    // Runnable
    //=================================================================

   /**
    * The run method must be overriden by an application.  Implementation
    * should check frequently for modification to the internal state arrising 
    * from processor requerst for suspension and termination.  On completion
    * the run implementation should invoke <code>signalCompletion</code> and 
    * return. If the appliance whishes to suspend the processor it should invoke
    * <code>signalSuspension</code>.  In the case of an internal failure or 
    * other condition where the appliance needs to terminate the processor, 
    * the run implementation should invoke one of the <code>signalTermination</code>
    * methods.  
    * <p>
    * The run method will be invoked by the hosting processor following a request
    * to start the processor (possibly following initation or after a prior 
    * suspension of processor activitity).  An appliance implementation is 
    * responsible any interim state it may need to optimise multiple run 
    * invocations within the course of a single process lifecycle.
    */
    public abstract void run();

    //=================================================================
    // Appliance
    //=================================================================

   /**
    * Method invoked by a processor to request appliance suspension.
    * The run implementation should frequently check the current state using the 
    * <code>getState</code> method for posible SUSPENDED or TERMINATED values
    * in which case the run implementation should return.
    */
    public void requestSuspension()
    {
        setState( SUSPENDED );
    }

   /**
    * Method invoked by a processor to request appliance termination.
    * The run implementation should frequently check the current state using the 
    * <code>getState</code> method for posible SUSPENDED or TERMINATED values
    * in which case the run implementation should return.
    */
    public void requestTermination()
    {
        setState( TERMINATED );
    }

    //=================================================================
    // AbstractAppliance
    //=================================================================

   /**
    * Return the preferred state of the appliance.  The value returned
    * from <code>getState</code> may be modified by the hosting processor
    * using either <code>requestSuspension</code> or <code>requestTermination</code>.
    * Implementation of the run method should check this value frequently and 
    * return if the state returned is TERMINATED or SUSPENDED.
    * @return int appliance state
    */
    public synchronized int getState()
    {
        return this.state;
    }

   /**
    * Sets the preferred appliance state.
    * @param value the appliance state
    */
    protected synchronized void setState( int value )
    {
	  if((value < PENDING) || (value > COMPLETED)) 
	    throw new IllegalArgumentException("invalid state value");
	  this.state = value;
    }

   /**
    * Convinience operation invoked by an appliance implementation 
    * that sets the current state to COMPLETED and notifies the host 
    * processor of completion.
    */
    protected void signalCompletion()
    {
        signalCompletion( null );
    }

   /**
    * Convinience operation invoked by an appliance implementation 
    * that sets the current state to COMPLETED, and notifies the host 
    * processor of completion with a supplied completion message.
    */
    protected void signalCompletion( SystemMessage message )
    {
        setState( COMPLETED );
	  try
	  {
            getProcessorCallback().completion( message );
	  }
	  catch( Exception e )
	  {
		// log a warning
	  }
    }

   /**
    * Convinience operation that sets the current state to SUSPENDED
    * and notifies the host processor with the supplied system message.
    * @param message a message to be directed to the processor's coordinating
    * task's owner
    */
    protected void signalSuspension( SystemMessage message )
    {
        setState( SUSPENDED );
	  try
	  {
            getProcessorCallback().suspend( message );
	  }
	  catch( Exception e )
	  {
		// log a warning
	  }
    }

   /**
    * Convinience operation that sets the current state to TERMINATED
    * and notifies the host processor.
    */
    protected void signalTermination( )
    {
        signalTermination( null );
    }

   /**
    * Convinience operation that sets the current state to TERMINATED
    * and notifies the host processor with the supplied exception.
    * @param exception the exception causing the execution thread to terminate
    */
    protected void signalTermination( Throwable exception )
    {
        setState( TERMINATED );
	  try
	  {
            getProcessorCallback().stop( exception );
	  }
	  catch( Exception e )
	  {
		// log a warning
	  }
    }

   /**
    * Convinience operation to return the host processor.
    */
    protected ProcessorCallback getProcessorCallback() throws ContextException
    {
        if( callback != null ) return callback;
        callback = (ProcessorCallback) context.get("CALLBACK");
        return callback;
    }

    //=================================================================
    // utilities 
    //=================================================================

    public SystemMessage createMessage( MessageClassification type, String subject, String content )
    {
	  try
	  {
            MessageHeader header = new MessageHeaderBase(
	        subject, MessagePriority.NORMAL, type, getProcessorCallback().getProcessorReference());
            MessageBody body = new MessageBodyBase( "text/html", "<p>" + content + "</p>" );
            return new SystemMessageBase( header, body );
	  }
	  catch( Throwable e )
	  {
		final String error = "unable to create message due to internal error";
		throw new RuntimeException( error, e );
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
	  if( getLogger().isDebugEnabled() ) getLogger().debug("dispose");
	  context = null;
	  callback = null;
    }

}
