
package net.osm.shell.control.path;

import java.io.File;
import java.awt.Frame;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.AbstractAction;
import javax.swing.SwingUtilities;
import javax.swing.JOptionPane;

import net.osm.util.ExceptionHelper;


/**
 * The <code>FileDefaultAction</code> class.
 *
 * @author  Stephen McConnell
 * @version 1.0 21 AUG 2001
 */
class FileDefaultAction extends AbstractAction implements PropertyChangeListener
{

    //==========================================================
    // state
    //==========================================================

    PathPanel path;
    File file;
    boolean trace = false;

    //==========================================================
    // constructor
    //==========================================================

   /**
    * Default constructor.
    * 
    * @param name the name of the action
    * @param component logically triggering the action
    * @param file default file
    */
    public FileDefaultAction( String name, PathPanel path, File file )
    {
        super( name );
        path.addPropertyChangeListener( this );
        this.path = path;
        this.file = file;
        setStatus( file );
    }

    //==========================================================
    // ActionListener
    //==========================================================

   /**
    * Creates the file.
    */
    public void actionPerformed( ActionEvent event )
    {
	  path.setToDefault( );
    }

   /**
    * Listens to file property changes - if the file held by the 
    * path panel changes to something other than the default, then 
    * we need to enable the default action.
    */
    public void propertyChange( PropertyChangeEvent event )
    {

        if( trace ) System.out.println(
		"create action is receiving property change event: " + 
		event.getPropertyName() );

	  if( event.getPropertyName().equals("file") )
        {
		if( event.getNewValue() instanceof File ) 
		{
		    //
		    // if the file in the event is not the file
		    // default file, then enable the default action
		    //

                File newFile = (File)event.getNewValue();
		    setStatus( newFile );
		}
	  }
    }

    private void setStatus( File someFile )
    {
        setEnabled( someFile != file );
    }
}
