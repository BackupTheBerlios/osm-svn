/*
 * Copyright 2002 OSM SARL. All Rights Reserved.
 * 
 * This software is the proprietary information of OSM SARL.  
 * Use is subject to license terms.
 * 
 * @author  Stephen McConnell
 * @version 1.0 28/08/2001
 */

package net.osm.community;

import net.osm.session.workspace.WorkspaceRuntimeException;
 
/**
 * General exception type for exceptions raised by a Desktop.
 * @version 1.0
 */
public class CommunityRuntimeException extends WorkspaceRuntimeException
{

    /**
     * Constructs an <code>CommunityRuntimeException</code> with the specified detail message.
     * @param msg the detail message.
     */
    public CommunityRuntimeException( String msg ) 
    {
        this( msg, null );
    }

    /**
     * Constructs an <code>CommunityRuntimeException</code> with the specified detail message
     * and causal exception.
     * @param msg the detail message.
     * @param cause the exception causing the problem.
     */
    public CommunityRuntimeException( String msg, Throwable ex ) 
    {
        super(msg, ex);
    }
}

