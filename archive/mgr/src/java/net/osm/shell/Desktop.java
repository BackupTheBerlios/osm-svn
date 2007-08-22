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
import java.awt.event.MouseEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.JMenu;
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
import javax.swing.JProgressBar;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.JTabbedPane;
import javax.swing.JOptionPane;
import javax.swing.JPasswordField;
import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;

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
import net.osm.shell.ClipboardHandler;
import net.osm.util.ActiveList;
import net.osm.shell.GenericAction;
import net.osm.shell.SplitPane;
import net.osm.shell.ScrollView;
import net.osm.shell.View;
import net.osm.shell.*;
import net.osm.util.ExceptionHelper;
import net.osm.util.IconHelper;

/**
 * The <code>MGR</code> is ...
 */

public class Desktop extends JFrame 
implements LogEnabled, Configurable, Initializable, Startable, Disposable, ContextListener, Clipboard, BlockListener, Shell
{

    //===========================================================
    // static
    //===========================================================

    public static Color background = Color.white;
    public static Color foreground = Color.black;
    public static Font font = new Font("Dialog", 0, 11);

    private static final String windowTitle = "MGR";
    private static final int windowWidth = 700;
    private static final int windowHeight = 500;

    private static final String outputString = "Output";
    private static boolean trace = false;

    private static final ImageIcon backIcon = IconHelper.loadIcon( 
	"net/osm/shell/image/back.gif" );
    private static final ImageIcon backOutlineIcon = IconHelper.loadIcon( 
	"net/osm/shell/image/back-outline.gif" );

    private static final ImageIcon forwardIcon = IconHelper.loadIcon( 
	"net/osm/shell/image/forward.gif" );
    private static final ImageIcon forwardOutlineIcon = IconHelper.loadIcon( 
	"net/osm/shell/image/forward-outline.gif" );

    private static final ImageIcon upIcon = IconHelper.loadIcon( 
	"net/osm/shell/image/up.gif" );
    private static final ImageIcon upOutlineIcon = IconHelper.loadIcon( 
	"net/osm/shell/image/up-outline.gif" );

    private static final ImageIcon cutIcon = IconHelper.loadIcon( 
	"net/osm/shell/image/cut.gif" );
    private static final ImageIcon cutOutlineIcon = IconHelper.loadIcon( 
	"net/osm/shell/image/cut-outline.gif" );

    private static final ImageIcon copyIcon = IconHelper.loadIcon( 
	"net/osm/shell/image/copy.gif" );
    private static final ImageIcon copyOutlineIcon = IconHelper.loadIcon( 
	"net/osm/shell/image/copy-outline.gif" );

    private static final ImageIcon pasteIcon = IconHelper.loadIcon( 
	"net/osm/shell/image/paste.gif" );
    private static final ImageIcon pasteOutlineIcon = IconHelper.loadIcon( 
	"net/osm/shell/image/paste-outline.gif" );

    private static final ImageIcon clearIcon = IconHelper.loadIcon( 
	"net/osm/shell/image/clear.gif" );
    private static final ImageIcon clearOutlineIcon = IconHelper.loadIcon( 
	"net/osm/shell/image/clear-outline.gif" );

    //===========================================================
    // state
    //===========================================================

    private Action backAction; // not used
    private Action forwardAction; // not used
    private Action upAction; // not used
    private Action cutAction;
    private Action copyAction;
    private Action pasteAction;
    private Action pasteSpecialAction;
    private Action clearAction;

    private JPopupMenu popup;
    private RenameAction rename;
    private Action propertiesAction;
    private Action modelAction;

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
    * The panel used to contain the navigation hierarchy.
    */
    private Panel body;

   /**
    * The currently selected entity.
    */
    private Entity entity;

   /**
    * The currently focused panel.
    */
    private Panel panel;
    private ClipboardHandler handler; // same as panel
 
   /**
    * Status panel.
    */
    private Status status = new Status();

   /**
    * The menubar.
    */
    private MenuBar menubar;

   /**
    * The split pane view containing the navigator panel.
    */
    private SplitPane pane;

   /**
    * List of entities installed as root elements within the navigator.
    */
    private ActiveList extensions;

   /**
    * The toolbar.
    */
    private ToolBar toolbar;

   /**
    * A null entity representing the shell - this is required as an argument
    * to the active panel containing the navigatable content.
    */
    private Entity shell;

   /**
    * The clipboard contents.
    */
    private Object[] scrap = new Object[0];

   /**
    * Application context
    */
    private BlockContext context;

    private CreateMenu createMenu;
    private CreateMenu createPopupMenu;
    private ResourceMenu resourceMenu;

   /**
    * The security subject established through the login policy handler.
    */
    private Subject subject;

    private long lastDoubleClick = 0;

    //========================================================================
    // static operations
    //========================================================================

    public static boolean getTracePolicy()
    {
        return trace;
    }
    

    //========================================================================
    // Constructor
    //========================================================================
    
    /**
     * The <code>Desktop</code> class provides a simple management desktop 
     * framework.
     */
    
    public Desktop( ) 
    {
        super();

    }

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
	  if( context instanceof BlockContext ) 
	  {
	      this.context = (BlockContext) context;
	  }
	  else
	  {
		throw new ContextException("Supplied context does not implement BlockContext.");
	  }
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
        this.configuration = config;
    }

    //========================================================================
    // Initializable
    //========================================================================

   /**
    * Initialization of the Desktop.
    * @exception Exception
    */
    public void initialize() throws Exception
    {

        extensions = new ActiveList( getLogger() );
        shell = new KernelEntity("Shell", extensions );

        backAction = new GenericAction( "Back", backOutlineIcon, this, "handleBack", false );
        forwardAction = new GenericAction( "Forward", forwardOutlineIcon, this, "handleForward", false );
        upAction = new GenericAction( "Up", upOutlineIcon, this, "handleUp", false );

        cutAction = new GenericAction( "Cut", this, "handleCut", false );
        copyAction = new GenericAction( "Copy", this, "handleCopy", false );
        pasteAction = new GenericAction( "Paste",  this, "handlePaste", false );
        pasteSpecialAction = new GenericAction( "Paste Special ...", 
	    this, "handlePasteSpecial", false );
        clearAction = new GenericAction( "Delete", this, "handleDelete", false );
        rename = new RenameAction( "Rename ...", false, -1 );

        propertiesAction = new GenericAction( "Properties...", this, "handleProperties", false );
        modelAction = new GenericAction( "Model...", this, "handleModel", false );

        addWindowListener(
		new WindowAdapter() 
            {
                public void windowClosing(WindowEvent e) 
                {
                    exit(0);
                }
            }
        );

	  //
        // Write copyright info to the log file.
        //

	  String name = "OSM Shell ";
	  String copyright = "Copyright (C), 2000-2001 OSM SARL. ";
        String rights = "All Rights Reserved.";
	  String banner = "\n" + name + copyright + "\n" + rights;
	  getLogger().info( banner );

	  //
        // Configure the root window.
        //

	  try
	  {
            setTitle( windowTitle );
            setSize( windowWidth, windowHeight );

            //
            // Create the menubar
	      //

            createMenu = new CreateMenu( "Create ", KeyEvent.VK_N  );
            resourceMenu = new ResourceMenu( "Resource", KeyEvent.VK_R, this, propertiesAction, modelAction );
		resourceMenu.insert( createMenu, 0 );

            menubar = new MenuBar( resourceMenu, new EditMenu( 
		  "Edit", KeyEvent.VK_E, cutAction, copyAction, pasteAction, pasteSpecialAction, 
              clearAction, rename ) );
            setJMenuBar( menubar );

		JMenu test = new JMenu("Test");
		test.setFont( font );
		menubar.add( test );
            test.add( new GenericAction( "Run Test", this, "doTest" ) );

            body = new NavigatorPanel( shell, "Navigator" );
            pane = new SplitPane( new ScrollView( body, false ));
            pane.addContextListener( this );

            //
            // configure the toolbar with the set of standard actions
	      //

            toolbar = new ToolBar( );
		toolbar.addAction( backAction, backOutlineIcon, backIcon );
            toolbar.addAction( forwardAction, forwardOutlineIcon, forwardIcon );
            toolbar.addAction( upAction, upOutlineIcon, upIcon );
		toolbar.addSeparator();
            toolbar.addAction( cutAction, cutOutlineIcon, cutIcon );
            toolbar.addAction( copyAction, copyOutlineIcon, copyIcon );
            toolbar.addAction( pasteAction, pasteOutlineIcon, pasteIcon );
		toolbar.addSeparator();
            toolbar.addAction( clearAction, clearOutlineIcon, clearIcon );

            //
            // configure the popup with the set of standard actions
	      //

            createPopupMenu = new CreateMenu( "Create" );
		popup = new JPopupMenu();
    		popup.setFont( font );
            popup.add( new MenuItem( propertiesAction ));
		popup.addSeparator();
            popup.add( new MenuItem( rename ));
		popup.addSeparator();
            popup.add( createPopupMenu );
		popup.addSeparator();
            popup.add( new MenuItem( cutAction ));
            popup.add( new MenuItem( copyAction ));
            popup.add( new MenuItem( pasteAction ));
            popup.add( new MenuItem( pasteSpecialAction ));
		popup.addSeparator();
            popup.add( new MenuItem( clearAction ));

            //
            // build the main window
            //

            Container contentPane = getContentPane();
            contentPane.add( toolbar, BorderLayout.NORTH );
            contentPane.add( pane, BorderLayout.CENTER );
            contentPane.add( status, BorderLayout.SOUTH );
	      status.setMessage( name + " - " + copyright + rights );
            setVisible(true);
        }
        catch( Throwable e )
        {
		ExceptionHelper.printException( e );
            throw new RuntimeException("failed to instantiate desktop", e );
        }

        if( getLogger().isDebugEnabled() ) getLogger().debug("initialization phase complete");
    }

   /**
    * Declare a progressive activitiy to the shell.
    */
    public void execute( Activity activity )
    {
	  status.execute( activity);
    }

   /**
    * Declare an error to the shell.
    */
    public void error( String message, Throwable e )
    {
        status.error( message, e );
    }

    public void doTest()
    {
	  System.out.println("hello");
    }

    public void handleProperties()
    {
	  Entity entity = null;
        if( panel.getSelectionCount() == 1 )
        {
            entity = panel.getDefaultEntity();
        }
	  else
	  {
		entity = panel.getEntity();
	  }
        PropertiesDialog dialog = new PropertiesDialog( "Properties", entity, this );
	  dialog.setVisible( true );
    }

    public boolean checkModelAction( Panel panel )
    {
	  if( panel == null ) return false;
	  if( panel.getEntity() instanceof Simulator ) return true;
	  if( panel.getDefaultEntity() != null ) if( panel.getDefaultEntity() instanceof Simulator ) return true;
	  return false;
    }

    public void handleModel()
    {
	  Entity entity = null;
	  if( panel == null ) return;
	  if( panel.getDefaultEntity() != null ) if( panel.getDefaultEntity() instanceof Simulator )
        {
	      entity = panel.getDefaultEntity();
	  }
	  else if( panel.getEntity() instanceof Simulator )
	  {
	      entity = panel.getEntity();
	  }
	  if( entity == null ) return;
        PropertiesDialog dialog = new PropertiesDialog( 
		"Model", entity, ((Simulator)entity).getModel(), this, new Dimension( 600, 450 ));
	  dialog.setVisible( true );
    }

    //========================================================================
    // BlockListener
    //========================================================================
    
   /**
    * Notification that a block has just been added
    * to Server Application.
    *
    * @param event the BlockEvent
    */
    public void blockAdded( BlockEvent event )
    {
	  try
	  {
		final String msg = "Loading block " + event.getName();
		if( getLogger().isDebugEnabled() ) getLogger().debug( "observing block: " + event.getName() );
  	      System.out.println("Adding block " + event.getName() );
		setMessage( );
	      Block block = event.getBlock();
	      if( block instanceof MGR ) ((MGR)block).setDesktop( this );

	      if( block instanceof net.osm.shell.Proxy )
	      {
		    if( getLogger().isDebugEnabled() ) getLogger().debug( 
		       "declaring desktop to " + event.getName() );
                ((Proxy)block).setDesktop( this );
            }

	      if( block instanceof net.osm.shell.Service )
	      {
		    setMessage( "Installing block " + event.getName() );
		    List list = ((Service)block).getTools();
		    Iterator iterator = list.iterator();
		    while( iterator.hasNext() )
		    {
			  addTool( (Action) iterator.next() );
		    }
            }
        }
        catch( Exception e )
	  {
		if( getLogger().isErrorEnabled() ) getLogger().error( "observation error", e );
		setMessage( "Block error: " + e );
	  }
    }

   /**
    * Notification that a block is just about to be 
    * removed from Server Application.
    *
    * @param event the BlockEvent
    */
    public void blockRemoved( BlockEvent event )
    {
        if( getLogger().isDebugEnabled() ) getLogger().debug( "removing block: " + event.getName() );
	  System.out.println("Removing block " + event.getName() );
	  setMessage( "Block removed: " + event.getName() );
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
    // Startable
    //========================================================================

    public void start()
    {
        if( getLogger().isInfoEnabled() ) getLogger().info("Shell startup complete");
    }

    public void stop()
    {
	  stop( 0 );
    }

    public void stop( int value )
    {
        //
        // need to stop all of the installed components
        //

        if( getLogger().isInfoEnabled() ) getLogger().info("stopping the shell");
        System.exit( value );
    }

    //========================================================================
    // Disposable
    //========================================================================

    public void dispose()
    {
        status.dispose();
    }

    //===============================================================
    // ContextListener
    //===============================================================

   /**
    * Method invoked when a control within the human interface gains command focus.
    */
    public void contextGained( ContextEvent event )
    {
	  
        Panel panel = event.getPanel();
        propertiesAction.setEnabled( true );
        createMenu.setPanel( panel );
        resourceMenu.setPanel( panel );
        rename.setEntity( panel.getDefaultEntity() );
	  modelAction.setEnabled( checkModelAction( panel ));
	  synchronized( menubar )
	  {
		this.panel = panel;
	      menubar.setPanel( panel );
		if( panel instanceof ClipboardHandler )
		{
		    handler = (ClipboardHandler) panel;
            }
		else
	      {
		    handler = null;
		}
	      clipboardChange( handler );
		if( panel.getDefaultEntity() == null )
		{
		    setMessage( "entity: " + panel.getEntity().getName() + " (" + panel.getRole() + ")");
		}
		else
	      {
		    setMessage( "entity: " + panel.getEntity().getName() + " (" + panel.getRole() 
		      + "), selection: " + panel.getDefaultEntity().getName() );
		}
        }

        if( event.getEvent() instanceof MouseEvent )
        {
            createPopupMenu.setPanel( panel );
		MouseEvent me = (MouseEvent) event.getEvent();
		if( me.isPopupTrigger() )
		{
		    // clear any extranouse menu items in the popup

	          int k = popup.getComponentCount();
		    if( k > 12 )
		    {
			  for( int i=(k-1); i>11; i-- )
			  {
				popup.remove( i );
			  }
		    }

		    if( panel instanceof JTable )
		    {
			  JTable table = (JTable) panel;
		        if( table.rowAtPoint( me.getPoint() ) > -1 )
			  {
			      int j = table.getSelectedRows().length;
				if( j == 1 )
				{
				    //
				    // get the entities set of actions
				    //
				    
				    Entity entity = panel.getDefaultEntity();
				    if(( entity != null ) && (entity instanceof ActionHandler))
				    {
					  List list = ((ActionHandler)entity).getActions( );
					  if( list.size() > 0 )
					  {
						popup.addSeparator();
						Iterator iterator = list.iterator();
						while( iterator.hasNext() )
						{
						    popup.add( new MenuItem( (Action) iterator.next() ));
						}
					  }
				    }
				}
				else
				{
				    //
				    // ignore this case
				    //
				}
			  }
			  else
			  {
				Entity entity = panel.getEntity();
			      if( entity instanceof ActionHandler )
			      {
				    List list = ((ActionHandler)entity).getActions( );
				    if( list.size() > 0 )
				    {
				 	  popup.addSeparator();
					  Iterator iterator = list.iterator();
					  while( iterator.hasNext() )
				   	  {
					      popup.add( new MenuItem( (Action) iterator.next() ));
					  }
				    }
			      }
		        }
                    popup.show( me.getComponent(), me.getX(), me.getY() );
		    }
		}
            else if (SwingUtilities.isLeftMouseButton( me ) && me.getClickCount() == 2) 
	      {
		    // the next line handles the problem that the JVM returns 
		    // two double click event sequentially - we basically ignore
		    // the second event if it occurs less than 300 milliseconds 
                // after the first event

		    if( lastDoubleClick + 300 > me.getWhen() ) return;		    
		    lastDoubleClick = me.getWhen();
		    if( panel instanceof JTable )
		    {
			  JTable table = (JTable) panel;
		        if( table.rowAtPoint( me.getPoint() ) > -1 )
			  {
			      int j = table.getSelectedRows().length;
				if( j == 1 )
				{
				    handleProperties();
				}
			  }
		    }
		}
        }
    }

   /**
    * Method invoked when a panel or item looses focus.
    */
    public void contextLost( ContextEvent event )
    {
    }

    //========================================================================
    // Shell
    //========================================================================

   /**
    * Installs an extension into the shell.
    */
    public void install( Entity entity )
    {
        if( getLogger().isInfoEnabled() ) getLogger().info("Installing: " + entity );
	  extensions.add( entity );
	  setMessage( "Installed '" + entity.getName() + "'.");
    }
    
   /**
    * Add a tool to the shell.
    */
    private void addTool( Action action )
    {
        menubar.addTool( action );
    }

    private void exitFileItemActionPerformed(ActionEvent evt) 
    {
        this.exit( 0 );
    }

   /**
    * Terminates the session.
    */
    private void exit( int status ) 
    {
        this.stop();
    }
    
   /**
    * Sets the message in the status bar to null value.
    */
    public void setMessage() 
    {
        status.setMessage( "" );
    }
    
   /**
    * Sets the message in the status bar the name of the entity.
    */
    public void setMessage( Panel panel ) 
    {
        setMessage( panel.getEntity() );
    }

   /**
    * Sets the message in the status bar the name of the entity.
    */
    public void setMessage( Entity entity ) 
    {
        status.setMessage( entity.getName() );
    }    

   /**
    * Sets the message in the status bar to the supplied value.
    * @param s String to be displayed in the status bar.
    */

    public void setMessage( String s ) 
    {
        status.setMessage( " " + s );
    }

    //===============================================================
    // Clipboard
    //===============================================================

   /**
    * Place array into the cliboard buffer.
    * @param object the entity to place on the clipboard
    */
    public void putScrap( Object[] array )
    {
        if( array == null ) 
	  {
		array = new Object[0];
        }
        else
        {
            scrap = array;
        }
	  clipboardChange( );
    }

   /**
    * Returns the current clipboard object.
    * @return the current (possibly null) clipboard object
    */
    public Object[] getScrap( )
    {
        if( scrap == null ) scrap = new Object[0];
        return scrap;
    }

    public void clipboardChange( )
    {
	  if( panel instanceof ClipboardHandler )
	  {
		clipboardChange( (ClipboardHandler) panel );
        }
        else
	  {
            clipboardChange( null );
	  }
    }

    public void clipboardChange( ClipboardHandler handler )
    {
	  if( handler != null )
	  {
		if( getScrap().length > 0 )
		{
		    pasteAction.setEnabled( handler.canPaste( getScrap() ));
		    pasteSpecialAction.setEnabled( handler.canPasteSpecial( getScrap() ));
            }
		else
		{
		    pasteAction.setEnabled( false );
                pasteSpecialAction.setEnabled( false );
            }
	      cutAction.setEnabled( handler.canCut() );
	      copyAction.setEnabled( handler.canCopy() );
	      clearAction.setEnabled( handler.canDelete() );
	  }
        else
	  {
		pasteAction.setEnabled( false );
            pasteSpecialAction.setEnabled( false );
	      cutAction.setEnabled( false );
	      copyAction.setEnabled( false );
	      clearAction.setEnabled( false );
	  }
    }

    //======================================================================
    // ClipboardHandler
    //======================================================================

   /**
    * Request to a handler to to process link deletion based on the current selection.
    * @return boolean true if the deletion action was completed sucessfully
    */
    public void handleDelete()
    {
        if( handler != null ) handler.handleDelete();
    }

   /**
    * Request to a handler to return an array of Entity instances to be placed
    * on the clipboard.
    * @return Object[] array of cut entities
    */
    public void handleCut()
    {
	  if( handler != null ) putScrap( handler.handleCut() );
    }

   /**
    * Request to a handler to return an array of Entity instances to be placed
    * on the clipboard in response to a user copy request.
    * @return Object[] array of cut entities
    */
    public void handleCopy()
    {
	  if( handler != null ) putScrap( handler.handleCopy() );
    }

   /**
    * Request issued by the desktop to a panel to handle the pasting of 
    * the current clipboard content into the panel.
    * @param array - the clipboard content
    */
    public void handlePaste( )
    {
        if( handler != null ) handler.handlePaste( scrap );
    }

   /**
    * Request issued by the desktop to a panel to handle the pasting of 
    * the current clipboard content into the panel using the Paste Special
    * context.
    * @param array the clipboard content
    */
    public void handlePasteSpecial( )
    {
        if( handler != null ) handler.handlePasteSpecial( scrap );
    }


}


