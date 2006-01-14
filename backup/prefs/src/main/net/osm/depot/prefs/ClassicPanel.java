/*
 * Copyright 2004 Stephen J. McConnell.
 *
 * Licensed  under the  Apache License,  Version 2.0  (the "License");
 * you may not use  this file  except in  compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed  under the  License is distributed on an "AS IS" BASIS,
 * WITHOUT  WARRANTIES OR CONDITIONS  OF ANY KIND, either  express  or
 * implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.dpml.depot.prefs;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Window;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.net.ContentHandler;
import java.util.prefs.Preferences;

import javax.swing.AbstractAction;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

/**
 * Runnable plugin that handles DPML environment setup.
 *
 * @author <a href="mailto:mcconnell@osm.net">OSM</a>
 */
class ClassicPanel extends JPanel
{
    //--------------------------------------------------------------
    // static
    //--------------------------------------------------------------

    static EmptyBorder border5 = new EmptyBorder( 5, 5, 5, 5);

    //--------------------------------------------------------------
    // state
    //--------------------------------------------------------------

    private Header m_header;
    private Body m_body;
    private JPanel m_footer;

    //--------------------------------------------------------------
    // constructor
    //--------------------------------------------------------------

    public ClassicPanel()
    {
        super( new BorderLayout() );
        
        m_header = new Header();
	  m_header.setLayout( new BoxLayout( m_header, BoxLayout.Y_AXIS ) );
        m_header.setBorder( new EmptyBorder( 0, 6, 0, 6 ) );
        add( m_header, BorderLayout.NORTH );

        m_body = new Body();
        add( m_body, BorderLayout.CENTER );

        m_footer = new JPanel();
        m_footer.setLayout( new FlowLayout( FlowLayout.RIGHT ) );
        //m_footer.setBorder( new EmptyBorder( 10, 10, 5, 0 ) );
        m_footer.setBorder( new EmptyBorder( 5, 10, 5, 0 ) );
        add( m_footer, BorderLayout.SOUTH );
    }

    public Header getHeader()
    {
        return m_header;
    }

    public Body getBody()
    {
        return m_body;
    }

    public JPanel getFooter()
    {
        return m_footer;
    }

    public class Header extends JPanel
    {
        public Header()
        {
            setLayout( new BoxLayout( m_header, BoxLayout.Y_AXIS ) );
            setBorder( new EmptyBorder( 0, 6, 0, 6 ) );
        }

        public void addEntry( JLabel label, String title, Component component )
        {
            JPanel holder = new JPanel();
	      holder.setLayout( new BoxLayout( holder, BoxLayout.Y_AXIS ) );
            holder.setBorder( new EmptyBorder( 10, 6, 0, 6 ) );

            JPanel panel = new JPanel();
	      panel.setLayout( new BorderLayout() );
            if( null != label )
            {
                panel.add( label, BorderLayout.CENTER );
            }
            panel.setBorder( 
              new CompoundBorder(
                new TitledBorder( 
                  null, title, TitledBorder.LEFT, TitledBorder.TOP), border5 ) );

            if( null != component )
            {
                JPanel buttons = new JPanel();
	          buttons.setLayout( new BorderLayout() );
                buttons.add( component, BorderLayout.SOUTH );
                panel.add( buttons, BorderLayout.EAST );
            }

            holder.add( panel );
            add( holder );
        }

        public void addStackedEntry( JLabel label, String title, Component component )
        {
            JPanel holder = new JPanel();
	      holder.setLayout( new BoxLayout( holder, BoxLayout.Y_AXIS ) );
            holder.setBorder( new EmptyBorder( 10, 6, 0, 6 ) );

            JPanel panel = new JPanel();
	      panel.setLayout( new BorderLayout() );
            if( null != label )
            {
                panel.add( label, BorderLayout.WEST );
            }
            panel.setBorder( 
              new CompoundBorder(
                new TitledBorder( 
                  null, title, TitledBorder.LEFT, TitledBorder.TOP), border5 ) );

            if( null != component )
            {
                panel.add( component, BorderLayout.SOUTH );
            }

            holder.add( panel );
            add( holder );
        }

    }

    public class Body extends JPanel
    {
        public Body()
        {
            super( new BorderLayout() );
            setBorder( new EmptyBorder( 10, 6, 0, 6 ) );
        }

        public void addScrollingEntry( Component view, String title, JButton[] buttons )
        {
            JPanel panel = new JPanel();
	      panel.setLayout( new BorderLayout() );

            TitledBorder tb = 
              new TitledBorder( 
                new EmptyBorder( 0,0,0,0 ), title, TitledBorder.LEFT, TitledBorder.TOP );
            panel.setBorder( new CompoundBorder( tb, border5 ) );

            JScrollPane scroller = new JScrollPane();

            //
            // NOTE: the following line is needed to create a reasonable looking
            // table background - however, if other platforms are using a background
            // for menu items that is not Color.WHITE - then we could have a problem
            //

            scroller.getViewport().setBackground( Color.WHITE );
            scroller.setVerticalScrollBarPolicy( 
              JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED );
            scroller.setHorizontalScrollBarPolicy( 
              JScrollPane.HORIZONTAL_SCROLLBAR_NEVER );
            scroller.setViewportView( view );

            panel.add( scroller );
 
            JPanel controls = new JPanel();
            controls.setBorder( new EmptyBorder( 10, 6, 0, 6 ) );
	      controls.setLayout( new FlowLayout( FlowLayout.RIGHT ) );
            for( int i=0; i<buttons.length; i++ )
            {
                controls.add( buttons[i] );
            }

            add( panel, BorderLayout.CENTER );
            add( controls, BorderLayout.SOUTH );
        }
    }

    //--------------------------------------------------------------
    // static
    //--------------------------------------------------------------

    public static JDialog createDialog( Window parent, String title, Dimension size, JPanel body )
    {
        JDialog dialog = null;
        if( parent instanceof Dialog )
        {
            dialog = new JDialog( (Dialog) parent, true );
        }
        else
        {
            dialog = new JDialog( (Frame) parent, true );
        }
        dialog.setTitle( title );
        JPanel frame = new JPanel();
        frame.setLayout( new BorderLayout() );
        frame.setBorder( new EmptyBorder( 10, 6, 0, 6 ) );
        frame.add( body, BorderLayout.CENTER );
        dialog.setContentPane( frame );
        dialog.setSize( size );
        return dialog;
    }
}
