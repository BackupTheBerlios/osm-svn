/*
 */
package net.osm.playground;

/**
 * The <code>SimpleService</code> executes an objective.
 *
 * @author <a href="mailto:mcconnell@osm.net">Stephen McConnell</a>
 */

public interface SimpleService
{

    static final String KEY = "net.osm.playground.SimpleService";

   /**
    * Execute the prime objective of this services.
    */
    void doObjective();
}
