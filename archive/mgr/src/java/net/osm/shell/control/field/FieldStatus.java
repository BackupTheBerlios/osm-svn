
package net.osm.shell.control.field;

import javax.swing.JTextField;


/**
 * The <code>PageEvent</code> class.
 *
 * @author  Stephen McConnell
 * @version 1.0 21 AUG 2001
 */
public class FieldStatus
{

    //==========================================================
    // state
    //==========================================================

    private JTextField field;
    private boolean value;

    //==========================================================
    // constructor
    //==========================================================

   /**
    * Creates a new FieldStatus instance.
    * 
    * @param field text field
    * @param value boolean status of the validity of the field
    */
    public FieldStatus( JTextField field, boolean value )
    {
        this.field = field;
	  this.value = value;
    }

    //==========================================================
    // methods
    //==========================================================

    public boolean getValue()
    {
        return value;
    }

    public String getText()
    {
        return field.getText();
    }
}
