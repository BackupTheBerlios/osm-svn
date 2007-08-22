
package net.osm.shell.control.activity;

import java.awt.Component;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;


/**
 * The <code>KeyCreateCancelAction</code> is an action handler that will 
 * invoke a calcel operation of a supplied Closable instance when activated.
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
