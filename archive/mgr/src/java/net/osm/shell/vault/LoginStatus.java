
package net.osm.shell.vault;


/**
 * The <code>KeyStatus</code> class.
 *
 * @author  Stephen McConnell
 * @version 1.0 21 AUG 2001
 */
public class LoginStatus
{

    //==========================================================
    // state
    //==========================================================

   /**
    * True if authentication was sucessful.
    */
    private boolean authenticated;

   /**
    * Internal error.
    */
    private Throwable error;

    //==========================================================
    // constructor
    //==========================================================

   /**
    * Creates a new LoginStatus instance containign the result of 
    * an authentication process.
    * 
    * @param authenticated result of the authentication process
    * @param error true if an internal error occured during authentication.
    */
    public LoginStatus( boolean authenticated )
    {
	  this.authenticated = authenticated;
    }

   /**
    * Creates a new LoginStatus instance containing an error.
    * 
    * @param error internal exception raised during authentication.
    */
    public LoginStatus( Throwable error )
    {
	  this.authenticated = false;
	  this.error = error;
    }


    //==========================================================
    // methods
    //==========================================================

    public boolean getAuthenticated()
    {
        return authenticated;
    }

    public Throwable getException()
    {
        return error;
    }
}
