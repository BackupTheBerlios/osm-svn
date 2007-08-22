
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
import org.omg.Session.SystemMessagesHolder;
import org.omg.Session.Desktop;
import org.omg.Session.SystemMessageIterator;
import org.omg.Session.User;
import org.omg.Session.UserHelper;
import net.osm.hub.home.Principal;
import net.osm.hub.home.PrincipalHelper;

import net.osm.agent.iterator.CollectionIterator;

public class PrincipalAgent extends UserAgent
{

    //=========================================================================
    // Members
    //=========================================================================

    protected net.osm.hub.home.Principal user;

    //=========================================================================
    // Constructor
    //=========================================================================

    public PrincipalAgent( )
    {
	  super();
    }

    public PrincipalAgent( ORB orb, net.osm.hub.home.Principal reference )
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
	  super.setReference( value );
        try
        {
	      this.user = PrincipalHelper.narrow( (org.omg.CORBA.Object) value );
        }
        catch( Exception local )
        {
            throw new CascadingRuntimeException( "Bad primary object reference.", local );
        }
    }

    //=========================================================================
    // Getter Operations
    //=========================================================================

    public AgentIterator getMessages()
    {
        try
        {
		SystemMessageIterator messages = user.get_messages( 0, new SystemMessagesHolder() );
		return new CollectionIterator( orb, messages, true );
        }
        catch( Exception e )
        {
            throw new CascadingRuntimeException( "Message iterator invalid.", e );
        }
    }
}
