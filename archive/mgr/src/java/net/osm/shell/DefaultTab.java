
package net.osm.shell;

import java.awt.Font;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;

import net.osm.shell.MGR;
import net.osm.shell.control.util.ControlUtility;
import net.osm.entity.*;

/**
 * The <code>DefaultTab</code> class manages is implementation of a default
 * tab used within the Properties dialog.
 *
 * @author  Stephen McConnell
 * @version 1.0 01 SEP 2001
 */
class DefaultTab extends JPanel
{

    //==========================================================
    // constructor
    //==========================================================

   /**
    * Default constructor.
    */
    public DefaultTab( String name ) 
    {
        super( );
        setLayout( new BorderLayout() );

        //
        // Create the header
        //

        JLabel label = new JLabel( name );
        label.setFont( MGR.font );
        label.setBorder( new EmptyBorder( 10, 10, 10, 10 ));
        JPanel labelHolder = new JPanel( new FlowLayout( FlowLayout.LEFT ) );
        labelHolder.add( label );
        add( labelHolder, BorderLayout.NORTH );
    }

}
