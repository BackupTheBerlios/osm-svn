
package net.osm.entity;

import net.osm.shell.Entity;

/**
 * A factory for Entity instances.
 * @author Stephen McConnell
 */

public interface EntityFactory
{

   /**
    * Creation of a new <code>Entity</code> instance.
    * @param object the primary object
    * @return Entity an entity wrapping the supplied object
    */
    public Entity newInstance( Object object );

   /**
    * Returns true if the factory represents an abstract type.
    */
    public boolean isAbstract();

}

