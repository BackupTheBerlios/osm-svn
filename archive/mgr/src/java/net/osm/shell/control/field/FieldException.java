/*
 * FieldException.java
 *
 * Copyright 2000 OSM SARL. All Rights Reserved.
 * 
 * This software is the proprietary information of OSM SARL.  
 * Use is subject to license terms.
 * 
 * @author  Stephen McConnell
 * @version 1.0 28/08/2001
 */

package net.osm.shell.control.field;

import net.osm.shell.control.ControlException;

/**
 * General exception type for exceptions raised by field controls.
 * @version 1.0
 */
public class FieldException extends ControlException 
{

    /**
     * Constructs an <code>FieldException</code> with the specified detail message.
     * @param msg the detail message.
     */
    public FieldException( String msg ) 
    {
        super(msg);
    }

    /**
     * Constructs an <code>FieldException</code> with the specified detail message
     * and causal exception.
     * @param msg the detail message.
     * @param cause the exception causing the problem.
     */
    public FieldException( String msg, Throwable ex ) 
    {
        super(msg, ex);
    }
}

