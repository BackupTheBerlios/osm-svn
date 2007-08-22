/*
 * @(#)StringHelper.java
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

/**
 * General utilities supporting property substitution in a string.
 */

public class StringHelper
{

    /**
    * Parse a string value and replace occurances of "${&lt;name&gt;}" with the 
    * corresponding system property value.  If a system property is undefined, 
    * throw a runtime exception with context message.
    */
    public static String parseValue( String input )
    {
        int i = input.indexOf("${");
        if( i < 0 ) return input;
        int j = input.indexOf("}",i);
        String key = input.substring( i+2, j );
        String value = System.getProperty( key );
        return parseValue( input.substring( 0, i ) 
           + value + input.substring( j+1, input.length() ));
    }
}
