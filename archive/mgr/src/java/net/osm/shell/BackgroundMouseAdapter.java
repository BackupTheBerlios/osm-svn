/*
 * @(#)ActivePanelSelectionAdapter.java
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

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.Component;
import javax.swing.JPanel;

import net.osm.shell.ContextEvent;
import net.osm.shell.ContextAdapter;


/**
 * Utility class used by ScrollView.
 */

class BackgroundMouseAdapter extends MouseAdapter
{
    //======================================================================
    // state
    //======================================================================

    private final ScrollView view;

    private ContextAdapter adapter;

   //======================================================================
    // constructor
    //======================================================================

   /**
    * Default constructor.
    */
    public BackgroundMouseAdapter( ScrollView view )
    {
        this( view, new ContextAdapter( ) );
    }

   /**
    * Default constructor.
    */
    public BackgroundMouseAdapter( ScrollView view, ContextAdapter adapter  )
    {
        this.view = view;
	  this.adapter = adapter;
        view.getViewport().addMouseListener( this );
    }

    //======================================================================
    // MouseListener
    //======================================================================

   /**
    * If the panel has focus then deselect any selected content otherwise 
    * push the panel into focus.
    * @param event the mouse event 
    */
    public void mouseReleased( MouseEvent event )
    {
        if( event.isPopupTrigger() )
	  {
	      pushPopupContext( event );
	  }
        else if( ((Component)view.getPanel()).isFocusOwner() )
	  {
		view.getPanel().clearSelection();
	  }
	  else
	  {
		((Component)view.getPanel()).requestFocus();
	  }
    }

    public void mousePressed(MouseEvent event ) 
    {
        if( event.isPopupTrigger() ) pushPopupContext( event );
    }

    private void pushPopupContext( MouseEvent event )
    {
        view.getPanel().clearSelection();
	  adapter.fireContextEvent( new ContextEvent( view.getPanel(), event, ContextEvent.CONTEXT_GAINED ) );
    }
}
