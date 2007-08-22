/*
 * @(#)ListListener.java
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
 * A <code>ListListener</code> listens to <code>ListEvent</code> events 
 * containing a focus event raised by a panel or item.
 */

public interface ListListener
{

   /**
    * Method invoked when an object is added to a list.  
    */
    public void addObject( ListEvent event );

   /**
    * Method invoked when an object is removed from the list.  
    */
    public void removeObject( ListEvent event );

}
