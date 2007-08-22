
package net.osm.shell.vault;

import java.io.File;
import java.awt.Frame;
import java.awt.Font;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Component;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.AbstractAction;
import javax.swing.SwingUtilities;
import javax.swing.JOptionPane;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JDialog;
import javax.swing.JCheckBox;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.JTabbedPane;
import javax.swing.Timer;

import java.security.KeyStore;
import javax.security.auth.x500.X500Principal;

import net.osm.util.ExceptionHelper;
import net.osm.shell.control.field.TextPanel;
import net.osm.shell.control.field.NewPasswordPanel;
import net.osm.shell.control.field.NotEmptyValidator;
import net.osm.shell.control.field.LabelPanel;
import net.osm.shell.MGR;
import net.osm.shell.control.activity.*;
import net.osm.shell.control.activity.DiodeDialog;
import net.osm.util.ExceptionHelper;

/**
 * The <code>X500PrincipalAction</code> class provides support for the creation 
 * of a digital identity.
 *
 * @author  Stephen McConnell
 * @version 1.0 21 AUG 2001
 */
class X500PrincipalAction extends AbstractAction implements Closable, PropertyChangeListener
{

    //==========================================================
    // static
    //==========================================================

    private String dialogTitle = "Key Factory";
 
    //==========================================================
    // state
    //==========================================================
 
   /**
    * Debug trace status.
    */
    private boolean trace = false;

   /**
    * Dialog containing the X500 Principal data collection form.
    */
    private JDialog dialog;

   /**
    * Parent JDialog against which modal status is established.
    */
    private JDialog parent;

   /**
    * The component in the parent from which the parent is resolved.
    */
    private Component source;

   /**
    * The dialog cancel button.
    */
    private JButton cancel;

   /**
    * The dialog cancel action.
    */
    private CancelAction cancelAction;

   /**
    * Field containing the alias name.
    */
    private TextPanel alias;

   /**
    * Field containing the X500 Common Name.
    */
    private TextPanel commonName;

   /**
    * Field containing the X500 Email address.
    */
    private TextPanel email;

   /**
    * Field containing the X500 Organization name.
    */
    private TextPanel organizationName;

   /**
    * Field containing the X500 Organization Unit name.
    */
    private TextPanel organizationUnitName;

   /**
    * Submit (Ok) button
    */
    private JButton submit;

   /**
    * Submit (Ok) action.
    */
    DiodeDialog submitAction;

   /**
    * X500 Principal established from form content.
    */
    X500Principal principal;

   /**
    * Key generation activiity to be executed under the submit action.
    */
    KeyGeneratorActivity activity;

   /**
    * The underlying keystore.
    */
    KeyStore keystore;

   /**
    * Keystore password.
    */
    char[] password;


    //==========================================================
    // constructor
    //==========================================================

   /**
    * Creation of a new <code>X500PrincipalAction</code> that presents an
    * X500 Principal creation form.
    * 
    * @param name the name of the action
    * @param component logically triggering the action
    * @param virgin true if the keystore does not exist
    */
    public X500PrincipalAction( String name, Component source )
    {
        super( name );
        this.source = source;
    }

   /**
    * Called by the AliasPage instance to declare the initialized keystore
    * into which principal identities shall be stored.
    */
    public void setKeystore( KeyStore keystore )
    {
        this.keystore = keystore;
    }

   /**
    * Called by the AliasPage instance to declare the keystore password.
    */
    public void setPassword( char[] password )
    {
        this.password = password;
    }


    //==========================================================
    // ActionListener
    //==========================================================

   /**
    * Triggered when the component hosting this action is trigger, resulting
    * in the display of the X500 Principal modal dialog.  
    */
    public void actionPerformed( ActionEvent event )
    {
        if( dialog == null ) dialog = createDialog();

        //
	  // make sure the KeyGeneratorActivity has a reference to an 
	  // initialized keystore
	  //

	  dialog.setVisible( true );
    }

    //==========================================================
    // Internals
    //==========================================================

   /**
    * Internal method to create the dialog and internal components.
    * @see actionPerformed
    */
    private JDialog createDialog()
    {

        //
        // Create the panel title and line separator
        //

        JLabel label = new JLabel( "X500 Principal" );
        label.setFont( new Font("Dialog", 0, 18 ));
        label.setBorder( new EmptyBorder( 35, 10, 14, 10 ));
        JPanel labelHolder = new JPanel( new FlowLayout( FlowLayout.LEFT ) );
        labelHolder.add( label );
        JPanel line = createSeparator( 380 );

        //
        // Package the label and the line into a header
        //

        JPanel header = new JPanel( new BorderLayout() );
        header.add( labelHolder, BorderLayout.NORTH );
        header.add( line, BorderLayout.SOUTH );

        //
        // create the body containing the alias name, X500 name fields
        // and supplementary password fields
        //

	  String defaultAliasString = "";
	  if( this.keystore == null ) defaultAliasString = System.getProperty("user.name");
        alias = new TextPanel(
	        "Alias:", defaultAliasString, 10, new NotEmptyValidator() );
        alias.setBorder( new EmptyBorder(0,20,20,20) );
        alias.addPropertyChangeListener( this );

        commonName = new TextPanel("Full Name (CN):", new NotEmptyValidator());
        commonName.addPropertyChangeListener( this );

        email = new TextPanel("Email Address (E):", new NotEmptyValidator());
        email.addPropertyChangeListener( this );

        Box name = new Box( BoxLayout.Y_AXIS );
        name.add( commonName );
        name.add( email );
        JPanel nameHolder = new JPanel( new BorderLayout() );
        nameHolder.add( name, BorderLayout.NORTH );

        organizationName = new TextPanel("Organization (O):", new NotEmptyValidator());
        organizationName.addPropertyChangeListener( this );

        organizationUnitName = new TextPanel("Unit (OU):", new NotEmptyValidator());
        organizationUnitName.addPropertyChangeListener( this );

        Box organization = new Box( BoxLayout.Y_AXIS );
        organization.add( organizationName );
        organization.add( organizationUnitName );
        JPanel organizationHolder = new JPanel( new BorderLayout() );
        organizationHolder.add( organization, BorderLayout.NORTH );

        JCheckBox protectWithPassword = new JCheckBox( );
        protectWithPassword.setFont( MGR.font );

        boolean defaultSecondaryProtection = false;
        JPanel protectWithPasswordHolder = new JPanel( new FlowLayout( FlowLayout.LEFT ));
        protectWithPasswordHolder.add( protectWithPassword );
        protectWithPasswordHolder.setBorder( new EmptyBorder(3,3,0,10) );
        NewPasswordPanel passwordPanel = new NewPasswordPanel( 15, 6, null );
        passwordPanel.setBorder( new EmptyBorder(0,20,0,10) );
        passwordPanel.setVisible( defaultSecondaryProtection );

        ToggleVisibilityAction toggle = new ToggleVisibilityAction(
          "Protect with a password (optional)", protectWithPassword, passwordPanel );
        toggle.setEnabled( true );
        protectWithPassword.setAction( toggle );

        Box protection = new Box( BoxLayout.Y_AXIS );
        protection.add( protectWithPasswordHolder );
        protection.add( passwordPanel );
        JPanel protectionHolder = new JPanel( new BorderLayout() );
        protectionHolder.add( protection, BorderLayout.NORTH );

        JTabbedPane pane = new JTabbedPane();
        pane.setBorder( new EmptyBorder(0,20,20,20) );
        pane.setFont( MGR.font );
        pane.addTab( "Name", nameHolder );
        pane.addTab( "Organization", organizationHolder );
        pane.addTab( "Password", protectionHolder );

        JPanel body = new JPanel( new BorderLayout() );
        body.add( alias, BorderLayout.NORTH );
        body.add( pane, BorderLayout.CENTER );
        
        //
        // Create the button controls
        //

        submit = new JButton( "Ok" );
        cancel = new JButton( "Cancel" );

        JPanel buttonPanel = new JPanel( new FlowLayout( FlowLayout.RIGHT ) );
        buttonPanel.add( cancel );
        buttonPanel.add( submit );
        Box buttonHolder = new Box( BoxLayout.Y_AXIS );
        buttonHolder.add( createSeparator( 380 ) );
        buttonHolder.add( buttonPanel );

        //
        // Package header, body and buttons
        //

        JPanel holder = new JPanel( new BorderLayout() );
	  holder.add( header, BorderLayout.NORTH );
	  holder.add( body, BorderLayout.CENTER );
	  holder.add( buttonHolder, BorderLayout.SOUTH );

        //
        // Create the dialog and set the content
        //

	  dialog = new JDialog( getDialog( source ) , dialogTitle, true );
        dialog.setContentPane( holder );
	  dialog.setSize( 400, 450 );
        dialog.setResizable( false );
        dialog.setLocationRelativeTo( source );
        dialog.getRootPane().setDefaultButton( submit );

        //
        // Add the action handlers
        //

        cancelAction = new CancelAction( "Cancel", cancel, this );
        cancel.setAction( cancelAction );

        activity = new KeyGeneratorActivity( "Key generator", submit, this );
        activity.addPropertyChangeListener( this );

        submitAction = new DiodeDialog("Ok", submit, "Key Generation Status", 5, activity );
        submit.setAction( submitAction );
        return dialog;
    }

    public JDialog getDialog( Component component )
    {
       if( parent == null ) parent = component instanceof JDialog ? (JDialog) component
              : (JDialog)SwingUtilities.getAncestorOfClass(JDialog.class, component);
       return parent;
    }

    //==========================================================
    // ActionListener
    //==========================================================

   /**
    * Close the supporting instance.
    */
    public void close( CancelAction source )
    {
        if( source == cancelAction ) dialog.setVisible( false );
    }

    //==========================================================
    // PropertyChangeListener
    //==========================================================

   /**
    * Listens to field status property changes.
    */
    public void propertyChange( PropertyChangeEvent event )
    {
        String name = event.getPropertyName();
	  if(( name.equals("status") ) && ( event.getSource() instanceof TextPanel ))
        {
		if( validateFormContent() )
		{

		    //
		    // enable the submit action which will allow 
		    // the user to trigger creation of a new key alias
		    // following which the key generator activity will
	          // be started during which it call requestInitialization
		    // to get the pricipal, alias and keystore
		    //

                submitAction.setEnabled( true );
		}
        }
	  else if( event.getSource() == activity )
        {
            if( event.getNewValue() instanceof KeyStatus )
		{
		    KeyStatus status = ((KeyStatus)event.getNewValue());
		    System.out.println(
			"X500-PRINCIPAL-ACTION, key status received: " + status.getValue() );
		    // this is where we should close the dialog
		    putValue("key", status ); 
		    if( status.getValue() )
		    {
			  // key generation was successful, close the dialog
		        dialog.setVisible( false );
		    }
		    else
		    {
			  // key generation failed - get the exception from the 
			  // key status instance and throw up a error dialog
			  
	          }
	      }
        }
    }

    private boolean validateFormContent()
    {
        if( !alias.verify() ) return false;
        if( !commonName.verify() ) return false;
        if( !email.verify() ) return false;
        if( !organizationName.verify() ) return false;
        if( !organizationUnitName.verify() ) return false;
        return true;
    }

   /**
    * Called by the key generator.  The implementation supplies
    * the principal, alias and keystore to the key generator 
    * action established during actionPerformed.
    */
    protected void requestInitialization( )
    {
        activity.setPrincipal( buildX500Principal() );
        activity.setKeystore( keystore );
        activity.setAlias( alias.getText() );

	  //
	  // WARNING: not currently using the option password
        // field - just passing keystore password
	  //

        activity.setPassword( password );
    }

    protected X500Principal buildX500Principal()
    {
	  String x500 = "CN=" + commonName.getText().trim() + ",";
        x500 = x500 + "EMAILADDRESS=" + email.getText().trim() + ",";
        x500 = x500 + "O=" + organizationName.getText().trim() + ",";
        x500 = x500 + "OU=" + organizationUnitName.getText().trim();
        principal = new X500Principal( x500 );
	  return principal;
    }

    //==========================================================
    // internals
    //==========================================================

    private JPanel createSeparator( int width )
    {
        //
        // Create a line seperator
        //

        JPanel line = new JPanel();
        line.setBorder( new EtchedBorder( EtchedBorder.LOWERED ) );
        line.setPreferredSize( new Dimension( width, 2 ));
	  JPanel lineHolder = new JPanel( );
        lineHolder.add( line );
        lineHolder.setBorder( new EmptyBorder( 3, 10, 3, 10 ));
        return line;
    }

}
