
package net.osm.shell.vault;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;

/**
 * The <code>LogoutAction</code> class handles redirection of logout
 * requests to the Vault.
 *
 * @see DesktopVault#logout
 * @author  Stephen McConnell
 * @version 1.0 21 JUN 2001
 */
public class LogoutAction extends AbstractAction 
{

    //==========================================================
    // state
    //==========================================================

   /**
    * Vault against which the logout will be invoked.
    */
    private DesktopVault vault;

    //==========================================================
    // constructor
    //==========================================================

   /**
    * Creation of a new LoginAction.
    */
    public LogoutAction( String name, DesktopVault vault ) 
    {
        super( name );
        this.vault = vault;
    }

    //==========================================================
    // ActionListener
    //==========================================================

   /**
    * Logout of a established security subject.  A side-effect of 
    * this operation is the raising of a PrincipalEvent referencing  
    * the default unknown principal.
    *
    * @param event the action event
    * @see DesktopVault#logout
    */
    public void actionPerformed( ActionEvent event )
    {
        try
	  {
	      vault.logout();
	  }
	  catch( Exception e )
	  {
	  }
    }
}
