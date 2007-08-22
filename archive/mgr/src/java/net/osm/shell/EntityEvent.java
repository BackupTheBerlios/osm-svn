/*
 * @(#)EntityEvent.java
 *
 * Copyright 2000 OSM SARL. All Rights Reserved.
 * 
 * This software is the proprietary information of OSM SARL.  
 * Use is subject to license terms.
 * 
 * @author  Stephen McConnell
 * @version 1.0 24/06/2001
 */

package net.osm.shell;

import java.util.EventObject;

/**
 * The <code>EntityEvent</code> signals disposal of an entity
 * towards an EntityHandler.
 */

public class EntityEvent extends EventObject
{

   //============================================================
    // state
    //============================================================

   /**
    * List initiating the event.
    */
    public final Entity entity;

    //============================================================
    // constructor
    //============================================================

   /**
    * Creation of a new EntityEvent signally the distruction of the 
    * entity.
    *
    * @param entity the source entity 
    */
    public EntityEvent( Entity entity ) 
    {
        super( entity );
        this.entity = entity;
    }

    //============================================================
    // implementation
    //============================================================

   /**
    * Returns the source entity.
    *
    * @return Entity the source entity.
    */
    public Entity getEntity()
    {
        return this.entity;
    }

   /**
    * Returns a string representation of the event.
    */
    public String toString()
    {
        return getClass().getName() + 
          "[" +	
          "id=" + System.identityHashCode( this ) + " " + 
          "entity=" + getEntity().getClass().getName() + "/" + 
		System.identityHashCode( getEntity() ) + " " + 
          "]";
    }

}

