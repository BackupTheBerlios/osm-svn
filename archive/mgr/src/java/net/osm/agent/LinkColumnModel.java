
package net.osm.agent;

import java.awt.Font;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableColumn;

import net.osm.shell.DefaultCellRenderer;

/**
 * LinkColumnModel is a utility class defining the table column structure
 * for the presentation of Link instances.
 *
 * @author Stephen McConnell
 */
public class LinkColumnModel extends DefaultTableColumnModel
{

    //=========================================================================
    // constructor
    //=========================================================================

   /**
    * Constructor of a new <code>ContainsColumnModel</code> model.
    */
    public LinkColumnModel( Font font )
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

	  final TableColumn modified = new TableColumn(2, 100, new DefaultCellRenderer( font ), null );
	  modified.setHeaderValue("Modified");
	  modified.setResizable( false );
	  modified.setMaxWidth( 160 );
	  modified.setMinWidth( 160 );

	  addColumn( icon );
	  addColumn( name );
	  addColumn( modified );
    }
}
