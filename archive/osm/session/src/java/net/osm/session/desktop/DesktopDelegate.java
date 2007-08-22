

package net.osm.session.desktop;

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
import org.omg.Session.WorkspaceHelper;
import org.omg.Session.Workspace;
import org.omg.Session.WorkspaceOperations;
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
import net.osm.session.workspace.WorkspaceDelegate;
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

public class DesktopDelegate extends WorkspaceDelegate implements DesktopOperations
{

    //======================================================================
    // state
    //======================================================================
    
   /**
    * Storage object representing this Workspace.
    */
    private DesktopStorage m_store;
    
   /**
    * Object reference to this Workspace.
    */
    private Desktop m_desktop;

   /**
    * Object reference to the owner of this Desktop
    */
    private User m_user;

   /**
    * Internal reference to the workspace service.
    */
    private DesktopService m_desktop_service;

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
        m_desktop_service = (DesktopService) manager.lookup(
          ActivatorService.ACTIVATOR_KEY );
        m_user_service = (UserService) manager.lookup( 
          UserService.USER_SERVICE_KEY );
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

        m_store = (DesktopStorage) super.getStorageObject();
        
	  setDesktopReference( 
          DesktopHelper.narrow( 
            m_desktop_service.getDesktopReference( m_store ) ) );
    }
    
    //==================================================
    // Vulnerable implementation
    //==================================================

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
            if( getLogger().isDebugEnabled() ) getLogger().debug("[Desktop] terminate ");
		super.terminate();
        }
    }

   /**
    * Clean up state members.
    */ 
    
    public synchronized void dispose()
    {
        this.m_store = null;
        this.m_desktop = null;
        this.m_user = null;
	  super.dispose();
    }

    // ==========================================================================
    // Desktop implementation
    // ==========================================================================
    
   /**
    * Sets the User to whom this Workspace is assigned to.
    * @param user - the user to assign the desktop to
    * @osm.warning Not implemented - always throws org.omg.CORBA.NO_IMPLEMENT
    * @osm.note Need to establish and document a protocol for the expected behaviour. 
    *   Issues include: 1. how the desktop merged or embedded in the target user's 
    *   desktop. 2. a new desktop has to be assigned to this user.
    */
    public void set_belongs_to(org.omg.Session.User user)
    {
        throw new org.omg.CORBA.NO_IMPLEMENT();
    }
    
   /**
    * Returns the User that this Desktop is assigned to.
    * @return User - the user that this Desktop is owned by.
    */
    public User belongs_to()
    {
	  if( m_user != null ) return m_user;
        try
	  {
		byte[] UID = m_store.owner_short_pid();
	      m_user = m_user_service.getShortUserReference( UID );
            return m_user;
        }
	  catch( Throwable e )
        {
            final String error = "Unable to resolve a user reference.";
            throw new DesktopRuntimeException( error, e );
        }
    }
    
    // ==========================================================================
    // AbstractResource operation override
    // ==========================================================================
    
   /**
    *  The most derived type that this <code>AbstractResource</code> represents.
    */
    
    public TypeCode resourceKind()
    {
        return DesktopHelper.type();
    }
    
    // ==========================================================================
    // BaseBusinessObject operation override
    // ==========================================================================
    
   /**
    * DeskptopDelegate supplements the WorkspaceDelegate remove operation through
    * addition of debug log entries.
    */
    public synchronized void remove()
    throws NotRemovable
    {
        synchronized( m_store )
        {
            if( getLogger().isDebugEnabled() ) getLogger().debug("[DSK] remove");

		try
		{
                terminate();
		}
		catch(CannotTerminate e)
		{
		    final String warn = "failed to terminate resource";
		    if( getLogger().isWarnEnabled() ) getLogger().warn( warn, e );
		    throw new NotRemovable( e.getMessage() ); // should not happen
		}

		try
		{
 		    dispose();
		}
		catch(Exception e)
		{
		    final String error = "failed to dispose of resource";
		    if( getLogger().isErrorEnabled() ) getLogger().error( error, e );
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
    protected void setDesktopReference( Desktop desktop )
    {
        m_desktop = desktop;
        setWorkspaceReference( desktop );
    }

   /**
    * Returns the object reference for this delegate.
    * @return Desktop the object referenced for the delegate
    */
    protected Desktop getDesktopReference( )
    {
        return m_desktop;
    }

   /**
    * Returns a <code>DesktopAdapter</code>.
    * @return Adapter an instance of <code>DesktopAdapter</code>.
    */
    public Adapter get_adapter()
    {
        return new DesktopValue( m_desktop );
    }

}
