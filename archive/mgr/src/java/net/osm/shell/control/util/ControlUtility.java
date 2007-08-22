
package net.osm.shell.control.util;

import java.awt.Dimension;
import java.awt.Component;
import javax.swing.JPanel;
import javax.swing.JDialog;
import javax.swing.border.EtchedBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

/**
 */
public class ControlUtility
{

    private static JPanel createSeparator( int width )
    {
        //
        // Create a line seperator
        //

        JPanel line = new JPanel();
        line.setBorder( new EtchedBorder( EtchedBorder.LOWERED ) );
        line.setPreferredSize( new Dimension( width, 2 ));
	  JPanel lineHolder = new JPanel( );
        lineHolder.add( line );
        lineHolder.setBorder( new EmptyBorder( 3, 10, 3, 10 ));
        return line;
    }

    public static JDialog getDialog( Component component )
    {
        if( component instanceof JDialog ) return (JDialog) component;
	  return (JDialog) SwingUtilities.getAncestorOfClass( JDialog.class, component);
    }

}
