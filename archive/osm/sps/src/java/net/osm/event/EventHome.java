
package net.osm.event;

import java.util.Vector;

import org.omg.CosNotification.StructuredEvent;
import org.omg.CosNotifyComm.StructuredPushConsumer;
import org.omg.CosPersistentState.NotFound;
import org.omg.CosNotification.EventType;

import org.apache.pss.util.Incrementor;

/**
 * The <code>AbstractResourceStore</code> extends the <code>AbstractResourceStorageBase</code>
 * class to add support for the notification of subscriber.
 */

public class EventHome extends EventStorageHomeBase
{

    private static final Incrementor incrementor = Incrementor.create("EVENT");

   /**
    * Creation of a new persistent event entry.
    * @param event the structured event
    */
    public EventStorage create( final StructuredEvent event )
    {
        final String name = "" + incrementor.increment();
        event.header.fixed_header.event_name = name;
        return super.create_event( event, name );
    }

}
