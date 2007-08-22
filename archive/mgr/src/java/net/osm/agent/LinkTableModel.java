
package net.osm.agent;

import java.util.List;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Date;
import javax.swing.table.AbstractTableModel;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;

import org.omg.CORBA.ORB;
import org.omg.CORBA.TypeCode;
import org.omg.Session.Link;
import org.omg.Session.LinkHelper;
import org.omg.Session.LinksHolder;
import org.omg.Session.LinkIterator;
import org.omg.Session.AbstractResource;

import net.osm.agent.util.CollectionIterator;
import net.osm.agent.Agent;
import net.osm.agent.AgentService;
import net.osm.agent.AgentService;
import net.osm.audit.RemoteEventListener;
import net.osm.audit.RemoteEvent;
import net.osm.audit.home.Adapter;
import net.osm.shell.ContextEvent;
import net.osm.shell.ContextListener;
import net.osm.shell.Panel;
import net.osm.shell.Entity;
import net.osm.shell.EntityTable;
import net.osm.util.ListEvent;
import net.osm.util.ListHandler;
import net.osm.util.ListListener;
import net.osm.util.ExceptionHelper;

/**
 * A table model backed by a active list of link entries.  The table model
 * propergates feature changes and handles table updating based on list 
 * additions and removals.
 */
public class LinkTableModel extends AbstractTableModel implements ListListener, PropertyChangeListener, EntityTable
{

    //=========================================================================
    // static
    //=========================================================================

    public static final int LINK_VALUE = -2;
    public static final int ENTITY_VALUE = -1;
    public static final int ICON = 0;
    public static final int NAME = 1;
    public static final int MODIFIED = 2;

    private static final int COLUMNS = 3;
    private static final boolean trace = true;

    //=========================================================================
    // state
    //=========================================================================

   /**
    * The List backing the table.
    */
    private LinkCollection list;

    //=========================================================================
    // constructor
    //=========================================================================

   /**
    * Constructor of a new <code>LinkTableModel</code> based on a supplied
    * list argument.
    *
    * @param list - the list to use as the data source for the table
    */
    public LinkTableModel( LinkCollection list )
    {
        super();
	  if( list == null ) throw new NullPointerException("null list argument");
	  this.list = list;

	  synchronized( this.list )
	  {
		Iterator iterator = list.iterator();
		while( iterator.hasNext() )
	      {
		    LinkAgent link = (LinkAgent) iterator.next();
		    AbstractResourceAgent agent = (AbstractResourceAgent) link.getTarget();
		    if( agent != null ) agent.addPropertyChangeListener( this );
		}
            ((ListHandler)list).addListListener( this );
	  }
    }

    //===============================================================
    // PropertyChangeListener
    //===============================================================
    
   /**
    * Property event change handler that handles a property change event 
    * from an entity referenced by a link within the list.  The implementation 
    * locates the entities link position and trigger an update of that row in 
    * the table.
    *
    * @param event the property change event
    */
    public void propertyChange( PropertyChangeEvent event )
    {
	  if( event.getPropertyName().equals("status") ) return;
	  Entity entity = (Entity) event.getSource();
        synchronized( list )
	  {
            int n = list.indexOf( entity );
		if( n > -1 ) fireTableRowsUpdated( n, n );
	  }
    }

    //=========================================================================
    // ListEventListener
    //=========================================================================

   /**
    * Method invoked when an object is added to a list.  
    */
    public synchronized void addObject( ListEvent event )
    {
	  LinkAgent link = (LinkAgent) event.getObject();
	  int n = list.indexOf( link );
	  AbstractResourceAgent agent = (AbstractResourceAgent) link.getTarget();
	  if( agent != null ) agent.addPropertyChangeListener( this );
        fireTableRowsInserted( n, n );
    }

   /**
    * Method invoked when an object is removed from the list.  
    */
    public void removeObject( ListEvent event )
    {
	  LinkAgent link = (LinkAgent) event.getObject();
	  if( link.getTarget() != null ) link.getTarget().removePropertyChangeListener( this );
        fireTableDataChanged();
    }

    //=========================================================================
    // EntityTable
    //=========================================================================

   /**
    * Returns the entity at a particular row.
    * @return Entity the at the row (possibly null)
    */
    public Entity getEntityAtRow( int row )
    {
        return (Entity) getValueAt( row, LINK_VALUE );
    }

    //=========================================================================
    // AbstractTableModel (override)
    //=========================================================================

    public int getRowCount()
    {
        return list.size();
    }

    public int getColumnCount()
    {
        return COLUMNS;
    }

    public Object getValueAt( int row, int column )
    {
        LinkAgent link = (LinkAgent) list.get( row );
        if( column == LINK_VALUE ) return link;
	  AbstractResourceAgent agent = (AbstractResourceAgent) link.getTarget( );
        if( agent == null ) return null;
        synchronized( agent )
	  {
            switch( column )
            {
            case ICON:
                return agent.getIcon( Entity.SMALL );
            case NAME:
                return agent.getName();
            case MODIFIED:
                return agent.getModification();
            case LINK_VALUE:
                return link;
            default:
		    return agent; // ENTITY_VALUE
            }
	  }
    }

   /**
    * JTable uses this method to determine the default renderer/
    * editor for each cell.
    */
    public Class getColumnClass( int column ) 
    {
        switch( column )
        {
            case ICON:
                return javax.swing.ImageIcon.class;
            case NAME:
                return String.class;
            case MODIFIED:
                return Date.class;
            default:
		    return String.class;
        } 
    }
}
