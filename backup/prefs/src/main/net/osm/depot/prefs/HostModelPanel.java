/*
 * Copyright 2005 Stephen J. McConnell.
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
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.net.URL;
import java.net.PasswordAuthentication;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.AbstractAction;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JList;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListDataListener;
import javax.swing.event.ListDataEvent;
import javax.swing.plaf.basic.BasicComboBoxRenderer;

import net.dpml.transit.model.HostModel;
import net.dpml.transit.model.LayoutRegistryModel;
import net.dpml.transit.model.HostListener;
import net.dpml.transit.model.LayoutModel;
import net.dpml.transit.model.HostChangeEvent;
import net.dpml.transit.model.HostLayoutEvent;
import net.dpml.transit.model.HostPriorityEvent;
import net.dpml.transit.model.HostNameEvent;

/**
 * A interactive panel that presents the preferences for a single host.
 *
 * @author <a href="mailto:mcconnell@osm.net">OSM</a>
 */
class HostModelPanel extends ClassicPanel 
  implements PropertyChangeListener, DocumentListener, ListDataListener
{
    //--------------------------------------------------------------
    // static
    //--------------------------------------------------------------

    static EmptyBorder border5 = new EmptyBorder( 5, 5, 5, 5);

    //--------------------------------------------------------------
    // state
    //--------------------------------------------------------------

    private final JDialog m_parent;
    private final HostModel m_model;
    private final LayoutRegistryModel m_layouts;

    private JLabel m_label;
    private JCheckBox m_enabled;
    private JCheckBox m_trusted;
    private JTextField m_base;
    private JButton m_ok;
    private JButton m_revert;
    private LayoutComboBoxModel m_strategy;
    private CredentialsAction m_credentials;
    private PasswordAuthentication m_auth;

    private final PropertyChangeSupport m_propertyChangeSupport;
    private final RemoteHostListener m_listener;

    //--------------------------------------------------------------
    // constructor
    //--------------------------------------------------------------

    public HostModelPanel( JDialog parent, HostModel model, LayoutRegistryModel layouts ) throws Exception 
    {
        super();

        m_parent = parent;
        m_model = model;
        m_listener = new RemoteHostListener();
        m_layouts = layouts;
        m_auth = model.getAuthentication();

        m_model.addHostListener( m_listener );
        m_propertyChangeSupport = new PropertyChangeSupport( this );
        
        //
        // create the dialog label containing the host descriptor name
        //

        String name = model.getHostName();
        m_label = 
          IconHelper.createImageIconJLabel( 
            getClass().getClassLoader(), 
            "net/dpml/depot/prefs/images/server.png", "Host", "Host: " + name );

        m_label.setBorder( new EmptyBorder( 0, 5, 0, 10 ) );
        JPanel labelPanel = new JPanel();
        labelPanel.setLayout( new BorderLayout() );
        labelPanel.add( m_label, BorderLayout.WEST );
        labelPanel.setBorder( new EmptyBorder( 10, 6, 10, 6 ) );

        //
        // create a panel to hold things in the center of the dialog
        //

        JPanel panel = new JPanel();
	  panel.setLayout( new BoxLayout( panel, BoxLayout.Y_AXIS ) );

        //
        // create a box layout for the host base url and groups button
        // TODO: action support for groups
        //

        JPanel paths = new JPanel();
        panel.add( paths );
	  paths.setLayout( new BorderLayout() );
        paths.setBorder( 
          new CompoundBorder(
            new TitledBorder( null, "Base URL", TitledBorder.LEFT, TitledBorder.TOP), border5 ) );

        m_base = new JTextField( getBasePath() );
        m_base.getDocument().addDocumentListener( this ); // listen for changes
        JPanel basePanel = new JPanel();
        basePanel.setLayout( new BorderLayout() );
        basePanel.setBorder( new EmptyBorder( 0, 0, 0, 30 ) );
        basePanel.add( m_base, BorderLayout.CENTER );

        JButton knownGroups = new JButton( "Groups" ); 
        knownGroups.setEnabled( false );
        JPanel groupsPanel = new JPanel();
        groupsPanel.setLayout( new BorderLayout() );
        groupsPanel.add( knownGroups, BorderLayout.EAST );

        paths.add( basePanel, BorderLayout.CENTER  );
        paths.add( groupsPanel, BorderLayout.EAST );

        //
        // create a box layout to hold the layout strategy and the credentials
        //

        JPanel layoutPlusCredentials = new JPanel();
	  layoutPlusCredentials.setLayout( 
           new BoxLayout( layoutPlusCredentials, BoxLayout.X_AXIS ) );
        panel.add( layoutPlusCredentials );

        // resolver layout strategy

        JPanel layout = new JPanel();
	  layout.setLayout( new BorderLayout() );
        layout.setBorder( 
          new CompoundBorder(
            new TitledBorder( null, "Repository Layout", TitledBorder.LEFT, TitledBorder.TOP), 
            border5 ) );
        layoutPlusCredentials.add( layout );

        m_strategy = new LayoutComboBoxModel( m_layouts, m_model );
        m_strategy.addListDataListener( this );
        JComboBox strategy = new JComboBox( m_strategy );
        strategy.setRenderer( new LayoutRenderer() );
        layout.add( strategy, BorderLayout.SOUTH  );

        // sign-on credentials

        JPanel credentials = new JPanel();
	  credentials.setLayout( new BoxLayout( credentials, BoxLayout.X_AXIS ) );
        credentials.setBorder( 
          new CompoundBorder(
            new TitledBorder( null, "Credentials", TitledBorder.LEFT, TitledBorder.TOP), 
            border5 ) );
        ImageIcon keysIcon = IconHelper.createImageIcon( 
          getClass().getClassLoader(), 
          "net/dpml/depot/prefs/images/password.png", 
          "Credentials" );
        JLabel keys = new JLabel( keysIcon );
        m_credentials = new CredentialsAction( "Settings" );
        JButton settings = new JButton( m_credentials );
        JPanel settingsPanel = new JPanel();
        settingsPanel.setLayout( new BorderLayout() );
        settingsPanel.add( keys, BorderLayout.WEST );
        settingsPanel.add( settings, BorderLayout.EAST );
        credentials.add( settingsPanel );
        layoutPlusCredentials.add( credentials );

        //
        // create a box layout for the enabled and trusted checkboxs
        //

        JPanel holder = new JPanel();
	  holder.setLayout( new BoxLayout( holder, BoxLayout.Y_AXIS ) );
        holder.setBorder( 
          new CompoundBorder(
            new TitledBorder( null, "Parameters", TitledBorder.LEFT, TitledBorder.TOP), 
            border5 ) );
        panel.add( holder );

        // add enabled status

        m_enabled = new JCheckBox( new EnableAction( "Enabled" ) );
        m_enabled.setSelected( m_model.getEnabled() );

        JPanel enabledPanel = new JPanel();
        enabledPanel.setLayout( new BorderLayout() );
        enabledPanel.add( m_enabled, BorderLayout.WEST );
        enabledPanel.add( m_enabled );
        holder.add( enabledPanel );

        // add trusted status

        m_trusted = new JCheckBox( new TrustedAction( "Trusted" ) );
        m_trusted.setSelected( m_model.getTrusted() );

        JPanel trustedPanel = new JPanel();
        trustedPanel.setLayout( new BorderLayout() );
        trustedPanel.add( m_trusted, BorderLayout.WEST );
        trustedPanel.add( m_trusted );
        holder.add( trustedPanel );

        //
        // add commmit/revert controls and assemble the dialog
        //

        m_ok = new JButton( new OKAction( "OK" ) );
        m_revert = new JButton( new RevertAction( "Undo Changes" ) );
        JPanel buttonHolder = new JPanel();
        buttonHolder.setLayout( new FlowLayout( FlowLayout.RIGHT ) );
        buttonHolder.add( m_revert );
        buttonHolder.add( m_ok );
        buttonHolder.add( new JButton( new CloseAction( "Cancel" ) ) );
        buttonHolder.setBorder( new EmptyBorder( 10, 10, 5, 0 ) );

        JPanel thing = new JPanel();
        thing.setLayout( new BorderLayout() );
        thing.add( panel, BorderLayout.NORTH  );

        add( labelPanel, BorderLayout.NORTH );
        add( thing , BorderLayout.CENTER );
        add( buttonHolder, BorderLayout.SOUTH );

        m_propertyChangeSupport.addPropertyChangeListener( this );
    }

    public void dispose()
    {
        try
        {
            m_strategy.removeListDataListener( this );
            m_propertyChangeSupport.removePropertyChangeListener( this );
            m_base.getDocument().removeDocumentListener( this );
            m_model.removeHostListener( m_listener );
            m_strategy.dispose();
        }
        catch( Throwable e )
        {
        }
    }

    //--------------------------------------------------------------
    // HostListener
    //--------------------------------------------------------------

    private class LayoutRenderer extends BasicComboBoxRenderer
    {
        public Component getListCellRendererComponent(
          JList list, Object value, int index, boolean isSelected, boolean cellHasFocus)
        {
             if( value instanceof LayoutModel )
             {
                 try
                 {
                     LayoutModel model = (LayoutModel) value;
                     String title = "  " + model.getTitle();
                     return super.getListCellRendererComponent( 
                       list, title, index, isSelected, cellHasFocus );
                 }
                 catch( RemoteException e )
                 {
                     throw new RuntimeException( e.getMessage(), e.getCause() );
                 }
             }
             else
             {
                 return super.getListCellRendererComponent( list, value, index, isSelected, cellHasFocus );
             }
        }
    }
 
    private class RemoteHostListener extends UnicastRemoteObject implements HostListener
    {
        public RemoteHostListener() throws RemoteException
        {
            super();
        }

       /**
        * Notify a consumer of an aggregated set of changes.
        * @param event the host change event
        */
        public void hostChanged( HostChangeEvent event )
        {
        }

       /**
        * Notify a consumer of a change to the host priority.
        * @param event the host event
        */
        public void priorityChanged( HostPriorityEvent event )
        {
        }
    
       /**
        * Notify a consumer of a change to the host priority.
        * @param event the host event
        */
        public void layoutChanged( HostLayoutEvent event )
        {
        }

       /**
        * Notify a consumer of a change to the availability status.
        * @param event the host event
        */
        public void nameChanged( HostNameEvent event )
        {
        }
    }

    //--------------------------------------------------------------
    // PropertyChangeListener
    //--------------------------------------------------------------

   /**
    * The actions dealing with changes to the dialog raise change events that 
    * are captured here.  This listener checks changes and enables or disabled 
    * the ok and undo buttons based on the state of controls relative to the 
    * underlying preferences for this host.
    */
    public void propertyChange( PropertyChangeEvent event )
    {
        try
        {
            boolean flag = false;
            {
                boolean value = m_enabled.isSelected();
                if( m_model.getEnabled() != value )
                {
                    flag = true;
                }
            }
            {
                boolean value = m_trusted.isSelected();
                if( m_model.getTrusted() != value )
                {
                    flag = true;
                }
            }
            {
                String path = m_base.getText();
                if( false == path.equals( getBasePath()) )
                {
                    flag = true;
                }
            }
            {
                PasswordAuthentication auth = m_model.getAuthentication();
                if( null == m_auth )
                {
                    if( null != auth )
                    {
                        flag = true;
                    }
                }
                else if( false == m_auth.equals( auth ) )
                {
                    flag = true;
                }
            }
            {
                LayoutModel layout = (LayoutModel) m_strategy.getSelectedItem();
                if( null != layout )
                {
                    if( false == layout.equals( m_model.getLayoutModel() ) )
                    {
                        flag = true;
                    }
                }
            }
            m_ok.setEnabled( flag );
            m_revert.setEnabled( flag );
            try
            {
                new URL( m_base.getText() );
                m_enabled.setEnabled( true );
            }
            catch( Throwable e )
            {
                m_enabled.setSelected( m_model.getEnabled() );
            }
        }
        catch( RemoteException re )
        {
            final String error = 
              "Unexpected remote exception while interigating host model.";
            throw new RuntimeException( error, re );
        }
    }

    //--------------------------------------------------------------
    // DocumentListener
    //--------------------------------------------------------------

    public void insertUpdate( DocumentEvent event )
    {
        fireBaseChangedEvent();
    }

    public void removeUpdate( DocumentEvent event )
    {
        fireBaseChangedEvent();
    }

    public void changedUpdate( DocumentEvent event )
    {
        fireBaseChangedEvent();
    }

    private void fireBaseChangedEvent()
    {
        String path = getBasePath();
        PropertyChangeEvent e = 
          new PropertyChangeEvent( 
            m_base, "base", path, m_base.getText() );
        m_propertyChangeSupport.firePropertyChange( e );
    }

    //--------------------------------------------------------------
    // ListDataListener (listen for changes to the layout selection)
    //--------------------------------------------------------------

    /** 
     * Sent after the indices in the index0,index1 
     * interval have been inserted in the data model.
     * The new interval includes both index0 and index1.
     *
     * @param e  a <code>ListDataEvent</code> encapsulating the
     *    event information
     */
    public void intervalAdded(ListDataEvent e)
    {
    }
    
    /**
     * Sent after the indices in the index0,index1 interval
     * have been removed from the data model.  The interval 
     * includes both index0 and index1.
     *
     * @param e  a <code>ListDataEvent</code> encapsulating the
     *    event information
     */
    public void intervalRemoved(ListDataEvent e)
    {
    }

    /** 
     * Sent when the contents of the list has changed in a way 
     * that's too complex to characterize with the previous 
     * methods. For example, this is sent when an item has been
     * replaced. Index0 and index1 bracket the change.
     *
     * @param e  a <code>ListDataEvent</code> encapsulating the
     *    event information
     */
    public void contentsChanged( ListDataEvent e )
    {
        Object selection = m_strategy.getSelectedItem();
        if( selection instanceof LayoutModel )
        {
            try
            {
                LayoutModel current = m_model.getLayoutModel();
                LayoutModel model = (LayoutModel) selection;
                if( false == model.equals( current ) )
                {
                    PropertyChangeEvent event = 
                      new PropertyChangeEvent( 
                    this, "layout", current, model );
                    m_propertyChangeSupport.firePropertyChange( event );
                }
            }
            catch( RemoteException re )
            {
                final String error = 
                  "Remote exception ccured while interigating host model.";
                Logger logger = Logger.getLogger( "depot.prefs" );
                logger.log( Level.SEVERE, error, re );
            }
        }
    }

    //--------------------------------------------------------------
    // utility classes
    //--------------------------------------------------------------

    private class EnableAction extends AbstractAction
    {
        EnableAction( String name )
        {
            super( name );
        }

        public void actionPerformed( ActionEvent event )
        {
            boolean selected = m_enabled.isSelected();
            PropertyChangeEvent e = 
              new PropertyChangeEvent( 
                this, "enabled", new Boolean( !selected ), new Boolean( selected ) );
            m_propertyChangeSupport.firePropertyChange( e );
        }
    }

    private class TrustedAction extends AbstractAction
    {
        TrustedAction( String name )
        {
            super( name );
        }

        public void actionPerformed( ActionEvent event )
        {
            boolean selected = m_trusted.isSelected();
            PropertyChangeEvent e = 
              new PropertyChangeEvent( 
                this, "trusted", new Boolean( !selected ), new Boolean( selected ) );
            m_propertyChangeSupport.firePropertyChange( e );
        }
    }

   /**
    * Action that handles the 'Cancel' dialog button.
    */
    private class CloseAction extends AbstractAction
    {
         CloseAction( String name )
         {
             super( name );
         }

         public void actionPerformed( ActionEvent event )
         {
             m_parent.hide();
             m_ok.setEnabled( false );
             dispose();
         }
     }

     private class OKAction extends AbstractAction 
     {
        OKAction( String name )
        {
            super( name );
            setEnabled( false );
        }

        // TODO: include index changes

        public void actionPerformed( ActionEvent event )
        {
            String base = m_base.getText();
            boolean trusted = m_trusted.isSelected();
            boolean enabled = m_enabled.isSelected();
            LayoutModel layout = (LayoutModel) m_strategy.getSelectedItem();
            try
            {
                String key = layout.getID();
                m_model.update( base, null, enabled, trusted, key, m_auth, "", "" );
            }
            catch( Exception e )
            {
                final String error = 
                  "Unexpected error while attempting to update the host.";
                Logger logger = Logger.getLogger( "depot.prefs" );
                logger.log( Level.SEVERE, error, e );
            }
            m_parent.hide();
            dispose();
        }
    }

    private class CredentialsAction extends AbstractAction implements PasswordAuthenticationListener
    {
        private ClassicDialog m_credentialsDialog;

        CredentialsAction( String name )
        {
            super( name );
        }

        public void actionPerformed( ActionEvent event )
        {
            if( m_credentialsDialog != null )
            {
                m_credentialsDialog.setVisible( true );
                return;
            }
            try
            {
                final String title = "Credentials: " + m_model.getHostName();
                final Dimension size = new Dimension( 300, 280 );
                ClassicDialog dialog = ClassicDialog.createDialog( m_parent, title, size );
                ClassicCredentialsPanel panel = 
                  new ClassicCredentialsPanel( dialog, m_model.getAuthentication(), m_auth, this );
                dialog.getBody().add( panel );
                dialog.setLocationRelativeTo( (Component) event.getSource() );
                dialog.setResizable( false );
                dialog.setVisible( true );
                m_credentialsDialog = dialog;
            }
            catch( Throwable e )
            {
                final String error = 
                  "Unexpected error while attempting to handle credentials action. "
                  + "\nCODEBASE: "
                  + getClass().getProtectionDomain().getCodeSource().getLocation().toString();
                Logger logger = Logger.getLogger( "depot.prefs" );
                logger.log( Level.SEVERE, error, e );
            }
        }

        public void revert()
        {
            m_credentialsDialog = null;
        }

       /**
        * Notify a consumer of a change to the host priority.
        * @param event the host event
        */
        public void passwordAuthenticationChanged( PasswordAuthenticationEvent event )
        {
            m_auth = event.getPasswordAuthentication();
            PropertyChangeEvent e = 
              new PropertyChangeEvent( 
                this, "auth", null, m_auth );
                m_propertyChangeSupport.firePropertyChange( e );
        }
    }

    private URL resolveBaseURL( String path )
    {
        try
        {
            return new URL( path );
        }
        catch( Exception e )
        {
            final String error = 
              "Invalid URL value: " + path;
            throw new RuntimeException( error, e );
        }
    }

    private class RevertAction extends AbstractAction
    {
        RevertAction( String name )
        {
            super( name );
            setEnabled( false );
        }

        public void actionPerformed( ActionEvent event )
        {
            try
            {
                m_enabled.setSelected( m_model.getEnabled() );
                m_trusted.setSelected( m_model.getTrusted() );
                m_strategy.setSelectedItem( m_model.getLayoutModel() );
                m_base.setText( getBasePath() );
                m_auth = m_model.getAuthentication();
                m_credentials.revert();
                PropertyChangeEvent e = 
                  new PropertyChangeEvent( 
                    this, "revert", null, null );
                m_propertyChangeSupport.firePropertyChange( e );
            }
            catch( RemoteException re )
            {
                final String error = 
                  "Remote exception occured while updating host.";
                throw new RuntimeException( error, re );
            }
        }
    }

    private String getBasePath()
    {
        try
        {
            URL base = m_model.getBaseURL();
            if( null == base ) 
            {
                return "";
            }
            else
            {
                return base.toExternalForm();
            }
        }
        catch( RemoteException e )
        {
            final String error = 
              "Remote exception while reading base url.";
            throw new RuntimeException( error, e );
        }
    }
}
