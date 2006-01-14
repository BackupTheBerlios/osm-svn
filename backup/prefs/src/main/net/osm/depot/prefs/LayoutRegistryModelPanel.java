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
import net.dpml.transit.Layout;
import net.dpml.transit.LayoutRegistry;
import net.dpml.transit.model.LayoutRegistryModel;
import net.dpml.transit.model.LayoutModel;
import net.dpml.transit.model.DuplicateKeyException;
import net.dpml.transit.model.UnknownKeyException;

/**
 * Runnable plugin that handles DPML environment setup.
 *
 * @author <a href="mailto:mcconnell@osm.net">OSM</a>
 */
class LayoutRegistryModelPanel extends ClassicPanel implements PropertyChangeListener
{
    //--------------------------------------------------------------
    // state
    //--------------------------------------------------------------

    private final Window m_parent;

    private ClassicTable m_table;
    private EditAction m_editAction;
    private JButton m_edit;
    private JButton m_delete;
    private String m_selection;

    private LayoutRegistryModel m_model;
    private LayoutRegistryTableModel m_tableModel;

    //--------------------------------------------------------------
    // constructor
    //--------------------------------------------------------------

    public LayoutRegistryModelPanel( Window parent, LayoutRegistryModel manager ) throws Exception
    {
        super();

        m_model = manager;
        m_parent = parent;

        m_editAction = new EditAction( "Edit" );
        m_edit = new JButton( m_editAction );
        m_delete = new JButton( new DeleteAction( "Delete" ) );

        JLabel label = 
          IconHelper.createImageIconJLabel( 
            getClass().getClassLoader(), MISC_IMG_PATH, 
            "Artifact", "Layout Registry Controller settings." ); 
        label.setBorder( new EmptyBorder( 0, 5, 0, 0 ) );
        ControllerAction controller = new ControllerAction( "Controller" );
        getHeader().addEntry( label, "Layout Registry Controller", new JButton( controller ) );

        JPanel panel = new JPanel();
	  panel.setLayout( new BorderLayout() );
        TitledBorder tb = 
          new TitledBorder( 
            new EmptyBorder( 0,0,0,0 ), "Layout Strategies", TitledBorder.LEFT, TitledBorder.TOP );
        panel.setBorder( new CompoundBorder( tb, border5 ) );
        getBody().add( panel );

        TableColumnModel columns = createResolverHandlerColumnModel();
        m_tableModel = new LayoutRegistryTableModel( manager );
        m_table = new ClassicTable( m_tableModel, columns );
        m_table.addPropertyChangeListener( this );
        m_table.setShowVerticalLines( false );
        m_table.setShowHorizontalLines( false );

        JButton[] buttons = new JButton[ 3 ];
        buttons[0] = new JButton( new AddAction( "Add" ) );
        buttons[1] = m_edit;
        buttons[2] = m_delete;
        getBody().addScrollingEntry( m_table, "Repository Layouts", buttons );
    }

    public void dispose()
    {
        m_table.removePropertyChangeListener( this );
        m_tableModel.dispose();
    }

    //--------------------------------------------------------------
    // PropertyChangelistener
    //--------------------------------------------------------------

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
                    LayoutModel resolver = m_model.getLayoutModel( m_selection );
                    m_edit.setEnabled( true );
                    if( false == resolver.isBootstrap() )
                    {
                        m_delete.setEnabled( true );
                    }
                    getRootPane().setDefaultButton( m_edit );
                }
                catch( UnknownKeyException e )
                {
                    m_edit.setEnabled( false );
                    m_delete.setEnabled( false );
                }
                catch( Exception e )
                {
                    final String error = 
                      "unexpected remote exception while attempting to resolve selected layout.";
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
            m_editAction.editSelection( m_edit );
        }
    }

    //--------------------------------------------------------------
    // utilities
    //--------------------------------------------------------------

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
                final String title = "Layout Registry Controller";
                final Dimension size = new Dimension( 400, 240 );
                ClassicDialog dialog = ClassicDialog.createDialog( m_parent, title, size );
                PluginModelPanel panel = 
                  new PluginModelPanel( dialog, m_model, LayoutRegistry.class );
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
    private static TableColumnModel createResolverHandlerColumnModel()
    {
        TableColumnModel model = new DefaultTableColumnModel();

	  TableColumn layout = new TableColumn( 0, 250, new ClassicCellRenderer(), null );
        layout.setHeaderValue( "Layout" );
	  model.addColumn( layout );

	  return model;
    }

    private class AddAction extends EditAction
    {
        public AddAction( String name )
        {
             super( name );
             setEnabled( true );
        }

        public void actionPerformed( ActionEvent event )
        {
            String id = JOptionPane.showInputDialog( m_parent, "Resolver Identifier:" );
            if( null == id )
            {
                return;
            }
            try
            {
                m_model.addLayoutModel( id );
            }
            catch( Exception e )
            {
                final String error = 
                  "Unexpected error while attempting to add a new layout model."
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
            editSelection( (Component) event.getSource() );
        }

        public void editSelection( Component source )
        {
            try
            {
                final String title = "Location Resolver";
                final Dimension size = new Dimension( 400, 240 );
                ClassicDialog dialog = ClassicDialog.createDialog( m_parent, title, size );
                LayoutModel manager = m_model.getLayoutModel( m_selection );
                if( null != manager )
                {
                    PluginModelPanel panel = 
                      new PluginModelPanel( dialog, manager, Layout.class );
                    dialog.getBody().add( panel );
                    dialog.setLocationRelativeTo( source );
                    dialog.setResizable( false );
                    dialog.setVisible(true);
                }
            }
            catch( Throwable e )
            {
                final String error = 
                  "Unexpected error while attempting to construct plugin dialog."
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
                LayoutModel layout = m_model.getLayoutModel( m_selection );
                m_model.removeLayoutModel( layout );
            }
            catch( Throwable e )
            {
                final String error = 
                  "Unexpected error while attempting to delete a layout model."
                  + "\nCODEBASE: "
                  + getClass().getProtectionDomain().getCodeSource().getLocation().toString();
                Logger logger = Logger.getLogger( "depot.prefs" );
                logger.log( Level.SEVERE, error, e );
            }
        }
    }

    private static String MISC_IMG_PATH = "net/dpml/depot/prefs/images/settings.png";
}
