/* 
 * Activity.java
 */

package net.osm.shell;

import java.util.List;
import javax.swing.Action;
import javax.swing.event.ChangeListener;

import org.apache.avalon.framework.activity.Executable;

/**
 * The <code>Activity</code> interface defines an action that is long running.
 * It exposes operations through which a client can add listeners to the activity
 * state, and operation through which a client can query current state.
 *
 * @author Stephen McConnell
 */

public interface Activity extends Executable
{

   /**
    * Add a change listener to the activity.
    */
    public void addChangeListener( ChangeListener listener );

   /**
    * Remove a change listener from the activity.
    */
    public void removeChangeListener( ChangeListener listener );

   /**
    * Returns the maximum possible value of activity progress.
    */
    public int getMaximum();

   /**
    * Returns the minimum possible value of activity progress.
    */
    public int getMinimum();

   /**
    * Returns the current value of activity progress.
    */
    public int getValue();

   /**
    * Returns the indeterminate status of the activitity.
    */
    public boolean getIndeterminate();

   /**
    * Returns an exception (normally null) resulting from the 
    * execution of the executable instance.
    */
    public Throwable getError();
}
