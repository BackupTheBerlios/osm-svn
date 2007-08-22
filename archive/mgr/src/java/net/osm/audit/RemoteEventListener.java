/*
 * @(#)RemoteEventListener.java
 *
 * Copyright 2000 OSM SARL. All Rights Reserved.
 * 
 * This software is the proprietary information of OSM SARL.  
 * Use is subject to license terms.
 * 
 * @author  Stephen McConnell
 * @version 1.0 6/02/2001
 */

package net.osm.audit;

/**
 * <code>RemoteEventListener</code> is a interface that must be supported by 
 * an object that wish to recieve notification of <code>RemoteEvent</code>
 * events.
 */

public interface RemoteEventListener
{

   /**
    * Method invoked when an an event has been received from a 
    * remote source signalling a state change in the source
    * object.
    */
    public void remoteChange( RemoteEvent event );

}
