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
 * A <code>ApplianceListener</code> listens to <code>ApplianceEvent</code> events.
 */

public interface ApplianceListener
{

   /**
    * Method invoked when the state of an appliance changes.  
    */
    public void stateChanged( ApplianceEvent event );

}
