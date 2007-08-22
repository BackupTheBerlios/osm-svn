/*
 * ResourceMenu.java
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

import java.util.LinkedList;
import java.awt.event.KeyEvent;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.Action;

import net.osm.util.ExceptionHelper;

/**
 * The resource menu.
 */

final class ResourceMenu extends JMenu
{

    private MenuItem openMenuItem = new MenuItem( "Open", false, KeyEvent.VK_O  );
    private Menu preferencesMenu = new PreferencesMenu( "Preferences", KeyEvent.VK_UNDEFINED );
    private MenuItem exitMenuItem = new MenuItem( "Exit", KeyEvent.VK_UNDEFINED );

    private MenuItem removeMenuItem = new MenuItem( "Remove", false, KeyEvent.VK_DELETE );

    public ResourceMenu( String label, int key, final Desktop desktop, Action properties, Action model )
    {
	  super( label );
        if( key > -1 ) setMnemonic( key );
        setFont( MGR.font );

	  add( openMenuItem );
        addSeparator();

	  add( new MenuItem( properties, KeyEvent.VK_I ) );
	  add( new MenuItem( model, KeyEvent.VK_M ) );
        addSeparator();

	  add( removeMenuItem );
        addSeparator();

	  add( preferencesMenu );
        addSeparator();

	  add( exitMenuItem );

        exitMenuItem.addActionListener(
            new java.awt.event.ActionListener() 
            {
                public void actionPerformed(ActionEvent evt) 
                {
			  desktop.stop();
                }
            }
        );
    }

    public void setPanel( Panel panel )
    {
	  preferencesMenu.setPanel( panel );
        setRemoveAction( panel.getDefaultEntity() );
    }

   /**
    * Clear the current selection.
    */
    public void setRemoveAction( Entity entity )
    {
        if( entity != null )
	  {
	      if( entity.removable() )
		{
		    Action action = entity.getRemoveAction();
		    removeMenuItem.setAction( action );
		    return;
		}
	  }
        removeMenuItem.setAction( null );
	  removeMenuItem.setText("Remove");
        removeMenuItem.setEnabled( false );
    }

}

