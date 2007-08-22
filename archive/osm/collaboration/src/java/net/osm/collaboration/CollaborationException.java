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

import net.osm.session.SessionException;

/**
 * General exception type for exceptions raised by a collaboration component.
 * @version 1.0
 */
public class CollaborationException extends SessionException
{

    /**
     * Constructs an <code>CommunityException</code> with the specified detail message.
     * @param msg the detail message.
     */
    public CollaborationException( String msg ) 
    {
        super(msg);
    }

    /**
     * Constructs an <code>CollaborationException</code> with the specified detail message
     * and causal exception.
     * @param msg the detail message.
     * @param cause the exception causing the problem.
     */
    public CollaborationException( String msg, Throwable ex ) 
    {
        super(msg, ex);
    }
}

