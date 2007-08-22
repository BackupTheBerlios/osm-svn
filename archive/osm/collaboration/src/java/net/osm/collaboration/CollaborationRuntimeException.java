/*
 * Copyright 2002 OSM SARL. All Rights Reserved.
 * 
 * This software is the proprietary information of OSM SARL.  
 * Use is subject to license terms.
 * 
 * @author  Stephen McConnell
 * @version 1.0 28/08/2001
 */

package net.osm.collaboration;

import net.osm.session.SessionRuntimeException;
 
/**
 * General exception type for exceptions raised by a collaboration component.
 * @version 1.0
 */
public class CollaborationRuntimeException extends SessionRuntimeException
{

    /**
     * Constructs an <code>CollaborationRuntimeException</code> with the specified detail message.
     * @param msg the detail message.
     */
    public CollaborationRuntimeException( String msg ) 
    {
        this( msg, null );
    }

    /**
     * Constructs an <code>CollaborationRuntimeException</code> with the specified detail message
     * and causal exception.
     * @param msg the detail message.
     * @param cause the exception causing the problem.
     */
    public CollaborationRuntimeException( String msg, Throwable ex ) 
    {
        super(msg, ex);
    }
}

