/*
 * StructuredPushSupplierRuntimeException.java
 *
 * Copyright 2000 OSM SARL. All Rights Reserved.
 * 
 * This software is the proprietary information of OSM SARL.  
 * Use is subject to license terms.
 * 
 * @author  Stephen McConnell
 * @version 1.0 28/08/2001
 */

package net.osm.sps;

import org.apache.avalon.framework.CascadingRuntimeException;

/**
 * General exception type for exceptions raised by the vault.
 * @version 1.0
 */
public class StructuredPushSupplierRuntimeException extends CascadingRuntimeException 
{

   /**
    * Constructs an <code>StructuredPushSupplierRuntimeException</code> with the 
    * specified detail message.
    * @param msg the detail message.
    */
    public StructuredPushSupplierRuntimeException( String msg ) 
    {
        this( msg, null );
    }

    /**
     * Constructs an <code>StructuredPushSupplierRuntimeException</code> with the 
     * specified detail message and causal exception.
     * @param msg the detail message.
     * @param cause the exception causing the problem.
     */
    public StructuredPushSupplierRuntimeException( String msg, Throwable cause ) 
    {
        super(msg, cause );
    }
}

