/*
 * @(#)FocusAdapter.java
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

import java.util.Iterator;
import java.util.LinkedList;
import java.awt.Component;
import java.awt.event.FocusListener;
import java.awt.event.FocusEvent;
import java.awt.AWTEventMulticaster;

/**
 * Utility class that generates focus event from context events.
 */

class FocusAdapter implements FocusListener
{
    //======================================================================
    // state
    //======================================================================

    private final Panel panel;

    private final ContextAdapter adapter;

    //======================================================================
    // constructor
    //======================================================================

   /**
    * Default constructor.
    */
    public FocusAdapter( Panel panel, ContextAdapter adapter )
    {
        this.panel = panel;
        this.adapter = adapter;
        if( ((Component)panel).isFocusable()) ((Component)panel).addFocusListener( this );
    }

    //======================================================================
    // FocusListener
    //======================================================================

    public void focusGained( FocusEvent event )
    {
        adapter.fireContextEvent( new ContextEvent( panel, event, ContextEvent.CONTEXT_GAINED ));
    }

    public void focusLost( FocusEvent event )
    {
        adapter.fireContextEvent( new ContextEvent( panel, event, ContextEvent.CONTEXT_LOST ));
    }

}
