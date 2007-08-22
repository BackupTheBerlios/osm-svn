/*
 * PreferencesMenu.java
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

import net.osm.shell.MGR;
import net.osm.shell.Panel;
import net.osm.shell.Entity;

/**
 * The resource menu.
 */

class PreferencesMenu extends Menu
{

    private MenuItem generalPreferencesMenuItem = new MenuItem( "General Preferences ...", false );

    public PreferencesMenu( String label, int key )
    {
	  super( label, key );
	  add( generalPreferencesMenuItem );
    }

   /**
    * To be removed.  
    * Preference menu items should be declared at service installation time.
    * Implementation does nothing.
    */
    public void setPanel( Panel panel )
    {
	  // not implemented
    }

}

