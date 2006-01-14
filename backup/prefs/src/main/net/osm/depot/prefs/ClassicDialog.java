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
public class ClassicDialog extends JDialog
{
    //--------------------------------------------------------------
    // state
    //--------------------------------------------------------------

    private JPanel m_body;

    //--------------------------------------------------------------
    // constructor
    //--------------------------------------------------------------

    public ClassicDialog( Dialog parent, String title, Dimension size )
    {
        super( parent, true );
        setup( title, size );
    }

    public ClassicDialog( Frame parent, String title, Dimension size )
    {
        super( parent, true );
        setup( title, size );
    }

    public JPanel getBody()
    {
        return m_body;
    }

    private void setup( String title, Dimension size )
    {
        setTitle( title );
        m_body = new JPanel();
        m_body.setLayout( new BorderLayout() );
        m_body.setBorder( new EmptyBorder( 10, 6, 0, 6 ) );
        setContentPane( m_body );
        setSize( size );
    }

    public static ClassicDialog createDialog( Window parent, String title, Dimension size )
    {
        ClassicDialog dialog = null;
        if( parent instanceof Dialog )
        {
            dialog = new ClassicDialog( (Dialog) parent, title, size );
        }
        else
        {
            dialog = new ClassicDialog( (Frame) parent, title, size );
        }
        return dialog;
    }
}
