/*
 * Copyright 2002 OSM SARL. All Rights Reserved.
 * 
 * This software is the proprietary information of OSM SARL.  
 * Use is subject to license terms.
 * 
 * @author  Stephen McConnell
 * @version 1.0 28/08/2001
 */

package net.osm.session.desktop;

import net.osm.session.workspace.WorkspaceException;

/**
 * General exception type for exceptions raised by a Desktop.
 * @version 1.0
 */
public class DesktopException extends WorkspaceException
{

    /**
     * Constructs an <code>DesktopException</code> with the specified detail message.
     * @param msg the detail message.
     */
    public DesktopException( String msg ) 
    {
        super(msg);
    }

    /**
     * Constructs an <code>DesktopException</code> with the specified detail message
     * and causal exception.
     * @param msg the detail message.
     * @param cause the exception causing the problem.
     */
    public DesktopException( String msg, Throwable ex ) 
    {
        super(msg, ex);
    }
}

