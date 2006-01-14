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
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.net.ContentHandler;
import java.net.PasswordAuthentication;
import java.net.URISyntaxException;
import java.net.URI;
import java.net.URL;
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
import javax.swing.JTextField;
import javax.swing.ImageIcon;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import net.dpml.transit.model.CacheModel;

/**
 * Control panel for editing the cache preferences.
 *
 * @author <a href="mailto:mcconnell@osm.net">OSM</a>
 */
class CacheDirectoryPanel extends ClassicPanel implements PropertyChangeListener, DocumentListener
{
    //--------------------------------------------------------------
    // state
    //--------------------------------------------------------------

    private final JDialog m_parent;
    private final CacheModel m_model;
    private JTextField m_base;
    private JButton m_ok;
    private JButton m_revert;
    private PropertyChangeSupport m_propertyChangeSupport;
    private String m_cache;
    private JButton m_close;

    //--------------------------------------------------------------
    // constructor
    //--------------------------------------------------------------

   /**
    * Creation of a panel containing the name of an exclude host.
    * 
    * @param parent the parent dialog 
    */
    public CacheDirectoryPanel( JDialog parent, CacheModel model ) throws Exception 
    {
        m_parent = parent;
        m_model = model;

        m_cache = model.getCacheDirectoryPath();
        m_propertyChangeSupport = new PropertyChangeSupport( this );
        m_ok = new JButton( new OKAction( "OK" ) );
        m_revert = new JButton( new RevertAction( "Undo" ) );
        m_close = new JButton( new CloseAction( "Close" ) );

        // add a text field containing the host url

        {
            JLabel label = 
              IconHelper.createImageIconJLabel( 
                getClass().getClassLoader(), FOLDER_IMG_PATH, 
                "Cache", "Cache Settings." ); 

            label.setBorder( new EmptyBorder( 0, 5, 0, 0 ) );
            JPanel padding = new JPanel();
            padding.setLayout( new BorderLayout() );
            padding.setBorder( new EmptyBorder( 10, 2, 5, 2) );
            m_base = new JTextField( m_cache );
            m_base.getDocument().addDocumentListener( this ); // listen for changes
            padding.add( m_base );

            getHeader().addStackedEntry( label, "Cache Directory", padding );
        }

        // add revert, ok, and cancel

        {
            JPanel panel = new JPanel();
            panel.setLayout( new FlowLayout( FlowLayout.RIGHT ) );
            panel.add( m_revert );
            panel.add( m_ok );
            panel.add( m_close );
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
        PropertyChangeEvent e = 
          new PropertyChangeEvent( 
            m_base, "cache", null, m_base.getText() );
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
        String base = m_base.getText();

        boolean flag = ( false == m_cache.equals( base ) );
        m_ok.setEnabled( flag );
        m_revert.setEnabled( flag );

        if( flag )
        {
            getRootPane().setDefaultButton( m_ok );
        }
        else
        {
            getRootPane().setDefaultButton( m_close );
        }
    }

    public JButton getDefaultButton()
    {
        String base = m_base.getText();
        boolean flag = ( false == m_cache.equals( base ) );
        if( flag )
        {
            return m_ok;
        }
        else
        {
            return m_close;
        }
    }

    //--------------------------------------------------------------
    // utilities
    //--------------------------------------------------------------

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
            String old = m_base.getText();
            m_base.setText( m_cache );
            PropertyChangeEvent e = 
              new PropertyChangeEvent( this, "undo", null, null );
            m_propertyChangeSupport.firePropertyChange( e );
        }
    }

    private class OKAction extends AbstractAction
    {
        OKAction( String name )
        {
            super( name );
            setEnabled( false );
        }

        public void actionPerformed( ActionEvent event )
        {
            String text = m_base.getText();
            if( false == text.equals( m_cache ) )
            {
                try
                {
                    if( "".equals( text ) )
                    {
                        m_model.setCacheDirectoryPath( null );
                    }
                    else
                    {
                        m_model.setCacheDirectoryPath( text );
                    }
                }
                catch( RemoteException e )
                {
                    final String error = 
                      "Unexpected remote exception setting cache directory path.";
                    throw new RuntimeException( error, e );
                }
            }
            m_parent.hide();
        }
    }

    //--------------------------------------------------------------
    // static utils
    //--------------------------------------------------------------

    private static EmptyBorder border5 = new EmptyBorder( 5, 5, 5, 5);
    private static String FOLDER_IMG_PATH = "net/dpml/depot/prefs/images/folder.png";
}


