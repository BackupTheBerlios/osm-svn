/*
 * NavigatorPanel.java
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
import java.util.List;
import java.util.LinkedList;
import java.awt.Component;
import java.awt.event.ContainerEvent;
import java.awt.event.ContainerListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusListener;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.FlowLayout;
import java.lang.reflect.InvocationTargetException;
import java.lang.NoSuchMethodException;
import java.lang.IllegalAccessException;
import java.lang.reflect.Method;
import javax.swing.JPanel;
import javax.swing.table.TableModel;
import javax.swing.table.TableColumnModel;
import javax.swing.ListSelectionModel;
import javax.swing.JTree;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeModel;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeSelectionModel;
import javax.swing.event.TreeSelectionListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeWillExpandListener;
import javax.swing.event.TreeExpansionEvent;
import java.awt.Color;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;

import net.osm.util.ListHandler;
import net.osm.util.ListListener;
import net.osm.util.ListEvent;

/**
 * A panel that presents a heirachical breakdown of entities.
 */

public class NavigatorPanel extends JTree implements Panel, TreeModelListener, TreeSelectionListener
{
    //======================================================================
    // static
    //======================================================================

    public static final Border emptyBorder = new LineBorder( MGR.background, 2 );

    //======================================================================
    // state
    //======================================================================

   /**
    * The entity that this panel is presenting.
    */
    private Entity entity;

   /**
    * The role that this panel represents relative to the entity.
    */
    private String role;

   /**
    * Context listeners.
    */
    private ContextAdapter adapter = new ContextAdapter();

    private NavigatorModel model;

    //======================================================================
    // constructor
    //======================================================================

   /**
    * Creation of a new NavigatorPanel.
    * @param entity the entity that this table is presenting
    * @param role a label designating the view that this table presents of the entity
    */
    public NavigatorPanel( Entity entity, String role )
    {
        this( entity, role, false );
    }

   /**
    * Creation of a new NavigatorPanel.
    * @param entity the entity that this table is presenting
    * @param role a label designating the view that this table presents of the entity
    * @param root true if the root node should be made visible
    */
    public NavigatorPanel( Entity entity, String role, boolean root )
    {
        super( new NavigatorModel( entity ) );
        try
	  {
	      this.role = role;
            this.entity = entity;
	      this.model = (NavigatorModel) getModel();
	      getModel().addTreeModelListener( this );
	      getSelectionModel().setSelectionMode( TreeSelectionModel.SINGLE_TREE_SELECTION );
	      setShowsRootHandles( true );
		expandPath( new TreePath( model.getRoot() ));
		setRootVisible( root );
		setScrollsOnExpand( true );
		setCellRenderer( new NavigatorCellRenderer() );
            putClientProperty("JTree.lineStyle","Angled");
		addTreeSelectionListener( this );
            setBorder( emptyBorder );
		setRowHeight( 22 );
            if( getRowCount() > 0 ) setSelectionRow(0);
        }
	  catch( Throwable e )
	  {
		e.printStackTrace( );
	  }
    }

    //======================================================================
    // Panel
    //======================================================================

   /**
    * Return the name of the role that this panel represents
    * relative to its primary entity.
    * @return String the name of the role that this panel presents
    */
    public String getRole()
    {
        return role;
    }

   /**
    * Return the base entity.
    *
    * @return Entity the entity that this item represents.
    */
    public Entity getEntity( )
    {
        return entity;
    }

   /**
    * Returns a possibly null value corresponding to an entity that is 
    * currently selected.
    * @return Entity the entity currently in focus or null if no entity
    */
    public Entity getDefaultEntity()
    {
	  TreePath path = getSelectionPath();
	  if( path == null ) return null;
        Object last = path.getLastPathComponent();
	  if( last == null ) return null;
	  return (Entity) ((Node)last).getUserObject();
    }

    //============================================================================
    // TreeSelectionListener
    //============================================================================

    public void valueChanged( TreeSelectionEvent event )
    {
        adapter.fireContextEvent( 
          new ContextEvent( this, event, ContextEvent.CONTEXT_GAINED ));
    }

    //============================================================================
    // TreeModelListener
    //============================================================================

    public void treeNodesChanged( TreeModelEvent event )
    {
        validate();
    }

    public void treeNodesInserted( TreeModelEvent event )
    {
        if( getSelectionCount() == 0 ) if( getRowCount() > 0 ) setSelectionRow(0);
        validate();
    }

    public void treeNodesRemoved( TreeModelEvent event )
    {
        validate();
    }

    public void treeStructureChanged( TreeModelEvent event )
    {
        validate();
    }

    //============================================================================
    // ContextHandler
    //============================================================================

   /**
    * Adds a <code>ContextListener</code>.
    */
    public void addContextListener( ContextListener listener )
    {
        adapter.addContextListener( listener );
    }

   /**
    * Removes a <code>ContextListener</code>.
    */
    public void removeContextListener( ContextListener listener )
    {
        adapter.removeContextListener( listener );
    }
}
