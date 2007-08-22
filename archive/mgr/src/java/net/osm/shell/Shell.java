/* 
 * Shell.java
 */

package net.osm.shell;

import javax.security.auth.Subject;
import javax.swing.Action;
import javax.swing.JProgressBar;
import java.awt.Font;
import java.awt.Color;

import org.apache.avalon.framework.component.Component;

import net.osm.shell.Entity;

/**
 * The <code>Shell</code> interface specifies the set of operations supported by an 
 * implementation of a application shell.
 *
 * @author Stephen McConnell
 */

public interface Shell extends Component
{

   /**
    * Returns the default font for presentation within desktop artifacts.
    * @return Font the default font
    */
    public Font getDefaultFont();

   /**
    * Returns the default background colour.
    * @return Color the default background
    */
    public Color getBackground();

   /**
    * Returns the default background colour.
    * @return Color the default foreground
    */
    public Color getForeground();

   /**
    * Installs an extension into the shell.
    * @param entity the entity to install
    */
    public void install( Entity entity );

   /**
    * Clears the message in the status panel.
    */
    public void setMessage( );

   /**
    * Set the message in the status panel to the supplied value.
    * @param message the message to display
    */
    public void setMessage( String message );

   /**
    * Declare a progressive activity to the shell.
    */
    public void execute( Activity activity );

   /**
    * Declare an error to the shell.
    */
    public void error( String message, Throwable e );

}
