/*
 * @(#)TablePanel.java
 *
 * Copyright 2000 OSM SARL. All Rights Reserved.
 * 
 * This software is the proprietary information of OSM SARL.  
 * Use is subject to license terms.
 * 
 * @author  Stephen McConnell
 * @version 1.0 24/06/2001
 */

package net.osm.agent;

import java.util.List;
import java.util.LinkedList;
import java.util.Iterator;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.Color;
import javax.swing.JTable;
import javax.swing.table.TableModel;
import javax.swing.table.TableColumnModel;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.ListSelectionModel;
import javax.swing.Action;

import net.osm.shell.ContextEvent;
import net.osm.shell.ContextListener;
import net.osm.shell.Entity;
import net.osm.shell.Panel;
import net.osm.shell.TablePanel;
import net.osm.shell.EntityTable;
import net.osm.shell.GenericAction;


/**
 * A <code>TablePanel</code> presenting the entities contained 
 * by a workspace. 
 */

public class ContainsTablePanel extends TablePanel
{

    //======================================================================
    // state
    //======================================================================

   /**
    * The primary workspace.
    */
    private WorkspaceAgent workspace;

   /**
    * The table data model.
    */
    private TableModel data;

   /**
    * List of actions exposed by this panel.
    */
    private List actions;

    //======================================================================
    // constructor
    //======================================================================

   /**
    * Creation of a new ContainsTablePanel.
    * @param entity the workspace that this table is presenting
    * @param role a label designating the view that this table presents of the entity
    * @param data a table model containing the data to layout in view
    * @param columns the table column model to apply
    */
    public ContainsTablePanel( WorkspaceAgent entity, String role, TableModel data, TableColumnModel columns )
    {
        super( entity, role, data, columns, false );
	  workspace = entity;
        this.data = data;
    }

    //======================================================================
    // ActionHandler
    //======================================================================

   /**
    * Returns a list of Action instances to be installed as 
    * action menu items within the desktop when the entity 
    * is selected.
    */
    public List getActions( )
    {
        if( actions != null ) return actions;

	  //
	  // crerate a set of actions based on the criteria exposed by the factory
	  //

        actions = super.getActions();
        Iterator iterator = workspace.getResourceServices().iterator();
        while( iterator.hasNext() )
        {
            actions.add( (Action) iterator.next() );
        }

	  //
	  // add an action to create a new sub-workspace
	  //

	  actions.add( new GenericAction("Workspace", workspace, "createWorkspace" ) );
        return actions;
    }


    //======================================================================
    // ClipboardHandler
    //======================================================================

   /**
    * Method invoked by the shell to determine if the current selection
    * within the panel can be cleared (that is to say that the relationships 
    * defining liks between the primary entity and the exposed entities 
    * can be deleted).
    * @return boolean - true if the panel can delete the selected links
    * @see #handleDelete
    */
    public boolean canDelete()
    {
        return ( !getSelectionModel().isSelectionEmpty() );
    }

   /**
    * Request to a handler to process link deletion based on the current selection.
    * @return boolean true if the deletion action was completed sucessfully
    * @see #canDelete
    */
    public boolean handleDelete()
    {
        //
        // for all of the selected entities, remove the links to the 
        // primary entity
        //

        if( !( data instanceof EntityTable ) ) return false;
        synchronized( data )
	  {
		int[] rows = getSelectedRows();
   		for( int i=rows.length-1; i>-1; i--)
	  	{
		    final LinkAgent link = (LinkAgent) ((EntityTable)data).getEntityAtRow( rows[i] );
		    workspace.removeResource( (AbstractResourceAgent) link.getTarget() );
		}
	  }
	  return true;
    }

   /**
    * Method invoked by the shell to determine if the current selection
    * within the panel can be trasfered to the clipboard.  If the method
    * returns true the shell will enable the Edit/Cut menu item and on 
    * user selection will invoke the handleCut operation on the handler.
    * @return boolean - true if the panel will accept the candidate object
    * @see #handleCut
    */
    public boolean canCut()
    {
        return ( !getSelectionModel().isSelectionEmpty() );
    }

   /**
    * Request to a handler to return an array of Entity instances to be placed
    * on the clipboard.
    * @return Object[] array of cut entities
    * @see #canCut
    */
    public Object[] handleCut()
    {
        // return the current entity selection and remove the selection
        // from the view

        return new Object[0];
    }

   /**
    * Method invoked by the shell to determine if the current selection
    * within the panel can be duplicated on the clipboard.
    * @return boolean - true if the panel supports copying of the current selection
    * @see #handleCopy
    */
    public boolean canCopy()
    {
        return !getSelectionModel().isSelectionEmpty();
    }

   /**
    * Request to a handler to return an array of Entity instances to be placed
    * on the clipboard in response to a user copy request.
    * @return Object[] array of cut entities
    * @see #canCopy
    */
    public Object[] handleCopy()
    {
        if( !( data instanceof EntityTable ) ) return new Object[0];
        synchronized( data )
	  {
		int[] rows = getSelectedRows();
		Object[] result = new Object[ rows.length ];
   		for( int i=0; i<rows.length; i++ )
	  	{
		    final LinkAgent link = (LinkAgent) ((EntityTable)data).getEntityAtRow( rows[i] );
		    result[i] = link.getTarget();
		}
	      return result;
	  }
    }

   /**
    * Method invoked by the shell to determine if the current clipboard 
    * content is a valid candidate for pasting into this panel.  The 
    * default implementation return false.  Classes derived from this base 
    * class must override this method and the corresponding <code>handlePaste</code>
    * to provide support for specialized cases.
    * @param array - the clipboard content
    * @return boolean - true if the panel will accept the candidate object
    * @see #handlePaste
    */
    public boolean canPaste( Object[] array )
    {
        // enable pasting if the selection is empty 

        if( getSelectionModel().isSelectionEmpty() )
	  {
            for( int i=0; i<array.length; i++ )
            {
		    Object object = array[i];
                if( object instanceof LinkAgent )
	  	    {
			  LinkAgent link = (LinkAgent) object;
		        if( !( link.getTarget() instanceof AbstractResourceAgent ) ) return false;
	  	    }
	  	    else if( !(object instanceof AbstractResourceAgent )) return false;
	      }
		return true;
	  }
        return false;
    }

   /**
    * Request issued by the desktop to a panel to handle the pasting of 
    * the current clipboard content into the panel.
    * @param array - the clipboard content
    * @see #canPaste
    */
    public boolean handlePaste( Object[] array )
    {
        for( int i=0; i<array.length; i++ )	
        {
		handlePaste( array[i] );
	  }
	  return true;
    }

    private void handlePaste( Object object )
    {
        AbstractResourceAgent resource = null;
        if( object instanceof LinkAgent )
	  {
		final LinkAgent link = (LinkAgent) object;
		if( link.getTarget() instanceof AbstractResourceAgent )
		{
		    resource = (AbstractResourceAgent)link.getTarget();
		}
	  }
	  else if( object instanceof AbstractResourceAgent )
	  {
		 resource = (AbstractResourceAgent)object;
	  }
	  if( resource != null ) 
	  {
		workspace.addResource( resource );
	  }
    }


   /**
    * Method invoked by the shell to determine if the current clipboard 
    * content is a valid candidate for pasting into this panel using the 
    * Paste Special case.  The default implementation return false.  
    * Classes derived from this base class must override this method and 
    * the corresponding <code>handlePaste</code> to provide support for 
    * specialized cases.
    * @param array the clipboard content
    * @return boolean - true if the panel will accept the content
    * @see #handlePasteSpecial
    */
    public boolean canPasteSpecial( Object[] array )
    {
        // panel specific
        return false;
    }

   /**
    * Request issued by the desktop to a panel to handle the pasting of 
    * the current clipboard content into the panel using the Paste Special
    * context.
    * @param array the clipboard content
    * @see #canPasteSpecial
    */
    public boolean handlePasteSpecial( Object[] array )
    {
        return false;
    }
}
