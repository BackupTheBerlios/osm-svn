
package net.osm.shell.control.util;

import java.awt.Component;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.JDialog;

/**
 * The <code>HideWindowAction</code>
 *
 * @author  Stephen McConnell
 * @version 1.0 21 JUN 2001
 */
public class HideWindowAction extends AbstractAction
{

    //==========================================================
    // state
    //==========================================================

    private Component source;

    //==========================================================
    // constructor
    //==========================================================

   /**
    * Default constructor.
    */
    public HideWindowAction( String name, Component source )
    {
        super( name );
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
        if( event.getSource() == source )
        {
            JDialog dialog = ControlUtility.getDialog( source );
            if( dialog != null ) dialog.setVisible( false );
        }
    }
}
