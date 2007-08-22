
package net.osm.sps;

import java.util.Arrays;
import java.util.List;
import java.util.LinkedList;

import org.apache.avalon.framework.logger.Logger;
import org.apache.avalon.framework.logger.LogEnabled;
import org.apache.avalon.framework.activity.Initializable;
import org.apache.avalon.framework.activity.Disposable;

import org.omg.CosNotification.EventType;
import org.omg.CosNotifyComm.InvalidEventType;
import org.omg.CosNotification.StructuredEvent;
import org.omg.CosPersistentState.NotFound;

import net.osm.event.EventHome;

/**
 * A Subscription is an extension of SubscriberStorage that provides extended
 * support for the management of event subscriptions.
 */

public class Subscription extends SubscriberStorageBase implements LogEnabled, Initializable, Disposable
{
    //======================================================================
    // state
    //======================================================================

    private Logger m_logger;
    private Dispatch m_thread;
    private boolean m_terminated = false;
    private boolean m_disposed = false;

    //======================================================================
    // LogEnabled
    //======================================================================

   /**
    * Assignment of a logging channel by the container.
    * @param logger the logging channel
    */
    public void enableLogging( Logger logger )
    {
        m_logger = logger;
    }

   /**
    * Returns the logging channel.
    * @return Logger the logging channel
    */
    protected Logger getLogger()
    {
        return m_logger;
    }

    //======================================================================
    // Initializable
    //======================================================================

   /**
    * Initialize the subscribers event queue and event dispatch thread.
    */
    public synchronized void initialize()
    {
        if( m_thread != null ) return;
        getLogger().debug( "initialization" );
        m_thread = new Dispatch( this, 20 );
        m_thread.enableLogging( getLogger() );
        m_thread.start();
    }

    //======================================================================
    // Disposable
    //======================================================================

   /**
    * Disposal of the subscription during which any pending events
    * are registered under the persistent queue.
    */
    public void dispose()
    {
        if( m_disposed ) return;
        if( getLogger().isDebugEnabled() ) getLogger().debug("disposal" );
        m_disposed = true;
        if( m_thread.isAlive() ) 
        {
            m_thread.dispose();
            m_thread.interrupt();
            try
            {
                m_thread.join();
                if( getLogger().isDebugEnabled() ) getLogger().debug(
                  "dispatch thread terminated" );
            }
            catch( InterruptedException ie )
            {
            }
        }
        m_thread = null;
    }

    //======================================================================
    // SubscriberStorage
    //======================================================================

   /**
    * Returns true if the consumer is subscribed to the supplied event type.
    */
    public synchronized boolean is_subscribed( EventType type )
    {
        if( m_terminated || m_disposed ) return false;
	  if( type == null ) throw new NullPointerException("null event type argument");

	  EventType[] types = super.subscription();
        for( int i=0; i<types.length; i++ )
        {
            EventType t = types[i];
		if( t.domain_name.equals(type.domain_name) 
              && t.type_name.equals( type.type_name )) return true;
        }
        return false;
    }
    
   /**
    * Convinience operation to change events based on supplied EventType sequences.
    */
    public synchronized void subscription_change(EventType[] added, EventType[] removed)
    throws InvalidEventType
    {

        if( m_terminated || m_disposed ) return;

        //
	  // get the array, remove any event declared in remove
	  // then add the added events
	  //

        List list = getSubscriptionsAsList();
        for( int i=0; i< removed.length; i++ )
        {
            list.remove( removed[i] );
        }
        for( int i=0; i< added.length; i++ )
        {
            list.add( added[i] );
        }
        subscription( (EventType[]) list.toArray( new EventType[0] ) );

    }
       
   /**
    * Add a structured event to the event queue.
    * @param event the structured event to be added to the event queue
    * @exception IllegalStateException if the queue has been terminated
    */
    public void post( StructuredEvent event )
    {
        if( m_terminated )
        {
            throw new IllegalStateException("queue is terminated");
        }

        if( m_disposed )
        {
            throw new IllegalStateException("queue is disposed");
        }

        if( m_thread == null ) 
        {
            throw new IllegalStateException("dispatch thread has not been initalized");
        }

        m_thread.post( event );
        m_thread.interrupt();
    }

    private List getSubscriptionsAsList()
    {
        if( m_terminated || m_disposed ) return new LinkedList();

	  List list = new LinkedList();
	  EventType[] array = super.subscription();
        for( int i=0; i<array.length; i++ )
	  {
		list.add( array[i] );
	  }
        return list;
    }

    protected void notifyTerminationOnException( Throwable cause )
    {
        if( getLogger().isErrorEnabled() ) getLogger().error(
          "subscription removal due to error", cause );
        retractAndDestroy();
    }

    protected void notifyConsumerNotExist()
    {
        if( getLogger().isDebugEnabled() ) getLogger().debug(
          "removing subscrition to non-existent consumer" );
        retractAndDestroy();
    }

    protected void notifyConsumerDisconnected()
    {
        if( getLogger().isDebugEnabled() ) getLogger().debug(
          "removing subscrition to disconnected consumer" );
        retractAndDestroy();
    }

    private void retractAndDestroy()
    {
        if( m_terminated || m_disposed ) return;
        m_terminated = true;
        destroy_object();
    }
}
