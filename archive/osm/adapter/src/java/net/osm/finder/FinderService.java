/*
 */
package net.osm.finder;

import org.apache.avalon.framework.component.Component;

/**
 * Interface supporting access to the finder.
 *
 * @author Stephen McConnell <mcconnell@osm.net>
 */
public interface FinderService extends Component
{

    public static final String FINDER_SERVICE_KEY = "FINDER_SERVICE_KEY";

   /**
    * Returns an object refererence to the finder.
    * @return Finder the finder object reference.
    */
    public Finder getFinder();

   /**
    * Register a provider of finder acapability with a parent finder.
    */
    public void register( FinderService service );

   /**
    * Removes a provider of finder acapability from the parent finder.
    */
    public void deregister( FinderService service );

}



