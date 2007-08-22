
package net.osm.shell;

import java.awt.Component;
import java.awt.Font;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.JTree;

/**
 * Default Node cell renderer.
 */
public class NavigatorCellRenderer extends DefaultTreeCellRenderer 
{

   /**
    * Creation of a new Navigator cell renderer.
    */
    public NavigatorCellRenderer( )
    {
        super();
	  setFont( MGR.font);
        setBorderSelectionColor( null );
    }

    public Component getTreeCellRendererComponent( JTree tree, Object value,
      boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus ) 
    {
        JLabel label = (JLabel) super.getTreeCellRendererComponent( 
	    tree, value, sel, expanded, leaf, row, hasFocus );
	  if( value != null ) if( value instanceof DefaultMutableTreeNode )
	  {
		Object object = ((DefaultMutableTreeNode)value).getUserObject();
		if( object != null ) if( object instanceof Entity ) label.setIcon( 
		  ((Entity)object).getIcon( Entity.SMALL ));
        }
        return label;
    }
}

