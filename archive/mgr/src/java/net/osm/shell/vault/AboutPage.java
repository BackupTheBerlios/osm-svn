
package net.osm.shell.vault;

import java.awt.Component;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.JLabel;

import net.osm.shell.control.wizard.Wizard;
import net.osm.shell.control.wizard.Page;

/**
 * The <code>AboutPage</code> is helper class related to 
 * the Wizard class.  It supplies support for the monitoring 
 * of a cancel event and notification to the wizard.
 *
 * @author  Stephen McConnell
 * @version 1.0 21 AUG 2001
 */
public class AboutPage extends Page
{

    //==========================================================
    // state
    //==========================================================

    private boolean trace = true;
    private String aboutMessage = 
              "The ConfigurationWizard assists in the establishment" +
              " of your initial digital identity.";
    private Wizard wizard;

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
    public AboutPage( Wizard wizard, String name )
    {
        super( name );
	  this.wizard = wizard;
    }

    //==========================================================
    // ActionListener
    //==========================================================

   /**
    * Initialize the about page.
    */
    public void actionPerformed( ActionEvent event )
    {
        wizard.setMessage( aboutMessage );
        getPanel().setCommandName( "Configuration Wizard" );
    }
}
