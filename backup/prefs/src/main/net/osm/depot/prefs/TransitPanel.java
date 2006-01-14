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
import java.rmi.RemoteException;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.Action;
import javax.swing.AbstractAction;
import javax.swing.Box;
import javax.swing.SwingConstants;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JScrollPane;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.JSplitPane;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableModel;
import javax.swing.ListSelectionModel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import javax.swing.ImageIcon;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;

import net.dpml.transit.model.DuplicateKeyException;
import net.dpml.transit.model.UnknownKeyException;
import net.dpml.transit.model.TransitModel;
import net.dpml.transit.model.CacheModel;
import net.dpml.transit.model.LayoutRegistryModel;
import net.dpml.transit.model.ContentRegistryModel;
import net.dpml.transit.model.ProxyModel;

/**
 * Runnable plugin that handles DPML environment setup.
 *
 * @author <a href="mailto:mcconnell@osm.net">OSM</a>
 */
class TransitPanel extends JPanel
{
    //--------------------------------------------------------------
    // state
    //--------------------------------------------------------------

    private final Window m_parent;

    private TransitModel m_model;
    private LayoutRegistryModelPanel m_layoutPanel;
    private CacheModelPanel m_cachePanel;
    private ContentRegistryModelPanel m_registryPanel;
    private ProxyModelPanel m_proxyPanel;

    //--------------------------------------------------------------
    // constructor
    //--------------------------------------------------------------

    public TransitPanel( Window parent, TransitModel model ) throws Exception
    {
        super( new BorderLayout() );

        m_model = model;
        m_parent = parent;

        String name = model.getID();
        JLabel label = new JLabel();
        label.setText( "ID: " + name );
        label.setBorder( new EmptyBorder( 0, 5, 0, 10 ) );
        JPanel labelPanel = new JPanel();
        labelPanel.setLayout( new BorderLayout() );
        labelPanel.add( label, BorderLayout.WEST );
        labelPanel.setBorder( new EmptyBorder( 10, 6, 10, 6 ) );

        add( labelPanel, BorderLayout.NORTH );

        final JTabbedPane tabbedPane = new JTabbedPane();
        //tabbedPane.setBorder( new EmptyBorder( 0, OFFSET, OFFSET, OFFSET ) );

        //
        // add the different management models to the tabbed pane
        //

        JPanel panel = new JPanel();

        SwingUtilities.invokeLater( 
          new Runnable() {
            public void run()
            {
               try
               {
                   final CacheModel cache = m_model.getCacheModel();
                   final LayoutRegistryModel layouts = cache.getLayoutRegistryModel();
                   m_layoutPanel = new LayoutRegistryModelPanel( m_parent, layouts );
                   m_cachePanel = new CacheModelPanel( m_parent, cache, layouts );
                   tabbedPane.addTab( "Cache", m_cachePanel );
                   tabbedPane.addTab( "Layout", m_layoutPanel );
                   tabbedPane.revalidate();
               }
               catch( Throwable e )
               {
                   final String error =
                     "Unexpected during cache panel establishment.";
                   Logger logger = Logger.getLogger( "depot.prefs" );
                   logger.log( Level.SEVERE, error, e );
               }
            }
          }
        );

        SwingUtilities.invokeLater( 
          new Runnable() {
            public void run()
            {
                try
                {
                    final ContentRegistryModel content = m_model.getContentRegistryModel();
                    m_registryPanel = new ContentRegistryModelPanel( m_parent, content );
                    tabbedPane.addTab( "Content", m_registryPanel );
                }
                catch( Throwable e )
                {
                    final String error =
                      "Unexpected during content registry panel establishment.";
                    Logger logger = Logger.getLogger( "depot.prefs" );
                    logger.log( Level.SEVERE, error, e );
                }
            }
          }
        );
        
        SwingUtilities.invokeLater( 
          new Runnable() {
            public void run()
            {
                try
                {
                    final ProxyModel proxy = m_model.getProxyModel();
                    m_proxyPanel = new ProxyModelPanel( m_parent, proxy );
                    tabbedPane.addTab( "Proxy", m_proxyPanel );
                }
                catch( Throwable e )
                {
                    final String error =
                      "Unexpected during proxy panel establishment.";
                    Logger logger = Logger.getLogger( "depot.prefs" );
                    logger.log( Level.SEVERE, error, e );
                }
            }
          }
        );

        //
        // package
        //
        
        add( tabbedPane, BorderLayout.CENTER );
    }

    public void dispose()
    {
    }

    private class ButtonPanel extends Box
    {
        private JButton m_close;

        ButtonPanel()
        {
            super( BoxLayout.Y_AXIS );

            m_close = new JButton( );
            Action closeAction = new CancelAction( "Close" );
            m_close.setAction( closeAction );
            JPanel buttonPanel = new JPanel( new FlowLayout( FlowLayout.RIGHT ) );
            buttonPanel.add( m_close );
            add( buttonPanel );
        }

        JButton getDefaultButton()
        {
            return m_close;
        }
    }

    private class CancelAction extends AbstractAction
    {
        public CancelAction( String label )
        {
            super( label );
        }

       /**
        * Called when the cancel button is trigged.
        * @param event the action event
        */
        public void actionPerformed( ActionEvent event )
        {
            dispose();
            System.exit(0);
        }
    }

   /**
    * Empty boarder offset.
    */
    private static final int OFFSET = 10;
}
