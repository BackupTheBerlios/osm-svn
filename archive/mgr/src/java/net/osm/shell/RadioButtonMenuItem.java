/*
 * RadioButtonMenuItem.java
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

import java.awt.event.KeyEvent;
import javax.swing.JRadioButtonMenuItem;

/**
 * A menu that handles dynamic content modification.
 */

class RadioButtonMenuItem extends JRadioButtonMenuItem
{

    public RadioButtonMenuItem()
    {
        this( "" );
    }

    public RadioButtonMenuItem( String label )
    {
        this( label, true );
    }

    public RadioButtonMenuItem( String label, int key )
    {
        this( label, true, key );
    }

    public RadioButtonMenuItem( String label, boolean enabled )
    {
        this( label, true, KeyEvent.VK_UNDEFINED );
    }
     
    public RadioButtonMenuItem( String label, boolean enabled, int key )
    {
	  super( label );
        setFont( MGR.font );
        if( key > -1 ) setMnemonic( key );
        getAccessibleContext().setAccessibleDescription( label + " radiop button menu item");
	  setEnabled( enabled );
    }
}

