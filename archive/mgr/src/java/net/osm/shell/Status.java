
/**
 */
package net.osm.shell;

import java.awt.Dimension;
import java.awt.BorderLayout;
import java.util.List;
import java.util.LinkedList;
import java.util.Iterator;
import javax.swing.JPanel;
import javax.swing.ImageIcon;
import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;
import javax.swing.border.EmptyBorder;
import javax.swing.JSeparator;
import javax.swing.JLabel;
import javax.swing.JProgressBar;
import javax.swing.Box;
import javax.swing.BoxLayout;

import org.apache.avalon.framework.activity.Disposable;

import net.osm.util.IconHelper;
import net.osm.util.ExceptionHelper;

class Status extends JPanel implements Disposable, ChangeListener
{

    private static final String path = "net/osm/shell/image/notification.gif";
    private static final ImageIcon icon = IconHelper.loadIcon( Status.class, path );
    private static final EmptyBorder border = new EmptyBorder(3,3,3,3);
    private static final EmptyBorder pborder = new EmptyBorder(3,3,3,3);
    private JProgressBar progress;

   /**
    * The list of currently running activities.
    */
    private final LinkedList activities = new LinkedList();
    private Activity activity;

   /**
    * The label containing the current message.
    */
    JLabel label = new JLabel( " " );

    public Status()
    {
        super( new BorderLayout() );

        //
        // create the message panel
	  //

	  label.setBorder( border );
	  label.setFont( MGR.font );
	  label.setForeground( MGR.foreground );
        add( label, BorderLayout.WEST );

        //
	  // add a dummy object to fill in the space between
        // the EAST and WEST components
	  //

        // add( new JPanel(), BorderLayout.CENTER );

        //
	  // create the progress bar
	  //

        progress = new JProgressBar();
	  progress.setSize( new Dimension( 160, 22 ) );
	  add( progress, BorderLayout.EAST );
	  progress.setIndeterminate( false );
    }

    synchronized public void setMessage( String message )
    {
        label.setText( " " + message );
    }

   /**
    * Declare a progressive activitiy.
    */
    public void execute( Activity activity )
    {
        // add the activitiy to our internal list of 
        // of activities, add a change listener, when the 
        // activity finishes (based on change 
        // event reception) remove the activity from the
        // list 

	  synchronized( activities )
	  {
	      activities.add( activity );
            activity.addChangeListener( this );
		try
		{
		    activity.execute();
	          handleChange( activities );
		}
		catch( Exception e )
		{
		    // beep and post an error message to the user
		    // indicating that the activity could not be executed
                activity.removeChangeListener( this );
	          activities.remove( activity );
		    System.out.println("STATUS ERROR");
		    e.printStackTrace();
	          handleChange( activities );
		}
	  }
    }

    public void stateChanged( ChangeEvent event )
    {
        handleChange( activities );
    }

   /**
    * If the list of activities is empty then show the progress
    * bar as empty.  If the list contains a single activitity, the 
    * the progress bar should reflect the status of that single activity.
    * If the activitity list contains multiple entries then show the 
    * progress bar as indeterminate.
    */
    private synchronized void handleChange( List list )
    {
	  int size = activities.size();
        if( size == 0 )
	  {
		progress.setMaximum( 100 );
		progress.setMinimum( 0 );
		progress.setValue( 0 );
		progress.setIndeterminate( false );
	  }
	  else if( size == 1 )
	  {
		Activity activity = (Activity) activities.get( 0 );
		if( activity.getValue() < activity.getMaximum() )
		{
		    if( activity.getIndeterminate() )
		    {
		        progress.setIndeterminate( true );			  
		    }
		    else
	          {
		        progress.setMaximum( activity.getMaximum() );
		        progress.setMinimum( activity.getMinimum() );
		        progress.setValue( activity.getValue() );
		        progress.setIndeterminate( false );
	          }
	      }
		else
		{
		    activity.removeChangeListener( this );
		    activities.remove( 0 );
		    handleChange( list );
            }
	  }
	  else
	  {
		progress.setIndeterminate( true );
		if( packList( list ) != size ) handleChange( list );
	  }
    }

   /**
    * Remove any completed activities.
    * @return int the size of the list following the pack procedure
    */
    private int packList( List list )
    {
	  synchronized( list )
	  {
		Iterator iterator = list.iterator();
	      while( iterator.hasNext() )
	      {
		    Activity a = (Activity) iterator.next();
		    if( a.getValue() < a.getMaximum() ) iterator.remove();
	      }
	  }
	  return list.size();
    } 

   /**
    * Declare an error to the shell.
    */
    public void error( String message, Throwable e )
    {
	   ExceptionHelper.printException( message, e, this, true );
    }

   /**
    * Cleanup and dipose of state members.
    */
    public void dispose() 
    {
	  synchronized( activities )
	  {
		Iterator iterator = activities.iterator();
	      while( iterator.hasNext() )
	      {
		    Activity a = (Activity) iterator.next();
		    iterator.remove();
	      }
	  }
        progress.setIndeterminate( false );
        progress.setVisible( false );
        progress = null;
    }
}
