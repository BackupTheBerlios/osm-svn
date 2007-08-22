
package net.osm.shell.control.path;

import java.io.File;
import java.awt.Frame;
import java.awt.Component;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.JFileChooser;
import javax.swing.SwingUtilities;
import javax.swing.JDialog;

import net.osm.util.ExceptionHelper;

/**
 * The <code>FileChooserAction</code> class.
 *
 * @author  Stephen McConnell
 * @version 1.0 21 AUG 2001
 */
class FileChooserAction extends AbstractAction
{

    //==========================================================
    // state
    //==========================================================

    private JFileChooser chooser = new JFileChooser();
    private Frame frame;
    private PathPanel panel;
    private File file;
    private boolean modified = false;
    private File newFile;
    private boolean trace = false;

    //==========================================================
    // constructor
    //==========================================================

   /**
    * Default constructor.
    * 
    * @param name the name of the action
    * @param panel logically triggering the action
    * @param file default file
    */
    public FileChooserAction( String name, PathPanel panel )
    {
        super( name );
        this.panel = panel;


    }

    //==========================================================
    // ActionListener
    //==========================================================

   /**
    * 
    */
    public void actionPerformed( ActionEvent event )
    {
	  try
	  {
            //
	      // set the current directory to supplied file
            //

		File file = panel.getFile();
	      chooser.setCurrentDirectory( file );
	      if( file != null ) if( file.exists() ) {
	        chooser.setSelectedFile( file );
	      }
            int result = chooser.showOpenDialog( getFrame( panel ) );
            if( result == JFileChooser.APPROVE_OPTION ) 
              panel.setFile( chooser.getSelectedFile() );
        }
        catch( Throwable e )
        {
            String message = "Unexpected exception in FileChooserAction constructor";
	      ExceptionHelper.printException( message, e );
        }
    }

    private Frame getFrame( Component component )
    {
       if( frame == null ) frame = component instanceof Frame ? (Frame) component
              : (Frame)SwingUtilities.getAncestorOfClass(Frame.class, component);
       return frame;
    }

}
