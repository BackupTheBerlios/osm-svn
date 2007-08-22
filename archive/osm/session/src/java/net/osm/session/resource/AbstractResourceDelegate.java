
package net.osm.session.resource;

import org.apache.avalon.framework.context.Context;
import org.apache.avalon.framework.CascadingRuntimeException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.logger.Logger;
import org.apache.avalon.framework.logger.LogEnabled;
import org.apache.avalon.framework.activity.Initializable;
import org.apache.avalon.framework.activity.Disposable;

import org.omg.CORBA.ORB;
import org.omg.CORBA.Any;
import org.omg.CORBA.TypeCode;
import org.omg.CORBA.BooleanHolder;
import org.omg.CosCollection.AnySequenceHolder;
import org.omg.CosLifeCycle.NotRemovable;
import org.omg.CosNotification.StructuredEvent;
import org.omg.CosNotification.EventType;
import org.omg.CosNotification.EventHeader;
import org.omg.CosNotification.FixedEventHeader;
import org.omg.CosNotification.Property;
import org.omg.CosObjectIdentity.IdentifiableObject;
import org.omg.CosTime.TimeService;
import org.omg.CosPersistentState.NotFound;
import org.omg.NamingAuthority.AuthorityId;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.Servant;
import org.omg.Session.ResourceUnavailable;
import org.omg.Session.SemanticConflict;
import org.omg.Session.ProcessorConflict;
import org.omg.Session.Workspace;
import org.omg.Session.WorkspaceHelper;
import org.omg.Session.WorkspacesHolder;
import org.omg.Session.WorkspaceIterator;
import org.omg.Session.WorkspaceIteratorHolder;
import org.omg.Session.WorkspaceIteratorPOA;
import org.omg.Session.WorkspaceIteratorPOATie;
import org.omg.Session.Task;
import org.omg.Session.TaskHelper;
import org.omg.Session.TasksHolder;
import org.omg.Session.TaskIterator;
import org.omg.Session.TaskIteratorHolder;
import org.omg.Session.TaskIteratorPOA;
import org.omg.Session.TaskIteratorPOATie;
import org.omg.Session.LinksHolder;
import org.omg.Session.LinkIteratorHolder;
import org.omg.Session.LinkIterator;
import org.omg.Session.LinkIteratorPOA;
import org.omg.Session.LinkIteratorPOATie;
import org.omg.Session.Link;
import org.omg.Session.LinkHelper;
import org.omg.Session.UsageHelper;
import org.omg.Session.ContainmentHelper;
import org.omg.Session.ConsumptionHelper;
import org.omg.Session.ProductionHelper;
import org.omg.Session.CollectedByHelper;
import org.omg.Session.IsPartOfHelper;
import org.omg.Session.ProducedByHelper;
import org.omg.Session.CollectedByHelper;
import org.omg.Session.ConsumedByHelper;
import org.omg.TimeBase.UtcT;
import org.omg.TimeBase.UtcTHelper;

import org.apache.pss.ActivatorService;
import org.apache.pss.util.Incrementor;
import org.apache.time.TimeUtils;

import net.osm.adapter.Adapter;
import net.osm.list.LinkedList;
import net.osm.list.Iterator;
import net.osm.list.NoEntry;
import net.osm.realm.StandardPrincipal;
import net.osm.realm.PrincipalManager;
import net.osm.realm.PrincipalManagerHelper;
import net.osm.sps.StructuredEventUtilities;
import net.osm.session.CannotTerminate;
import net.osm.session.ComposedOf;
import net.osm.session.IsPartOf;
import net.osm.session.ProducedBy;
import net.osm.session.CollectedBy;
import net.osm.session.ConsumedBy;
import net.osm.session.Collects;
import net.osm.session.SessionSingleton;
import net.osm.session.task.TaskIteratorDelegate;
import net.osm.session.linkage.LinkIteratorDelegate;
import net.osm.session.workspace.WorkspaceIteratorDelegate;
import net.osm.session.linkage.LinkStorage;
import net.osm.session.linkage.LinkStorageHome;


/**
 * AbstractResourceDelegate is the implememntation of the
 * <code>org.omg.Session.AbstractResource</code> interface.
 * An AbstractResource is a transactional and persistent CORBA
 * objects contained in one or more Workspaces. They may be
 * selected, consumed and produced by Tasks. AbstractResources
 * are found and selected by tools and facilities that present
 * lists of candidate resources. These lists may be filtered by
 * things like security credentials, by type, and by
 * implementation. CORBAservice Trading can be used to build
 * resource candidate lists. Resources selected from the lists
 * are then wrapped by the tool or facility as AbstractResources.
 * Task and Workspace are dependent on the AbstractResources
 * they use and contain. Implementations are required to notify
 * Task and Workspace of changes and defer deletion requests
 * until all linked Tasks signal their readiness to handle.
 * <p>
 *
 * The bind, replace and release operations enable a client to
 * declare a dependency on an AbstractResource.  When a Task,
 * User or Workspace establishes a usage of containment dependency
 * on an AbstractResource, it is required to invoke the bind
 * operation.  When dependencies are changed, such as the modification
 * of the owner of a Task or the replacement of a resource within a
 * workspaces, an implementation is required to invoke the replace
 * operation.  When a relationship is retracted, as a result of the
 * completion of a task, an implementation is required to invoke
 * the release operation on resources to which it has established
 * a dependency.<p>
 *
 * Exceptions raised under the bind and replace operations include
 * ResourceUnavailable, ProducerConflict and SemanticConflict.
 * The ResourceUnavailable and ProducerConflict exception may be
 * raised by an implementation to indicate that the resource that
 * is the target of a bind or replace operation is unable to
 * fulfill the request.  ResourceUnavailable may be raised as a
 * result of a concurrency conflict.  The ProducerConflict
 * exception may be raises in a situation where the producer
 * resource is unable to support the association (for example,
 * as a result of a processing capacity limit). A SemanticConflict
 * exception may be raised if an attempt is made to violate the
 * cardinality or type rules concerning the link kind referenced
 * under the Link argument.<p>
 */

public class AbstractResourceDelegate extends BaseBusinessObjectDelegate 
implements AbstractResourceOperations, OwnedResource
{

    // ============================================================
    // static
    // ============================================================

   /**
    * Name of the event domain classifying structured events produced by this resource type.
    */

    public static final String EVENT_DOMAIN = SessionSingleton.EVENT_DOMAIN;
    
   /**
    * The event type for a 'move' event.
    */
    public static final EventType moveEventType = SessionSingleton.moveEventType;
    
   /**
    * The event type for a 'remove' event.
    */
    public static final EventType removeEventType = SessionSingleton.removeEventType;
    
   /**
    * The event type for a 'update' event.
    */
    public static final EventType updateEventType = SessionSingleton.updateEventType;

   /**
    * The event type for a 'bind' event.
    */
    public static final EventType bindEventType = SessionSingleton.bindEventType;
    
   /**
    * The event type for a 'replace' event.
    */
    public static final EventType replaceEventType = SessionSingleton.replaceEventType;

   /**
    * The event type for a 'release' event.
    */
    public static final EventType releaseEventType = SessionSingleton.releaseEventType;

   /**
    * Default name to be used when creating a new resource.
    */
    private static final String defaultName = "Untitled";

    // ============================================================
    // state
    // ============================================================

   /**
    * The typecode of this resource - overridden by concrete resource types.
    */
    protected TypeCode typeCode = AbstractResourceHelper.type();
    
   /**
    * Storage object representing this abstract resource.
    */
    private AbstractResourceStorage m_store;
    
   /**
    * Principal manager.
    */
    private PrincipalManager principalManager;
    
   /**
    * The current principal.
    */
    private StandardPrincipal principal;

   /**
    * Object reference of this abstract resource.
    */
    private AbstractResource m_resource;
    
   /**
    * Reference to the AbstractResourceService
    */
    private AbstractResourceService m_resource_service;

    //=======================================================================
    // Serviceable
    //=======================================================================

    /**
     * Pass the <code>ServiceManager</code> to the <code>Composable</code>.
     * The <code>Serviceable</code> implementation uses the supplied
     * <code>ServiceManager</code> to acquire the components it needs for
     * execution.
     * @param controller the <code>ServiceManager</code> for this delegate
     * @exception ServiceException
     */
    public void service( ServiceManager manager )
    throws ServiceException
    {
	  super.service( manager );
        m_resource_service = (AbstractResourceService) manager.lookup(
          ActivatorService.ACTIVATOR_KEY );
    }

    //=======================================================================
    // Initializable
    //=======================================================================
    
   /**
    * Signal completion of contextualization phase and readiness to serve requests.
    * @exception Exception if the servant cannot complete normal initialization
    */
    public void initialize()
    throws Exception
    {       
        super.initialize();
        this.m_store = (AbstractResourceStorage) getStorageObject();

        //
        // get the publisher and initialize it so that its event 
        // queue is up-and-running
        //

        Object publisher = this.m_store.publisher();
        if( publisher instanceof LogEnabled ) ((LogEnabled)publisher).enableLogging(
          getLogger().getChildLogger( "publisher" ) );
        if( publisher instanceof Initializable ) ((Initializable)publisher).initialize();
    }
    
    //==================================================
    // Vulnerable
    //==================================================

   /**
    * Test is this instance can be terminated or not.
    * @return boolean true if the persistent identity of this 
    * instance can be destroyed.
    */
    public boolean expendable( )
    {
        synchronized( m_store )
        {
            if( m_store.produced_by().resource() != null ) return false;
            return super.expendable();
        }
    }

   /**
    * Destroys the persistent identity of this object. An 
    * implementation is responsible for disposing of references 
    * to external resources on completion of termiation actions. 
    */
    public void terminate( ) throws CannotTerminate
    {
	  synchronized( m_store )
        {
		if( !expendable() ) throw new CannotTerminate("resource is in use");
            if( getLogger().isDebugEnabled() ) getLogger().debug("[ABR] terminate");
		try
		{
		    //
		    // request release of resource associations held by external 
	          // workspaces
		    //

                LinkedList collected_by = m_store.collected_by();
                if( getLogger().isDebugEnabled() ) getLogger().debug(
                  "[ABR] destroying CollectedBy links");
		    Iterator iterator = collected_by.iterator();
		    while( iterator.has_next() )
                {
                    LinkStorage s = (LinkStorage) iterator.next();
                    synchronized( s )
	              {
			      try
				{
				    if( s.link() instanceof IsPartOf )
				    {
                                s.link().resource().release( new ComposedOf( getReference() ) );
				    }
				    else 
				    {
                                s.link().resource().release( new Collects( getReference() ) );
				    }
				}
				catch( Exception remoteException )
				{
                            String problem = "remote exception during release notification";
                            if( getLogger().isWarnEnabled() ) getLogger().warn( 
                              problem, remoteException );
				}

			      try
		            {
                            if( getLogger().isDebugEnabled() ) getLogger().debug("[ABR] release/2 "
                            + "\n\tSOURCE: " + m_store.name()
                            + "\n\tLINK: " + s.link().getClass().getName()
                            + "\n\tTARGET: " + s.link().resource().name() );
                        }
		            catch( Exception e )
			      {
                            if( getLogger().isDebugEnabled() ) getLogger().debug("[ABR] release/2 "
                            + "\n\tSOURCE: " + m_store.name()
                            + "\n\tLINK: " + s.link().getClass().getName()
                            + "\n\tTARGET: Information unavailable" );
			      }
                        post( newReleaseEvent( s.link() ));
                        iterator.remove();
                        s.destroy_object();
                    }
                }
		}
		catch( Throwable e )
		{
                final String error = "unexpected exception during termination";
		    if( getLogger().isErrorEnabled() ) getLogger().error( error, e);
		    e.printStackTrace();
		}

		try
		{
	          //
                // post the remove event
		    //

                post( newRemoveEvent( ) );
		}
		catch( Throwable e )
		{
                final String error = "unexpected exception while posting remove event";
		    if( getLogger().isErrorEnabled() ) getLogger().error( error, e);
		}

            super.terminate();
        }
    }

    // =====================================================================
    // Disposable
    // =====================================================================

   /**
    * Clean up state members.
    */ 
    public synchronized void dispose()
    {
        if( m_store.publisher() instanceof Disposable) ((Disposable)m_store.publisher()).dispose();
        
        m_store = null;
        m_resource = null;
        this.typeCode = null;
	  super.dispose();
    }

    // =====================================================================
    // OwnedResource
    // =====================================================================
    
   /**
    * Returns the principal owner of this instance.
    * @return StandardPrincipal - the security principal that owns this instance.
    */
    public StandardPrincipal getPrincipalOwner()
    {
        return this.m_store.owner();
    }

    // =====================================================================
    // Structured Events
    // =====================================================================
    
   /**
    * Creation of a new 'move' StructuredEvent.
    * @param result AbstractResource reference after the move
    * @return move structured event
    */
    public StructuredEvent newMoveEvent( AbstractResource result )
    {
        Property sourceProp = createSourceProperty( );
        Any b = ORB.init().create_any();
        AbstractResourceHelper.insert( b, result );
        Property resultProp = new Property( "new", b );
        return StructuredEventUtilities.createEvent( 
		moveEventType, 
		new Property[]
		{ 
			StructuredEventUtilities.timestamp( ), 
			sourceProp, 
			resultProp 
		}
	  );
    }

   /**
    * Creation of a new 'remove' StructuredEvent, fired when the resource is removed (destroyed).
    * @return remove structured event
    */
    public StructuredEvent newRemoveEvent( )
    {
        Property sourceProp = createSourceProperty();
        return StructuredEventUtilities.createEvent( removeEventType, 
		new Property[]
		{ 
			StructuredEventUtilities.timestamp( ), 
			sourceProp 
		}
	  );
    }

   /**
    * Creation of a new 'update' StructuredEvent produced when a feature of 
    * an abststract resource (such as its name) is modified.
    * @param feature String name of the feature that is updated
    * @param old any the old value
    * @param _new any the new value
    * @return update structured event
    */
    public StructuredEvent newUpdateEvent( String feature, Any old, Any _new )
    {
        Property sourceProp = createSourceProperty( );
        Any f = ORB.init().create_any();
        f.insert_string( feature );
        Property featureProp = new Property( "feature", f );
        Property oldProp = new Property( "old", old );
        Property newProp = new Property( "new", _new );
        return StructuredEventUtilities.createEvent( updateEventType, 
		new Property[]
		{ 
			StructuredEventUtilities.timestamp( ), 
			sourceProp, 
			featureProp, 
			oldProp, 
			newProp 
		}
	  );
    }

   /**
    * Creation of a new 'bind' StructuredEvent, produced when a new association  
    * such as UsedBy, ProducedBy, ContainedBy, etc, is added to the resource.
    *
    * @param link Link added to the resource
    * @return 'bind' structured event
    */
    public StructuredEvent newBindEvent( Link link )
    {
        Property sourceProp = createSourceProperty( );
        Any b = ORB.init().create_any();
        LinkHelper.insert( b, link );
        Property linkProp = new Property( "link", b );
        return StructuredEventUtilities.createEvent( bindEventType, 
		new Property[]{ 
			StructuredEventUtilities.timestamp( ), 
			sourceProp, 
			linkProp 
		}
	  );
    }
    
   /**
    * Creation of a new 'replace' StructuredEvent, produced when an existing association is replaced 
    * by another association.
    * @param old Link being replaced
    * @param _new Link replacement
    * @return 'replace' structured event
    */
    public StructuredEvent newReplaceEvent( Link old, Link _new )
    {
        Property sourceProp = createSourceProperty( );
        Any b = ORB.init().create_any();
        LinkHelper.insert( b, old );
        Property oldProp = new Property( "old", b );
        Any c = ORB.init().create_any();
        LinkHelper.insert( c, _new );
        Property newProp = new Property( "new", b );
        return StructuredEventUtilities.createEvent( replaceEventType, 
		new Property[]{ 
			StructuredEventUtilities.timestamp( ), 
			sourceProp, 
			oldProp, 
			newProp 
		}
	  );
    }
    
   /**
    * Creation of a new 'release' StructuredEvent, produced when an association between 
    * one AbstractResource and another is retracted.
    * @param link Link being replaced
    * @return 'release' structured event
    */
    public StructuredEvent newReleaseEvent( Link link )
    {
        Property sourceProp = createSourceProperty( );
        Any b = ORB.init().create_any();
        LinkHelper.insert( b, link );
        Property linkProp = new Property( "link", b );
        return StructuredEventUtilities.createEvent( releaseEventType, 
		new Property[]{ 
			StructuredEventUtilities.timestamp(), 
			sourceProp, 
			linkProp 
		}
	  );
    }
    
   /**
    * Internal utility to a structured event property containing 
    * an abstract resource wrapped within an Any with the property
    * name of 'source'.
    */
    protected Property createSourceProperty( )
    {
        return createSourceProperty( getEventSource() );
    }

   /**
    * Internal utility to a structured event property containing 
    * an abstract resource wrapped within an Any with the property
    * name of 'source'.
    */
    protected Property createSourceProperty( AbstractResource res )
    {
        final Any a = ORB.init().create_any();
        AbstractResourceHelper.insert( a, res );
        return new Property( "source", a );
    }

   /**
    * Returns an object reference in the form of an AbstractResource.  This
    * operation is a utility supporting the creation of appropriate references
    * for packing into the source property of a structured event.
    * @return AbstractResource a reference in the form of an AbstractResource
    */
    protected AbstractResource getEventSource(  )
    {
        return m_resource_service.getAbstractResourceReference( this.m_store );
    }

    // =====================================================================
    // AbstractResourceOperations
    // =====================================================================
    
   /**
    * Returns the name of the <code>AbstractResource</code>.
    * @return String the current name
    */
    public String name()
    {
        touch( m_store );
        return this.m_store.name();
    }
    
   /**
    * Sets the name of the <code>AbstractResource</code>.
    * @param value String new name
    */
    public void name( String value )
    {
        
        synchronized( m_store )
        {
            // update the value of the name of the resource
            
            getLogger().debug("[ABR] update name from " + m_store.name() + " to " + value);
            Any oldValue = ORB.init().create_any();
            oldValue.insert_string( name() );
            this.m_store.name( value );
            modify( m_store );
            
            // post update event
            Any newValue = ORB.init().create_any();
            newValue.insert_string( value );
            post( newUpdateEvent( "name", oldValue, newValue ));
        }
    }
    
   /**
    *  The most derived type that this <code>AbstractResource</code> represents.
    */
    public TypeCode resourceKind()
    {
        touch( m_store );
        return this.typeCode;
    }
    
    
   /**
    * <p>Resource may declare dependecies on each other by invoking the bind operation
    * on the resource to which a dependency exists.  For example, a Task has dependecies
    * on the resource it consumes and produces.  These resource have reciprical
    * associations towards the Task.  When associating a resource with another, the
    * dependant resource creates a new Link instance referencing itself as the dependent
    * resource and passes this Link to the bind operation on the resource it is dependant
    * on.  E.g. the implementation of create_task on User is responsible for declaring
    * the association from the Task to the owning User.  The implementation does this
    * by creating a new <code>Owns</code> instance, setting the resource value to the
    * newly created <code>Task</code> and invoking bind on User, passing the
    * <code>Owns</code> instance as the link argument. An implememntation of Task is
    * responsible for returning <code>OwnedBy</code> link under the expand operation,
    * reflecting the ownership association to the User.  An implementation of User
    * is responsible for returning 0..n <code>Owns</code> instances referncing the
    * <code>Task</code> instances it owns.</p>
    *
    * <p>AbstractResource provides support for the binding of CollectedBy, IsPartOf,
    * ConsumedBy and ProducedBy links.  Types derived from AbstractResourceDelegate are
    * responsible for overriding this method to support supplimentary link kinds.
    *
    * @param  link a Link declaring the resource that has a dependency on the resource that
    * is the target of invocation - where the type of dependency is qualified by the
    * most derived type of the Link instance.
    * <P>
    * @exception  <code>ResourceUnavailable</code>
    * if the resource cannot accept the link binding due to an implementation dependent constraint.
    * @exception  <code>ProcessorConflict</code>
    * processor is unable or unwilling to provide processing services to a Task.
    * @exception  <code>SemanticConflict</code>
    * if the resource cannot accept the link binding due to a cardinality constraint.
    */
    public void bind(Link link)
    throws ResourceUnavailable, ProcessorConflict, SemanticConflict
    {

        if( getLogger().isDebugEnabled() ) getLogger().debug("bind " + link.getClass().getName() );
        synchronized( m_store )
        {
            if( link instanceof org.omg.Session.ProducedBy )
            {
                // Notification to this AbstractResource that is being
                // produced by a Task. This operation is valid if there is no
                // current ProducedBy association otherwise the client should be
                // invoking the replace operation.
                
                if( m_store.produced_by().resource() != null ) throw new SemanticConflict();
                m_store.produced_by( (ProducedBy) link );
                if( getLogger().isDebugEnabled() ) getLogger().debug("[ABR] bind "
                + "\n\tSOURCE: " + m_store.name()
                + "\n\tLINK: " + link.getClass().getName()
                + "\n\tTARGET: " + link.resource().name() );
                post( newBindEvent( link ));
                
            }
            else if (link instanceof org.omg.Session.CollectedBy)
            {
                // Notification to this resource that the resource is contained within
                // a workspace declared under the resource member of the link argument.
                // We need to validate that this resource does not already contain a
                // CollectedBy or IsPartOf reference to the Workspace in question, and
                // if not, then add the supplied link to the collected_by list.
                
                LinkedList list = m_store.collected_by();
                synchronized( list )
                {
                    if( !containsLink( list, link ))
                    {
				if( getLogger().isDebugEnabled() ) getLogger().debug(
                          "calling addLink with CollectedBy");
                        addLink( getReference(), list, link );
                    }
                    else
                    {
                        throw new SemanticConflict();
                    }
                }
            }
            else if (link instanceof org.omg.Session.ConsumedBy)
            {
                
                // Notification to this resource that the resource is consumed by
                // a task declared under the resource member of the link argument.
                // We need to validate that this resource does not already contain a
                // ConsumedBy reference to the Task in question, and
                // if not, then add the supplied link to the consumed_by list.
                
                LinkedList list = m_store.consumed_by();
                synchronized( list )
                {
                    if( !containsLink( list, link ))
                    {
                        addLink( getReference(), list, link );
                    }
                    else
                    {
                        throw new SemanticConflict();
                    }
                }
            }
            else
            {
                String s = "Attempt to bind using an unknown link type " + link.getClass() ;
                throw new RuntimeException(s);
            }
            modify( m_store );
        }
    }
    
   /**
    * Replaces an existing dependecy with another.
    * @param  old the Link to replace
    * @param  new the replacement Link
    * @exception  <code>ResourceUnavailable</code>
    *   if the resource cannot accept the new link binding
    * @exception  <code>ProcessorConflict</code>
    *   if a processor is unable or unwilling to provide processing services to a Task.
    * @exception  <code>SemanticConflict</code>
    *   if the resource cannot accept the link binding due to a cardinality constraint.
    */
    public void replace(Link old, Link _new)
    throws ResourceUnavailable, ProcessorConflict, SemanticConflict
    {
        synchronized( m_store )
        {
            getLogger().debug("replace");
            touch( m_store );
            
            if(( old instanceof org.omg.Session.ProducedBy ) && ( _new instanceof org.omg.Session.ProducedBy ))
            {
                
                // client is requesting the replacement of an exiting ProducedBy
                // association from a Task
                
                if( m_store.produced_by().resource() != null )
                {
                    if( m_store.produced_by().resource()._is_equivalent( old.resource() ))
                    {
                        m_store.produced_by( (ProducedBy) _new );
                		if( getLogger().isDebugEnabled() ) getLogger().debug("[ABR] replace "
                			+ "\n\tSOURCE: " + m_store.name()
                			+ "\n\tOLD: " + old.getClass().getName()
                			+ "\n\tNEW: " + _new.getClass().getName());
                        post( newReplaceEvent( old, _new ));
                        modify( m_store );
                        return;
                    }
                    else
                    {
                        if( getLogger().isDebugEnabled() ) getLogger().debug(
                          "[ABR] supplied 'old' value does not match current 'ProducedBy' link");
                        throw new SemanticConflict();
                    }
                }
                else if( old.resource() == null )
                {
                    m_store.produced_by( (ProducedBy) _new );
                	  if( getLogger().isDebugEnabled() ) getLogger().debug("[ABR] replace "
                		+ "\n\tSOURCE: " + m_store.name()
                		+ "\n\tOLD: " + old.getClass().getName()
                		+ "\n\tNEW: " + _new.getClass().getName());
                    post( newReplaceEvent( old, _new ));
                    modify( m_store );
                    return;
                }
                else
                {
                    if( getLogger().isDebugEnabled() ) getLogger().debug(
                      "[ABR] old argument cannot replace a non assigned link");
                    throw new SemanticConflict();
                }
            }
            else if (( old instanceof org.omg.Session.CollectedBy) 
              && ( _new instanceof org.omg.Session.CollectedBy ))
            {
                // client is requesting the replacement of an exiting CollectedBy
                // association from a Workspace

                try
                {
                    replaceLink( getReference(), m_store.collected_by(), old, _new );
                }
                catch (Exception e)
                {
                    String s = "unexpected exception while resolving 'CollectedBy' references";
                    throw new CascadingRuntimeException( s, e );
                }
            }
            else if ((old instanceof org.omg.Session.ConsumedBy) 
              && ( _new instanceof org.omg.Session.ConsumedBy))
            {
                // client is requesting the replacement of an exiting ConsumedBy
                // association from a Task
                try
                {
                    replaceLink( getReference(), m_store.consumed_by(), old, _new );
                }
                catch (Exception e)
                {
                    String s = "unexpected exception while resolving 'ConsumedBy' references";
                    throw new CascadingRuntimeException(s, e);
                }
            }
            else
            {
                String s = "Unsupported link type.";
                if( getLogger().isErrorEnabled() ) getLogger().error( "[ABR] " + s );
                throw new SemanticConflict();
            }
        }
    }
    
    
   /**
    * Releases an existing dependecy.
    * @param  link the Link to retract
    */
    public void release(Link link)
    {
        
        synchronized( m_store )
        {
            
            getLogger().debug("release");
            touch( m_store );
            
            if( link instanceof org.omg.Session.ProducedBy )
            {
                
                // a Task is notifying this resource of the retraction of
                // a ProducedBy association
                
                if( m_store.produced_by().resource() != null )
                {
                    try
                    {
                        if( m_store.produced_by().resource()._is_equivalent( m_resource ) )
                        {
                            m_store.produced_by( new ProducedBy() );
                	  	    if( getLogger().isDebugEnabled() ) getLogger().debug("[ABR] release "
                			+ "\n\tSOURCE: " + m_store.name()
                			+ "\n\tLINK: " + link.getClass().getName()
                			+ "\n\tTARGET: " + link.resource().name() );
                            post( newReleaseEvent( link ));
                        }
                    }
                    catch( Exception e)
                    {
                        String s = "failed to release ProducedBy association";
                        throw new CascadingRuntimeException( s, e );
                    }
                }
            }
            else if (link instanceof org.omg.Session.CollectedBy)
            {
                
                // containing Workspace is notifying this resource of the
                // retraction of a CollectedBy association
                
                try
                {
                    LinkedList list = m_store.collected_by();
                    releaseLink( getReference(), list, link );
                }
                catch( Exception e)
                {
                    String s = "Failed to release CollectedBy association";
                    throw new CascadingRuntimeException( s, e );
                }
            }
            else if (link instanceof org.omg.Session.ConsumedBy)
            {
                
                // consuming Task is notifying this resource of the
                // retraction of a ConsumedBy association
                
                try
                {
                    LinkedList list = m_store.consumed_by();
                    releaseLink( getReference(), list, link );
                }
                catch( Exception e)
                {
                    String s = "failed to release ConsumedBy association";
                    throw new CascadingRuntimeException( s, e );
                }
            }
            else
            {
                // unknown link type
                return;
            }
            modify( m_store );
            
        }
    }
   /**
    * Returns a list of Workspaces containing this resource.
    * @param  max_number long the maximum number of Workspace instances to
    * include in the returned Workspaces sequence.
    * @param  workspaces Session::Workspaces a sequence of Workspace instances of
    * a length no greater than max_number.
    * @param  wsit WorkspaceIterator an iterator of the Workspace instances
    * containing this resource.
    */
    public void list_contained(int max_number, WorkspacesHolder workspaces, WorkspaceIteratorHolder wsit)
    {
	  WorkspaceIterator iterator = null;
        LinkedList list = m_store.collected_by();

        synchronized( list )
        {
	      try
	      {
	          WorkspaceIteratorDelegate delegate = new WorkspaceIteratorDelegate( 
                  list.iterator() );
	          WorkspaceIteratorPOA servant = new WorkspaceIteratorPOATie( delegate );
		    iterator = servant._this( super.getORB() );

		    AnySequenceHolder anysHolder = new AnySequenceHolder();
		    BooleanHolder booleanHolder = new BooleanHolder();
		    if( iterator.retrieve_next_n_elements( max_number, anysHolder, booleanHolder ) )
		    {
			  int k = anysHolder.value.length;
			  Workspace[] sequence = new Workspace[k];
		        for( int i=0; i<k; i++ )
			  {
				sequence[i] = WorkspaceHelper.extract( anysHolder.value[i]);
		        }
			  workspaces.value = sequence;
		    }
		    else
		    {
		        workspaces.value = new Workspace[0];
		    }
		}
		catch( Exception e )
		{
		    String error = "failed to establish WorkspaceIterator" ;
		    throw new CascadingRuntimeException( error, e );
		}
        }
	  wsit.value = iterator;
        touch( m_store );
    }
    
    
   /**
    * Returns a list of Tasks using or consuming this resource.
    * @param  max_number long the maximum number of Task instances to
    * include in the returned Tasks sequence.
    * @param  tasks Session::Tasks a sequence of Task instances of
    * a length no greater than max_number.
    * @param  wsit TaskIterator an iterator of the Task instances
    * consuming this resource.
    */
    public void list_consumers(int max_number, TasksHolder tasks, TaskIteratorHolder taskit)
    {
	  TaskIterator iterator = null;
        LinkedList list = m_store.consumed_by();
        synchronized( list )
        {
	      try
	      {
	          TaskIteratorDelegate delegate = new TaskIteratorDelegate( list.iterator() );
	          TaskIteratorPOA servant = new TaskIteratorPOATie( delegate );
		    iterator = servant._this( super.getORB() );

		    AnySequenceHolder anysHolder = new AnySequenceHolder();
		    BooleanHolder booleanHolder = new BooleanHolder();
		    if( iterator.retrieve_next_n_elements( max_number, anysHolder, booleanHolder ) )
		    {
			  int k = anysHolder.value.length;
			  Task[] sequence = new Task[k];
		        for( int i=0; i<k; i++ )
			  {
				sequence[i] = TaskHelper.extract( anysHolder.value[i]);
		        }
			  tasks.value = sequence;
		    }
		    else
		    {
		        tasks.value = new Task[0];
		    }
		}
		catch( Exception e )
		{
		    String error = "failed to establish TaskIterator" ;
		    throw new CascadingRuntimeException( error, e );
		}
        }
	  taskit.value = iterator;
        touch( m_store );
    }
    
   /**
    * Returns the Task that is producing this resource.
    * @return  Task producing this resource.
    */
    public Task get_producer()
    {
        getLogger().debug("get_producer");
        touch( m_store );
        return TaskHelper.narrow( m_store.produced_by().resource() );
    }
    
   /**
    * Returns the number of Links held by an AbstractResource corresponding to a
    * given TypeCode filter criteria.  Filter criteria is expressed as a TypeCode
    * desribing a type of Link.  AbstractResource is aware of the link types
    * presented in the table below.  Types derived from AbstractResource may
    * override the count method to return counts based on more specific link
    * associations.
    *
    * <table>
    * <tr>
    *   <td>TypeCode</td><td>Description</td>
    * <tr>
    *   <td valign="top">Link</td><td>Super type of all link declarations</td>
    * <tr>
    *   <td valign="top">Usage</td>
    *   <td>
    *     Super type of Consumption and Production abstract link types.
    *     AbstractResource count will return the number of ConsumedBy
    *     and ProducedBy links.
    *   </td>
    * <tr>
    *   <td valign="top">Containment</td>
    *   <td>
    *     Super type of the abstract Collects and CollectedBy link types.
    *     AbstractResource count will return the number of CollectedBy
    *     links, representing the number of loose agregation assoications
    *     to containing workspaces, and between 0 and 1 IsPartOf link
    *     referring to the strong aggregation association to the pricipal
    *     containing workspace.
    *   </td>
    * </table>
    * <br>
    *
    * @param  type CORBA::TypeCode a type code of a Link derived type
    * @return  int number of links corresponding to the supplied type within the
    *              scope of the AbstractResource type.
    */
    public short count(org.omg.CORBA.TypeCode type)
    {
        getLogger().debug("count");
        int count = 0;
        
        // abstract link types
        
        if( type.equal( LinkHelper.type() ) ) { count = linkCount(); }
        else if( type.equal( UsageHelper.type() ) ) { count = usageCount(); }
        else if( type.equal( ContainmentHelper.type() ) ) { count = containmentCount(); }
        else if( type.equal( ConsumptionHelper.type() ) ) { count = consumptionCount(); }
        else if( type.equal( ProductionHelper.type() ) ) { count = productionCount(); }
        
        //concrete link types
        
        else if( type.equal( CollectedByHelper.type() ) ) { count = collectedByCount(); }
        else if( type.equal( ConsumedByHelper.type() ) ) { count = consumedByCount(); }
        else if( type.equal( ProducedByHelper.type() ) ) { count = producedByCount(); }
        
        touch( m_store );
        Integer v = new Integer( count );
        return v.shortValue();
    }
    
   /**
    * Returns a set of resources linked to it by a specific relationship.
    * Objects returned are, or are created as, AbstractResources. This operation
    * may be used by desktop managers to present object relationship graphs.
    * The AbstractResource expand implmentation supports resolution of ProducedBy,
    * ConsumedBy, and CollectedBy link types.  Support for abstract link expansion
    * and LinkIterator is not supported at this time.
    *
    * @return  LinkIterator an iterator of Link instances
    * @param  max_number maximum number of Link instance to include in the
    * seq value.
    * @param  seq Links a sequence of Links matching the type filter
    * @param  type TypeCode filter
    */
    public LinkIterator expand(org.omg.CORBA.TypeCode type, int max_number, LinksHolder links)
    {
        getLogger().debug("expand");
        LinkIterator linkIterator = null; 
             
	  if( type.equivalent( ProducedByHelper.type() ))
	  {
		links.value = new Link[]{ m_store.produced_by() };
		touch( m_store );
	 	return null;
	  }
        else if( type.equivalent( ConsumedByHelper.type() ))
        {
            LinkedList consumedBy = m_store.consumed_by();
            synchronized( consumedBy )
            {
                // prepare resource sequence
                links.value = create_link_sequence( consumedBy, max_number ); 
                LinkIteratorDelegate delegate = new LinkIteratorDelegate( super.getORB(), consumedBy, type );
	          LinkIteratorPOA servant = new LinkIteratorPOATie( delegate );
		    return servant._this( super.getORB() );
            }
        }
        else if( type.equivalent( CollectedByHelper.type() ))
        {
            LinkedList collectedBy = m_store.collected_by();
            synchronized( collectedBy )
            {
                // prepare resource sequence
                links.value = create_link_sequence( collectedBy, max_number ); 
                LinkIteratorDelegate delegate = new LinkIteratorDelegate( super.getORB(), collectedBy, type );
	          LinkIteratorPOA servant = new LinkIteratorPOATie( delegate );
		    return servant._this( super.getORB() );
            }
        }
        else
        {
		// unknown type
		if( getLogger().isWarnEnabled() ) getLogger().warn("cannot handle the supplied link type");
		return null;
        }
    }
    
   /**
    *Utility used to create a sequence of links of a size determined by
    *the supplied max_number value and supplied list of Link instances.
    */
    protected Link[] create_link_sequence( LinkedList list, int max_number )
    {
        Link[] seq = null;
        int n = list.size();
        if( ( max_number == 0 ) || ( n < max_number ) )
        {
            seq = new Link[n];
        }
        else
        {
		if( max_number > 0 )
		{
                seq = new Link[max_number];
		}
		else
		{
                seq = new Link[0];
		}
        }
        
        Iterator iterator = list.iterator();
        for( int i=0; i<seq.length; i++ )
        {
            try
            {
                seq[i] = ((LinkStorage) iterator.next()).link();
            }
            catch (NoEntry e)
            {
                // ignore
            }
            catch (Exception e)
            {
                String s = "Unexpected error while populating a link sequence.";
                throw new CascadingRuntimeException( s, e );
            }
        }
        return seq;
    }
    
    // ==========================================================================
    // BaseBusinessObject suppliments
    // ==========================================================================
    
   /**
    * AbstractResourceDelegate extends the remove operation through retraction of
    * containment links.
    */
    
    public synchronized void remove()
    throws NotRemovable
    {
        synchronized( m_store )
        {
            if( getLogger().isDebugEnabled() ) getLogger().debug("[ABR] remove");
		try
		{
                terminate();
		}
		catch(CannotTerminate e)
		{
		    if( getLogger().isErrorEnabled() ) getLogger().error( e.getMessage(), e );
		    throw new NotRemovable( e.getMessage() ); // should not happen
		}
		try
		{
 		    dispose();
		}
		catch(Exception e)
		{
		    System.err.println("unexpected error while removing resource");
		    e.printStackTrace();
		}
        }
    }
    
    //===========================================================
    // utilities
    //===========================================================

   /**
    * Set the object reference to be returned for this delegate.
    * @param resource an object referenced derived from AbstractResource
    */
    protected void setReference( AbstractResource resource )
    {
        m_resource = resource;
    }

   /**
    * Returns the object reference for this delegate.
    * @return the object referenced for the delegate
    */
    protected AbstractResource getReference( )
    {
        return m_resource;
    }

   /**
    * Returns the number of ConsumedBy, ProducedBy and CollectedBy
    * links held by this AbstractResource.
    */
    protected int linkCount()
    {
        return usageCount() + containmentCount();
    }
    
   /**
    * Returns the number of ConsumedBy and ProducedBy
    * links held by this AbstractResource.
    */
    
    protected int usageCount()
    {
        return consumptionCount() + productionCount();
    }
    
   /**
    * Returns the number of IsPartOf and CollectedBy
    * links held by this AbstractResource.
    */
    protected int containmentCount()
    {
        return collectedByCount();
    }
    
   /**
    * Returns the number of ConsumedBy links held by this AbstractResource.
    */
    protected int consumptionCount()
    {
        return consumedByCount();
    }
    
   /**
    * Returns the number of ProducedBy links held by this AbstractResource.
    */
    protected int productionCount()
    {
        return producedByCount();
    }
    
   /**
    * Returns the number of CollectedBy links held by this
    * AbstractResource.
    */
    protected int collectedByCount()
    {
        LinkedList list = m_store.collected_by();
        synchronized( list )
        {
            return list.size();
        }
    }
    
   /**
    * Returns the number of ConsumedBy links held by this
    * AbstractResource.
    */
    protected int consumedByCount()
    {
        LinkedList list = m_store.consumed_by();
        synchronized( list )
        {
            return list.size();
        }
    }
    
   /**
    * Returns the number of ProducedBy links held by this
    * AbstractResource.
    */
    protected int producedByCount()
    {
        if( m_store.produced_by().resource() != null ) return 1;
        return 0;
    }
    
    
   /**
    * Internal method used to check is list contains the supplied link.
    *
    * @param list LinkedList the list to search within
    * @param link Link to search for
    * @return boolean if link is contained within the list
    */
    protected boolean containsLink( LinkedList list, Link link )
    {
        
        synchronized( list )
        {   
            //
            // search though the list for a link containing a resource
            // with same identity as the remoteKey value
            //
            
            try
            {
                Iterator iterator = list.iterator();
                while( iterator.has_next() )
                {
			  try
			  {
                        org.omg.Session.AbstractResource r = (
                          (LinkStorage)iterator.next()).link().resource();
                        if( r._is_equivalent( link.resource() )) return true;
			  }
			  catch( NoEntry e )
			  {
                         // ignore this case
			  }
			  catch( org.omg.CORBA.OBJECT_NOT_EXIST e )
			  {
                         String error = "non-existant object reference, removing list entry";
                         if( getLogger().isWarnEnabled() ) getLogger().warn( error, e );
				 iterator.remove();
			  }
			  catch( Throwable e )
			  {
                         String error = "unexpected exception ignored comparing links for equality";
                         if( getLogger().isWarnEnabled() ) getLogger().warn( error, e );
			  }
                }
            }
            catch (Exception exp )
            {
                String error = "unexpected error while comparing links for resource equality";
                throw new CascadingRuntimeException( error, exp );
            }
            return false;
        }
    }

    /**
     * Returns the storage home for LinkStorage types.
     * @osm.warning this operation will be moved to a service under the component manager
     */
    protected LinkStorageHome getLinkHome() throws NotFound 
    {
        return ( LinkStorageHome ) m_store.get_storage_home().get_catalog().find_storage_home( 
			"PSDL:osm.net/session/linkage/LinkStorageHomeBase:1.0" );
    }
    
   /**
    * Internal method to add a link to a supplied list and issue a bind
    * event.
    */
    protected void addLink( AbstractResource r, LinkedList list, Link link )
    {
        if( r == null ) throw new RuntimeException(
		"Null resource argument supplied to addLink.");
        if( list == null ) throw new RuntimeException(
		"Null list argument supplied to addLink.");
        if( link == null ) throw new RuntimeException(
		"Null link argument supplied to addLink.");

        LinkStorageHome h = null;
        try
        {
	      h = getLinkHome();
        }
        catch( NotFound nf )
	  {
		throw new RuntimeException("failed to locate link storarge home", nf );
	  }

        synchronized( list )
        {
            list.add( h.create( link ) );
            if( getLogger().isDebugEnabled() ) getLogger().debug("[ABR] bind "
            + "\n\tSOURCE: " + m_store.name()
            + "\n\tLINK: " + link.getClass().getName()
            + "\n\tTARGET: " + link.resource().name() );		
            post( newBindEvent( link ));
            modify( m_store );
        }
    }
    
   /**
    * Internal method to replace an existing link in a supplied list with a new
    * link of the same type.
    */
    protected void replaceLink( AbstractResource r, LinkedList list, Link oldLink, Link newLink ) throws NoEntry
    {

        if( r == null ) throw new RuntimeException(
		"AbstractResourceDelegate. Null resource argument supplied to replaceLink.");
        if( list == null ) throw new RuntimeException(
		"AbstractResourceDelegate. Null list argument supplied to replaceLink.");
        if( oldLink == null ) throw new RuntimeException(
		"AbstractResourceDelegate. Null oldLink argument supplied to replaceLink.");
        if( newLink == null ) throw new RuntimeException(
		"AbstractResourceDelegate. Null newLink argument supplied to replaceLink.");

        synchronized( list )
        {
            Iterator iterator = list.iterator();
            while( iterator.has_next() )
            {
                LinkStorage link = (LinkStorage) iterator.next();
                synchronized( link )
                {
                    if( link.equal( oldLink ) )
                    {
                        link.link( newLink );
                        if( getLogger().isDebugEnabled() ) getLogger().debug("[ABR] replace "
                        + "\n\tSOURCE: " + m_store.name()
                        + "\n\tLINK: " + oldLink.getClass().getName()
                        + "\n\tTARGET: " + newLink.resource().name() );
                        post( newReplaceEvent( oldLink, newLink ));
                        modify( m_store );
                        return;
                    }
                }
            }
        }
    }
    
    
   /**
    * Internal method that handles the notification of the release
    * of a Link association to an resource and the removal of the link
    * storage instance.
    *
    * @param r AbstractResource reference usage to construct a release event
    * @param list LinkedList containing the link
    * @param link Link to be released
    */
    protected void releaseLink( AbstractResource r, LinkedList list, Link link ) throws NoEntry
    {

        if( r == null ) throw new RuntimeException(
		"AbstractResourceDelegate. Null resource argument supplied to releaseLink.");
        if( list == null ) throw new RuntimeException(
		"AbstractResourceDelegate. Null list argument supplied to releaseLink.");
        if( link == null ) throw new RuntimeException(
		"AbstractResourceDelegate. Null link argument supplied to releaseLink.");

        synchronized( list )
        {
            Iterator iterator = list.iterator();
            boolean result = false;
            while( iterator.has_next() )
            {
                try
                {
                    // check if the supplied link is the same as this link
                    LinkStorage linkStore = (LinkStorage) iterator.next();
                    synchronized( linkStore )
                    {
                        if( linkStore.equal( link ))
                        {
                            iterator.remove();
                            linkStore.destroy_object();
                            if( getLogger().isDebugEnabled() ) getLogger().debug("[ABR] release "
                            + "\n\tSOURCE: " + m_store.name()
                            + "\n\tLINK: " + link.getClass().getName()
                            + "\n\tTARGET: " + link.resource().name() );
                            post( newReleaseEvent( link ));
                            modify( m_store );
                            return;
                        }
                    }
                }
                catch( NoEntry e)
                {
                    // ignore case
                }
                catch( Exception e)
                {
                    String s = "unexpected error while releasing " + link.getClass().getName() + " link";
                    throw new CascadingRuntimeException( s, e);
                }
            }
            
            //
            // if we get here it means that the link was not found
            //
            
            if( getLogger().isWarnEnabled() ) getLogger().warn(
		  "requesting release of a link that does not exist in this list of " + list.size() + " entries"
            + "\n\tSOURCE: " + m_store.name()
            + "\n\tLINK: " + link.getClass().getName()
            + "\n\tTARGET: " + link.resource().name()
            );
            System.err.println(
		"requesting release of a link that does not exist in this list of " + list.size() + " entries"
            + "\n\tSOURCE: " + m_store.name()
            + "\n\tLINK: " + link.getClass().getName()
            + "\n\tTARGET: " + link.resource().name()
            );
            throw new NoEntry();
        }
    }
    
    
   /**
    * Internal method to release all links and notify respective resources
    * of the release.
    *
    * @param r AbstractResource consituting the source of a release event
    * @param list List from which links will be released
    * @param link Link representing the inverse release message
    */
    protected void retractEveryLink( AbstractResource r, LinkedList list, String type ) throws NoEntry
    {
        synchronized( list )
        {
            if( getLogger().isDebugEnabled() ) getLogger().debug(
	        "[ABR] releasing " + list.size() + " " + type + " links");
            Iterator iterator = list.iterator();
            while( iterator.has_next() )
            {                
                // get link
                LinkStorage s = (LinkStorage) iterator.next();
                synchronized( s )
                {
                    // notify resource
                    try
                    {
                        s.link().resource().release( s.link().inverse( r ) );
                    }
                    catch( Exception e )
                    {
                        String problem = "remote exception during release notification";
                        if( getLogger().isWarnEnabled() ) getLogger().warn( "[ABR] " + problem, e );
                    }
                    if( getLogger().isDebugEnabled() ) getLogger().debug("[ABR] release/2 "
                    + "\n\tSOURCE: " + m_store.name()
                    + "\n\tLINK: " + s.link().getClass().getName()
                    + "\n\tTARGET: " + s.link().resource().name() );
                    
                    post( newReleaseEvent( s.link() ));
                    iterator.remove();
                    s.destroy_object();
                }
            }
        }
    }

   /**
    * Internal method to release all links and notify respective resources
    * of the release.
    *
    * @param r AbstractResource consituting the source of a release event
    * @param list List from which links will be released
    * @param link Link representing the inverse release message
    * @deprecated use retractEveryLink( AbstractResource r, LinkedList list, String type )
    */
    protected void retractEveryLink( AbstractResource r, LinkedList list, String type, Link link ) throws NoEntry
    {
        synchronized( list )
        {
            if( getLogger().isDebugEnabled() ) getLogger().debug(
	        "[ABR] releasing " + list.size() + " " + type + " links");
            Iterator iterator = list.iterator();
            while( iterator.has_next() )
            {                
                // get link
                LinkStorage s = (LinkStorage) iterator.next();
                synchronized( s )
                {
                    // notify resource
                    try
                    {
                        s.link().resource().release( link );
                    }
                    catch( Exception e )
                    {
                        String problem = "remote exception during release notification";
                        if( getLogger().isWarnEnabled() ) getLogger().warn( "[ABR] " + problem, e );
                    }
                    if( getLogger().isDebugEnabled() ) getLogger().debug("[ABR] release/2 "
                    + "\n\tSOURCE: " + m_store.name()
                    + "\n\tLINK: " + s.link().getClass().getName()
                    + "\n\tTARGET: " + s.link().resource().name() );
                    
                    post( newReleaseEvent( s.link() ));
                    iterator.remove();
                    s.destroy_object();
                }
            }
        }
    }

   /**
    * Returns an adapter for this resource.
    * @return Adapter object adapter.
    */
    public Adapter get_adapter()
    {
        return new AbstractResourceValue( m_resource );
    }

   /**
    * Returns true of the invoking principal is the owner of the object.
    */
    protected final boolean isOwner()
    {
        try
        {
            final boolean result = getPrincipal().equals( m_store.owner() );
            getLogger().debug( "principal owner: " + result );
            return result;
        }
        catch( Throwable e )
        {
            throw new AbstractResourceRuntimeException( "failed to resolve self equivalence.", e );
        }
    }
}

