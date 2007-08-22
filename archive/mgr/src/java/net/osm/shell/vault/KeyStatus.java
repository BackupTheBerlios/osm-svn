
package net.osm.shell.vault;


/**
 * The <code>KeyStatus</code> class.
 *
 * @author  Stephen McConnell
 * @version 1.0 21 AUG 2001
 */
public class KeyStatus
{

    //==========================================================
    // state
    //==========================================================

    private String alias;

    private boolean value;

    private Throwable throwable;

    //==========================================================
    // constructor
    //==========================================================

   /**
    * Creates a new KeyStatus instance.
    * 
    * @param alias the alias name of the key.
    * @param value boolean status of completion.
    */
    public KeyStatus( String alias, boolean value )
    {
	  this.alias = alias;
	  this.value = value;
    }


   /**
    * Creates a new KeyStatus instance containing an exception.
    * 
    * @param alias the alias name of the key.
    * @param throwable exception raised during the key creation process.
    */
    public KeyStatus( String alias, Throwable throwable )
    {
	  this.alias = alias;
	  this.throwable = throwable;
	  this.value = false;
    }


    //==========================================================
    // methods
    //==========================================================

    public boolean getValue()
    {
        return value;
    }

    public String getAlias()
    {
        return alias;
    }

    public Throwable getCause()
    {
        return throwable;
    }
}
