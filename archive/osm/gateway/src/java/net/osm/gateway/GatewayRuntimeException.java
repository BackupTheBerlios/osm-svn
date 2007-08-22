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

import org.apache.avalon.framework.CascadingRuntimeException;

/**
 * General exception type for exceptions raised by the gateway.
 * @version 1.0
 */
public class GatewayRuntimeException extends CascadingRuntimeException 
{

    /**
     * Constructs an <code>GatewayRuntimeException</code> with the specified detail message.
     * @param msg the detail message.
     */
    public GatewayRuntimeException( String msg ) 
    {
        this( msg, null );
    }

    /**
     * Constructs an <code>GatewayRuntimeException</code> with the specified detail message
     * and causal exception.
     * @param msg the detail message.
     * @param cause the exception causing the problem.
     */
    public GatewayRuntimeException( String msg, Throwable ex ) 
    {
        super(msg, ex);
    }
}

