/*
 */
package net.osm.playground;

/**
 * The <code>BasicService</code> executes a prime objective.
 *
 * @author <a href="mailto:mcconnell@osm.net">Stephen McConnell</a>
 */
public interface BasicService
{

   static final String KEY = "net.osm.playground.BasicService";

   /**
    * Execute the prime objective of this services.
    */
    void doPrimeObjective();
}
