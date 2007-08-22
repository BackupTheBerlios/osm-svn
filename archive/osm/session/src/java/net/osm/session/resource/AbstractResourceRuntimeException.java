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

import net.osm.session.SessionRuntimeException;
 
/**
 * General exception type for exceptions raised by the abstract resource framework.
 * @version 1.0
 */
public class AbstractResourceRuntimeException extends SessionRuntimeException
{

    /**
     * Constructs an <code>AbstractResourceRuntimeException</code> with the specified detail message.
     * @param msg the detail message.
     */
    public AbstractResourceRuntimeException( String msg ) 
    {
        this( msg, null );
    }

    /**
     * Constructs an <code>AbstractResourceRuntimeException</code> with the specified detail message
     * and causal exception.
     * @param msg the detail message.
     * @param cause the exception causing the problem.
     */
    public AbstractResourceRuntimeException( String msg, Throwable ex ) 
    {
        super(msg, ex);
    }
}

