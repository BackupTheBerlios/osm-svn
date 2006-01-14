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
import java.awt.Dialog;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.net.ContentHandler;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

import javax.swing.Action;
import javax.swing.AbstractAction;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import net.dpml.transit.Transit;
import net.dpml.transit.TransitException;
import net.dpml.transit.model.CodeBaseModel;
import net.dpml.transit.model.CodeBaseListener;
import net.dpml.transit.model.CodeBaseEvent;
import net.dpml.transit.model.ParametersEvent;
import net.dpml.transit.model.LocationEvent;
import net.dpml.transit.Plugin;
import net.dpml.transit.Repository;

/**
 * Control panel for editing the cache preferences.
 *
 * @author <a href="mailto:mcconnell@osm.net">OSM</a>
 */
class PluginModelPanel extends ClassicPanel 
  implements PropertyChangeListener, DocumentListener
{
    //--------------------------------------------------------------
    // state
    //--------------------------------------------------------------

    private final JDialog m_parent;
    private final RemoteListener m_listener;
    private final CodeBaseModel m_manager;
    private final PropertyChangeSupport m_propertyChangeSupport;
    private final JTextField m_base;
    private final JButton m_ok;
    private final JButton m_revert;
    private final SettingsAction m_settings;
    private final Action m_action;

    //--------------------------------------------------------------
    // constructor
    //--------------------------------------------------------------

   /**
    * Creation of a standard plugin setup panel containing 
    * a user modifiable plugin artifact uri and a controller 
    * parameters button. If a class named [classname]ControlPanel
    * exists (where [classname] is the plugin classname), the control
    * panel class will be used to construct a dialog for the selected
    * plugin.  If a class named [classname]Installer exists, the 
    * installer will be instantiated as part of plugin dialog 
    * establishment.
    * 
    * @param parent the parent dialog 
    * @param manager the plugin manager
    * @param service the class that a plugin controller must implement
    */
    public PluginModelPanel( 
      JDialog parent, CodeBaseModel manager, Class service ) throws Exception 
    {
        this( parent, manager, null, service );
    }

   /**
    * Creation of a standard plugin setup panel containing 
    * a user modifiable plugin artifact uri and a controller 
    * parameters button. If a class named [classname]ControlPanel
    * exists (where [classname] is the plugin classname), the control
    * panel class will be used to construct a dialog for the selected
    * plugin.  If a class named [classname]Installer exists, the 
    * installer will be instantiated as part of plugin dialog 
    * establishment.
    * 
    * @param parent the parent dialog 
    * @param manager the plugin manager
    * @param action a possibly null action object to be assigned to the controller button
    *    when not plugin uri is specificed
    * @param service the class that a plugin controller must implement
    */
    public PluginModelPanel( 
      JDialog parent, CodeBaseModel manager, Action action, Class service ) throws Exception 
    {
        super();

        m_parent = parent;
        m_propertyChangeSupport = new PropertyChangeSupport( this );
        m_listener = new RemoteListener();
        m_manager = manager;
        m_action = action;

        m_ok = new JButton( new OKAction( "Install", service ) );
        m_revert = new JButton( new RevertAction( "Undo" ) );
        m_manager.addCodeBaseListener( m_listener );

        setLayout( new BorderLayout() );

        // create a label containing an icon and label

        {
            JLabel label = 
              IconHelper.createImageIconJLabel( 
                getClass().getClassLoader(), SOURCE_IMG_PATH, "URI", "Plugin URI" ); 
            label.setBorder( new EmptyBorder( 0, 5, 0, 0 ) );
            JPanel panel = new JPanel();
            panel.setLayout( new BorderLayout() );
            panel.add( label, BorderLayout.WEST );
            add( panel, BorderLayout.NORTH );
        }

        // add a test field containing the plugin artifact uri and a 
        // button for optional plugin configuration

        {
            JPanel holder = new JPanel();
            holder.setLayout( new BorderLayout() );

            JPanel panel = new JPanel();
            panel.setLayout( new BoxLayout( panel, BoxLayout.Y_AXIS ) );
            panel.setBorder( new EmptyBorder( 10, 10, 10, 10 ) );
            {
                // add the text field

                JPanel padding = new JPanel();
                padding.setLayout( new BorderLayout() );
                padding.setBorder( new EmptyBorder( 10, 2, 5, 2) );
                String artifact = getControllerFieldText();
                m_base = new JTextField( artifact );
                m_base.getDocument().addDocumentListener( this ); // listen for changes
                padding.add( m_base );
                panel.add( padding );
            }
            {
                // add the optional controller settings button

                JPanel settingsPanel = new JPanel();
                settingsPanel.setBorder( 
                  new CompoundBorder(
                    new TitledBorder( 
                      null, "Controller Settings", TitledBorder.LEFT, TitledBorder.TOP), 
                    new EmptyBorder( 5, 5, 5, 10) 
                  )
                );
                settingsPanel.setLayout( new BorderLayout() );
                m_settings = new SettingsAction( "Controller Settings", m_parent );
                JButton settings = new JButton( m_settings );
                settingsPanel.add( settings, BorderLayout.EAST );
                panel.add( settingsPanel );
            }
            holder.add( panel, BorderLayout.NORTH );
            add( holder, BorderLayout.CENTER );
        }

        // add revert, ok, and cancel

        {
            JPanel panel = new JPanel();
            panel.setLayout( new FlowLayout( FlowLayout.RIGHT ) );
            panel.add( m_revert );
            panel.add( m_ok );
            panel.add( new JButton( new CloseAction( "Close" ) ) );
            panel.setBorder( new EmptyBorder( 10, 10, 5, 0 ) );
            add( panel, BorderLayout.SOUTH );
        }

        m_propertyChangeSupport.addPropertyChangeListener( this );
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
        String artifact = getControllerFieldText();
        PropertyChangeEvent e = 
          new PropertyChangeEvent( 
            m_base, "base", null, m_base.getText() );
        m_propertyChangeSupport.firePropertyChange( e );
    }

    //--------------------------------------------------------------
    // PropertyChangeListener
    //--------------------------------------------------------------

   /**
    * The actions dealing with changes to the dialog raise change events that 
    * are captured here.  This listener checks changes and enables or disabled 
    * the ok and undo buttons based on the state of controls relative to the 
    * underlying preferences for the cache.
    */
    public void propertyChange( PropertyChangeEvent event )
    {
        String artifact = getControllerFieldText();
        boolean flag = ( false == artifact.equals( m_base.getText() ) );
        m_ok.setEnabled( flag );
        m_revert.setEnabled( flag );
        if( !flag )
        {
            m_settings.setup();
        }
        else
        {
            m_settings.setEnabled( null != m_action );
        }
    }

    private String getControllerFieldText()
    {
        try
        {
            URI uri = m_manager.getCodeBaseURI();
            if( null == uri )
            {
                return "artifact:plugin:";
            }
            else
            {
                return uri.toString();
            }
        }
        catch( RemoteException e )
        {
            final String error = 
              "Unexpected remote exception while resolving plugin uri.";
            throw new RuntimeException( error, e );
        }
    }

    private String getControllerPath()
    {
        try
        {
            URI uri = m_manager.getCodeBaseURI();
            if( null == uri )
            {
                return null;
            }
            else
            {
                return uri.toString();
            }
        }
        catch( RemoteException e )
        {
            final String error = 
              "Unexpected remote exception while resolving plugin uri.";
            throw new RuntimeException( error, e );
        }
    }

    //--------------------------------------------------------------
    // CodeBaseListener
    //--------------------------------------------------------------

    private class RemoteListener extends UnicastRemoteObject implements CodeBaseListener
    {
        public RemoteListener() throws RemoteException
        {
            super();
        }

        public void codeBaseChanged( LocationEvent event ) throws RemoteException
        {
            URI uri = event.getCodeBaseURI();
            String artifact = "";
            if( uri != null )
            {
                artifact = uri.toString();
            }
            if( false == m_base.getText().equals( artifact ) )
            {
                m_base.setText( artifact );
            }
        }

        public void parametersChanged( ParametersEvent event ) throws RemoteException
        {
            // TODO
        }
    }

    //--------------------------------------------------------------
    // utils
    //--------------------------------------------------------------

    private void dispose()
    {
        try
        {
            m_manager.removeCodeBaseListener( m_listener );
        }
        catch( Throwable e )
        {
        }
    }

    private class CloseAction extends AbstractAction
    {
         CloseAction( String name )
         {
             super( name );
         }

         public void actionPerformed( ActionEvent event )
         {
             m_parent.hide();
             dispose();
         }
     }

    private class SettingsAction extends AbstractAction 
    {
        private Dialog m_parent;
        private Dialog m_dialog;

        SettingsAction( String name, Dialog parent )
        {
            super( name );
            m_parent = parent;
            setup();
        }

        public void setup()
        {
            String controllerPath = getControllerPath();
            if( null == controllerPath )
            {
                setEnabled( m_action != null );
            }
            else
            {
                try
                {
                    URI uri = new URI( controllerPath );
                    String classname = getControllerClassname( uri );
                    String controlClassname = classname + CONTROL_PANEL_SUFFIX;
                    ClassLoader classloader = getControllerClassLoader( uri );
                    m_dialog = createControlPanel( classloader, controlClassname, m_parent );
                    m_dialog.setModal( true );
                    setEnabled( true );
                }
                catch( ClassNotFoundException e )
                {
                    setEnabled( false );
                }
                catch( Throwable e )
                {
                    System.err.println( e.toString() );
                    e.printStackTrace();
                    setEnabled( false );
                }
            }
        }

        public void actionPerformed( ActionEvent event )
        {
            if( m_dialog != null )
            {
                m_dialog.setLocationRelativeTo( (Component) event.getSource() );
                m_dialog.setVisible( true );
            }
            else
            {
                if( null != m_action )
                {
                    m_action.actionPerformed( event );
                }
            }
        }

        private String getControllerClassname( URI uri ) throws Exception
        {
            URL url = new URL( uri.toASCIIString() );
            Plugin plugin = (Plugin) url.getContent( new Class[]{ Plugin.class } );
            return plugin.getClassname();
        }

        private ClassLoader getControllerClassLoader( URI uri ) throws Exception
        {
            URL url = new URL( uri.toString() );
            return (ClassLoader) url.getContent( new Class[]{ ClassLoader.class } );
        }

        private Dialog createControlPanel( ClassLoader classloader, String classname, Dialog parent )
          throws ClassNotFoundException, TransitException
        {
            try
            {
                Repository repository = Transit.getInstance().getRepository();
                Class cls = classloader.loadClass( classname );
                Object[] args = new Object[]{ parent, m_manager };
                return (Dialog) repository.instantiate( cls, args );
            }
            catch( ClassNotFoundException e )
            {
                throw e;
            }
            catch( Throwable e )
            {
                String error =
                  "Unable to establish a control panel using the class ["
                  + classname
                  + "].";
                throw new TransitException( error, e );
            }
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
            String artifact = getControllerFieldText();
            String old = m_base.getText();
            m_base.setText( artifact );
            PropertyChangeEvent e = 
              new PropertyChangeEvent( this, "uri", old, artifact );
            m_propertyChangeSupport.firePropertyChange( e );
        }
    }

    private class OKAction extends AbstractAction
    {
        private Class m_service;

        OKAction( String name, Class service )
        {
            super( name );
            setEnabled( false );
            m_service = service;
        }

        public void actionPerformed( ActionEvent event )
        {
            String text = m_base.getText();
            String artifact = getControllerFieldText();
            if( false == text.equals( artifact ) )
            {
                try
                {
                    if( "".equals( text ) )
                    {
                        m_manager.setCodeBaseURI( null );
                    }
                    else
                    {
                        URI uri = validateControllerSpec( m_parent, text, m_service );
                        if( null != uri )
                        {
                            m_manager.setCodeBaseURI( uri );
                        }
                    }
                }
                catch( RemoteException e )
                {
                    final String error = 
                      "Unexpected remote exception while setting codebase uri.";
                    throw new RuntimeException( error, e );
                }
            }
            m_ok.setEnabled( false );
            m_revert.setEnabled( false );
            m_settings.setup();
        }
    }

    private URI validateControllerSpec( Component component, String spec, Class service )
    {
        URI uri = createControllerURI( component, spec );
        if( null == uri )
        {
            return null;
        }
        URL url = getURLFromURI( component, uri );
        if( null == url )
        {
            return null;
        }
        Plugin plugin = getPluginFromURL( component, url );
        if( null == plugin )
        {
            return null;
        }
        String classname = plugin.getClassname();
        if( false == checkPluginClass( component, url, service ) )
        {
            return null;
        }
        return uri;
    }

    private URI createControllerURI( Component component, String spec )
    {
        try
        {
            return new URI( spec );
        }
        catch( URISyntaxException e )
        {
            final String title = "Controller URI Error";
            final String message = "Invalid uri.";
            JOptionPane.showMessageDialog( component, message, title, JOptionPane.ERROR_MESSAGE );
            return null;
        }
    }

    private URL getURLFromURI( Component component, URI uri )
    {
        try
        {
            String spec = uri.toString();
            return new URL( spec );
        }
        catch( IOException e )
        {
            final String title = "Controller URI Error";
            final String message = "Cannot convert the uri to a url.";
            JOptionPane.showMessageDialog( component, message, title, JOptionPane.ERROR_MESSAGE );
            return null;
        }
    }

    private Plugin getPluginFromURL( Component component, URL url )
    {
        try
        {
            return (Plugin) url.getContent( new Class[]{ Plugin.class } );
        }
        catch( Throwable e )
        {
            final String title = "Controller URI Error";
            final String message = e.getMessage();
            JOptionPane.showMessageDialog( component, message, title, JOptionPane.ERROR_MESSAGE );
            return null;
        }
    }

    private boolean checkPluginClass( Component component, URL url, Class service )
    {
        Class c = null;
        try
        {
            c = (Class) url.getContent( new Class[]{ Class.class } );
        }
        catch( Throwable e )
        {
            final String title = "Controller URI Error";
            final String message = e.getMessage();
            e.printStackTrace();
            JOptionPane.showMessageDialog( component, message, title, JOptionPane.ERROR_MESSAGE );
            return false;
        }
        if( false == service.isAssignableFrom( c ) )
        {
            final String title = "Controller URI Error";
            final String message = "Plugin class does not implement " + service.getName() + ".";
            JOptionPane.showMessageDialog( component, message, title, JOptionPane.ERROR_MESSAGE );
            System.out.println( c.getClassLoader().toString() );            
            return false;
        }
        else
        {
            //
            // check for an installer class
            //

            String installer = c.getName() + "Installer";
            try
            {
                Class install = c.getClassLoader().loadClass( installer );
                install.newInstance();
                return true;
            }
            catch( ClassNotFoundException e )
            {
                return true;
            }
            catch( Throwable e )
            {
                final String title = "Controller URI Error";
                final String message = "Plugin class install error in " + installer + ".";
                JOptionPane.showMessageDialog( component, message, title, JOptionPane.ERROR_MESSAGE );
                return false;
            }
        }
    }

    //--------------------------------------------------------------
    // static utils
    //--------------------------------------------------------------

    private static EmptyBorder border5 = new EmptyBorder( 5, 5, 5, 5);

    private static String SOURCE_IMG_PATH = "net/dpml/depot/prefs/images/binary.png";

    private static final String CONTROL_PANEL_SUFFIX = "ControlPanel";
}

