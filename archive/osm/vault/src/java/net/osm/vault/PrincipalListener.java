/*
 * PrincipalListener.java
 *
 * Copyright 2000 OSM SARL. All Rights Reserved.
 * 
 * This software is the proprietary information of OSM SARL.  
 * Use is subject to license terms.
 * 
 * @author  Stephen McConnell
 * @version 1.0 22/08/2001
 */

package net.osm.vault;

/**
 * Interface implemeented by objects that can be notified of 
 * of changes in a principal.
 * @see PrincipalEvent
 */

public interface PrincipalListener
{

   /**
    * Notify the listener of a change to the principal.
    * @param event the event containing the new principal
    */
    public void principalChanged( PrincipalEvent event );


}
