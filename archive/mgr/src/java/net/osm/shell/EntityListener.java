/*
 * @(#)EntityListener.java
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
 * A <code>ListListener</code> listens to <code>ListEvent</code> events 
 * containing a focus event raised by a panel or item.
 */

public interface EntityListener
{

   /**
    * Method invoked when an an entity is dosposed of.
    */
    public void notifyEntityDisposal( EntityEvent event );

}
