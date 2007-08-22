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

public class INSContextBase implements INSContext
{

    // ============================================
    // State members
    // ============================================

    NCStorage root;

    // ============================================
    // Constructor
    // ============================================

    public INSContextBase( NCStorage root )
    {
	 this.root = root;
    }

    // ============================================
    // InteroperableNamingServiceContext
    // ============================================

   /**
    * Returns the PSS NamingContext instance that is the root of the 
    * naming tree.
    */

    public NCStorage getRootNamingContextStore( )
    {
	  return this.root;
    }

    // ============================================
    // Implementation of Context interface
    // ============================================

    public Object get( Object key )
    {
	  String k = "non-string-value";
	  try
	  {
		k = (String) key;
		if( k.equals("ins")) 
		{
		    return (INSContext) this;
		}
		return null;
	  }
	  catch( Exception e )
	  {
		return null;
	  }
    }

}



