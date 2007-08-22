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

import net.osm.shell.MGR;
import net.osm.shell.Panel;
import net.osm.shell.Entity;
import net.osm.shell.View;
import net.osm.shell.ContextListener;
import net.osm.shell.ContextEvent;
import net.osm.shell.ContextAdapter;


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

public class TabbedView extends JTabbedPane implements View, ContextListener, ChangeListener
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

    //========================================================================
    // Constructor
    //========================================================================

   /**
    * Creation of a new <code>TabbedPane</code>.
    */
    public TabbedView( Entity entity )
    {
         this( entity, entity.getViews() );
    }

   /**
    * Creation of a new <code>TabbedPane</code>.
    */
    public TabbedView( Entity entity, List views )
    {
	  super();
        setFont( MGR.font );
	  addChangeListener( this );
        //setUI( new OSMTabbedPaneUI());
        setTabLayoutPolicy( JTabbedPane.SCROLL_TAB_LAYOUT );
	  Iterator iterator = views.iterator();
        while( iterator.hasNext() )
	  {
		try
		{
		    View view = (View) iterator.next();
		    String role = view.getPanel().getRole();
                addTab( role, (Component) view );
                view.addContextListener( this );
		}
		catch( Exception e )
	      {
		    throw new RuntimeException(
		      "TabbedPane.  Unexpected exception while populating pane.", e );
	      }
	  }
    }

    //======================================================================
    // View
    //======================================================================

   /**
    * Adds a <code>ContextListener</code> to the view.
    */
    public void addContextListener( ContextListener listener )
    {
        contextAdapter.addContextListener( listener );
    }

   /**
    * Removes a <code>ContextListener</code> from the view.
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
        contextAdapter.fireContextEvent( new ContextEvent( this, event ) );
    }

    public void contextLost( ContextEvent event )
    {
	  setVisibleFocus( false );
        contextAdapter.fireContextEvent( new ContextEvent( this, event ) );
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
