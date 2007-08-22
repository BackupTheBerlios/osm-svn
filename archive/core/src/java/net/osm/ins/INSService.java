/*
 * @(#)INSService.java
 *
 * Copyright 2000 OSM SARL. All Rights Reserved.
 * 
 * This software is the proprietary information of OSM SARL.  
 * Use is subject to license terms.
 * 
 * @author  Stephen McConnell
 * @version 1.0 26/03/2001
 */

package net.osm.ins;

import org.omg.CosNaming.NamingContext;



/**
 * Avalon service interface that provides access to the 
 * the root of an InteroperableNamingService.
 *
 * @author Stephen McConnell <mcconnell@osm.net>
 */

public interface INSService
{

   /**
    * Returns a reference to an Interoperable Naming Service.
    */
    NamingContext getRootNamingContext( );

}



