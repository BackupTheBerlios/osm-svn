/*
 * @(#)StructuredEventUtilities.java
 *
 * Copyright 2000 OSM SARL. All Rights Reserved.
 * 
 * This software is the proprietary information of OSM SARL.  
 * Use is subject to license terms.
 * 
 * @author  Stephen McConnell
 * @version 1.0 13/04/2001
 */


package net.osm.sps;


import org.apache.time.TimeUtils;

import org.omg.CORBA.Any;
import org.omg.CORBA.ORB;
import org.omg.CosNotification.StructuredEvent;
import org.omg.CosNotification.EventType;
import org.omg.CosNotification.EventHeader;
import org.omg.CosNotification.FixedEventHeader;
import org.omg.CosNotification.Property;
import org.omg.TimeBase.UtcTHelper;

/**
 * Static utilities supporting the creation of structured events.
 */
public abstract class StructuredEventUtilities 
{
    
    // =====================================================================
    // Static methods
    // =====================================================================

   /**
    * Creation of a timestamp property for inclusion within a StructuredEvent.
    * @deprecated Use <code>timestamp()</code>
    */
    public static Property timestamp( ORB orb )
    {
        return timestamp();
    }

   /**
    * Creation of a timestamp property for inclusion within a StructuredEvent.
    */
    public static Property timestamp()
    {
        Any value = ORB.init().create_any();
        UtcTHelper.insert( value, TimeUtils.getCurrentTime() );
        return new Property( "timestamp", value );
    }

   /**
    * Creation of a new empty structured event.
    * @deprecated Use <code>createEvent( EventType type, Property[] props )</code>
    */
    public static StructuredEvent createEvent( ORB orb, EventType type, Property[] props )
    {
        return createEvent( type, props );
    }

   /**
    * Creation of a new empty structured event.
    */
    public static StructuredEvent createEvent( EventType type, Property[] props )
    {
        EventHeader header = new EventHeader( 
           new FixedEventHeader( type, "untitled event" ), new Property[0] );
        Any any = ORB.init().create_any();
        any.insert_string("");
        return new StructuredEvent( header, props, any );
    }

   /**
    * Returns a filterable event property from the supplied event that matches
    * the supplied property name.  If the name is not found the operation returns
    * null.
    * @param event the structured event containgin the filterable properties
    * @param name the property name to search for
    * @return Property a property matching the supplied name or null if no property
    *   matches the name
    */
    public static Property getFilterableProperty( StructuredEvent event, String name )
    {
        Property[] properties = event.filterable_data;
	  for( int i=0; i<properties.length; i++ )
	  {
		if( properties[i].name.equals( name ) ) return properties[i];
	  }
	  return null;
    }


}
