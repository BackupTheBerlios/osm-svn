
package net.osm.session.workspace;

import java.util.List;
import java.util.Iterator;
import org.omg.Session.AbstractResource;
import org.omg.Session.AbstractResourceHelper;
import org.omg.Session.AbstractResourceIterator;
import org.omg.Session.AbstractResourceIteratorHolder;
import org.omg.Session.AbstractResourcesHolder;
import org.omg.Session.User;

import net.osm.session.util.AdapterIterator;
import net.osm.session.resource.AbstractResourceAdapter;
import net.osm.session.resource.AbstractResourceValue;

/**
 * An adapter providing EJB style access to a <code>Workspace</code>.
 */
public class WorkspaceValue extends AbstractResourceValue
implements WorkspaceAdapter
{
    //=============================================================
    // static
    //=============================================================

    public static final String BASE_KEYWORD = "workspace";

    /**
     * truncatable ids
     */
    static final String[] _ids_list =
    {
        "IDL:osm.net/session/workspace/WorkspaceAdapter:1.0",
    };

    //=============================================================
    // state
    //=============================================================

   /**
    * Internal reference to the workspace backing the adapter.
    */
    org.omg.Session.Workspace m_workspace;
    
    //=============================================================
    // constructors
    //=============================================================
    
   /**
    * Default constructor.
    */
    public WorkspaceValue( ) 
    {
    }

   /**
    * Creation of a new DefaultWorkspaceAdapter.
    * @param primary the <code>Workspace</code object reference 
    *   backing the adapter
    */
    public WorkspaceValue( org.omg.Session.Workspace primary ) 
    {
	  super( primary );
    }

    //=============================================================
    // DefaultWorkspaceAdapter
    //=============================================================

    /**
     * Returns the primary object reference.
     * @return org.omg.Session.Workspace the primary workspace
     */
    public org.omg.Session.Workspace getPrimaryWorkspace()
    {
        if( m_workspace != null ) return m_workspace;
        m_workspace = org.omg.Session.WorkspaceHelper.narrow( getPrimary() );
        return m_workspace;
    }

    //=============================================================
    // WorkspaceAdapter
    //=============================================================

    /**
     * Creation of a new sub-workspace within this workspace.
     * @return WorkspaceAdapter the sub-workspace
     */
    public WorkspaceAdapter createSubWorkspace( )
    {
        return createSubWorkspace( "Untitled Workspace" );
    }

    /**
     * Creation of a new sub-workspace within this workspace.
     * @param  name the name to assign to the sub-workspace
     */
    public WorkspaceAdapter createSubWorkspace( String name )
    {
        if( getLogger().isDebugEnabled() ) getLogger().debug("createSubWorkspace: " + name );
        return (WorkspaceAdapter) net.osm.session.workspace.WorkspaceHelper.narrow( 
          getPrimaryWorkspace().create_subworkspace( "Untitled Workspace", new User[0] ) ).get_adapter();
    }

    /**
     * Add a resource to this workspace.
     * @param resource the <code>AbstractResourceAdapter</code> to add to the workspace
     */
    public void addResource( AbstractResourceAdapter resource )
    {
        getPrimaryWorkspace().add_contains_resource( 
          AbstractResourceHelper.narrow( resource.getPrimary() ) );
    }

    /**
     * Removes a resource from this workspace.
     * @param resource the <code>AbstractResourceAdapter</code> to remove from the workspace
     */
    public void removeResource( AbstractResourceAdapter resource )
    {
        getPrimaryWorkspace().remove_contains_resource( 
          AbstractResourceHelper.narrow( resource.getPrimary() ) );
    }

    /**
     * Returns an iterator of resources contained by workspace.
     * @return Iterator of the list of resources contained within the workspace
     */
    public Iterator getContained()
    {
        AbstractResourcesHolder sequence_holder = new AbstractResourcesHolder( new AbstractResource[0] );
        AbstractResourceIteratorHolder iterator_holder = new AbstractResourceIteratorHolder();
        getPrimaryWorkspace().list_resources_by_type( 
          AbstractResourceHelper.type(), 0, sequence_holder, iterator_holder );
        return new AdapterIterator( iterator_holder.value );
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
        buffer.append( "\n" + lead + "Contains: " + getContained() );
    }

    /**
     * Return a URL for the adapter.
     */
     public String getURL()
     {
         return "workspace?resolve=" + getIdentity();
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
    * Returns the truncatable ids identifying this valuetype.
    * @return String[] truncatable ids
    */
    public String [] _truncatable_ids()
    {
        return _ids_list;
    }

}
