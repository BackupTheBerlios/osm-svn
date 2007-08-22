/*
 * @(#)EntityHandler.java
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
 * An EntityHandler is an object capable of handling notification for
 * the disposal of an entity.
 */

public interface EntityHandler
{

   /**
    * Adds a <code>EntityListener</code>.
    */
    public void addEntityListener( EntityListener listener );

   /**
    * Removes a <code>EntityListener</code>.
    */
    public void removeEntityListener( EntityListener listener );
}
