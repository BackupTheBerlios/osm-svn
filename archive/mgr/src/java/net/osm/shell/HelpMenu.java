/*
 * HelpMenu.java
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

import net.osm.shell.Panel;
import net.osm.shell.Entity;
import net.osm.shell.MGR;
import net.osm.shell.Shell;

/**
 * The resource menu.
 */

final class HelpMenu extends Menu
{

    private MenuItem topicMenuItem = new MenuItem( "Help Topics", false );
    private MenuItem aboutMenuItem = new MenuItem( "About OSM", false );
    private MenuItem planetMenuItem = new MenuItem( "About osm.planet", false );

   /**
    * Creation of a new Help Menu.
    */
    public HelpMenu( String label, int key )
    {
	  super( label, key );
	  add( topicMenuItem );
        addSeparator();
	  add( aboutMenuItem );
	  add( planetMenuItem );
    }

   /**
    * Suppliments the configuration of the help menu based on the current panel.
    * @osm.net not implemented
    */
    public void setPanel( Panel panel )
    {
        // not implemented
    }
}

