/*
 * @(#)DesktopTablePanel.java
 *
 * Copyright 2000 OSM SARL. All Rights Reserved.
 * 
 * This software is the proprietary information of OSM SARL.  
 * Use is subject to license terms.
 * 
 * @author  Stephen McConnell
 * @version 1.0 24/06/2001
 */

package net.osm.agent;

import javax.swing.table.TableModel;
import javax.swing.table.TableColumnModel;

import net.osm.shell.TablePanel;
import net.osm.shell.Entity;


/**
 * A panel presenting the <code>AbstractResources</code> contained by a <code>Desktop</code> 
 * in the form of a table.
 */
public class DesktopTablePanel extends ContainsTablePanel 
{

    //======================================================================
    // state
    //======================================================================

   /**
    * Local reference to the user that owns the desktop.
    */
    private UserAgent user;

    //======================================================================
    // constructor
    //======================================================================

   /**
    * Creation of a new DesktopTablePanel.
    */
    public DesktopTablePanel( UserAgent user, DesktopAgent desktop, String role, TableModel data, TableColumnModel columns )
    {
	  super( desktop, role, data, columns );
        this.user = user;
    }

    //======================================================================
    // Panel
    //======================================================================

    public Entity getEntity()
    {
        return user;
    }
}
