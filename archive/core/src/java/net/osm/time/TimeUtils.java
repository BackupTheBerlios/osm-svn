/*
 * @(#)TimeUtils.java
 *
 * Copyright 2000 OSM SARL. All Rights Reserved.
 * 
 * This software is the proprietary information of OSM SARL.  
 * Use is subject to license terms.
 * 
 * @author  Stephen McConnell
 * @version 1.0 24/02/2001
 */


package net.osm.time;

import org.omg.TimeBase.UtcT;
import org.omg.CosTime.TimeUnavailable;
import org.omg.CosTime.TimeService;

import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Calendar;


/**
 * The TimeUtils class contains a set of static utility methods
 * supporting maniputlation of UTcT values.
 */

public abstract class TimeUtils {
    
    private static final long STDACCURACCY = 10000;
    
    /**
     * Converts a UtcT datastructure to a java.util.Date format.
     */
    
    public static Date convertToDate( UtcT utc ) {
        long t = utc.time;
        return new Date(t);
    }
    
    public static Date convertToDate( long value ) {
        long t = convertToUtcT( value ).time;
        return new Date(t);
    }
    
    public static UtcT convertToUtcT( long value )
    {
        long tmp = STDACCURACCY >> 32;
        return new UtcT
        (
        value,
        (int )( STDACCURACCY & Integer.MAX_VALUE),
        (short )(tmp & Short.MAX_VALUE),
        (short )0
        );
    }
    
    /**
     * Returns the current time in UtcT format based on the current time
     * returned from the local virtual machine.  As such, the current time
     * returned here may be out of sync with the time returned from a
     * TimeService - however, the operation does not involve a server request
     * and as such is usefull when setting UtcT default values in local
     * valuetype constructors.
     */
    
    public static UtcT dateToUtc( Date date ) {
        long tmp = STDACCURACCY >> 32;
        return new org.omg.TimeBase.UtcT
        (
        date.getTime(),
        (int )( STDACCURACCY & Integer.MAX_VALUE),
        (short )(tmp & Short.MAX_VALUE),
        (short )0
        );
    }

    public static UtcT getCurrentTime() {
	  return dateToUtc( new java.util.Date() );
    }
    
    /**
    * Returns the current time in the form of a long based on the 
    * the current time from the supplied time server.
    */

    public static long getTime( TimeService time ) throws TimeUnavailable
    {
        return time.universal_time().time();
    }

   /**
    * Resolves time by attempting to get time from a time server, and if
    * unavailable, uses the JVM.
    */

    public static long resolveTime( TimeService time )
    {
	  try
	  {
            return getTime( time );
	  }
	  catch(Exception e)
	  {
		return getCurrentTime().time;
        }
    }
}
