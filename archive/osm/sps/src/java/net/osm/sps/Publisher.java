
package net.osm.sps;

import java.util.Vector;
import java.util.List;
import java.util.LinkedList;

import org.apache.avalon.framework.logger.Logger;
import org.apache.avalon.framework.logger.LogEnabled;
import org.apache.avalon.framework.activity.Initializable;
import org.apache.avalon.framework.activity.Disposable;

import org.omg.CosNotification.StructuredEvent;
import org.omg.CosNotifyComm.StructuredPushConsumer;
import org.omg.CosPersistentState.NotFound;
import org.omg.CosNotification.EventType;

import net.osm.event.EventHome;

/**
 * The <code>AbstractResourceStore</code> extends the <code>AbstractResourceStorageBase</code>
 * class to add support for the notification of subscriber.
 */

public class Publisher extends PublisherStorageBase implements LogEnabled, Initializable, Disposable
{

    //======================================================================
    // state
    //======================================================================

    private SubscriberStorageHome m_subscriber_home;
    private EventHome m_event_home;
    private List m_subscriber_list;
    private Logger m_logger;
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
    * Initialization of the publisher.
    */
    public void initialize() throws Exception
    {
        getLogger().debug( "initialization" );

        //
        // initialize all of the subscribers
        //

        m_disposed = false;
        SubscriberStorage[] subscribers = getSubscriberArray();
        for( int i=0; i<subscribers.length; i++ )
        {
            SubscriberStorage subscriber = (SubscriberStorage) subscribers[i];
            if( subscriber instanceof LogEnabled ) ((LogEnabled)subscriber).enableLogging(
               getLogger().getChildLogger("subscriber-" + i ) );
            if( subscriber instanceof Initializable ) ((Initializable)subscriber).initialize();
        }
    }

    //======================================================================
    // Disposable
    //======================================================================

    public void dispose()
    {
        getLogger().debug("disposal");
        SubscriberStorage[] subscribers = getSubscriberArray();
        for( int i=0; i<subscribers.length; i++ )
        {
            SubscriberStorage subscriber = (SubscriberStorage) subscribers[i];
            if( subscriber instanceof Disposable ) ((Disposable)subscriber).dispose();
        }
        m_subscriber_home = null;
        m_event_home = null;
        m_subscriber_list = null;
        m_disposed = true;
    }

    //======================================================================
    // PublisherStorage
    //======================================================================

   /**
    * Add a structured push consumer to the set of subscribers.
    * @param spc the structured push consumer
    */
    public SubscriberStorage add_consumer( StructuredPushConsumer spc )
    {
        if( spc == null ) throw new NullPointerException( "Illegal null consumer argument." );
        try
        {
            SubscriberStorage subscriber = (SubscriberStorage) getSubscriberHome().create( 
              spc, new EventType[0], new byte[0][], false );
            add_subscriber( subscriber );
            if( subscriber instanceof LogEnabled ) ((LogEnabled)subscriber).enableLogging(
               getLogger().getChildLogger("subscriber-" + m_subscriber_list.size() ) );
            if( subscriber instanceof Initializable ) ((Initializable)subscriber).initialize();
            return subscriber;
        }
        catch( Throwable e )
        {
            final String error = "Unexpected error while adding consumer to publisher.";
            throw new StructuredPushSupplierRuntimeException( error, e );
        }
    }

   /**
    * Add a subscriber to the set of subscribers.
    * @param SubscriberStorage subscriber
    */
    public synchronized void add_subscriber( SubscriberStorage subscriber )
    {
        if( subscriber == null ) throw new NullPointerException( "Illegal null subscriber argument." );
        try
        {
            List list = getSubscriberList();
            list.remove( subscriber );
            list.add( subscriber );
            saveSubscriberList();
        }
        catch( Throwable e )
        {
            final String error = "Unexpected error while adding subscriber to publisher.";
            throw new StructuredPushSupplierRuntimeException( error, e );
        }
    }

   /**
    * Notification to the publisher of the disposal of a subscriber.
    * @param  subscriber - the subscription storage instance that has 
    * been disposed.
    */
    public synchronized void remove_subscriber( SubscriberStorage subscriber )
    {
        List list = getSubscriberList();
        list.remove( subscriber );
        saveSubscriberList();
    }

   /**
    * Returns TRUE if a subscription for a supplied consumer is 
    * currently established.
    * @param StructuredPushConsumer consumer
    */
    public boolean is_subscribed( StructuredPushConsumer consumer )
    {
        try
        {
            get_subscriber( consumer );
            return true;
        }
        catch( NotFound e )
        {
            return false;
        }
    }

   /**
    * Returns the subscriber matching a subscriber consumer.
    * @param StructuredPushConsumer consumer
    * @return SubscriberStorage subscription
    */
    public SubscriberStorage get_subscriber( StructuredPushConsumer consumer )
    throws NotFound
    {
        SubscriberStorage[] subscribers = getSubscriberArray();
        for( int i=0; i<subscribers.length; i++ )
        {
            SubscriberStorage subscriber = subscribers[i];
            if( subscriber.consumer()._is_equivalent( consumer ) )
            {
                return subscriber;
            }
        }
        throw new NotFound();
    }

    private boolean equivalent( byte[] arg1, byte[] arg2 )
    {
        if( arg1.length != arg2.length ) return false;
        for( int i=0; i<arg1.length; i++ )
        {
            if( arg1[i] != arg2[i] ) return false;
        }
        return true;
    }

   /**
    * If there is a subscription associated with this resource that has subscribed
    * to the event type of the suplied event, then event will be placed into a persistent
    * event storage object and passed to each respective consumer queue.
    */
    public void post( final StructuredEvent event )
    {
        //
        // Create a persistent record of the event during which the 
        // event name will be assigned.
        //

        getEventHome().create( event );

        //
        // Post the event to all of the subscriptions that have registered
        // for events matching the event type
        //

        EventType type = event.header.fixed_header.event_type;
        SubscriberStorage[] subscribers = getSubscriberArray();
        for( int i=0; i<subscribers.length; i++ )
        {
            SubscriberStorage subscriber = subscribers[i];
            if( subscriber.is_subscribed( type ) )
            {
                subscriber.post( event );
            }
        }
    }

    private synchronized List getSubscriberList()
    {
        if( m_subscriber_list != null ) return m_subscriber_list;
        m_subscriber_list = new LinkedList();

        byte[][] subscribers = super.subscribers();
        for( int i=0; i<subscribers.length; i++ )
        {
            try
            {
                SubscriberStorage subscriber = (SubscriberStorage)
                  getSubscriberHome().find_by_short_pid( subscribers[i] );
                m_subscriber_list.add( subscriber );
            }
            catch( NotFound nf )
            {
            }
        }
        return m_subscriber_list;
    }

    private SubscriberStorage[] getSubscriberArray()
    {
        List list = getSubscriberList();
        return (SubscriberStorage[]) list.toArray( 
          new SubscriberStorage[0] );
    }

    private synchronized void saveSubscriberList()
    {
        SubscriberStorage[] subscribers = getSubscriberArray();
        byte[][] array = new byte[ subscribers.length ][];
        for( int i=0; i<subscribers.length; i++ )
        {
            array[i] = subscribers[i].get_short_pid();
        }
        super.subscribers( array );
    }

    private SubscriberStorageHome getSubscriberHome()
    {
        if( m_subscriber_home != null ) return m_subscriber_home;
        try
        {
            SubscriberStorageHome m_subscriber_home = 
              (SubscriberStorageHome) get_storage_home().get_catalog().find_storage_home(
              "PSDL:osm.net/sps/SubscriberStorageHomeBase:1.0" );
            return m_subscriber_home;
        }
        catch( NotFound e )
        {
            final String error = "Could not locate SubscriberStorageHomeBase.";
            throw new StructuredPushSupplierRuntimeException( error, e );
        }
    }

    private EventHome getEventHome()
    {
        if( m_event_home != null ) return m_event_home;
        try
        {
            m_event_home = (EventHome) get_storage_home().get_catalog().find_storage_home( 
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
