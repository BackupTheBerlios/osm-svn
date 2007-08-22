/*
 * PopupMenu.java
 *
 * Copyright 2000 OSM SARL. All Rights Reserved.
 * 
 * This software is the proprietary information of OSM SARL.  
 * Use is subject to license terms.
 * 
 * @author  Stephen McConnell
 * @version 1.0 19/08/2001
 */

package net.osm.shell;

import java.util.List;
import java.util.Iterator;
import java.awt.event.MouseEvent;
import java.awt.event.ActionEvent;
import javax.swing.JPopupMenu;
import javax.swing.Action;
import javax.swing.AbstractAction;
import javax.swing.JTable;

/**
 * The create menu.
 */

final class PopupMenu extends JPopupMenu
{

    private Desktop desktop;

    private CreateMenu create;
    private Action cut;
    private Action copy;
    private Action paste;
    private Action special;
    private Action clear;
    private RenameAction rename;
    private Action props;

    private ClipboardHandler handler;
    private Panel panel;

   /**
    * Creation of a new PopupMenu.
    */
    public PopupMenu( Desktop desktop )
    {
	  super( );
        setFont( desktop.getDefaultFont() );
        setEnabled( false );
        this.desktop = desktop;

        cut = new GenericAction( "Cut", this, "handleCut", false );
        copy = new GenericAction( "Copy", this, "handleCopy", false );
        paste = new GenericAction( "Paste",  this, "handlePaste", false );
        special = new GenericAction( "Paste Special ...", 
	    this, "handlePasteSpecial", false );
        clear = new GenericAction( "Delete", this, "handleDelete", false );
        rename = new RenameAction( "Rename ...", false, -1 );
        create = new CreateMenu("Create");
        props = new AbstractAction( "Properties..." )
        {
            public void actionPerformed( ActionEvent event )
		{
		    handleProperties();
		}
        };

	  add( new MenuItem( props ));
        addSeparator();
	  add( new MenuItem( rename ));
        addSeparator();
	  add( create );
        addSeparator();
	  add( new MenuItem( cut ));
	  add( new MenuItem( copy ));
	  add( new MenuItem( paste ));
	  add( new MenuItem( special ));
        addSeparator();
	  add( new MenuItem( clear ));
    }

    public void setPanel( Panel panel, MouseEvent me )
    {
        this.panel = panel;
	  clearExtraItems();
        create.setPanel( panel );
	  props.setEnabled( panel.getSelectionCount() == 1 );
	  if( panel instanceof ClipboardHandler )
        {
		clipboardChange( (ClipboardHandler) panel );
        }
	  else
	  {
		clipboardChange( null );
	  }
        if( panel instanceof JTable )
	  {
	      JTable table = (JTable) panel;
		if( table.rowAtPoint( me.getPoint() ) > -1 )
		{
		    int j = table.getSelectedRows().length;
		    if( j == 1 )
		    {
		        //
			  // get the entities set of actions
			  //
				    
			  Entity entity = panel.getDefaultEntity();
			  if(entity instanceof ActionHandler)
		        {
			      List list = ((ActionHandler)entity).getActions( );
			      if( list.size() > 0 )
			      {
			  	    addSeparator();
				    Iterator iterator = list.iterator();
				    while( iterator.hasNext() )
				    {
					  add( new MenuItem( (Action) iterator.next() ));
				    }
			      }
		        }
		    }
		    else
		    {
   		        //
			  // ignore this case
			  //
		    }
	      }
		else
		{
		    Entity entity = panel.getEntity();
		    if( entity instanceof ActionHandler )
		    {
		        List list = ((ActionHandler)entity).getActions( );
			  if( list.size() > 0 )
			  {
			      addSeparator();
				Iterator iterator = list.iterator();
				while( iterator.hasNext() )
				{
				    add( new MenuItem( (Action) iterator.next() ));
			      }
			  }
		    }
            }
        }
    }

    protected Panel getPanel()
    {
        return panel;
    }

    private void clearExtraItems()
    {
	  int k = getComponentCount();
        if( k > 12 )
	  {
	      for( int i=(k-1); i>11; i-- )
		{
		    remove( i );
	      }
        }
    }

    public void clipboardChange( ClipboardHandler handler )
    {
        this.handler = handler;
	  if( handler != null )
	  {
		if( desktop.getScrap().length > 0 )
		{
		    paste.setEnabled( handler.canPaste( desktop.getScrap() ));
		    special.setEnabled( handler.canPasteSpecial( desktop.getScrap() ));
            }
		else
		{
		    paste.setEnabled( false );
                special.setEnabled( false );
            }
	      cut.setEnabled( handler.canCut() );
	      copy.setEnabled( handler.canCopy() );
	      clear.setEnabled( handler.canDelete() );
	  }
        else
	  {
		paste.setEnabled( false );
            special.setEnabled( false );
	      cut.setEnabled( false );
	      copy.setEnabled( false );
	      clear.setEnabled( false );
	  }
    }

    //======================================================================
    // ClipboardHandler
    //======================================================================

   /**
    * Request to a handler to to process link deletion based on the current selection.
    * @return boolean true if the deletion action was completed sucessfully
    */
    public void handleDelete()
    {
        if( handler != null ) handler.handleDelete();
    }

   /**
    * Request to a handler to return an array of Entity instances to be placed
    * on the clipboard.
    * @return Object[] array of cut entities
    */
    public void handleCut()
    {
	  if( handler != null ) desktop.putScrap( handler.handleCut() );
    }

   /**
    * Request to a handler to return an array of Entity instances to be placed
    * on the clipboard in response to a user copy request.
    * @return Object[] array of cut entities
    */
    public void handleCopy()
    {
	  if( handler != null ) desktop.putScrap( handler.handleCopy() );
    }

   /**
    * Request issued by the desktop to a panel to handle the pasting of 
    * the current clipboard content into the panel.
    * @param array - the clipboard content
    */
    public void handlePaste( )
    {
        if( handler != null ) handler.handlePaste( desktop.getScrap() );
    }

   /**
    * Request issued by the desktop to a panel to handle the pasting of 
    * the current clipboard content into the panel using the Paste Special
    * context.
    * @param array the clipboard content
    */
    public void handlePasteSpecial( )
    {
        if( handler != null ) handler.handlePasteSpecial( desktop.getScrap() );
    }

    private void handleProperties()
    {
        if( panel == null ) return;
        if( panel.getDefaultEntity() == null ) return;
        PropertiesDialog dialog = new PropertiesDialog( "Properties", panel.getDefaultEntity(), desktop );
	  dialog.setVisible( true );
    }

    class PropertiesAction extends AbstractAction
    {
        PropertiesAction( String name )
	  {
	      super( name );
	  }

        public void actionPerformed( ActionEvent event )
	  {
		handleProperties();
	  }
    }
}

