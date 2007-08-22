
package net.osm.shell.control.wizard;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;

/**
 * The <code>DefaultCancelAction</code> is helper class related to 
 * the Wizard class.  It supplies support for the monitoring 
 * of a cancel event and notification to the wizard.
 *
 * @author  Stephen McConnell
 * @version 1.0 21 AUG 2001
 */
public class DefaultAction extends AbstractAction
{

    Wizard wizard;
    private int id;

    //==========================================================
    // constructor
    //==========================================================

   /**
    * Default constructor.
    * 
    * @param name the name of the action
    * @param coordinator that the action is attached to
    * @param enabled the inital enabled state
    */
    public DefaultAction( String name, int id, Wizard wizard )
    {
        super( name );
        setEnabled( false );
        this.wizard = wizard;
        this.id = id;
    }

    //==========================================================
    // ActionListener
    //==========================================================

   /**
    * Notify the wizard that a cancel event has occured.
    */
    public void actionPerformed( ActionEvent event )
    {
        wizard.notifyActionPerformed( id );
    }

}
