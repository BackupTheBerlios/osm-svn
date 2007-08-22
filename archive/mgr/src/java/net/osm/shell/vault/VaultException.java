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

package net.osm.shell.vault;

import net.osm.shell.ShellException;

/**
 * General exception type for exceptions raised by controls.
 * @version 1.0
 */
public class VaultException extends ShellException 
{

    /**
     * Constructs an <code>VaultException</code> with the specified detail message.
     * @param msg the detail message.
     */
    public VaultException( String msg ) 
    {
        super(msg);
    }

    /**
     * Constructs an <code>VaultException</code> with the specified detail message
     * and causal exception.
     * @param msg the detail message.
     * @param cause the exception causing the problem.
     */
    public VaultException( String msg, Throwable ex ) 
    {
        super(msg, ex);
    }
}

