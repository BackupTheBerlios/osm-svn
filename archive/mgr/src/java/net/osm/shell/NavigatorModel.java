/*
 * NavigatorModel.java
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
import java.util.Enumeration;
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

import net.osm.util.List;
import net.osm.util.ListHandler;
import net.osm.util.ListListener;
import net.osm.util.ListEvent;


/**
 * A tree model that represents a hierachical element data structure.
 */

public class NavigatorModel extends DefaultTreeModel
{

    private DefaultMutableTreeNode root;

    //======================================================================
    // constructor
    //======================================================================

   /**
    * Creation of a new NavigatorModel based on a supplied list of entities.
    * The implementation will add all elements in the supplied list as nodes
    * within the tree and establish a listener for change to the list.
    *
    * @param entity the root entity
    */
    public NavigatorModel( Entity entity )
    {
	  super( new Node( entity ) );
	  setAsksAllowsChildren( false );
	  Node root = (Node) getRoot();
	  root.setModel( this );
    }

    protected void notifyTreeNodeChanges( Node node, Object[] path, int[] indexes, Object[] values )
    {
        super.fireTreeNodesChanged( node, path, indexes, values );
    }

    protected void notifyTreeNodesInserted( Node node, Object[] path, int[] indexes, Object[] values )
    {
        super.fireTreeNodesInserted( node, path, indexes, values );
    }

    protected void notifyTreeNodesRemoved( Node node, Object[] path, int[] indexes, Object[] values )
    {
        super.fireTreeNodesRemoved( node, path, indexes, values );
    }
}
