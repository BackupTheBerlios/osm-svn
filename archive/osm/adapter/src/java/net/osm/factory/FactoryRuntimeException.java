/*
 * Copyright 2002 OSM SARL. All Rights Reserved.
 * 
 * This software is the proprietary information of OSM SARL.  
 * Use is subject to license terms.
 * 
 * @author  Stephen McConnell
 * @version 1.0 28/08/2001
 */

package net.osm.factory;

import org.apache.avalon.framework.CascadingRuntimeException;

/**
 * General exception type for exceptions raised by the factory framework.
 * @version 1.0
 */
public class FactoryRuntimeException extends CascadingRuntimeException 
{

    /**
     * Constructs an <code>FactoryRuntimeException</code> with the specified detail message.
     * @param msg the detail message.
     */
    public FactoryRuntimeException( String msg ) 
    {
        this( msg, null );
    }

    /**
     * Constructs an <code>FactoryRuntimeException</code> with the specified detail message
     * and causal exception.
     * @param msg the detail message.
     * @param cause the exception causing the problem.
     */
    public FactoryRuntimeException( String msg, Throwable ex ) 
    {
        super(msg, ex);
    }
}

