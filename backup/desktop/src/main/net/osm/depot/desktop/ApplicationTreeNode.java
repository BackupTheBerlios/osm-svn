/*
 * Copyright 2005 Stephen McConnell
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.dpml.depot.desktop;

import java.awt.Component;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;
import java.net.URI;
import java.beans.PropertyChangeEvent;
import java.lang.reflect.Constructor;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.LinkedList;
import java.util.Enumeration;

import javax.swing.Icon;
import javax.swing.border.EmptyBorder;
import javax.swing.JLabel;
import javax.swing.SwingConstants;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JToggleButton;
import javax.swing.JTextField;
import javax.swing.JPanel;
import javax.swing.JComboBox;
import javax.swing.JSpinner;
import javax.swing.JOptionPane;
import javax.swing.JFrame;
import javax.swing.JTree;
import javax.swing.JTabbedPane;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.AbstractAction;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.tree.TreeNode;
import javax.swing.tree.DefaultMutableTreeNode;

import net.dpml.station.Application;
import net.dpml.station.ApplicationEvent;
import net.dpml.station.ApplicationListener;
import net.dpml.station.Application.State;

import net.dpml.part.PartHandler;

import net.dpml.profile.info.StartupPolicy;
import net.dpml.profile.model.ApplicationRegistry;
import net.dpml.profile.model.ApplicationProfile;

import net.dpml.transit.Logger;
import net.dpml.transit.Artifact;
import net.dpml.transit.Transit;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;

/**
 * Application profile tree node. 
 */
public final class ApplicationTreeNode extends Node
{
    private static final ImageIcon ICON = readImageIcon( "16/application.png" );

    public static final Icon DPML_DESKTOP_LEAF_ICON = ICON;
    public static final Icon DPML_DESKTOP_EXPANDED_ICON = ICON;
    public static final Icon DPML_DESKTOP_COLLAPSED_ICON = ICON;

    private static final Object[] POLICY_OPTIONS = 
      new Object[]
      {
        ApplicationProfile.DISABLED,
        ApplicationProfile.MANUAL,
        ApplicationProfile.AUTOMATIC,
      };

    private final Logger m_logger;
    private final Application m_application;
    private final ApplicationProfile m_profile;
    private final String m_name;
    private final String m_title;
    private final Component m_component;
    private final PolicyComboBox m_policy;

    private final StartAction m_start = new StartAction();
    private final StopAction m_stop = new StopAction();
    private final RestartAction m_restart = new RestartAction();

    private LinkedList m_changes = new LinkedList();

    public ApplicationTreeNode( Logger logger, PartHandler handler, Application application ) throws Exception
    {
        super( application );
        
        m_logger = logger;
        m_application = application;
        m_profile = application.getProfile();
        m_policy = new PolicyComboBox();
        m_title = m_profile.getTitle();
        m_name = resolveName( m_profile.getID() );

        m_application.addApplicationListener( new RemoteApplicationListener() );

        m_component = buildComponent( handler );
        setupControlButtons();
    }

    public String getName()
    {
        return m_name;
    }

    public String toString()
    {
        return getName();
    }

    Component getComponent()
    {
        return m_component;
    }

    private String resolveName( String id )
    {
        int n = id.lastIndexOf( "/" );
        return id.substring( n+1 ); 
    }

   /**
    * Construct a JTabbedPane containing the available views of the application.
    */
    private Component buildComponent( PartHandler handler ) throws Exception
    {
        JTabbedPane panel = new JTabbedPane();
        URI uri = m_profile.getCodeBaseURI();
        Artifact artifact = Artifact.createArtifact( uri );
        String type = artifact.getType();
        
        //
        // TODO: update the process component start/stop/restart action handlers 
        // for consistency with transit plugins and stardard 'main' based execution.
        //
        
        panel.add( "Process", buildProcessComponent() );
        panel.add( "Properties", new SystemPropertiesBuilder( m_profile ).getComponent() );
        
        //
        // add specific views based on the codebase
        //
        
        if( "plugin".equals( type ) )
        {
            //
            // TODO: construct the panels presenting a Transit plugin using
            // the repository to load the Plugin descriptor
            //
            
            JPanel pluginPanel = new JPanel();
            pluginPanel.setName( "Plugin" );
            panel.add( pluginPanel );
            return panel;
        }
        else if( "part".equals( type ) )
        {
            //
            // get the set of panels containing the available part views
            //
            
            PartHelper helper = new PartHelper( m_logger, handler, m_application );
            Component[] components = helper.getPartPanels();
            for( int i=0; i<components.length; i++ )
            {
                panel.add( components[i] );
            }
            
            //
            // get any child nodes exposed by the part
            //
            
            TreeNode[] nodes = helper.getPartNodes();
            for( int i=0; i<nodes.length; i++ )
            {
                TreeNode node = nodes[i];
                if( node instanceof DefaultMutableTreeNode )
                {
                    add( (DefaultMutableTreeNode) node );
                }
            }
            
            //
            // TODO: get declared actions and populate menus
            //
            
            return panel;
        }
        else
        {
            return panel;
        }
    }

    private Component buildProcessComponent() throws Exception
    {
        FormLayout layout = new FormLayout(
          "right:pref, 3dlu, 60dlu, fill:max(60dlu;pref), 7dlu, pref", 
          "pref, 3dlu, pref, 3dlu, pref, 3dlu, pref, 3dlu, pref, 3dlu, pref, 3dlu, pref, 3dlu, pref, 3dlu, pref, 3dlu, pref, 3dlu, pref, 3dlu, pref" );

        PanelBuilder builder = new PanelBuilder( layout );
        builder.setDefaultDialogBorder();

        CellConstraints cc = new CellConstraints();

        builder.addSeparator( "Configuration",             cc.xyw( 1, 1, 6 ) );

        builder.addLabel( "ID:",                            cc.xy( 1, 3 ) ); 
        builder.addLabel( getProfileID() ,                 cc.xyw( 3, 3, 4 ) );

        builder.addLabel( "Title:",                         cc.xy( 1, 5 ) ); 
        builder.add( getTitleComponent(),                  cc.xyw( 3, 5, 4 ) );

        builder.addLabel( "Codebase:",                      cc.xy( 1, 7 ) ); 
        builder.add( getCodebaseComponent(),               cc.xyw( 3, 7, 4 ) );

        builder.addLabel( "Base Directory:",                cc.xy( 1, 9 ) ); 
        builder.add( getBasedirComponent(),                cc.xyw( 3, 9, 2 ) );
        builder.add( new JButton( "chooser" ),              cc.xy( 6, 9 ) );

        builder.addLabel( "Startup Policy",                 cc.xy( 1, 11 ) ); 
        builder.add( getStartupPolicyComponent(),          cc.xyw( 3, 11, 1 ) ); 

        builder.addSeparator( "Timeouts",                  cc.xyw( 1, 13, 6 ) );

        builder.addLabel( "Startup",                        cc.xy( 1, 15 ) ); 
        builder.add( getStartupTimeoutComponent(),         cc.xyw( 3, 15, 1 ) ); 

        builder.addLabel( "Shutdown",                       cc.xy( 1, 17 ) ); 
        builder.add( getShutdownTimeoutComponent(),        cc.xyw( 3, 17, 1 ) ); 

        builder.addSeparator( "Process",                   cc.xyw( 1, 19, 6 ) );

        builder.add( getProcessComponent(),                cc.xyw( 3, 21, 4 ) ); 

        return builder.getPanel();
    }

    private Component getProcessComponent() throws Exception
    {
        JPanel buttons = new JPanel( new FlowLayout( FlowLayout.LEFT ) ); 
        buttons.setBorder( new EmptyBorder( 0, 0, 0, 0 ) );

        buttons.add( new JButton( m_start ) );
        buttons.add( new JButton( m_stop ) );
        buttons.add( new JButton( m_restart ) );

        return buttons;
    }

    private String getProfileID() throws Exception
    {
        return m_profile.getID();
    }

    private Component getTitleComponent() throws Exception
    {
        return new TitleDocument();
    }

    private Component getStartupTimeoutComponent() throws Exception
    {
        StartupTimeoutSpinnerModel model = new StartupTimeoutSpinnerModel();
        return new JSpinner( model );
    }

    private Component getShutdownTimeoutComponent() throws Exception
    {
        ShutdownTimeoutSpinnerModel model = new ShutdownTimeoutSpinnerModel();
        return new JSpinner( model );
    }

    private Component getCodebaseComponent() throws Exception
    {
        return new CodeBaseDocument();
    }

    private Component getBasedirComponent() throws Exception
    {
        return new JTextField( m_profile.getWorkingDirectoryPath() );
    }

    private Component getStartupPolicyComponent()
    {
        return m_policy;
    }

    private Component getPluginClassNameLabel( URI uri ) throws Exception
    {
        ClassLoader anchor = getClass().getClassLoader();
        Class c = Transit.getInstance().getRepository().getPluginClass( anchor, uri );
        return new JLabel( c.getName() );
    }

    public boolean isModified()
    {
        return m_changes.size() > 0;
    }

    public void apply() throws Exception
    {
        Volotile[] components = (Volotile[]) m_changes.toArray( new Volotile[0] );
        for( int i=0; i < components.length; i++ )
        {
            Volotile component = components[i];
            component.apply();
        }
    }

    public void revert() throws Exception
    {
        Volotile[] components = (Volotile[]) m_changes.toArray( new Volotile[0] );
        for( int i=0; i < components.length; i++ )
        {
            Volotile component = components[i];
            component.revert();
        }
    }

   /**
    * If the supplied component is modified we add it to the list of modified 
    * controls otherwise the control if removed from the list.  When a request
    * to apply or revert changes is received we grap all of the modified 
    * componenets and invoke the respective apply/revert operation.
    */
    private void updateChangeList( Volotile component, boolean modified )
    {
        boolean originalState = isModified();
        m_changes.remove( component );
        if( modified )
        {
            m_changes.add( component );
        }
        boolean newState = isModified();
        if( originalState != newState )
        {
            PropertyChangeEvent e = 
              new PropertyChangeEvent( 
                this, "modified", new Boolean( originalState ), new Boolean( newState ) );
            firePropertyChange( e );
        }
    }

    private void setupControlButtons()
    {
        try
        {
            State state = m_application.getState();
            setupControlButtons( state );
        }
        catch( Throwable e )
        {
            // ignore
        }
    }

    private void setupControlButtons( State state )
    {
        if( Application.READY.equals( state ) )
        {
            m_start.setEnabled( true );
            m_stop.setEnabled( false );
            m_restart.setEnabled( false );
            
            //int n = getChildCount();
            //for( int i=0; i<n; i++ )
            //{
            //    Node node = (Node) getLastChild();
            //    m_desktop.getTreeModel().removeNodeFromParent( node );
            //}
        }
        else if( Application.STARTING.equals( state ) )
        {
            m_start.setEnabled( false );
            m_stop.setEnabled( false );
            m_restart.setEnabled( false );
        }
        else if( Application.RUNNING.equals( state ) )
        {
            m_start.setEnabled( false );
            m_stop.setEnabled( true );
            m_restart.setEnabled( true );
            //setupComponentNodes();
        }
        else if( Application.STOPPING.equals( state ) )
        {
            m_start.setEnabled( false );
            m_stop.setEnabled( false );
            m_restart.setEnabled( false );
        }
    }

    /*
    private void setupComponentNodes()
    {
        try
        {
            net.dpml.component.runtime.Component component = m_application.getComponent();
            if( component instanceof Container )
            {
                Container container = (Container) component;
                net.dpml.component.runtime.Component[] components = container.getComponents();
                for( int i=0; i<components.length; i++ )
                {
                    net.dpml.component.runtime.Component c = components[i];
                    ComponentTreeNode node = new ComponentTreeNode( c );
                    m_desktop.getTreeModel().insertNodeInto( node, this, i );
                }
            }
        }
        catch( Throwable e )
        {
            e.printStackTrace();
        }
    }
    */

    private JFrame getParentFrame( Component component ) 
    {
        return (JFrame) SwingUtilities.getWindowAncestor( component );
    }

    //--------------------------------------------------------------------
    // startup policy components
    //--------------------------------------------------------------------

   /**
    * The PolicyComboBox is a volotile component that maintains the 
    * presentation of the application profile startup policy. 
    */
    private class PolicyComboBox extends JComboBox implements Volotile
    {
        private PolicyComboBox() throws Exception
        {
            super( POLICY_OPTIONS );
            setSelectedItem( m_profile.getStartupPolicy() );
        }

        public void selectedItemChanged()
        {
            super.selectedItemChanged();
            updateChangeList( this, isModified() );
        }

       /**
        * Returns TRUE if the selected value is not the same as the application
        * profile startup policy value.
        * @return the modified status of the component
        */
        public boolean isModified()
        {
            try
            {
                return !m_profile.getStartupPolicy().equals( super.getSelectedItem() );
            }
            catch( Exception e )
            {
                return false;
            }
        }

       /**
        * Apply changes such that the application profile statup policy
        * is synchronized with the component value.
        * @return true if the update was successfull
        */
        public void apply() throws Exception
        {
            StartupPolicy policy = (StartupPolicy) super.getSelectedItem();
            m_profile.setStartupPolicy( policy );
            updateChangeList( this, false );
        }

       /**
        * Revert changes such that the component reflects the status of the 
        * application profile statup policy.
        * @return true if the reversion was successfull
        */
        public void revert() throws Exception
        {
            super.setSelectedItem( m_profile.getStartupPolicy() );
            updateChangeList( this, false );
        }
    }

    //--------------------------------------------------------------------
    // timeout components
    //--------------------------------------------------------------------

    private abstract class TimeoutSpinnerModel extends SpinnerNumberModel implements Volotile
    {
        TimeoutSpinnerModel()
        {
            super();
        }

        public void setValue( Object value )
        {
            super.setValue( value );
            updateChangeList( this, true );
        }

        abstract Integer getTimeout();
        abstract void setTimeout( Integer value );

        public boolean isModified()
        {
            try
            {
                Object value = getTimeout();
                return value.equals( getValue() );
            }
            catch( Throwable e )
            {
                return false;
            }
        }

        public void apply() throws Exception
        {
            Integer value = (Integer) getValue();
            setTimeout( value );
            updateChangeList( this, false );
        }

        public void revert() throws Exception
        {
            Integer value = getTimeout();
            setValue( value );
            updateChangeList( this, false );
        }
    }

    private class StartupTimeoutSpinnerModel extends TimeoutSpinnerModel
    {
        StartupTimeoutSpinnerModel() throws Exception
        {
            super();
            revert();
        }

        Integer getTimeout()
        {
            try
            {
                return new Integer( m_profile.getStartupTimeout() );
            }
            catch( Throwable e )
            {
                e.printStackTrace();
                return new Integer( 0 );
            }
        }

        void setTimeout( Integer value )
        {
            try
            {
                int n = value.intValue();
                m_profile.setStartupTimeout( n );
            }
            catch( Throwable e )
            {
                e.printStackTrace();
            }
        }
    }

    private class ShutdownTimeoutSpinnerModel extends TimeoutSpinnerModel
    {
        ShutdownTimeoutSpinnerModel() throws Exception
        {
            super();
            revert();
        }

        Integer getTimeout()
        {
            try
            {
                return new Integer( m_profile.getShutdownTimeout() );
            }
            catch( Throwable e )
            {
                e.printStackTrace();
                return new Integer( 0 );
            }
        }

        void setTimeout( Integer value )
        {
            try
            {
                int n = value.intValue();
                m_profile.setShutdownTimeout( n );
            }
            catch( Throwable e )
            {
                // ignore
            }
        }
    }

    //--------------------------------------------------------------------
    // field components
    //--------------------------------------------------------------------

    private abstract class VolotileDocument extends JTextField implements DocumentListener, Volotile
    {
        public VolotileDocument( String text )
        {
            super( text );
            getDocument().addDocumentListener( this );
        }

        public void insertUpdate( DocumentEvent event )
        {
            setModified();
        }

        public void removeUpdate( DocumentEvent event )
        {
            setModified();
        }

        public void changedUpdate( DocumentEvent event )
        {
            setModified();
        }

        private void setModified()
        {
            updateChangeList( this, true );
        }

        public abstract boolean isModified();
        public abstract void apply() throws Exception;
        public abstract void revert() throws Exception;

    }

    private class TitleDocument extends VolotileDocument 
    {
        public TitleDocument() throws Exception
        {
            super( m_profile.getTitle() );
        }

        public boolean isModified()
        {
            try
            {
                String text = getText();
                String title = m_profile.getTitle();
                return text.equals( title );
            }
            catch( Exception e )
            {
                return false;
            }
        }

        public void apply() throws Exception
        {
            String text = getText();
            m_profile.setTitle( text );
            updateChangeList( this, false );
        }

        public void revert() throws Exception
        {
            String text = m_profile.getTitle();
            setText( text );
            updateChangeList( this, false );
        }
    }

    private class CodeBaseDocument extends VolotileDocument 
    {
        public CodeBaseDocument() throws Exception
        {
            super( m_profile.getCodeBaseURI().toString() );
        }

        public boolean isModified()
        {
            try
            {
                String text = getText();
                String codebase = m_profile.getCodeBaseURI().toString();
                return text.equals( codebase );
            }
            catch( Exception e )
            {
                return false;
            }
        }

        public void apply() throws Exception
        {
            String text = getText();
            m_profile.setCodeBaseURI( new URI( text ) );
            updateChangeList( this, false );
        }

        public void revert() throws Exception
        {
            String text = m_profile.getCodeBaseURI().toString();
            setText( text );
            updateChangeList( this, false );
        }
    }

    private class RemoteApplicationListener extends UnicastRemoteObject implements ApplicationListener
    {
        private Thread m_thread;

        private RemoteApplicationListener() throws RemoteException
        {
            super();
        }

        public void applicationStateChanged( final ApplicationEvent event ) throws RemoteException
        {
            SwingUtilities.invokeLater( 
              new Runnable() 
              {
                public void run()
                {
                   try
                   {
                       State state = event.getState();
                       setupControlButtons( state );
                   }
                   catch( Throwable e )
                   {
                       e.printStackTrace();
                   }
                }
              }
            );
        }
    }

    private class StartAction extends AbstractAction
    {
        private StartAction()
        {
            putValue( NAME, "Start" );
            //putValue( SMALL_ICON, readImageIcon( "start-process.png" ) );
            putValue( SHORT_DESCRIPTION, "Starts a new process." );
        }

        public void actionPerformed( final ActionEvent event ) 
        {
            Thread thread = 
              new Thread()
              {
                  public void run()
                  {
                      try
                      {
                          m_application.start();
                      }
                      catch( Exception e ) 
                      {
                          final String message = 
                            "An process startup error occured."
                            + "\n" + e.getClass().getName()
                            + "\n" + e.getMessage();
                          JOptionPane.showMessageDialog(
                            getParentFrame( (Component) event.getSource() ), 
                            message, "Error", JOptionPane.ERROR_MESSAGE );
                      }
                  }
              };
            thread.start();
        }
    }

    private class StopAction extends AbstractAction
    {
        private StopAction()
        {
            putValue( NAME, "Stop" );
            //putValue( SMALL_ICON, readImageIcon( "stop-process.png" ) );
            putValue( SHORT_DESCRIPTION, "Terminates the process." );
        }

        public void actionPerformed( final ActionEvent event ) 
        {
            Thread thread = 
              new Thread()
              {
                  public void run()
                  {
                      try
                      {
                          m_application.stop();
                      }
                      catch( Exception e ) 
                      {
                          final String message = 
                            "A process termination error occured."
                            + "\n" + e.getClass().getName()
                            + "\n" + e.getMessage();
                          JOptionPane.showMessageDialog(
                            getParentFrame( (Component) event.getSource() ), 
                            message, "Error", JOptionPane.ERROR_MESSAGE );
                      }
                  }
              };
            thread.start();
        }
    }

    private class RestartAction extends AbstractAction
    {
        private RestartAction()
        {
            putValue( NAME, "Restart" );
            putValue( 
              SHORT_DESCRIPTION, 
              "Terminates the current process and starts a new replacement process." );
        }

        public void actionPerformed( final ActionEvent event ) 
        {
            Thread thread = 
              new Thread()
              {
                  public void run()
                  {
                      try
                      {
                          m_application.restart();
                      }
                      catch( Exception e ) 
                      {
                          final String message = 
                            "A process restart error occured."
                            + "\n" + e.getClass().getName()
                            + "\n" + e.getMessage();
                          JOptionPane.showMessageDialog(
                            getParentFrame( (Component) event.getSource() ), 
                            message, "Error", JOptionPane.ERROR_MESSAGE );
                      }
                  }
              };
            thread.start();
        }
    }
}