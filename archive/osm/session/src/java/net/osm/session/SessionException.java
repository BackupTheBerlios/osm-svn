/*
 * Copyright 2002 OSM SARL. All Rights Reserved.
 * 
 * This software is the proprietary information of OSM SARL.  
 * Use is subject to license terms.
 * 
 * @author  Stephen McConnell
 * @version 1.0 28/08/2001
 */

package net.osm.session;

import org.apache.avalon.framework.CascadingException;

/**
 * General exception type for exceptions raised by the session facility.
 * @version 1.0
 */
public class SessionException extends CascadingException 
{

    /**
     * Constructs an <code>SessionException</code> with the specified detail message.
     * @param msg the detail message.
     */
    public SessionException( String msg ) 
    {
        this( msg, null );
    }

    /**
     * Constructs an <code>SessionException</code> with the specified detail message
     * and causal exception.
     * @param msg the detail message.
     * @param cause the exception causing the problem.
     */
    public SessionException( String msg, Throwable ex ) 
    {
        super(msg, ex);
    }
}

