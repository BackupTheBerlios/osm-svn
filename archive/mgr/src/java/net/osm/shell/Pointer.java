/*
 * Pointer.java
 *
 * Copyright 2000 OSM SARL. All Rights Reserved.
 * 
 * This software is the proprietary information of OSM SARL.  
 * Use is subject to license terms.
 * 
 * @author  Stephen McConnell
 * @version 1.0 6/02/2001
 */

package net.osm.shell;

/**
 * An <code>Pointer</code> interface declares that the implementation 
 * object is a pointer another Entity. 
 */
public interface Pointer
{

   /**
    * Returns the target that the pointer is referencing. 
    * @return Entity the entity that the pointer is referencing
    */
    public Entity getTarget( );

}
