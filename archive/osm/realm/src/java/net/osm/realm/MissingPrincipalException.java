/*
 * PrincipalNotFound.java
 *
 * Copyright 2000 OSM SARL. All Rights Reserved.
 * 
 * This software is the proprietary information of OSM SARL.  
 * Use is subject to license terms.
 * 
 * @author  Stephen McConnell
 * @version 1.0 28/08/2001
 */

package net.osm.realm;

/**
 * Exception indication the the realm inteception services detected the 
 * absence of a current principal.
 * @version 1.0
 */
public class MissingPrincipalException extends RealmRuntimeException
{

     private static final String reason = "missing principal context";
    /**
     * Constructs an <code>MissingPrincipalException</code> with the specified detail message.
     * @param msg the detail message.
     */
    public MissingPrincipalException( ) 
    {
 	  super( reason );
    }
}

