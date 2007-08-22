
package net.osm.shell.control.activity;

/**
 * Interface defining operation that a long running activity can call
 * to notify current status of execution.
 */
public interface ActivityCallback 
{

   /**
    * Called by the activiity to declare its readiness to execute.
    * @param status set the enabled status of the callback implementation
    *   (typically used to enable an AbstractAction and thereby enable 
    *   menu items or other controls that the action is associated to).
    */
    public void setEnabled( boolean status );

   /**
    * Set a status message inidicating the current phase
    * of activitiy.
    * @param message a message describing the phase of execution
    */
    public void setMessage( String message );

   /**
    * Declare the current progress of the activity.
    * @param progress an integer value indicating that a specific
    *   phase has been established
    */
    public void setProgress( int progress );

}
