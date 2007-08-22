/*
 * Copyright 2000 OSM All Rights Reserved.
 * 
 * This software is the proprietary information of OSM
 * Use is subject to license terms.
 * 
 * @author  Stephen McConnell
 * @version 1.0 19/03/2001
 */

package net.osm.session;

import org.apache.avalon.framework.CascadingException;

/**
 * The <code>CannotTerminate</code> is thrown by the terminate
 * operation if an attempt is made to terminate an instace that 
 * is not in an appropriate state for termination (e.g. existance
 * of external dependencies).
 *
 * @author  mcconnell
 * @version 1.0
 */

public class CannotTerminate extends CascadingException {

    /**
     * Construct a new <code>CannotTerminate</code> instance with the 
     * supplied message parameter and a null value for the cause exception.
     *
     * @param message Message summarising the exception.
     */

    public CannotTerminate( final String message ) 
    {
        this( message, null );
    }

    /**
     * Construct a new <code>CannotTerminate</code> instance with the 
     * supplied message parameter and a supplied cause exception.
     *
     * @param message The detail message for this exception.
     * @param cause the root cause of the exception
     */

    public CannotTerminate( final String message, final Throwable cause ) 
    {
        super( message, cause );
    }

}

