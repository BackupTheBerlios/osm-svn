/*
 * GatewayRuntimeException.java
 *
 * Created on March 28, 2001, 0:39 AM
 */
package net.osm.hub.gateway;

import org.apache.avalon.framework.CascadingRuntimeException;

/**
 * The <code>InternalException</code> may be thrown by an Appliance 
 * as a result of an unexpected internal exception during process
 * execution.
 *
 * @author  mcconnell
 * @version 1.0
 */

public class GatewayRuntimeException extends CascadingRuntimeException {

    /**
     * Construct a new <code>GatewayRuntimeException</code> instance with the 
     * supplied message parameter and a null value for the cause exception.
     *
     * @param message Message summarising the exception.
     */

    public GatewayRuntimeException( final String message ) 
    {
        this( message, null );
    }

    /**
     * Construct a new <code>GatewayRuntimeException</code> instance with the 
     * supplied message parameter and a supplied cause exception.
     *
     * @param message The detail message for this exception.
     * @param cause the root cause of the exception
     */

    public GatewayRuntimeException( final String message, final Throwable cause ) 
    {
        super( message, cause );
    }

}

