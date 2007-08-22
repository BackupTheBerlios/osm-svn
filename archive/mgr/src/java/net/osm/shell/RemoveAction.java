
package net.osm.shell;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import org.apache.avalon.framework.activity.Disposable;


/**
 * The <code>RemoveAction</code> class invokes dispose on the 
 * the supplied <code>Disposable</code> constructor argument.
 *
 * @author  Stephen McConnell
 * @version 1.0 01 SEP 2001
 * @see RenameAction
 */
class RemoveAction extends AbstractAction
{
    //==========================================================
    // state
    //==========================================================

    private Disposable disposable;

    //==========================================================
    // constructor
    //==========================================================

   /**
    * Default constructor.
    */
    public RemoveAction( String name, Disposable disposable ) 
    {
        super( name );
        this.disposable = disposable;
    }

    //==========================================================
    // ActionListener
    //==========================================================

   /**
    * Remove the entity.
    */
    public void actionPerformed( ActionEvent event )
    {
	  this.disposable.dispose( );
    }
}
