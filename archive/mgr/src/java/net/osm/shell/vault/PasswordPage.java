
package net.osm.shell.vault;

import java.io.File;
import java.io.FileInputStream;
import java.math.BigInteger;
import java.util.Random;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.AbstractAction;
import javax.swing.JLabel;
import javax.swing.Action;

import java.security.KeyStore;

import net.osm.shell.control.wizard.Wizard;
import net.osm.shell.control.wizard.Page;
import net.osm.shell.control.path.PathPanel;
import net.osm.shell.control.field.PasswordPanel;
import net.osm.shell.control.field.NewPasswordPanel;
import net.osm.shell.control.field.FieldException;
import net.osm.util.ExceptionHelper;

/**
 * The <code>AboutPage</code> is helper class related to 
 * the Wizard class.  It supplies support for the monitoring 
 * of a cancel event and notification to the wizard.
 *
 * @author  Stephen McConnell
 * @version 1.0 21 AUG 2001
 */
public class PasswordPage extends Page implements PropertyChangeListener
{

    //==========================================================
    // static
    //==========================================================

    private static final Random random = new Random();

    //==========================================================
    // state
    //==========================================================

    private boolean trace = false;
    private String virginMessage =
      "Please enter a password to protect your keystore. During login " +
      "you will be prompted to provide this in order to authenticate your " +
      "identity.";
    private String standardMessage = 
           "Please enter your keystore password.";
    private File keystoreFile = null;
    private Wizard wizard;

    private PasswordPanel passwordPanel;
    private NewPasswordPanel newPasswordPanel;
    private char[] password;

    private BigInteger magic;

    //==========================================================
    // constructor
    //==========================================================

   /**
    * PasswordPage creation given a source action (representing the 
    * the source of keystore change events), the wizard into which
    * context messages are to be placed, and name for this action.
    * 
    * @param config keystore source
    * @param wizard the wizard into which context is placed
    * @param name the name of the action
    */
    public PasswordPage( Action config, Wizard wizard, String name ) throws VaultException
    {
        super( name );
        this.magic = getRandom();
	  this.wizard = wizard;
        try
        {
            putValue( "password", null );
		config.addPropertyChangeListener( this );
        }
        catch( Exception e )
        {
            throw new VaultException(
              "Unable to create a password page.", e );
        }
    }

    //==========================================================
    // ActionListener
    //==========================================================

   /**
    * Called when the page is made active by the wizard.
    */
    public void actionPerformed( ActionEvent event )
    {

        getPanel().setCommandName( "Password" );

        // check if the keystore really exists - if so, do a 
	  // password validation, otherwise get the password from 
	  // the user

        try
	  {
	      if( keystoreFile.exists() )
	      {
                setupForExistingKeystore();
	      }
	      else
	      {
                setupForNewKeystore();
	      }
	  }
        catch( Exception e )
	  {
	      ExceptionHelper.printException(
		  "Unable to handle keystore change event.", e );
	  }
    }

    //==========================================================
    // PropertyChangeListener
    //==========================================================

   /**
    * Listens to file property changes - if the file held by the 
    * path panel is in fact a keystore, then enable the next button.
    */
    public void propertyChange( PropertyChangeEvent event )
    {
        String name = event.getPropertyName();
	  if( name.equals("keystore") )
        {
		//
		// the user has cycled back and changed the 
		// keystore - we have to rebuild the password page
		// to reflect the keystore value - possible values
		// for keystore include:
		//
		// null      - just clear the password fields
		//             and set password status to false
		// exists    - setup authentication
		// not exist - setup new password entry field
		// 

            putValue("password", null );
		Object o = event.getNewValue();
		if( o instanceof File )
		{
		    try
	          {
		        keystoreFile = (File) o;
		        if( keystoreFile.exists() )
		        {
		            if( trace ) System.out.println(
				  "\tPASSWORD-PAGE, existing keystore" + o);
			      setupForExistingKeystore();
		        }
	              else
		        {
		            if( trace ) System.out.println(
				  "\tPASSWORD-PAGE, new keystore " + o);
			      newPasswordPanel = null;
			      setupForNewKeystore();
			  }
	          }
		    catch( Exception e )
	          {
			  ExceptionHelper.printException(
			    "Unable to handle keystore change event.", e );
	          }
		}
		else
	      {
		    if( trace ) System.out.println("\tPASSWORD-PAGE, null keystore");
                keystoreFile = null;
		    putValue("password", null );
            }
        }
        else if( name.equals("password"))
	  {
		Object source = event.getSource();
		if(( source == newPasswordPanel ) || (source == passwordPanel))
		{
		    putValue( "password", event.getNewValue() );
		}
	  }
    }

    private void setupForNewKeystore() throws FieldException
    {
        wizard.setMessage( virginMessage );
        if( newPasswordPanel == null ) createNewPasswordPanel();
        getPanel().setCommandBody( newPasswordPanel );
    }

    private NewPasswordPanel createNewPasswordPanel() throws FieldException
    {
	  newPasswordPanel = new NewPasswordPanel( 20, 8, magic );
        newPasswordPanel.addPropertyChangeListener( this );
	  return newPasswordPanel;
    }

    private void setupForExistingKeystore() throws FieldException
    {
        wizard.setMessage( standardMessage );
	  if( passwordPanel == null ) passwordPanel = createPasswordPanel();
        getPanel().setCommandBody( passwordPanel );
    }

    private PasswordPanel createPasswordPanel() throws FieldException
    {
	  passwordPanel = new PasswordPanel( password, 20, keystoreFile );
        passwordPanel.addPropertyChangeListener( this );
        return passwordPanel;
    }

    //==========================================================
    // internals
    //==========================================================

    private static BigInteger getRandom() 
    {
	  return new BigInteger( 6, random );
    }

}
