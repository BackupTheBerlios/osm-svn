
package net.osm.agent;

import java.awt.Font;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableColumn;

import net.osm.shell.DefaultCellRenderer;

/**
 * CriteriaColumnModel is a utility class defining the table column structure
 * for the presentation of Criteria instances.
 *
 * @author Stephen McConnell
 */
public class CriteriaColumnModel extends DefaultTableColumnModel
{

    //=========================================================================
    // constructor
    //=========================================================================

   /**
    * Constructor of a new <code>ContainsColumnModel</code> model.
    */
    public CriteriaColumnModel( Font font )
    {
        super();
        setColumnSelectionAllowed( false );

        final TableColumn icon = new TableColumn(0, 30, new DefaultCellRenderer( font ), null );
	  icon.setHeaderValue("");
	  icon.setMaxWidth( 30 );
	  icon.setMinWidth( 30 );
	  icon.setResizable( false );

	  final TableColumn name = new TableColumn(1, 300, new DefaultCellRenderer( font ), null );
	  name.setHeaderValue("Name");
	  name.setMinWidth( 100 );
        name.sizeWidthToFit();

	  final TableColumn type = new TableColumn(2, 100, new DefaultCellRenderer( font ), null );
	  type.setHeaderValue("Kind");
	  type.setResizable( true );
	  type.setMinWidth( 160 );

	  addColumn( icon );
	  addColumn( name );
	  addColumn( type );
    }
}
