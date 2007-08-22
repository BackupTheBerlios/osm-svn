/*
 * GatewayRuntimeException.java
 *
 * Created on May 23, 2001, 13:25
 */
package net.osm.util;

import org.apache.avalon.framework.CascadingException;

/**
 * The <code>MalformedElementException </code> may be thrown  
 * as a result of badly formed XML element.  The defintion of what 
 * constitutes 'badly-formed' is application dependant.  Typically 
 * this exception will be thorwn in the case where an element does
 * not declare required attributes.
 *
 * @author  mcconnell
 * @version 1.0
 */

public class MalformedElementException extends CascadingException {

    /**
     * Construct a new <code>MalformedElementException </code> instance with the 
     * supplied message parameter and a null value for the cause exception.
     *
     * @param message Message summarising the exception.
     */

    public MalformedElementException ( final String message ) 
    {
        this( message, null );
    }

    /**
     * Construct a new <code>MalformedElementException </code> instance with the 
     * supplied message parameter and a supplied cause exception.
     *
     * @param message The detail message for this exception.
     * @param cause the root cause of the exception
     */

    public MalformedElementException ( final String message, final Throwable cause ) 
    {
        super( message, cause );
    }

}

