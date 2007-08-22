
package net.osm.shell.vault;

import java.io.File;
import java.io.FileInputStream;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.AbstractAction;
import javax.swing.JLabel;

import java.security.KeyStore;

import net.osm.shell.control.wizard.Wizard;
import net.osm.shell.control.wizard.Page;
import net.osm.shell.control.path.PathPanel;

/**
 * The <code>AboutPage</code> is helper class related to 
 * the Wizard class.  It supplies support for the monitoring 
 * of a cancel event and notification to the wizard.
 *
 * @author  Stephen McConnell
 * @version 1.0 21 AUG 2001
 */
public class KeystoreSelectionPage extends Page implements PropertyChangeListener
{

    //==========================================================
    // state
    //==========================================================

    private boolean trace = false;
    private String selectionMessage = 
           "Digital identities (key entries and trusted certificates) are " +
	     "maintained within a password protected keystore.  This page lets " +
	     "you select an existing keystore or request creation of a new keystore.";
    private PathPanel keystorePathPanel;
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
    public KeystoreSelectionPage( Wizard wizard, String name, File file ) throws VaultException
    {
        super( name );
        try
        {
		this.wizard = wizard;
            putValue( "keystore", null );
	      keystorePathPanel = new PathPanel( "Keystore path", file );
            keystorePathPanel.addPropertyChangeListener( this );
        }
        catch( Exception e )
        {
            throw new VaultException("Unable to create a keystore selection page.", e );
        }
    }

    public void setFile( File file )
    {
        keystorePathPanel.setFile( file );
    }

    //==========================================================
    // ActionListener
    //==========================================================

   /**
    * Notify the wizard that a cancel event has occured.
    */
    public void actionPerformed( ActionEvent event )
    {
        wizard.setMessage( selectionMessage );
        getPanel().setCommandName( "Keystore" );
        getPanel().setCommandBody( keystorePathPanel );
    }

    //==========================================================
    // PropertyChangeListener
    //==========================================================

   /**
    * Listens to file property changes - if the file held by the 
    * path panel is in fact a keystore, then set the keystore
    * property to true.
    */
    public void propertyChange( PropertyChangeEvent event )
    {
        String name = event.getPropertyName();
	  if( name.equals("file") )
        {
	      if( trace ) System.out.println(
		  "KeystoreSelectionPage/incomming event: " + name );

		if( event.getNewValue() instanceof File ) 
		{
		    //
		    // if the file in the event is not the file
		    // default file, then enable the default action
		    //

                File newFile = (File)event.getNewValue();
		    if( isaKeystore( newFile ) )
		    {
		        putValue( "keystore", event.getNewValue() );
		    }
		    else
		    {
		        putValue( "keystore", null );
		    }
		}
	  }
    }


    //==========================================================
    // internals
    //==========================================================

    private boolean isaKeystore( File file )
    {
        int n = 0;
        FileInputStream s = null;
        boolean result = false;
        KeyStore k = null;
        try
	  {
            k = KeyStore.getInstance(KeyStore.getDefaultType());
		if( file != null ) if( file.exists() ) s = new FileInputStream( file );
	      if( trace ) System.out.println("file: " + file );
		if( trace ) System.out.println("k: " + k );
		if( trace ) System.out.println("s: " + s );
            k.load( s, null );
		result = true;
	  }
        catch( Exception e )
        {
		result = false;
        }
        finally
        {
            if( s != null ) try
	      {
		    s.close();
                s = null;
		}
	      catch( Exception e )
		{
            }
            k = null;
        }

        if( trace ) System.out.println("validation: " + result );
        return result;
    }

}
