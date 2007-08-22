/*
 * @(#)CannotRemoveException.java
 *
 * Copyright 2000 OSM SARL. All Rights Reserved.
 * 
 * This software is the proprietary information of OSM SARL.  
 * Use is subject to license terms.
 * 
 * @author  Stephen McConnell, mcconnell@osm.net
 * @version 1.0 18/06/2001
 */

package net.osm.agent;


/**
 * Exception indicating that a reosurce cannot be removed due to 
 * a usage constraint.
 * @author  mcconnell
 * @version 1.0
 */
public class CannotRemoveException extends Exception {

   /**
    * Creates new <code>CannotRemoveException</code> without detail message.
    * @param message the detail message.
    */
    public CannotRemoveException( String message ) {
	  super( message );
    }

   /**
    * Constructs an <code>CannotRemoveException</code> with the specified detail message.
    * @param message the detail message.
    * @param throwable the cause of the exception.
    */
    public CannotRemoveException( String message, Throwable throwable ) {
        super(message, throwable );
    }
}

