
package net.osm.agent;

import java.awt.Component;
import java.awt.Font;
import javax.swing.ImageIcon;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

import net.osm.shell.DefaultCellRenderer;
import net.osm.shell.Entity;

/**
 * A cell render that renders a UsageAgents based on the usage target.
 */ 
public class UsageCellRenderer extends DefaultCellRenderer
{

   /**
    * Creation of a new UsageAgent cell renderer.
    */
    public UsageCellRenderer( )
    {
        super();
    }

    public Component getTableCellRendererComponent(
	JTable table, Object object, boolean selected, boolean focus, int row, int column )
    {
	  JLabel label = (JLabel) super.getTableCellRendererComponent( table, object, selected, focus, row, column );
	  if( object == null )
	  {
		label.setIcon( null );
		label.setText( null );
	      return label;
	  }
	  if( !( object instanceof UsageAgent )) return label;
        final UsageAgent agent = (UsageAgent) object;
        label.setIcon( agent.getTarget().getIcon( Entity.SMALL ));
        label.setText( agent.getTarget().getName() );
        return label;
    }
}

