/*
 * Copyright 2002 OSM SARL. All Rights Reserved.
 * 
 * This software is the proprietary information of OSM SARL.  
 * Use is subject to license terms.
 * 
 * @author  Stephen McConnell
 * @version 1.0 28/08/2001
 */

package net.osm.session.resource;

import net.osm.session.SessionException;

/**
 * General exception type for exceptions raised by the abstract resource facility.
 * @version 1.0
 */
public class AbstractResourceException extends SessionException 
{

    /**
     * Constructs an <code>AbstractResourceException</code> with the specified detail message.
     * @param msg the detail message.
     */
    public AbstractResourceException( String msg ) 
    {
        super(msg);
    }

    /**
     * Constructs an <code>AbstractResourceException</code> with the specified detail message
     * and causal exception.
     * @param msg the detail message.
     * @param cause the exception causing the problem.
     */
    public AbstractResourceException( String msg, Throwable ex ) 
    {
        super(msg, ex);
    }
}

