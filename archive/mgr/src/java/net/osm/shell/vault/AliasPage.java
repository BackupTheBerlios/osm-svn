
package net.osm.shell.vault;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.math.BigInteger;
import java.util.Random;
import java.util.Enumeration;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.AbstractAction;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JButton;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.Action;
import javax.swing.JTabbedPane;
import javax.swing.JScrollPane;
import javax.swing.JDialog;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumnModel;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.TableColumn;
import javax.swing.event.TableModelListener;
import javax.swing.event.TableModelEvent;

import java.security.KeyStore;
import java.security.KeyStoreException;

import net.osm.shell.control.wizard.Wizard;
import net.osm.shell.control.wizard.Page;
import net.osm.shell.control.path.PathPanel;
import net.osm.shell.control.field.PasswordPanel;
import net.osm.shell.control.field.NewPasswordPanel;
import net.osm.shell.control.field.FieldException;
import net.osm.shell.MGR;
import net.osm.util.ExceptionHelper;

/**
 * The <code>AboutPage</code> is helper class related to 
 * the Wizard class.  It supplies support for the monitoring 
 * of a cancel event and notification to the wizard.
 *
 * @author  Stephen McConnell
 * @version 1.0 21 AUG 2001
 */
public class AliasPage extends Page implements PropertyChangeListener, TableModelListener
{

    //==========================================================
    // state
    //==========================================================

    private boolean trace = false;

    private Action config;

    private String virginMessage = 
      "In order to establish a security \"Subject\", you must have at " +
	"least one key alias in your keystore.  A key alias is password " +
      "protected name that refers to an X509 certificate.";

    private String standardMessage = 
      "This page list the digital identities you have " +
      "established, lets you add new digital identities or " +
	"remove existing identities, and select a default " +
	"identity to use during login and authentication. ";

    private File keystoreFile = null;

    private Wizard wizard;

    private Component aliasPanel;

    private char[] password;

    private BigInteger magic;

    private DefaultTableModel keys;
  
    private DefaultTableModel certs;

   /**
    * A loaded keystore (null if loading failed).
    */
    private KeyStore keystore;

    private JButton defaultButton;

    private JButton createButton;

    private X500PrincipalAction createAction;

    //==========================================================
    // constructor
    //==========================================================

   /**
    * PasswordPage creation given a source action (representing the 
    * the source of keystore change events), the wizard into which
    * context messages are to be placed, and name for this action.
    * 
    * @param config keystore source
    * @param wizard the wizard into which context is placed
    * @param name the name of the action
    */
    public AliasPage( Action config, Wizard wizard, String name ) throws VaultException
    {
        super( name );
	  this.wizard = wizard;
        this.config = config;
        try
        {
            putValue( "alias", null );
		config.addPropertyChangeListener( this );
        }
        catch( Exception e )
        {
            throw new VaultException(
              "Unable to create a alias page.", e );
        }
    }

    //==========================================================
    // ActionListener
    //==========================================================

   /**
    * Called when the page is made active by the wizard.
    */
    public void actionPerformed( ActionEvent event )
    {
        getPanel().setCommandName( "Alias" );
        try
	  {
	      if( keystoreFile.exists() )
	      {
                wizard.setMessage( standardMessage );
	      }
	      else
	      {
                wizard.setMessage( virginMessage );
	      }

            //
            // create a list of the key and certificate aliases
	      // in the keystore
		//

            if( aliasPanel == null ) 
		{
                loadKeystore();
		    aliasPanel = createAliasPanel();
		    getPanel().setCommandBody( aliasPanel );
	  	    populateTables();
		}

		//
		// make sure that the create action has a good keystore 
		// and password reference
		//

		createAction.setKeystore( keystore );
		createAction.setPassword( password );

		//
		// set the default button (if there is a key alias then 
            // default is FINISH otherwise default is CREATE
		//

		if( keys.getRowCount() == 0 ) setCreateAsDefault();
	  }
        catch( Exception e )
	  {
	      ExceptionHelper.printException(
		  "Unexpected exception while building alias tables.", e );
		e.printStackTrace();
	  }
    }

    private Component createAliasPanel()
    {

        //
	  // create the shared column model
	  //

	  TableColumn aliasColumn = new TableColumn( 0, 100 );
        aliasColumn.setHeaderValue("Alias");
	  TableColumn dateColumn = new TableColumn( 1, 210 );
        dateColumn.setHeaderValue("Created");

        TableColumnModel columnModel = 
		new DefaultTableColumnModel();
	  columnModel.addColumn( aliasColumn );
	  columnModel.addColumn( dateColumn );

        //
        // create the data models and respective tables
        //

	  keys = new DefaultTableModel( new String[]{"Alias","Created"},0 );
	  keys.addTableModelListener( this );
	  certs = new DefaultTableModel( new String[]{"Alias","Created"},0 );
        JTable keysTable = new JTable( keys, columnModel );
        JTable certsTable = new JTable( certs, columnModel );

        //
        // create a tabbed pane and add the tables
        //

        JScrollPane certsScroller = new JScrollPane( certsTable );
        certsScroller.getViewport().setBackground( MGR.background );

        JScrollPane keysScroller = new JScrollPane( keysTable );
        keysScroller.getViewport().setBackground( MGR.background );

        JTabbedPane pane = new JTabbedPane();
        pane.setFont( MGR.font );
        pane.setPreferredSize( new Dimension( 320, 130 ));
        pane.addTab( "Keys", keysScroller );
        pane.addTab( "Certificates", certsScroller );

        //
        // create addition buttons and package everything
        // into a single container
        //

        JButton editButton = new JButton( "Edit" );
	  editButton.setEnabled( false );

	  createButton = new JButton( );
        createAction = new X500PrincipalAction( "Create", createButton );
	  createAction.addPropertyChangeListener( this );
        createButton.setAction( createAction );

	  JPanel buttonBar = new JPanel( new FlowLayout( FlowLayout.LEFT ) );
        buttonBar.add( createButton );
        buttonBar.add( editButton );

        Box box = new Box( BoxLayout.Y_AXIS );
        box.add( pane );
        box.add( buttonBar );

        return box;
    }

    private void populateTables() throws VaultException
    {
        try
	  {
            Enumeration aliases = keystore.aliases();
		while( aliases.hasMoreElements() )
		{
		    String alias = (String) aliases.nextElement();
		    String type = "";
		    if( keystore.isKeyEntry( alias ) )
		    {
			  type = "key";
			  addKeyEntry( alias ); 
	          }
		    else
		    {
			  type = "certificate";
			  Object[] row = new Object[]{ alias, keystore.getCreationDate( alias )};
		        certs.addRow( row );
	          }
		    System.out.println("alias: " + alias + ", " + type );
		}
	  }
	  catch( Exception e )
	  {
	      throw new VaultException("Unable to populate alias tables.", e );
	  }
    }

    private void addKeyEntry( String alias ) throws KeyStoreException
    {
        Object[] row = new Object[]{ alias, keystore.getCreationDate( alias )};
        keys.addRow( row );
    }

    private void loadKeystore() throws VaultException
    {
        if( trace ) System.out.println("loading keystore" );
        boolean result = false;
        FileInputStream stream = null;
        try
        {
            if( keystoreFile.exists() ) stream = new FileInputStream( keystoreFile );
            keystore = KeyStore.getInstance( KeyStore.getDefaultType() );
		keystore.load( stream, password );
		if( trace ) System.out.println("result: ok");
        }
        catch (Exception e) 
        {
            if( trace ) ExceptionHelper.printException("ALIAS PAGE, KEYSTORE LOADING", e );
		closeStream( stream );
	      stream = null;
		keystore = null;
		throw new VaultException("Unable to load keystore.", e );
	  }
    }

    private void saveKeystore() throws VaultException
    {
        if( trace ) System.out.println("saving keystore" );
        FileOutputStream stream = null;
        try
        {
            stream = new FileOutputStream( keystoreFile );
            keystore.store( stream, password );
	  }
	  catch( Exception error )
	  {
            throw new VaultException("Unable to save keystore.", error );
	  }
    }

    private void closeStream( FileInputStream stream )
    {
        if( stream != null ) try
	  {
		stream.close();
	  }
	  catch( Exception anything )
	  {
	  }
    }

    //==========================================================
    // PropertyChangeListener
    //==========================================================

   /**
    * Listens to file property changes - if the file held by the 
    * path panel is in fact a keystore, then enable the next button.
    */
    public void propertyChange( PropertyChangeEvent event )
    {
        String name = event.getPropertyName();
        if( event.getSource() == config )
	  {
	      if( name.equals("keystore") )
            {
		    Object o = event.getNewValue();
		    if( o instanceof File ) keystoreFile = (File) o;
                putValue("alias", new Boolean(false));
		    aliasPanel = null;
		    keys = new DefaultTableModel( new String[]{"Alias","Created"},0 );
		    certs = new DefaultTableModel( new String[]{"Alias","Created"},0 );
		    createAction = null;
	      }
	      else if( name.equals("password") )
		{
		    Object o = event.getNewValue();
		    if( o instanceof char[]  )
		    {
			  password = (char[]) o;
		    }
		    else
		    {
			  password = null;
		    }
	      }
        }
        else if( event.getSource() == createAction )
        {
            //
            // create has modified the keystore so we need to refresh 
	      // the key table
		//

            if( event.getNewValue() instanceof KeyStatus )
		{
		    try
	          {
		        KeyStatus status = (KeyStatus) event.getNewValue();
		        System.out.println(
		          "ALIAS-PAGE, key status received: " + status.getValue() ); 
		        addKeyEntry( status.getAlias() );
			  saveKeystore();
		    }
	          catch( Exception e )
		    {
	              ExceptionHelper.printException("Unable to add new key alias.", e );
		    }
	      }
        }
    }

    //==========================================================
    // TableModelListener
    //==========================================================

   /**
    * Listens to key table changes. If the size of the key table
    * changes from 0 to 1, a page alias event is issued with a true
    * value.  If the size falls to 0, a false alias event is issued.
    */
    public void tableChanged( TableModelEvent event )
    {
	  int n = keys.getRowCount();
	  System.out.println("count: " + n );
        putValue("alias", new Boolean( n > 0 ));
	  if( n == 0 ) 
	  {
		setCreateAsDefault();
	  }
    }

    //==========================================================
    // internals
    //==========================================================

    private void setCreateAsDefault( )
    {
        if( defaultButton == null ) defaultButton = getCurrentDefault();
        setDefaultButton( createButton );
    }

    private JButton getCurrentDefault()
    {
	  JDialog root = getDialog( createButton );
	  return root.getRootPane().getDefaultButton();
    }

    private void setDefaultButton( JButton button )
    {
	  JDialog root = getDialog( createButton );
	  root.getRootPane().setDefaultButton( button );
	  button.requestFocus();
    }
}
