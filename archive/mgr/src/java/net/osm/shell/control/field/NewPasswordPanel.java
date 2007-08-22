
package net.osm.shell.control.field;

import java.math.BigInteger;
import java.awt.Font;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JPasswordField;
import javax.swing.event.CaretListener;
import javax.swing.event.CaretEvent;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;

import net.osm.shell.MGR;
import net.osm.shell.control.field.LabelPanel;

/**
 * The <code>NewPasswordPanel</code> is a form containing a 
 * password entry and password validation field.
 *
 * @author  Stephen McConnell
 * @version 1.0 22 AUG 2001
 */
public class NewPasswordPanel extends JPanel implements CaretListener
{
    //==========================================================
    // state
    //==========================================================

    private JPasswordField primary;
    private JPasswordField secondary;
    private boolean trace = false;
    private char[] password;
    private BigInteger magic;
    private int count;

    //==========================================================
    // constructor
    //==========================================================

   /**
    * Creates an new NewPasswordPanel based on the supplied minimum
    * character policy.
    */
    public NewPasswordPanel( int columns, int count, BigInteger magic )
    {
        super( new BorderLayout() );
        this.count = count;
        this.magic = magic;

        LabelPanel primaryLabel = new LabelPanel( 
		"Password: (minimum of " + count + " characters)" );
        primaryLabel.setBorder( new EmptyBorder( 0,0,0,0 ) );
        primary = new JPasswordField( "", columns );
        primary.setBorder( new EtchedBorder( EtchedBorder.LOWERED ));

        LabelPanel secondaryLabel = new LabelPanel("Verification:");
        secondaryLabel.setBorder( new EmptyBorder( 0,0,0,0 ) );
        secondary = new JPasswordField( "", columns );
        secondary.setBorder( new EtchedBorder( EtchedBorder.LOWERED ));

	  Box box = new Box( BoxLayout.Y_AXIS );
	  box.add( primaryLabel );
	  box.add( primary );
	  box.add( secondaryLabel );
	  box.add( secondary );
        box.add( Box.createVerticalGlue() );

	  JPanel holder = new JPanel( new FlowLayout( FlowLayout.LEFT ) );
	  holder.add( box );
	  add( holder, BorderLayout.NORTH );

        primary.addCaretListener( this );
        secondary.addCaretListener( this );

    }

    //==========================================================
    // CaretListener
    //==========================================================

    public void caretUpdate( CaretEvent event )
    {
        JPasswordField source = (JPasswordField) event.getSource();
	  if(( source == primary ) || (source == secondary ))
	  {
            boolean result = verify();
	      if( trace ) if( result ) System.out.println("\tCARET: " + result );
	      if( result )
	      {
                firePropertyChange( "password", null, primary.getPassword() );
	      }
            else
	      {
                firePropertyChange( "password", new char[0], null );
	      }
        }
    }

    //==========================================================
    // internals
    //==========================================================

    private boolean verify()
    {
	  if( primary.getPassword().length < count ) return false;
	  if( secondary.getPassword().length < count ) return false;
	  String p1 = new String( primary.getPassword() );
	  String p2 = new String( secondary.getPassword() );
        boolean result = p1.equals( p2 );
        p1 = null;
	  p2 = null;
	  return result;
    }
}