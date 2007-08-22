
package net.osm.shell;

import java.awt.Component;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.JDialog;
import javax.swing.SwingUtilities;

/**
 * The <code>HideWindowAction</code>
 *
 * @author  Stephen McConnell
 * @version 1.0 21 JUN 2001
 */
class HideWindowAction extends AbstractAction
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
            JDialog dialog = getDialog( source );
            if( dialog != null ) dialog.setVisible( false );
        }
    }

    private static JDialog getDialog( Component component )
    {
        if( component instanceof JDialog ) return (JDialog) component;
	  return (JDialog) SwingUtilities.getAncestorOfClass( JDialog.class, component);
    }

}
