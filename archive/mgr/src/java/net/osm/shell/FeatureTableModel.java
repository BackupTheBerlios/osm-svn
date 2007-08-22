
package net.osm.shell;

import java.util.List;
import java.util.Iterator;
import javax.swing.table.AbstractTableModel;
import javax.swing.Icon;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;

import net.osm.util.IconHelper;

class FeatureTableModel extends AbstractTableModel implements PropertyChangeListener
{

    //======================================================
    // static
    //======================================================

   /**
    * Default small icon path.
    */
    private static final String path = "net/osm/shell/image/feature.gif";

   /**
    * Default small icon.
    */
    private Icon icon = IconHelper.loadIcon( path );

   /**
    * Constant row identifier for the icon.
    */
    public static final int ICON = 0;

   /**
    * Constant row identifier for the name.
    */
    public static final int NAME = 1;

   /**
    * Constant row identifier for the name.
    */
    public static final int VALUE = 2;


    //======================================================
    // state
    //======================================================
    
    private static final int COLUMN_COUNT = 3;

    private List list;

    private Entity entity;

    //======================================================
    // constructor
    //======================================================

    public FeatureTableModel ( Entity entity ) 
    {
        super();
        this.entity = entity;
        this.list = entity.getFeatures();
	  entity.addPropertyChangeListener( this );
    }

    //===============================================================
    // PropertyChangeListener
    //===============================================================
    
   /**
    * Property event change handler that handles a property change event 
    * from an entity backing a particular feature. 
    * @osm.note optimization by holding a reference to active list entries is pending
    * @param event the property change event
    */
    public void propertyChange( PropertyChangeEvent event )
    {
	  int n = locate( event.getPropertyName() );
	  if( n > -1 ) fireTableCellUpdated( n, VALUE );
    }

   /**
    * Locate the position of an active feature in the list.
    */
    private int locate( String key )
    {
	  int n = 0;
        Iterator iterator = list.iterator();
	  while( iterator.hasNext() )
	  {
		Object object = iterator.next();
	      if( object instanceof ActiveFeature )
		{
		    if( ((ActiveFeature)object).getPropertyName().equals( key )) return n;
		}
	      n++;
	  }
	  return -1;
    }

    //======================================================
    // FeatureTableModel
    //======================================================

   /**
    * Returns the number of model columns.
    * @return int the number of columns maintained by the model
    */
    public int getColumnCount()
    { 
        return COLUMN_COUNT;
    }

   /**
    * Returns the number of rows in the model.  The value returned is
    * equivilent to the number of elements in the list backing the model.
    * @return int the number of rows maintained by the model
    */
    public int getRowCount()
    { 
        return list.size();
    }

   /**
    * Returns the feature object at the request column and row combination.
    * If the col index is out of range the method returns the agent corresponding
    * to the row identifier.
    * @return Object
    */
    public Object getValueAt(int row, int col) 
    { 
	  Object result = "";
        Feature feature = getFeature( row );
	  if( feature != null ) switch(col)
        {
	      case ICON :
		    return icon;
	      case NAME : 
		    return feature.getName();
		case VALUE : 
		    return feature.getValue();
		default: 
		    return entity;
	  }
        return result;
    }

   /**
    * Returns an instance of feature given a supplied row.
    * @param row the row in the feature list
    */
    private Feature getFeature( int row )
    {
        return (Feature) list.get( row );
    }
}
