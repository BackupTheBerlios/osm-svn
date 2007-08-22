/*
 * @(#)View.java
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

import java.awt.Color;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;

import net.osm.shell.Panel;
import net.osm.shell.ContextHandler;

/**
 * A View is an container of one or more panels.
 */

public interface View extends ContextHandler
{

    public static final Border emptyBorder = new LineBorder( Color.lightGray, 2 );
    public static final Border focusBorder = new LineBorder( Color.gray, 2 );

   /**
    * Get the panel in this view.
    *
    * @return Panel the panel from the view.
    */
    public Panel getPanel( );

   /**
    * Sets the visible adorments of the view to reflect active status.
    */
    public void setVisibleFocus( boolean value );

}
