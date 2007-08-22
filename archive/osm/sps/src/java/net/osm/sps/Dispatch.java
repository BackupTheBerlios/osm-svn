
package net.osm.sps;

import java.util.List;
import java.util.LinkedList;
import java.util.Iterator;
import java.util.Vector;

import org.apache.avalon.framework.logger.Logger;
import org.apache.avalon.framework.logger.LogEnabled;
import org.apache.avalon.framework.activity.Disposable;
import org.apache.avalon.framework.activity.Initializable;

import org.omg.CosPersistentState.NotFound;
import org.omg.CosPersistentState.StorageObject;
import org.omg.CosNotification.EventType;
import org.omg.CosNotification.StructuredEvent;
import org.omg.CosNotifyComm.StructuredPushConsumer;
import org.omg.CosNotifyComm.InvalidEventType;
import org.omg.CosEventComm.Disconnected;

import net.osm.event.EventStorage;
import net.osm.event.EventHome;


/**
 * 
 */

class Dispatch extends Thread
{

    //=======================================================================
    // static
    //=======================================================================

    private static final int CONTINUE = 1;
    private static final int DISPOSAL = 2;
    private static final int TERMINATION = 3;
    private static final int SLEEP = 60000;

    //=======================================================================
    // state
    //=======================================================================

   /**
    * The number on concurrent comm failures
    */
    private int m_comm_failure_count = 0;

   /**
    * The number on tolerable concurrent comm failures
    */
    private int m_comm_failure_ceiling = 10;

   /**
    * Thread execution status.
    */
    private boolean m_status = false;

   /**
    * Execution mode.
    */
    private int m_mode;

   /**
    * Logging channel.
    */
    private Logger m_logger;

   /**
    * A queue of event storage objects supplied under the Dispatch constructor.
    */
    private EventBuffer m_buffer;

   /**
    * The subscription storage object.
    */
    private Subscription m_subscription;

   /**
    * An event that is currently pending delivery and is no longer in 
    * the buffer.
    */
    private StructuredEvent m_event;

   /**
    * Convinience reference to the event home.
    */
    private EventHome m_event_home;

   /**
    * Convinience reference to the event consumer object reference.
    */
    private StructuredPushConsumer m_consumer;

    //=======================================================================
    // constructor
    //=======================================================================

    public Dispatch( Subscription subscription, int ceiling )
    {
        m_comm_failure_ceiling = ceiling;
        m_subscription = subscription;
        m_consumer = m_subscription.consumer();
        m_buffer = new EventBuffer();
        m_status = true;
        m_mode = CONTINUE;

        byte[][] queue = m_subscription.queue();
        synchronized( m_buffer )
        {
            for( int i=0; i<queue.length; i++ )
            {
                try
                {
                    EventStorage event = (EventStorage) getEventHome().find_by_short_pid( queue[i] );
                    m_buffer.add( event.structured_event() );
                }
                catch( NotFound nf )
                {
                }
            }
        }
        
        //
        // clear the persistent event queue
        //

        m_subscription.queue( new byte[0][] );
    }

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

    //======================================================================================
    // Runnable
    //======================================================================================

    public void run()
    {
        while( m_status )
        {
            if( m_mode == CONTINUE )
            {
                try
                {
                    handleBufferedEvents();
                    sleep( SLEEP );
                }
                catch( InterruptedException e )
                {
                    getLogger().debug("interrupt");
                }
	          catch( org.omg.CORBA.OBJECT_NOT_EXIST e )
	          {
                    m_subscription.notifyConsumerNotExist();
                    terminate( );
	          }
	          catch( Disconnected e )
	          {
                    m_subscription.notifyConsumerDisconnected();
                    terminate( );
	          }
                catch( Throwable e ) 
                {
                    m_subscription.notifyTerminationOnException( e );
                    terminate( );
                }
            }
            else // DISPOSAL
            {
                saveEventsToSubscription();
            }
        }
        cleanup();
    }

    //======================================================================
    // Disposable
    //======================================================================

   /**
    * Notification by the container the the dispatch thread is to 
    * terminate execution and save pending events to the subscription store.
    */
    public synchronized void dispose()
    {
        m_mode = DISPOSAL;
    }

    private void terminate()
    {
        m_status = false;
    }

    private void cleanup( )
    {
        m_subscription = null;
        m_consumer = null;
        m_buffer = null;
    }

    //======================================================================================
    // Dispatch
    //======================================================================================

   /**
    * Add a structured event to the event queue.
    * @param event the structured event to be added to the event queue
    * @exception IllegalStateException if the queue has been terminated
    */
    public void post( StructuredEvent event )
    {
        synchronized( m_buffer )
        {
            m_buffer.add( event );
        }
    }

    //======================================================================================
    // utilities
    //======================================================================================

    private void saveEventsToSubscription()
    {
        Vector vector = new Vector();
        byte[] event_pid = getShortPID( m_event );
        if( event_pid != null ) vector.add( event_pid );
        synchronized( m_buffer )
        {
            StructuredEvent event = getNextEvent();
            while( event != null )
            {
                byte[] pid = getShortPID( m_event );
                if( pid != null ) vector.add( pid );
            }
            m_subscription.queue( (byte[][]) vector.toArray( new byte[][]{} ));
        }
    }

    private byte[] getShortPID( final StructuredEvent event )
    {
        if( event == null ) return null;
        try
        {
            return getEventHome().find_by_name( 
               event.header.fixed_header.event_name ).get_short_pid();
        }
        catch( NotFound nf )
        {
            return null;
        }
    }

   /**
    * Attempt to deliver all events in the queue.
    * @return boolean true if the processing of the queue resulted in 
    *   a state where more events need to be posted (i.e. queue processing
    *   needs to be re-executed)
    */
    private void handleBufferedEvents() throws Exception
    {
        if( m_event == null ) m_event = getNextEvent();
        while(( m_event != null ) && m_status )
        {
            handleBufferedEvent( m_event );
            if( m_event == null ) m_event = getNextEvent();
        }
    }

    private StructuredEvent getNextEvent()
    {
        synchronized( m_buffer )
        {
            try
            {
                return (StructuredEvent) m_buffer.remove();
            }
            catch( IllegalStateException ise )
            {
                return null;
            }
        }
    }

    private void handleBufferedEvent( StructuredEvent event ) throws Exception
    {
        try
        {
		m_consumer.push_structured_event( event );
            m_comm_failure_count = 0;
            m_event = null;
	  }
        catch( org.omg.CORBA.COMM_FAILURE comms )
        {
            m_comm_failure_count++;
            if( m_comm_failure_count > m_comm_failure_ceiling )
            {
                throw new StructuredPushSupplierException(
                    "Comm failure ceiling exceeded.", comms );
            }
            else
            {
                return;
            }
        }
    }

    //======================================================================================
    // utilities
    //======================================================================================

    private EventHome getEventHome()
    {
        if( m_event_home != null ) return m_event_home;
        try
        {
            m_event_home = (EventHome) m_subscription.get_storage_home().get_catalog().find_storage_home( 
              "PSDL:osm.net/event/EventStorageHomeBase:1.0" );
            return m_event_home;
        }
        catch( NotFound nf )
        {
            throw new StructuredPushSupplierRuntimeException(
              "Could not locate the event storage home." );
        }
    }

}
