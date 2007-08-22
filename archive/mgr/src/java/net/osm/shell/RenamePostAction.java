
package net.osm.shell;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;


/**
 * The <code>RenameAction</code> class redirects an rename 
 * request to the <code>RenameDialog</code> passed under 
 * the constructor arguments.
 *
 * @author  Stephen McConnell
 * @version 1.0 01 SEP 2001
 * @see RenameAction
 */
class RenamePostAction extends AbstractAction
{
    //==========================================================
    // state
    //==========================================================

    private RenameAction  source;

    //==========================================================
    // constructor
    //==========================================================

   /**
    * Default constructor.
    */
    public RenamePostAction( String name, RenameAction source ) 
    {
        super( name );
        this.source = source;
    }

    //==========================================================
    // ActionListener
    //==========================================================

   /**
    * Establishes the modal dialog.
    */
    public void actionPerformed( ActionEvent event )
    {
	  source.doRename( event );
    }
}
