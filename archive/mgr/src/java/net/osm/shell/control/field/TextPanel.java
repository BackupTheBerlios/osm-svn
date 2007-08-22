
package net.osm.shell.control.field;

import java.awt.Font;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.border.LineBorder;
import javax.swing.event.CaretListener;
import javax.swing.event.CaretEvent;

import net.osm.shell.MGR;
import net.osm.shell.control.field.LabelPanel;

/**
 * The <code>TestPanel</code> contains a single editable line of text
 * and validation mechanism.
 *
 * @author  Stephen McConnell
 * @version 1.0 22 AUG 2001
 */
public class TextPanel extends JPanel implements Verifiable, CaretListener
{
    //==========================================================
    // state
    //==========================================================

    private JTextField field;
    private Validator validator;
    private boolean trace = false;
    private String header;
    private String value;
    private FieldStatus status;
    private LabelPanel label;

    //==========================================================
    // constructor
    //==========================================================

   /**
    * TextPanel based on the supplied string.
    */
    public TextPanel( String header )
    {
        this( header, "", 20, null );
    }

   /**
    * TextPanel based on the supplied string and validator.
    */
    public TextPanel( String header, Validator validator  )
    {
        this( header, "", 20, validator );
    }

   /**
    * TextPanel based on the supplied string and initial value.
    */
    public TextPanel( String header, String text )
    {
        this( header, text, 20, null );
    }

   /**
    * TextPanel based on the supplied string, initial value, number of columns and validator.
    */
    public TextPanel( String header, String text, int columns, Validator validator )
    {
        super( new BorderLayout() );

        if( text == null ) 
        {
		value = "";
	  }
	  else
	  {
		value = text;
	  }

        this.validator = validator;
        setLayout( new FlowLayout( FlowLayout.LEFT ) );
        JPanel holder = new JPanel( new BorderLayout() );
        setBorder( new EmptyBorder( 0, 0, 0, 0 ) );
        label = new LabelPanel( header );
        label.setBorder( new EmptyBorder( 0, 0, 0, 0 ) );
        field = new JTextField( value, columns );
        field.setBorder( new EtchedBorder( EtchedBorder.LOWERED ));
        field.addCaretListener( this );
        holder.add( label, BorderLayout.NORTH );
        holder.add( field, BorderLayout.CENTER );
	  add( holder );

    }

    //==========================================================
    // CaretListener
    //==========================================================

    public void caretUpdate( CaretEvent event )
    {
	  if( event.getSource() == field ) fireStatusEvent();
    }

    //==========================================================
    // public methods
    //==========================================================

    public boolean verify()
    {
        if( status != null ) return status.getValue();
        return doVerify();
    }

    private boolean doVerify()
    {
	  if( validator == null ) return true;
	  boolean verification = validator.verify( field.getText() );
	  if( trace ) System.out.println( "verification: " + verification );
        return verification;
    }

    public void setLabel( String text )
    {
        label.setText( text );
    }

    public void setText( String text )
    {
        field.setText( text );
    }

    public String getText()
    {
        return field.getText();
    }

    //==========================================================
    // internals
    //==========================================================

    private void fireStatusEvent()
    {
        FieldStatus s = new FieldStatus( field, doVerify() );
 	  firePropertyChange( "status", status, s );
        status = s;
    }

}