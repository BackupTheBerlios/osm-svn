/*
 * EditMenu.java
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

import javax.swing.JMenu;
import javax.swing.JMenuItem;
import java.awt.event.KeyEvent;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.Action;


/**
 * Implementation of the Edit menu including access points for 
 * the registration of edit related actions including cut, copy, 
 * paste, paste-special, rename, select-all, delete and find.
 */
final class EditMenu extends JMenu
{

    private MenuItem undoMenuItem = new MenuItem( "Undo", false, KeyEvent.VK_Z );
    private MenuItem redoMenuItem = new MenuItem( "Redo", false, KeyEvent.VK_Y );
    private MenuItem clearMenuItem = new MenuItem( "Delete", false );
    private MenuItem cutMenuItem = new MenuItem( "Cut", false, KeyEvent.VK_X );
    private MenuItem copyMenuItem = new MenuItem( "Copy", false, KeyEvent.VK_C );
    private MenuItem pasteMenuItem = new MenuItem( "Paste", false, KeyEvent.VK_V );
    private MenuItem pasteSpecialMenuItem = new MenuItem( "Paste Special ...", false );
    private MenuItem selectAllMenuItem = new MenuItem( "Select All", false, KeyEvent.VK_A );
    private MenuItem findMenuItem = new MenuItem( "Find ...", false, KeyEvent.VK_F );
    private MenuItem renameMenuItem = new MenuItem( "Rename ...", false, KeyEvent.VK_R );

   /**
    * Creation of a new edit menu.
    * @param label the label to use (e.g. 'Edit')
    * @param key the keyboard code to activate the menu
    * @param cut the cut action handler
    * @param copy the copy action handler
    * @param paste the paste action handler
    * @param special the paste-special action handler
    */
    public EditMenu( String label, int key, Action cut, Action copy, Action paste, Action special, Action clear, Action rename )
    {
	  super( label );
        if( key > -1 ) setMnemonic( key );
        setFont( MGR.font );

	  add( undoMenuItem );
	  add( redoMenuItem );

        addSeparator();

  	  add( selectAllMenuItem );
	  add( renameMenuItem  );

        addSeparator();

	  add( cutMenuItem );
	  add( copyMenuItem );
	  add( pasteMenuItem  );
	  add( pasteSpecialMenuItem  );

        addSeparator();

  	  add( clearMenuItem );
        addSeparator();

  	  add( findMenuItem );

	  cutMenuItem.setAction( cut );
	  copyMenuItem.setAction( copy );
 	  pasteMenuItem.setAction( paste );
 	  pasteSpecialMenuItem.setAction( special );
        clearMenuItem.setAction( clear );
        renameMenuItem.setAction( rename );
    }
}

