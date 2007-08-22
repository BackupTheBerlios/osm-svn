/*
 * @(#)ScrollView.java
 *
 * Copyright 2000 OSM SARL. All Rights Reserved.
 * 
 * This software is the proprietary information of OSM SARL.  
 * Use is subject to license terms.
 * 
 * @author  Stephen McConnell
 * @version 1.0 03/09/2001
 */

package net.osm.shell;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import javax.swing.JScrollPane;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

import net.osm.shell.MGR;
import net.osm.shell.View;
import net.osm.shell.Panel;
import net.osm.shell.ContextEvent;
import net.osm.shell.ContextListener;
import net.osm.shell.ContextAdapter;


/**
 */

public class ScrollView extends JScrollPane implements View, ContextListener
{
    private boolean trace = true;

    private boolean hasVisibleFocus = false;

    private Panel panel;

   /**
    * Context adapter.
    */
    private final ContextAdapter contextAdapter;

   /**
    * Mouse adapter to handle background clicks.
    */
    private BackgroundMouseAdapter mouseAdapter; 

    private boolean showFocus = true;

    private boolean preferred = false;


    //======================================================================
    // Constructors
    //======================================================================

    public ScrollView( Panel panel )
    {
        this( panel, true );
    }

    public ScrollView( Panel panel, boolean showFocus )
    {
        super();

        this.showFocus = showFocus;
        contextAdapter = new ContextAdapter( );

        setVerticalScrollBarPolicy( 
          JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED );
        setHorizontalScrollBarPolicy( 
          JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED );

        setPanel( panel );
        if( showFocus )
        {
		setBorder( View.emptyBorder );
        	setVisibleFocus( false );
	  }
        getViewport().setBackground( MGR.background );
        if( showFocus ) mouseAdapter = new BackgroundMouseAdapter( this, contextAdapter );
    }

    public void setPreferred( boolean value )
    {
        preferred = value;
    }

    public boolean getPreferred( )
    {
        return preferred;
    }

    //======================================================================
    // View
    //======================================================================

   /**
    * Get the panel in this view.
    *
    * @return Panel the panel from the view.
    */
    public Panel getPanel( )
    {
        return panel;
    }

    //======================================================================
    // ContextHandler
    //======================================================================

   /**
    * Adds a <code>ContextListener</code>.
    */
    public void addContextListener( ContextListener listener )
    {
        contextAdapter.addContextListener( listener );
    }

   /**
    * Removes a <code>ContextListener</code>.
    */
    public void removeContextListener( ContextListener listener )
    {
        contextAdapter.removeContextListener( listener );
    }

    //======================================================================
    // ContextListener
    //======================================================================

    public void contextGained( ContextEvent event )
    {
        if( showFocus ) setVisibleFocus( true );
        contextAdapter.fireContextEvent( new ContextEvent( this, event ) );
    }

    public void contextLost( ContextEvent event )
    {
        contextAdapter.fireContextEvent( new ContextEvent( this, event ) );
    }

    //======================================================================
    // ScrollView
    //======================================================================

   /**
    * Set the panel in this view.
    *
    * @param panel the panel to add to the view.
    */
    public void setPanel( Panel panel )
    {
        if( !( panel instanceof Component ) ) throw new RuntimeException(
		"Supplied panel is not a component.");

        if( this.panel != null ) this.panel.removeContextListener( this );
        this.panel = panel;
        setViewportView( (Component) panel );
        panel.addContextListener( this );
    }

   /**
    * Sets the visible adorments of the view to reflect command focus.
    */
    public void setVisibleFocus( boolean value )
    {
        if( showFocus )
	  {
	      if( value )
	      {
                setBorder( View.focusBorder );
	      }
	      else
	      {
                setBorder( View.emptyBorder );
	      }
	  }
    }

    //======================================================================
    // Object
    //======================================================================

    public String toString()
    {
        return getClass().getName() + 
		"[" +  
		"id=" + System.identityHashCode( this ) + " " +
		"Panel=" + getPanel() +  " " +
		"Focussed=" + hasVisibleFocus + " " +
            "]";
    }

}
