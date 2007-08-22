
package net.osm.agent;

import java.awt.Font;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableColumn;

import net.osm.shell.DefaultCellRenderer;

/**
 * TasksColumnModel is a utility class defining the table column structure
 * for the presentation of Task agents.
 *
 * @author Stephen McConnell
 */
public class TasksColumnModel extends DefaultTableColumnModel
{

    //=========================================================================
    // constructor
    //=========================================================================

   /**
    * Constructor of a new <code>TasksColumnModel</code> model.
    *
    * @param user - the user agent from which the underlying list will be established
    */
    public TasksColumnModel( Font font )
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

	  final TableColumn status = new TableColumn(3, 160, new DefaultCellRenderer( font ), null );
	  status.setHeaderValue("Status");
	  status.setResizable( false );
	  status.setMaxWidth( 160 );
	  status.setMinWidth( 160 );

	  addColumn( icon );
	  addColumn( name );
	  addColumn( status );
    }
}
