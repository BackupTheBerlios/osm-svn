
package net.osm.agent;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.Icon;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import org.omg.Session.task_state;

import net.osm.util.ExceptionHelper;
import net.osm.shell.Shell;

/**
 * Action to start a task.
 *
 * @author  Stephen McConnell
 * @version 1.0 21 JUN 2001
 */
class TaskStartAction extends AbstractAction implements PropertyChangeListener
{

    //==========================================================
    // state
    //==========================================================

   /**
    * Entity task which the action will be invoked.
    */
    private TaskAgent task;

   /**
    * Shell that will handle error conditions.
    */
    private Shell shell;

    //==========================================================
    // constructor
    //==========================================================

   /**
    * Creation of a new <code>TaskStartAction</code>.
    * @param shell the shell to which any error condition will be signalled
    * @param name - the name of the action
    * @param task - the target task
    */
    public TaskStartAction( Shell shell, String name, TaskAgent task ) 
    {
        super( name );
	  this.shell = shell;
        this.task = task;
	  task.addPropertyChangeListener( this );
        task.getProcessor().getModel().addPropertyChangeListener( this );
        setEnabled( checkEnablement() );
    }

    //======================================================================
    // PropertyChangeListener
    //======================================================================

   /**
    * Listens to file property changes - if the file held by the 
    * path panel is in fact a keystore, then set the keystore
    * property to true.
    */
    public void propertyChange( PropertyChangeEvent event )
    {
        String name = event.getPropertyName();
	  if( event.getSource() == task )
	  {
	      if( name.equals("state") ) setEnabled( checkEnablement() );
	  }
	  else if( name.equals("configured") )
        {
            setEnabled( checkEnablement() );
	  }
    }

    private boolean checkEnablement()
    {
	  task_state state = (task_state) task.getTaskState();
	  if( ( state == task_state.notstarted ) || ( state == task_state.suspended ) )
	  {
		return task.getProcessor().getModel().verify();
	  }
	  return false;
    }

    //==========================================================
    // ActionListener
    //==========================================================

   /**
    * Abstract actionPerformed.
    * @param event the action event
    * @osm.warning Errors are currntly printed to System.out.  This neeeds
    *   to be updated to generate an interactive error/warning dialog
    */
    public void actionPerformed( ActionEvent event )
    {
        try
	  {
		task.start();
	  }
	  catch( Throwable e )
	  {
		final String error = "cannot start task";
		shell.error( error, e );
	  }
    }
}
