
package net.osm.shell.control.activity;


/**
 * Interface that describes a long-running activitiy (equivilent to a 
 * long duration transient method invocation under a seperate thread of 
 * control).
 */
public interface Activity 
{
    /**
     * Called from an action to start the task.
     * @exception Exception general exception
     */
    public void execute() throws Exception;

    /**
     * Returns the result of the activity execution.
     * @return Object the result of activiity execution
     */
    public Object getResult();

   /**
    * Declare the callback handler to the activity.
    * @param callback a callback handler that will handle
    *   notification of progress of the activity
    */
    public void setCallback( ActivityCallback callback );

}
