/*
 * @(#)ListEvent.java
 *
 * Copyright 2000 OSM SARL. All Rights Reserved.
 * 
 * This software is the proprietary information of OSM SARL.  
 * Use is subject to license terms.
 * 
 * @author  Stephen McConnell
 * @version 1.0 24/06/2001
 */

package net.osm.util;

import java.util.EventObject;

/**
 * The <code>ListEvent</code> is an event raised by a panel
 * signally the focus of the panel itself, or of a contained item.
 */

public class ListEvent extends EventObject
{
    //============================================================
    // static
    //============================================================

    public static final int ADD = 0;

    public static final int REMOVE = 1;


   //============================================================
    // state
    //============================================================

   /**
    * List initiating the event.
    */
    public final List list;

   /**
    * Object added or removed.
    */
    public final Object object;

   /**
    * Mode.
    */
    public final int mode;


    //============================================================
    // constructor
    //============================================================

   /**
    * Creation of a new ListEvent signally the addition or removal 
    * of a object from the list.
    *
    * @param list the source list 
    * @param object the object added or removed
    * @param mode int value of ListEvent.ADD, ListEvent.REMOVE
    */
    public ListEvent( List list, Object object, int mode ) 
    {
        super( list );
        this.list = list;
        this.object = object;
        this.mode = mode;
    }

    //============================================================
    // ContextEvent
    //============================================================

   /**
    * Returns the source list.
    *
    * @return List the source list
    */
    public List getList()
    {
        return this.list;
    }

   /**
    * Returns the object triggering the change.
    * @return Object the object added or removed to/from the list
    */
    public Object getObject()
    {
        return this.object;
    }

   /**
    * Returns the event mode.
    *
    * @return int event mode, ADD or REMOVE
    */
    public int getMode()
    {
        return this.mode;
    }

   /**
    * Utility methods that handles conversion of mode integers to their string
    * representation.
    */
    public static String modeToString( int mode )
    {
	  final String added = "ADD";
        final String removed = "REMOVED";
        final String badMode = "BAD MODE";

        switch( mode )
        {
            case ADD :
                return added;
            case REMOVE:
                return removed;
        }
        return badMode;
    }

   /**
    * Returns a string representation of the event.
    */
    public String toString()
    {
        return getClass().getName() + 
          "[" +	
          "id=" + System.identityHashCode( this ) + " " + 
          "source=" + getList() + " " + 
          "object=" + getObject() + " " + 
          "mode=" + modeToString( getMode() ) + " " + 
          "]";
    }

}

