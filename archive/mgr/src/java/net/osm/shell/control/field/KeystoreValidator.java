
package net.osm.shell.control.field;

import java.io.File;
import java.io.FileInputStream;
import java.awt.Frame;
import java.awt.Component;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.JDialog;
import javax.swing.JButton;
import javax.swing.SwingUtilities;
import javax.swing.JPasswordField;
import javax.swing.event.CaretListener;
import javax.swing.event.CaretEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;
import java.security.KeyStore;

import net.osm.util.ExceptionHelper;


/**
 * The <code>KeystoreValidator</code> class.
 *
 * @author  Stephen McConnell
 * @version 1.0 22 AUG 2001
 */
public final class KeystoreValidator extends AbstractAction implements CaretListener
{

    //==========================================================
    // state
    //==========================================================

    private char[] characters;
    private File file;
    private JPasswordField field;

    private KeyStore keystore;
    private FileInputStream stream;
    private static final boolean trace = false;

    private JDialog dialog;
    private JButton defaultButton;
    private JButton button;

    //==========================================================
    // constructor
    //==========================================================

   /**
    * Creates a password validator based on a supplied keystore.
    * The constructor verifies that the supplied file is a valid
    * keystore (either a keystore or a non-existant file are valid).
    * The instance of the validator can then validate the password
    * against a supplied character array.
    * 
    * @param file the keystore
    */
    public KeystoreValidator( 
      String name, final JPasswordField field, final File file, JButton button ) 
    throws FieldException
    {
	  super( name );
        if( file == null ) throw new FieldException(
		"KeystoreValidator does not support a null file arguments.");
        if( !file.exists() ) throw new FieldException(
		"KeystoreValidator cannot validate a non-existing keystore.");
        if( field == null ) throw new FieldException(
		"KeystoreValidator does not support a null field arguments.");

        this.file = file;
        this.field = field;
        this.button = button;
	  try
	  {
            setEnabled( false );
            field.addCaretListener( this );
		putValue("validate", null );
	  }
	  catch( Exception e )
	  {
	      throw new FieldException("Unable to load keystore.");
	  }
    }

    //==========================================================
    // CaretListener
    //==========================================================

    public void caretUpdate( CaretEvent event )
    {
        JPasswordField source = (JPasswordField) event.getSource();
	  if( source == field )
	  {
		if( field.getPassword().length > 0 )
		{
		    setEnabled( true );
		    if( defaultButton == null )
		    {
		        JDialog root = getDialog( field );
		        defaultButton = root.getRootPane().getDefaultButton();
		        root.getRootPane().setDefaultButton( button );
		    }
		}
		else
		{
		    setEnabled( false );
		    JDialog root = getDialog( field );
		    if( defaultButton != null ) 
			root.getRootPane().setDefaultButton( defaultButton );
		}
        }
    }

    private JDialog getDialog( Component component )
    {
       if( dialog == null ) dialog = component instanceof JDialog ? (JDialog) component
              : (JDialog)SwingUtilities.getAncestorOfClass(JDialog.class, component);
       return dialog;
    }

    //==========================================================
    // ActionListener
    //==========================================================

   /**
    * Validates the content of the password field against the 
    * keystore.
    */
    public void actionPerformed( ActionEvent event )
    {
	  boolean result = false;
	  try
	  {
	      if( verify( field.getPassword() ))
		{
	          putValue( "validate", characters );
		    JDialog root = getDialog( field );
		    if( defaultButton != null )
		    {
			  root.getRootPane().setDefaultButton( defaultButton );
			  defaultButton.doClick();
		    }
	      }
	      else
	      {
	          putValue( "validate", null );
	      }
	  }
	  catch( Exception e )
	  {
	      result = false;
		ExceptionHelper.printException("Password validation error", e );
	      putValue( "validate", null );
	  }
    }

    //==========================================================
    // methods
    //==========================================================

   /**
    * Tests if the supplied character array is equivilent to the 
    * password.
    */

    public final boolean verify( final char[] characters ) throws Exception
    {

        if( trace ) System.out.println("verify" );
        boolean result = false;
        try
        {
            stream = new FileInputStream( file );
            keystore = KeyStore.getInstance(KeyStore.getDefaultType());
            if( trace )
	      {
	          System.out.println("keystore: " + keystore );
	          System.out.println("stream: " + stream );
	          System.out.println("characters: " + new String( characters ) );
	      }
		keystore.load( stream, characters );
		if( trace ) System.out.println("result: ok");
		this.characters = characters;
            result = true;
        }
        catch (Exception e) 
        {
            if( trace ) ExceptionHelper.printException("VERIFY", e );
        }
        finally
	  {
		if( stream != null ) try
		{
		    stream.close();
	      }
	      catch( Exception anything )
		{
		}
		keystore = null;
		if( trace ) System.out.println("result: " + result );
		return result;
	  }
    }
}
