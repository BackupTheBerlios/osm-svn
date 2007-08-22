/*
 * Copyright 2002 OSM SARL. All Rights Reserved.
 * 
 * This software is the proprietary information of OSM SARL.  
 * Use is subject to license terms.
 * 
 * @author  Stephen McConnell
 * @version 1.0 28/08/2001
 */

package net.osm.gateway;

import org.apache.avalon.framework.CascadingException;

/**
 * Excception raised if the gateway service is not available or disconnected.
 * @version 1.0
 */
public class GatewayDisconnectException extends GatewayRuntimeException 
{

    /**
     * Constructs an <code>GatewayDisconnectException</code> with the specified detail message.
     * @param msg the detail message.
     */
    public GatewayDisconnectException( String msg ) 
    {
        this( msg, null );
    }

    /**
     * Constructs an <code>GatewayDisconnectException</code> with the specified detail message
     * and causal exception.
     * @param msg the detail message.
     * @param cause the exception causing the problem.
     */
    public GatewayDisconnectException( String msg, Throwable ex ) 
    {
        super(msg, ex);
    }
}

