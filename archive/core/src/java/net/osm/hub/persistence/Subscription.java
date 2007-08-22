
package net.osm.hub.persistence;

import java.util.Arrays;
import java.util.List;
import java.util.LinkedList;

import org.omg.CosNotification.EventType;
import org.omg.CosNotifyComm.InvalidEventType;

import net.osm.hub.pss.SubscriptionStorageBase;


/**
 * A Subscription is an extension of SubscriptionStorage that provides extended
 * support for the management of event subscriptions.
 */

public class Subscription extends SubscriptionStorageBase
{
    
   /**
    * Returns true if the consumer is subscribed to the supplied event type.
    */
    public synchronized boolean is_subscribed( EventType type )
    {
	  if( type == null ) throw new NullPointerException("null event type argument");

	  EventType[] types = subscription();
        for( int i=0; i<types.length; i++ )
        {
            EventType t = types[i];
		if( t.domain_name.equals(type.domain_name) && t.type_name.equals( type.type_name )) return true;
        }
        return false;
    }
    
   /**
    * Convinience operation to change events based on supplied EventType sequences.
    */
    public synchronized void subscription_change(EventType[] added, EventType[] removed)
    throws InvalidEventType
    {

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

    private List getSubscriptionsAsList()
    {
	  List list = new LinkedList();
	  EventType[] array = subscription();
        for( int i=0; i<array.length; i++ )
	  {
		list.add( array[i] );
	  }
        return list;
    }
}
