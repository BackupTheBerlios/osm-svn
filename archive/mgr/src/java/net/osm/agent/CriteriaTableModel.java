
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

import net.osm.shell.ContextEvent;
import net.osm.shell.ContextListener;
import net.osm.shell.Panel;
import net.osm.shell.Entity;
import net.osm.util.ListEvent;
import net.osm.util.ListHandler;
import net.osm.util.ListListener;
import net.osm.util.ExceptionHelper;
import net.osm.agent.util.CollectionIterator;
import net.osm.agent.Agent;
import net.osm.agent.AgentService;
import net.osm.agent.AgentService;
import net.osm.audit.RemoteEventListener;
import net.osm.audit.RemoteEvent;
import net.osm.audit.home.Adapter;

/**
 * A table model backed by a list of Criteria descriptions. 
 */
public class CriteriaTableModel extends AbstractTableModel
{

    //=========================================================================
    // static
    //=========================================================================

    private static final boolean trace = true;

    public static final int ICON = 0;

    public static final int NAME = 1;
    
    public static final int TYPE = 2;

    private static final int COLUMNS = 3;

    //=========================================================================
    // state
    //=========================================================================

   /**
    * The List backing the table.
    */
    private List list;

    //=========================================================================
    // constructor
    //=========================================================================

   /**
    * Constructor of a new <code>CriteriaTableModel</code> based on a supplied
    * list argument.
    *
    * @param list - the list to use as the data source for the table
    */
    public CriteriaTableModel( List list )
    {
        super();
	  this.list = list;
    }

    //=========================================================================
    // AbstractTableModel (override)
    //=========================================================================

    public String getColumnName( int column )
    {
        switch( column )
        {
            case ICON:
                return "";
            case NAME:
                return "Name";
            case TYPE:
                return "Type";
            default:
		    return "";
        }
    }

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
	  CriteriaAgent agent = (CriteriaAgent) list.get( row );
        if( agent == null ) return null;
        synchronized( agent )
	  {
          switch( column )
          {
            case ICON:
                return agent.getIcon( Entity.SMALL );
            case NAME:
                return agent.getName();
            case TYPE:
                return agent.getKind();
            default:
		    return agent;
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
            default:
		    return String.class;
        } 
    }
}
