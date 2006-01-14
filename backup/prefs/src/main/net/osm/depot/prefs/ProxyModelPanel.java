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
import java.rmi.RemoteException;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.util.ArrayList;

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

import net.dpml.transit.Transit;
import net.dpml.transit.model.ProxyModel;
import net.dpml.transit.model.ProxyListener;

/**
 * Runnable plugin that handles DPML environment setup.
 *
 * @author <a href="mailto:mcconnell@osm.net">OSM</a>
 */
class ProxyModelPanel extends ClassicPanel implements PropertyChangeListener
{
    //--------------------------------------------------------------
    // static
    //--------------------------------------------------------------

    static EmptyBorder border5 = new EmptyBorder( 5, 5, 5, 5);

    //--------------------------------------------------------------
    // state
    //--------------------------------------------------------------

    private final Window m_parent;

    private ProxyModel m_model;
    private AddAction m_add;
    private EditAction m_edit;
    private JButton m_editButton;
    private RemoveAction m_remove;
    private SettingsAction m_settings;
    private String m_selection;
    private ProxyExcludesTableModel m_tableModel;
    private ClassicTable m_table;

    //--------------------------------------------------------------
    // constructor
    //--------------------------------------------------------------

    public ProxyModelPanel( Window parent, ProxyModel model ) throws Exception
    {
        super();

        m_parent = parent;
        m_model = model;

        //
        // setup info about the proxy host, scheme and prompt
        //
       
        JLabel label = 
          IconHelper.createImageIconJLabel( 
            getClass().getClassLoader(), SERVER_IMG_PATH, 
            "Proxy", "Proxy server settings." ); 
        label.setBorder( new EmptyBorder( 0, 5, 0, 0 ) );
        m_settings = new SettingsAction( "Settings" );
        getHeader().addEntry( label, "Proxy Server", new JButton( m_settings ) );

        // create a table containing the excludes

        m_add = new AddAction( "Add" );
        m_edit = new EditAction( "Edit" );
        m_remove = new RemoveAction( "Delete" );
        m_editButton = new JButton( m_edit );

        TableColumnModel columns = new DefaultTableColumnModel();
        TableColumn column = new TableColumn( 0, 100, new ClassicCellRenderer(), null ) ;
        column.setHeaderValue( "Host" );
	  columns.addColumn( column );

        m_tableModel = new ProxyExcludesTableModel( model );
        m_table = new ClassicTable( m_tableModel, columns );
        m_table.addPropertyChangeListener( this );

        JButton[] buttons = new JButton[ 3 ];
        buttons[0] = new JButton( m_add );
        buttons[1] = m_editButton;
        buttons[2] = new JButton( m_remove );
        getBody().addScrollingEntry( m_table, "Excluded hosts", buttons );
    }

    public void dispose()
    {
        m_table.removePropertyChangeListener( this );
        m_tableModel.dispose();
    }

    protected void finalize()
    {
        dispose();
    }

    //--------------------------------------------------------------
    // PropertyChangelistener
    //--------------------------------------------------------------

   /**
    * Handle property change events raised by the table model.
    */
    public void propertyChange( PropertyChangeEvent event )
    {
        if( "selection".equals( event.getPropertyName() ) )
        {
            m_selection = (String) event.getNewValue();
            if( null != m_selection )
            {
                m_edit.setEnabled( true );
                m_remove.setEnabled( true );
                getRootPane().setDefaultButton( m_editButton );
            }
            else
            {
                m_edit.setEnabled( false );
                m_remove.setEnabled( false );
            }
        }
        else if( "doubleclick".equals( event.getPropertyName() ) )
        {
            m_edit.edit();
        }
    }

    //--------------------------------------------------------------
    // impl
    //--------------------------------------------------------------

    private void removeExclude( String exclude )
    {
        if( null == exclude )
        {
            return;
        }
        ArrayList list = new ArrayList();
        String[] excludes = getExcludes();
        for( int i=0; i<excludes.length; i++ )
        {
            String s = excludes[i];
            if( false == exclude.equals( s ) )
            {
                list.add( s );
            }
        }
        String[] values = (String[]) list.toArray( new String[0] );
        setExcludes( values );
    }

    private void changeExclude( String exclude, String value )
    {
        if( null == exclude )
        {
            return;
        }
        if( null == value )
        {
            return;
        }
        ArrayList list = new ArrayList();
        String[] excludes = getExcludes();
        for( int i=0; i<excludes.length; i++ )
        {
            String s = excludes[i];
            if( exclude.equals( s ) )
            {
                list.add( value );
            }
            else
            {
                list.add( s );
            }
        }
        String[] values = (String[]) list.toArray( new String[0] );
        setExcludes( values );
    }

    private void addExclude( String exclude )
    {
        ArrayList list = new ArrayList();
        String[] excludes = getExcludes();
        for( int i=0; i<excludes.length; i++ )
        {
            String s = excludes[i];
            list.add( s );
        }
        list.add( exclude );
        String[] values = (String[]) list.toArray( new String[0] );
        setExcludes( values );
    }

    //--------------------------------------------------------------
    // internals
    //--------------------------------------------------------------

    private class RemoveAction extends AbstractAction
    {
        public RemoveAction( String name )
        {
             super( name );
             setEnabled( false );
        }

        public void actionPerformed( ActionEvent event )
        {
            if( m_selection != null )
            {
                removeExclude( m_selection );
            }
        }
    }

    private class EditAction extends AbstractAction
    {
        public EditAction( String name )
        {
             super( name );
             setEnabled( false );
        }

        public void actionPerformed( ActionEvent event )
        {
            edit();
        }

        public void edit()
        {
            String name = JOptionPane.showInputDialog( m_parent, "Host:", m_selection );
            if( null == name )
            {
                return;
            }
            else
            {
                changeExclude( m_selection, name );
            }
        }
    }

    private class AddAction extends AbstractAction
    {
        public AddAction( String name )
        {
             super( name );
             setEnabled( true );
        }

        public void actionPerformed( ActionEvent event )
        {
            String name = JOptionPane.showInputDialog( m_parent, "Host:" );
            if( null == name )
            {
                return;
            }
            else
            {
                addExclude( name );
            }
        }
    }

    private class SettingsAction extends AbstractAction
    {
        public SettingsAction( String name )
        {
             super( name );
             setEnabled( true );
        }

        public void actionPerformed( ActionEvent event )
        {
            try
            {
                final String title = "Proxy Host Settings";
                final Dimension size = new Dimension( 400, 280 );
                ClassicDialog dialog = ClassicDialog.createDialog( m_parent, title, size );
                ProxyFeaturesPanel panel = new ProxyFeaturesPanel( dialog, m_model );
                dialog.getBody().add( panel );
                dialog.setLocationRelativeTo( (Component) event.getSource() );
                dialog.getRootPane().setDefaultButton( panel.getDefaultButton() );
                dialog.setResizable( false );
                dialog.setVisible(true);
            }
            catch( Throwable e )
            {
                final String error = 
                  "Unexpected error while attempting to handle the 'Proxy Settings' action. "
                  + "\nCODEBASE: "
                  + getClass().getProtectionDomain().getCodeSource().getLocation().toString();
                Logger logger = Logger.getLogger( "depot.prefs" );
                logger.log( Level.SEVERE, error, e );
            }
        }
    }

    private String[] getExcludes()
    {
        try
        {
            return m_model.getExcludes();
        }
        catch( RemoteException e )
        {
            final String error = 
              "Remote exception while resolving proxy excludes.";
            throw new RuntimeException( error, e );
        }
    }

    private void setExcludes( String[] excludes )
    {
        try
        {
            m_model.setExcludes( excludes );
        }
        catch( RemoteException e )
        {
            final String error = 
              "Remote exception while setting proxy excludes.";
            throw new RuntimeException( error, e );
        }
    }


    //--------------------------------------------------------------
    // static (utils)
    //--------------------------------------------------------------

    private static String SERVER_IMG_PATH = "net/dpml/depot/prefs/images/server.png";

}
