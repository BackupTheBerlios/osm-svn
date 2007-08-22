
package net.osm.agent;

import java.awt.Font;
import java.util.Date;
import java.util.List;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import javax.swing.ImageIcon;
import javax.swing.Action;

import org.apache.avalon.framework.component.Composable;
import org.apache.avalon.framework.component.ComponentManager;
import org.apache.avalon.framework.component.ComponentException;
import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.context.Contextualizable;
import org.apache.avalon.framework.context.Context;
import org.apache.avalon.framework.context.ContextException;


import org.omg.CORBA.TypeCode;
import org.omg.CORBA.ORB;
import org.omg.CosLifeCycle.NotRemovable;
import org.omg.CosNotification.EventType;
import org.omg.Session.LinksHelper;
import org.omg.Session.LinksHolder;
import org.omg.Session.LinkIterator;
import org.omg.Session.AbstractResourceHelper;
import org.omg.Session.WorkspacesHolder;
import org.omg.Session.WorkspaceIteratorHolder;
import org.omg.Session.TasksHolder;
import org.omg.Session.TaskIteratorHolder;
import org.omg.Session.BaseBusinessObjectKey;
import org.omg.Session.ConsumedBy;
import org.omg.Session.ConsumedByHelper;
import org.omg.Session.CollectedBy;
import org.omg.Session.CollectedByHelper;
import org.omg.Session.AbstractResource;
import org.omg.TimeBase.UtcT;
import org.omg.TimeBase.UtcTHelper;

import net.osm.agent.*;
import net.osm.agent.WorkspaceAgent;
import net.osm.agent.util.Collection;
import net.osm.audit.RemoteEvent;
import net.osm.audit.RemoteEventListener;
import net.osm.audit.home.Adapter;
import net.osm.audit.AuditService;
import net.osm.entity.EntityContext;
import net.osm.entity.EntityService;
import net.osm.shell.TablePanel;
import net.osm.shell.ActiveFeature;
import net.osm.shell.StaticFeature;
import net.osm.orb.ORBService;
import net.osm.shell.View;
import net.osm.shell.GenericAction;
import net.osm.shell.ScrollView;
import net.osm.time.TimeUtils;
import net.osm.util.ExceptionHelper;
import net.osm.util.IconHelper;

/**
 * The <code>AbstractResourceAgent</code> is a business agent supporting the 
 * abstract business object org.omg.Session.AbstractResource.  The 
 * AbstractResourceAgent is supported by a audit service that 
 * maintains a event subcription with the remote business object. This 
 * implementation provides operations related the resource name, access to 
 * resource creation, modification 
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
 *
 * @author Stephen McConnell
 */
public class AbstractResourceAgent extends AbstractAgent implements ActiveAgent, Contextualizable, Composable
{

    //==============================================================
    // static
    //==============================================================

    protected static final boolean trace = false;

    private static final String path = "net/osm/agent/image/resource.gif";
    private static final ImageIcon icon = IconHelper.loadIcon( path );

    private static final EventType[] removals = new EventType[0];
    private static final EventType[] additions = new EventType[]
    { 
	  new EventType("org.omg.session","update"),
	  new EventType("org.omg.session","remove"),
	  new EventType("org.omg.session","bind"), 
	  new EventType("org.omg.session","replace"), 
	  new EventType("org.omg.session","release")
    };

    //==============================================================
    // state
    //==============================================================

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
    * Cache reference to kind IDL string.
    */
    protected String kind;

   /**
    * Cached reference to the stringified IOR.
    */
    protected String ior;

   /**
    * Cached reference to the random identifier.
    */
    private int random;

   /**
    * Cached reference to the domain name.
    */
    private String domain;

   /**
    * Cached reference to the creation date.
    */
    protected Date creation;

   /**
    * Cached reference to the modification date.
    */
    protected Date modification;

   /**
    * Cached reference to the task that produced this resource.
    */
    private TaskAgent producer;

   /**
    * Flag to indicate if the producer task has been resolved.
    */
    private boolean producerAccessment = false;
    
    private List list;

   /**
    * Remove action.
    */
    private Action removeAction;

    private LinkCollection collectedByCollection;
    private LinkCollection consumedByCollection;

    private LinkTableModel collectedByModel;
    private LinkTableModel consumedByModel;

   /**
    * List of features.
    */
    private List features;

    protected AuditService audit;

    private boolean removed = false;

    //=========================================================================
    // Agent
    //=========================================================================

   /**
    * The <code>setPrimary</code> operation set the primary object that the agent 
    * will represent.  Typically, the object passed to this argument will be a CORBA
    * object reference of a business object, or valuetype related to the business 
    * object.
    *
    * @param value the remote object reference
    */
    public void setPrimary( Object value )
    {
	  if( value == null ) throw new RuntimeException(
	    "AbstractResourceAgent/setPrimary - null value supplied.");

        try
	  {
	      this.reference = AbstractResourceHelper.narrow( (org.omg.CORBA.Object) value );
        }
        catch( Throwable e )
        {
		final String error = "supplied primary type missmatch";
		if( getLogger().isErrorEnabled() ) getLogger().error( error + 
		  "\n\tSUPPLIED: " + value.getClass().getName(), e );
            throw new RuntimeException( error, e );
        }
    }

    //=================================================================
    // Contextualizable
    //=================================================================

    public void contextualize( Context context ) throws ContextException
    {
	  super.contextualize( context );
	  if( context instanceof EntityContext )
	  {
		setPrimary( ((EntityContext)context).getPrimary() );
	  } 
    }

    //=========================================================
    // Composable implementation
    //=========================================================

    public void compose( ComponentManager manager )
    throws ComponentException
    {
        super.compose( manager );
	  try
	  {
		audit = (AuditService) manager.lookup("AUDIT");
		orb = ((ORBService) manager.lookup("ORB")).getOrb();
        }
        catch( Exception e )
        {
		throw new ComponentException("unexpected exception during composition phase", e );
        }
    }

    //================================================================
    // Initializable
    //================================================================

   /**
    * Initialization is invoked by the framework following instance creation
    * and contextualization.  
    */
    public void initialize()
    throws Exception
    {
        super.initialize();

	  // the ORB is provided during the composition phase
        if( orb == null ) throw new Exception(
	    "orb has not been declared.");

        // the primary reference is established during the contextualization phase
        if( reference == null ) throw new Exception(
	    "primary resource has not been declared.");

        try
	  {
		audit.addRemoteEventListener( getAbstractResource(), this );
	  }
	  catch( Throwable e )
	  {
		final String error = "Could not initalize agent due to an audit related error.";
		throw new Exception( error, e );
	  }

        //
        // initial state
	  //

	  try
	  {
            super.setName( reference.name() );
            this.key = reference.get_key();
            this.random = reference.constant_random_id();
	  }
	  catch( Exception e )
	  {
		final String error = "Could not initalize agent state.";
		throw new Exception( error, e );
	  }
    }

    //=========================================================================
    // Entity
    //=========================================================================

   /**
    * Returns the <code>Action</code> object that will be handle
    * an action event signalling removal of the instance.
    * @return Action an <code>Action</code> that will remove the 
    * <code>Entity</code> 
    */
    public Action getRemoveAction()
    {
        if( removeAction != null ) return removeAction;
        removeAction = new GenericAction( "Remove " + getType(), this, "remove" );
        return removeAction;
    }

   /**
    * The <code>setName</code> method sets the name of the resource to the supplied value.
    * @param name the name to assign to the resource
    */
    public void setName( String name )
    {
        if(getLogger().isDebugEnabled()) getLogger().debug("setName");
	  try
        {
            reference.name( name );
            super.setName( reference.name() );
	  }
	  catch( Throwable e )
        {
            throw new RuntimeException( 
		  "AbstractResourceAgent. Unable to set resource name.", e 
            );
        }
    }

    //=========================================================================
    // RemoteEventListener
    //=========================================================================

   /**
    * Method invoked when an an event has been received from a 
    * remote source signalling a state change in the source
    * object.
    */
    public void remoteChange( RemoteEvent event )
    {
	  if( event.getDomain().equals("org.omg.session") )
	  {
		String type = event.getType();
		if( !type.equals("remove") )
		{
		    if( event.getType().equals("update") )
		    {
		        String feature = event.getProperty("feature").extract_string();
		        if( feature.equals( "name" ))
		        {
			      String name = event.getProperty("new").extract_string();
                        super.setName( name );
		        }
		    }
		    UtcT timestamp = UtcTHelper.extract( event.getProperty("timestamp") );
                modification = TimeUtils.convertToDate( timestamp );
		    putValue("modification", modification );
		}
	      else
	      {
		    if(getLogger().isDebugEnabled()) getLogger().debug("EVENT REMOVE: " + this );
		    removed = true;
		    dispose();
		}
	  }
    }

    //=========================================================================
    // ActiveAgent
    //=========================================================================

   /**
    * Setter method for establishing the current ORB.
    * @param value the ORB
    */
    public void setOrb( ORB value ) 
    {
	  if( value == null ) throw new RuntimeException(
          "Null value supplied as an argument to setOrb.");
	  this.orb = value;
    }

   /**
    * Return the current orb.
    */
    public ORB getOrb()
    {
        return orb;
    }

    public boolean getActive()
    {
        return audit.getConnected( getAbstractResource() );
    }

    //=========================================================================
    // AbstractResourceAgent
    //=========================================================================

   /**
    * Returns the primary object reference in the form of an AbstractResource.
    * @return AbstractResource the resource backing the agent
    */
    public AbstractResource getAbstractResource()
    {
        return reference;
    }

   /**
    * The <code>getKind</code> method returns the IDL identifier of the 
    * org.omg.Session.AbstractResource backing the agent.
    * @return String IDL identifier
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
            throw new RuntimeException( "unable to resolve resource kind", e );
        }
    }

   /**
    * The <code>getType</code> method returns a human-friendly name derived from the IDL identifier.
    * @return String short human-friendly identifier
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
            throw new RuntimeException( "AbstractResourceAgent:getKind", e );
        }
    }

   /**
    * Returns the resource random identifier that identifies this resource within the scope of 
    * the resource domain address.
    * @return int constant random identifier
    * @see #getDomain
    */
    public int getRandom( )
    {
        return random;
    }

   /**
    * The <code>getDomain</code> method returns the org.omg.Session.AbstractResource domain authority 
    * and address as a string.
    * @return String strigified domain address
    */
    public String getDomain( )
    {
        if(getLogger().isDebugEnabled()) getLogger().debug("setDomain");
	  if( domain != null ) return domain;
	  try
        {
		domain = reference.domain().naming_entity;
            return domain;
 	  }
	  catch( Throwable e )
        {
		final String warning = "remote exception while resolving domain.";
            if( getLogger().isWarnEnabled() ) getLogger().warn( warning, e );
            throw new RuntimeException( warning, e );
        }
    }

   /**
    * The <code>getCreation</code> method return the creation date of the 
    * <code>org.omg.Session.AbstractResource</code> backing the agent.
    *
    * @return Date creation date
    */
    public Date getCreation(  )
    {
        if( getLogger().isDebugEnabled() ) getLogger().debug("getCreation");
        if( creation != null ) return creation;
	  try
        {
	      creation = TimeUtils.convertToDate( reference.creation() );
            return creation;
 	  }
	  catch( Throwable e )
        {
		final String warning = "remote exception while getting creation date.";
            if( getLogger().isWarnEnabled() ) getLogger().warn( warning, e );
            throw new RuntimeException( warning, e );
        }
    }

   /**
    * The <code>getModification</code> method return the timestamp of the last 
    * event issued by the primary <code>org.omg.Session.AbstractResource</code>
    * backing the agent.
    * @return Date modification date
    */
    public Date getModification(  )
    {
        if( getLogger().isDebugEnabled() ) getLogger().debug("getModification");
	  if( modification != null ) return modification;
	  try
        {
	      modification = TimeUtils.convertToDate( reference.modification() );
		putValue("modification", modification);
		return modification;
 	  }
	  catch( Throwable e )
        {
		final String warning = "remote exception while getting modification date.";
            if( getLogger().isWarnEnabled() ) getLogger().warn( warning, e );
            throw new RuntimeException( warning, e );
        }
    }

   /**
    * The <code>getAccess</code> method return the last access date of 
    * the primary <code>org.omg.Session.AbstractResource</code>. 
    * backing the agent.
    * @return Date last access date
    */
    public Date getAccess(  )
    {
        if( getLogger().isDebugEnabled() ) getLogger().debug("getAccess");
	  try
        {
	      return TimeUtils.convertToDate( reference.access() );
 	  }
	  catch( Throwable e )
        {
		final String warning = "remote exception while getting last access date.";
            if( getLogger().isWarnEnabled() ) getLogger().warn( warning, e );
            throw new RuntimeException( warning, e );
        }
    }

   /**
    * The <code>getProducer</code> method returns a TaskAgent that is producing this resource, or, 
    * null if no Task is currently associated as producer.
    * @return TaskAgent task agent producing this resource (possibly null)
    */
    public TaskAgent getProducer()
    {
        if( getLogger().isDebugEnabled() ) getLogger().debug("getProducer");
        if( producerAccessment ) return producer;
	  try
	  {
		AbstractResource r = reference.get_producer();
		if( r != null ) producer = (TaskAgent) getResolver().resolve( r );
		producerAccessment = true;
        }
        catch( Throwable e )
        {
		final String warning = "unable to resolve producing task.";
            if( getLogger().isWarnEnabled() ) getLogger().warn( warning, e );
            throw new RuntimeException( warning, e );
        }
	  finally
	  {
		return producer;
	  }
    }

   /**
    * The <code>getConsumers</code> method returns an iterator of Tasks consuming this resource.
    * Resources may be simultaneoulsy consumed by any number of Tasks.
    * @return AgentIterator iterator of consuming tasks
    */
    public net.osm.util.List getConsumers()
    {
        if( getLogger().isDebugEnabled() ) getLogger().debug("getConsumers");
	  if( consumedByCollection != null ) return consumedByCollection;
	  try
	  {
            consumedByCollection = new LinkCollection( 
		  getLogger().getChildLogger("consumers"), 
		  getOrb(), getResolver(), reference, audit, 
		  ConsumedByHelper.type(), ConsumedBy.class 
            );
            return consumedByCollection;
	  }
	  catch( Exception e )
	  {
		final String error = "unable to resolve resource consumers.";
            if( getLogger().isErrorEnabled() ) getLogger().error( error, e );
            throw new RuntimeException( error, e );
	  }
    }

    private LinkTableModel getConsumedByModel()
    {
	  if( consumedByModel != null ) return consumedByModel;
        consumedByModel = new LinkTableModel( (LinkCollection) getConsumers( ) );
	  return consumedByModel;
    }

   /**
    * The <code>getContainers</code> method returns an iterator of Workspaces that this resource
    * is referenced within. Resources may be simultaneoulsy contained by any number of Workspaces.
    * @return AgentIterator iterator of Workspaces referencing this resoruce.
    */
    public List getContainers()
    {
        if( getLogger().isDebugEnabled() ) getLogger().debug("getContainers");
	  if( collectedByCollection != null ) return collectedByCollection;
	  try
	  {
            collectedByCollection = new LinkCollection( 
		  getLogger().getChildLogger("containers"), getOrb(), 
              getResolver(), reference, audit, 
		  CollectedByHelper.type(), CollectedBy.class 
		);
		return collectedByCollection;
	  }
	  catch( Exception e )
	  {
		final String error = "unable to resolve resource containers.";
            if( getLogger().isErrorEnabled() ) getLogger().error( error, e );
            throw new RuntimeException( error, e );
	  }
    }

    private LinkTableModel getContainedByModel()
    {
        if( collectedByModel != null ) return collectedByModel;
	  collectedByModel = new LinkTableModel( (LinkCollection) getContainers( ) );
	  return collectedByModel;
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
		final String warning = "unable to resolve resource IOR.";
            if( getLogger().isWarnEnabled() ) getLogger().warn( warning, e );
            throw new RuntimeException( warning, e );
        }
    }

   /**
    * The <code>remove</code> method request the destruction of the business object
    * backing this agent, resulting in the possible retraction of the objects 
    * associations with other resources.  If the resource is currently in use by a
    * task as a consumed or produced resource, the remove operation will throw a
    * <code>CannotRemoveException</code> exception.
    * @exception CannotRemoveException
    * @osm.warning does not currently asses the need for or issue a warning dialog
    */
    public void remove() throws CannotRemoveException
    {
        if( getLogger().isDebugEnabled() ) getLogger().debug("remove");
	  try
        {
            reference.remove();
        }
	  catch( org.omg.CORBA.OBJECT_NOT_EXIST e )
        {
		dispose();
	  }
	  catch( NotRemovable e )
        {
		final String warning = "Cannot remove the resource.";
		throw new CannotRemoveException( warning, e );
	  }
	  catch( Exception e )
        {
		final String warning = "remote exception while attempting to remove the resource";
            if(getLogger().isWarnEnabled()) getLogger().warn( warning, e );
            throw new RuntimeException( warning, e );
        }
    }
    
    //=========================================================================
    // Entity implementation
    //=========================================================================

   /**
    * The <code>getStandardViews</code> operation returns a sequence of panels 
    * representing different views of the content and/or associations maintained by
    * and agent.
    */

    public List getPropertyPanels()
    {
        if( getLogger().isDebugEnabled() ) getLogger().debug("getPropertyPanels");

	  //
	  // get all of the supertype panels
	  // together with the local panels
        //

	  if( list == null )
        {
		Font font = getShell().getDefaultFont();
	      list = super.getPropertyPanels();

		list.add( 
		  new ScrollView( 
                new TablePanel( 
			this, "Containers", getContainedByModel(), new LinkColumnModel( font )
		    )
		  )
		);

		list.add( 
		  new ScrollView( 
                new TablePanel( 
			this, "Consumers", getConsumedByModel(), new LinkColumnModel( font )
		    )
		  )
		);

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
        if( getLogger().isDebugEnabled() ) getLogger().debug("getFeatures");

        if( features != null ) return features;

        List list = super.getFeatures();
	  try
	  {
		list.add( new ActiveFeature( this, "name", "getName", "name" ));
            list.add( new StaticFeature("domain", getDomain()));
	      list.add( new StaticFeature("identifier", "" + getRandom()));
		list.add( new StaticFeature("creation", getCreation().toString()));
		list.add( new ActiveFeature( this, "modification", "getModification", "modification" ));
		list.add( new ActiveFeature( this, "alive", "getActive", "alive" ));
        }
	  catch( Exception e )
	  {
	      return list;
	  }
	  this.features = list;
	  return features;
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
        if( getLogger().isDebugEnabled() ) getLogger().debug("dispose");

	  if( audit != null ) audit.removeRemoteEventListener( getAbstractResource(), this );

        if( consumedByCollection != null ) consumedByCollection.dispose();
        if( collectedByCollection != null ) collectedByCollection.dispose();
	  if( producer != null ) producer.dispose();

        reference = null;
        orb = null;
        key = null;
        kind= null;
        ior = null;
        domain = null;
        creation = null;
        producer = null;
        list = null;
        removeAction = null;

	  super.dispose();

    }

    //=========================================================================
    // Object
    //=========================================================================

    public boolean equals( Object object )
    {
        if( super.equals( object ) ) return true;
        if( object instanceof AbstractResourceAgent )
	  {
		return equivalent( (AbstractResourceAgent) object );
        }
        else if( object instanceof LinkAgent )
	  {
		return equivalent( (LinkAgent) object );
        }
        else if( object instanceof org.omg.Session.AbstractResource )
	  {
		return equivalent( (org.omg.Session.AbstractResource) object );
        }
        return false;
    }

    public boolean equivalent( AbstractResourceAgent agent )
    {
        return equivalent( agent.getAbstractResource() );
    }

    public boolean equivalent( LinkAgent link )
    {
        boolean result = equivalent( (AbstractResourceAgent) link.getTarget() );
        return result;
    }

   /**
    * Test that returns true if the agents principal object is equal to the 
    * supplied primary resopurce object reference.
    * @param resource object reference to compare against this agents primary resource
    */
    public boolean equivalent( org.omg.Session.AbstractResource resource )
    {
	  
        if( resource == null ) return false;
	  if( reference == null ) return false;
	  return reference._is_equivalent( resource );
    }
}