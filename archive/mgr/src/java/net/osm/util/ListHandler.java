/*
 * @(#)ListHandler.java
 *
 * Copyright 2000 OSM SARL. All Rights Reserved.
 * 
 * This software is the proprietary information of OSM SARL.  
 * Use is subject to license terms.
 * 
 * @author  Stephen McConnell
 * @version 1.0 6/02/2001
 */

package net.osm.util;


/**
 * A ListHandler is an object capable of handling requests for
 * the addition and removal of ListListener listeners.
 */

public interface ListHandler
{

   /**
    * Adds a <code>ListListener</code>.
    */
    public void addListListener( ListListener listener );

   /**
    * Removes a <code>ListListener</code>.
    */
    public void removeListListener( ListListener listener );

}
