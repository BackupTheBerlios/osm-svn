/*
 * @(#)Button.java
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

import java.awt.Dimension;
import java.awt.event.KeyEvent;
import javax.swing.JToolBar;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.SwingConstants;

import net.osm.shell.Entity;
import net.osm.util.ExceptionHelper;
import net.osm.util.IconHelper;


/**
 * A button suitable for addition to the tool bar.
 */

class Button extends JButton
{
    
    //====================================================
    // constructor 
    //====================================================

   /**
    * Creates a new Button associated to the supplied action and 
    * configured with the name and icon from the action, and a rollover 
    * icon from the supplied <code>rollover</code> parameter.
    * @param action the action to assign
    * @param rollover the icon to use as the rollover image
    */
    public Button( Action action, Icon rollover )
    {
        this( action, null, rollover );
    }

   /**
    * Creates a new Button associated to the supplied action and 
    * configured with the name and icon from the action, and a rollover 
    * icon from the supplied <code>rollover</code> parameter.
    * @param action the action to assign
    * @param rollover the icon to use as the rollover image
    */
    public Button( Action action, Icon icon, Icon rollover )
    {
        super( );
        setAction ( action );
        configurePropertiesFromAction( action );
        if( icon != null ) setIcon( icon );
        setHorizontalTextPosition( SwingConstants.CENTER );
        setVerticalTextPosition( SwingConstants.BOTTOM );
        setText( (String) action.getValue("Name"));
        setFont( MGR.font );
        setBorderPainted( false );
        setFocusPainted( false );
        setContentAreaFilled( false );
	  setRolloverEnabled( true );
        setRolloverIcon( rollover );
    }
}

