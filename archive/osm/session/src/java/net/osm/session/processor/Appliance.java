

package net.osm.session.processor;

import org.apache.avalon.framework.activity.Startable;
import org.apache.avalon.framework.activity.Suspendable;

/**
 * Appliance defines the operations that shall be implemented 
 * by an runnable object capable of handling a processor primary thread of 
 * execution.  An appliance implementation is required to motify its hosting
 * processor of state changes via an <code>ApplianceEvent</code>.
 */
public interface Appliance extends Startable, Suspendable, ApplianceHandler, Runnable
{

    //============================================================
    // static
    //============================================================

   /**
    * Constate state value indicating the the appliance is not running.
    */
    public static final int IDLE = 0;

   /**
    * Constate state value indicating the the appliance is running.
    */
    public static final int RUNNING = 1;

   /**
    * Constate state value indicating the the appliance is suspended.
    */
    public static final int SUSPENDED = 2;

   /**
    * Constate state value indicating the the appliance is stopped abnormally.
    */
    public static final int TERMINATED = 3;

   /**
    * Constate state value indicating the the appliance is completed normally.
    */
    public static final int COMPLETED = 4;


    //=================================================================
    // AbstractAppliance
    //=================================================================

   /**
    * Return the preferred state of the appliance.
    * @return int appliance state
    */
    public int getApplianceState();

}
