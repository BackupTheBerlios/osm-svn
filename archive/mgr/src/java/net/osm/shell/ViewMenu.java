/*
 * ViewMenu.java
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
import javax.swing.ButtonGroup;
import java.awt.event.KeyEvent;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

import net.osm.shell.Entity;
import net.osm.shell.Panel;

/**
 * The view menu.
 */
final class ViewMenu extends Menu
{

    private RadioButtonMenuItem largeMenuItem = 
      new RadioButtonMenuItem( "Large Icons", false, KeyEvent.VK_UNDEFINED );
    private RadioButtonMenuItem smallMenuItem = 
      new RadioButtonMenuItem( "Small Icons", false, KeyEvent.VK_UNDEFINED);
    private RadioButtonMenuItem listMenuItem = 
      new RadioButtonMenuItem( "List", true, KeyEvent.VK_UNDEFINED);
    private ButtonGroup group = new ButtonGroup();

    public ViewMenu( String label, int key )
    {
	  super( label, key );
        listMenuItem.setSelected( true );
        group.add( largeMenuItem );
        group.add( smallMenuItem );
        group.add( listMenuItem );
	  add( largeMenuItem );
	  add( smallMenuItem);
        addSeparator();
	  add( listMenuItem );
    }

    public void setPanel( Panel panel )
    {
        clearActions();
    }


}

