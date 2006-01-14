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

import java.net.URI;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.prefs.Preferences;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;

import com.jgoodies.looks.FontSizeHints;
import com.jgoodies.looks.LookUtils;
import com.jgoodies.looks.Options;

import net.dpml.profile.model.ApplicationRegistry;

import net.dpml.transit.Transit;
import net.dpml.transit.Repository;
import net.dpml.transit.Logger;

/**
 * Dialog that presents the default preferences for DPML applications including
 * Transit cache and repository settings, transit content handler plugins, logging
 * preferences and application profiles runnable via the depot command line script.
 */
public class DepotPreferencesFrame extends JFrame 
{
   /**
    * The default dialog width.
    */
    private static final int DEFAULT_DIALOG_WIDTH = 550;
 
   /**
    * The default dialog height.
    */
    private static final int DEFAULT_DIALOG_HEIGHT = 700;

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

    public DepotPreferencesFrame( String[] args, Preferences prefs, Logger logger ) throws Exception
    {
        super();

        configureUI();

        /*
        boolean metal = isFlagPresent( args, "-metal" );
        if( false == metal )
        {
            String os = System.getProperty( "os.name" ).toLowerCase();
            if( ( os.indexOf( "win" ) >= 0 ) || ( os.indexOf( "Mac OS" ) >= 0 ) )
            {
                String classname = UIManager.getSystemLookAndFeelClassName();
                UIManager.setLookAndFeel( classname );
            }
        }
        */

        Repository repository = Transit.getInstance().getRepository();
        ClassLoader classloader = DepotPreferencesFrame.class.getClassLoader();
        URI uri = new URI( DEPOT_PROFILE_URI );
        ApplicationRegistry depot = (ApplicationRegistry) repository.getPlugin( classloader, uri, new Object[]{ prefs, logger } );

        //DepotHome store = new DepotStorageUnit( prefs );
        //ApplicationRegistry depot = new DefaultDepotProfile( logger, store );

        setTitle( "DPML ApplicationRegistry" );
        Dimension size = new Dimension( DEFAULT_DIALOG_WIDTH, DEFAULT_DIALOG_HEIGHT );
        setSize( size );
        DepotPreferencesPanel panel = new DepotPreferencesPanel( this, depot );
        setContentPane( panel );
        getRootPane().setDefaultButton( panel.getDefaultButton() );
        addWindowListener( 
          new WindowAdapter() 
          {
	    	  public void windowClosing(WindowEvent e) 
              {
                  System.exit(0);
		  }
          } 
        );
        setLocation( 300, 200 );
        setVisible(true);
    }

    private void configureUI()
    {

        UIManager.put( Options.USE_SYSTEM_FONTS_APP_KEY, Boolean.TRUE );
        Options.setGlobalFontSizeHints( FontSizeHints.MIXED );
        Options.setDefaultIconSize( new Dimension( 18, 18 ) );

        String lafName =
            LookUtils.IS_OS_WINDOWS_XP
                ? Options.getCrossPlatformLookAndFeelClassName()
                : Options.getSystemLookAndFeelClassName();

        com.jgoodies.looks.plastic.PlasticLookAndFeel.setMyCurrentTheme( new com.jgoodies.looks.plastic.theme.SkyBlue() );
        try 
        {
            UIManager.setLookAndFeel( lafName );
        } 
        catch( Exception e )
        {
            System.err.println( "Can't set look & feel:" + e );
        }
    }

    private static boolean isFlagPresent( String[] args, String flag )
    {
        for( int i=0; i < args.length; i++ )
        {
            String arg = args[i];
            if( arg.equals( flag ) )
            {
                return true;
            }
        }
        return false;
    }

    private static final String DEPOT_PROFILE_URI = "@DEPOT-PROFILE-PLUGIN-URI@";

}
