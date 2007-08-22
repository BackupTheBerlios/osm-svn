
package net.osm.shell.vault;

import java.util.Date;
import java.util.GregorianCalendar;
import java.awt.Component;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.Timer;
import javax.swing.AbstractButton;

import java.security.KeyPairGenerator;
import java.security.SecureRandom;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Principal;
import java.security.InvalidKeyException;
import java.security.SignatureException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Signature;
import java.security.KeyStore;
import java.security.cert.X509Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateEncodingException;
import javax.security.auth.x500.X500Principal;

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

import net.osm.util.ExceptionHelper;
import net.osm.shell.control.activity.Activity;
import net.osm.shell.control.activity.ActivityCallback;
import net.osm.shell.control.activity.ComponentRunner;
import net.osm.pki.pkcs.PKCS10;
import net.osm.pki.pkcs.PKCS10Wrapper;


/**
 * The <code>KeyGeneratorAction</code> class handles the generation of a new 
 * public and private key pair.
 *
 * @author  Stephen McConnell
 * @version 1.0 20 JUN 2001
 */
public class KeyGeneratorActivity extends AbstractAction implements Activity
{

    //==========================================================
    // state
    //==========================================================

    //private ProgressMonitor progressMonitor;
    private ActivityCallback callback;
    private Timer timer;

   /**
    * Secure random.
    */
    private SecureRandom secureRandom = new SecureRandom();

   /**
    * Key pair generator.
    */
    private KeyPairGenerator keyPairGenerator;

   /**
    * The root window.
    */
    private Component root;

   /**
    * The result of the action.
    */
    private X509Certificate certificate;

   /**
    * X500PrincipalAction action from which the X500Principal is resolvable.
    */
    private X500PrincipalAction action;

   /**
    * The principal.
    */
    private X500Principal subject;

   /**
    * The alias name under which a new key pair will be stored.
    */
    private String alias;

   /**
    * Key password or keystore password.
    */
    private char[] password;

   /**
    * The keystore.
    */
    private KeyStore keystore;

   /**
    * Keypair created by the activity.
    */
    private KeyPair keyPair;

    //==========================================================
    // constructor
    //==========================================================

   /**
    * Creates a new KeyGeneratorActivity that will create an 
    * key pair based on an X500 Principal established under the 
    * supplied action.
    */
    public KeyGeneratorActivity( String name, Component root, X500PrincipalAction action )
    {
        super( name );
        this.root = root;
        this.action = action;
    }

    protected void setKeystore( KeyStore keystore )
    {
        this.keystore = keystore;
	  verifyArguments();
    }

    protected void setAlias( String alias )
    {
        this.alias = alias;
	  verifyArguments();
    }

    public void setPrincipal( X500Principal subject )
    {
        this.subject = subject;
	  verifyArguments();
    }

    public void setPassword( char[] password )
    {
        this.password = password;
	  verifyArguments();
    }

    private void verifyArguments()
    {
        if( keystore == null ) return;
        if( alias == null ) return;
        if( subject == null ) return;
        if( password == null ) return;
        if( callback != null ) callback.setEnabled( true );
    }

    //==========================================================
    // ActionListener
    //==========================================================

   /**
    * Not used.
    */
    public void actionPerformed( ActionEvent event )
    {
    }
    
    //==========================================================
    // Activity
    //==========================================================

   /**
    * Declare the callback handler to the activity
    * @see ActivityCallback
    * @see net.osm.shell.control.activity.DiodeDialog
    */
    public void setCallback( ActivityCallback callback )
    {
        this.callback = callback;
        callback.setEnabled( subject != null );
    }

   /**
    * Called by DiodeDialog's actionPerfored method to initiate
    * execution of the key generation process.
    */
    public void execute( ) throws Exception
    {
        try 
	  {

		if( callback != null ) 
	      {
		    callback.setMessage("Configuration");
		    callback.setProgress(0);
            }

		//
		// prepare arguments by calling back to the 
		// X500 Principal form which will invoke the setPrincipal,
	      // setAlias and setKeystore methods on this instance
		//

            action.requestInitialization();
            String keyType = "DSA";
            int size = 1024;

		//
		// generate keypair (side effects include establishing
	      // the keypair variable)
		//

            certificate = getSelfCertificate( 
              callback, keyType, size, subject, new Date(), 90 );

		//
		// register certificate
		//

		if( callback != null )
		{
		    callback.setProgress(3);
		    callback.setMessage("Updating keystore.");
            }

		try
	      {
		    X509Certificate[] certificates = new X509Certificate[]{ certificate };
	          keystore.setKeyEntry( alias, keyPair.getPrivate(), password, certificates );
                putValue("key", new KeyStatus( alias, true ));
		}
		catch( Exception keyException )
		{
		    ExceptionHelper.printException("Unable to store key.", keyException );
                putValue("key", new KeyStatus( alias, false )); 
		}

		//
		// create and store the PKCS10 certificate request
		//

		if( callback != null )
		{
		    callback.setProgress(4);
		    callback.setMessage("Generating PKCS10 request.");
            }
		try
	      {
                Thread.sleep(1000); 
		    //PKCS10 request = new PKCS10Wrapper( subject );
		}
		catch( Exception keyException )
		{
		    ExceptionHelper.printException("Unable to store key.", keyException );
                putValue("key", new KeyStatus( alias, false )); 
		}

            //
            // signal completion
            //

		callback.setMessage("Complete");
		if( callback != null ) 
		{
                try
	          {
                    Thread.sleep(1000); 
		    }
		    catch (InterruptedException e) {}
            }
		callback.setProgress(5);

        }
        catch( Exception e )
	  {
            ExceptionHelper.printException("New Self Signed Certificate", e );
            putValue("key", new KeyStatus( alias, false )); 
        }
    }

    /**
     * Returns the result of the activity execution.
     */
    public Object getResult()
    {
        return certificate;
    }


    //===================================================================
    // impementation
    //===================================================================

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
      ActivityCallback callback, String keyType, int size, X500Principal subject, Date date, int days )
    throws Exception
    {
        try
        {
		if( callback != null ) callback.setMessage("Keypair generation");
		if( callback != null ) callback.setProgress(1);

		String sigAlgorithm = algorithmFromKeyType( keyType );

		//
		// establish the validity period
		// (need to set the callendar current date based on the supplied
            // value - not implemented yet - assumes now for offset )
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

		//
		// prepare certificate info block
		//

		if( callback != null ) callback.setMessage("Certificate Info preparation");
		if( callback != null ) callback.setProgress(2);

            X509CertInfo info = new X509CertInfo();
            info.set("version", new CertificateVersion( CertificateVersion.V3 ) );
            info.set("serialNumber", new CertificateSerialNumber((int)(date.getTime() / 1000L)) );
            AlgorithmId algorithmid = x500signer.getAlgorithmId();
            info.set("algorithmID", new CertificateAlgorithmId( algorithmid ) );
            info.set("subject", new CertificateSubjectName( name ) );
            info.set("key", new CertificateX509Key( keyPair.getPublic() ));
            info.set("validity", certificatevalidity );
            info.set("issuer", new CertificateIssuerName( x500signer.getSigner() ) );

		//
		// generate, sign and return certificate
		//

		if( callback != null ) callback.setMessage("Signing");
		if( callback != null ) callback.setProgress(3);
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
