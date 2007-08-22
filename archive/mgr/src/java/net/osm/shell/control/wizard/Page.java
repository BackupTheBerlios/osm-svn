
package net.osm.shell.control.wizard;

import java.awt.Component;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;
import javax.swing.JDialog;

/**
 * The <code>DefaultCancelAction</code> is helper class related to 
 * the Wizard class.  It supplies support for the monitoring 
 * of a cancel event and notification to the wizard.
 *
 * @author  Stephen McConnell
 * @version 1.0 21 AUG 2001
 */
public abstract class Page extends AbstractAction
{
    //==========================================================
    // static
    //==========================================================

    public static final int CANCEL = Wizard.CANCEL;
    public static final int PREV = Wizard.PREV;
    public static final int NEXT = Wizard.NEXT;
    public static final int FINISH = Wizard.FINISH;

    //==========================================================
    // state
    //==========================================================

    private CommandPanel component = new CommandPanel();
    private String name;
    private JDialog dialog;

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
    public Page( String name )
    {
        super( name );
	  this.name = name;
    }

    //==========================================================
    // methods
    //==========================================================

    public String getName()
    {
        return name;
    }

    public CommandPanel getPanel()
    {
        return component;
    }

    public JDialog getDialog( Component component )
    {
       if( dialog == null ) dialog = component instanceof JDialog ? (JDialog) component
              : (JDialog)SwingUtilities.getAncestorOfClass(JDialog.class, component);
       return dialog;
    }
}
