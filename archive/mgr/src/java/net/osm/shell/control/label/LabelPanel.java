
package net.osm.shell.control.label;

import java.awt.Font;
import java.awt.Color;
import java.awt.FlowLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

import net.osm.shell.Desktop;

/**
 * The <code>LabelPanel</code> contains a single line of text.
 *
 * @author  Stephen McConnell
 * @version 1.0 22 AUG 2001
 */
public class LabelPanel extends JPanel
{
    //==========================================================
    // state
    //==========================================================

    private JLabel label;

    //==========================================================
    // constructor
    //==========================================================

   /**
    * A left aligned label panel.
    */
    public LabelPanel( String text )
    {
        super( );
        setLayout( new FlowLayout( FlowLayout.LEFT ) );
        setBorder( new EmptyBorder( 0, 0, 0, 0 ) );

        label = new JLabel( text );
        label.setFont( Desktop.font );
        label.setBorder( new EmptyBorder( 2, 0, 1, 2 ) );
        add( label );
    }

    //==========================================================
    // methods
    //==========================================================

   /**
    * Set the name of the command panel.
    */
    public void setText( String name )
    {
        label.setText( name );
    }
}
