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

import net.osm.shell.Panel;
import net.osm.shell.Entity;
import net.osm.util.ExceptionHelper;


/**
 * The resource menu.
 */

final class ActionsMenu extends Menu
{

    public ActionsMenu( String label, int key )
    {
	  super( label, key );
    }

    public void setPanel( Panel panel )
    {
	  try
	  {
            clearActions();
            if( panel.getDefaultEntity() != null )
		{
		    setActions( panel.getDefaultEntity().getActions( ) );
		}
		else if( panel.getSelectionCount() == 0 )
		{
		    setActions( panel.getEntity().getActions( ) );		
		}
	  }
	  catch( Exception e )
	  {
		ExceptionHelper.printException("ActionMenu, Failed set panel: " + panel, e  );
	  }
    }
}

