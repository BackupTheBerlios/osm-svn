/*
 * VaultException.java
 *
 * Copyright 2000 OSM SARL. All Rights Reserved.
 * 
 * This software is the proprietary information of OSM SARL.  
 * Use is subject to license terms.
 * 
 * @author  Stephen McConnell
 * @version 1.0 28/08/2001
 */

package net.osm.vault;

import org.apache.avalon.framework.CascadingRuntimeException;

/**
 * General exception type for exceptions raised by the vault.
 * @version 1.0
 */
public class VaultRuntimeException extends CascadingRuntimeException 
{

    /**
     * Constructs an <code>VaultRuntimeException</code> with the specified detail message.
     * @param msg the detail message.
     */
    public VaultRuntimeException( String msg ) 
    {
        this( msg, null );
    }

    /**
     * Constructs an <code>VaultRuntimeException</code> with the specified detail message
     * and causal exception.
     * @param msg the detail message.
     * @param cause the exception causing the problem.
     */
    public VaultRuntimeException( String msg, Throwable ex ) 
    {
        super(msg, ex);
    }
}

