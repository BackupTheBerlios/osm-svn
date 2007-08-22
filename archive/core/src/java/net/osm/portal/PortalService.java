/*
 * @(#)PortalService.java
 *
 * Copyright 2000 OSM SARL. All Rights Reserved.
 * 
 * This software is the proprietary information of OSM SARL.  
 * Use is subject to license terms.
 * 
 * @author  Stephen McConnell
 * @version 1.0 19/02/2001
 */

package net.osm.portal;

import org.omg.CORBA.ORB;
import org.omg.PortableServer.POA;
import org.omg.CosTime.TimeService;
import net.osm.discovery.Portal;

/**
 *
 * @author Stephen McConnell <mcconnell@osm.net>
 */

public interface PortalService 
{

   /**
    * Returns the current ORB.
    */

    public ORB getORB( );

   /**
    * Return the main POA used to generate object references.
    */

    public POA getPOA( );

   /**
    * Returns a reference to a TimeService.
    */

    public TimeService getTimeService( );

   /**
    * Returns a reference to a Portal.
    */

    public Portal getPortal( );

}



