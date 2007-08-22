/*
 * Copyright 2002 OSM SARL. All Rights Reserved.
 * 
 * This software is the proprietary information of OSM SARL.  
 * Use is subject to license terms.
 * 
 * @author  Stephen McConnell
 * @version 1.0 28/08/2001
 */

package net.osm.chooser;

import org.apache.avalon.framework.CascadingException;

/**
 * General exception type for exceptions raised by a chooser.
 * @version 1.0
 */
public class ChooserException extends CascadingException 
{

    /**
     * Constructs an <code>ChooserException</code> with the specified detail message.
     * @param msg the detail message.
     */
    public ChooserException( String msg ) 
    {
        this( msg, null );
    }

    /**
     * Constructs an <code>ChooserException</code> with the specified detail message
     * and causal exception.
     * @param msg the detail message.
     * @param cause the exception causing the problem.
     */
    public ChooserException( String msg, Throwable ex ) 
    {
        super(msg, ex);
    }
}

