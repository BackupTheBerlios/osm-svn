/*
 */
package net.osm.session.resource;

import org.apache.avalon.framework.component.Component;

/**
 * Interface supporting AbstractResource management actions.
 *
 * @author Stephen McConnell <mcconnell@osm.net>
 */
public interface ActivatorService extends Component
{

   /**
    * Service lookup key.
    */
    public static final String ACTIVATOR_KEY = "activator_key";    

}



