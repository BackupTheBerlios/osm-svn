/*
 */
package net.osm.chooser;

import org.apache.avalon.framework.component.Component;

import net.osm.adapter.Adaptive;

/**
 * Interface supporting access to the finder.
 *
 * @author Stephen McConnell <mcconnell@osm.net>
 */
public interface ChooserService extends Component
{

    public static final String CHOOSER_SERVICE_KEY = "CHOOSER_SERVICE_KEY";

   /**
    * Returns an object refererence to the finder.
    * @return Finder the finder object reference.
    */
    Chooser getChooser();

   /**
    * Get the sequence of keys supported by lookup.
    */
    String[] get_keys();

   /**
    * Register a provider of finder acapability with a parent finder.
    */
    void register( String name, Adaptive service );

   /**
    * Removes a provider of finder acapability from the parent finder.
    */
    void deregister( String name );

}



