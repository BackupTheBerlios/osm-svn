
package net.osm.agent;

import java.awt.event.ActionEvent;
import java.awt.Component;
import javax.swing.AbstractAction;
import javax.swing.JDialog;
import javax.swing.JProgressBar;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.DefaultBoundedRangeModel;
import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.EventListenerList;

import org.omg.CommunityFramework.Criteria;
import org.omg.CommunityFramework.GenericResource;
import org.omg.CommunityFramework.GenericResourceHelper;
import org.omg.CommunityFramework.ResourceFactoryProblem;
import org.omg.CollaborationFramework.Processor;
import org.omg.CollaborationFramework.ProcessorHelper;

import net.osm.entity.EntityService;
import net.osm.hub.home.ResourceFactory;
import net.osm.hub.home.CriteriaNotFound;
import net.osm.pki.pkcs.PKCS10;
import net.osm.shell.Shell;
import net.osm.shell.Activity;
import net.osm.vault.Vault;
import net.osm.util.IOR;
import net.osm.util.ExceptionHelper;

/**
 * The <code>CertificationRequestActivity</code> handles the establishment
 * of a PKCS10 certification request.  The activity creates a new GenericResource
 * and adds a PKCS10 request as the resource value.
 * @author  Stephen McConnell
 * @version 1.0 23 DEC 2001
 */
class CertificationRequestActivity extends AbstractAction implements Activity
{

    //==========================================================
    // state
    //==========================================================

   /**
    * The change event listener list.
    */
    private final EventListenerList listeners = new EventListenerList();
    
   /**
    * A change event signalling modification of the activity status.
    */
    private ChangeEvent changeEvent;

   /**
    * The vault against which a PKCS10 instance will be created.
    */
    private final Vault vault;

   /**
    * The principal user agent under which the certification request
    * task will be established.
    */
    private final UserAgent user;

   /**
    * The resource factory that will be used to create the generic 
    * resource containing the certification request value.
    */
    private final ResourceFactory factory;

   /**
    * The entity servicce against which supporting agents can be 
    * created.
    */
    private final EntityService service;

   /**
    * Declaration of a progress monitor will be made to the shell
    * and the shell will handle presentation of progress to the user
    * via the status panel.
    */
    private final Shell shell;

    private boolean indeterminate = true;
    private int maximum = 100;
    private int minimum = 0;
    private int value = 0;
    private Throwable error;

    //==========================================================
    // constructor
    //==========================================================

   /**
    * Creation of a new CertificationRequestActivity.
    * @param shell the shell within which the status of the action will be presented
    * @param vault the vault from which the PKCS10 request will be created
    * @param user the user agent to be assigned as owner of the certification task
    * @param factory the factory on the server for creation of a new generic resource
    *    containing the PKCS10 request
    * @param service the service suppporting creation of new entities wrapping the 
    *    remote resources and tasks
    */
    CertificationRequestActivity( 
      final String name, final Shell shell, final Vault vault, final UserAgent user, final ResourceFactory factory, final EntityService service ) 
    {
        super( name );
        this.shell = shell;
        this.vault = vault;
        this.user = user;
        this.factory = factory;
        this.service = service;
    }

    //==========================================================
    // Activity
    //==========================================================

   /**
    * Add a change listener to the activity.
    */
    public void addChangeListener( ChangeListener listener )
    {
        listeners.add( ChangeListener.class, listener );
    }

   /**
    * Remove a change listener from the activity.
    */
    public void removeChangeListener( ChangeListener listener )
    {
        listeners.remove( ChangeListener.class, listener );
    }

   /**
    * Returns the maximum possible value of activity progress.
    */
    public int getMaximum()
    {
        return maximum;
    }

   /**
    * Returns the minimum possible value of activity progress.
    */
    public int getMinimum()
    {
        return minimum;
    }

   /**
    * Returns the current value of activity progress.
    */
    public int getValue()
    {
        return value;
    }

   /**
    * Returns the indeterminate status of the activitity.
    */
    public boolean getIndeterminate()
    {
        return indeterminate;
    }

   /**
    * Returns an exception (normally null) resulting from the 
    * execution of the executable instance.
    */
    public Throwable getError()
    {
        return error;
    }

   /**
    * Creates a thread within which a PKCS10 certificate request is instantiated,
    * placed within a generic resource and assigned as an input argument to a new
    * task owned by the primary user.
    * @exception Exception if the executable instance cannot initialize
    */
    public void execute() throws Exception
    {
        Thread thread = new Thread(
          new Runnable() 
          {
            public void run()
            {
                GenericResourceAgent pkcs10 = null;
		    TaskAgent task = null;
		    try
		    {
			  System.out.println("\tcreating input resource");
			  pkcs10 = createGenericResource();
		    }
		    catch( Throwable e )
		    {
			  error = e;
			  notifyCompletion( null );
			  return;
		    }

		    try
		    {
			  System.out.println("\tcreating task");
			  task = createTask();
		    }
		    catch( Throwable e )
		    {
			  error = e;
			  notifyCompletion( task );
			  return;
		    }

		    try
		    {
			  System.out.println("\tstarting task");
			  task.addConsumed( pkcs10, "request" );
		    }
		    catch( Throwable e )
		    {
			  final String problem = "Unable to assign PKCS10 resource to task.";
			  error = new Exception( problem, e );
			  try
			  {
				task.remove();
			  }
			  catch( Throwable internal )
			  {
			      final String msg = "Unable to retract redundant task. ";
			      System.out.println( msg + internal.toString() );
			      notifyCompletion( task );
			      return;
			  }
		    }

                try
                {
			  task.start();
			  error = null;
                }
                catch( Throwable e)
                {
			  final String problem = "Failed to start task.";
			  error = new Exception( problem, e );
                }
	          finally
		    {
			  notifyCompletion( task );
	          }
            }
          }
        );
        thread.start();
    }

    private GenericResourceAgent createGenericResource() throws Exception
    {
	  GenericResourceAgent pkcs10 = null;
	  GenericResource generic = null;
	  final String label = "net.osm.pki.certification.request";
        try
	  {
	      final PKCS10 request = vault.createPKCS10();
		final Criteria criteria = factory.criterion( label );
		generic = GenericResourceHelper.narrow( factory.create( 
              "PKCS10 Certification Request", criteria ) );
		generic.set_value( request );
		pkcs10 = (GenericResourceAgent) service.resolve( generic );
		return pkcs10;
	  }
	  catch( CriteriaNotFound e )
	  {
		final String error = "Gateway could not locate a server supporting the criteria : ";
            System.out.println( error + label );
		throw new Exception( error + label, e );
        }
	  catch( ResourceFactoryProblem e )
	  {
		final String error = "ResourceFactory raised an exception.";
            ResourceFactoryExceptionHelper.printException( error, e, this );
		throw new Exception( error + label, e );
        }
	  catch( Throwable e )
	  {
            if( pkcs10 != null )
		{
		    try
		    {
		        System.out.println("\tdisposed on pkcs10 agent due to creation error");
			  pkcs10.remove();
		    }
		    catch( Throwable ignore )
		    {
		        System.out.println("\tpkcs10 disposal error");
		    }
		}
		else if( generic != null )
		{
		    try
		    {
		        System.out.println("\tdisposed on generic resource due to creation error");
			  generic.remove();
		    }
		    catch( Throwable ignore )
		    {
		        System.out.println("\tgeneric disposal error");
		    }
		}
		final String error = "Could not create PKCS10 Generic Resource.";
		throw new Exception( error, e );
	  }
    }

    private TaskAgent createTask() throws Exception
    {
	  Processor p = null;
	  ProcessorAgent processor = null;
	  final String label = "net.osm.pki.certification";
	  TaskAgent task = null;
        try
	  {
            final Criteria c = factory.criterion( label );
            p =  ProcessorHelper.narrow( factory.create( "Certification Process", c ));
		processor = (ProcessorAgent) service.resolve( p );
            task = user.createTask( "Digital Identity Certification", processor );
		return task;
	  }
	  catch( CriteriaNotFound e )
	  {
		final String error = "Gateway could not locate a server supporting the processor criteria : ";
		throw new Exception( error + label, e );
        }
	  catch( Throwable e )
	  {
            if( task != null )
		{
		    try
		    {
		        System.out.println("\tdisposed on task agent due to creation error");
			  task.remove();
		    }
		    catch( Throwable ignore )
		    {
		        System.out.println("\terror in disposed on task agent");
		    }
		}
            else if( processor != null )
		{
		    try
		    {
		        System.out.println("\tdisposed on processor agent due to creation error");
			  processor.remove();
		    }
		    catch( Throwable ignore )
		    {
		        System.out.println("\terror removing processor agent " + ignore.toString());
			  try
			  {
			      processor.dispose();
			  }
			  catch( Throwable ignoringDisposal )
			  {
		            System.out.println("\terror in disposed on processor agent " + ignore.toString());
			  }
		    }
		}
		else if( p != null )
		{
		    try
		    {
		        System.out.println("\tdisposed on processor reference due to creation error");
			  p.remove();
		    }
		    catch( Throwable ignore )
		    {
		        System.out.println("\terror in disposed on processor reference");
		    }
		}
		final String error = "Failed to create certification task.";
		throw new Exception( error, e );
	  }
    }


    //==========================================================
    // ActionListener
    //==========================================================

   /**
    * Triggers execution of the certificate request under the shell.
    * @param event action event initiating the action
    */
    public void actionPerformed( ActionEvent event )
    {
        shell.execute( this );
    }

    /** 
     * Runs each <code>ChangeListener</code>'s <code>stateChanged</code> method.
     */
    protected void fireStateChanged() 
    {
        Object[] list = listeners.getListenerList();
        for (int i = list.length - 2; i >= 0; i -=2 ) {
            if (list[i] == ChangeListener.class) {
                if (changeEvent == null) {
                    changeEvent = new ChangeEvent(this);
                }
                ((ChangeListener)list[i+1]).stateChanged(changeEvent);
            }          
        }
    }

   /**
    * Set the activities status to indicate completion.
    */
    protected void notifyCompletion( TaskAgent task )
    {
        value = 100;
	  if( error != null )
	  {
		ExceptionHelper.printException( 
		  "Certification activity execution failure.", error, this, false );
	  }
	  else
	  {
		System.out.println( "TASK: " + task );
	  }
        indeterminate = false;
	  fireStateChanged();
    }
}
