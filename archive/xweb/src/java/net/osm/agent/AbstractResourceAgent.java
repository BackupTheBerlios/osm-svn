
package net.osm.agent;

import java.util.Date;
import org.apache.avalon.framework.CascadingRuntimeException;
import org.omg.CORBA.TypeCode;
import org.omg.CORBA.ORB;
import org.omg.Session.LinksHelper;
import org.omg.Session.LinksHolder;
import org.omg.Session.LinkIterator;
import org.omg.Session.AbstractResourceHelper;
import org.omg.Session.WorkspacesHolder;
import org.omg.Session.WorkspacesHolder;
import org.omg.Session.WorkspaceIteratorHolder;
import org.omg.Session.TasksHolder;
import org.omg.Session.TaskIteratorHolder;
import org.omg.Session.BaseBusinessObjectKey;

import net.osm.service.time.TimeUtils;
import net.osm.audit.home.Adapter;
import net.osm.agent.iterator.CollectionIterator;

/**
 * The <code>AbstractResourceAgent</code> is a business agent supporting the 
 * abstract busines object org.omg.Session.AbstractResource.  The 
 * AbstractResourceAgent is backed by a persistent object adapter that 
 * maintains a event subcription with the remote business object (see 
 * AbstractResource below).  This implementation provides operations 
 * related the resource name, access to resource creation, modification 
 * and last access dates, the resource kind (IDL identifier),
 * access to iterators exposing TaskAgent instances consuming this resource 
 * and a WorkspaceAgent instances within which this agent is referenced.  
 * AbstractResourceAgent serves as a base class for the UserAgent, WorkspaceAgent, 
 * TaskAgent, CommunityAgent and ProcessorAgents.
 *
 * An AbstractResourceAgent (refered to here as a resource) is backed by a 
 * transactional and persistent CORBA object and is contained in one or more 
 * WorkspaceAgent instances. They may be selected, consumed and produced by 
 * TaskAgents. Resources are found and selected by tools and facilities that 
 * present lists of candidate resources. These lists may be filtered by things 
 * like security credentials, by type, and by implementation. Task and workspace 
 * are dependent on the resources they use and contain. Implementations are 
 * required to notify task and workspace of changes and defer deletion requests 
 * until all linked tasks signal their readiness to handle. 
 */

public class AbstractResourceAgent extends AbstractAgent implements ActiveAgent
{

   /**
    * The current Object Request Broker.
    */
    protected ORB orb;

   /**
    * The object reference to the AbstractResource that this agents 
    * represents.
    */
    protected org.omg.Session.AbstractResource reference;

   /**
    * Locally cached identifier of the resource.
    */
    protected BaseBusinessObjectKey key;

   /**
    * IDC adapter that handles event subscriptions and recieption of remote 
    * notification of events.
    */
    protected Adapter adapter;

   /**
    * Cache reference to kind IDL string.
    */
    protected String kind;

   /**
    * Cached reference to the stringified IOR.
    */
    protected String ior;

   /**
    * Cached reference to the creation date.
    */
    protected Date creation;

    //=========================================================================
    // Constructor
    //=========================================================================


   /**
    * Default constructor.
    */
    public AbstractResourceAgent(  )
    {
    }

   /**
    * Convinence constructor to create a new AbstractResourceAgent based on a supplied
    * orb and AbstractResource reference.
    * @param orb object request broker
    * @param object reference to the primary org.omg.Session.AbstractResource
    * @see org.omg.Session.AbstractResource
    */
    public AbstractResourceAgent( ORB orb, Object reference )
    {
	  if( orb == null ) throw new RuntimeException(
		"AbstractResourceAgent - null orb supplied to agent constructor.");
	  if( reference == null ) throw new RuntimeException(
		"AbstractResourceAgent - null reference supplied to agent constructor");

        this.orb = orb;
	  try
        {
		setReference( reference );
        }
        catch( Exception local )
        {
            throw new CascadingRuntimeException( "Bad primary object reference.", local );
        }
    }


    //=========================================================================
    // Atrribute setters
    //=========================================================================

   /**
    * Setter method for establishing the current ORB.
    * @param value the ORB
    */
    public void setOrb( ORB value ) 
    {
	  if( value == null ) throw new RuntimeException("Null value supplied as an argument to setOrb.");
	  this.orb = value;
    }

   /**
    * The <code>setReference</code> operation set the princial object that the agent 
    * will represent.  Typically, the object passed to this argument will be a CORBA
    * object reference of a business object, or valuetype related to the business 
    * object.
    * @param value the pricipal object
    */
    public void setReference( Object value ) 
    {
	  if( value == null ) throw new RuntimeException(
		"AbstractResourceAgent/setReference - null value supplied.");
        try
	  {
	      this.reference = AbstractResourceHelper.narrow( (org.omg.CORBA.Object) value );
            this.key = this.reference.get_key();
        }
        catch( Exception e )
        {
		throw new RuntimeException(
		"AbstractResourceAgent/setReference - object is not an AbstractResource.");
        }
    }

   /**
    * The <code>setAdapter</code> operation is used to declare the Adapter 
    * instance to be assigned to the agent.  The supplied adapter provides
    * the implementation of the mechanisms enabling event subscription, 
    * subscription modification, event reception, and forwarding of local 
    * events to associated agents.  An implementation of setAdapter is 
    * responsible for establishing the agents internal reference to the primary
    * resource. 
    * @param value the adapter supporting event subscription and incomming event
    * @see net.osm.agent.Agent#setReference
    */
    public void setAdapter( Adapter value ) 
    {
	  if( value == null ) throw new RuntimeException("Cannot set adapter to a null value.");
        try
	  {
	      this.adapter = value;
		setReference( value.principal() );
        }
        catch( Exception e )
        {
		throw new CascadingRuntimeException("Unexpected exception while setting adapter.", e );
        }
    }

   /**
    * Test that returns true if the agents principal object is equal to the 
    * supplied object.
    * @param object object to compare against this agents primary object
    * @see #setReference
    */
    public boolean equals( org.omg.Session.AbstractResource resource )
    {
        return this.key.equal( resource.get_key() );
    }


    //=========================================================================
    // Getter methods
    //=========================================================================

   /**
    * The <code>getName</code> method returns the name of the resource.
    * @returns String the name of the resource
    */

    public String getName()
    {
	  try
        {
            return reference.name();
	  }
	  catch( Throwable e )
        {
            throw new CascadingRuntimeException( "Failed to get the resource name.", e );
        }
    }

   /**
    * The <code>setName</code> method sets the name of the resource to the supplied value.
    * @param name the name to assign to the resource
    */

    public void setName( String name )
    {
	  try
        {
            reference.name( name );
	  }
	  catch( Throwable e )
        {
            throw new CascadingRuntimeException( "Failed to set resource name.", e );
        }
    }

   /**
    * The <code>getKind</code> method returns the IDL identifier of the org.omg.Session.AbstractResource
    * backing the agent.
    * @returns String IDL identifier
    */

    public String getKind( )
    {
	  if( kind != null ) return kind;
	  try
        {
            kind = reference.resourceKind().id().trim();
		return kind;
	  }
	  catch( Throwable e )
        {
            throw new CascadingRuntimeException( "AbstractResourceAgent:getKind", e );
        }
    }

   /**
    * The <code>getType</code> method returns a human-friendly name derived from the IDL identifier.
    * @returns String short human-friendly identifier
    */

    public String getType( )
    {
	  if( type != null ) return type;
	  try
        {
		getKind();
            type = kind.substring( kind.lastIndexOf("/") + 1, kind.lastIndexOf(":")).toLowerCase();
		return type;
	  }
	  catch( Throwable e )
        {
            throw new CascadingRuntimeException( "AbstractResourceAgent:getKind", e );
        }
    }

   /**
    * Returns the resource random identifier that identifies this resource within the scope of 
    * the resource domain address.
    * @returns int constant random identifier
    * @see #getDomain
    */
    public int getRandom( )
    {
	  try
        {
            return reference.constant_random_id();
 	  }
	  catch( Throwable e )
        {
            throw new CascadingRuntimeException( "Unexpected remote exception while resolving random.", e );
        }
   }

   /**
    * The <code>getDomain</code> method returns the org.omg.Session.AbstractResource domain authority 
    * and address as a string.
    * @returns String strigified domain address
    */
    public String getDomain( )
    {
	  try
        {
            return reference.domain().naming_entity;
 	  }
	  catch( Throwable e )
        {
            throw new CascadingRuntimeException( "Unexpected remote exception while resolving domain.", e );
        }
    }

   /**
    * The <code>getCreation</code> method return the creation date of the org.omg.Session.AbstractResource 
    * backing the agent.
    * @returns Date creation date
    */
    public Date getCreation(  )
    {
	  try
        {
	      if( creation == null ) creation = TimeUtils.convertToDate( reference.creation() );
            return creation;
 	  }
	  catch( Throwable e )
        {
            throw new CascadingRuntimeException( "AbstractResourceAgent:getCreation", e );
        }
    }

   /**
    * The <code>getModification</code> method return the modification date of the org.omg.Session.AbstractResource 
    * backing the agent.
    * @returns Date modification date
    */
    public Date getModification(  )
    {
	  try
        {
	      return TimeUtils.convertToDate( reference.modification() );
 	  }
	  catch( Throwable e )
        {
            throw new CascadingRuntimeException( "AbstractResourceAgent:getModification", e );
        }
    }

   /**
    * The <code>getAccess</code> method return the last access date of the org.omg.Session.AbstractResource 
    * backing the agent.
    * @returns Date last access date
    */
    public Date getAccess(  )
    {
	  try
        {
	      return TimeUtils.convertToDate( reference.access() );
 	  }
	  catch( Throwable e )
        {
            throw new CascadingRuntimeException( "AbstractResourceAgent:getAccess", e );
        }
    }

   /**
    * The <code>getProducer</code> method returns a TaskAgent that is producing this resource, or, 
    * null if no Task is currently associated as producer.  Any resource may be associated with 
    * at most 1 producing resource. 
    * @returns TaskAgent task agent producing this resource
    */
    public TaskAgent getProducer()
    {
	  try
	  {
            return new TaskAgent( orb, reference.get_producer() );
        }
        catch( Throwable e )
        {
            return null;
        }
    }

   /**
    * The <code>getConsumers</code> method returns an iterator of Tasks consuming this resource.
    * Resources may be simultaneoulsy consumed by any number of Tasks.
    * @returns AgentIterator iterator of consuming tasks
    */
    public AgentIterator getConsumers()
    {
        try
        {
		TasksHolder tasksHolder = new TasksHolder();
		TaskIteratorHolder iteratorHolder = new TaskIteratorHolder();
		reference.list_consumers( 0, tasksHolder, iteratorHolder );
		return new CollectionIterator( orb, iteratorHolder.value );
        }
        catch( Throwable e )
        {
            throw new CascadingRuntimeException( "Task iterator invalid.", e );
        }
    }

   /**
    * The <code>getContainers</code> method returns an iterator of Workspaces that this resource
    * is reference within. Resources may be simultaneoulsy contained by any number of Workspaces.
    * @returns AgentIterator iterator of Workspaces referencing this resoruce.
    */
    public AgentIterator getContainers()
    {
        try
        {
		WorkspacesHolder workspacesHolder = new WorkspacesHolder();
		WorkspaceIteratorHolder iteratorHolder = new WorkspaceIteratorHolder();
		reference.list_contained( 0, workspacesHolder, iteratorHolder );
		return new CollectionIterator( orb, iteratorHolder.value );
        }
        catch( Throwable e )
        {
            throw new CascadingRuntimeException( "Workspace iterator invalid.", e );
        }
    }

   /**
    * The <code>getLinks</code> operation provides for client navigation of busienss 
    * agent associations such as usage by tasks, workspace containment, ownership, composition
    * and so forth.  The type of links returned by this operation is dependent on the type 
    * of agent that the operation is applied against, and may be constraind by the supplied 
    * typecode (corresponding to the type of Link to include in the returned iterator).
    *
    * @param tc type code of a Link or class derived from Link.
    * @returns AgentIterator iterator of LinkAgent instances
    */
    public AgentIterator getLinks( TypeCode tc )
    {
        try
        {
		LinksHolder holder = new LinksHolder();
		return new CollectionIterator( orb, reference.expand( tc, 0, holder ), true );
        }
        catch( Throwable e )
        {
            throw new CascadingRuntimeException( "Link iterator invalid.", e );
        }
    }

   /**
    * The <code>getIor</code> method returns the interoperable object reference to the 
    * org.omg.Session.AbstractResource backing the business agent.
    */
    public String getIor( ) 
    {
	  if( ior != null ) return ior;
	  try
        {
            ior = orb.object_to_string( reference );
		return ior;
 	  }
	  catch( Throwable e )
        {
            throw new CascadingRuntimeException( "TaskAgent:getIor", e );
        }
    }

   /**
    * The <code>remove</code> method request the destruction of the business object
    * backing this adapter, resulting in the possible retraction of the objects 
    * associations with other resources.  If the resource is currently in use by 
    * task as a consumed or produced resource, the remove operation will throw an
    * exception.
    */

    public void remove() throws CannotRemoveException
    {
	  try
        {
            reference.remove();
		this.dispose();
        }
	  catch( Exception e )
        {
	      throw new CannotRemoveException("Remote exception while attempting to remove the agent.",e);	
        }
    }


    //=================================================================
    // ActiveAgent operations
    //=================================================================

   /**
    * The <code>connect</code> operation requests the establishment of a 
    * a connection between the agent adapter and the remote primary event producer.
    * The <code>connect</code> operation must be explicity invoked and an initial 
    * subscription declared before event reception will occur.
    */
    public void connect()
    {
        if( adapter == null ) throw new RuntimeException("Cannot connect to a null adapter.");
	  try
	  {
            adapter.connect();
        }
        catch( Exception e )
        {
		throw new CascadingRuntimeException("Remote exception will attempting to commenct.", e );
        }
    }

   /**
    * The <code>disconnect</code> operation requests the termination of 
    * event reception by the agent (or more strictly speaking, disconnection of 
    * the adapter's asociuation to the event producer).
    */
    public void disconnect()
    {
        if( adapter == null ) throw new RuntimeException("Cannot disconnect with a null adapter.");
	  try
	  {
            adapter.disconnect();
        }
        catch( Exception e )
        {
		throw new CascadingRuntimeException("Remote exception will attempting to disconnect.", e );
        }
    }

    //
    // refresh
    //
   
    public void refresh()
    {
        throw new RuntimeException("NotImplemented");
    }

    //
    // getSubscription
    // -- needs to return array[][string, boolean]

    public void getSubscription()
    {
        throw new RuntimeException("NotImplemented");
    }

    //
    // setSubscription
    //

    public void setSubscription( String type )
    {
        throw new RuntimeException("NotImplemented");
    }
    
    //=================================================================
    // Disposable operations
    //=================================================================

   /**
    * The <code>dispose</code> method is invoked prior to removal of the 
    * agent.  The implementation handles cleaning-up of state members.
    */

    public void dispose()
    {
	  super.dispose();
        this.orb = null;
    }

}
