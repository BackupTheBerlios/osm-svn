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

package net.osm.shell;

import java.util.List;
import java.util.LinkedList;
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
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;


/**
 * A panel presenting entities in the form of a table.
 */

public class TablePanel extends JTable implements Panel, ClipboardHandler, ActionHandler, ListSelectionListener, FocusListener
{

    //======================================================================
    // static
    //======================================================================

    private static final boolean trace = false;

    //======================================================================
    // state
    //======================================================================

    private Entity entity;

   /**
    * Context listeners.
    */
    private ContextAdapter adapter = new ContextAdapter();

   /**
    * Focus adapter.
    */
    private FocusAdapter focusAdapter;

   /**
    * Role that this view plays relative to the entity.
    */
    private String role;

   /**
    * The currently selected item if only 1 instance is selected.
    */
    private Entity selection;

   /**
    * The table data model.
    */
    private TableModel data;

    private boolean readonly;

    private static final Color enabledColor = new Color( 204,204,255 );
    private static final Color disabledColor = new Color( 228,228,255 );

    private final List actions = new LinkedList();


    //======================================================================
    // constructor
    //======================================================================

   /**
    * Creation of a new TablePanel.
    * @param entity the entity that this table is presenting
    * @param role a label designating the view that this table presents of the entity
    * @param data a table model containing the data to layout in view
    * @param columns the table column model to apply
    */
    public TablePanel( Entity entity, String role, TableModel data, TableColumnModel columns )
    {
        this( entity, role, data, columns, false );
    }

   /**
    * Creation of a new TablePanel.
    * @param entity the entity that this table is presenting
    * @param role a label designating the view that this table presents of the entity
    * @param data a table model containing the data to layout in view
    * @param columns the table column model to apply
    * @param readonly indicates that content may no be cut or cleared within the panel
    */
    public TablePanel( Entity entity, String role, TableModel data, TableColumnModel columns, boolean readonly )
    {
	  super( data, columns );

	  if( entity == null ) throw new RuntimeException(
	    "TablePanel. Illegal null entity constructor argument.");
	  if( role == null ) throw new RuntimeException(
	    "TablePanel. Illegal null role constructor argument.");

        this.readonly = readonly;
        this.data = data;
        this.entity = entity;
        this.role = role;

        setShowGrid( false );
        setRowHeight(22);
        getTableHeader().setReorderingAllowed( false );
        focusAdapter = new FocusAdapter( this, adapter );

        MouseListener popupListener = new PopupListener();
        addMouseListener(popupListener);
        addFocusListener( this );

    }

    //======================================================================
    // ListSelectionListener
    //======================================================================

   /**
    * Listens to changes in the selected state of the table and 
    * propergates a <code>ContextEvent</code> referencing this table as 
    * the event's panel when the table selection changes.
    * @param event a list selection event
    */
    public void valueChanged( ListSelectionEvent event )
    {
	  super.valueChanged( event );
	  if( !event.getValueIsAdjusting() ) 
	  {
            ListSelectionModel model = getSelectionModel();
		synchronized( model )
		{
		    int n = model.getMinSelectionIndex();
		    if( n == model.getMaxSelectionIndex()) if( n > -1 ) 
		    {
			  // we have a single selection
			  selection = (Entity) data.getValueAt( n, -1 );
			  adapter.fireContextEvent( 
                      new ContextEvent( this, event, ContextEvent.CONTEXT_GAINED ) );
		    }
		    else if( selection != null ) 
		    {
			  selection = null;
			  adapter.fireContextEvent( 
                      new ContextEvent( this, event, ContextEvent.CONTEXT_GAINED ) );
	          }
		}
        }
    }

    //======================================================================
    // FocusListener
    //======================================================================

    public void focusGained( FocusEvent event )
    {
        setSelectionBackground( enabledColor );
    }


    public void focusLost( FocusEvent event )
    {
        setSelectionBackground( disabledColor );
    }

    //======================================================================
    // ClipboardHandler
    //======================================================================

   /**
    * Method invoked by the shell to determine if the current selection
    * within the panel can be cleared (that is to say that the relationships 
    * defining liks between the primary entity and the exposed entities 
    * can be deleted.
    * @return boolean - true if the panel can delete the selected links
    * @see #handleDelete
    */
    public boolean canDelete()
    {
        return ( !getSelectionModel().isSelectionEmpty() && !readonly && (data instanceof EntityTable ) );
    }

   /**
    * Request to a handler to process link deletion based on the current selection.
    * @return boolean true if the deletion action was completed sucessfully
    * @see #canDelete
    */
    public boolean handleDelete()
    {
        return false;
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
        return ( !getSelectionModel().isSelectionEmpty() && !readonly );
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
        // enable pasting if the selection is empty and the panel 
        // is not readonly

        return ( getSelectionModel().isSelectionEmpty() && !readonly );
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

    //======================================================================
    // ActionHandler
    //======================================================================

   /**
    * Returns a list of Action instances to be installed as 
    * action menu items within the desktop when the panel has 
    * focus.
    * @return List the list of actions exposed by the panel
    */
    public List getActions( )
    {
        return actions;
    }


    //======================================================================
    // Panel
    //======================================================================

   /**
    * Returns the number of entries in the current selection.
    * @return int the number of entries in the current selection
    */
    public int getSelectionCount()
    {
        return getSelectedRows().length;
    }

   /**
    * Clears any current selection.
    */
    public void clearSelection()
    {
        getSelectionModel().clearSelection();
    }

   /**
    * Returns a possibly null value corresponding to an entity that is 
    * currently selected.
    * @return Entity the entity currently in focus
    */
    public Entity getDefaultEntity()
    {
        ListSelectionModel model = getSelectionModel();
	  synchronized( model )
	  {
	      if( getSelectionCount() != 1 ) return null;
	      int n = model.getMaxSelectionIndex();
		return (Entity) data.getValueAt( n, -1 );
	  }
    }

   /**
    * Return the name of the role that this panel is presenting.
    */
    public String getRole()
    {
        return role;
    }

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

    public String getName()
    {
        return role + " in: '" + entity.getName();
    }

    public Entity getEntity()
    {
        return entity;
    }

    protected void handleMouseEvent( MouseEvent event )
    {
        if( event.isPopupTrigger() )
	  {
		int j = rowAtPoint( event.getPoint() );
            if( j > -1 ) 
		{
		    if( !isRowSelected( j ) ) 
		    {
		        clearSelection();
			  setRowSelectionInterval( j, j );
		    }
		    adapter.fireContextEvent( 
		       new ContextEvent( this, event, ContextEvent.CONTEXT_GAINED ) );
		}
		else
		{
		    clearSelection();
		}
	  }
        else if (SwingUtilities.isLeftMouseButton(event) && event.getClickCount() == 2) 
	  {
		int j = rowAtPoint( event.getPoint() );
            if( j > -1 ) 
		{
		    if( !isRowSelected( j ) ) 
		    {
		        clearSelection();
			  setRowSelectionInterval( j, j );
		    }
		    adapter.fireContextEvent( 
		       new ContextEvent( this, event, ContextEvent.CONTEXT_GAINED ) );
		}
		else
		{
		    clearSelection();
		}
        }
    }

    class PopupListener extends MouseAdapter 
    {
        public void mousePressed(MouseEvent e) {
            handleMouseEvent( e );
        }

        public void mouseReleased(MouseEvent e) {
            handleMouseEvent( e );
        }
    }
}
