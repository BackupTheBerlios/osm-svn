/*
 * @(#)ToolBar.java
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

import java.awt.Insets;
import java.awt.event.KeyEvent;
import javax.swing.JToolBar;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.SwingConstants;



/**
 * Non-floating toolbar with enhancements to support the addition
 * of action based controls.
 */

class ToolBar extends JToolBar
{

    //====================================================
    // constructor 
    //====================================================

    public ToolBar( )
    {
        super();
        setBorderPainted( true );
        setMargin( new Insets( 0,0,0,0 ));
        setFloatable( false );
    }

    //====================================================
    // ToolBar 
    //====================================================

   /**
    * Creates a new Button associated to the supplied action and 
    * configured with the name and icon from the action, and a rollover 
    * icon from the supplied <code>rollover</code> parameter.
    * @param action the action to assign
    * @param rollover the icon to use as the rollover image
    * @osm.note this method is required to bypass a bug in the JToolBar
    *   add operation (rollover icon has to be reset after addition 
    *   of the button into the toolbar).
    */
    public JButton addAction(Action a, Icon rollover ) 
    {
	  return addAction( a, null, rollover );
    }

   /**
    * Creates a new Button associated to the supplied action and 
    * configured with the name, icon, and a rollover 
    * icon from the supplied <code>rollover</code> parameter.
    * @param action the action to assign
    * @param icon the icon to use 
    * @param rollover the rollover image
    * @osm.note this method is required to bypass a bug in the JToolBar
    *   add operation (rollover icon has to be reset after addition 
    *   of the button into the toolbar).
    */
    public JButton addAction(Action a, Icon icon, Icon rollover ) 
    {
	  JButton b = new Button( a, icon, rollover );
        add( b );
        b.setRolloverEnabled( true );
        return b;
    }
}

