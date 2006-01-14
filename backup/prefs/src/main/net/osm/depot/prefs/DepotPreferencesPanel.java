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
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.logging.Logger;
import java.util.logging.Level;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;

import net.dpml.transit.Transit;
import net.dpml.transit.model.TransitModel;
import net.dpml.transit.model.TransitRegistryModel;
import net.dpml.transit.model.DefaultTransitRegistryModel;
import net.dpml.transit.model.LayoutRegistryModel;
import net.dpml.transit.model.CacheModel;
import net.dpml.transit.model.ContentRegistryModel;
import net.dpml.transit.model.ProxyModel;
import net.dpml.transit.monitor.LoggingAdapter;
import net.dpml.transit.store.TransitStorageHome;

import net.dpml.profile.model.ApplicationRegistry;

/**
 * Panel that presents the default preferences for DPML applications including
 * Transit cache and repository settings, transit content handler plugins, logging
 * preferences and application profiles runnable via the depot command line script.
 */
class DepotPreferencesPanel extends JPanel 
{
   /**
    * The DPML icon.
    */
    private static String DPML_ICON_FILENAME = "net/dpml/depot/prefs/images/dpml.gif";

   /**
    * Empty boarder offset.
    */
    private static final int OFFSET = 10;

   /**
    * Empty boarder offset.
    */
    private static final int LEAD = 20;

   /**
    * Null offset
    */
    private static final int ZERO = 0;

    private ButtonPanel m_buttons;

    private TransitRegistryModel m_home;
    private LayoutRegistryModelPanel m_layoutPanel;
    private CacheModelPanel m_cachePanel;
    private ContentRegistryModelPanel m_registryPanel;
    private ProxyModelPanel m_proxyPanel;
    //private ApplicationsRegistryPanel m_depotPanel;
    private TransitHomePanel m_transitPanel;

    private final Window m_window;
    private final ApplicationRegistry m_depot;

    public DepotPreferencesPanel( 
      Window window, final ApplicationRegistry depot ) throws Exception
    {
        super( new BorderLayout() );

        m_window = window;
        m_depot = depot;

        net.dpml.transit.Logger logger = new LoggingAdapter( "depot.prefs" );
        TransitStorageHome home = new TransitStorageHome();
        m_home = new DefaultTransitRegistryModel( logger, home );

        JLabel label = 
          IconHelper.createImageIconJLabel( 
            getClass().getClassLoader(), DPML_ICON_FILENAME, "", "DPML Depot" ); 
        label.setBorder( new EmptyBorder( 5, 0, 0, 0 ) );

        final JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setBorder( new EmptyBorder( 0, OFFSET, OFFSET, OFFSET ) );

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
                   m_transitPanel = new TransitHomePanel( m_window, m_home );
                   tabbedPane.addTab( "Transit", m_transitPanel );
               }
               catch( Throwable e )
               {
                    final String error =
                      "Unexpected during transit home panel establishment.";
                   Logger logger = Logger.getLogger( "depot.prefs" );
                   logger.log( Level.SEVERE, error, e );
               }
            }
          }
        );

        /*
        SwingUtilities.invokeLater( 
          new Runnable() {
            public void run()
            {
               try
               {
                   m_depotPanel = new ApplicationsRegistryPanel( m_window, m_depot );
                   tabbedPane.addTab( "Applications", m_depotPanel );
               }
               catch( Throwable e )
               {
                    final String error =
                      "Unexpected during depot panel establishment.";
                   Logger logger = Logger.getLogger( "depot.prefs" );
                   logger.log( Level.SEVERE, error, e );
               }
            }
          }
        );
        */

        //
        // add the Close button
        //

        m_buttons = new ButtonPanel( this );
        m_buttons.setBorder( new EmptyBorder( 0, 7, 7, 7 ) );

        //
        // package
        //
        
        add( label, BorderLayout.NORTH );
        add( tabbedPane, BorderLayout.CENTER );
        add( m_buttons, BorderLayout.SOUTH );
    }

    public void dispose()
    {
    }

    public JButton getDefaultButton()
    {
        return m_buttons.getDefaultButton();
    }

    private class ButtonPanel extends Box
    {
        private JButton m_close;

        ButtonPanel( DepotPreferencesPanel panel )
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
}
