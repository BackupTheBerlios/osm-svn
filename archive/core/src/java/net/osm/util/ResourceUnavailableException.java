/*
 * ResourceUnavailableException.java
 *
 * Created on May 23, 2001, 13:25
 */
package net.osm.util;

import org.apache.avalon.framework.CascadingException;

/**
 * The <code>ResourceUnavailableException </code> is thrown  
 * as a result of the non-availabilty of a remote service.
 *
 * @author  mcconnell
 * @version 1.0
 */

public class ResourceUnavailableException extends CascadingException {

    /**
     * Construct a new <code>ResourceUnavailableException </code> instance with the 
     * supplied message parameter and a null value for the cause exception.
     *
     * @param message Message summarising the exception.
     */

    public ResourceUnavailableException ( final String message ) 
    {
        this( message, null );
    }

    /**
     * Construct a new <code>ResourceUnavailableException </code> instance with the 
     * supplied message parameter and a supplied cause exception.
     *
     * @param message The detail message for this exception.
     * @param cause the root cause of the exception
     */

    public ResourceUnavailableException ( final String message, final Throwable cause ) 
    {
        super( message, cause );
    }

}

