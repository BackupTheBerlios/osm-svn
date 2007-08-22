// Sun Dec 17 16:53:23 CET 2000

package net.osm.session.user;

import java.util.List;
import java.util.Iterator;

import org.omg.CosLifeCycle.NoFactory;
import org.omg.CosNaming.NameComponent;
import org.omg.Session.connect_state;
import org.omg.Session.TaskIterator;
import org.omg.Session.TaskIteratorHolder;
import org.omg.Session.TasksHolder;

import net.osm.session.message.SystemMessage;
import net.osm.session.desktop.DesktopValue;
import net.osm.session.desktop.DesktopAdapter;
import net.osm.session.desktop.DesktopHelper;
import net.osm.session.processor.ProcessorAdapter;
import net.osm.session.processor.ProcessorHelper;
import net.osm.session.task.Task;
import net.osm.session.task.TaskAdapter;
import net.osm.session.task.TaskValue;
import net.osm.session.task.TaskHelper;
import net.osm.session.resource.AbstractResourceAdapter;
import net.osm.session.util.AdapterIterator;
import net.osm.chooser.Chooser;
import net.osm.chooser.ChooserAdapter;
import net.osm.chooser.ChooserHelper;

/**
 * An adapter providing EJB style access to a <code>User</code> where 
 * the invoking client is the principal that this user object represents.
 */
public class PrincipalValue extends UserValue
implements PrincipalAdapter
{
    //=============================================================
    // static
    //=============================================================

    public static final String BASE_KEYWORD = "principal";

    /**
     * truncatable ids
     */
    static final String[] _ids_list =
    {
        "IDL:osm.net/session/user/PrincipalAdapter:1.0",
    };

    //=============================================================
    // state
    //=============================================================

   /**
    * Internal reference to the primary user.
    */
    private User m_user;

   /**
    * Internal refernce to the desktop adapter.
    */
    private DesktopAdapter m_desktop_adapter;
   
    //=============================================================
    // constructors
    //=============================================================
    
   /**
    * Default constructor.
    */
    public PrincipalValue( ) 
    {
    }

   /**
    * Creation of a new DefaultUserAdapter.
    * @param primary the primary <code>User</code> object reference
    */
    public PrincipalValue( User primary ) 
    {
	  super( primary );
    }

    //=============================================================
    // PrincipalAdapter
    //=============================================================

    /**
     * If the user is disconnected, the operation changes the 
     * connected state of the user to connected.
     */
    public void connect()
    {
        try
        {
            getLogger().debug("connecting");
            getPrimaryUser().connect();
        }
        catch( Throwable e )
        {
            getLogger().debug("connect error", e );
        }
    }

    /**
     * If the user is connected, the operation changes the 
     * connected state of the user to disconnected.
     */
    public void disconnect()
    {
        try
        {
            getLogger().debug("disconnecting");
            getPrimaryUser().disconnect();
        }
        catch( Throwable e )
        {
            getLogger().debug("disconnect error", e );
        }
    }

    /**
     * Returns the users desktop.
     * @return  DesktopAdapter the user's desktop.
     */
    public DesktopAdapter getDesktop()
    {
        if( m_desktop_adapter != null ) return m_desktop_adapter;

        try
        {
            m_desktop_adapter = (DesktopAdapter) DesktopHelper.narrow( 
               getPrimaryUser().get_desktop() ).get_adapter();
        }
        catch( Throwable e )
        {
            m_desktop_adapter = new DesktopValue( 
              getPrimaryUser().get_desktop() );
        }
        finally
        {
            if( m_desktop_adapter != null )
            {
                return m_desktop_adapter;
            }
            else
            {
                final String error = "Server returned a null desktop value.";
                throw new UserRuntimeException( error );
            }
        }
    }

    /**
     * Returns an iterator of tasks owned by the user.
     * @return Iterator of owned tasks
     */
    public Iterator getTasks()
    {
        TaskIteratorHolder iterator_holder = new TaskIteratorHolder();
        TasksHolder sequence_holder = new TasksHolder( new Task[0] );
        super.getPrimaryUser().list_tasks( 0, sequence_holder, iterator_holder );
        TaskIterator cos_iterator = iterator_holder.value;
        return new AdapterIterator( cos_iterator );
    }

    /**
     * Returns an iterator of message queue against the user.
     * @return Iterator of messages
     */
    public Iterator getMessages()
    {
        return null;
    }

    /**
     * Returns an iterator of folders managed by the user
     * @return Iterator of message folders
     */
    public Iterator getFolders()
    {
        return null;
    }

    /**
     * Returns an iterator of workspaces associated with the user.
     * @return Iterator of workspaces
     */
    public Iterator getWorkspaces()
    {
        return null;
    }

    /**
     * Returns an iterator of the resources contained with the users desktop.
     * This is a convinience accessor equivilant to retrival of the the desktop
     * and expansion of the desktop content.
     * @return Iterator of workspaces
     */
    public Iterator getContents()
    {
        return getDesktop().getContained();
    }

    /**
     * Creation of a new <code>TaskAdapter</code> using a supplied 
     * <code>ProcessorAdapter</code>.
     * @param  processor the processor to apply as the execution source
     */
    public TaskAdapter createTask( ProcessorAdapter processor )
    {
        return (TaskAdapter) TaskHelper.narrow( 
          getPrimaryUser().create_task( 
            "Untitled Task", 
            ProcessorHelper.narrow( processor.getPrimary() ), 
            null ) 
          ).get_adapter();
    }

    /**
     * Return an iterator of services available to this user.
     * @return Iterator the services iterator
     */
    public Iterator getServices()
    {
        try
        {
            final NameComponent name = new NameComponent("chooser","");
            org.omg.CORBA.Object[] objects =
              getPrimaryUser().find_factories( new NameComponent[]{ name } );
            if( objects.length < 1 )
            {
                throw new UserRuntimeException(
                  "Empty object array returned from find_factories operation." );
            }
            try
            {
                final Chooser chooser = ChooserHelper.narrow( objects[0] );
                final ChooserAdapter adapter = (ChooserAdapter) chooser.get_adapter();
                return adapter.getServices();
            }
            catch( Throwable e )
            {
                final String error = 
                  "Invalid object returned from find_factories operation.";
                throw new UserRuntimeException( error, e );
            }
        }
        catch( NoFactory nf )
        {
            throw new UserRuntimeException(
              "Unable to locate the default chooser.", nf );
        }
    }

    //=============================================================
    // Adapter
    //=============================================================

    /**
     * Suppliments the supplied <code>StringBuffer</code> with a short 
     * description of the adapted object.
     * @param  buffer the string buffer to append the description to
     * @param  lead a <code>String</code> that is prepended to content
     */
    public void report( StringBuffer buffer, String lead )
    {
        super.report( buffer, lead );
        buffer.append( "\n" + lead + "Desktop: " + getDesktop().getName() );
        buffer.append("\n" + lead + "Tasks: ");
        try
        {
            Iterator iterator = getTasks();
            while( iterator.hasNext() )
            {
                AbstractResourceAdapter adapter = (AbstractResourceAdapter) iterator.next();
                buffer.append( "\n" + lead + "  task://" 
                  + adapter.getDomain() + "/id=" + adapter.getIdentity() );
            }
        }
        catch( Throwable e )
        {
            getLogger().error( "task iteration error", e );
        }
        buffer.append( "\n" + lead + "Messages: " + getMessages() );
        buffer.append( "\n" + lead + "Folders: " + getFolders() );
        buffer.append( "\n" + lead + "Workspaces: " + getWorkspaces() );
    }

    /**
     * Return a URL for the adapter.
     */
     public String getURL()
     {
         return "principal?resolve=" + getIdentity();
     }

   /**
    * Returns the static base keyword for the entity.
    */
    public String getBase()
    {
        return BASE_KEYWORD;
    }


    //=============================================================
    // ValueBase
    //=============================================================

    /**
     * Return the truncatable ids
     * @return String[] truncatable ids
     */
    public String [] _truncatable_ids()
    {
        return _ids_list;
    }
}
