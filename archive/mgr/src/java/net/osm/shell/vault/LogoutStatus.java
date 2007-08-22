
package net.osm.shell.vault;


/**
 * The <code>KeyStatus</code> class.
 *
 * @author  Stephen McConnell
 * @version 1.0 21 AUG 2001
 */
public class LogoutStatus
{

    //==========================================================
    // state
    //==========================================================

   /**
    * Internal error.
    */
    private Throwable exception;

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
    public LogoutStatus( )
    {
    }

   /**
    * Creates a new LoginStatus instance containing an error.
    * 
    * @param error internal exception raised during authentication.
    */
    public LogoutStatus( Throwable error )
    {
	  this.exception = error;
    }


    //==========================================================
    // methods
    //==========================================================

    public Throwable getException()
    {
        return exception;
    }
}
