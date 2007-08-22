
package net.osm.shell.control.path;

import java.io.File;
import java.awt.Font;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.BorderLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JButton;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.border.EmptyBorder;

import net.osm.shell.MGR;
import net.osm.shell.control.field.LabelPanel;

/**
 * The <code>PathPanel</code> contains a label, filename and browse button.
 * The object represent a selected file.  A change of the selection triggers 
 * property event named "<code>file</code>".
 *
 * @author  Stephen McConnell
 * @version 1.0 22 AUG 2001
 */
public class PathPanel extends JPanel
{
    //==========================================================
    // state
    //==========================================================

    private File file;
    private File defaultFile;
    private LabelPanel label;
    private LabelPanel path;
    private JButton button;
    private JButton create;
    private boolean exists;
    private boolean trace = false;

    //==========================================================
    // constructor
    //==========================================================

   /**
    * PathPanel constructor based on the supplied string 
    * and alignment.
    */
    public PathPanel( String text, File defaultFile ) throws PathException
    {
        super( new BorderLayout() );

        try
	  {
            if( defaultFile == null ) 
	        throw new PathException(
		  "Null default file supplied to the path constructor.");
            this.defaultFile = defaultFile;

            label = new LabelPanel( text );
            path = new LabelPanel( defaultFile.getAbsolutePath() );
            Box holder = new Box( BoxLayout.Y_AXIS  );
            holder.add( label );
            holder.add( path );

            //
            // Button holder.
            //

            button = new JButton(  );
            button.setAction( new FileChooserAction( "Browse", this ) );

            JButton defaultButton = new JButton();
	      FileDefaultAction defaultAction = 
			new FileDefaultAction( "Default", this, defaultFile );
        	defaultButton.setAction( defaultAction );

        	JPanel buttonHolder = new JPanel( new FlowLayout( FlowLayout.LEFT ) );
        	buttonHolder.add( button );
        	buttonHolder.add( defaultButton );
        	holder.add( buttonHolder );

        	add( holder, BorderLayout.NORTH );
        	add( Box.createVerticalGlue(), BorderLayout.CENTER );
        }
	  catch( Exception e )
	  {
	      throw new PathException("Unable to create the path panel.", e );
        }
    }

   /**
    * Changes the file presented under this panel and triggers a <code>file</code>
    * property change event.
    */
    public void setFile( File file )
    {
	  if( trace ) System.out.println("panel is firing file property change.");
	  if( trace ) System.out.println("\t\tOLD: " + this.file );
	  if( trace ) System.out.println("\t\tNEW: " + file );
	  if( trace ) System.out.println("\t\tEXISTS: " + file.exists() );
	  if( trace ) System.out.println("\t\tEQUAL: " + file.equals(this.file));

        if( this.file != file )
	  {
	      String filename = file.getAbsolutePath();
            path.setText( filename );
            File old = this.file;
            this.file = file;
            firePropertyChange( "file", old, file );
        }
    }

   /**
    * Return the current file.
    */
    public File getFile()
    {
	  if( file == null ) return defaultFile;
        return file;
    }

    protected void setToDefault( )
    {
        setFile( defaultFile );
    }
}
