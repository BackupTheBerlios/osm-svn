
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
import java.net.URL;

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
import org.apache.avalon.framework.activity.Disposable;
import org.apache.avalon.framework.context.Context;
import org.apache.avalon.framework.context.Contextualizable;
import org.apache.avalon.framework.context.ContextException;
import org.apache.avalon.framework.CascadingException;
import org.apache.avalon.framework.CascadingRuntimeException;

import sun.security.x509.X509CertInfo;

import javax.swing.JFrame;

/**
 * <p>The <code>DefaultVault</code> block provides support for the establishment 
 * of propergation of an X509 principal using an underlying JAAS 
 * authentication mechanism.  The <code>Vault</code> assumes that an 
 * authentication profile will be established by the client application using
 * a keystore login configuration.</p>
 *
 * <p><table border="1" cellpadding="3" cellspacing="0" width="100%">
 * <tr bgcolor="#ccccff">
 * <td colspan="2"><b>Component Lifecycle</b></td>
 * <tr><td width="20%"><b>Phase</b></td><td><b>Description</b></td></tr>
 *
 * <tr><td valign="top">Configurable</td>
 * <td><p>A configuration may declare the attribute <code>interactive</code> to be 
 * be <code>TRUE</code> in which case the authentication process will be based 
 * on an interactive alias/password challenge.  If the attrribute is <code>FALSE</code>
 * or undefined, the default behaviour of background authentication will apply.</p>
 * <p>Example Configuration</p>
 * <pre>  &lt;vault interactive="true"/&gt;</pre>
 * </td></tr>
 *
 * <tr><td valign="top">Contextualizable</td>
 * <td>Optional phase that may be used to supply a <code>{@link VaultContext}</code> to 
 * the component, resulting in the establishment of an authentication handler that 
 * uses the context based values for authentication responses. If the interactive
 * mode of authentication is enabled, the context argument will be ignored.</td></tr>
 *
 * <tr><td valign="top">Initializable</td>
 * <td>Resolution of the authentication mode to apply.  Authentication modes include
 * the following:
 *   <table border="0" cellpadding="3" cellspacing="0" width="100%">
 *   <tr><td width="20%" valign="top">Mode</td><td>Description</td></tr>
 *   <tr><td valign="top">Interactive</td><td>Authentication is achieved through
 *   an interactive challenge dialog under alias, password and option private key password
 *   can be supplied.</td></tr>
 *   <tr><td  valign="top">Contextual</td><td>Authentication is achieved through
 *   context values for alias and passwords.</td></tr>
 *   <tr><td  valign="top">JAAS</td><td>Authentication is achieved using parameters
 *   supplied under the JAAS login configuration.</td></tr>
 *   </table>
 * </td></tr>
 * <tr><td width="20%" valign="top">Disposable</td>
 * <td>Cleanup and disposal of state members.</td></tr>
 * </table>
 *
 * @see net.osm.vault
 *
 * @author  Stephen McConnell
 * @version 1.0 20 JUN 2001
 */
public final class DefaultVault extends AbstractLogEnabled
implements LogEnabled, Configurable, Contextualizable, Initializable, Disposable, Vault
{

    //==========================================================
    // static
    //==========================================================

    private static boolean trace = false;

    //================================================================
    // state
    //================================================================

   /**
    * LinkedList of principal listeners.
    */
    private final LinkedList m_listeners = new LinkedList();

   /**
    * Internal reference to the configuration.
    */
    private Configuration m_config;

   /**
    * Secure random.
    */
    private SecureRandom m_secure_random = new SecureRandom();

   /**
    * Key pair generator.
    */
    private KeyPairGenerator m_key_pair_generator;

   /**
    * Internal reference to the keystore.
    */
    private KeyStore m_keystore;

   /**
    * The security subject established after the vault is loaded.
    */
    private Subject m_subject;

   /**
    * The X500Principal established from the security subject.
    */
    private X500Principal principal;

   /**
    * Vault context.
    */
    private VaultContext m_context;

   /**
    * Flag interactive login in which case max failure count is set to
    * 3 instead of 1.
    */
    private boolean m_interactive = false;

   /**
    * The login context.
    */
    private LoginContext m_login_context;

   /**
    * A root frame when invoking an interactive login that can be 
    * disposed of on component disposal.
    */
    private JFrame m_frame;

    //=================================================================
    // Contextualizable
    //=================================================================

   /**
    * Optional provision of a application context.  If the context is 
    * an instance of <code>ValutContext</code>, the context will be used 
    * to support background authentication, otherwise the context value
    * will be ignored.
    * @param context the application context
    */
    public void contextualize( Context context ) throws ContextException
    {
        if( getLogger() == null ) throw new IllegalStateException(
		"Logging channel unavailable.");

	  getLogger().debug("contextualize ("+ ( context instanceof VaultContext ) + ")" );
        if( context instanceof VaultContext ) m_context = (VaultContext) context;
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
        if( config == null ) throw new ConfigurationException(
		"Null configuration supplied to constructor.");

        if( getLogger() == null ) throw new IllegalStateException(
		"Logging channel unavailable.");

        getLogger().debug("configuration");
        m_config = config;
    }

    //================================================================
    // Initializable
    //================================================================
    
   /**
    * Initialization is invoked by the framework during which the 
    * appropriate loging context is established.
    */
    public void initialize()
    throws Exception
    {
        if( getLogger() == null ) throw new IllegalStateException(
		"Logging channel unavailable.");

        if( m_config == null ) throw new IllegalStateException(
		"Configuration unavailable.");

        getLogger().debug("initialization");

        if( m_config.getAttributeAsBoolean("interactive", false ) )
        {
            //
            // interactive authentication
            //

            m_frame = new JFrame();
            m_login_context = new LoginContext( 
              Vault.class.getName(), new DialogCallbackHandler( m_frame ));
            m_interactive = true;
            getLogger().debug("interactive authentication enabled");
        }
        else
        {
            //
            // background authentication
            //

            if( m_context != null )
            {
                //
                // client has supplied an explicit context argument
                // so we need to setup the loging handler to provide
                // supplied login arguments
                //

		    DefaultCallbackHandler cb = new DefaultCallbackHandler( m_context );
		    cb.enableLogging( getLogger().getChildLogger("callback" ));
                m_login_context = new LoginContext( Vault.class.getName(), cb );
                getLogger().debug("context based authentication enabled");
            }
            else
            {
                //
                // create a login context without a handler (everything must be 
                // declared in a JAAS login configuration
                //

                m_login_context = new LoginContext( Vault.class.getName() );
                getLogger().debug("JAAS default authentication enabled");
            }
        }
    }

    //================================================================
    // Disposable
    //================================================================

   /**
    * Dispose of the component.
    */
    public void dispose()
    {
        if( getLogger().isDebugEnabled() ) getLogger().debug("disposal");
        if( m_frame != null )
        {
            m_frame.dispose();
        }    
    }

    //================================================================
    // Vault
    //================================================================

   /**
    * Authenticates the user based on the login context established
    * during component initialization.
    */
    public void login( ) throws LoginException
    {

        if( m_login_context == null ) throw new VaultRuntimeException(
	    "Login context has not been initialized." );

	  int max = 1;
        if( m_interactive ) max = 3;
	  int count = 0;

	  while( count < max )
	  {
	      try
	      {
	          m_login_context.login();
		    break;
	      }
	      catch( LoginException loginException )
	      {
	  	    // if m_interactive then beep
	          count++;
		    final String fail = "attempted login failure - attempt: ";
		    if( getLogger().isWarnEnabled() ) getLogger().warn( fail + count );
		}

		if( count >= max )
		{
		    final String suddenDeath = "exceeded maximum login attempt: ";
		    if( getLogger().isWarnEnabled() ) getLogger().warn( suddenDeath + count );
		    throw new LoginException( suddenDeath );
		}
        }

	  try
	  {
	      m_subject = m_login_context.getSubject();
		X500Principal p = resolvePrincipal( m_subject );
		if( principal != p ) synchronized( this )
	      {
		    principal = p;
	          firePrincipalEvent( principal );
	      }
	  }
	  catch( Exception e )
	  {
		final String error = "unable to establish an X500 principal";
		throw new VaultRuntimeException( error, e );
	  }
    }

   /**
    * Invoke logout.
    */
    public void logout( ) throws LoginException
    {
        m_login_context.logout();
        m_subject = null;
        principal = null;
        synchronized( this )
	  {
	      firePrincipalEvent( principal );
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
	  if( m_subject == null ) throw new RuntimeException(
          "Vault, Security subject has not been initialized.");
	  return m_subject.getPublicCredentials( CertPath.class );
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
		throw new CascadingRuntimeException(
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
    * Return the security subject.
    * @return Subject the security subject.
    */
    public Subject getSubject()
    {
        return m_subject;
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
	  synchronized( m_listeners )
	  {
            m_listeners.add( listener );
        }
    }

   /**
    * Removes a PrincipalListener from the Vault.
    */
    public void removePrincipalListener( PrincipalListener listener )
    {
	  synchronized( m_listeners )
	  {
            m_listeners.remove( listener );
        }
    }

   /**
    * Proceses principal events by dispatching a PrincipalEvent to all 
    * registered listeners.
    */
    private synchronized void firePrincipalEvent( X500Principal principal )
    {
        firePrincipalEvent( new PrincipalEvent( this, principal ));
    }

   /**
    * Proceses principal events by dispatching a PrincipalEvent to all 
    * registered listeners.
    */
    private synchronized void firePrincipalEvent( PrincipalEvent event )
    {
	  synchronized( m_listeners )
	  {
	      Iterator iterator = m_listeners.iterator();
            while( iterator.hasNext() ) 
            {
                PrincipalListener listener = (PrincipalListener) iterator.next();
	  	    listener.principalChanged( event );
            }
        }
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
	  if( m_subject == null ) throw new IllegalStateException("subject has not been established");
        Iterator iterator = m_subject.getPrivateCredentials().iterator();
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
	      String error = "Cannot resolve a signature form from the algorithm '" 
              + algorithm + "'.";
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
        if( m_context == null ) throw new VaultException(
          "Cannot load keystore without vault context.");

        boolean result = false;
        FileInputStream stream = null;
        try
        {
            if( m_context.getKeystorePath().exists() ) stream = new FileInputStream( 
              m_context.getKeystorePath() );
            m_keystore = KeyStore.getInstance( KeyStore.getDefaultType() );
		m_keystore.load( stream, m_context.getChallenge().toCharArray() );
            if( getLogger().isDebugEnabled() ) getLogger().debug( "keystore loading ok" );
        }
        catch (Exception e) 
        {
		final String error = "Unable to load keystore from " + m_context.getKeystorePath();
		closeStream( stream );
	      stream = null;
		m_keystore = null;
		throw new VaultException( error, e );
	  }
    }

    private void saveKeystore() throws VaultException
    {
        if( getLogger().isDebugEnabled() ) getLogger().debug( "saving keystore" );
        FileOutputStream stream = null;
        try
        {
            stream = new FileOutputStream( m_context.getKeystorePath() );
            m_keystore.store( stream, m_context.getChallenge().toCharArray() );
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
        if( getLogger().isDebugEnabled() ) getLogger().debug( 
           "creating self-signed certificate" );
        try
        {

		String sigAlgorithm = algorithmFromKeyType( keyType );

		//
		// establish the validity period
		// (need to set the calendar current date based on the supplied
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

		KeyPair keyPair = generateKeyPair( keyType, size );
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
            throw new CascadingException(
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
            if( m_key_pair_generator == null ) m_key_pair_generator = 
			KeyPairGenerator.getInstance( keyType );
            m_key_pair_generator.initialize( keySize, m_secure_random );
            keypair = m_key_pair_generator.generateKeyPair();
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
    private String algorithmFromKeyType( String keyType ) throws Exception
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
	      throw new CascadingException( error );
	  }
        return form;
    }

}
