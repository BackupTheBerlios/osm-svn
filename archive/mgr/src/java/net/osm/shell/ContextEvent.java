/*
 * @(#)ContextEvent.java
 *
 * Copyright 2000 OSM SARL. All Rights Reserved.
 * 
 * This software is the proprietary information of OSM SARL.  
 * Use is subject to license terms.
 * 
 * @author  Stephen McConnell
 * @version 1.0 24/06/2001
 */

package net.osm.shell;

import java.util.EventObject;

/**
 * The <code>ContextEvent</code> is an event raised by a panel
 * signally that the panel has gained or lost logical command focus.
 */

public class ContextEvent extends EventObject
{

    //============================================================
    // static
    //============================================================

    public static final int CONTEXT_GAINED = 0;
    public static final int CONTEXT_LOST = 1;

    //============================================================
    // state
    //============================================================

   /**
    * Panel initiating the event.
    */
    public final Panel panel;

   /**
    * The event that triggered the context event.
    */
    public final EventObject event;

   /**
    * Mode.
    */
    public final int mode;


    //============================================================
    // constructor
    //============================================================

   /**
    * Creation of a new ContextEvent signally that a subsidary object
    * has gained command focus.
    *
    * @param panel the source of focus 
    * @param event the triggering event
    * @param mode int value of ContextEvent.CONTEXT_GAINED or ContextEvent.CONTEXT_LOST
    */
    public ContextEvent( Panel panel, EventObject event, int mode ) 
    {
        super( panel );
        this.mode = mode;
        this.panel = panel;
        this.event = event;
    }

   /**
    * Creation of a new ContextEvent signally that a subsidary object
    * has gained focus.
    *
    * @param Object the source of the event 
    * @param event ContextEvent to be propergated
    */
    public ContextEvent( Object source,  ContextEvent event ) 
    {
        super( source );
        this.mode = event.getMode();
        this.panel = event.getPanel();
        this.event = event.getEvent();
    }


    //============================================================
    // ContextEvent
    //============================================================

   /**
    * Returns the panel.
    * @return Panel the panel
    */
    public Panel getPanel()
    {
        return this.panel;
    }

   /**
    * Returns the event triggerring the context event.
    * @return EventObject the source event
    */
    public EventObject getEvent()
    {
        return this.event;
    }


   /**
    * Returns the event mode.
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
        final String contextGained = "CONTEXT_GAINED";
        final String contextLost = "CONTEXT_LOST";
        final String contextUnknown = "CONTEXT_UNKNOWN";

        switch( mode )
        {
            case CONTEXT_GAINED :
                return contextGained;
            case CONTEXT_LOST:
                return contextLost;
        }
        return contextUnknown;
    }

   /**
    * Returns a string representation of the event.
    */
    public String toString()
    {
        return getClass().getName() + 
		"[" +  
		"id=" + System.identityHashCode( this ) + " " +
		"source=" + getSource() + " " + 
		"panel=" + getPanel() + " " + 
		"mode=" + modeToString( getMode() ) + " " + 
            "]";
    }
}

