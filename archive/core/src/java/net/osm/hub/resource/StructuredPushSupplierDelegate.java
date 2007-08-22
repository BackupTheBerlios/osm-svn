
package net.osm.hub.resource;

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
import org.apache.avalon.framework.component.ComponentManager;
import org.apache.avalon.framework.component.ComponentException;
import org.apache.avalon.framework.component.Composable;
import org.apache.avalon.phoenix.BlockContext;
import org.apache.avalon.phoenix.Block;

import org.omg.CosPersistentState.NotFound;
import org.omg.CosPersistentState.StorageObject;
import org.omg.CosNotification.EventType;
import org.omg.CosNotification.StructuredEvent;
import org.omg.CosNotifyComm.StructuredPushSupplier;
import org.omg.CosNotifyComm.InvalidEventType;
import org.omg.CosEventComm.Disconnected;

import net.osm.event.EventStorage;
import net.osm.event.EventStorageHome;
import net.osm.hub.gateway.DefaultDelegate;
import net.osm.hub.gateway.Manager;
import net.osm.hub.gateway.ServantContext;
import net.osm.hub.pss.SubscriberStorage;
import net.osm.hub.pss.SubscriberStorageHome;
import net.osm.hub.pss.AbstractResourceStorage;
import net.osm.list.List;
import net.osm.list.LinkedList;
import net.osm.list.NoEntry;
import net.osm.util.Incrementor;
import net.osm.util.ExceptionHelper;

import net.osm.session.SubscriberProxyOperations;


/**
 * Supplier is an object associated to exactly one StructuredPushConsumer that
 * is responsible for handling the issuance of StructuredEvents to the consumer,
 * and responding to disconnect request.
 */

public class StructuredPushSupplierDelegate extends DefaultDelegate 
implements Block, Contextualizable, Composable, Initializable, SubscriberProxyOperations, Runnable
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
    protected ServantContext context;

   /**
    * The storage object for this subscription.
    */
    protected SubscriberStorage store;

   /**
    * The component manager that we notify completion to.
    */
    private Manager manager;

   /**
    * The event storage home used when entering events into the subscriber
    * event queue.
    */
    private EventStorageHome eventHome;

    private Thread thread;

    private boolean disposed = false;

    private int trip = 0;

    private int commFailureCount = 0;
    private final int commFailureCeiling = 6;

    //=======================================================================
    // Composable
    //=======================================================================

    /**
     * Pass the <code>ComponentManager</code> to the <code>Composable</code>.
     * The <code>Composable</code> implementation uses the supplied
     * <code>ComponentManager</code> to acquire the components it needs for
     * execution.
     * @param manager the <code>ComponentManager</code> for this delegate
     * @exception ComponentException
     */
    public void compose( ComponentManager manager )
    throws ComponentException
    {
	  super.compose( manager );
        if( getLogger().isDebugEnabled() ) getLogger().debug( "composition" );
        if( !( manager instanceof Manager )) throw new ComponentException( "invalid manager" );
	  this.manager = (Manager) manager;
    }

        
    //=======================================================================
    // Contextualizable
    //=======================================================================
   
   /**
    * Establish the servant context.
    * @param context the servant context
    */
    public void contextualize( Context context ) 
    throws ContextException
    {
	  super.contextualize( context );
        if( getLogger().isDebugEnabled() ) getLogger().debug("contextualization");
        if( !( context instanceof ServantContext )) throw new ContextException( "invalid context" );
	  this.context = (ServantContext) context;
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
        if( getLogger().isDebugEnabled() ) getLogger().debug("initialization");
        this.store = (SubscriberStorage) context.getStorageObject();
    }

    //=======================================================================
    // StructuredPushSupplier
    //=======================================================================
    
   /**
    * The disconnect_structured_push_supplier operation is invoked to terminate a
    * connection between the target StructuredPushSupplier, and its associated consumer.
    * This operation takes no input parameters and returns no values. The result of this
    * operation is that the target StructuredPushSupplier will release all resources it had
    * allocated to support the connection, and dispose its own object reference.
    */
    public void disconnect_structured_push_supplier()
    {
	  mode = DISPOSAL;
        if( getLogger().isDebugEnabled() ) getLogger().debug("disconnection");
        launchDeathThread();
    }

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
        if( added == null ) throw new NullPointerException(
		"Null added argument supplied to subscription change.");
        if( removed == null ) throw new NullPointerException(
		"Null removed argument supplied to subscription change.");

        synchronized( store )
	  {
            if( getLogger().isDebugEnabled() ) getLogger().debug("subscription change");
            java.util.List list = getSubscriptionsAsList();
            for( int i=0; i< removed.length; i++ )
            {
                list.remove( removed[i] );
            }
            for( int i=0; i< added.length; i++ )
            {
                list.add( added[i] );
            }
            store.subscription( (EventType[]) list.toArray( new EventType[0] ) );
        }
    }

    //======================================================================================
    // SubscriberProxy
    //======================================================================================

   /**
    * Return the persitent object identifier for this object.
    * @return byte[] the persitent object identifier
    */
    public byte[] getIdentifier() // operation is redundant ??
    {
        return store.get_pid();
    }

   /**
    * Adds a structured event to the event queue.
    * @param event the structured event to add to the queue.
    */
    public synchronized void post( StructuredEvent structuredEvent )
    {
	  if( mode > NORMAL ) return;
	  if( structuredEvent == null ) throw new NullPointerException(
	    "Null structured event argument supplied to post.");
        if( getLogger().isDebugEnabled() ) getLogger().debug("adding structured event to queue");

        synchronized( store )
	  {
	      final long id = eventIncrementor.increment();
		structuredEvent.header.fixed_header.event_name = "" + id;
	      EventStorage event = getEventHome().create( id, structuredEvent );
		if( getLogger().isDebugEnabled() ) getLogger().debug( 
		  "created new event storage with id: " + id );
		net.osm.list.LinkedList queue = store.queue();
		synchronized( queue )
		{
                try
                {
		        queue.add( event );
                }
                catch( Throwable e )
                {
                    //
                    // don't throw any exceptions back to the client - this is 
                    // a post and forget operation - instead write exceptions 
                    // and a stack trace to the log file
                    //

                    if( getLogger().isErrorEnabled() ) getLogger().error(
			    "Internal error while queuing an event.", e );
                }
		}
	  }

	  //
	  // launch a thread to handle event dispatch
	  //

	  initiateEventDispach();
    }

   /**
    * Initate the termination sequence for this proxy.  The implementation
    * shall complete delivery of all pending events followed by servant
    * disposal.
    */
    public void terminate( boolean policy )
    {
	  mode = SHUTDOWN;
	  store.terminal( true );
        if( getLogger().isDebugEnabled() ) getLogger().debug( "termination" );
        launchDeathThread( );
    }

    //======================================================================================
    // utilities
    //======================================================================================

    private java.util.List getSubscriptionsAsList()
    {
	  java.util.List list = new java.util.LinkedList();
	  EventType[] array = store.subscription();
        for( int i=0; i<array.length; i++ )
	  {
		list.add( array[i] );
	  }
        return list;
    }

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
		    Thread.currentThread().sleep( 100 );
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
      try
	{
	  if( mode == DISPOSAL ) return false;

	  if( getLogger().isDebugEnabled() ) getLogger().debug("processing pending event queue, "
	    + store.queue().size() + ")" );

        trip = 0;
        LinkedList list = store.queue();
	  synchronized( list )
	  {
            net.osm.list.Iterator iterator = list.iterator();
            while( iterator.has_next() && mode < DISPOSAL )
            {
		    EventStorage event = null;
		    try
		    {
			  Object object = iterator.next();
			  if( !( object instanceof EventStorage ) ) 
			  {
				// ummm, bizzar .. should not happen because this class
				// is controlling additions to the queue and is adding addition 
                        // as EventStorage instancee - but has been known
			      // to happen when using PSS database connector

			     throw new IllegalStateException(
				"Event queue returned an invalid storage object: " 
				  + object + " in list: " + list );
			  }

		        event = (EventStorage) object;
			  postEvent( event );
		        iterator.remove();
			  try
			  {
		            event.destroy_object();
			  }
			  catch( Throwable eventError )
			  {
				final String warning = "Failed to destroy event after processing.";
				if( getLogger().isWarnEnabled() ) getLogger().warn( warning, eventError );
				System.out.println(warning);
			  }
		    }
		    catch( NoEntry noEntry )
		    {
		        final String info = "Iterator return no-entry.";
		        if( getLogger().isWarnEnabled() ) getLogger().warn( info, noEntry );
			  return false;
		    }
		    catch( org.omg.CORBA.COMM_FAILURE comms )
		    {
			  if( commFailureCount > commFailureCeiling )
			  {
				final String failure = "Communications failure exceeded ceiling.";
			      mode = DISPOSAL;
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
			  final String error = "Subscriber event queue processing failure - terminating queue.";
			  if( getLogger().isWarnEnabled() ) getLogger().warn( error, e );
			  ExceptionHelper.printException( error, e, this, true );
			  launchDeathThread();
			  return false;
		    }
		}
	  }
        if( store != null ) synchronized( store )
	  {
	      return ( store.queue().size() > 0 );
        }
	  else
	  {
		return false;
	  }
      }
      catch( Throwable throwable )
	{
         final String error = "Unexpected internal error.";
	   ExceptionHelper.printException( error, throwable, this );
	   if( getLogger().isErrorEnabled() ) getLogger().error( error, throwable );
	   return false;
	}
    }

   /**
    * Post a single event to a consumed.  
    * @param event the storage object containing the structured event
    * @return boolean true if the event was succewssfull delivered
    */
    private void postEvent( EventStorage event ) throws Exception
    {
	  if( event == null ) throw new NullPointerException(
	    "Cannot dispatch a null event.");

	  final String name = event.structured_event().header.fixed_header.event_name;
	  try
	  {
		if( getLogger().isDebugEnabled() ) getLogger().debug("sending: " + name );
		store.consumer().push_structured_event( event.structured_event() );
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

    private void purgeEvents()
    {
	  synchronized( store )
	  {
            net.osm.list.Iterator iterator = store.queue().iterator();
            while( iterator.has_next() )
            {
		    try
	          {
		        EventStorage event = (EventStorage) iterator.next();
		        iterator.remove();
		        event.destroy_object();
		    }
		    catch( Throwable e )
	          {
	          }
	      }
        }
    }
    
   /**
    * Returns the storage home for EventStorage types.
    * @osm.warning this service should be moved to the component manager
    */
    protected EventStorageHome getEventHome()
    {
	  if( eventHome != null ) return eventHome;
	  try
	  {
            eventHome = ( EventStorageHome ) getSession().find_storage_home( 
			"PSDL:osm.net/event/EventStorageHomeBase:1.0" );
	      return eventHome;
	  }
	  catch( Throwable e )
	  {
		throw new RuntimeException("Event storage home not found.", e ); 
	  }
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
	  // retract this subscriber from its list of subscribers
	  //

        if( store != null ) if( !store.terminal() )
        {
            if( getLogger().isDebugEnabled() ) getLogger().debug("retracting subscription");
	      AbstractResourceStorage r = null;
	      try
	      {
	          r = (AbstractResourceStorage) store.
			get_storage_home().get_catalog().find_by_pid( store.resource() );
		    r.subscriptions().remove( store );
	      }
	      catch( NotFound e )
	      {
	          // ignore this case because a NotFound means that we are not in the 
                // list of subscribers
	      }
	      catch( Throwable e )
	      {
	          final String warning = "Unexpected error (ignored) while removing subscriber from resource.";
                if( getLogger().isWarnEnabled() ) getLogger().warn( warning, e );
	      }
        }
        else
	  {
            final String debug = "primary resource has been terminated";
            if( getLogger().isDebugEnabled() ) getLogger().debug( debug );
        }

        //
        // make sure this delegate has been removed from the pool of supplier delegates
        //

	  if( store != null ) try
	  {
            if( getLogger().isDebugEnabled() ) getLogger().debug( "removing subscriber delegate" );
            manager.remove( store.get_pid() );
	  }
	  catch( Throwable e )
	  {
	      final String warning = "exception (ignored) while notifying manager of subscription removal";
            if( getLogger().isWarnEnabled() ) getLogger().warn( warning, e );
        }

        //
        // destroy the storage object for this subscriber
        //

	  if( store != null ) try
	  {
            if( getLogger().isDebugEnabled() ) getLogger().debug( "removing subscriber storage" );
            store.destroy_object();
	  }
	  catch( Throwable e )
	  {
	      final String warning = "exception (ignored) while destroying storage object";
            if( getLogger().isWarnEnabled() ) getLogger().warn( warning, e );
        }

        //
        // release references to other objects
        //

        store = null;
        context = null;
	  manager = null;
	  eventHome = null;
    }
}
