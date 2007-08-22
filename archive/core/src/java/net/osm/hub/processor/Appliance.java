

package net.osm.hub.processor;

import java.io.Serializable;

/**
 * Appliance defines the operations that shall be implemented 
 * by an runnable object capable of handling a processor primary thread of 
 * execution.
 */
public interface Appliance extends Runnable
{

    //=================================================================
    // static
    //=================================================================

    public static final int PENDING = 0;
    public static final int RUNNING = 1;
    public static final int SUSPENDED = 2;
    public static final int TERMINATED = 3;
    public static final int COMPLETED = 4;

    //=================================================================
    // Appliance
    //=================================================================

   /**
    * Method invoked by a processor to request appliance suspension.
    * The run implementation should frequently check the current state using the 
    * <code>getState</code> method for posible SUSPENDED or TERMINATED values
    * in which case the run implementation should return.
    */
    public void requestSuspension();

   /**
    * Method invoked by a processor to request appliance termination.
    * The run implementation should frequently check the current state using the 
    * <code>getState</code> method for posible SUSPENDED or TERMINATED values
    * in which case the run implementation should return.
    */
    public void requestTermination();

    //=================================================================
    // AbstractAppliance
    //=================================================================

   /**
    * Return the preferred state of the appliance.
    * @return int appliance state
    */
    public int getState();

}
