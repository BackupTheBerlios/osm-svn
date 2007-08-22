/*
 * @(#)Menu.java
 *
 * Copyright 2000 OSM SARL. All Rights Reserved.
 * 
 * This software is the proprietary information of OSM SARL.  
 * Use is subject to license terms.
 * 
 * @author  Stephen McConnell
 * @version 1.0 6/02/2001
 */

package net.osm.shell;

import java.awt.event.KeyEvent;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.Action;

import net.osm.util.ExceptionHelper;


/**
 * The main desktop menau bar.
 */
final class MenuBar extends JMenuBar
{

    //====================================================
    // static 
    //====================================================

    public static final int CREATE_MENU_GROUP = 0;
    public static final int PREFERENCES_MENU_GROUP = 1;
    public static final int VIEWS_MENU_GROUP = 2;
    public static final int TOOLS_MENU_GROUP = 4;
    public static final int HELP_MENU_GROUP = 5;

    public static final int MODEL_ACTION = 19;
    public static final int PROPERTIES_ACTION = 20;

    //====================================================
    // state 
    //====================================================

    private final ViewMenu viewMenu = new ViewMenu( "Views", KeyEvent.VK_F5 );
    private final ActionsMenu actionsMenu = new ActionsMenu( "Actions", KeyEvent.VK_F6 );
    private final ToolsMenu toolsMenu = new ToolsMenu( "Tools", KeyEvent.VK_F7 );
    private final HelpMenu helpMenu = new HelpMenu( "Help", KeyEvent.VK_8 );

    private Entity entity;
    private Panel panel;
    
    //====================================================
    // constructor 
    //====================================================

    public MenuBar( ResourceMenu resource, EditMenu edit )
    {
        super();
        try
        {
            add( resource );
            add( edit );
            add( viewMenu );
            add( actionsMenu );
            add( toolsMenu );
            add( helpMenu );
        }
        catch( Exception e )
        {
            throw new RuntimeException("Unexpected exception while creating menubar.", e );
        }
    }

    //====================================================
    // MenuBar 
    //====================================================

    public synchronized void setPanel( )
    {
       setPanel( null );
    }

    public synchronized void setPanel( Panel panel )
    {
        this.panel = panel;
        if( this.panel == null )
        {
            clearMenus();
        }
	  else
	  {
		actionsMenu.setPanel( panel );
		helpMenu.setPanel( panel );
        }
    }

    private synchronized void clearMenus()
    {
        actionsMenu.setPanel( null );
    }

   /**
    * Add an action to the tools menu.
    */
    public void addTool( Action action )
    {
        toolsMenu.addTool( action );
    }

}

