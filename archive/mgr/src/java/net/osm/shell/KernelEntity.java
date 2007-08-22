/*
 * @(#)NullEntity.java
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

import java.util.List;

import net.osm.shell.DefaultEntity;

/**
 * The root entity in the navigator heirachy.
 */

class KernelEntity extends DefaultEntity
{

    List children;

    String name;

    //==============================================================
    // constructor
    //==============================================================

   /**
    * Creation of a null entity using a supplied name.
    * @param name the name to assign to the entity.
    */
    public KernelEntity( String name, List children )
    {
        super( );
        this.children = children;
        this.name = name;
    }

    //==============================================================
    // Entity
    //==============================================================

   /**
    * Set the name of the entity to the supplied <code>String</code>.
    * The <code>NullEntity</code> implementation does nothing.
    *
    * @param name the new entity name
    */
    public String getName()
    {
        return name;
    }

   /**
    * Test if this entity if a leaf or a composite.  The Kernel entity
    * always returns in order for the entity to be applied as the root 
    * node in the navigator heirachy.
    * @return boolean true if this is a leaf entity
    */
    public boolean isaLeaf( )
    {
        return false;
    }

   /**
    * Returns a list of entities that represents the navigatable content
    * of the target entity. 
    * @return List the navigatable content
    */
    public List getChildren( )
    {
        return children;
    }
}


