/*
 * @(#)MenuItem.java
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
import javax.swing.JMenuItem;
import javax.swing.Action;

import net.osm.shell.MGR;
import net.osm.shell.Entity;
import net.osm.shell.Panel;

/**
 * A menu that handles dynamic content modification.
 */

class MenuItem extends JMenuItem
{

    public MenuItem()
    {
        this( "" );
    }

    public MenuItem( String label )
    {
        this( label, true );
    }

    public MenuItem( String label, int key )
    {
        this( label, true, key );
    }

    public MenuItem( String label, boolean enabled )
    {
        this( label, enabled, KeyEvent.VK_UNDEFINED );
    }
     
    public MenuItem( String label, boolean enabled, int key )
    {
	  super( label );
        setFont( MGR.font );
        if( key > -1 ) setMnemonic( key );
        getAccessibleContext().setAccessibleDescription( label + " menu item");
	  setEnabled( enabled );
    }

    public MenuItem( Action action )
    {
        this( action, -1 );
    }

    public MenuItem( Action action, int key )
    {
        super( action );
        setFont( MGR.font );
        if( key > -1 ) setMnemonic( key );
    }

    public void setAction( Action action )
    {
        super.setAction( action );
        if( action != null ) super.configurePropertiesFromAction( action );
    }


}

