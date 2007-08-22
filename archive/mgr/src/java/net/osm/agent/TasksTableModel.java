
package net.osm.agent;

import java.util.List;
import java.util.Iterator;
import java.util.LinkedList;
import javax.swing.table.AbstractTableModel;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;

import org.apache.avalon.framework.logger.Logger;

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
import net.osm.util.ListEvent;
import net.osm.util.ListHandler;
import net.osm.util.ListListener;
import net.osm.util.ExceptionHelper;

/**
 * A table model backed by a list of Task agents. 
 */
public class TasksTableModel extends LinkTableModel
{

    //=========================================================================
    // static
    //=========================================================================

    public static final int STATE = 3;

    private static final int COLUMNS = 4;

    //=========================================================================
    // state
    //=========================================================================

   /**
    * The UserAgent that this model is viewing.
    */
    private UserAgent user;

   /**
    * A list supporting the ListHandler interface that consitutes the source of
    * the table data.
    */
    private List list;

    //=========================================================================
    // constructor
    //=========================================================================

   /**
    * Constructor of a new <code>TasksTableModel</code> model.
    *
    * @param user - the user agent from which the underlying list will be established
    */
    public TasksTableModel( LinkCollection list )
    {
        super( list );
        this.list = list;
    }

    //=========================================================================
    // AbstractTableModel (override)
    //=========================================================================

    public int getColumnCount()
    {
        return COLUMNS;
    }

   /**
    * Returns either a object for presentation given a row/column index.
    * @param row the row index.  
    * @param column the column index
    * @return Object object at index
    */
    public Object getValueAt( int row, int column )
    {
	  if( column != STATE ) return super.getValueAt( row, column );
	  TaskAgent task = (TaskAgent) ((LinkAgent)list.get( row )).getTarget( );
        if( task == null ) return null;
        return task.getState();
    }
}
