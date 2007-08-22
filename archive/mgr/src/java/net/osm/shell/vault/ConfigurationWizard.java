
package net.osm.shell.vault;

import java.io.File;
import java.math.BigInteger;
import java.awt.Frame;
import java.awt.Component;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowListener;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;

import net.osm.util.ExceptionHelper;
import net.osm.shell.control.wizard.Wizard;
import net.osm.shell.control.wizard.WizardException;
import net.osm.shell.control.wizard.PageEvent;
import net.osm.shell.control.wizard.Page;

/**
 * The <code>ConfigurationWizard</code>
 *
 * @author  Stephen McConnell
 * @version 1.0 21 JUN 2001
 */
public class ConfigurationWizard extends AbstractAction implements PropertyChangeListener
{
    //==========================================================
    // static
    //==========================================================

    public static final int CANCEL = Wizard.CANCEL;
    public static final int PREV = Wizard.PREV;
    public static final int NEXT = Wizard.NEXT;
    public static final int FINISH = Wizard.FINISH;
    public static final int CLOSE = Wizard.CLOSE;

    //==========================================================
    // state
    //==========================================================

    private boolean trace = false;

    private File defaultKeystore = new File(
      System.getProperty("user.home") + 
	System.getProperty("file.separator") +
	".keystore" );

    private AboutPage aboutPage;

    private KeystoreSelectionPage keystoreSelectionPage;

    private PasswordPage passwordPage;

    private AliasPage aliasPage;

    private Wizard wizard;

    private Object password;

    private boolean alias = false;

    private VaultEntity vault;

    private BigInteger magic;

    //==========================================================
    // constructor
    //==========================================================

   /**
    * Default constructor.
    */
    public ConfigurationWizard( 
      VaultEntity vault, BigInteger magic, Frame frame, File file ) 
    throws VaultException
    {
        super( "Vault Configuration ..." );
        putValue("keystore", null );

        if( file != null ) defaultKeystore = file;
        try
	  {
            wizard = new Wizard( frame );
		wizard.addPropertyChangeListener( this );

		aboutPage = new AboutPage( wizard, "About" );

		keystoreSelectionPage = 
		  new KeystoreSelectionPage( wizard, "Keystore", defaultKeystore );
            keystoreSelectionPage.addPropertyChangeListener( this );

            passwordPage = new PasswordPage( this, wizard, "Password" );
            passwordPage.addPropertyChangeListener( this );

		aliasPage = new AliasPage( this, wizard, "Alias" );
            aliasPage.addPropertyChangeListener( this );

        }
        catch(Exception e)
        {
            ExceptionHelper.printException("ConfigurationWizard:", e );
		throw new VaultException("Unable to create the configuration wizard.", e );
        }

        //
        // startup the wizard by setting the inital page
        //

        try
	  {
            keystoreSelectionPage.setFile( defaultKeystore );
		wizard.setPage( aboutPage );
        }
        catch(Exception e)
        {
            ExceptionHelper.printException("ConfigurationWizard:", e );
		throw new VaultException("Unable to start the configuration wizard.", e );
        }
    }

    //==========================================================
    // ActionListener
    //==========================================================

   /**
    * Establishes the modal dialog.
    */
    public void actionPerformed( ActionEvent event )
    {
	  wizard.setVisible( true );
    }

    //==========================================================
    // PropertyChangeListener
    //==========================================================

   /**
    * Listens to property changes from pages.
    */
    public void propertyChange( PropertyChangeEvent event )
    {
        if( event.getSource() == wizard )
	  {
	      handleWizardEvent( event );
	  }
	  else
	  {
            handleVaultEvent( event );
        }
    }

    private void handleWizardEvent( PropertyChangeEvent event )
    {
        if( event.getPropertyName().equals("command") )
	  {
            if( trace ) System.out.println("\tCONFIG/command " + event.getNewValue() );
	      handleCommandEvent( (PageEvent) event.getNewValue() );
        }
        else if( event.getPropertyName().equals("page") )
	  {
            if( trace ) System.out.println("\tCONFIG/page ");
		setControls();
        }
    }

    private void handleCommandEvent( PageEvent pageEvent )
    {
	  int command = pageEvent.getCommand();
	  try
	  {
            if( command == CANCEL )
            {
		    close( false );
            }
            else if( command == CLOSE )
            {
		    close( false ); 
            }
            else if( command == FINISH )
            {
		    close( true );
            }
		else if( command == NEXT )
		{
		    Page page = wizard.getPage();
		    if( page == aboutPage )
		    {
		        wizard.setPage( keystoreSelectionPage );
	          }
		    else if( page == keystoreSelectionPage )
		    {
		        wizard.setPage( passwordPage );
	          }
		    else if( page == passwordPage )
		    {
		        wizard.setPage( aliasPage );
	          }
            }
		else if( command == PREV )
		{
		    Page page = wizard.getPage();
		    if( page == aliasPage )
		    {
			  wizard.setPage( passwordPage );
	          }
		    else if( page == passwordPage )
		    {
			  wizard.setPage( keystoreSelectionPage );
	          }
		    else if( page == keystoreSelectionPage )
		    {
			  wizard.setPage( aboutPage );
	          }
		}
        }
        catch( WizardException wiz )
	  {
	      ExceptionHelper.printException(
		  "Unexpected exception while handling wizard event", wiz );
        }
    }

    private void setControls()
    {
        Page page = wizard.getPage();
        if( page == aboutPage )
        {
		wizard.setEnabled( CANCEL, true );
		wizard.setEnabled( PREV, false );
		wizard.setEnabled( NEXT, true );
		wizard.setEnabled( FINISH, false );
		wizard.setDefaultButton( NEXT );
        }
        else if( page == keystoreSelectionPage )
        {
		wizard.setEnabled( CANCEL, true );
		wizard.setEnabled( PREV, true );
		wizard.setEnabled( NEXT, getKeystoreStatus() );
		wizard.setEnabled( FINISH, false );
		wizard.setDefaultButton( NEXT );
        }
        else if( page == passwordPage )
        {
		wizard.setEnabled( CANCEL, true );
		wizard.setEnabled( PREV, true );
		wizard.setEnabled( NEXT, getPasswordStatus() );
		wizard.setEnabled( FINISH, false );
		wizard.setDefaultButton( NEXT );
        }
        else if( page == aliasPage )
        {
		wizard.setEnabled( CANCEL, true );
		wizard.setEnabled( PREV, true );
		wizard.setEnabled( NEXT, false );
		wizard.setEnabled( FINISH, getAliasStatus() );
        }
    }

    private void handleVaultEvent( PropertyChangeEvent event )
    {
        String name = event.getPropertyName();
	  if( name.equals("keystore") )
        {
            if( trace ) System.out.println("\tKEYSTORE-FILE-CHANGE: " + event.getNewValue() );
	      putValue("keystore", event.getNewValue() );
        }
	  else if( name.equals("password") )
        {
	      password = event.getNewValue();
            if( trace ) System.out.println("\tPASSWORD-CHANGE: " + getPasswordStatus() );
	      putValue("password", event.getNewValue() );
        }
	  else if( name.equals("alias") )
        {
	      alias = booleanValue( event );
            if( trace ) System.out.println("\tALIAS-CHANGE: " + getAliasStatus() );
        }
	  setControls();
    }

    private boolean getKeystoreStatus()
    {
        return ( getValue("keystore") != null );
    }

    private boolean getPasswordStatus()
    {
        return ( password != null );
    }

    private boolean getAliasStatus()
    {
        return alias;
    }

    private boolean booleanValue( String key )
    {
        Object object = getValue( key );
        if( object == null ) return false;
	  if( !( object instanceof Boolean )) return false;
	  return ((Boolean)object).booleanValue();
    }

    private boolean booleanValue( PropertyChangeEvent event )
    {
	 Object object = event.getNewValue();
	 if( object instanceof Boolean ) return ((Boolean)object).booleanValue();
       return false;
    }

    private void close( boolean value )
    {
        try
	  {
            wizard.setVisible( false );
	      wizard.setPage( aboutPage );
            if( !value )
            {
                putValue("keystore",defaultKeystore);
            }
        }
        catch( Exception e )
	  {
	      ExceptionHelper.printException(
		  "Unexpected exception during CopnfigurationWizard close.", e );
	  }
    }
}
