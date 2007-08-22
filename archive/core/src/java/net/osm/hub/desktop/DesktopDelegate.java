

package net.osm.hub.desktop;

import org.apache.avalon.framework.context.Context;
import org.apache.avalon.framework.component.ComponentException;
import org.apache.avalon.framework.component.ComponentManager;

import org.omg.CORBA.ORB;
import org.omg.CORBA.Any;
import org.omg.CORBA.TypeCode;
import org.omg.CORBA.NO_IMPLEMENT;
import org.omg.PortableServer.POA;
import org.omg.CosTime.TimeService;
import org.omg.CosLifeCycle.NotRemovable;
import org.omg.Session.DesktopOperations;
import org.omg.Session.Desktop;
import org.omg.Session.DesktopHelper;
import org.omg.Session.User;
import org.omg.Session.UserHelper;

import net.osm.hub.pss.DesktopStorage;
import net.osm.hub.workspace.WorkspaceDelegate;
import net.osm.hub.gateway.CannotTerminate;
import net.osm.hub.user.UserService;
import net.osm.list.LinkedList;
import net.osm.list.Iterator;
import net.osm.list.NoEntry;

/**
* The Desktop interface (implememted by this class) links Users to many Workspaces and 
* Workspaces to many Users. Each User has one Desktop and many Workspaces. Workspaces 
* may be shared so they may have many Users.
*/

public class DesktopDelegate extends WorkspaceDelegate 
implements DesktopOperations
{
    
   /**
    * Storage object representing this Desktop.
    */
    private DesktopStorage store;
    
   /**
    * Object reference to this Desktop.
    */
    private Desktop m_desktop;
    
   /**
    * Object reference to the owner of this Desktop
    */
    private User m_user;

    private UserService userService;

    //=================================================================
    // Composable
    //=================================================================
    
    /**
     * Pass the <code>ComponentManager</code> to the <code>Composable</code>.
     * The <code>Composable</code> implementation should use the specified
     * <code>ComponentManager</code> to acquire the components it needs for
     * execution.
     * @param manager The <code>ComponentManager</code> which this
     *                <code>Composer</code> uses.
     */
    public void compose( ComponentManager manager )
    throws ComponentException
    {
	  super.compose( manager );
	  this.userService = (UserService)manager.lookup("USER");
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
        if( getLogger().isDebugEnabled() ) getLogger().debug( "initialize (desktop)" );
        this.store = (DesktopStorage) super.getContext().getStorageObject();
        setDesktopReference( DesktopHelper.narrow( getManager().getReference( store.get_pid(), DesktopHelper.id() )));
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
        synchronized( store )
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
	  synchronized( store )
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
        this.store = null;
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
	  if( m_user == null ) try
	  {
		byte[] UID = store.owner_short_pid();
	      m_user = getUserService().getShortUserReference( UID );
        }
	  catch( Throwable e )
        {
		if( getLogger().isErrorEnabled() ) getLogger().error("failed to resolve a user reference", e );
            throw new org.omg.CORBA.INTERNAL();
        }
        return m_user;
    }
    
    // ==========================================================================
    // AbstractResource operation override
    // ==========================================================================
    
   /**
    *  The most derived type that this <code>AbstractResource</code> represents.
    */
    public TypeCode resourceKind()
    {
        getLogger().info("resourceKind");
        touch( store );
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
        synchronized( store )
        {
            if( getLogger().isDebugEnabled() ) getLogger().debug("remove (desktop)");
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
		    if( getLogger() != null ) 
		    {
		  	  if( getLogger().isErrorEnabled() ) getLogger().error( error, e );
		    }
		    else
	          {
		        System.err.println("unexpected error while disposing of resource");
		        e.printStackTrace();
		    }
		}
        }
    }

    // ==========================================================================
    // internal
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

    protected UserService getUserService()
    {
        return userService;
    }

}
