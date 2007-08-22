/*
 * @(#)Incrementor.java
 *
 * Copyright 2000 OSM SARL. All Rights Reserved.
 * 
 * This software is the proprietary information of OSM SARL.  
 * Use is subject to license terms.
 * 
 * @author  Stephen McConnell
 * @version 1.0 24/02/2001
 */

package net.osm.util;

import java.util.Date;
import java.util.Hashtable;


/**
 * Utility supporting the generation in unique incrementor values.
 */

public class Incrementor {
    
    private static final Hashtable incrementors = new Hashtable();

   /**
    * Returns an incrementor instance matching the supplied name.  If 
    * no existing incrementor matching the supplied name is found, a 
    * new incrementor will be created and returned.
    */

    public static Incrementor create( String name )
    {
	  Incrementor inc = (Incrementor) incrementors.get( name );
	  if( null == inc )
	  {
	      inc = new Incrementor();
	   	incrementors.put( name, inc );
	  }
	  return inc;
    }

    private final long time;
    private long count;

    private Incrementor( )
    {
	  count = 0;
	  time = new Date().getTime();
    }

    public synchronized long increment()
    {
	  synchronized( this ) 
	  {
	      count++;
        }
	  return time + count;
    }
}
