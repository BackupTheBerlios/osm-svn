/*
 * @(#)AvailableAliasValidator.java
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

import net.osm.shell.control.field.NotEmptyValidator;


/**
 * Tests for an alias that is non-null, non 0 length, and 
 * non-clonflicing with existing alias declarations.
 */

public class AvailableAliasValidator extends NotEmptyValidator implements Validator
{

   /**
    * This validator
    * returns true on verify if the source text is not null and 
    * not 0 length.
    */
    public boolean verify( Object source )
    {
        boolean interim = super.verify( source );
        if( !interim ) return false;

	  // 
	  // do assesment of non-conflicting alias name
        // WARNING: (not implemeted yet)
        //

	  return true;

    }
}
