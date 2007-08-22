/*
 * @(#)ContextListener.java
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

/**
 * A <code>ContextListener</code> listens to <code>ContextEvent</code> events 
 * containing a focus event raised by a panel or item.
 */

public interface ContextListener
{

   /**
    * Method invoked when a selection is established.  
    */
    public void contextGained( ContextEvent event );

   /**
    * Method invoked when the selection changes.  
    */
    public void contextLost( ContextEvent event );

}
