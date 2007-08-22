
package net.osm.shell.control.field;

import java.awt.Font;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JButton;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JPasswordField;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;

import net.osm.shell.MGR;
import net.osm.shell.control.field.LabelPanel;

/**
 * The <code>PasswordPanel</code> contains a gidden password entry field
 * and password validation mechanism.
 *
 * @author  Stephen McConnell
 * @version 1.0 22 AUG 2001
 */
public class PasswordPanel extends JPanel implements PropertyChangeListener
{
    //==========================================================
    // state
    //==========================================================

    private JPasswordField field;
    private KeystoreValidator validator;
    private boolean trace = false;
    private String password = "";
    private Object lastPasswordValue;

    //==========================================================
    // constructor
    //==========================================================

   /**
    * PasswordPanel based on the supplied string and alignment.
    */
    public PasswordPanel( char[] text, int columns, File file ) throws FieldException
    {
        super( new BorderLayout() );
        if( text != null ) password = new String ( text );

	  try
	  {
            Box holder = new Box( BoxLayout.Y_AXIS  );

            LabelPanel label = new LabelPanel("Password:");
            label.setBorder( new EmptyBorder( 0,3,0,0 ) );
            field = new JPasswordField( password, columns );
            field.setBorder( new EtchedBorder( EtchedBorder.LOWERED ));
            JPanel fieldHolder = new JPanel( new FlowLayout( FlowLayout.LEFT ));
		fieldHolder.setBorder( new EmptyBorder( 0,0,0,0 ) );
		fieldHolder.add( field );

            JButton button = new JButton( );
		validator = new KeystoreValidator( "Validate", field, file, button ); 
            button.setAction( validator );
		validator.addPropertyChangeListener( this );
            JPanel buttonHolder = new JPanel( new FlowLayout( FlowLayout.LEFT ));
		buttonHolder.setBorder( new EmptyBorder( 2,0,3,10 ) );
		buttonHolder.add( button );

            holder.add( label );
            holder.add( fieldHolder );
            holder.add( buttonHolder );
            holder.add( Box.createVerticalGlue() );
	      add( holder, BorderLayout.NORTH );
        }
        catch( Exception e )
	  {
		throw new FieldException("Unable to create new PasswordPanel.", e );
	  }
    }

    //==========================================================
    // PropertyChangeListener
    //==========================================================

   /**
    * Listens to property changes from pages.
    */
    public void propertyChange( PropertyChangeEvent event )
    {
        if( event.getSource() == validator )
	  {
		if( event.getPropertyName().equals("validate"))
		{
                if( trace ) System.out.println("\tVALIDATE/event" );
		    firePropertyChange( "password", lastPasswordValue, event.getNewValue() );
		    lastPasswordValue = event.getNewValue();
            }
        }
    }
}