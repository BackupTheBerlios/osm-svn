
package net.osm.agent;

import org.apache.avalon.framework.CascadingRuntimeException;

import org.omg.CORBA.Any;
import org.omg.CORBA.AnyHolder;
import org.omg.CORBA.BooleanHolder;
import org.omg.CORBA.TypeCode;
import org.omg.CORBA.ORB;
import org.omg.Session.Workspace;
import org.omg.Session.WorkspaceHelper;
import org.omg.Session.AbstractResource;
import org.omg.Session.AbstractResourcesHolder;
import org.omg.Session.AbstractResourceIteratorHolder;
import org.omg.Session.AbstractResourceHelper;
import org.omg.Session.AccessedByHelper;

import net.osm.agent.iterator.CollectionIterator;


public class WorkspaceAgent extends AbstractResourceAgent
{

    //=========================================================================
    // Members
    //=========================================================================

    protected Workspace workspace;

    //=========================================================================
    // Constructor
    //=========================================================================

    public WorkspaceAgent( )
    {
	  super();
    }

    public WorkspaceAgent( ORB orb, Workspace reference )
    {
        super( orb, reference );
	  this.workspace = reference;
    }

    //=========================================================================
    // Operations
    //=========================================================================

   /**
    * Set the resource that is to be presented.
    */
    public void setReference( Object value ) 
    {
	  super.setReference( value );
	  try
        {
	      this.workspace = WorkspaceHelper.narrow( (org.omg.CORBA.Object) value );
        }
        catch( Exception local )
        {
            throw new CascadingRuntimeException( "Bad primary object reference.", local );
        }
    }

   /**
    * The getResources operation is a synonym for the getContent method.
    * @see #getContents
    */
    public AgentIterator getResources()
    {
        return this.getContents( );
    }


    public AgentIterator getContents()
    {
        return this.getContents( AbstractResourceHelper.type() );
    }

    public AgentIterator getContents( TypeCode type )
    {
        AbstractResourcesHolder holder = new AbstractResourcesHolder();
	  AbstractResourceIteratorHolder iteratorHolder = new AbstractResourceIteratorHolder();
        try
        {
		workspace.list_resources_by_type( type, 0, holder, iteratorHolder );
        }
        catch( Exception remote )
        {
            throw new CascadingRuntimeException( "Remote exception while expanding iterator.", remote );
        }
        try
        {
		return new CollectionIterator( orb, iteratorHolder.value );
        }
        catch( Exception local )
        {
            throw new CascadingRuntimeException( "Local exception while establishing AgentIterator.", local );
        }
    }

    public AgentIterator getAccessedBy( )
    {
        return getLinks( AccessedByHelper.type() );
    }

}
