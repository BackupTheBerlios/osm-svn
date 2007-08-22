/*
 */
package net.osm.session;

import org.apache.avalon.framework.activity.Disposable;

/**
 * The Vulnerable interface can be implmentated by any object that can be 
 * destroyed in terms of its persistent existance. 
 *
 * @author Stephen McConnell <mcconnell@osm.net>
 */

public interface Vulnerable extends Disposable
{

   /**
    * Test is this instance can be terminated or not.
    * @return boolean true if the persistent identity of this 
    * instance can be destroyed.
    */
    public boolean expendable( );

   /**
    * Destroys the persistent identity of this object. An 
    * implementation is responsible for disposing of internal 
    * resources on completion of termiation actions. 
    */
    public void terminate( ) throws CannotTerminate;


}



