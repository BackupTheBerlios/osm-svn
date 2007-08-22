/*
 * @(#)SplitPane.java
 *
 * Copyright 2000 OSM SARL. All Rights Reserved.
 * 
 * This software is the proprietary information of OSM SARL.  
 * Use is subject to license terms.
 * 
 * @author  Stephen McConnell
 * @version 1.0 30/06/2001
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
import java.util.LinkedList;
import javax.swing.JPanel;
import javax.swing.JComponent;
import javax.swing.JSplitPane;
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
 * This class is a split pane containing two associated views. The left view
 * is a primary view that triggers the loading of the right view based on the 
 * the left view selection status.
 */

public class SplitPane extends JSplitPane implements ContextListener, View
{

    protected boolean trace = true;

   /**
    * Default offset of the view divider.
    */
    private int offset = 200;

   /**
    * Default width of the divider.
    */
    private int divider = 5;

   /**
    * The left view.
    */
    private final View left;

   /**
    * The right view.
    */
    private View right;

   /**
    * Local reference to the view in focus.
    */
    private View focus;

   /**
    * Context adapter.
    */
    private final ContextAdapter contextAdapter = new ContextAdapter();

   /**
    * Role of the view.
    */
    private String role;


    //========================================================================
    // Constructor
    //========================================================================

   /**
    * Creation of a new <code>SplitPane</code> with an initial left view suppled
    * as a constructor argument. The implementation listens to ContextEvents 
    * propergated by a left and right panel and maintains visual focus feedback
    * to the user.  Context events propergated by the right and left panels are
    * exposed to ContextEvent listeners attached to the SplitPane.
    *
    * @param view the view to be placed in the left panel.
    */
    public SplitPane( View view )
    {
        this( "Navigator", view );
    }

   /**
    * Creation of a new <code>SplitPane</code> with an initial left view suppled
    * as a constructor argument. The implementation listens to ContextEvents 
    * propergated by a left and right panel and maintains visual focus feedback
    * to the user.  Context events propergated by the right and left panels are
    * exposed to ContextEvent listeners attached to the SplitPane.
    *
    * @param view the view to be placed in the left panel.
    */
    public SplitPane( String role, View view )
    {
	  super();
        
	  if(!( view instanceof Component )) 
          throw new RuntimeException("View is not a Component.");

        this.left = view;
	  this.focus = left;
        this.role = role;

	  setOneTouchExpandable( false );
	  setDividerSize( divider );

        left.addContextListener( this );
	  setLeftComponent( (Component) left );
	  
        setDividerLocation( offset );
	  if( view.getPanel() != null )
	  {
		if( view.getPanel().getDefaultEntity() != null )
		{
		    setRight( view.getPanel().getDefaultEntity().getView() );
		}
		else
		{
                setRight( null );
		}
	  }
    }

    //======================================================================
    // ContextHandler
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

    //======================================================================
    // ContextListener
    //======================================================================

    public void contextGained( ContextEvent event )
    {
        contextAdapter.fireContextEvent( new ContextEvent( this, event ) );
	  handleContext( event );
    }

    public void contextLost( ContextEvent event )
    {
        contextAdapter.fireContextEvent( new ContextEvent( this, event ) );
    }

    public void handleContext( ContextEvent event )
    {
        if( !( event.getSource() instanceof View ) ) return;
        View view = (View) event.getSource();

        if( view != focus ) if( view == left ) focus = left; else focus = right;
        if( view == left )
        {
		Entity entity = view.getPanel().getDefaultEntity();
		if( entity != null ) 
		{
		    View v = entity.getView();
		    if( v != right ) setRight( v );
		}
        }
    }

   /**
    * Sets the right panel to the supplied value.
    */
    public void setRight( View view )
    {
        if( right != null ) right.removeContextListener( this );
	  setLastDividerLocation( getDividerLocation( ) );
        right = view;
	  if( right != null )
	  {
            setRightComponent( (Component) right );
        }
	  else
	  {
		JPanel dummy = new JPanel();
		dummy.setBackground( MGR.background );
            setRightComponent( new JScrollPane( dummy ));
	  }
        if( right != null ) 
	  {
		right.addContextListener( this );
	  	((Component)right).requestFocus();
	  }
        setDividerLocation( getLastDividerLocation() );
    }

    public String getRole()
    {
        return this.role;
    }

    //======================================================================
    // View
    //======================================================================

    public Panel getPanel()
    {
	  return left.getPanel();
    }

    public void setVisibleFocus( boolean value )
    {
	  left.setVisibleFocus( value );
    }

    //======================================================================
    // Object
    //======================================================================

    public String toString()
    {
        return getClass().getName() + 
		"[" +  
		"id=" + System.identityHashCode( this ) + " " +
		"left=" + left +  " " +
		"right=" + right + " " +
            "]";
    }
}
