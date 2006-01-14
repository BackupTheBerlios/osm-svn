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
import java.net.PasswordAuthentication;

import javax.swing.AbstractAction;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JPasswordField;
import javax.swing.ImageIcon;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

/**
 * Control panel for editing the cache preferences.
 *
 * @author <a href="mailto:mcconnell@osm.net">OSM</a>
 */
class ClassicCredentialsPanel extends ClassicPanel implements PasswordAuthenticationModel
{
    //--------------------------------------------------------------
    // state
    //--------------------------------------------------------------

    private final JDialog m_parent;
    private JTextField m_username;
    private JTextField m_password;
    private JButton m_ok;
    private JButton m_revert;
    private PropertyChangeSupport m_propertyChangeSupport;
    private PasswordAuthentication m_authentication;
    private PasswordAuthentication m_auth;
    private PasswordAuthenticationListener m_listener;
    private boolean m_modified = false;

    //--------------------------------------------------------------
    // constructor
    //--------------------------------------------------------------

   /**
    * Creation of a panel containing the name of an exclude host.
    * 
    * @param parent the parent dialog 
    */
    public ClassicCredentialsPanel( 
      JDialog parent, PasswordAuthentication authentication, PasswordAuthentication auth,
      PasswordAuthenticationListener listener ) throws Exception 
    {
        m_parent = parent;
        m_authentication = authentication;
        m_auth = auth;
        m_listener = listener;

        m_propertyChangeSupport = new PropertyChangeSupport( this );
        m_ok = new JButton( new OKAction( "OK", this ) );
        m_revert = new JButton( new RevertAction( "Undo" ) );

        // add a text field containing the host url

        {
            JLabel label = 
              IconHelper.createImageIconJLabel( 
                getClass().getClassLoader(), PASSWORD_IMG_PATH, 
                "Credentials", "Username and password settings." );
            label.setBorder( new EmptyBorder( 0, 10, 0, 0 ) );

            JPanel rack = new JPanel();
            rack.setLayout( new BoxLayout( rack, BoxLayout.Y_AXIS ) );
            m_username = new JTextField( getUserNameValue() );
            m_username.getDocument().addDocumentListener( new UsernameDocumentListener() );
            rack.add( new LabelHolder( "Username:" ) );
            rack.add( m_username );

            m_password = new JPasswordField( new String( getPasswordValue() ) );
            m_password.getDocument().addDocumentListener( new PasswordDocumentListener() );
            rack.add( new LabelHolder( "Password:" ) );
            rack.add( m_password );

            JPanel padding = new JPanel();
            padding.setLayout( new BorderLayout() );
            padding.setBorder( new EmptyBorder( 10, 2, 5, 2) );
            padding.add( rack, BorderLayout.NORTH );

            getHeader().addStackedEntry( label, "Credentials", padding );
        }

        // add controls

        {
            JPanel panel = new JPanel();
            panel.setLayout( new FlowLayout( FlowLayout.RIGHT ) );
            panel.add( m_revert );
            panel.add( m_ok );
            panel.add( new JButton( new CloseAction( "Close" ) ) );
            panel.setBorder( new EmptyBorder( 10, 10, 5, 0 ) );
            add( panel, BorderLayout.SOUTH );
        }

        m_propertyChangeSupport.addPropertyChangeListener( new CredentialsListener() );
    }

    private class LabelHolder extends JPanel
    {
        public LabelHolder( String text )
        {
            super( new BorderLayout() );
            setBorder( new EmptyBorder( 5, 0, 5, 0 ) );
            JLabel label = new JLabel( text );
            add( label, BorderLayout.WEST );
        }
    }

    private String getUserNameValue()
    {
        if( null == m_auth )
        {
            return "";
        }
        else
        {
            return m_authentication.getUserName();
        }
    }

    private String getPasswordValue()
    {
        if( null == m_auth )
        {
            return "";
        }
        else
        {
            return new String( m_authentication.getPassword() );
        }
    }


    //--------------------------------------------------------------
    // PasswordAuthenticationModel
    //--------------------------------------------------------------

   /**
    * Return the password authentication instance.
    * @return the authentication credentials
    */
    public PasswordAuthentication getAuthentication()
    {
        return m_auth;
    }

    //--------------------------------------------------------------
    // utilities
    //--------------------------------------------------------------

    private abstract class AbstractDocumentListener implements DocumentListener
    {
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

        protected abstract void fireBaseChangedEvent();
    }

    private class UsernameDocumentListener extends AbstractDocumentListener 
    {
        protected void fireBaseChangedEvent()
        {
            PropertyChangeEvent e = 
              new PropertyChangeEvent( 
                m_username, "username", null, m_username.getText() );
            m_propertyChangeSupport.firePropertyChange( e );
        }
    }

    private class PasswordDocumentListener extends AbstractDocumentListener 
    {
        protected void fireBaseChangedEvent()
        {
            PropertyChangeEvent e = 
              new PropertyChangeEvent( 
                m_password, "password", null, m_password.getText() );
            m_propertyChangeSupport.firePropertyChange( e );
        }
    }

    private class CredentialsListener implements PropertyChangeListener
    {
        public void propertyChange( PropertyChangeEvent event )
        {
            boolean modified = isModified();
            m_ok.setEnabled( modified );
            m_revert.setEnabled( modified );
            m_modified = modified;
        }

        private boolean isModified()
        {
            if( isPasswordModified() )
            {
                return true;
            }
            else if( isUsernameModified() )
            {
                return true;
            }
            else
            {
                return false;
            }
        }

        private boolean isPasswordModified()
        {
            String password = getPasswordValue();
            return !m_password.getText().equals( password );
        }

        private boolean isUsernameModified()
        {
            String username = getUserNameValue();
            return !m_username.getText().equals( username );
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
            String username = getUserNameValue();
            String old = m_username.getText();
            m_username.setText( username );
            PropertyChangeEvent usernameEvent = 
              new PropertyChangeEvent( this, "username", old, username );
            m_propertyChangeSupport.firePropertyChange( usernameEvent );
            m_auth = m_authentication;
            String password = new String( getPasswordValue() );
            m_password.setText( password );
            PropertyChangeEvent passwordEvent = 
              new PropertyChangeEvent( this, "password", null, null );
            m_propertyChangeSupport.firePropertyChange( passwordEvent );
        }
    }

    private class OKAction extends AbstractAction
    {
        private PasswordAuthenticationModel m_model;

        OKAction( String name, PasswordAuthenticationModel model )
        {
            super( name );
            setEnabled( false );
            m_model = model;
        }

        public void actionPerformed( ActionEvent event )
        {
            if( m_modified )
            {
                String username = m_username.getText();
                String password = m_password.getText();
                char[] pswd = password.toCharArray();
                PasswordAuthentication auth = new PasswordAuthentication( username, pswd );
                m_auth = auth;
                try
                {
                    PasswordAuthenticationEvent e =
                      new PasswordAuthenticationEvent( m_model, auth );
                    m_listener.passwordAuthenticationChanged( e );
                }
                catch( Throwable e )
                {
                    e.printStackTrace();
                }
            }
            m_parent.hide();
        }
    }

    //--------------------------------------------------------------
    // static utils
    //--------------------------------------------------------------

    private static EmptyBorder border5 = new EmptyBorder( 5, 5, 5, 5);
    private static String SERVER_IMG_PATH = "net/dpml/depot/prefs/images/server.png";
    private static String PASSWORD_IMG_PATH = "net/dpml/depot/prefs/images/password.png";
}

