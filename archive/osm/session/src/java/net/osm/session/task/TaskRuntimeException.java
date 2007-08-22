/*
 * Copyright 2002 OSM SARL. All Rights Reserved.
 * 
 * This software is the proprietary information of OSM SARL.  
 * Use is subject to license terms.
 * 
 * @author  Stephen McConnell
 * @version 1.0 28/08/2001
 */

package net.osm.session.task;

import net.osm.session.resource.AbstractResourceRuntimeException;
 
/**
 * General exception type for exceptions raised by a Task.
 * @version 1.0
 */
public class TaskRuntimeException extends AbstractResourceRuntimeException
{

    /**
     * Constructs an <code>TaskRuntimeException</code> with the specified detail message.
     * @param msg the detail message.
     */
    public TaskRuntimeException( String msg ) 
    {
        this( msg, null );
    }

    /**
     * Constructs an <code>TaskRuntimeException</code> with the specified detail message
     * and causal exception.
     * @param msg the detail message.
     * @param cause the exception causing the problem.
     */
    public TaskRuntimeException( String msg, Throwable ex ) 
    {
        super(msg, ex);
    }
}

