/*
 * @(#)ContextHandler.java
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
 * A ContextHandler is an object capable of handling requests for
 * the addition and removal of ContextEvent listeners.
 */

public interface ContextHandler
{

   /**
    * Adds a <code>ContextListener</code>.
    */
    public void addContextListener( ContextListener listener );

   /**
    * Removes a <code>ContextListener</code>.
    */
    public void removeContextListener( ContextListener listener );

}
