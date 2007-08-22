/* 
 * Copyright 2000 OSM SARL. All Rights Reserved.
 * 
 * This software is the proprietary information of OSM SARL.  
 * Use is subject to license terms.
 * 
 * @author  Stephen McConnell
 * @version 1.0 6/02/2001
 */

package net.osm.session.processor;

/**
 * An <code>ApplianceHandler</code> is an object capable of handling requests for
 * the addition and removal of <code>ApplianceListener</code> listeners.
 */
public interface ApplianceHandler
{

   /**
    * Adds an <code>ApplianceListener</code> to an <code>Appliance</code>.
    */
    public void addApplianceListener( ApplianceListener listener );

   /**
    * Removes an <code>ApplianceListener</code> from an <code>Appliance</code>.
    */
    public void removeApplianceListener( ApplianceListener listener );

}
