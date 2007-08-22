
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
public class ToggleVisibilityAction extends AbstractAction
{

    //==========================================================
    // state
    //==========================================================

   /**
    * The root dialog.
    */
    private Component target;

    private Component source;

    //==========================================================
    // constructor
    //==========================================================

   /**
    * Default constructor.
    */
    ToggleVisibilityAction( String name, Component source, Component target )
    {
        super( name );
        this.target = target;
        this.source = source;
    }

    //==========================================================
    // ActionListener
    //==========================================================

   /**
    * Called when the source trigged.
    */
    public void actionPerformed( ActionEvent event )
    {
        if( event.getSource() == source ) target.setVisible( !target.isVisible() );
    }
}
