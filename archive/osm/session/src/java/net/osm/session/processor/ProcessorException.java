/*
 * Copyright 2002 OSM SARL. All Rights Reserved.
 * 
 * This software is the proprietary information of OSM SARL.  
 * Use is subject to license terms.
 * 
 * @author  Stephen McConnell
 * @version 1.0 28/08/2001
 */

package net.osm.session.processor;

import net.osm.session.resource.AbstractResourceException;

/**
 * General exception type for exceptions raised by a Processor.
 * @version 1.0
 */
public class ProcessorException extends AbstractResourceException 
{

    /**
     * Constructs an <code>ProcessorException</code> with the specified detail message.
     * @param msg the detail message.
     */
    public ProcessorException( String msg ) 
    {
        super(msg);
    }

    /**
     * Constructs an <code>ProcessorException</code> with the specified detail message
     * and causal exception.
     * @param msg the detail message.
     * @param cause the exception causing the problem.
     */
    public ProcessorException( String msg, Throwable ex ) 
    {
        super(msg, ex);
    }
}

