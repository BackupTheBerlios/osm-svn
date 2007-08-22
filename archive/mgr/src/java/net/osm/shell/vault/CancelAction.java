
package net.osm.shell.vault;

import java.awt.Component;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;


/**
 * The <code>KeyCreateCancelAction</code>
 *
 * @author  Stephen McConnell
 * @version 1.0 21 JUN 2001
 */
public class CancelAction extends AbstractAction
{

    //==========================================================
    // state
    //==========================================================

   /**
    * The root dialog.
    */
    private Closable closable;

    private Component source;

    //==========================================================
    // constructor
    //==========================================================

   /**
    * Default constructor.
    */
    CancelAction( String name, Component source, Closable closable )
    {
        super( name );
        this.closable = closable;
        this.source = source;
    }

    //==========================================================
    // ActionListener
    //==========================================================

   /**
    * Called when the cancel button is trigged.
    */
    public void actionPerformed( ActionEvent event )
    {
        if( event.getSource() == source ) closable.close( this );
    }
}
