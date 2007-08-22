
package net.osm.shell;

import java.awt.Font;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Component;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;

import net.osm.shell.MGR;
import net.osm.shell.control.field.LabelPanel;
import net.osm.shell.View;
import net.osm.shell.ScrollView;

/**
 * The <code>ListTab</code> is a tabbed panel used within a properties
 * dialog to hold an embedded scroll view.
 *
 * @author  Stephen McConnell
 * @version 1.0 01 SEP 2001
 */
class ListTab extends DefaultTab
{

    //==========================================================
    // constructor
    //==========================================================

   /**
    * Default constructor.
    */
    public ListTab( String name, Component view ) 
    {
        super( name );
        if( view instanceof View ) ((View)view).setVisibleFocus( true );
	  JPanel holder = new JPanel( new BorderLayout());
	  holder.setBorder( new EmptyBorder(0,10,10,10));
	  holder.add( view, BorderLayout.CENTER );
        add( holder );
    }
}
