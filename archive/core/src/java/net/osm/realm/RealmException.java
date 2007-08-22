/*
 * RealmException.java
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
 * General exception type for exceptions raised by the realm services.
 * @version 1.0
 */
public class RealmException extends Exception 
{

    /**
     * Constructs an <code>RealmException</code> with the specified detail message.
     * @param msg the detail message.
     */
    public RealmException( String msg ) 
    {
        super(msg);
    }

    /**
     * Constructs an <code>RealmException</code> with the specified detail message
     * and causal exception.
     * @param msg the detail message.
     * @param cause the exception causing the problem.
     */
    public RealmException( String msg, Throwable ex ) 
    {
        super(msg, ex);
    }
}

