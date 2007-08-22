
package net.osm.shell.vault;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;

/**
 * The <code>LoginAction</code> handles redirection of a user login
 * request to the Vault.
 *
 * @see DesktopVault
 * @author  Stephen McConnell
 * @version 1.0 21 JUN 2001
 */
public class LoginAction extends AbstractAction 
{

    //==========================================================
    // state
    //==========================================================

   /**
    * Vault handling login execution and principal establishment.
    */
    private DesktopVault vault;

    //==========================================================
    // constructor
    //==========================================================

   /**
    * Creation of a new LoginAction.
    * @param name the name of the action
    * @param vault the vault handling the login action
    */
    public LoginAction( String name, DesktopVault vault ) 
    {
        super( name );
        this.vault = vault;
    }

    //==========================================================
    // ActionListener
    //==========================================================

   /**
    * Executes authentication of a user against a login context, 
    * and if successful result in the initation by the valut of a 
    * principal change event.
    * 
    * @param event action event initiating the action
    * @see DesktopVault#login
    */
    public void actionPerformed( ActionEvent event )
    {
        try
	  {
	      vault.login();
	  }
	  catch( Exception e )
	  {
	  }
    }
}
