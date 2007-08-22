/*
 * TabbedView.java
 *
 * Copyright 2000 OSM SARL. All Rights Reserved.
 * 
 * This software is the proprietary information of OSM SARL.  
 * Use is subject to license terms.
 * 
 * @author  Stephen McConnell
 * @version 1.0 14/09/2001
 */

package net.osm.shell;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Component;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseAdapter;
import java.awt.event.ContainerListener;
import java.awt.event.ContainerEvent;
import java.util.Iterator;
import java.util.List;
import java.util.LinkedList;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JScrollPane;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import net.osm.shell.View;
import net.osm.shell.ScrollView;
import net.osm.shell.SplitPane;


/**
 * This class is a tabbed pane containing multiple views.  The view 
 * is intended to provide a relativly static framework within which 
 * subsidary views are switch in and out.  As such, the class provides
 * an operation to set the primary <code>Entity</code> and the 
 * the preferred tab position.  The implementation extracts views from 
 * the entities <code>getViews</code> method and populates the tab pane
 * with the views in the order returned from the <code>getViews</code> 
 * method.
 */

class PropertiesView extends JTabbedPane implements View, ContextListener, ChangeListener
{

    //========================================================================
    // state
    //========================================================================

   /**
    * Context adapter.
    */
    private final ContextAdapter contextAdapter = new ContextAdapter();

   /**
    * Current entity that the tabbed panel is presenting.
    */
    private Entity entity;

   /**
    * The last view in focus.
    */
    private View focus;

    private Desktop desktop;

   /**
    * Panel established by context event.
    */
    private Panel panel;

    private PopupMenu popup;

    private int preferred = -1;

    //========================================================================
    // Constructor
    //========================================================================

   /**
    * Creation of a new <code>PropertiesPanel</code>.
    */
    public PropertiesView( Desktop desktop, Entity entity )
    {
	  super();

        this.desktop = desktop;
	  addChangeListener( this );
        setBorder( new EmptyBorder( 20, 10, 10, 10 ));
        setFont( desktop.getDefaultFont() );

        popup = new PopupMenu( desktop );

	  addTab( "Features", 
	    new ListTab("Features", 
		new ScrollView( 
          	  new FeaturesPanel( entity, "Features", desktop.getDefaultFont() )
            )
	    )
        );

        List views = entity.getPropertyPanels();
	  Iterator iterator = views.iterator();
        while( iterator.hasNext() )
	  {
		addTab( (View) iterator.next() );
	  }

	  if( preferred > -1 ) setSelectedIndex( preferred );
    }

    public void addTab( View view )
    {
        if( view instanceof ScrollView )
	  {
	      ScrollView v = (ScrollView) view;
	      String role = v.getPanel().getRole();
	      ListTab tab = new ListTab( role, v );
	      super.addTab( role, tab );
		if( v.getPreferred() ) preferred = getTabCount() -1;
	  }
	  else if( view instanceof SplitPane )
	  {
	      SplitPane v = (SplitPane) view;
	      String role = v.getRole();
	      ListTab tab = new ListTab( role, v );
	      super.addTab( role, tab );	  
	  }
        else
        {
            final String role = view.getPanel().getRole();
            super.addTab( role, (Component) view );
        }
        view.addContextListener( this );

    }

    //======================================================================
    // View
    //======================================================================

   /**
    * Adds a <code>ContextListener</code> to the item.
    */
    public void addContextListener( ContextListener listener )
    {
        contextAdapter.addContextListener( listener );
    }

   /**
    * Removes a <code>ContextListener</code> from the handler.
    */
    public void removeContextListener( ContextListener listener )
    {
        contextAdapter.removeContextListener( listener );
    }

   /**
    * Get the panel in the currently active view in this tabbed view.
    *
    * @return Panel the panel from the current view.
    */
    public Panel getPanel( )
    {
	  Component c = getSelectedComponent();
	  View v = (View) c;
	  return v.getPanel();
    }

   /**
    * Sets the visible adorments of the view to reflect active status.
    */
    public void setVisibleFocus( boolean value )
    {
	  try
	  {
	      Component c = getSelectedComponent();
	      View v = (View) c;
	      v.setVisibleFocus( value );
        }
	  catch( Exception e )
	  {
	  }
    }


    //======================================================================
    // ContextListener
    //======================================================================

    public void contextGained( ContextEvent event )
    {
        panel = event.getPanel();
        if( event.getEvent() instanceof MouseEvent )
        {
		MouseEvent me = (MouseEvent) event.getEvent();
            if( me.isPopupTrigger() ) 
		{
		    popup.setPanel( panel, me );
		    popup.show( me.getComponent(), me.getX(), me.getY() );
		}
        }
    }

    public void contextLost( ContextEvent event )
    {
    }

    //======================================================================
    // ChangeListener
    //======================================================================

    public void stateChanged( ChangeEvent event )
    {
	  try
	  {
            if( getSelectedComponent() != focus ) if( focus != null ) focus.setVisibleFocus( false );
            if( getSelectedComponent() instanceof View ) 
	      {
		    focus = (View)getSelectedComponent();
		    contextAdapter.fireContextEvent( 
			new ContextEvent( focus.getPanel(), event, ContextEvent.CONTEXT_GAINED ));
            }
        }
	  catch( Exception e )
	  {
	  }
    }

    public void setVisible( boolean value )
    {
	  System.out.println("setting visible: " + this );
	  super.setVisible( value );
    }

    //======================================================================
    // Object
    //======================================================================

    public String toString()
    {
        return getClass().getName() + 
		"[" +  
		"id=" + System.identityHashCode( this ) + 
		"selected=" + getSelectedComponent() +
            "]";
    }
}
