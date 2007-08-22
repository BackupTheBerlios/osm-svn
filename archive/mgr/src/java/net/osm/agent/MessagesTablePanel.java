/*
 * @(#)MessagesTablePanel.java
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

import org.omg.Session.User;
import org.omg.Session.UserHelper;

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

public class MessagesTablePanel extends TablePanel
{

    //======================================================================
    // state
    //======================================================================

   /**
    * The primary user.
    */
    private UserAgent user;

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
    * Creation of a new MessagesTablePanel.
    * @param entity the workspace that this table is presenting
    * @param role a label designating the view that this table presents of the entity
    * @param data a table model containing the data to layout in view
    * @param columns the table column model to apply
    */
    public MessagesTablePanel( UserAgent entity, String role, TableModel data, TableColumnModel columns )
    {
        super( entity, role, data, columns );
        this.data = data;
	  user = entity;
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
        actions = super.getActions();
        return actions;
    }


    //======================================================================
    // ClipboardHandler
    //======================================================================

   /**
    * Method invoked by the shell to determine if the current selection
    * within the panel can be cleared.
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
        // dequeue all of the selected entities
        //

        if( !( data instanceof EntityTable ) ) return false;
        synchronized( data )
	  {
		int[] rows = getSelectedRows();
   		for( int i=rows.length-1; i>-1; i--)
	  	{
		    final MessageAgent message = (MessageAgent) ((EntityTable)data).getEntityAtRow( rows[i] );
		    user.getUser().dequeue( message.getIdentifier() );
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
	  return false;
    }

   /**
    * Request to a handler to return an array of Entity instances to be placed
    * on the clipboard.
    * @return Object[] array of cut entities
    * @see #canCut
    */
    public Object[] handleCut()
    {
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
        return false;
    }

   /**
    * Request to a handler to return an array of Entity instances to be placed
    * on the clipboard in response to a user copy request.
    * @return Object[] array of cut entities
    * @see #canCopy
    */
    public Object[] handleCopy()
    {
        return new Object[0];
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
	  return false;
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
