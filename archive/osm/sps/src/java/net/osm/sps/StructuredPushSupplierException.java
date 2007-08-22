/*
 * Copyright 2000 OSM SARL. All Rights Reserved.
 * 
 * This software is the proprietary information of OSM SARL.  
 * Use is subject to license terms.
 * 
 * @author  Stephen McConnell
 * @version 1.0 28/08/2001
 */

package net.osm.sps;

import org.apache.avalon.framework.CascadingException;

/**
 * General exception type for exceptions raised by a structured
 * push suppplied.
 * @version 1.0
 */
public class StructuredPushSupplierException extends CascadingException 
{

   /**
    * Constructs an <code>StructuredPushSupplierException</code> with the 
    * specified detail message.
    * @param msg the detail message.
    */
    public StructuredPushSupplierException( String msg ) 
    {
        super(msg, null);
    }

   /**
    * Constructs an <code>StructuredPushSupplierException</code> with the 
    * specified detail message and causal exception.
    * @param msg the detail message.
    * @param cause the exception causing the problem.
    */
    public StructuredPushSupplierException( String msg, Throwable ex ) 
    {
        super(msg, ex);
    }
}

