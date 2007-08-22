/*
 * @(#)Desktop.java
 *
 * Copyright 2000 OSM SARL. All Rights Reserved.
 * 
 * This software is the proprietary information of OSM SARL.  
 * Use is subject to license terms.
 * 
 * @author  Stephen McConnell
 * @version 1.0 6/02/2001
 */

package net.osm.shell;

import java.io.File;
import java.io.IOException;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.io.InputStream;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.util.Set;
import java.util.Iterator;
import java.util.List;
import java.util.LinkedList;
import java.net.URL;
import java.net.URLConnection;
import java.awt.Font;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Container;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.JMenu;
import javax.swing.JProgressBar;
import javax.swing.JMenuItem;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.ButtonGroup;
import javax.swing.OverlayLayout;
import javax.swing.Action;
import javax.swing.KeyStroke;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JTextArea;
import javax.swing.JScrollPane;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.JTabbedPane;
import javax.swing.JOptionPane;
import javax.swing.JPasswordField;
import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;

import org.apache.avalon.framework.context.Context;
import org.apache.avalon.framework.context.Contextualizable;
import org.apache.avalon.framework.context.ContextException;
import org.apache.avalon.framework.component.ComponentManager;
import org.apache.avalon.framework.component.ComponentException;
import org.apache.avalon.framework.component.DefaultComponentManager;

import org.apache.avalon.framework.activity.Disposable;
import org.apache.avalon.framework.activity.Initializable;
import org.apache.avalon.framework.activity.Startable;
import org.apache.avalon.framework.logger.LogEnabled;
import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.phoenix.BlockContext;
import org.apache.avalon.phoenix.Block;
import org.apache.avalon.phoenix.BlockListener;
import org.apache.avalon.phoenix.BlockEvent;
import org.apache.avalon.framework.logger.Logger;
import org.apache.avalon.phoenix.metainfo.BlockInfo;

import java.security.Signature;
import java.security.PrivateKey;
import java.security.cert.CertPath;
import java.security.cert.X509Certificate;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.callback.TextOutputCallback;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.ConfirmationCallback;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;
import javax.security.auth.login.AccountExpiredException;
import javax.security.auth.login.CredentialExpiredException;
import javax.security.auth.login.FailedLoginException;
import javax.security.auth.x500.X500Principal;
import javax.security.auth.x500.X500PrivateCredential;
import javax.security.auth.Subject;

import net.osm.shell.Entity;
import net.osm.shell.Panel;
import net.osm.shell.ContextEvent;
import net.osm.shell.ContextListener;
import net.osm.shell.GenericAction;
import net.osm.shell.SplitPane;
import net.osm.shell.ScrollView;
import net.osm.shell.View;
import net.osm.util.ExceptionHelper;
import net.osm.util.IconHelper;
import net.osm.util.ActiveList;

/**
 * Support for the establishment of system and user preferences and the installation of 
 * pluggable desktop services.
 */
public class MGR extends DefaultComponentManager 
implements Block, LogEnabled, Contextualizable, Configurable, Initializable, Disposable, Shell, Service, Proxy
{

    //===========================================================
    // static
    //===========================================================

    public static Color background = Color.white;
    public static Color foreground = Color.black;
    public static Font font = new Font("Dialog", 0, 11);

    //===========================================================
    // state
    //===========================================================

   /**
    * The default logger for the service.
    */
    private Logger log;

   /**
    * The configuration is an in memory representation of the XML assembly file used 
    * as a container of static configuration values.
    */
    private Configuration configuration;

   /**
    * The currently selected entity.
    */
    private Entity entity;

   /**
    * The currently focused panel.
    */
    private Panel panel;
 
   /**
    * Application context
    */
    private BlockContext context;

   /**
    * The desktop.
    */
    private Desktop desktop;


    //========================================================================
    // LogEnabled
    //========================================================================
    
   /**
    * Sets the logger to be used during configuration, conposition, initialization 
    * and execution phase.
    *
    * @param logger Logger to direct log entries to
    */ 
    public void enableLogging( final Logger logger )
    {        
        log = logger;
    }

   /**
    * Returns the logging channel.
    */
    public Logger getLogger()
    {
        return log;
    }
    
    //=================================================================
    // Contextualizable
    //=================================================================

    public void contextualize( Context context ) throws ContextException
    {
	  if( getLogger().isDebugEnabled() ) getLogger().debug("shell contextulization");
	  if( context instanceof BlockContext ) 
	  {
	      this.context = (BlockContext) context;
	  }
	  else
	  {
		throw new ContextException("Supplied context does not implement BlockContext.");
	  }
	  if( getLogger().isDebugEnabled() ) getLogger().debug("shell contextulization complete");
    }


    //========================================================================
    // Configurable implementation
    //========================================================================
    
   /**
    * Configuration of the runtime environment based on a supplied Configuration arguments.
    * @param config Configuration representing an internalized model of the static xml configuration.
    * @exception ConfigurationException if the supplied configuration is incomplete or badly formed.
    */
    public void configure( final Configuration config )
    throws ConfigurationException
    {
	  if( getLogger().isDebugEnabled() ) getLogger().debug("shell confiration");
        this.configuration = config;
	  if( getLogger().isDebugEnabled() ) getLogger().debug("shell confiration complete");
    }

    //========================================================================
    // Initializable implementation
    //========================================================================

   /**
    * Initialization of the Desktop.
    * @exception Exception
    */
    public void initialize() throws Exception
    {

        // set preferences

	  if( getLogger().isDebugEnabled() ) getLogger().debug("shell initialization");
        final Configuration prefs = configuration.getChild( "preferences" );
        final Configuration fonts = prefs.getChild( "font" );
        final Configuration defaultFont = prefs.getChild( "default" );
	  font = new Font( 
		defaultFont.getAttribute("face", "Dialog"), 
		defaultFont.getAttributeAsInteger("style", 0),
		defaultFont.getAttributeAsInteger("size", 11)
        );

        final Configuration colours = prefs.getChild( "colours" );
        final Configuration defaultColor = prefs.getChild( "default" );
	  background = lookupColor( defaultColor.getAttribute("background", "white" ));
	  foreground = lookupColor( defaultColor.getAttribute("foreground", "black" ));

        if( getLogger().isDebugEnabled() ) getLogger().debug("shell initialization complete");
    }

   /**
    * Returns the default font for presentation within desktop artifacts.
    * @return Font the default font
    */
    public Font getDefaultFont()
    {
        return font;
    }

   /**
    * Returns the default background colour.
    * @return Color the default background
    */
    public Color getBackground()
    {
	  return background;
    }

   /**
    * Returns the default background colour.
    * @return Color the default foreground
    */
    public Color getForeground()
    {
	  return foreground;
    }

    private Color lookupColor( String string )
    {
        if( string.equals("white") )
	  {
		return Color.white;
        }
	  else if( string.equals("black") )
        {
		return Color.black;
        }
        else
        {
		return Color.gray;
        }
    }

    //========================================================================
    // Disposable
    //========================================================================

    public void dispose()
    {
    }

    //===============================================================
    // Service
    //===============================================================

   /**
    * The <code>setShell</code> is invoked by a <code>Desktop</code>
    * against blocks implementing the <code>Service</code> inerface.  
    * Services use the <code>Desktop</code> for message, error and common 
    * humman interface management tasks.
    */
    public void setDesktop( Desktop desktop )
    {
        if( getLogger().isDebugEnabled() ) getLogger().debug( "setting desktop" );
        this.desktop = desktop;
    }

    public List getTools()
    {
	  return new LinkedList();
    }

   /**
    * Returns an Action instances to be installed as 
    * menu items within the desktop preferences menu group.
    */
    public Action getPreferencesAction( )
    {
        return null;
    }


    //========================================================================
    // Shell
    //========================================================================

   /**
    * Declare an error to the shell.
    */
    public void error( String message, Throwable e )
    {
        desktop.error( message, e );
    }

   /**
    * Installs an extension into the shell.
    */
    public void install( Entity entity )
    {
        if( desktop == null )
	  {
		final String error = "desktop has not been set";
		throw new IllegalStateException( error );
        }

	  try
	  {
            if( getLogger().isDebugEnabled() ) getLogger().debug("install: " + entity );
	      if( entity == null ) throw new NullPointerException("cannot install a null entity");
	      desktop.install( entity );
	  }
	  catch( Throwable e )
	  {
	      final String error = "unable to install entity: " + entity ;
	      if( getLogger().isErrorEnabled() ) getLogger().debug( error, e );	
		ExceptionHelper.printException( error, e );
        }
    }
    
   /**
    * Sets the message in the status bar to null value.
    */
    public void setMessage() 
    {
        desktop.setMessage( "" );
    }
    
   /**
    * Sets the message in the status bar the name of the entity.
    */
    public void setMessage( Panel panel ) 
    {
        desktop.setMessage( panel.getEntity() );
    }

   /**
    * Sets the message in the status bar the name of the entity.
    */
    public void setMessage( Entity entity ) 
    {
        desktop.setMessage( entity.getName() );
    }
    
   /**
    * Sets the message in the status bar to the supplied value.
    * @param s String to be displayed in the status bar.
    */

    public void setMessage( String s ) 
    {
        desktop.setMessage( " " + s );
    }

   /**
    * Declare a progressive activitiy to the shell.
    */
    public void execute( Activity activity )
    {
	  desktop.execute( activity);
    }

}


