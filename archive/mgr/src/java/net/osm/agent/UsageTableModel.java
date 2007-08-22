
package net.osm.agent;

import java.util.List;
import java.util.LinkedList;
import java.util.Iterator;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableColumn;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;

import org.omg.CollaborationFramework.InputDescriptor;

import net.osm.shell.Entity;
import net.osm.shell.EntityTable;
import net.osm.shell.DefaultCellRenderer;

/**
 * A usage descriptor table data model.
 */

class UsageTableModel extends AbstractTableModel implements EntityTable, PropertyChangeListener
{

    //=========================================================================
    // static
    //=========================================================================

    public static final int VALUE = -2;
    public static final int ICON = 0;
    public static final int TAG = 1;
    public static final int ASSIGNMENT = 2;

    private static final int COLUMN_COUNT = 3;

    //=========================================================================
    // state
    //=========================================================================

    private List usage;

    private ProcessorModelAgent model;

    //=========================================================================
    // constructor
    //=========================================================================

   /**
    * Creation of a new UsageTableModel based on a supplied list
    * of UsageDescriptorAgent instances.
    * @param usage list of usage descriptors
    */
    public UsageTableModel ( ProcessorModelAgent model ) {
        super();
        this.model = model;
        usage = model.getDescriptors();
	  Iterator iterator = usage.iterator();
	  while( iterator.hasNext() )
	  {
		UsageDescriptorAgent uda = (UsageDescriptorAgent) iterator.next();
		uda.addPropertyChangeListener( this );
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
	  if( event.getPropertyName().equals("assignment") ) fireTableDataChanged();
    }


    //=========================================================================
    // AbstractTableModel (override)
    //=========================================================================

    public int getColumnCount() 
    { 
        return COLUMN_COUNT;
    }

    public int getRowCount()
    { 
        return usage.size();
    }

    public Object getValueAt(int row, int col) 
    { 
	  UsageDescriptorAgent agent = (UsageDescriptorAgent) usage.get( row );
        if( agent == null ) return "";
	  switch(col)
        {
	      case ICON : 
		    return agent.getDefaultIcon();
		case TAG : 
		    return agent.getTag();
	      case ASSIGNMENT: 
		    return agent.getAssignment();
		default: 
		    return agent;
	  }
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
        return (Entity) getValueAt( row, VALUE );
    }

    //=========================================================================
    // utilities
    //=========================================================================

   /**
    * Creation of a new table column model for usage descriptors
    */
    public static TableColumnModel createTableColumnModel()
    {
        TableColumn io = new TableColumn(0, 30, new DefaultCellRenderer(), null );
	  TableColumn tag = new TableColumn(1, 60, new DefaultCellRenderer(), null );
	  TableColumn assignment = new TableColumn(2, 300, new UsageCellRenderer(), null );

	  io.setHeaderValue("");
	  io.setMaxWidth( 30 );
	  io.setResizable( false );
	  io.setMaxWidth( 30 );
	  io.setMinWidth( 30 );

	  tag.setHeaderValue("Tag");
	  assignment.setHeaderValue("Assignment");

	  TableColumnModel columns = new DefaultTableColumnModel();
	  columns.addColumn( io );
	  columns.addColumn( tag );
	  columns.addColumn( assignment );

	  return columns;
    }

}
