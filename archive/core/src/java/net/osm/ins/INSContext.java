/*
 * @(#)InteroperableNamingServiceContext.java
 *
 * Copyright 2000 OSM S.A.R.L. All Rights Reserved.
 * 
 * This software is the proprietary information of OSM S.A.R.L.  
 * Use is subject to license terms.
 * 
 * @author  Stephen McConnell
 * @version 1.0 19/03/2001
 */

package net.osm.ins;


import org.apache.avalon.framework.context.Context;

import net.osm.ins.pss.NCStorage;



/**
 *
 * @author Stephen McConnell <mcconnell@osm.net>
 */

public interface INSContext extends Context
{

   /**
    * Returns the PSS NamingContextStorage instance that is the root of the 
    * naming tree.
    */

    public NCStorage getRootNamingContextStore( );

}



