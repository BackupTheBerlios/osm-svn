
package net.osm.session.resource;

import java.util.Date;
import java.util.List;
import java.util.Iterator;
import java.io.Serializable;

import org.apache.time.TimeUtils;

import org.omg.CORBA.TypeCode;
import org.omg.CORBA.portable.StreamableValue;
import org.omg.CORBA.portable.InputStream;
import org.omg.CORBA.portable.OutputStream;
import org.omg.CORBA.portable.ValueFactory;
import org.omg.Session.WorkspaceHelper;
import org.omg.Session.WorkspaceIterator;
import org.omg.Session.WorkspaceIteratorHolder;
import org.omg.Session.WorkspacesHolder;
import org.omg.Session.WorkspaceIteratorHolder;
import org.omg.Session.TaskIterator;
import org.omg.Session.TaskIteratorHolder;
import org.omg.Session.TasksHolder;
import org.omg.Session.Workspace;

import net.osm.adapter.AdapterValue;
import net.osm.session.util.AdapterIterator;
import net.osm.session.task.Task;
import net.osm.session.task.TaskValue;
import net.osm.session.task.TaskHelper;
import net.osm.session.task.TaskAdapter;

/**
 * An adapter for an <code>AbstractResource</code> that provides 
 * state accessors that follow the EJB patterns.
 */
public class AbstractResourceValue extends AdapterValue
implements AbstractResourceAdapter
{

    public static final String BASE_KEYWORD = "resource";

    //=============================================================
    // state
    //=============================================================
    
    private org.omg.Session.AbstractResource m_resource;

    //=============================================================
    // constructors
    //=============================================================
 
   /**
    * Default constructor.
    */
    public AbstractResourceValue( ) 
    {
    }
   
   /**
    * Creation of a new AbstractResourceValue.
    * @param resource the resource backing the adapter
    */
    public AbstractResourceValue( org.omg.Session.AbstractResource resource ) 
    {
	  super( resource, null );
    }

    //=============================================================
    // AbstractResourceValue
    //=============================================================

    /**
     * Operation getAbstractResource
     */
    public org.omg.Session.AbstractResource getAbstractResource()
    {
        if( m_resource != null ) return m_resource;
        m_resource = org.omg.Session.AbstractResourceHelper.narrow( m_primary );
        return m_resource;
    }

    //=============================================================
    // AbstractResourceAdapter
    //=============================================================

    /**
     * Returns the name of the resource.
     * @return  String the resource name
     */
    public String getName()
    {
        return getAbstractResource().name();
    }

    /**
     * Returns the string representation of the domain managing this
     * resource.
     * @return  String the domain
     */
    public String getDomain()
    {
        return getAbstractResource().domain().naming_entity;
    }

    /**
     * Returns an integer value identifying this resources within the 
     * scope of the domain.
     * @return  int the resource identifier
     */
    public int getIdentity()
    {
        return getAbstractResource().constant_random_id();
    }

    /**
     * Sets the name of the resource to the supplied value.
     * @param  name the resource name
     */
    public void setName(String name)
    {
        getAbstractResource().name( name );
    }

    /**
     * Returns the resource creation date.
     * @return  Date the resource creation date
     */
    public Date getCreationDate()
    {
        return TimeUtils.convertToDate( getAbstractResource().creation() );
    }

    /**
     * Returns the resource modification date.
     * @return  Date the resource modification date
     */
    public Date getModificationDate()
    {
        return TimeUtils.convertToDate( getAbstractResource().modification() );
    }

    /**
     * Returns a list representing the set of tasks consuming the 
     * resource.
     * @return  List the list of tasks consuming this resource
     */
    public Date getAccessDate()
    {
        return TimeUtils.convertToDate( getAbstractResource().access() );
    }

    /**
     * Returns an iterator of task adapters consuming this resource.
     * @return Iterator of task adapters consuming this resource
     * @see net.osm.session.task.TaskAdapter
     */
    public Iterator getConsumers()
    {
        TaskIteratorHolder iterator_holder = new TaskIteratorHolder();
        TasksHolder sequence_holder = new TasksHolder( new Task[0] );
        getAbstractResource().list_consumers( 0, sequence_holder, iterator_holder );
        TaskIterator cos_iterator = iterator_holder.value;
        return new AdapterIterator( cos_iterator );
    }

    /**
     * Returns an iterator of workspace adapters referencing workspaces
     * that are holding refertences to the resource.
     * @return Iterator of workspace adapters containing this resource
     * @see net.osm.session.workspace.WorkspaceAdapter
     */
    public Iterator getContainers()
    {
        WorkspaceIteratorHolder iterator_holder = new WorkspaceIteratorHolder();
        WorkspacesHolder sequence_holder = new WorkspacesHolder( new Workspace[0] );
        getAbstractResource().list_contained( 0, sequence_holder, iterator_holder );
        WorkspaceIterator cos_iterator = iterator_holder.value;
        return new AdapterIterator( cos_iterator );
    }

    /**
     * Returns the task producing this resource or null of no attached producing task.
     * @return TaskAdapter the producing task or null
     */
    public net.osm.session.task.TaskAdapter getProducer()
    {
        Task task = TaskHelper.narrow( m_resource.get_producer() );
        if( task == null ) return null;
        return new TaskValue( task );
    }

    //=============================================================
    // Adapter
    //=============================================================

    /**
     * Return a URL for the adapter.
     */
     public String getURL()
     {
         return "?resolve=" + getIdentity();
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

        if( getLogger().isDebugEnabled() ) getLogger().debug("report");

        //
        // get the basic abstract resource state
        //

        buffer.append( "\n" + lead + "name: " + getAbstractResource().name() );
        try
        {
            buffer.append( "\n" + lead + "kind: " 
              + getAbstractResource().resourceKind().id() );
        }
        catch( Throwable e )
        {
        }
        buffer.append( "\n" + lead + "domain: " + getDomain() );
        buffer.append( "\n" + lead + "identity: " + getIdentity() );
        buffer.append( "\n" + lead + "creation: " + getCreationDate() );
        buffer.append( "\n" + lead + "modification: " + getModificationDate() );
        buffer.append( "\n" + lead + "access: " + getAccessDate() );

        //
        // list workspaces that contain this resource
        //

        buffer.append("\n" + lead + "Containment: ");
        try
        {
            Iterator iterator = getContainers();
            while( iterator.hasNext() )
            {
                AbstractResourceAdapter adapter = (AbstractResourceAdapter) iterator.next();
                buffer.append("\n\t" + lead + "container: " 
                  + adapter.getName() + " ["  
                  + adapter.getIdentity() + "]");
            }
        }
        catch( Throwable e )
        {
            buffer.append("\n\t" + lead + "error: " + e.toString() );
        }

        //
        // list task consuming this resource
        //

        buffer.append("\n" + lead + "Consumers: ");
        try
        {
            Iterator iterator = getConsumers();
            while( iterator.hasNext() )
            {
                AbstractResourceAdapter adapter = (AbstractResourceAdapter) iterator.next();
                buffer.append("\n\t" + lead + "container: " 
                  + adapter.getName() + " ["  
                  + adapter.getIdentity() + "]");
            }
        }
        catch( Throwable e )
        {
            buffer.append("\n\t" + lead + "error: " + e.toString() );
        }

        //
        // list resource producing this resource
        //

        buffer.append("\n" + lead + "Producer:");
        TaskAdapter producer = getProducer();
        if( producer != null )
        {
            buffer.append( producer.getName() );
        }
    }
}
