
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
import org.omg.CORBA.ORB;
import org.omg.Session.AbstractResource;
import org.omg.Session.Workspace;
import org.omg.Session.WorkspacesHolder;
import org.omg.Session.WorkspaceIteratorHolder;
import org.omg.Session.TasksHolder;
import org.omg.Session.TaskIteratorHolder;
import org.omg.Session.SystemMessage;
import org.omg.Session.SystemMessageHolder;
import org.omg.Session.Desktop;
import org.omg.Session.User;
import org.omg.Session.UserHelper;
import org.omg.Session.connect_state;

import net.osm.agent.iterator.CollectionIterator;


public class UserAgent extends AbstractResourceAgent
{

    //=========================================================================
    // Members
    //=========================================================================

    protected User user;

    protected DesktopAgent desktop;

    //=========================================================================
    // Constructor
    //=========================================================================

    public UserAgent( )
    {
	  super();
    }

    public UserAgent( ORB orb, User reference )
    {
        super( orb, reference );
	  try
        {
	      this.user = reference;
        }
        catch( Exception e )
        {
            throw new CascadingRuntimeException( "Bad type.", e );
        }
    }

    //=========================================================================
    // Configuration Operations
    //=========================================================================

   /**
    * Set the resource that is to be presented.
    */
    public void setReference( Object value ) 
    {
	  try
        {
	      super.setReference( value );
 	      this.user = UserHelper.narrow( (org.omg.CORBA.Object) value );
        }
        catch( Exception e )
        {
            throw new CascadingRuntimeException( "Bad type.", e );
        }
    }

    //=========================================================================
    // Getter Operations
    //=========================================================================

    public boolean getConnected()
    {
        try
	  {
            return ( user.connectstate() == connect_state.connected );
 	  }
	  catch( Throwable e )
	  {
		e.printStackTrace();
	      throw new CascadingRuntimeException( "Unexpected exception while resolving connected state.", e );
	  }       
    }

    public DesktopAgent getDesktop()
    {
	  if( desktop != null ) return desktop;
	  Desktop d = null;
	  try
	  {
	      d = user.get_desktop();
	  }
	  catch( Throwable de )
	  {
		de.printStackTrace();
	      throw new CascadingRuntimeException( "User agent failed to resolve remote desktop reference.", de );
	  }
	  try
	  {
		desktop = new DesktopAgent( orb, d );
            return desktop;
        }
        catch( Throwable ae )
        {
	      throw new CascadingRuntimeException( "User agent failed to establish the DesktopAgent", ae );
        }
    }

    public AgentIterator getTasks()
    {
        try
        {
		TasksHolder holder = new TasksHolder();
		TaskIteratorHolder iteratorHolder = new TaskIteratorHolder();
		user.list_tasks( 0, holder, iteratorHolder );
		return new CollectionIterator( orb, iteratorHolder.value );
        }
        catch( Exception e )
        {
            throw new CascadingRuntimeException( "Task iterator invalid.", e );
        }
    }

    public AgentIterator getWorkspaces()
    {
        try
        {
		WorkspacesHolder holder = new WorkspacesHolder();
		WorkspaceIteratorHolder iteratorHolder = new WorkspaceIteratorHolder();
		user.list_workspaces( 0, holder, iteratorHolder );
		return new CollectionIterator( orb, iteratorHolder.value );
        }
        catch( Exception e )
        {
            throw new CascadingRuntimeException( "Workspace iterator invalid.", e );
        }
    }

    //=========================================================================
    // Commands
    //=========================================================================

    public void doEnqueue( SystemMessage message )
    {
        try
        {
		user.enqueue( message );
        }
        catch( Exception e )
        {
            throw new CascadingRuntimeException( "Message enqueue failed.", e );
        }
    }
}
