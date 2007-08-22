
package net.osm.shell.control.wizard;


/**
 * The <code>PageEvent</code> class.
 *
 * @author  Stephen McConnell
 * @version 1.0 21 AUG 2001
 */
public class PageEvent
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

    private Page page;
    private int command;

    //==========================================================
    // constructor
    //==========================================================

   /**
    * constructor.
    * 
    * @param name the name of the action
    * @param coordinator that the action is attached to
    * @param enabled the inital enabled state
    */
    public PageEvent( Page page, int command )
    {
        this.page = page;
	  this.command = command;
    }

    //==========================================================
    // methods
    //==========================================================

    public Page getPage()
    {
        return page;
    }

    public int getCommand()
    {
        return command;
    }
}
