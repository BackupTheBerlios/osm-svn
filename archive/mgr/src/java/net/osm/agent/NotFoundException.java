/*
 * @(#)LoadException.java
 *
 * Copyright 2000 OSM SARL. All Rights Reserved.
 * 
 * This software is the proprietary information of OSM SARL.  
 * Use is subject to license terms.
 * 
 * @author  Stephen McConnell
 * @version 1.0 17/06/2001
 */

package net.osm.agent;

/**
 * Exception raised in resolse to ta failure to locate an agent.
 * @version 1.0
 */
public class NotFoundException extends Exception {

    /**
     * Creates new <code>NotFoundException</code> without detail message.
     */
    public NotFoundException() {
    }


    /**
     * Constructs an <code>NotFoundException</code> with the specified detail message.
     * @param msg the detail message.
     */
    public NotFoundException(String msg) {
        super(msg);
    }
}

