
package net.osm.session.task;

import java.util.List;
import java.util.Iterator;

import org.omg.Session.task_state;
import org.omg.Session.AbstractResourceIterator;
import org.omg.Session.AbstractResourceIteratorHolder;
import org.omg.Session.AbstractResourcesHolder;
import org.omg.Session.LinkIteratorHolder;
import org.omg.Session.ResourceUnavailable;

import net.osm.session.util.AdapterIterator;
import net.osm.session.resource.AbstractResource;
import net.osm.session.resource.AbstractResourceHelper;
import net.osm.session.resource.AbstractResourceAdapter;
import net.osm.session.resource.AbstractResourceValue;
import net.osm.session.user.UserAdapter;
import net.osm.session.user.UserValue;
import net.osm.session.user.UserHelper;

/**
 * An adapter providing EJB style access to a <code>Task</code>.
 */
public class TaskValue extends AbstractResourceValue
implements TaskAdapter
{
    //=============================================================
    // static
    //=============================================================

    public static final String BASE_KEYWORD = "task";

    /**
     * truncatable ids
     */
    static final String[] _ids_list =
    {
        "IDL:osm.net/session/task/TaskAdapter:1.0",
    };

    //=============================================================
    // state
    //=============================================================

   /**
    * Internal reference to the task backing the adapter.
    */
    private Task m_task;

   /**
    * Cached referenced to the task owner.
    */
    private UserAdapter m_owner_adapter;
    
    //=============================================================
    // constructors
    //=============================================================
    
   /**
    * Default constructor.
    */
    public TaskValue( ) 
    {
    }

   /**
    * Creation of a new DefaultTaskAdapter.
    * @param primary the <code>Task</code> object reference 
    *   backing the adapter
    */
    public TaskValue( Task primary ) 
    {
	  super( primary );
    }

    //=============================================================
    // TaskValue
    //=============================================================

    /**
     * Returns the primary object reference.
     * @return org.omg.Session.Workspace the primary workspace
     */
    public Task getPrimaryTask()
    {
        if( m_task != null ) return m_task;
        m_task = TaskHelper.narrow( m_primary );
        return m_task;
    }

    //=============================================================
    // AbstractTaskAdapter
    //=============================================================

    /**
     * Returns the <code>UserAdapter</code> of the user that owns the task.
     * @return  UserAdapter the owner of the task
     */
    public UserAdapter getOwner()
    {
        try
        {
            if( m_owner_adapter != null ) return m_owner_adapter;
            return (UserAdapter) UserHelper.narrow( 
              getPrimaryTask().owned_by() ).get_adapter();
        }
        catch( Throwable e )
        {
            throw new TaskRuntimeException( 
              "Unexpected exception while attempting to resolve task owner.", e );
        }
    }

    /**
     * Returns the <code>AbstractResourceAdapter</code> that is handling the procesing
     * of this task.
     */
    public AbstractResourceAdapter getProcessor()
    {
        try
        {
            return (AbstractResourceAdapter) AbstractResourceHelper.narrow( 
              getPrimaryTask().get_processor() ).get_adapter();
        }
        catch( Throwable e )
        {
            throw new TaskRuntimeException( 
              "Unexpected exception while attempting to resolve task processor.", e );
        }
    }


    /**
     * Adds a resource to the set of resources consumed by this task.
     * @param  resource the rersource to add to the set of consumed task
     * @param  role the role of the resource within the scope of the task
     */
    public void addConsumed( AbstractResourceAdapter resource, String role )
    {
        try
        {
            getPrimaryTask().add_consumed( 
              AbstractResourceHelper.narrow( resource.getPrimary() ), 
              role );
        }
        catch( Throwable e )
        {
            throw new TaskRuntimeException( 
              "Remote exception while adding consumed resource.", e );
        }
    }

    /**
     * Removes a resource to the set of resources consumed by this task.
     * @param  role the resource role
     */
    public void removeConsumed( String role )
    {
        try
        {
            getPrimaryTask().remove_consumed( role );
        }
        catch( Throwable e )
        {
            throw new TaskRuntimeException( 
              "Remote exception while removing consumed resource.", e );
        }
    }

    /**
     * Returns an consumed resource by name.
     * @param role the consumption role
     * @return  AbstractResourceAdapter the consumed resources matching the supplied role
     */
    public AbstractResourceAdapter getConsumedByRole( String role ) throws ResourceUnavailable
    {
        try
        {
            return (AbstractResourceAdapter) 
              net.osm.session.resource.AbstractResourceHelper.narrow( 
                getPrimaryTask().get_consumed( role ) ).get_adapter();
        }
        catch( ResourceUnavailable e )
        {
            throw e;
        }
        catch( Throwable e )
        {
            throw new TaskRuntimeException( 
              "Remote exception while resoving consumed resource.", e );
        }
    }

    /**
     * Returns an iterator of resources consumed by the task.
     * @return Iterator of consumed resources
     */
    public Iterator getConsumed()
    {
        try
        {
            AbstractResourceIteratorHolder iterator_holder = new AbstractResourceIteratorHolder();
            LinkIteratorHolder link_iterator_holder = new LinkIteratorHolder ();
            AbstractResourcesHolder sequence_holder = new AbstractResourcesHolder( new AbstractResource[0] );
            getPrimaryTask().list_consumed( 
              0, sequence_holder, iterator_holder, link_iterator_holder );
            AbstractResourceIterator cos_iterator = iterator_holder.value;
            return new AdapterIterator( cos_iterator );
        }
        catch( Throwable e )
        {
            throw new TaskRuntimeException( 
              "Remote exception while retrieving consumed collection.", e );
        }
    }

    /**
     * Adds a resource to the set of resources produced by this task.
     * @param  resource the rersource to add to the set of produced resources
     * @param  role the role of the resource within the scope of the task
     */
    public void addProduced( AbstractResourceAdapter resource, String role )
    {
        try
        {
            getPrimaryTask().add_produced( 
              AbstractResourceHelper.narrow( resource.getPrimary() ), role );
        }
        catch( Throwable e )
        {
            throw new TaskRuntimeException( 
              "Remote exception while adding a produced resource.", e );
        }
    }

    /**
     * Removes a resource to the set of resources produced by this task.
     * @param  role the resource role
     */
    public void removeProduced( AbstractResourceAdapter resource )
    {
        try
        {
            getPrimaryTask().remove_produced( 
              AbstractResourceHelper.narrow( resource.getPrimary() ) );
        }
        catch( Throwable e )
        {
            throw new TaskRuntimeException( 
              "Remote exception while removing a produced resource.", e );
        }
    }

    /**
     * Returns an iterator of resources produced by the task.
     * @return Iterator of produced resources
     */
    public Iterator getProduced()
    {
        try
        {
            AbstractResourcesHolder sequence_holder = new AbstractResourcesHolder( new AbstractResource[0] );
            AbstractResourceIteratorHolder iterator_holder = new AbstractResourceIteratorHolder();
            LinkIteratorHolder link_iterator_holder = new LinkIteratorHolder ();
            getPrimaryTask().list_produced( 
              0, sequence_holder, iterator_holder, link_iterator_holder );
            AbstractResourceIterator cos_iterator = iterator_holder.value;
            return new AdapterIterator( cos_iterator );
        }
        catch( Throwable e )
        {
            throw new TaskRuntimeException( 
              "Remote exception while retriving the produced collection.", e );
        }
    }

   /**
    * Returns the task state.
    * @return task_state the task state
    */
    public task_state getState()
    {
        try
        {
            return getPrimaryTask().get_state();
        }
        catch( Throwable e )
        {
            throw new TaskRuntimeException( 
              "Remote exception while retriving task state.", e );
        }
    }

   /**
    * Returns the task description
    * @return String the task description
    */
    public String getDescription()
    {
        try
        {
            return getPrimaryTask().description();
        }
        catch( Throwable e )
        {
            throw new TaskRuntimeException( 
               "Remote exception while retriving task description.", e );
        }
    }


    //=============================================================
    // Startable
    //=============================================================

    public void start() throws Exception
    {
        try
        {
            getPrimaryTask().start();
        }
        catch( Throwable e )
        {
            throw new TaskException( 
              "Remote exception while attempting to start the task.", e );
        }
    }
    
    public void stop() throws Exception
    {
        try
        {
            getPrimaryTask().stop();
        }
        catch( Throwable e )
        {
            throw new TaskException( 
              "Remote exception while attempting to stop the task.", e );
        }
    }
    
    public void suspend()
    {
        try
        {
            getPrimaryTask().suspend();
        }
        catch( Throwable e )
        {
            throw new TaskRuntimeException( 
              "Remote exception while attempting to suspend the task.", e );
        }
    }
    
    public void resume()
    {
        try
        {
            getPrimaryTask().start();
        }
        catch( Throwable e )
        {
            throw new TaskRuntimeException( 
              "Remote exception while attempting to resume the task.", e );
        }
    }

    //=============================================================
    // Adapter
    //=============================================================

    /**
     * Return a URL for the adapter.
     */
     public String getURL()
     {
         return "task?resolve=" + getIdentity();
     }

   /**
    * Returns the static base keyword for the entity.
    */
    public String getBase()
    {
        return BASE_KEYWORD;
    }


    /**
     * Suppliments the supplied <code>StringBuffer</code> with a short 
     * description of the adapted object.
     * @param  buffer the string buffer to append the description to
     * @param  lead a <code>String</code> that is prepended to content
     */
    public void report( StringBuffer buffer, String lead )
    {
        super.report( buffer, lead );
        buffer.append( "\n" + lead + "Owner: " + getOwner().getName() );
        buffer.append( "\n" + lead + "Consuming: " + getConsumed() );
        buffer.append( "\n" + lead + "Producing: " + getProduced() );
        buffer.append( "\n" + lead + "State: " + getState().toString() );
    }

    //=============================================================
    // ValueBase
    //=============================================================

   /**
    * Rerturns the truncatable ids identifying this valuetype.
    * @return String[] truncatable ids
    */
    public String [] _truncatable_ids()
    {
        return _ids_list;
    }

}
