
package net.osm.shell.control.wizard;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowListener;
import java.awt.event.WindowEvent;


/**
 * The <code>WizardMonitor</code>
 *
 * @author  Stephen McConnell
 * @version 1.0 21 JUN 2001
 */
public class WizardMonitor extends WindowAdapter
{

    //==========================================================
    // state
    //==========================================================

   /**
    * The root window.
    */
    private Wizard wizard;

    //==========================================================
    // constructor
    //==========================================================

   /**
    * Default constructor.
    */
    WizardMonitor( Wizard wizard )
    {
        this.wizard = wizard;
    }

    //==========================================================
    // WindowListener
    //==========================================================

   /**
    * Listen for a window close event in the wizard and notify
    * the wizard accordinly.  
    */
    public void windowClosing( WindowEvent event )
    {
        wizard.windowClosing( event );
    }
}
