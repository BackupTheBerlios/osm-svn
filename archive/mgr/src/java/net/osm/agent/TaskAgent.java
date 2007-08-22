
package net.osm.agent;

import java.awt.event.ActionEvent;
import java.util.LinkedList;
import java.util.List;
import java.util.Arrays;
import javax.swing.Action;

import org.omg.CORBA.Any;
import org.omg.CORBA.AnyHolder;
import org.omg.CORBA.BooleanHolder;
import org.omg.CORBA.TypeCode;
import org.omg.CORBA.ORB;
import org.omg.CosNotification.EventType;
import org.omg.CosCollection.Iterator;
import org.omg.Session.LinksHolder;
import org.omg.Session.CollectedByHelper;
import org.omg.Session.ConsumedByHelper;
import org.omg.Session.AbstractResource;
import org.omg.Session.AbstractResourcesHolder;
import org.omg.Session.AbstractResourceIteratorHolder;
import org.omg.Session.AbstractResourceHelper;
import org.omg.Session.LinkIteratorHolder;
import org.omg.Session.Task;
import org.omg.Session.TaskHelper;
import org.omg.Session.task_state;
import org.omg.Session.task_stateHelper;
import org.omg.Session.Consumes;
import org.omg.Session.ConsumesHelper;
import org.omg.Session.Produces;
import org.omg.Session.ProducesHelper;
import org.omg.CollaborationFramework.ProcessorHelper;

import net.osm.audit.RemoteEvent;
import net.osm.agent.util.Collection;
import net.osm.agent.AbstractResourceAgent;
import net.osm.agent.LinkCollection;
import net.osm.shell.GenericAction;
import net.osm.shell.ActiveFeature;
import net.osm.shell.TablePanel;
import net.osm.shell.ScrollView;
import net.osm.util.ExceptionHelper;

/**
 * TaskAgent is a an agent encapsulating a remote reference to a Task.  TaskAgent
 * listens to the associated processor model for changes to the <code>configured</code>
 * status.
 * @author Stephen McConnell
 */
public class TaskAgent extends AbstractResourceAgent
{

    //=========================================================================
    // static
    //=========================================================================

    private static final String CLOSED_STATE_STRING = "closed";
    private static final String COMPLETED_STATE_STRING = "completed";
    private static final String NOT_RUNNING_STATE_STRING = "not running";
    private static final String NOT_STARTED_STATE_STRING = "not started";
    private static final String OPEN_STATE_STRING = "open";
    private static final String RUNNING_STATE_STRING = "running";
    private static final String SUSPENDED_STATE_STRING = "suspended";
    private static final String TERMINATED_STATE_STRING = "terminated";
    private static final String UNKNOWN_STATE_STRING = "unknown";

    private static final EventType[] removals = new EventType[0];
    private static final EventType[] additions = new EventType[]
    { 
	  new EventType("org.omg.session","process_state")
    };

    //=========================================================================
    // state
    //=========================================================================

    protected Task task;

    private task_state taskState;

    private List list;
    private List features;
    private LinkCollection consumedList;
    private LinkCollection producedList;
    private LinkTableModel consumedModel;
    private LinkTableModel producedModel;

    private List actions;
    private Action startAction;
    private Action suspendAction;
    private Action stopAction;

    private ProcessorModelAgent model;

    //=================================================================
    // Disposable operations
    //=================================================================

    //================================================================
    // Initializable
    //================================================================
    
   /**
    * Initialization is invoked by the framework following instance creation
    * and contextualization.  
    * @osm.note task actions need to be updated to track changes in task state
    */
    public void initialize()
    throws Exception
    {        
        super.initialize();

	  //
	  // add actions to control task state
	  //

	  startAction = new TaskStartAction( getShell(), "Start", this );
	  suspendAction = new TaskSuspendAction( getShell(), "Suspend", this );
	  stopAction = new TaskStopAction( getShell(), "Stop", this );

        //
        // set event subscriptions
	  //

	  //try
	  //{
        //    if( this.adapter != null ) adapter.update( additions, removals );
	  //}
	  //catch( Exception e )
	  //{
	//	ExceptionHelper.printException(
	//	  "Unable to establish subscription.", e, this, true );
	  //}
    }

    //=========================================================================
    // Agent
    //=========================================================================

   /**
    * Set the resource that is to be presented.
    */
    public void setPrimary( Object value ) 
    {
	  super.setPrimary( value );
        try
        {
	      this.task = TaskHelper.narrow( (org.omg.CORBA.Object) value );
        }
        catch( Exception local )
        {
            throw new RuntimeException( 
		  "TaskAgent. Bad primary object reference.", local );
        }
    }

   /**
    * The <code>getType</code> method returns a human-friendly name of the entity.
    */
    public String getType( )
    {
	  return "Task";
    }

    //=========================================================================
    // RemoteEventListener
    //=========================================================================

   /**
    * Method invoked when an an event has been received from a 
    * remote source.  The agent handles <code>process_state</code> changes
    * by updating the corresponding property value and propergating this
    * to property event listeners.
    */
    public void remoteChange( RemoteEvent event )
    {
	  if( event.getDomain().equals("org.omg.session") )
	  {
		if( event.getType().equals("process_state") )
		{
		    taskState = task_stateHelper.extract( event.getProperty("task_state"));
		    putValue("state", taskState );
		    putValue("stateString", getState() );
		    final String debug = "updated task state from remote event to: ";
                if( getLogger().isDebugEnabled() ) getLogger().debug( debug + getState() );
		    return;
		}
	  }
        super.remoteChange( event );
    }

    //=========================================================================
    // TaskAgent
    //=========================================================================

    public UserAgent getOwner()
    {
	  try
	  {
		return (UserAgent) getResolver().resolve( task.owned_by() );
        }
        catch( Throwable e )
        {
            throw new RuntimeException("unable to resolve owning user.", e );
        }
    }

    public task_state getTaskState()
    {
	  if( taskState != null ) return taskState;
	  taskState = task.get_state();
        putValue("stateString", getState() );
        putValue("state", taskState );
        return taskState;
    }

    public String getDescription()
    {
	  try
	  {
            return task.description();
        }
        catch( Throwable e )
        {
            return null;
        }
    }

    public String getState()
    {
	  task_state state = getTaskState();
        if( state == null ) return UNKNOWN_STATE_STRING;
	  if( state == task_state.closed )
        {
            return CLOSED_STATE_STRING;
        }
        else if( state == task_state.completed )
        {
            return COMPLETED_STATE_STRING;
        }
        else if( state == task_state.not_running )
        {
            return NOT_RUNNING_STATE_STRING;
        }
        else if( state == task_state.notstarted )
        {
            return NOT_STARTED_STATE_STRING;
        }
        else if( state == task_state.open )
        {
            return OPEN_STATE_STRING;
        }
        else if( state == task_state.running )
        {
            return RUNNING_STATE_STRING;
        }
        else if( state == task_state.suspended )
        {
            return SUSPENDED_STATE_STRING;
        }
        else if( state == task_state.terminated )
        {
            return TERMINATED_STATE_STRING;
        }
        return UNKNOWN_STATE_STRING;
    }

   /**
    * Returns the processor model attached to the processor that is currently bound to 
    * the Task (i.e. in effect the Task is acting as a proxy to the processor relative to the
    * client).
    */
    public ProcessorModelAgent getModel( )
    {
        if( model != null ) return model;
        try
        {
	      model = getProcessor().getModel();
		model.setTask( this );
		return model;
        }
        catch( Throwable e )
        {
            throw new RuntimeException( "Unable to resolve processor model.", e );
        }
    }

   /**
    * Return the processor attached to the Task.
    */
    public ProcessorAgent getProcessor()
    {
	  try
        {
            AbstractResource r = task.get_processor();
	      return (ProcessorAgent) getResolver().resolve( r );		
	  }
	  catch( Throwable e )
        {
            throw new RuntimeException( "Unable to resolve processor.", e );
        }
    }

    public net.osm.util.List getConsumed()
    {
	  if( consumedList != null ) return consumedList;
	  try
	  {
            consumedList = new LinkCollection( 
	        getLogger().getChildLogger("consuming"), 
		  getOrb(), getResolver(), task, audit, 
		  ConsumesHelper.type(), Consumes.class 
            );
		return consumedList;
	  }
	  catch( Exception e )
	  {
		final String error = "unable to create consumed resource collection";
            if( getLogger().isErrorEnabled() ) getLogger().error( error, e );
		throw new RuntimeException( error, e );
	  }
    }

    private LinkTableModel getConsumedModel()
    {
        if( consumedModel != null ) return consumedModel;
        consumedModel = new LinkTableModel( (LinkCollection) getConsumed( ) );
	  return consumedModel;
    }

   /**
    * Returns a possibly empty list of the resources produced by this task.
    * @return List of <code>LinkAgent</code> instances.
    */
    public net.osm.util.List getProduced()
    {
	  if( producedList != null ) return producedList;
	  try
	  {
            producedList = new LinkCollection( 
              getLogger().getChildLogger("producing"), 
		  getOrb(), getResolver(), task, audit, 
		  ProducesHelper.type(), Produces.class 
            );
		return producedList;
	  }
	  catch( Exception e )
	  {
		final String error = "unable to create produced resource collection";
            if( getLogger().isErrorEnabled() ) getLogger().error( error, e );
		throw new RuntimeException( error, e );
	  }
    }

    private LinkTableModel getProducedModel()
    {
        if( producedModel != null ) return producedModel;
	  producedModel = new LinkTableModel( (LinkCollection) getProduced( ) );
	  return producedModel;
    }

   /**
    * Starts a task.
    */
    public void start() throws Exception
    {
	  task.start();
    }

   /**
    * Suspends a task.
    */
    public void suspend() throws Exception
    {
	  task.suspend();
    }

   /**
    * Stops a task.
    */
    public void stop() throws Exception
    {
	  task.stop();
    }

   /**
    * Add a resource to this task under a supplied tag.
    * @param resource the resource to add under a consumption association
    * @param tag the usage tag value
    */
    public void addConsumed( AbstractResourceAgent agent, String tag )
    {
        addConsumed( getModel().lookupDescriptor( tag ), agent );
    }

   /**
    * Add a resource to this task under a supplied tag.
    * @param resource the resource to add under a consumption association
    * @param tag the usage tag value
    */
    public void addConsumed( UsageDescriptorAgent usage, AbstractResourceAgent agent )
    {
        try
        {
		synchronized( usage )
	      {
                task.add_consumed( agent.getAbstractResource(), usage.getTag() );
	          usage.setAssignment( (UsageAgent) getResolver().resolve( 
		      new Consumes( agent.getAbstractResource(), usage.getTag() ) ) );
		    //putValue("verification", new Boolean( getModel().verify() ) );
		}
	  }
	  catch( Exception e )
	  {
		throw new RuntimeException(
		  "Unable to bind resource to the task due to a remote exception.", e );
	  }
    }

   /**
    * Removes a resource from this task.
    * @param tag the usage tag value
    */
    public void removeConsumed( String tag )
    {
        try
        {
            task.remove_consumed( tag );
		putValue("verification", new Boolean( getModel().verify() ) );
	  }
	  catch( Exception e )
	  {
		throw new RuntimeException(
		  "Unable to retract consumed resource due to a remote exception.", e );
	  }
    }

    //=========================================================================
    // Entity
    //=========================================================================

   /**
    * Returns a list of Action instances to be installed as 
    * action menu items within the desktop when the entity 
    * is selected.
    */
    public List getActions( )
    {
	  if( actions != null ) return actions;
	  actions = super.getActions();
	  actions.add( startAction );
	  actions.add( suspendAction );
	  actions.add( stopAction );
        return actions;
    }

   /**
    * The <code>getStandardViews</code> operation returns a sequence of panels 
    * representing different views of the content and/or associations maintained by
    * and agent.
    */

    public List getPropertyPanels()
    {
	  //
	  // get all of the supertype panels
	  // together with the local panels
        //

	  if( list == null )
        {
	      list = super.getPropertyPanels();
		ScrollView view = new ScrollView( 
                new UsageTablePanel( 
			this, "Usage", new UsageTableModel( getModel() ), 
		    	UsageTableModel.createTableColumnModel()
		    )
		);
		view.setPreferred( true );
		list.add( view );
	  }
        return list;
    }

   /**
    * Returns a list of <code>Features</code> instances to be presented under 
    * the Properties dialog features panel.
    * @return List the list of <code>Feature</code> instances
    */
    public List getFeatures()
    {
        if( features != null ) return features;

        List list = super.getFeatures();
	  try
	  {
            list.add( new ActiveFeature( this, "state", "getState", "state" ));
        }
	  catch( Exception e )
	  {
	      return list;
	  }
	  this.features = list;
	  return features;
    }

   /**
    * The <code>dispose</code> method is invoked prior to removal of the 
    * agent.  The implementation handles cleaning-up of state members.
    */
    public void dispose()
    {
        if( getLogger().isDebugEnabled() ) getLogger().debug("dispose");

        try
        {
            getProcessor().dispose();
        }
        catch( Throwable anything ){}

        try
        {
            if( consumedList != null ) consumedList.dispose();
        }
        catch( Throwable anything ){}

        try
        {
        if( producedList != null ) producedList.dispose();
        }
        catch( Throwable anything ){}

        list = null;
        features = null;
        consumedList = null;
        producedList = null;
        consumedModel = null;
        producedModel = null;
        startAction = null;
        suspendAction = null;
        stopAction = null;
        model = null;

	  super.dispose();

    }
}
