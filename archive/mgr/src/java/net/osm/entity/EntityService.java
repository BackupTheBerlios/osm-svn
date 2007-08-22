/* 
 * EntityService.java
 */

package net.osm.entity;

import org.apache.avalon.framework.component.Component;

import net.osm.shell.Entity;

/**
 * @author Stephen McConnell
 */

public interface EntityService extends Component
{

     public Entity resolve( Object object );

}
