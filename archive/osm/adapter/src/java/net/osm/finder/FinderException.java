/*
 * Copyright 2002 OSM SARL. All Rights Reserved.
 * 
 * This software is the proprietary information of OSM SARL.  
 * Use is subject to license terms.
 * 
 * @author  Stephen McConnell
 * @version 1.0 28/08/2001
 */

package net.osm.finder;

import org.apache.avalon.framework.CascadingException;

/**
 * General exception type for exceptions raised by the finder facility.
 * @version 1.0
 */
public class FinderException extends CascadingException 
{

    /**
     * Constructs an <code>FinderException</code> with the specified detail message.
     * @param msg the detail message.
     */
    public FinderException( String msg ) 
    {
        this( msg, null );
    }

    /**
     * Constructs an <code>FinderException</code> with the specified detail message
     * and causal exception.
     * @param msg the detail message.
     * @param cause the exception causing the problem.
     */
    public FinderException( String msg, Throwable ex ) 
    {
        super(msg, ex);
    }
}

