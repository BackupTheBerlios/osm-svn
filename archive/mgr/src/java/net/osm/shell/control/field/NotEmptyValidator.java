/*
 * @(#)NotEmptyValidator.java
 *
 * Copyright 2000 OSM SARL. All Rights Reserved.
 * 
 * This software is the proprietary information of OSM SARL.  
 * Use is subject to license terms.
 * 
 * @author  Stephen McConnell
 * @version 1.0 22/08/2001
 */

package net.osm.shell.control.field;

/**
 * Tests for the existance of a non null and non-empty text string.
 */

public class NotEmptyValidator implements Validator
{

   /**
    * This validator
    * returns true on verify if the source text is not null and 
    * not 0 length.
    */
    public boolean verify( Object source )
    {
        if( source == null ) return false;
	  if( source instanceof String ) return (((String)source).length() > 0);
        return false;
    }

}
