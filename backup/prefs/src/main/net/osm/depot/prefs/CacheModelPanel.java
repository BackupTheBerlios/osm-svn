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
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Window;
import java.awt.Dialog;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.rmi.RemoteException;
import java.util.logging.Logger;
import java.util.logging.Level;

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
import net.dpml.transit.util.ExceptionHelper;
import net.dpml.transit.model.CacheModel;
import net.dpml.transit.model.LayoutRegistryModel;
import net.dpml.transit.model.HostModel;
import net.dpml.transit.model.UnknownKeyException;
import net.dpml.transit.CacheHandler;

/**
 * Runnable plugin that handles DPML environment setup.
 *
 * @author <a href="mailto:mcconnell@osm.net">OSM</a>
 */
class CacheModelPanel extends ClassicPanel implements PropertyChangeListener
{
    //--------------------------------------------------------------
    // static
    //--------------------------------------------------------------

    static EmptyBorder border5 = new EmptyBorder( 5, 5, 5, 5);

    //--------------------------------------------------------------
    // state
    //--------------------------------------------------------------

    private final Window m_parent;

    private final ClassicTable m_table;
    private final EditAction m_editAction;
    private final JButton m_edit;
    private final JButton m_delete;
    private final CacheModel m_model;
    private final LayoutRegistryModel m_layouts;
    private final CacheAction m_cache;
    private final CacheTableModel m_tableModel;

    private String m_selection; // selected hostname

    //--------------------------------------------------------------
    // constructor
    //--------------------------------------------------------------

    public CacheModelPanel( 
      Window parent, CacheModel model, LayoutRegistryModel layouts )
    {
        Transit.getInstance();

        m_parent = parent;
        m_model = model;
        m_layouts = layouts;

        m_editAction = new EditAction( "Edit" );
        m_edit = new JButton( m_editAction );
        m_delete = new JButton( new DeleteAction( "Delete" ) );
        m_cache = new CacheAction( "Cache" );

        JLabel label = 
          IconHelper.createImageIconJLabel( 
            getClass().getClassLoader(), MISC_IMG_PATH, 
            "Cache", "Cache settings." ); 
        label.setBorder( new EmptyBorder( 0, 5, 0, 0 ) );
        CacheAction cacheHandler = new CacheAction( "Cache" );
        getHeader().addEntry( label, "Cache Directory Settings", new JButton( cacheHandler ) );

        JPanel hostPanel = new JPanel();
	  hostPanel.setLayout( new BorderLayout() );
        TitledBorder tb = 
          new TitledBorder( 
            new EmptyBorder( 0,0,0,0 ), "Resource Hosts", TitledBorder.LEFT, TitledBorder.TOP );
        hostPanel.setBorder( new CompoundBorder( tb, border5 ) );
        getBody().add( hostPanel );

        TableColumnModel columns = createHostsColumnModel();
        m_tableModel = new CacheTableModel( model );
        m_table = new ClassicTable( m_tableModel, columns );
        m_table.addPropertyChangeListener( this );
        m_table.setShowVerticalLines( false );
        m_table.setShowHorizontalLines( false );

        JButton[] buttons = new JButton[ 3 ];
        buttons[0] = new JButton( new AddAction( "Add" ) );
        buttons[1] = m_edit;
        buttons[2] = m_delete;
        getBody().addScrollingEntry( m_table, "Hosts", buttons );
    }

    public void dispose()
    {
        m_table.removePropertyChangeListener( this );
        m_tableModel.dispose();
    }

   /**
    * handle property change events raised by the table model.
    */
    public void propertyChange( PropertyChangeEvent event )
    {
        if( "selection".equals( event.getPropertyName() ) )
        {
            m_selection = (String) event.getNewValue();
            if( null != m_selection )
            {
                try
                {
                    HostModel model = m_model.getHostModel( m_selection );
                    m_edit.setEnabled( true );
                    m_delete.setEnabled( false == model.isBootstrap() );                
                    getRootPane().setDefaultButton( m_edit );
                }
                catch( UnknownKeyException e )
                {
                    m_edit.setEnabled( false );
                    m_delete.setEnabled( false );
                }
                catch( RemoteException e )
                {
                    final String error = 
                      "Remote exception occured while interigating host model.";
                    throw new RuntimeException( error, e );
                }
               
            }
            else
            {
                m_edit.setEnabled( false );
                m_delete.setEnabled( false );
            }
        }
        else if( "doubleclick".equals( event.getPropertyName() ) )
        {
            m_editAction.editSelectedHost( m_edit );
        }
    }

    private class CacheAction extends AbstractAction
    {
        CacheAction( String name )
        {
            super( name );
        }

        public void actionPerformed( ActionEvent event )
        {
            try
            {
                final String title = "Resource Cache";
                final Dimension size = new Dimension( 400, 220 );
                ClassicDialog dialog = ClassicDialog.createDialog( m_parent, title, size );
                CacheDirectoryPanel panel = new CacheDirectoryPanel( dialog, m_model );
                dialog.getBody().add( panel );
                dialog.setLocationRelativeTo( (Component) event.getSource() );
                dialog.setResizable( false );
                dialog.setVisible(true);
            }
            catch( Throwable e )
            {
                final String error = 
                  "Unexpected error while attempting to handle the 'Cache' action. "
                  + "\nCODEBASE: "
                  + getClass().getProtectionDomain().getCodeSource().getLocation().toString();
                Logger logger = Logger.getLogger( "depot.prefs" );
                logger.log( Level.SEVERE, error, e );
            }
        }
    }

    private class ControllerAction extends AbstractAction
    {
        ControllerAction( String name )
        {
            super( name );
        }

        public void actionPerformed( ActionEvent event )
        {
            try
            {
                final String title = "Cache Controller";
                final Dimension size = new Dimension( 400, 240 );
                ClassicDialog dialog = ClassicDialog.createDialog( m_parent, title, size );
                PluginModelPanel panel = 
                  new PluginModelPanel( dialog, m_model, m_cache, CacheHandler.class );
                dialog.getBody().add( panel );
                dialog.setLocationRelativeTo( (Component) event.getSource() );
                dialog.setResizable( false );
                dialog.setVisible(true);
            }
            catch( Throwable e )
            {
                final String error = 
                  "Unexpected error while attempting to handle the 'Controller' action. "
                  + "\nCODEBASE: "
                  + getClass().getProtectionDomain().getCodeSource().getLocation().toString();
                Logger logger = Logger.getLogger( "depot.prefs" );
                logger.log( Level.SEVERE, error, e );
            }
        }
    }


    //--------------------------------------------------------------
    // utilities
    //--------------------------------------------------------------

    private static JPanel createLabel( String name )
    {
        JLabel label = new JLabel( name );
        label.setBorder( new EmptyBorder( 10, 10, 10, 10 ) );
        JPanel panel = new JPanel( new FlowLayout( FlowLayout.LEFT ) );
        panel.add( label );
        return panel;
    }

   /**
    * Creation of a parameterized scroll pane.
    * @param view the viewport view
    * @return the scroll pane wrapping the supplied view
    */
    private static JScrollPane createScrollPanel( Component view )
    {
        JScrollPane scroller = new JScrollPane();
        scroller.setVerticalScrollBarPolicy( 
          JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED );
        scroller.setHorizontalScrollBarPolicy( 
          JScrollPane.HORIZONTAL_SCROLLBAR_NEVER );
        scroller.setViewportView( view );
        return scroller;
    }

   /**
    * Utility method to construct the hosts table column model.
    * @return the table
    */
    private static TableColumnModel createHostsColumnModel()
    {
        TableColumnModel model = new DefaultTableColumnModel();

	  TableColumn host = new TableColumn( 0, 250, new ClassicCellRenderer(), null );
        host.setHeaderValue( "Host" );
	  model.addColumn( host );

	  return model;
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
            String name = JOptionPane.showInputDialog( m_parent, "Host name:" );
            if( null == name )
            {
                return;
            }
            try
            {
                m_model.addHostModel( name );
            }
            catch( Throwable e )
            {
                final String error = 
                  "Unexpected error while attempting to add a new host."
                  + "\nCODEBASE: "
                  + getClass().getProtectionDomain().getCodeSource().getLocation().toString();
                Logger logger = Logger.getLogger( "depot.prefs" );
                logger.log( Level.SEVERE, error, e );
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
            editSelectedHost( (Component) event.getSource() );
        }

        public void editSelectedHost( Component source)
        {
            try
            {
                final String title = "Resource Host: " + m_selection;
                final Dimension size = new Dimension( 400, 360 );
                ClassicDialog dialog = ClassicDialog.createDialog( m_parent, title, size );
                HostModel model = m_model.getHostModel( m_selection );
                HostModelPanel panel = new HostModelPanel( dialog, model, m_layouts );
                dialog.getBody().add( panel );
                dialog.setLocationRelativeTo( source );
                dialog.setResizable( false );
                dialog.setVisible(true);
            }
            catch( Throwable e )
            {
                final String error = 
                  "Unexpected error while attempting to construct host dialog."
                  + "\nCODEBASE: "
                  + getClass().getProtectionDomain().getCodeSource().getLocation().toString();
                Logger logger = Logger.getLogger( "depot.prefs" );
                logger.log( Level.SEVERE, error, e );
            }
        }
    }

    private class DeleteAction extends AbstractAction
    {
        public DeleteAction( String name )
        {
             super( name );
             setEnabled( false );
        }

        public void actionPerformed( ActionEvent event )
        {
            if( null == m_selection )
            {
                return;
            }
            
            try
            {
                HostModel host = m_model.getHostModel( m_selection );
                m_model.removeHostModel( host );
            }
            catch( Throwable e )
            {
                final String error = 
                  "Unexpected error while attempting to delete a host preferences node."
                  + "\nCODEBASE: "
                  + CacheModelPanel.class.getProtectionDomain().getCodeSource().getLocation().toString();
                Logger logger = Logger.getLogger( "depot.prefs" );
                logger.log( Level.SEVERE, error, e );
            }
        }
    }

    private static String CACHE_IMG_PATH = "net/dpml/depot/prefs/images/cache.jpg";
    private static String MISC_IMG_PATH = "net/dpml/depot/prefs/images/cache.png";

}
