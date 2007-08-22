
package net.osm.entity;

import org.apache.avalon.framework.context.DefaultContext;

import net.osm.shell.Entity;

/**
 *
 * @author Stephen McConnell
 */

public class EntityContext extends DefaultContext
{

    //=================================================================
    // state
    //=================================================================

    private Object primary;

    //=================================================================
    // constructor
    //=================================================================

   /**
    * Creation of a new <code>EntityContext</code>.
    */
    public EntityContext( Object primary )
    {
	  if( primary == null ) throw new NullPointerException("null primary argument");
        this.primary = primary;
    }

   /**
    * Return the primary object that the entity is representing.
    */
    public Object getPrimary()
    {
        return primary;
    }
}