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

import javax.swing.AbstractAction;
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

import net.dpml.transit.model.TransitModel;
import net.dpml.transit.model.TransitRegistryModel;
import net.dpml.transit.model.DuplicateKeyException;
import net.dpml.transit.model.UnknownKeyException;

/**
 * Runnable plugin that handles DPML environment setup.
 *
 * @author <a href="mailto:mcconnell@osm.net">OSM</a>
 */
class TransitHomePanel extends ClassicPanel implements ListSelectionListener
{
    //--------------------------------------------------------------
    // state
    //--------------------------------------------------------------

    private final Window m_parent;

    private TransitRegistryModel m_model;
    private TransitListModel m_data;
    private JList m_list;
    private TransitModel m_selection;
    private JSplitPane m_splitPane;

    //--------------------------------------------------------------
    // constructor
    //--------------------------------------------------------------

    public TransitHomePanel( Window parent, TransitRegistryModel model ) throws Exception
    {
        super();

        m_model = model;
        m_parent = parent;

        m_data = new TransitListModel( model );
        m_list = new JList( m_data );
        m_list.setCellRenderer( new TransitCellRenderer() );
        m_splitPane = new JSplitPane();
        JScrollPane scroller = new JScrollPane();
        scroller.getViewport().setBackground( Color.WHITE );
        scroller.setViewportView( m_list );
        m_splitPane.setLeftComponent( scroller );
        if( model.getTransitModelCount() > 0 )
        {
            TransitModel profile = model.getTransitModels()[0];
            TransitPanel panel = new TransitPanel( m_parent, profile );
            m_list.setSelectedIndex( 0 );
            m_splitPane.setRightComponent( panel );
        }
        m_splitPane.setDividerLocation( 100 );
        m_list.getSelectionModel().addListSelectionListener( this );
        getBody().add( m_splitPane );
    }

    public void dispose()
    {
        m_list.getSelectionModel().removeListSelectionListener( this );
        m_data.dispose();
    }

    //--------------------------------------------------------------------------
    // ListSelectionListener
    //--------------------------------------------------------------------------

   /**
    * Listens to changes in the selected state of the table and 
    * propergates a <code>ContextEvent</code> referencing this table as 
    * the event's panel when the table selection changes.
    * @param event a list selection event
    */
    public void valueChanged( ListSelectionEvent event )
    {
        if( !event.getValueIsAdjusting() ) 
        {
            ListSelectionModel model = m_list.getSelectionModel();
            synchronized( model )
            {
                Object old = m_selection;
                m_selection = (TransitModel) m_list.getSelectedValue();
                if( null != m_selection )
                {
                    try
                    {
                        TransitPanel panel = 
                          new TransitPanel( m_parent, m_selection );
                        m_splitPane.setRightComponent( panel );
                        m_splitPane.setDividerLocation( 100 );
                    }
                    catch( Throwable e )
                    {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    //--------------------------------------------------------------
    // utilities
    //--------------------------------------------------------------

    private class TransitCellRenderer extends JLabel implements ListCellRenderer
    {
        public Component getListCellRendererComponent(
          JList list, Object object, int index, boolean selected, boolean focus )
        {
            try
            {
                TransitModel profile = (TransitModel) object;
                setIcon( MISC_ICON );
                setText( profile.getID() );
                setHorizontalAlignment( SwingConstants.CENTER );
                setVerticalTextPosition( JLabel.BOTTOM );
                setHorizontalTextPosition( JLabel.CENTER );
                setBorder( border );
                if( selected )
                {
                    if( focus )
                    {
                        setBackground( ENABLED_COLOR );
                    }
                    else
                    {
                        setBackground( DISABLED_COLOR );
                    }
                }
                return this;
            }
            catch( Throwable e )
            {
                e.printStackTrace();
                return this;
            }
        }
    }

    private static String MISC_IMG_PATH = "net/dpml/depot/prefs/images/process.png";
    private ImageIcon MISC_ICON = 
      IconHelper.createImageIcon( TransitHomePanel.class.getClassLoader(), MISC_IMG_PATH, "" );
    static EmptyBorder border = new EmptyBorder( 5, 5, 5, 5);
    static final Color ENABLED_COLOR = new Color( 204, 204, 255 );
    static final Color DISABLED_COLOR = new Color( 228, 228, 255 );
}
