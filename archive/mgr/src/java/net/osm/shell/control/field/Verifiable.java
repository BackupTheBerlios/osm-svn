/*
 * @(#)Validator.java
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
 */

public interface Verifiable
{

   /**
    * Return the base entity.
    */
    public boolean verify( );

}
