
package net.osm.sps;

import java.util.List;
import java.util.LinkedList;
import java.util.Iterator;

import org.apache.avalon.framework.logger.Logger;
import org.apache.avalon.framework.logger.LogEnabled;
import org.apache.avalon.framework.logger.AbstractLogEnabled;
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
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.Serviceable;
import org.apache.avalon.framework.CascadingRuntimeException;
import org.apache.avalon.framework.CascadingException;
import org.apache.avalon.phoenix.BlockContext;
import org.apache.avalon.phoenix.Block;
import org.apache.pss.StorageContext;
import org.apache.pss.SessionContext;
import org.apache.pss.Session;
import org.apache.pss.util.Incrementor;

import org.omg.CosPersistentState.NotFound;
import org.omg.CosPersistentState.StorageObject;
import org.omg.CosNotification.EventType;
import org.omg.CosNotification.StructuredEvent;
import org.omg.CosNotifyComm.StructuredPushSupplier;
import org.omg.CosNotifyComm.StructuredPushSupplierPOA;
import org.omg.CosNotifyComm.StructuredPushSupplierPOATie;
import org.omg.CosNotifyComm.InvalidEventType;
import org.omg.CosEventComm.Disconnected;

import net.osm.event.EventStorage;
import net.osm.event.EventStorageHome;


/**
 * Supplier is an object associated to exactly one StructuredPushConsumer that
 * is responsible for handling the issuance of StructuredEvents to the consumer,
 * and responding to disconnect request.
 */

public class StructuredPushSupplierDelegate extends AbstractLogEnabled
implements Block, Contextualizable, Serviceable, Initializable, Startable, SubscriberProxyOperations, Runnable
{

    //=======================================================================
    // static
    //=======================================================================
    
    private static final Incrementor eventIncrementor = Incrementor.create("EVENT-IDENTIFIER");

    private static final int NORMAL = 0;
    private static final int SHUTDOWN = 1;
    private static final int DISPOSAL = 2;

    //=======================================================================
    // state
    //=======================================================================

    private int mode = NORMAL;

   /**
    * The servant context from which the storage object will be resolved.
    */
    protected StorageContext m_context;

   /**
    * The storage object for this subscription.
    */
    protected Subscription m_store;

   /**
    * The component manager that we notify completion to.
    */
    private ServiceManager m_manager;

    private Thread thread;

    private boolean disposed = false;

    private int trip = 0;

    private int commFailureCount = 0;
    private final int commFailureCeiling = 6;

   /**
    * A queue of event storage objects established during initialization of the 
    * class and writen out on normal closure of the class to the subscription 
    * storage object.
    */
    private final LinkedList m_queue = new LinkedList();

    private Session m_session;


    //=======================================================================
    // Contextualizable
    //=======================================================================
   
   /**
    * Establish the delegate context during which the <code>Subscription</code>
    * storage object is resolved.
    * @param context the servant context
    * @exception ContextException if an error occurs during contextualization
    */
    public void contextualize( Context context ) 
    throws ContextException
    {
        if( getLogger().isDebugEnabled() ) getLogger().debug("contextualization");
        if( !( context instanceof StorageContext )) throw new ContextException( 
          "Supplied context must be an instance of org.apache.pss.StorageContext" );
        try
        {
	      m_store = (Subscription) ((StorageContext)context).getStorageObject();
        }
        catch( Throwable e )
        {
            final String error = "Unexpected error while resolving subscription.";
            throw new ContextException( error, e );
        }
    }

    //=======================================================================
    // Serviceable
    //=======================================================================

    /**
     * Pass the <code>ServiceManager</code> to the <code>Composable</code>.
     * The <code>Serviceable</code> implementation uses the supplied
     * <code>ServiceManager</code> to acquire the components it needs for
     * execution.
     * @param manager the <code>ServiceManager</code> for this delegate
     * @exception ServiceException
     */
    public void service( ServiceManager manager )
    throws ServiceException
    {
        if( getLogger().isDebugEnabled() ) getLogger().debug( "composition" );
        m_manager = manager;
        m_session = (Session) manager.lookup( SessionContext.SESSION_KEY );
    }

    //=======================================================================
    // Initializable
    //=======================================================================
    
   /**
    * Signal completion of contextualization phase and readiness to serve requests.
    * @exception Exception if the servant cannot complete initialization
    */
    public void initialize()
    throws Exception
    {       
        if( getLogger().isDebugEnabled() ) getLogger().debug("initialization");
        byte[][] ids = m_store.queue();
        for( int i=0; i<ids.length; i++ )
        {
            try
            {
                EventStorage event = (EventStorage) m_session.find_by_pid( ids[i] );
                StructuredEvent se = (StructuredEvent) event.structured_event();
                EventType t = se.header.fixed_header.event_type;
                if( m_store.is_subscribed( t ))
                {
                    m_queue.add( se );
                }
                event.destroy_object();
            }
            catch( Throwable e )
            {
                //don't throw an error but log the issue
                if( getLogger().isWarnEnabled() ) getLogger().warn(
                  "Unexpected execption occured while internalizing an event.", e );
            }
        }
    }

    //=======================================================================
    // Startable
    //=======================================================================

    public void start() throws Exception
    {
    }

    public void stop()
    {
	  mode = DISPOSAL;
        if( getLogger().isDebugEnabled() ) getLogger().debug("disconnection");
        launchDeathThread();
    }

    //=======================================================================
    // StructuredPushSupplier
    //=======================================================================
    
   /**
    * The subscription_change operation takes as input two sequences of event type names:
    * the first specifying those event types which the associated Consumer wants to add to
    * its subscription list, and the second specifying those event types which the associated
    * consumer wants to remove from its subscription list. This operation raises the
    * InvalidEventType exception if one of the event type names supplied in either input
    * parameter is syntactically invalid. If this case, the invalid name is returned in the type
    * field of the exception.
    */
    public void subscription_change(EventType[] added, EventType[] removed)
    throws InvalidEventType
    {
        m_store.subscription_change( added, removed );
    }

   /**
    * The disconnect_structured_push_supplier operation is invoked to terminate a
    * connection between the target StructuredPushSupplier, and its associated consumer.
    * This operation takes no input parameters and returns no values. The result of this
    * operation is that the StructuredPushSupplier will release all resources it had
    * allocated to support the connection, and dispose its own object reference.
    */
    public void disconnect_structured_push_supplier()
    {
        stop();
    }

    //======================================================================================
    // SubscriberProxy
    //======================================================================================

   /**
    * Adds a structured event to the event queue.
    * @param event the structured event to add to the queue.
    */
    public synchronized void post( StructuredEvent event )
    {
	  if( mode > NORMAL ) throw new IllegalStateException(
          "Supplier is in an invalid state to handle event post requests.");

	  if( event == null ) throw new NullPointerException(
	    "Null structured event argument supplied to post.");

        if( getLogger().isDebugEnabled() ) getLogger().debug(
          "adding structured event to queue");

        synchronized( m_queue )
	  {
	      try
            {
		    m_queue.add( event );
            }
            catch( Throwable e )
            {
                //
                // don't throw any exceptions back to the client - this is 
                // a post and forget operation - instead write exceptions 
                // and a stack trace to the log file
                //

                if( getLogger().isWarnEnabled() ) getLogger().warn(
			  "Internal error while queuing an event.", e );
		}
	  }
    }

   /**
    * Initate the termination sequence for this proxy.  The implementation
    * shall complete delivery of all pending events followed by servant
    * disposal.
    */
    public void terminate( boolean policy )
    {
	  mode = SHUTDOWN;
	  m_store.terminal( true );
        if( getLogger().isDebugEnabled() ) getLogger().debug( "termination" );
        launchDeathThread( );
    }

    //======================================================================================
    // utilities
    //======================================================================================

   /**
    * Initate the event dispath thread.
    */
    private void initiateEventDispach()
    {
	  if(( thread == null ) || ((thread != null) && !thread.isAlive() ))
	  {
            if( getLogger().isDebugEnabled() ) getLogger().debug("starting dispatch thread");
            thread = new Thread( this );
            thread.start();
	  }
    }

    public void run()
    {
	  while( processQueue() && ( mode < DISPOSAL ))
	  {
		try
		{
                if( getLogger().isDebugEnabled() ) getLogger().debug("dispatch thread suspended");
		    Thread.currentThread().sleep( 1000 );
		}
		catch( Throwable interrupt )
		{
		}
	  }
        if( getLogger().isDebugEnabled() ) getLogger().debug("dispatch thread completed");
    }

   /**
    * Attempt to deliver all events in the queue.
    * @return boolean true if the processing of the queue resulted in 
    *   a state where more events need to be posted (i.e. queue processing
    *   needs to be re-executed)
    */
    private synchronized boolean processQueue()
    {
	  if( mode == DISPOSAL ) return false;
	  if( getLogger().isDebugEnabled() ) getLogger().debug(
           "processing pending event queue, "
	     + m_queue.size() + ")" );
        trip = 0;
        List list = null;
	  synchronized( m_queue )
	  {
            list = (List) m_queue.clone();
        }

        Iterator iterator = list.iterator();
        while( iterator.hasNext() && mode < DISPOSAL )
        {
            try
            {
                StructuredEvent event = (StructuredEvent) iterator.next();
		    postEvent( event );
                m_queue.remove( event );
		}
            catch( org.omg.CORBA.COMM_FAILURE comms )
		{
                if( commFailureCount > commFailureCeiling )
		    {
                    mode = DISPOSAL;
                    final String failure = "Communications failure exceeded ceiling.";
                    if( getLogger().isWarnEnabled() ) getLogger().warn( failure, comms );
			  launchDeathThread();
			  return false;
                }
                else
                {
                    commFailureCount++;
                    return true;
                }
            }
            catch( Throwable e )
            {
                trip = 10;
                mode = DISPOSAL;
	          final String error = 
                   "Subscriber event queue processing failure - terminating queue.";
                if( getLogger().isWarnEnabled() ) getLogger().warn( error, e );
                launchDeathThread();
                return false;
		}
	  }
        synchronized( m_queue )
	  {
	      return ( m_queue.size() > 0 );
        }
    }

   /**
    * Post a single event to a consumer.  
    * @param event the storage object containing the structured event
    * @return boolean true if the event was succewssfull delivered
    */
    private void postEvent( StructuredEvent event ) throws Exception
    {
	  if( event == null ) throw new NullPointerException(
	    "Cannot dispatch a null event.");

	  final String name = event.header.fixed_header.event_name;
	  final String type = event.header.fixed_header.event_type.type_name;
	  try
	  {
		if( getLogger().isDebugEnabled() ) getLogger().debug(
              "sending: " + name 
              + ", type: " + type 
            );
		m_store.consumer().push_structured_event( event );
		if( getLogger().isDebugEnabled() ) getLogger().debug("sent: " + name );
	  }
	  catch( org.omg.CORBA.OBJECT_NOT_EXIST e )
	  {
		if( getLogger().isDebugEnabled() ) getLogger().debug(
		  "event " + name + " disgarded because supplier does not exist" );
	  }
	  catch( Disconnected e )
	  {
		if( getLogger().isDebugEnabled() ) getLogger().debug(
		  "event " + name + " disgarded because supplier is disconnected" );
	  }
    }

    private void launchDeathThread( )
    {
	  Thread deathThread = new Thread()
        {
		public void run()
	      {
		    if( getLogger().isDebugEnabled() ) getLogger().debug(
		 	"death-thread running (" + (mode < DISPOSAL) + ")" );

		    if( mode == SHUTDOWN )
		    {
			  //
			  // wait for the thread to die
			  //
                    if( thread != null ) try
			  {
		            if( getLogger().isDebugEnabled() ) getLogger().debug(
		 	        "death-thread waiting" );
			      thread.join();
			  }
			  catch( Throwable e )
			  {
		            if( getLogger().isDebugEnabled() ) getLogger().debug(
		 	        "death-thread interruption" );
			  }
		    }
		    if( getLogger().isDebugEnabled() ) getLogger().debug(
                  "death-thread disposal invocation" );
		    dispose();
		}
	  };
	  deathThread.start();
    }

    //======================================================================================
    // Disposable
    //======================================================================================

   /**
    * Disposal of resources consumed by the delegate.
    */
    public void dispose()
    {

        if( getLogger().isDebugEnabled() ) getLogger().debug("subscription disposal");

        //
        // destroy the storage object for this subscriber
        //

	  if( m_store != null ) try
	  {
            if( getLogger().isDebugEnabled() ) getLogger().debug( "removing subscriber storage" );
            m_store.destroy_object();
	  }
	  catch( Throwable e )
	  {
	      final String warning = "exception (ignored) while destroying storage object";
            if( getLogger().isWarnEnabled() ) getLogger().warn( warning, e );
        }

        //
        // release references to other objects
        //

        m_store = null;
        m_context = null;
	  m_manager = null;
    }
}
