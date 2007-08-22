/*
 * Copyright 2002 OSM SARL. All Rights Reserved.
 * 
 * This software is the proprietary information of OSM SARL.  
 * Use is subject to license terms.
 * 
 * @author  Stephen McConnell
 * @version 1.0 28/03/2002
 */

package net.osm.adapter;

import org.apache.avalon.framework.CascadingRuntimeException;

/**
 * General exception type for exceptions raised by the adapter framework.
 * @version 1.0
 */
public class AdapterRuntimeException extends CascadingRuntimeException 
{

    /**
     * Constructs an <code>AdapterRuntimeException</code> with the specified detail message.
     * @param msg the detail message.
     */
    public AdapterRuntimeException( String msg ) 
    {
        this( msg, null );
    }

    /**
     * Constructs an <code>AdapterRuntimeException</code> with the specified detail message
     * and causal exception.
     * @param msg the detail message.
     * @param cause the exception causing the problem.
     */
    public AdapterRuntimeException( String msg, Throwable ex ) 
    {
        super(msg, ex);
    }
}

