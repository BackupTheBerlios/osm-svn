

package net.osm.session.workspace;

import java.util.Vector;

import org.apache.avalon.framework.context.Context;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.ServiceException;

import org.omg.CORBA.ORB;
import org.omg.CORBA.Any;
import org.omg.CORBA.TypeCode;
import org.omg.CORBA.NO_IMPLEMENT;
import org.omg.CORBA.BooleanHolder;
import org.omg.CosCollection.AnySequenceHolder;
import org.omg.CosObjectIdentity.IdentifiableObject;
import org.omg.CosTime.TimeService;
import org.omg.CosNaming.NameComponent;
import org.omg.CosLifeCycle.NoFactory;
import org.omg.CosLifeCycle.NotRemovable;
import org.omg.NamingAuthority.AuthorityId;
import org.omg.PortableServer.POA;
import org.omg.Session.LinkIterator;
import org.omg.Session.AbstractResource;
import org.omg.Session.AbstractResourceHelper;
import org.omg.Session.AbstractResourceIteratorPOA;
import org.omg.Session.AbstractResourceIteratorPOATie;
import org.omg.Session.AbstractResourceIteratorHelper;
import org.omg.Session.LinkIterator;
import org.omg.Session.LinkIteratorPOA;
import org.omg.Session.LinkIteratorPOATie;
import org.omg.Session.AbstractResourcesHolder;
import org.omg.Session.AbstractResourceIterator;
import org.omg.Session.AbstractResourceIteratorHolder;
import org.omg.Session.User;
import org.omg.Session.Link;
import org.omg.Session.LinksHolder;
import org.omg.Session.ResourceUnavailable;
import org.omg.Session.ProcessorConflict;
import org.omg.Session.SemanticConflict;
import org.omg.Session.AccessedByHelper;
import org.omg.Session.AccessedBy;
import org.omg.Session.CollectedBy;
import org.omg.Session.ComposedOf;
import org.omg.Session.ComposedOfHelper;
import org.omg.Session.Collects;
import org.omg.Session.CollectsHelper;
import org.omg.Session.AdministeredBy;
import org.omg.Session.Administers;
import org.omg.Session.IsPartOf;
import org.omg.Session.ContainmentHelper;

import org.apache.pss.ActivatorService;
import org.apache.pss.util.Incrementor;
import org.apache.time.TimeUtils;

import net.osm.adapter.Adapter;
import net.osm.session.CannotTerminate;
import net.osm.session.user.UserService;
import net.osm.session.resource.AbstractResourceDelegate;
import net.osm.session.resource.AbstractResourceIteratorDelegate;
import net.osm.session.linkage.LinkIteratorDelegate;
import net.osm.session.linkage.LinkStorage;
import net.osm.list.LinkedList;
import net.osm.list.Iterator;
import net.osm.list.NoEntry;

/**
 * Workspace defines private and shared places where resources, including
 * Task and Session objects, may be contained. Workspaces may contain
 * Workspaces.  The support for sharing and synchronizing the use of
 * objects available in Workspaces is provided by the objects and their
 * managers. Each Workspace may contain any collection of private and
 * shared objects that the objects and their managers provide access to,
 * and control use of.
 */

public class WorkspaceDelegate extends AbstractResourceDelegate implements WorkspaceOperations
{

    //======================================================================
    // state
    //======================================================================
    
   /**
    * Storage object representing this Workspace.
    */
    private WorkspaceStorage m_store;
    
   /**
    * Object reference to this Workspace.
    */
    private Workspace m_workspace;

   /**
    * Internal reference to the workspace service.
    */
    private WorkspaceService m_workspace_service;

   /**
    * Internal reference to the user service.
    */
    private UserService m_user_service;

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
        m_workspace_service = (WorkspaceService) manager.lookup(
          WorkspaceService.WORKSPACE_SERVICE_KEY );
        m_user_service = (UserService) manager.lookup( 
          UserService.USER_SERVICE_KEY );
    }

    protected UserService getUserService()
    {
        return m_user_service;
    }

    protected WorkspaceService getWorkspaceService()
    {
        return m_workspace_service;
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

        m_store = (WorkspaceStorage) super.getStorageObject();
        
	  setWorkspaceReference( 
          WorkspaceHelper.narrow( 
            m_workspace_service.getWorkspaceReference( m_store ) ) );
    }
    
    //==================================================
    // Vulnerable implementation
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
            if( getLogger().isDebugEnabled() ) getLogger().debug("[WSP] terminate ");
            
            // set a flag indicating if there the removal of
            // containment associations succeeds (i.e. strong
            // agregation links).  If there is a problem
            // we need to halt the removal of ourselves.
            
            boolean removable = true;
            try
            {
                //
                // remove the Collects link entries
                //
                
                LinkedList list = m_store.collects();
                removable = retractContaintmentLinks( getWorkspaceReference(), list );
		}
            catch (Exception e)
            {
                if( getLogger().isErrorEnabled() ) getLogger().error("Unexpected termination exception", e );
                throw new CannotTerminate( e.getMessage() );
            }
		finally
		{
		    if( !removable ) throw new CannotTerminate( 
			"Workspace. could not removed a contained resource" );
            }

		//
		// from here on its irreversible
		//

            try
            {
                //
                // remove the AccessedBy link entries
                //
                
                LinkedList accessedBy = m_store.accessed_by();
                retractEveryLink( getWorkspaceReference(), accessedBy, "accessed_by" );
            }
            catch (Exception e)
            {
                if( getLogger().isErrorEnabled() ) getLogger().error(
                  "Unexpected termination exception", e );
            }

		super.terminate();
        }
    }

   /**
    * Clean up state members.
    */ 
    
    public synchronized void dispose()
    {
        this.m_store = null;
        this.m_workspace = null;
	  super.dispose();
    }


    // ==========================================================================
    // Workspace
    // ==========================================================================
    
   /**
    * Adds the supplied <code>AbstractResource</code> to the <code>Workspace</code>.
    * An implementation of add_contains_resource must invoke the bind operation on
    * the target resource with the link kind of contains and the containing Workspace
    * as the resource argument.
    */
    
    public void add_contains_resource( AbstractResource r )
    {

        LinkedList list = m_store.collects();
        synchronized( list )
        {
            try
            {
                Collects link = new net.osm.session.Collects( r );
                if( !containsLink( list, link ))
                {
                    r.bind( new net.osm.session.CollectedBy( getWorkspaceReference() ));
                    addLink( getWorkspaceReference(), list, link );
                }
                else
                {
                    // don't do anything because the resource is already here
			  if( getLogger().isWarnEnabled() ) getLogger().warn(
                      "Attempting to duplicate a Collects association" 
				+ "\n\tSOURCE: " + m_store.name()
				+ "\n\tTARGET: " + r.name() );
                }
            }
            catch (Exception e)
            {
                String s = "Unable to add resource to the Workspace.";
                throw new WorkspaceRuntimeException( s, e );
            }
        }
    }
    
   /**
    * Remove a containment association between a resource and its
    * containing Workspace.
    * An implementation of remove_contains_resource must invoke the release operation on
    * the target resource with the link kind of contains and the containing Workspace as
    * the resource state member.
    *
    * @param resource AbstractResource to remove from the Workspace
    */
    public void remove_contains_resource(AbstractResource r)
    {
        LinkedList list = m_store.collects();
        synchronized( list )
        {
            try
            {
                Collects link = new net.osm.session.Collects( r );
                releaseLink( getWorkspaceReference(), list, link );
                
                // notify the contained resource that it is no
                // longer contained
                
                try
                {
                    r.release( new net.osm.session.CollectedBy( getWorkspaceReference() ));
                }
                catch( Exception e )
                {
                    String s = "Remote resource raised an exception when notifying it " +
                    "of the release of a ContainedBy association";
                    if( getLogger().isDebugEnabled() ) getLogger().debug( s, e );
                }
            }
            catch( NoEntry noEntry )
            {
                String problem = "Cannot remove a resource that isn't contained.";
                throw new WorkspaceRuntimeException( problem, noEntry );
            }
        }
    }
    
   /**
    * Creation of a sub-workspace to this Workspace.
    * An implementation of create_subworkspace must invoke the bind operation on newly
    * created workspace using the Contains link kind and the parent workspace as the resource
    * argument. On creation of a new workspace the principal User creating the new
    * instance is implicitly associated with the workspace as administrator.
    * As administrator, the User holds rights enabling the modification of the access control
    * list through bind, replace and release operations.
    */
    public org.omg.Session.Workspace create_subworkspace(String name, User[] accesslist)
    {
	  //
	  // create the sub-workspace
	  //

        if( getLogger().isDebugEnabled() ) getLogger().debug(
          "create_subworkspace" );

        Workspace w = null;
        try
        {
            w = getWorkspaceService().createWorkspace( name );
	  }
	  catch( Exception e )
	  {
		String error = "Unable to create workspace.";
            throw new WorkspaceRuntimeException( error );
	  }

        User user = null;
	  try
        {
	      user = getUserService().locateUser();
	  }
	  catch( Throwable e )
	  {
		String error = "Unable to resolve the principal user.";
		throw new WorkspaceRuntimeException( error, e ); 
	  }

        IsPartOf isPartOf = new net.osm.session.IsPartOf( getWorkspaceReference() );
        ComposedOf composedOf = new net.osm.session.ComposedOf( w );
	  Administers administers = new net.osm.session.Administers( w );
        AdministeredBy administeredBy = new net.osm.session.AdministeredBy( user );
        try
        {
		w.bind( isPartOf );
	      bind( composedOf );
	      user.bind( administers );
	      w.bind( administeredBy );
		return w;
        }
	  catch( Throwable e )
	  {
		String error = "Failed to establish sub-workspace associations.";
            if( getLogger().isErrorEnabled() ) getLogger().error( error, e );
		try
	      {
		    getWorkspaceReference().release( composedOf );
		}
		catch( Throwable t ){}
		try
	      {
		    user.release( administers );
		}
		catch( Throwable t ){}
		try
	      {
		    w.remove();
		}
		catch( Throwable t ){}
            throw new WorkspaceRuntimeException( error, e );
	  }
    }
    
   /**
    * The list resources operation will return a list of all Workspace resources
    * by type. This facilitates organization of resource types by user interfaces and
    * use by task creation, workflow, and other functions requiring specified types.
    *
    * @param  resourcetype org.omg.CORBA.TypeCode restricts returned resources to
    *         be of a type equal to or derived from this type.
    * @param  max_number long the maximum number of AbstractResources instances to
    *         include in the returned AbstractResources sequence.
    * @param  resources Session::AbstractResources a sequence of AbstractResources
    *         instances of a length no greater than max_number that are contained
    *         by this Workspace that correspond in type to the supplied typecode.
    * @param  resourceit AbstractResourceIterator an iterator of the AbstractResource
    *         instances
    *
    */

    public void list_resources_by_type( TypeCode resourcetype, int max_number,
    	AbstractResourcesHolder resources, AbstractResourceIteratorHolder resourceit )
    {

	  if( getLogger().isDebugEnabled() ) getLogger().debug("listing resources by type");
	  AbstractResourceIterator arIterator = null;
        LinkedList list = m_store.collects();
        synchronized( list )
        {
	      try
	      {
		    Iterator iterator = list.iterator();

	          AbstractResourceIteratorDelegate delegate = 
                  new AbstractResourceIteratorDelegate( iterator, resourcetype );
	          AbstractResourceIteratorPOA servant = 
                  new AbstractResourceIteratorPOATie( delegate );
		    arIterator = servant._this( super.getORB() );

		    AnySequenceHolder anysHolder = new AnySequenceHolder();
		    BooleanHolder booleanHolder = new BooleanHolder();
		    if( arIterator.retrieve_next_n_elements( max_number, anysHolder, booleanHolder ) )
		    {
			  int k = anysHolder.value.length;
			  AbstractResource[] sequence = new AbstractResource[k];
		        for( int i=0; i<k; i++ )
			  {
				sequence[i] = AbstractResourceHelper.extract( anysHolder.value[i]);
		        }
			  resources.value = sequence;
		    }
		    else
		    {
		        resources.value = new AbstractResource[0];
		    }
		}
		catch( Exception e )
		{
                final String error = "Internal error while preparing workspace content iterator.";
                if( getLogger().isErrorEnabled() ) getLogger().error( error, e );
		}
        }
	  resourceit.value = arIterator;
    }


    // ==========================================================================
    // FactoryFinder implementation
    // ==========================================================================
        
   /**
    * The find_factories operation is passed a key used to identify the desired factory.
    * The key is a name, as defined by the naming service. More than one factory may
    * match the key. As such, the factory finder returns a sequence of factories. If there are
    * no matches, the NoFactory exception is raised.<p>
    * The scope of the key is the factory finder. The factory finder assigns no semantics to
    * the key. It simply matches keys. It makes no guarantees about the interface or
    * implementation of the returned factories or objects they create.
    */
    
    public org.omg.CORBA.Object[] find_factories( NameComponent[] factory_key )
    throws NoFactory
    {
	  // WARNING: The current implementation throws a NoFactory exception pending 
        // implementation of configuration based factory creation.

        throw new NoFactory();
    }
    
    // ==========================================================================
    // AbstractResource operation override
    // ==========================================================================
    
   /**
    *  The most derived type that this <code>AbstractResource</code> represents.
    */
    
    public TypeCode resourceKind()
    {
        return WorkspaceHelper.type();
    }
    
   /**
    * Extension of the AbstractResource bind operation to support reception of the
    * notification of association dependencies from external resources on this
    * Workspace.
    *
    * @param link Link notification of an association dependency
    */
    public synchronized void bind(Link link)
    throws ResourceUnavailable, ProcessorConflict, SemanticConflict
    {
        synchronized( m_store )
        {
            if( getLogger().isDebugEnabled() ) getLogger().debug("bind " 
		  + link.getClass().getName() + " (" + (getWorkspaceReference() != null ) + ")");
            touch( m_store );

            if( link instanceof org.omg.Session.Collects )
            {
		    
                // Notification to this Workspace that an AbstractResource has been
                // added to the set of resources collected by this Workspace.
                // We need to validate that this resource does not already contain a
                // Collects link (with the collects list), and if not, add the
                // supplied link to the collects list.
                
                LinkedList list = m_store.collects();
                synchronized( list )
                {
                    if( !containsLink( list, link ))
                    {
				if( getLogger().isDebugEnabled() ) getLogger().debug(
                          "calling addLink with Collects");
                        addLink( getWorkspaceReference(), list, link );
                    }
                    else
                    {
                        throw new SemanticConflict();
                    }
                }
            }
            else if( link instanceof org.omg.Session.AccessedBy )
            {
                
                // Notification to this Workspace that a User has been
                // added to the set of users accessing this Workspace.
                // We need to validate that this user is not already contained
                // under an AccessedBy link (with the access_by list), and if not,
                // add the supplied link to the list.
                
                LinkedList list = m_store.accessed_by();
                synchronized( list )
                {
                    if( !containsLink( list, link ))
                    {
				if( getLogger().isDebugEnabled() ) getLogger().debug(
                          "calling addLink with AccessedBy");
                        addLink( getWorkspaceReference(), list, link );
                    }
                    else
                    {
                        throw new SemanticConflict();
                    }
                }
            }
            else
            {
                super.bind( link );
            }
        }
    }
    
   /**
    * Replaces an existing Workspace dependecy with another.
    * @param  old the Link to replace
    * @param  new the replacement Link
    * @exception  <code>ResourceUnavailable</code>
    * if the resource cannot accept the new link binding
    * @exception  <code>ProcessorConflict</code>
    * if a processor is unable or unwilling to provide processing services to a Task.
    * @exception  <code>SemanticConflict</code>
    * if the resource cannot accept the link binding due to a cardinality constraint.
    */
    
    public synchronized void replace(Link old, Link _new)
    throws ResourceUnavailable, ProcessorConflict, SemanticConflict
    {
        synchronized( m_store )
        {
            if( getLogger().isDebugEnabled() ) getLogger().debug(
              "[Workspace]replace");
            touch( m_store );

            if (( old instanceof org.omg.Session.Collects) 
              && ( _new instanceof org.omg.Session.Collects ))
            {
                // client is requesting the replacement of an exiting Collects
                // association from a Workspace
                try
                {
                    replaceLink( getReference(), m_store.collects(), old, _new );
                }
                catch (Exception e)
                {
                    String s = "unexpected exception while resolving 'Collects' references";
                    throw new WorkspaceRuntimeException( s, e );
                }
            }
            else if (( old instanceof org.omg.Session.AccessedBy) 
               && ( _new instanceof org.omg.Session.AccessedBy ))
            {
                // client is requesting the replacement of an exiting AccessedBy
                // association from a User
                try
                {
                    replaceLink( getReference(), m_store.accessed_by(), old, _new );
                }
                catch (Exception e)
                {
                    String s = "Unexpected exception while resolving 'AccessedBy' references.";
                    throw new WorkspaceRuntimeException( s, e );
                }
            }
            else
            {
                super.replace( old, _new );
            }
            modify( m_store );
        }
    }
    
   /**
    * Releases an existing dependecy.
    * @param link Link to retract
    */
    public synchronized void release(Link link)
    {
        synchronized( m_store )
        {
            getLogger().debug("release");
            touch( m_store );
            
            if( link instanceof org.omg.Session.Collects )
            {
                
                // an AbstractResource is notifying this resource of the retraction of
                // n Collects association (i.e. an AbstractResource has been removed from
                // this Workspace)
                
                try
                {
                    LinkedList list = m_store.collects();
                    releaseLink( getWorkspaceReference(), list, link );
                }
                catch( Exception e)
                {
                    String s = "Unable to release Collects association";
                    throw new WorkspaceRuntimeException( s, e );
                }
            }
            else if( link instanceof org.omg.Session.AccessedBy )
            {
                
                // a User is notifying this resource of the retraction of
                // an AccessedBy association (i.e. an User has been removed from
                // this Workspace accessed_by list)
                
                try
                {
                    LinkedList list = m_store.accessed_by();
                    releaseLink( getWorkspaceReference(), list, link );
                }
                catch( Exception e)
                {
                    String s = "failed to release AccessedBy association";
                    throw new WorkspaceRuntimeException( s, e );
                }
            }
            else
            {
                super.release( link );
            }
        }
    }

   /**
    * Returns an integer corresponding to the number of links of the 
    * link type declared by the type argument.
    */
    public short count(org.omg.CORBA.TypeCode type)
    {
        getLogger().debug("count");
        int count = 0;

        if( type.equal( CollectsHelper.type() ) ) 
	  {
		count = m_store.collects().size();
	  }
        else if( type.equal( ComposedOfHelper.type() ) ) 
	  {
		// WARNING: following is incorrect - needs to count only 
		// the links derived from ComposedOf
	      count = m_store.collects().size();
	  }
        else if( type.equal( ContainmentHelper.type() ) ) 
	  {
		count = m_store.collects().size();
	  }
        else if( type.equal( AccessedByHelper.type() ) ) 
	  {
		count = m_store.accessed_by().size();
	  }
	  else
	  {
	      count = super.count( type );
	  }
        touch( m_store );
        Integer v = new Integer( count );
        return v.shortValue();
    }
  
   /**
    * Returns a set of resources linked to this Workspace by a specific relationship.
    * This operation may be used by desktop managers to present object relationship 
    * graphs. The Workspace expand implmentation suppliments the AbstractResource 
    * implementation with the addition of support for IsPartOf, ComposedOf, Collects, 
    * and AccessedBy link types.
    * Support for abstract link expansion and LinkIterator is not available at this time.
    *
    * @return  LinkIterator an iterator of Link instances
    * @param  max_number maximum number of Link instance to include in the
    * seq value.
    * @param  seq Links a sequence of Links matching the type filter
    *
    * @param link Link to retract
    */
    public LinkIterator expand(org.omg.CORBA.TypeCode type, int max_number, LinksHolder links)
    {
        getLogger().debug("expand");
	  if( type == null ) throw new org.omg.CORBA.BAD_PARAM(
	    "Workspace.  Illegal null type argument supplied to expand.");
	  if( links == null ) throw new org.omg.CORBA.BAD_PARAM(
	    "Workspace.  Illegal null links argument supplied to expand.");
             
        if( type.equivalent( AccessedByHelper.type() ))
        {
            LinkedList list = m_store.accessed_by();
            synchronized( list )
            {
                links.value = create_link_sequence( list, max_number ); 
		    touch( m_store );
                LinkIteratorDelegate delegate = new LinkIteratorDelegate( super.getORB(), list, type );
	          LinkIteratorPOA servant = new LinkIteratorPOATie( delegate );
		    return servant._this( super.getORB() );
            }
	  }
        else if( type.equivalent( CollectsHelper.type() ))
        {
            LinkedList list = m_store.collects();
            synchronized( list )
            {
                links.value = create_link_sequence( list, max_number ); 
		    touch( m_store );
                LinkIteratorDelegate delegate = new LinkIteratorDelegate( super.getORB(), list, type );
	          LinkIteratorPOA servant = new LinkIteratorPOATie( delegate );
		    return servant._this( super.getORB() );
            }
        }
        else
        {
            return super.expand( type, max_number, links );
        }
    }

    
    // ==========================================================================
    // BaseBusinessObject operation override
    // ==========================================================================
    
   /**
    * WorkspaceDelegate extends the remove operation through retraction of
    * containment links towards abstract resources it collects and that are
    * part of (strong aggregation collection) this workspace.  If a contained
    * resource is not removable, this method will throw a NotRemovable exception
    * after having removed all other removable resources.
    */
    
    public synchronized void remove()
    throws NotRemovable
    {
        synchronized( m_store )
        {
            if( getLogger().isDebugEnabled() ) getLogger().debug("[Workspace] remove");
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
		    if( getLogger().isWarnEnabled() ) getLogger().warn( 
                 "Ignoring disposal exception during workspace removal.", e );
		}
        }
    }

    
    // ==========================================================================
    // utilities
    // ==========================================================================
    
   /**
    * Set the object reference to be returned for this delegate.
    * @param workspace the object reference for the workspace
    */
    protected void setWorkspaceReference( Workspace workspace )
    {
        m_workspace = workspace;
        super.setReference( workspace );
    }

   /**
    * Returns the object reference for this delegate.
    * @return Workspace the object referenced for the delegate
    */
    protected Workspace getWorkspaceReference( )
    {
        return m_workspace;
    }

   /**
    * Internal implementation of the remove operation supporting
    * retraction of Collects and ComposedOf links between the
    * Workspace and other resources.
    */
    
    protected boolean retractContaintmentLinks( Workspace w, LinkedList list ) throws NoEntry
    {
        //
        // remove the ComposedOf link entries
        // (strong aggregation) - can throw NotRemovable
        // exception
        //
                
        if( getLogger().isDebugEnabled() ) getLogger().debug("removing Containment links");
        boolean removable = true;
    
	  // 
	  // create a vector of the resources that are part of this workspace, and 
	  // anther vector of the resources collected by reference
	  //

	  Vector strongVector = new Vector();
	  Vector weakVector = new Vector();

        synchronized( list )
        {
            Iterator iterator = list.iterator();
            while( iterator.has_next() )
            {
                try
                {
                    LinkStorage s = (LinkStorage) iterator.next();
		        if( s.link() instanceof ComposedOf )
		        {
			      strongVector.addElement( s.link().resource() );
			  }
			  else 
			  {
			      weakVector.addElement( s.link() );
			  }
		    }
		    catch( Exception e )
		    {
			  System.err.println("???");
		    }
            }
	  }

	  //
        // retract associations to the weakly bound resources
	  //

	  try
	  {
		int j = weakVector.size();
		for( int i=0; i<j; i++ )
		{
		    Link link = (Link) weakVector.elementAt( i );
		    AbstractResource r = link.resource();
		    r.release( new net.osm.session.CollectedBy( getWorkspaceReference() ) );
                post( newReleaseEvent( link ));
		}
        }
        catch( Exception e )
	  {
		System.err.println("weak retract failed");
		e.printStackTrace();
        }

	  //
        // remove all of the strongly associated resources
	  //

	  try
	  {
		int j = strongVector.size();
		for( int i=0; i<j; i++ )
		{
		    org.omg.CORBA.Object obj = (org.omg.CORBA.Object) strongVector.elementAt( i );
		    AbstractResource r = AbstractResourceHelper.narrow( obj );
		    r.remove();
		}
        }
        catch( Exception e )
	  {
		System.err.println("strong retract failed");
		e.printStackTrace();
            removable = false;
        }
	  return removable;
    }

   /**
    * Returns a <code>WorkspaceAdapter</code>.
    * @return Adapter an instance of <code>WorkspaceAdapter</code>.
    */
    public Adapter get_adapter()
    {
        return new WorkspaceValue( m_workspace );
    }

}
