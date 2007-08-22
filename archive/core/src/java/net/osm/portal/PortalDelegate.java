/*
 * @(#)PortalDelegate.java
 *
 * Copyright 2000 OSM S.A.R.L. All Rights Reserved.
 * 
 * This software is the proprietary information of OSM S.A.R.L.  
 * Use is subject to license terms.
 * 
 * @author  Stephen McConnell
 * @version 1.0 29/07/2000
 */

package net.osm.portal;

import net.osm.discovery.PortalOperations;

public class PortalDelegate extends DirectoryDelegate implements PortalOperations
{

    //===================================================
    // constructor
    //===================================================

   /**
    * Creation of a new <code>PortalDelegate</code>.
    */
    public PortalDelegate(  ) 
    {
	  super();
    }
}
