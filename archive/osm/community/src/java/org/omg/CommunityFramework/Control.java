// Thu Nov 23 07:22:00 CET 2000

package org.omg.CommunityFramework;

import java.io.Serializable;
import java.awt.Component;
import java.awt.Font;
import java.awt.Color;
import java.awt.Component;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JScrollPane;
import javax.swing.border.LineBorder;
import javax.swing.border.EmptyBorder;

import org.apache.avalon.framework.configuration.Configuration;

import org.omg.CORBA.TypeCode;
import org.omg.CORBA.portable.StreamableValue;
import org.omg.CORBA.portable.InputStream;
import org.omg.CORBA.portable.OutputStream;
import org.omg.CORBA.portable.ValueFactory;

/**
 * Control is an identifiable valuetype used in definition of 
 * valuetypes defining complex models.  Control contains a human 
 * readable label and descriptive note. Control is used as a 
 * utility state container by several valuetypes defined within 
 * the Community and Collaboration frameworks. 
 */

public class Control
implements StreamableValue, ValueFactory
{
    
    //==========================================================
    // static
    //==========================================================
    
   /**
    * Return the truncatable ids
    */
    static final String[] _ids_list = { 
        "IDL:omg.org/CommunityFramework/Control:1.0"
    };

    //==========================================================
    // state
    //==========================================================

    /**
    * Name of the control.
    */
    public String label;

    /**
    * Description of the control.
    */
    public String note;

    // private members
    private JLabel labelView;
    private JTextArea noteView;
    private JPanel controlView;
    
    //==========================================================
    // constructors
    //==========================================================

    /** 
    * Empty constructure used during valuetype internalization.
    */
    public Control( ){}

   /**
    * Creation of a new Controls of a control based on a supplied Configuration.
    */
    public Control( Configuration conf )
    {
	  if( conf == null ) throw new RuntimeException("Null configuration.");
        this.label = conf.getAttribute("label","untitled");
        this.note = conf.getAttribute("note","");
    }

   /**
    * Utility constructor.
    */
    public Control( String label, String note ) {
        this.label = label;
	  this.note = note;
    }

    //==========================================================
    // Control
    //==========================================================

   /**
    * Return the value TypeCode
    */    
    public org.omg.CORBA.TypeCode _type()
    {
        return ControlHelper.type();
    }
    
   /**
    * Unmarshal the value into an InputStream
    */
    public void _read(InputStream _is)
    {
        label = LabelHelper.read(_is);
        note = NoteHelper.read(_is);
    }
    
   /**
    * Marshal the value into an OutputStream
    */
    public void _write(OutputStream _os)
    {
        LabelHelper.write(_os, label);
        NoteHelper.write(_os, note);
    }
            
   /**
    * Return the truncatable ids
    */
    public String[] _truncatable_ids() { return _ids_list; }

   /**
    * Control factory.
    */
    public Serializable read_value(org.omg.CORBA_2_3.portable.InputStream is) {
        return is.read_value( new Control() );
    }
    
    //==========================================================
    // Extra - depricated
    //==========================================================

   /**
    * Returns the label associated with this control.
    * 
    * @return String label
    */

    public String getLabel(){
	  if( label == null ) return "";
	  return label;
    }


   /*
    * Returns the note associated with this control.
    * 
    * @return String note
    */

    public String getNote(){
	  if( note == null ) return "";
	  return note;
    }

   /**
    * Get the graphic Component that represents this item.
    * This implementation returns a JLabel containing the 
    * label text with a tooltip showing the control note.
    * 
    * @return JPanel a panel used to present the control in a 
    * human user interface.
    */
/*    public JPanel getControlView( ){
	  if (controlView == null ) controlView = new ControlView( this );
	  return controlView;
    }
*/
    /** 
    * Returns the label in the form of a swing JLabel.
    *
    * @return JLabel used to present the label in a 
    * human user interface.
    */
/*
    public JLabel getLabelView( ){
	  if (labelView == null ) {
		labelView = new JLabel( getLabel() );
            labelView.setForeground( Config.forground );
            labelView.setFont( Config.bodyfont );
	  }
	  return labelView;
    }
*/
    /** 
    * Returns the note in the form of a swing JTextArea.
    *
    * @return JTextArea used to present the note in a 
    * human user interface.
    */
/*
    public JTextArea getNoteView( ){
	  if (noteView == null ) {
		noteView = new JTextArea( getNote());
            noteView.setEditable(false);
            noteView.setForeground( Config.forground );
            noteView.setFont( Config.font );
        	noteView.setLineWrap(true);
        	noteView.setWrapStyleWord(true);
	  }
	  return noteView;
    }

    private class ControlView extends JPanel {

        private ControlView( Control c ) {

		super( );
        	GridBagLayout layout = new GridBagLayout();
	  	setLayout( layout );

        	// label label constraint
        	GridBagConstraints labelLabelConstraint = new GridBagConstraints();
	  	labelLabelConstraint.anchor = GridBagConstraints.NORTHEAST;
	  	labelLabelConstraint.fill = GridBagConstraints.NONE;
	  	labelLabelConstraint.ipadx = 10;
	  	labelLabelConstraint.ipady = 10;

	  	// label value constraint
	  	GridBagConstraints labelValueConstraint = new GridBagConstraints();
	  	labelValueConstraint.gridwidth = GridBagConstraints.REMAINDER;
	  	labelValueConstraint.anchor = GridBagConstraints.NORTHWEST;
	  	labelValueConstraint.fill = GridBagConstraints.HORIZONTAL;
	  	labelValueConstraint.ipadx = 10;
	  	labelValueConstraint.ipady = 10;

        	// note label constraint
        	GridBagConstraints noteLabelConstraint = new GridBagConstraints();
	  	noteLabelConstraint.anchor = GridBagConstraints.NORTHEAST;
	  	noteLabelConstraint.fill = GridBagConstraints.NONE;
	  	noteLabelConstraint.ipadx = 10;
	  	noteLabelConstraint.ipady = 10;

	  	// note value constraint
	  	GridBagConstraints noteValueConstraint = new GridBagConstraints();
	  	noteValueConstraint.gridwidth = GridBagConstraints.REMAINDER;
	  	noteValueConstraint.gridheight = GridBagConstraints.REMAINDER;
	  	noteValueConstraint.anchor = GridBagConstraints.NORTHWEST;
	  	noteValueConstraint.fill = GridBagConstraints.BOTH;
	  	noteValueConstraint.ipadx = 10;
	  	noteValueConstraint.ipady = 10;
	  	noteValueConstraint.weightx = 1;
	  	noteValueConstraint.weighty = 1;

		// label label
	  	JLabel labelText = new JLabel("Label:");
            labelText.setFont( Config.font );
	      //labelText.setBorder( new LineBorder( Color.black ));
	  	layout.setConstraints( labelText, labelLabelConstraint );
	  	add( labelText );

		// label value
	  	JLabel labelValue = c.getLabelView();
            labelValue.setFont( Config.font );
	  	layout.setConstraints( labelValue, labelValueConstraint );
	  	add( labelValue );

		// note label
	  	Component noteText = new JLabel("Note:");
            noteText.setFont( Config.font );
	  	layout.setConstraints( noteText, noteLabelConstraint );
	  	add( noteText );

		// note value
	  	Component noteValue = c.getNoteView();
		noteValue.setBackground( noteText.getBackground());
            JScrollPane jsp = new JScrollPane( noteValue );
            jsp.setViewportBorder( new EmptyBorder( 5,10,10,10));
	  	layout.setConstraints( jsp, noteValueConstraint );
	 	add( jsp );
	  }
    }
*/
}
