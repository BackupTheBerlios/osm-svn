
package net.osm.vault;

import java.awt.Component;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.util.Set;
import java.util.Arrays;
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

import java.security.Key;
import java.security.KeyPairGenerator;
import java.security.SecureRandom;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Principal;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.InvalidKeyException;
import java.security.SignatureException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Signature;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertPath;
import javax.security.auth.Subject;
import javax.security.auth.x500.X500Principal;
import javax.security.auth.x500.X500PrivateCredential;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;
import javax.security.auth.login.AccountExpiredException;
import javax.security.auth.login.CredentialExpiredException;
import javax.security.auth.login.FailedLoginException;
import javax.security.auth.callback.CallbackHandler;

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

import org.apache.avalon.framework.logger.Logger;
import org.apache.avalon.framework.logger.LogEnabled;
import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.activity.Initializable;
import org.apache.avalon.framework.context.Context;
import org.apache.avalon.framework.context.Contextualizable;
import org.apache.avalon.framework.context.ContextException;
import org.apache.avalon.phoenix.Block;
import org.apache.avalon.phoenix.BlockContext;

import net.osm.util.ExceptionHelper;
import net.osm.pki.pkcs.PKCS10;
import net.osm.pki.pkcs.PKCS10Wrapper;

import sun.security.x509.X509CertInfo;


/**
 * The <code>Vault</code> block provides support for the establishment 
 * of propergation of an authenticated X509 principal.
 *
 * @author  Stephen McConnell
 * @version 1.0 20 JUN 2001
 */
public final class LocalVault extends AbstractLogEnabled 
implements Block, Contextualizable, Configurable, Initializable, Vault
{

    //==========================================================
    // static
    //==========================================================

   /**
    * The default keystore path.
    */
    private static final String defaultKeystoreFilename = ".keystore";

    private static boolean trace = false;

    //================================================================
    // state
    //================================================================

   /**
    * LinkedList of principal listeners.
    */
    private final LinkedList listeners = new LinkedList();

   /**
    * Internal reference to the configuration.
    */
    private Configuration configuration;

   /**
    * Internal reference to the initalization state.
    */
    private boolean initialized;

   /**
    * Internal calendar against which dates are evaluated.
    */
    private GregorianCalendar calendar = new GregorianCalendar();

   /**
    * Secure random.
    */
    private SecureRandom secureRandom = new SecureRandom();

   /**
    * Key pair generator.
    */
    private KeyPairGenerator keyPairGenerator;

   /**
    * Certificate factory used to construct the CertChain instance.
    */
    private CertificateFactory factory;

   /**
    * Internal reference to the name of the root alias.
    */
    private String alias;

   /**
    * Internal reference to the keystore.
    */
    private KeyStore keystore;

   /**
    * Flag indicating modification of the keystore.
    */
    private boolean modifiedKeystore = false;

   /**
    * Internal reference to the supplied keystore password used to 
    * load and save the keystore.
    * This value is normally established during the configuration phase
    */
    private char[] password;

   /**
    * The keystore file path used to construct the 
    * file input and output streams.
    */
    private File file;

   /**
    * The distinguished name to use if we need to generate a private key.
    * This value is normally established during the configuration phase
    */
    private String subjectName;

   /**
    * The security subject established after the vault is loaded.
    */
    private Subject subject;

   /**
    * The X500Principal established from the security subject.
    */
    private X500Principal principal;

   /**
    * Keypair created during a self signed certificate generation process.
    */
    private KeyPair keyPair;

   /**
    * The public credentials certificate path.
    */
    private CertPath certificatePath;

   /**
    * Application context
    */
    private BlockContext context;

   /**
    * The login callback handler to be used to respond to the 
    * login context process. The value depends on the interactive of 
    * background mode we are running in.
    */
    private CallbackHandler callback;

    private boolean interactive = false;

    private LoginContext loginContext;

    //===================================================================
    // constructor
    //===================================================================

   /**
    * Creation of a new VaultEntity.
    */
    public LocalVault( )
    {
    }

    //=================================================================
    // Contextualizable
    //=================================================================

    public void contextualize( Context context ) throws ContextException
    {
	  getLogger().debug("contextualize");
	  if(!( context instanceof BlockContext )) throw new ContextException(
		"Supplied context does not implement BlockContext.");
	  this.context = (BlockContext) context;
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
        if( config == null ) throw new ConfigurationException(
		"Null configuration supplied to constructor.");

        getLogger().debug("configuration");
        configuration = config;
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
        getLogger().debug("initialization");

	  //
	  // the actions taken during the initalization phase depend on the 
	  // interactive nature of the session.  If its interative then 
	  // the establishment of the principal is achieved through a dialog
	  // containing a alias/password challenge, otherwise, the authentication
	  // is based on a supplied keystore files, alias and password under the 
	  // the configuration. 
	  //

	  interactive = configuration.getAttributeAsBoolean("interactive", interactive );
        if( !interactive ) 
	  {
		DefaultCallbackHandler cb = new DefaultCallbackHandler( configuration );
		cb.enableLogging( getLogger().getChildLogger("callback" ));
		callback = cb;
	  }
        else
	  {
		callback = new DialogCallbackHandler( null );
	  }

        boolean result = false;
        try
	  {
            loginContext = new LoginContext( Vault.class.getName(), callback );
	      result = login();
        }
	  catch( Throwable e )
        {
		final String error = "unable to establish a security subject";
		ExceptionHelper.printException( error, e );
		throw new Exception( error, e );
        }
        finally
        {
		if( result )
		{
                initialized = true;
                getLogger().debug("vault initalization sucessfull");
		    getPrivateKey();
		}
		else
		{
		    final String failure = "login was not successful";
		    throw new VaultException( failure );
		}
        }
    }

   /**
    * Authenticates the user based on the underlying login 
    * configuration.
    */
    public boolean login( ) throws VaultException
    {

        if( loginContext == null ) throw new VaultException(
	    "Login context has not been initialized." );

	  int max = 1;
        if( interactive ) max = 3;
	  int count = 0;
	  while( count < max )
	  {
	      try
	      {
	          loginContext.login();
		    break;
	      }
	      catch( LoginException loginException )
	      {
	  	    // if interactive then beep
	          count++;
		    final String fail = "attempted login failure - attempt: ";
		    if( getLogger().isWarnEnabled() ) getLogger().warn( fail + count );
		}
		catch( Throwable generalException )
	      {
		    final String unexpected = "unexpect error while handling login";
		    throw new VaultException( unexpected, generalException );
		}
		if( count >= max )
		{
		    final String suddenDeath = "exceeded maximum login attempt: ";
		    if( getLogger().isWarnEnabled() ) getLogger().warn( suddenDeath + count );
		    return false;
		}
        }

	  try
	  {
	      subject = loginContext.getSubject();
		X500Principal p = resolvePrincipal( subject );
		if( principal != p ) synchronized( this )
	      {
		    principal = p;
	          firePrincipalEvent( principal );
	      }
	  }
	  catch( Exception e )
	  {
		final String error = "unable to establish an X500 principal";
		throw new VaultException( error, e );
	  }
	  finally
	  {
		return true;
	  }
    }

    //==========================================================
    // Vault
    //==========================================================

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
    * Return the first certificate path in the subjects's public credentials list.
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
    * Return the security principal.
    * @return X500Principal the security principal
    */
    public X500Principal getPrincipal() throws CertificateException
    {
        return principal;
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
    * Creation of a new certification request using a self signed certificate 
    * packaged as a PKCS10 request.
    * @return PKSC10 the certificate request
    */
    public PKCS10 createPKCS10()
    {
	  if( subject == null ) throw new IllegalStateException(
	    "cannot create a security subject without a valid login" );
        return new PKCS10Wrapper( subject );
    }

    //==========================================================
    // CertificateFactory
    //==========================================================

   /**
    * Creation of a new signed certificate.
    * @return X509Certificate the new certificate
    */
    public X509Certificate createCertificate( X509CertInfo info )
    throws VaultException
    {
        try
	  {
            sun.security.x509.X509CertImpl certificate = 
		  new sun.security.x509.X509CertImpl( info );
            certificate.sign( getPrivateKey(), getSignatureAlgorithm() );
            return certificate;
        }
	  catch( Throwable e )
	  {
            final String error = "unable to create certificate";
            throw new VaultException( error, e );
	  }
    }

    private PrivateKey getPrivateKey()
    {
	  if( subject == null ) throw new IllegalStateException("suject has not been established");
        Iterator iterator = subject.getPrivateCredentials().iterator();
        while( iterator.hasNext() )
        {
	      Object next = iterator.next();
		if( next instanceof X500PrivateCredential)
		{
		    return ((X500PrivateCredential)next).getPrivateKey();
            }
        }
	  return null;
    }

   /**
    * Returns the signature algorith that will be used to sign certificates.
    */
    public String getSignatureAlgorithm()
    throws NoSuchAlgorithmException
    {
        return getSignatureAlgorithm( getPrivateKey() );
    }

   /**
    * Returns the signature algorith that will be used to sign certificates
    * using the private key as the base for determination of the algorithm.
    */
    private String getSignatureAlgorithm( Key key ) 
    throws NoSuchAlgorithmException
    {
        String algorithm = key.getAlgorithm();
	  String signatureForm = null;
	  if( algorithm.equalsIgnoreCase("DSA") || algorithm.equalsIgnoreCase("DSS") )
	  {
            signatureForm = "SHA1WithDSA";
	  }
	  else if( algorithm.equalsIgnoreCase("RSA") )
	  {
            signatureForm = "MD5WithRSA";
	  }
	  else
	  {
	      String error = "Cannot resolve a signature form from the algorithm '" + algorithm + "'.";
	      throw new NoSuchAlgorithmException( error );
	  }
	  return signatureForm;
    }

    //==========================================================
    // internals
    //==========================================================

    private boolean isaKeystore( File file )
    {
        int n = 0;
        FileInputStream s = null;
        boolean result = false;
        KeyStore k = null;
        try
	  {
            k = KeyStore.getInstance(KeyStore.getDefaultType());
		if( file != null ) if( file.exists() ) s = new FileInputStream( file );
	      if( trace ) System.out.println("file: " + file );
		if( trace ) System.out.println("k: " + k );
		if( trace ) System.out.println("s: " + s );
            k.load( s, null );
		result = true;
	  }
        catch( Exception e )
        {
		result = false;
        }
        finally
        {
            if( s != null ) try
	      {
		    s.close();
                s = null;
		}
	      catch( Exception e )
		{
            }
            k = null;
        }

        if( trace ) System.out.println("validation: " + result );
        return result;
    }

    private void loadKeystore( ) throws VaultException
    {
        if( getLogger().isDebugEnabled() ) getLogger().debug( "loading keystore" );
        boolean result = false;
        FileInputStream stream = null;
        try
        {
            if( file.exists() ) stream = new FileInputStream( file );
            keystore = KeyStore.getInstance( KeyStore.getDefaultType() );
		keystore.load( stream, password );
            if( getLogger().isDebugEnabled() ) getLogger().debug( "keystore loading ok" );
        }
        catch (Exception e) 
        {
		final String error = "Unable to load keystore from ";
            if( getLogger().isErrorEnabled() ) getLogger().error( error + file, e);
		closeStream( stream );
	      stream = null;
		keystore = null;
		throw new VaultException( error + file, e );
	  }
    }

    private void saveKeystore() throws VaultException
    {
        if( getLogger().isDebugEnabled() ) getLogger().debug( "saving keystore" );
        FileOutputStream stream = null;
        try
        {
            stream = new FileOutputStream( file );
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

   /**
    * Generate a self-signed certificate.
    *
    * @param keyType - type of key to generate (DES, DSS or RSA)
    * @param size - size of the generated key
    * @param subject - name of subject certificate
    * @param date - date on which the certificate becomes valid
    * @param days - period of validity in days
    * @return X509Certificate self signed certificate
    * @exception CertificateException
    * @exception InvalidKeyException
    * @exception SignatureException
    * @exception NoSuchAlgorithmException
    * @exception NoSuchProviderException
    */
    public X509Certificate getSelfCertificate( 
      String keyType, int size, X500Principal subject, Date date, int days )
    throws Exception
    {
        if( getLogger().isDebugEnabled() ) getLogger().debug( "creating self-signed certificate" );
        try
        {

		String sigAlgorithm = algorithmFromKeyType( keyType );

		//
		// establish the validity period
		// (need to set the callendar current date based on the supplied
            // value - not implemented yet - just using an offset of n days )
            //

		GregorianCalendar calendar = new GregorianCalendar();
		calendar.add( GregorianCalendar.DAY_OF_YEAR, days );
		Date expires = calendar.getTime();
            CertificateValidity certificatevalidity = 
			new CertificateValidity( date, expires );

		//
		// generate the keypair, name and signer
		//

		keyPair = generateKeyPair( keyType, size );
            X500Name name = new X500Name( subject.getName() );
            X500Signer x500signer = getSigner( name, sigAlgorithm, keyPair.getPrivate() );
            int serial = (int)( date.getTime() / 1000L );
		if( getLogger().isDebugEnabled() ) getLogger().debug("serial number: " + serial );

		//
		// prepare certificate info block
		//

            X509CertInfo info = new X509CertInfo();
            info.set("version", new CertificateVersion( CertificateVersion.V3 ) );
            info.set("serialNumber", new CertificateSerialNumber( serial ));
            info.set("algorithmID", new CertificateAlgorithmId( x500signer.getAlgorithmId() ) );
            info.set("subject", new CertificateSubjectName( name ) );
            info.set("key", new CertificateX509Key( keyPair.getPublic() ));
            info.set("validity", certificatevalidity );
            info.set("issuer", new CertificateIssuerName( x500signer.getSigner() ) );

		//
		// generate, sign and return certificate
		//

            X509CertImpl certificate = new X509CertImpl(info);
            certificate.sign( keyPair.getPrivate(), sigAlgorithm );
            return certificate;
        }
        catch( Exception e )
        {
            throw new Exception(
			"Problem encountered while creating self-signed certificate", e );
        }
    }

   /**
    * Generate a new key-pair.
    *
    * @param keyType the type of the key (DSA, DSS or RSA)
    * @param keySize the size of the key (1024)
    * @return KeyPair the public and private key pair
    */
    private KeyPair generateKeyPair( String keyType, int keySize ) throws InvalidKeyException
    {
        if( getLogger().isDebugEnabled() ) getLogger().debug( "generating keypair" );
        KeyPair keypair;
        try
        {
            if( keyPairGenerator == null ) keyPairGenerator = 
			KeyPairGenerator.getInstance( keyType );
            keyPairGenerator.initialize( keySize, secureRandom );
            keypair = keyPairGenerator.generateKeyPair();
        }
        catch(Exception exception)
        {
            throw new IllegalArgumentException(exception.getMessage());
        }
        return keypair;
    }

   /**
    * Creates a certificate signer given the supplied parameters.
    *
    * @param x500Name principal signing the certificate
    * @param algorithm the signature algorithm to be used to generate the 
    *        the signer - may be one of the following:</br>
    *        <ul>
    *        <li>SHA1withDSA: The DSA with SHA-1 signature algorithm which 
    *            uses the SHA-1 digest algorithm and DSA to create and verify 
    *            DSA digital signatures as defined in FIPS PUB 186. </li>
    *        <li>MD2withRSA: The MD2 with RSA Encryption signature algorithm 
    *            which uses the MD2 digest algorithm and RSA to create and 
    *            verify RSA digital signatures as defined in PKCS#1. </li>
    *        <li>MD5withRSA: The MD5 with RSA Encryption signature algorithm 
    *            which uses the MD5 digest algorithm and RSA to create and 
    *            verify RSA digital signatures as defined in PKCS#1. </li>
    *        <li>SHA1withRSA: The signature algorithm with SHA-1 and the 
    *            RSA encryption algorithm as defined in the OSI 
    *            Interoperability Workshop, using the padding conventions 
    *            described in PKCS #1. </li>
    *        </ul>
    * @param privateKey the private key to be used by the signer
    */ 
    private X500Signer getSigner( X500Name x500name, String algorithm, PrivateKey privateKey )
    throws InvalidKeyException, NoSuchAlgorithmException
    {
        Signature signature = Signature.getInstance( algorithm );
        signature.initSign( privateKey );
        return new X500Signer( signature, x500name );
    }

   /**
    * Get the signature for for a given algorithm identifier.
    *
    * @param type a string corresponding to <code>DSA</code>, 
    *             <code>DSS</code> or <code>RSA</code>.
    * @return String algorithm form corresponding to 
    *             <code>SHA1WithDSA</code> or <code>MD5WithRSA</code>
    * @exception Exception if the supplied type is unknown.
    */
    public String algorithmFromKeyType( String keyType ) throws Exception
    {
        String form = "";
	  if( keyType.equalsIgnoreCase("DSA") || keyType.equalsIgnoreCase("DSS") )
	  {
            form = "SHA1WithDSA";
        }
	  else if( keyType.equalsIgnoreCase("RSA") )
	  {
            form = "MD5WithRSA";
	  }
	  else
	  {
	      String error = "Signature algorithm does not match DSA, DSS or RSA";
	      throw new Exception( error );
	  }
        return form;
    }

}
