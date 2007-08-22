/*
 * @(#)RemoteEvent.java
 *
 * Copyright 2000 OSM SARL. All Rights Reserved.
 * 
 * This software is the proprietary information of OSM SARL.  
 * Use is subject to license terms.
 * 
 * @author  Stephen McConnell
 * @version 1.0 24/06/2001
 */

package net.osm.audit;

import java.util.EventObject;
import java.util.Hashtable;

import org.omg.CORBA.Any;
import org.omg.CosNotification.StructuredEvent;
import org.omg.CosNotification.EventType;
import net.osm.audit.home.Adapter;


/**
 * The <code>RemoteEvent</code> class is an event type that contains
 * the a source <code>Adapter</code> and a <code>StructuredEvent</code>.
 */

public class RemoteEvent extends EventObject
{

    private StructuredEvent event;

    private Hashtable table;

    public RemoteEvent( Object source, StructuredEvent event ) 
    {    
        super( source );
        this.event = event;
    }

   /**
    * Return the original remote structured event.
    * @return StructuredEvent the structured event
    */
    public StructuredEvent getEvent( )
    {
        return event;
    }

    //
    // FixedEventHeader content
    //

   /**
    * Return the structured event fixed header event type domain name.
    * @return String event domain name
    * @see EventType
    */
    public String getDomain()
    {
        return event.header.fixed_header.event_type.domain_name;
    }

   /**
    * Return the structured event fixed header event type, type name.
    * @return String event type name
    * @see EventType
    */
    public String getType()
    {
        return event.header.fixed_header.event_type.type_name;
    }

   /**
    * Return the structured event fixed header event name.
    * @return String event name
    * @see org.omg.CosNotification.FixedEventHeader
    */
    public String getName()
    {
        return event.header.fixed_header.event_name;
    }

   /**
    * Return the structured event fixed header properties.
    * @return org.omg.CosNotification.Property[] fixed header properties
    * @see org.omg.CosNotification.FixedEventHeader
    */
    public org.omg.CosNotification.Property[] getHeaderProperties()
    {
        return event.header.variable_header;
    }

   /**
    * Return the structured event properties.
    * @return org.omg.CosNotification.Property[] the event filterable properties
    * @see org.omg.CosNotification.StructuredEvent
    */
    public org.omg.CosNotification.Property[] getProperties()
    {
        return event.filterable_data;
    }

   /**
    * Returns a property based on a supplied name or null if no 
    * property matches the name.
    * @param name property name
    * @return org.omg.CORBA.Any property value
    */
    public Any getProperty( String name )
    {
	  if( table == null ) table = mapPropertiesToHashtable();
        return (Any) table.get( name );
    }

   /**
    * Return the remainder portion of the structured event.
    * @return org.omg.CORBA.Any remainder of the event
    * @see org.omg.CosNotification.StructuredEvent
    */
    public Any getRemainder()
    {
        return event.remainder_of_body;
    }

   /**
    * Creates a hashtable of the property names and values.
    */
    private Hashtable mapPropertiesToHashtable()
    {
        Hashtable table = new Hashtable();
        for( int i=0; i<getProperties().length; i++ )
	  {
		org.omg.CosNotification.Property property = getProperties()[i];
	      table.put( property.name, property.value );	    
	  }
	  return table;
    }

   /**
    * Returns a string representation of the event.
    */
    public String toString()
    {
        return getClass().getName() + 
          "[" +	
          "system=" + System.identityHashCode( this ) + " " + 
          "domain=" + getDomain() + " " + 
          "type=" + getType() + " " + 
          "name=" + getName() + " " + 
          "]";
    }

}
