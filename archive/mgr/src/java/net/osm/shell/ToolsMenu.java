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
 * The tools menu.
 */

final class ToolsMenu extends JMenu
{

    protected ToolsMenu( String label, int key )
    {
	  super( label );
        if( key > -1 ) setMnemonic( key );
        setFont( MGR.font );
	  setEnabled( false );
    }

   /**
    * Adds a static action to the Actions menu.
    */
    protected void addTool( Action action )
    {
	  if( action == null ) return;
        MenuItem item = new MenuItem();
        item.setAction( action );
        add( item );
	  setEnabled( true );
    }
}

