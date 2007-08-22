
package net.osm.shell.vault;

import java.awt.Component;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.util.Set;
import java.util.List;
import java.util.LinkedList;
import java.util.Date;
import java.util.Iterator;
import java.util.GregorianCalendar;
import java.util.Random;
import javax.swing.Action;
import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import java.security.KeyPairGenerator;
import java.security.SecureRandom;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Principal;
import java.security.KeyStore;
import java.security.InvalidKeyException;
import java.security.SignatureException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Signature;
import java.security.cert.X509Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertPath;
import javax.security.auth.Subject;
import javax.security.auth.x500.X500Principal;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;
import javax.security.auth.login.AccountExpiredException;
import javax.security.auth.login.CredentialExpiredException;
import javax.security.auth.login.FailedLoginException;

import sun.security.x509.X500Name;
import sun.security.x509.X500Signer;
import sun.security.x509.CertificateValidity;
import sun.security.x509.CertificateVersion;
import sun.security.x509.CertificateSerialNumber;
import sun.security.x509.CertificateAlgorithmId;
import sun.security.x509.CertificateSubjectName;
import sun.security.x509.CertificateX509Key;
import sun.security.x509.CertificateIssuerName;
import sun.security.x509.X509CertImpl;
import sun.security.x509.AlgorithmId;
import sun.security.x509.X509CertInfo;

import org.apache.avalon.framework.activity.Initializable;
import org.apache.avalon.framework.logger.LogEnabled;
import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.phoenix.BlockContext;
import org.apache.avalon.framework.logger.Logger;

import net.osm.shell.Entity;
import net.osm.shell.DefaultEntity;
import net.osm.shell.Shell;
import net.osm.shell.Service;
import net.osm.util.IconHelper;
import net.osm.util.ExceptionHelper;
import net.osm.vault.Vault;
import net.osm.vault.VaultException;
import net.osm.vault.PrincipalEvent;
import net.osm.vault.PrincipalListener;
import net.osm.pki.pkcs.PKCS10;

/**
 * The <code>VaultEntity</code> class represents a keystore.
 *
 * @author  Stephen McConnell
 * @version 1.0 20 JUN 2001
 */
public class VaultEntity extends DefaultEntity implements DesktopVault
{

    //==========================================================
    // static
    //==========================================================

    private static final Random random = new Random();

    public static final X500Principal UNKNOWN_PRINCIPAL = 
      new X500Principal("CN=Unknown");

    private static final String path = "net/osm/agent/image/vault.gif";
    private static final ImageIcon icon = IconHelper.loadIcon( path );

    //================================================================
    // state
    //================================================================

   /**
    * LinkedList of principal listeners.
    */
    private final LinkedList listeners = new LinkedList();

   /**
    * Internal reference to the log channel.
    */
    private Logger log;

   /**
    * Internal reference to the configuration.
    */
    private Configuration configuration;

   /**
    * Internal calendar against which dates are evaluated.
    */
    private GregorianCalendar calendar = new GregorianCalendar();

   /**
    * Secure random.
    */
    SecureRandom secureRandom = new SecureRandom();

   /**
    * Key pair generator.
    */
    KeyPairGenerator keyPairGenerator;

   /**
    * The default keystore path.
    */
    String defaultKeystorePath = System.getProperty("user.home") + 
	System.getProperty("file.separator") +
	".keystore";

   /**
    * Default name of the vault.
    */
    private String name = "Vault";

   /**
    * Internal reference to the name of the root alias.
    */
    private String alias;

   /**
    * Internal reference to an in-memory keystore.
    */
    private KeyStore keystore;

   /**
    * Flag indicating modification of the keystore.
    */
    private boolean modifiedKeystore = false;

   /**
    * Internal reference to the supplied keystore password used to 
    * load and save the keystore.
    */
    private char[] password;

   /**
    * The keystore file path used during load and save to construct the 
    * file input and output streams.
    */
    private File file;

   /**
    * List of views.
    */
    private LinkedList views;

   /**
    * List of properties.
    */
    private LinkedList properties;

   /**
    * Containing window.
    */
    private Frame root;

   /**
    * Action to select a new or existing alternative keystore. 
    */
    private Action configureKeyStore;

   /**
    * List of actions supported by this instance.
    */
    private List actions;

   /**
    * Login action.
    */
    private Action login;

   /**
    * Logout action.
    */
    private Action logout;

   /**
    * Login context against which login and logout actions are performed, 	
    * and from which the security subject is established.
    */
    private LoginContext context;

   /**
    * The security subject.
    */
    private Subject subject;

   /**
    * The X500Principal established from the security subject.
    */
    private X500Principal principal = UNKNOWN_PRINCIPAL;

   /**
    * Application context
    */
    private BlockContext applicationContext;

   /**
    * Login policy file.
    */
    private File loginPolicyFile;

    //===================================================================
    // constructor
    //===================================================================

   /**
    * Creation of a new VaultEntity.
    */
    public VaultEntity( BlockContext context )
    {
        super( "Vault" );
        this.applicationContext = context;
    }

   /**
    * Creation of a new VaultEntity.
    */
    public VaultEntity( Frame root, BlockContext context )
    {
        super( "Vault" );
        this.root = root;
        this.applicationContext = context;
    }

    //================================================================
    // LogEnabled implementation
    //================================================================

   /**
    * Sets the logging channel.
    *
    * @param logger Logger to direct log entries to
    */ 
    public void enableLogging( final Logger logger )
    {
        log = logger;
    }

    //================================================================
    // Configurable implementation
    //================================================================
    
   /**
    * Configuration of the runtime environment based on a supplied 
    * Configuration arguments including the file path of the keystore 
    * and the keystore password.
    *
    * @param config keystore configuration block
    * @exception ConfigurationException if the supplied configuration is badly formed.
    */
    public void configure( final Configuration config )
    throws ConfigurationException
    {
        if( configuration != null ) throw new ConfigurationException(
		"Attempt to re-configure a static configuration.");
        try
	  {
            configuration = config;
		password = 
		  new String( configuration.getAttribute("keystorepass", "") ).toCharArray();
		alias = configuration.getAttribute("alias", System.getProperty("user.name", "alias") );
		String filename = configuration.getAttribute("file", defaultKeystorePath );
	      file = new File( filename );
		
		loginPolicyFile = new File( 
			applicationContext.getBaseDirectory(), 
			configuration.getAttribute("login","login.conf")
		);

		if( loginPolicyFile.exists() )
		{
		    System.setProperty( "java.security.auth.login.config", loginPolicyFile.getAbsolutePath() );
            }
		else
	      {
		    throw new VaultException("Unable to locate login policy configuration from path '" + 
			loginPolicyFile.toString() + "'." );
		}
        }
	  catch( Exception e )
        {
            throw new ConfigurationException("Failed to configure the vault entity.", e );
        }
    }


    //================================================================
    // Initializable
    //================================================================
    
   /**
    * Initialization is invoked by the framework during which 
    * the keystore is established.
    */
    public void initialize()
    throws Exception
    {
        super.initialize();

        //
        // setup the actions
        //

        try
        {
		BigInteger magic = getRandom(); // is this needed ?
            configureKeyStore = new ConfigurationWizard( this, magic, root, file );
            context = getLoginContext();
		login = new LoginAction( "Login ...", this );
		logout = new LogoutAction( "Logout", this );

	      configureKeyStore.setEnabled( true );
	      login.setEnabled( false );
	      logout.setEnabled( false );

        }
        catch (Exception e) 
        {
            throw new Exception("VaultEntity unable to initialize actions.", e );
        }

        if( !file.exists() ) 
        {

            //
            // if the keystore does not exist then this either a corrupted system
            // or a new installation - in either case, throw up the configuration
            // wizard
            //

		try
	      {
                configureKeyStore.actionPerformed( 
			new ActionEvent( root, 1000, "configure" ) );
		}
		catch( Exception e )
		{
		    throw new VaultException(
			"Unexpected exception while initiating configuration.", e );
		}
        }

        if( file.exists() )
        {
		try
	      {
		    login();
		}
		catch( Exception e )
		{
		    throw new VaultException(
			"Unexpected exception while executing login.", e );
		}
        }
    }

   /**
    * Returns an Action instances to be installed as 
    * menu items within the desktop preferences menu group.
    */
    public Action getPreferencesAction( )
    {
        return configureKeyStore;
    }

    //==========================================================
    // Vault
    //==========================================================

    public PKCS10 createPKCS10() 
    {
        throw new RuntimeException("not implemented because this class is out-of-date");
    }

   /**
    * Return the current X500 principal.
    * @return X500Principal corresponding to a 
    *     principal that has been authenticated by the vault, or 
    *     the default UNKNOWN_PRINCIPAL in the case of failed 
    *     a failed or null authentication.
    */
    public X500Principal getPrincipal()
    {
        return principal;
    }

   /**
    * Returns the public certificate paths of the principal. If no certificate
    * path can be located the operation returns an empty set.
    */
    public Set getCertificatePaths()
    {
	  if( subject == null ) throw new RuntimeException(
          "Vault, Security subject has not been initialized.");
	  return subject.getPublicCredentials( CertPath.class );
    }

   /**
    * Returns the first certificate path in the set of certificate paths.
    */
    public CertPath getCertificatePath( )
    {
	  Set set = getCertificatePaths();
        try
	  {
		Iterator iterator = set.iterator();
		while( iterator.hasNext() )
		{
		    return (CertPath) iterator.next();
		}
		return null;
        }
	  catch( Exception e )
	  {
		throw new RuntimeException(
		  "Vault, Unexpected exception while resolving first certificate path.", e );
	  }
    }


   /**
    * Add a PrincipalListener to the Vault.
    */
    public void addPrincipalListener( PrincipalListener listener )
    {
	  synchronized( listeners )
	  {
            listeners.add( listener );
        }
    }

   /**
    * Removes a PrincipalListener from the Vault.
    */
    public void removePrincipalListener( PrincipalListener listener )
    {
	  synchronized( listeners )
	  {
            listeners.remove( listener );
        }
    }

   /**
    * Proceses principal events by dispatching a PrincipalEvent to all 
    * registered listeners.
    */
    public synchronized void firePrincipalEvent( X500Principal principal )
    {
        firePrincipalEvent( new PrincipalEvent( this, principal ));
    }

   /**
    * Proceses principal events by dispatching a PrincipalEvent to all 
    * registered listeners.
    */
    public synchronized void firePrincipalEvent( PrincipalEvent event )
    {
	  synchronized( listeners )
	  {
	      Iterator iterator = listeners.iterator();
            while( iterator.hasNext() ) 
            {
                PrincipalListener listener = (PrincipalListener) iterator.next();
	  	    listener.principalChanged( event );
            }
        }
    }

   /**
    * Authenticates the user based on the underlying login 
    * configuration.
    */
    public boolean login( ) throws VaultException
    {

        if( context == null ) throw new VaultException(
	    "Login context has not been initialized." );

	  // the user has 3 attempts to authenticate successfully

	  for ( int i = 0; i < 3; i++ ) 
	  {
	      try
	      {
		    //
		    // attempt authentication
		    // if we return with no exception, the 
		    // the authentication succeeded and we 
		    // can return a valid subject
		    //

		    context.login();
		    login.setEnabled( false );
		    logout.setEnabled( true );
		    if( subject != context.getSubject() )
		    {
                    subject = context.getSubject();
			  
			  //
			  // get the X500 Principal that the subject represents
			  // and fire a principal change event
			  //

			  X500Principal p = resolvePrincipal( subject );
			  if( principal != p ) synchronized( principal )
			  {
			      principal = p;
				firePrincipalEvent( principal );
	              }
                }
		    return true;
	      }
	      catch (AccountExpiredException aee) 
	      {
	          JOptionPane.showMessageDialog( 
 			root, 
			"Your account has expired.  " +
			"\nPlease notify your administrator.",
			"Login Failure", 
			JOptionPane.ERROR_MESSAGE 
		    );
	          throw new VaultException( "Expired account.", aee );
	      } 
	      catch (CredentialExpiredException cee) 
	      {
	          JOptionPane.showMessageDialog( 
 			root, 
			"Your credentials have expired." +
			"\nPlease notify your administrator.",
			"Login Failure", 
			JOptionPane.ERROR_MESSAGE 
		    );
	          throw new VaultException( "Expired credentials.", cee );
	      } 
	      catch (FailedLoginException fle) 
	      {
		    try 
		    {
		        Thread.currentThread().sleep(3000);
		    }
		    catch (Exception e) 
		    {
		    }
	      }
	      catch (Exception e) 
	      {
		    String error = ExceptionHelper.packException(
			"Unable to complete login.", e );
	          JOptionPane.showMessageDialog( 
 			root, 
			error,
			"Login Failure", 
			JOptionPane.ERROR_MESSAGE 
		    );
	          throw new VaultException( "Unexpected login exception.", e );
	      }
	  }
        return false;
    }

   /**
    * Logout of a established security subject.  A side-effect of 
    * this operation is the raising of a PrincipalEvent referencing  
    * the default unknown principal.
    */
    public void logout()
    {
        if( context == null ) throw new RuntimeException(
	    "Login context has not been initialized." );

	  try
	  {
	      context.logout();
	  }
   	  catch( Exception e )
        {
        }

	  synchronized( principal )
	  {
	      subject = null;
	      principal = UNKNOWN_PRINCIPAL;
		login.setEnabled( true );
		logout.setEnabled( false );
		firePrincipalEvent( principal );
        }
    }

   /**
    * Return the <code>Subject</code>.
    */
    public Subject getSubject()
    {
        return subject;
    }


   /**
    * Resolve the X500 Principal from a supplied subject.
    */
    private X500Principal resolvePrincipal( Subject subject )
    {
	  Iterator iterator = subject.getPrincipals( X500Principal.class ).iterator();
	  while( iterator.hasNext() )
	  {
	      return (X500Principal) iterator.next();
	  }
        return null;
    }

   /**
    * Create a login context.
    */
    private LoginContext getLoginContext( ) throws Exception
    {
        //
        // Use the login module configured by the customer to 
	  // authenticate the user and establish the subject.
	  //

        javax.security.auth.login.Configuration c = 
		javax.security.auth.login.Configuration.getConfiguration();
	  if( c.getAppConfigurationEntry( this.getClass().getName() ).length == 0 )
        {
	      JOptionPane.showMessageDialog( 
 		   root, 
		   "Missing login configuration." +
		   "\nPlease check your login configuration.",
		   "Login Aborted", 
		   JOptionPane.ERROR_MESSAGE 
		);
	      return null;
        }

        return new LoginContext(
	    this.getClass().getName() , 
          new DialogCallbackHandler( root ));
    }

   /**
    * Creation of a new signed certificate.
    * @return X509Certificate the new certificate
    */
    public X509Certificate createCertificate( X509CertInfo info )
    throws VaultException
    {
        return null;
    }

   /**
    * Returns the signature algorith that will be used to sign certificates.
    */
    public String getSignatureAlgorithm()
    throws NoSuchAlgorithmException
    {
        return null;
    }

    //===================================================================
    // Entity impementation
    //===================================================================

   /**
    * Returns the renamable state of the Entity.
    * @return boolean true if this entity is renamable.
    */
    public boolean renameable()
    {
        return false;
    }

   /**
    * Returns a list of Action instances to be installed as 
    * action menu items within the desktop when the entity 
    * is selected.
    */
    public List getActions( )
    {
	  if( actions != null ) return actions;
	  actions = super.getActions();
	  actions.add( login );
	  actions.add( logout );
        return actions;
    }

   /**
    * Returns the removable state of the Value.
    * @return boolean true if this entity is removable (always returns false)
    */
    public boolean removable()
    {
        return false;
    }

   /**
    * Set the name of the entity to the supplied <code>String</code>.
    * The <code>Valut</code> implementation does nothing.
    *
    * @param name the new entity name
    */
    public void setName( String name ){}

   /**
    * Returns the name of the <code>Entity</code> as a <code>String</code>.
    */
    public String getName()
    {
        return name;
    }

    //==========================================================
    // internals
    //==========================================================

    public static BigInteger getRandom() 
    {
	  return new BigInteger( 6, random );
    }
}
