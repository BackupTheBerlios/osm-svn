
package net.osm.shell.control.wizard;

import java.awt.Font;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Component;
import java.awt.BorderLayout;
import java.awt.FlowLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.EtchedBorder;

/**
 * The <code>CommandPanel</code> class.
 *
 * @author  Stephen McConnell
 * @version 1.0 22 AUG 2001
 */
public class CommandPanel extends JPanel
{
    //==========================================================
    // state
    //==========================================================

    private JLabel label;
    private JPanel bodyHolder;
    private Component body;


    //==========================================================
    // constructor
    //==========================================================

   /**
    * Default constructor.
    */
    public CommandPanel(  )
    {
        this( "Untitled", null );
    }

   /**
    * Creation of a new CommandPanel using the supplied 
    * title and component.
    * 
    * @param title to display at the tope of the command panel
    * @param component to be placed in the body of the command panel
    */
    public CommandPanel( String title, Component component )
    {
        super( new BorderLayout() );
        
        //
        // Create the command panel title
        //

        label = new JLabel( );
        label.setFont( new Font("Dialog", 0, 18 ));
        label.setBorder( new EmptyBorder( 70, 10, 20, 10 ));
        JPanel labelHolder = new JPanel( new FlowLayout( FlowLayout.LEFT ) );
        labelHolder.add( label );

        //
        // Create a line seperator
        //

        JPanel line = new JPanel();
        line.setBorder( new EtchedBorder( EtchedBorder.LOWERED ) );
        line.setPreferredSize( new Dimension( 380, 2 ));
	  JPanel lineHolder = new JPanel( );
        lineHolder.add( line );
        lineHolder.setBorder( new EmptyBorder( 3, 10, 3, 10 ));

        //
        // Package the label and the line into a header
        //

        JPanel header = new JPanel( new BorderLayout() );
        header.add( labelHolder, BorderLayout.NORTH );
        header.add( lineHolder, BorderLayout.SOUTH );

        //
        // Creeate the command panel body holder
        //

        bodyHolder = new JPanel( new BorderLayout() );
        bodyHolder.setBorder( new EmptyBorder( 30, 20, 20, 20 ));

        //
        // Package the header and the boddy holder into the contents of this panel
        //

        add( header, BorderLayout.NORTH );
        add( bodyHolder, BorderLayout.CENTER );

        //
        // Initialize
        //

        setCommandName( title );
        setCommandBody( component );

    }

    //==========================================================
    // methods
    //==========================================================

   /**
    * Set the name of the command panel.
    */
    public void setCommandName( String name )
    {
	  if( name.length() == 0 ) 
	  {
	      label.setText( "" );
        }
	  else
	  {
            label.setText( name );
	  }
    }

   /**
    * Set the body.
    */
    public void setCommandBody( Component component )
    {
        if( body != null ) bodyHolder.remove( body );
        body = null;
        if( component != null )
        {
	      body = component;
            bodyHolder.add( body, BorderLayout.NORTH );
        }
        bodyHolder.validate();
        bodyHolder.repaint();
    }
}
