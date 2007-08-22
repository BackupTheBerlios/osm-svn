/*
 * Copyright 2002 OSM SARL. All Rights Reserved.
 * 
 * This software is the proprietary information of OSM SARL.  
 * Use is subject to license terms.
 * 
 * @author  Stephen McConnell
 * @version 1.0 28/08/2001
 */

package net.osm.domain;

import org.apache.avalon.framework.CascadingRuntimeException;

/**
 * General exception type for exceptions raised by the domain service.
 * @version 1.0
 */
public class DomainRuntimeException extends CascadingRuntimeException 
{

    /**
     * Constructs an <code>DomainRuntimeException</code> with the specified detail message.
     * @param msg the detail message.
     */
    public DomainRuntimeException( String msg ) 
    {
        this( msg, null );
    }

    /**
     * Constructs an <code>DomainRuntimeException</code> with the specified detail message
     * and causal exception.
     * @param msg the detail message.
     * @param cause the exception causing the problem.
     */
    public DomainRuntimeException( String msg, Throwable ex ) 
    {
        super(msg, ex);
    }
}

