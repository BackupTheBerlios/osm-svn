
package net.osm.shell;

import java.awt.Component;
import java.awt.Font;
import javax.swing.ImageIcon;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;

import net.osm.shell.MGR;
import net.osm.shell.Entity;

public class DefaultCellRenderer extends DefaultTableCellRenderer 
{

    private static final EmptyBorder border = new EmptyBorder(1,5,1,3);

    private Font font;

   /**
    * Creation of a new cell renderer using the default font.
    */
    public DefaultCellRenderer( )
    {
        this( MGR.font );
    }

   /**
    * Creation of a new cell renderer using a supplied font.
    * @param font the default font to use when rendering components
    */
    public DefaultCellRenderer( Font font )
    {
        this.font = font;
    }

    public Component getTableCellRendererComponent(
	JTable table, Object object, boolean selected, boolean focus, int row, int column )
    {
	  JLabel label = (JLabel) super.getTableCellRendererComponent( table, object, selected, focus, row, column );

	  label.setBorder( border );
        label.setFont( font );
        label.setHorizontalAlignment( SwingConstants.LEFT );

	  if( object == null )
	  {
		label.setIcon( null );
		label.setText( null );
	      return label;
	  }
	  
	  if( object instanceof ImageIcon )
	  {
		label.setText( null );
		label.setIcon( (ImageIcon) object );
		return label;
	  }
	  else if( object instanceof Boolean ) 
	  {
		JCheckBox checkBox = new JCheckBox();
		checkBox.setSelected( ((Boolean)object).booleanValue() );
		if( selected )
		{
	          checkBox.setForeground(table.getSelectionForeground());
	          checkBox.setBackground(table.getSelectionBackground());
            }
		else
	      {
	          checkBox.setForeground(table.getForeground());
	          checkBox.setBackground(table.getBackground());
		}
		checkBox.setBorder( new EmptyBorder(0,20,0,10) );
		checkBox.setHorizontalAlignment(JCheckBox.LEFT);
		return checkBox;
	  }
	  else if( object instanceof Entity ) 
	  {
		final Entity entity = (Entity) object;
		label.setIcon( entity.getIcon( Entity.SMALL ) );
		label.setText( entity.getName( ) );
            return label; 
        }
	  else
	  {
            return label;
	  }
    }
}

