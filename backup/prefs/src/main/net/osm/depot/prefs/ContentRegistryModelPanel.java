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
import net.dpml.transit.ContentRegistry;
import net.dpml.transit.model.ContentRegistryModel;
import net.dpml.transit.model.ContentModel;
import net.dpml.transit.model.UnknownKeyException;
import net.dpml.transit.model.DuplicateKeyException;

/**
 * Runnable plugin that handles DPML environment setup.
 *
 * @author <a href="mailto:mcconnell@osm.net">OSM</a>
 */
class ContentRegistryModelPanel extends ClassicPanel implements PropertyChangeListener
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

    private ContentRegistryModel m_model;
    private ContentRegistryTableModel m_tableModel;

    //--------------------------------------------------------------
    // constructor
    //--------------------------------------------------------------

    public ContentRegistryModelPanel( Window parent, ContentRegistryModel model )
    {
        super();

        m_model = model;
        m_parent = parent;

        m_editAction = new EditAction( "Edit" );
        m_edit = new JButton( m_editAction );
        m_delete = new JButton( new DeleteAction( "Delete" ) );

        JLabel label = 
          IconHelper.createImageIconJLabel( 
            getClass().getClassLoader(), MISC_IMG_PATH, 
            "Artifact", "Content Handler Controller settings." ); 
        label.setBorder( new EmptyBorder( 0, 5, 0, 0 ) );
        ControllerAction controller = new ControllerAction( "Controller" );
        getHeader().addEntry( label, "Content Management Controller", new JButton( controller ) );

        JPanel panel = new JPanel();
	  panel.setLayout( new BorderLayout() );
        TitledBorder tb = 
          new TitledBorder( 
            new EmptyBorder( 0,0,0,0 ), "Content Handlers", TitledBorder.LEFT, TitledBorder.TOP );
        panel.setBorder( new CompoundBorder( tb, border5 ) );
        getBody().add( panel );

        TableColumnModel columns = createContentHandlerColumnModel();
        m_tableModel = new ContentRegistryTableModel( model );
        m_table = new ClassicTable( m_tableModel, columns );
        m_table.addPropertyChangeListener( this );
        m_table.setShowVerticalLines( false );
        m_table.setShowHorizontalLines( false );

        JButton[] buttons = new JButton[ 3 ];
        buttons[0] = new JButton( new AddAction( "Add" ) );
        buttons[1] = m_edit;
        buttons[2] = m_delete;
        getBody().addScrollingEntry( m_table, "Content Handlers", buttons );
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
                m_edit.setEnabled( true );
                m_delete.setEnabled( true );
                getRootPane().setDefaultButton( m_edit );
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
                final String title = "Content Handler Controller";
                final Dimension size = new Dimension( 400, 240 );
                ClassicDialog dialog = ClassicDialog.createDialog( m_parent, title, size );
                PluginModelPanel panel = 
                  new PluginModelPanel( dialog, m_model, ContentRegistry.class );
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
    private static TableColumnModel createContentHandlerColumnModel()
    {
        TableColumnModel model = new DefaultTableColumnModel();

	  TableColumn type = new TableColumn( 0, 30, new ClassicCellRenderer(), null );
        type.setHeaderValue( "Type" );
	  model.addColumn( type );

	  TableColumn codebase = new TableColumn( 1, 250, new ClassicCellRenderer(), null );
        codebase.setHeaderValue( "Codebase" );
	  model.addColumn( codebase );

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
            String type = JOptionPane.showInputDialog( m_parent, "Content Type:" );
            if( null == type )
            {
                return;
            }
            try
            {
                m_model.addContentModel( type );
            }
            catch( DuplicateKeyException e )
            {
                final String error = 
                  "Unexpected error while attempting to add a content type."
                  + "\nCODEBASE: "
                  + getClass().getProtectionDomain().getCodeSource().getLocation().toString();
                Logger logger = Logger.getLogger( "depot.prefs" );
                logger.log( Level.SEVERE, error, e );
            }
            catch( RemoteException e )
            {
                final String error = 
                  "Unexpected remote error while attempting to add a content type."
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
                final String title = "Content Handler";
                final Dimension size = new Dimension( 400, 240 );
                ClassicDialog dialog = ClassicDialog.createDialog( m_parent, title, size );
                ContentModel model = m_model.getContentModel( m_selection );
                if( null != model )
                {
                    PluginModelPanel panel = 
                      new PluginModelPanel( dialog, model, ContentHandler.class );
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
                ContentModel m = m_model.getContentModel( m_selection );
                m_model.removeContentModel( m );
            }
            catch( Throwable e )
            {
                final String error = 
                  "Unexpected error while attempting to delete a content handler plugin preferences node."
                  + "\nCODEBASE: "
                  + getClass().getProtectionDomain().getCodeSource().getLocation().toString();
                Logger logger = Logger.getLogger( "depot.prefs" );
                logger.log( Level.SEVERE, error, e );
            }
        }
    }

    private static String MISC_IMG_PATH = "net/dpml/depot/prefs/images/settings.png";
}
