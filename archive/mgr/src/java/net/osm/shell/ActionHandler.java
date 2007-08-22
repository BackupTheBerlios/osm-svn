/*
 * @(#)ActionHandler.java
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

import java.util.List;

/**
 * Interface through which the Shell can access context related actions.
 */

public interface ActionHandler
{

   /**
    * Returns a list of Action instances to be installed as 
    * action menu items within the desktop when the panel 
    * is in focus and the selection is empty.
    * @return List a list of <code>Action</code instances
    */
    public List getActions( );

}
