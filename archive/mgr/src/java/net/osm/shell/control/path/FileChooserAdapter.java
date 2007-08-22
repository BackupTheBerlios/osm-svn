
package net.osm.shell.control.path;

import java.io.File;
import java.awt.Frame;
import java.awt.Component;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.JFileChooser;
import javax.swing.SwingUtilities;
import javax.swing.JDialog;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;


/**
 * The <code>FileChooserAction</code> class.
 *
 * @author  Stephen McConnell
 * @version 1.0 21 AUG 2001
 */
class FileChooserAdapter implements PropertyChangeListener
{

    //==========================================================
    // state
    //==========================================================

    private PathPanel panel;
    private FileChooserAction action;
    private boolean trace = false;

    //==========================================================
    // constructor
    //==========================================================

   /**
    * Default constructor.
    * 
    * @param name the name of the action
    * @param component logically containing component
    * @param file default file
    */
    public FileChooserAdapter( PathPanel panel, FileChooserAction action )
    {
        this.panel = panel;
        this.action = action;
        action.addPropertyChangeListener( this );
    }

    //==========================================================
    // ActionListener
    //==========================================================

   /**
    * Updates the filename field in the path panel based on an action 
    * performed.
    */
    public void propertyChange( PropertyChangeEvent event )
    {
	  if( event.getNewValue() instanceof File )
        {
            if( trace ) System.out.println("File chooser adapter received event.");
            panel.setFile( (File) event.getNewValue() );
        }
    }
}
