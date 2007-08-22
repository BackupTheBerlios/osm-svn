
package net.osm.agent;

import javax.servlet.ServletContext;
import javax.servlet.jsp.*;
import javax.servlet.jsp.tagext.*;
import java.io.IOException;
import java.util.List;
import java.util.Arrays;
import java.util.Date;
import org.apache.avalon.framework.CascadingRuntimeException;
import org.omg.CORBA.Any;
import org.omg.CORBA.AnyHolder;
import org.omg.CORBA.BooleanHolder;
import org.omg.CORBA.TypeCode;
import org.omg.CORBA.ORB;
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
import org.omg.CollaborationFramework.ProcessorHelper;

import net.osm.agent.iterator.CollectionIterator;

public class TaskAgent extends AbstractResourceAgent
{

    //=========================================================================
    // Constants
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

    //=========================================================================
    // Members
    //=========================================================================

    protected Task task;


    //=========================================================================
    // Constructor
    //=========================================================================

    public TaskAgent(  )
    {
	  super();
    }

    public TaskAgent( ORB orb, Task reference )
    {
        super( orb, reference );
	  this.task = task;
    }

    //=========================================================================
    // Configuration Operations
    //=========================================================================

   /**
    * Set the resource that is to be presented.
    */
    public void setReference( Object value ) 
    {
	  super.setReference( value );
        try
        {
	      this.task = TaskHelper.narrow( (org.omg.CORBA.Object) value );
        }
        catch( Exception local )
        {
            throw new CascadingRuntimeException( "Bad primary object reference.", local );
        }
    }

    //=========================================================================
    // Operations
    //=========================================================================

    public UserAgent getOwner()
    {
	  try
	  {
            return new UserAgent( orb, task.owned_by() );
        }
        catch( Throwable e )
        {
            return null;
        }
    }

    public task_state getTaskState()
    {
	  try
	  {
            return task.get_state();
        }
        catch( Throwable e )
        {
            return null;
        }
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
        try
        {
	      AbstractResourceAgent r = getProcessor();
	      if( r instanceof ProcessorAgent ) return ((ProcessorAgent)r).getModel();
            return null;
        }
        catch( Throwable e )
        {
            return null;
        }
    }

   /**
    * Return the processor attached to the Task.
    */
    public AbstractResourceAgent getProcessor()
    {
	  try
        {
            AbstractResource r = task.get_processor();
	      return (AbstractResourceAgent) AgentServer.getAgentService().resolve( r );		
	  }
	  catch( Throwable e )
        {
            throw new CascadingRuntimeException( "TaskAgent:getProcessor", e );
        }
    }

    public AgentIterator getConsumed()
    {
        try
        {
		AbstractResourcesHolder holder = new AbstractResourcesHolder();
		AbstractResourceIteratorHolder iteratorHolder = new AbstractResourceIteratorHolder();
		LinkIteratorHolder linksHolder = new LinkIteratorHolder();
		task.list_consumed( 0, holder, iteratorHolder, linksHolder );
		return new CollectionIterator( orb, iteratorHolder.value );
        }
        catch( Exception e )
        {
            throw new CascadingRuntimeException( "Consumed iterator invalid.", e );
        }
    }

    public AgentIterator getProduced()
    {
        try
        {
		AbstractResourcesHolder holder = new AbstractResourcesHolder();
		AbstractResourceIteratorHolder iteratorHolder = new AbstractResourceIteratorHolder();
		LinkIteratorHolder linksHolder = new LinkIteratorHolder();
		task.list_produced( 0, holder, iteratorHolder, linksHolder );
		return new CollectionIterator( orb, iteratorHolder.value );
        }
        catch( Exception e )
        {
            throw new CascadingRuntimeException( "Produced iterator invalid.", e );
        }
    }
}
