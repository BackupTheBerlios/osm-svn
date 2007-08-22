/*
 * @(#)RequestCertificateServer.java
 *
 * Copyright 2001 OSM SARL. All Rights Reserved.
 * 
 * This software is the proprietary information of OSM SARL.  
 * Use is subject to license terms.
 * 
 * @author  Stephen McConnell
 * @version 1.0 28/12/2001
 */

package net.osm.pki.authority;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Iterator;
import java.util.GregorianCalendar;
import java.security.PublicKey;
import java.security.InvalidKeyException;
import java.security.SignatureException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.cert.X509Certificate;
import java.security.cert.CertificateException;

import sun.security.x509.AlgorithmId;
import sun.security.x509.X509CertInfo;
import sun.security.x509.CertificateValidity;
import sun.security.x509.CertificateVersion;
import sun.security.x509.CertificateSerialNumber;
import sun.security.x509.CertificateAlgorithmId;
import sun.security.x509.CertificateSubjectName;
import sun.security.x509.CertificateX509Key;
import sun.security.x509.CertificateIssuerName;

import org.apache.avalon.framework.activity.Disposable;
import org.apache.avalon.framework.activity.Initializable;
import org.apache.avalon.framework.activity.Startable;
import org.apache.avalon.framework.context.Context;
import org.apache.avalon.framework.context.Contextualizable;
import org.apache.avalon.framework.context.ContextException;
import org.apache.avalon.framework.component.Component;
import org.apache.avalon.framework.component.ComponentManager;
import org.apache.avalon.framework.logger.Logger;
import org.apache.avalon.framework.logger.LogEnabled;
import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.component.ComponentManager;
import org.apache.avalon.framework.component.ComponentException;
import org.apache.avalon.framework.component.Composable;
import org.apache.avalon.phoenix.BlockContext;
import org.apache.avalon.phoenix.Block;

import org.omg.CORBA_2_3.ORB;
import org.omg.CORBA.Any;
import org.omg.CORBA.LocalObject;
import org.omg.CORBA.Policy;
import org.omg.PortableServer.ServantActivator;
import org.omg.CosPersistentState.NotFound;
import org.omg.CosPersistentState.StorageObject;
import org.omg.PortableServer.ServantLocator;
import org.omg.PortableServer.Servant;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.ServantLocatorPackage.CookieHolder ;
import org.omg.PortableServer.ForwardRequest;
import org.omg.PortableServer.POAHelper;
import org.omg.PortableServer.IdAssignmentPolicyValue;
import org.omg.PortableServer.RequestProcessingPolicyValue;
import org.omg.PortableServer.ImplicitActivationPolicyValue;
import org.omg.PortableServer.LifespanPolicyValue;
import org.omg.PortableServer.IdUniquenessPolicyValue;
import org.omg.PortableServer.ServantRetentionPolicyValue;
import org.omg.CosPersistentState.NotFound;
import org.omg.CosPersistentState.Connector;
import org.omg.CosPersistentState.Session;
import org.omg.CosPersistentState.StorageObject;

import org.omg.PKI.UnknownEncoding;
import org.omg.PKI.CertificateRequest;
import org.omg.PKI.UnknownContinue;
import org.omg.PKI.PKIXCMCConfirm;
import org.omg.PKI.Continue;
import org.omg.PKI.EncodedData;
import org.omg.PKI.PKCS10CertificateRequest;
import org.omg.PKI.DEREncoding;
import org.omg.PKI.PKISuccess;
import org.omg.PKI.Certificate;
import org.omg.PKI.CertificateListHolder;
import org.omg.PKI.ContinueHolder;

import org.omg.PKIAuthority.RequestCertificateManager;
import org.omg.PKIAuthority.RequestCertificateManagerPOA;
import org.omg.PKIAuthority.RequestCertificateManagerHelper;
import org.omg.PKIAuthority.UnsupportedTypeException;
import org.omg.PKIAuthority.UnsupportedEncodingException;
import org.omg.PKIAuthority.MalformedDataException;

import net.osm.pki.authority.RequestCertificateStorage;
import net.osm.pki.authority.RequestCertificateStorageHome;
import net.osm.pki.authority.RequestCertificateStorageHomeBase;
import net.osm.pki.base.ContinueBase;
import net.osm.pki.pkcs.PKCS10;
import net.osm.pki.pkcs.PKCS10Wrapper;

import net.osm.pss.PersistanceHandler;
import net.osm.pss.PSSConnectorService;
import net.osm.pss.PSSSessionService;
import net.osm.orb.ORBService;
import net.osm.orb.ObjectIdentifierService;
import net.osm.vault.Vault;
import net.osm.vault.VaultException;
import net.osm.pki.base.X500Name;

/**
 * The abstract <code>AbstractResourceServer</code> block declares services supporting the 
 * creation of new <code>AbstractResource</code> references and the handling incomming 
 * resource requests.
 *
 * @author <a href="mailto:mcconnell@osm.net">Stephen McConnell</a>
 */

public class RequestCertificateServant extends RequestCertificateManagerPOA
implements LogEnabled, Composable, Configurable, Initializable, Disposable
{

    //============================================================================
    // static  
    //============================================================================

    private static final String PSDL = 
      "PSDL:osm.net/pki/authority/RequestCertificateStorageHomeBase:1.0";

   /**
    * Empty endoded data structure used to fill storage slot during 
    * storage instance initialization.
    */
    private static final EncodedData emptyData = 
	new EncodedData( UnknownEncoding.value, new byte[0] );

   /**
    * Null continuation structure used to fill storage slot during 
    * storage instance initialization.
    */
    private static final Continue nullContinue = 
	new ContinueBase( UnknownContinue.value, emptyData );

    private static final String nonInitStateMsg = "server is not ready to handle requests";
    private static final String disposedStateMsg = "server no longer available";
    private static final String nullObjectIdentifier = "thread level object identifier is null";
    private static final String reason = "local object";

    //============================================================================
    // state  
    //============================================================================

    private ORB orb;
    private POA poa;
    private Configuration configuration;
    private Connector connector;
    private Session session;
    private Logger log;
    private RequestCertificateStorageHome home;
    private Vault vault;
    private boolean initialized = false;
    private boolean disposed = false;

   /**
    * Internal calendar against which dates are evaluated.
    */
    private GregorianCalendar calendar = new GregorianCalendar();

    private RequestCertificateStorage store;

    //=======================================================================
    // Constructor
    //=======================================================================

    public RequestCertificateServant( RequestCertificateStorage store )
    {
	  this.store = store;
    }

    //=======================================================================
    // Loggable
    //=======================================================================
    
   /**
    * Sets the logging channel.
    * @param logger the logging channel
    */
    public void enableLogging( final Logger logger )
    {
        log = logger;
    }

   /**
    * Returns the logging channel.
    * @return Logger the logging channel
    */
    public Logger getLogger()
    {
        return log;
    }
 
    //============================================================================
    // Composable
    //============================================================================
    
    /**
     * Pass the <code>ComponentManager</code> to the <code>Composable</code>.
     * The <code>Composable</code> implementation should use the specified
     * <code>ComponentManager</code> to acquire the components it needs for
     * execution.
     *
     * @param manager The <code>ComponentManager</code> which this
     *                <code>Composer</code> uses.
     */
    public void compose( ComponentManager manager )
    throws ComponentException
    {
        if( getLogger().isDebugEnabled() ) getLogger().debug( "composition" );
	  try
	  {
            vault = (Vault) manager.lookup("VAULT");
            orb = ((ORBService) manager.lookup("ORB")).getOrb();
            connector = ((PSSConnectorService) manager.lookup("PSS-CONNECTOR")).getPSSConnector();
            session = ((PSSSessionService) manager.lookup("PSS-SESSION")).getPSSSession();
        }
        catch( Exception e )
	  {
		final String error = "composition phase failure";
		throw new ComponentException( error, e );
	  }
        if( getLogger().isDebugEnabled() ) getLogger().debug( 
		"composition complete" );
    }

    //=======================================================================
    // Configurable
    //=======================================================================
    
    public void configure( final Configuration config )
    throws ConfigurationException
    {
	  if( getLogger().isDebugEnabled() ) getLogger().debug( "configuration" );
        if( null != configuration ) throw new ConfigurationException( 
	        "Configurations for block " + this + " already set" );
        this.configuration = config;
    }

    //=======================================================================
    // Initializable
    //=======================================================================
    
    public void initialize()
    throws Exception
    {
        if( getLogger().isDebugEnabled() ) getLogger().debug( "initialization" );
        initialized = true;
    }

    //=======================================================================
    // RequestManager
    //=======================================================================

   /**
    * An attribute representing the status of the transaction
    * associated with the manager object.
    */
    public int status()
    {
        return store.status();
    }

   /**
    * A read only attribute representing an identifier for a particular
    * transaction
    */
    public long transaction_ID()
    {
        return store.tid();
    }

   /**
    * Operation to acknowledge negotiation is complete.
    * 
    * @param  confirm_data message to confirm content is correct and
    * received.
    * @exception  UnsupportedTypeException
    * @exception  UnsupportedEncodingException
    * @exception  MalformedDataException
    */
    public void confirm_content( Continue confirm_data )
    throws UnsupportedTypeException, UnsupportedEncodingException, MalformedDataException
    {
        System.out.println("RequestManagerDelegate: confirm_content" );
    }

    //=======================================================================
    // RequestCertificateManager
    //=======================================================================

   /**
    * Used for continuing a certificate request that has
    * already been initiated but requires more interaction to complete
    * the request. An example of the use of this operation is for Proof
    * Of possession (POP) of the private key.
    * 
    * @param  request_data <code>PKI::RequestData</code> structure
    * containing details for the continuation of the
    * initial request.
    * @param  certificates List of certificates possibly partially
    * formed.
    * @exception  UnsupportedTypeException
    * @exception  UnsupportedEncodingException
    * @exception  MalformedDataException
    */
    public void continue_request_certificate( Continue request_data, Certificate[] certificates)
    throws UnsupportedTypeException, UnsupportedEncodingException, MalformedDataException
    {
        System.out.println("RequestCertificateManagerDelegate: continue_request_certificate" );
    }

   /**
    * Obtains final or interim results of a particular request.  The values assigned to 
    * the supplied holders (certificates and response_data) is extracted from the persistent
    * storage.  These values are established either by the initialization of the delegate 
    * or as a result of a client providing supplimentary information to the delegate via 
    * the <code>continue_request_certificate</code> method.
    *
    * @param  certificates A list of certificates
    * @param  response_data <code> PKI::ResponsData</code> structure
    * containing details of the request thus far.
    * @return   <code>PKI::PKIStatus</code>, indicating the status of
    * the request.
    * @exception  UnsupportedTypeException
    * @exception  UnsupportedEncodingException
    * @exception  MalformedDataException
    */
    public int get_certificate_request_result( CertificateListHolder certificates, ContinueHolder response_data)
    throws UnsupportedTypeException, UnsupportedEncodingException, MalformedDataException
    {
        getLogger().debug("get_certificate_request_result." );
	  certificates.value = store.certificates();
	  response_data.value = store.response();
        return store.status();
    }

    //=======================================================================
    // RequestCertificateService
    //=======================================================================
    
   /**
    * Creation of a CORBA reference to a RequestCertificateManager 
    * that will handle a PKCS10 certificate request.
    * @param request the incomming certificate request
    * @return RequestCertificateManager manager to handle the certificate request
    * @exception UnsupportedTypeException
    * @exception UnsupportedEncodingException
    * @exception MalformedDataException
    * @exception NullPointerException if the certificate request is null
    */
    public RequestCertificateManager createRequestCertificateManager( CertificateRequest request )
    throws UnsupportedTypeException, UnsupportedEncodingException, MalformedDataException
    {
	  if( !initialized() ) throw new IllegalStateException( nonInitStateMsg );
	  if( disposed() ) throw new IllegalStateException( disposedStateMsg );
        if( request == null ) throw new NullPointerException(
          "null certificate request argument");

	  //
	  // validate the certificate request type
	  //

        int certRequestType = request.cert_request_type;
        if( certRequestType != PKCS10CertificateRequest.value )
        {
	      final String error = "requested certificate request type not supported";
	      throw new UnsupportedTypeException( error );
	  }

	  //
	  // validate the certificate request encoding
	  //

	  EncodedData encodedData = request.data;
	  int encodingType = encodedData.encoding_type;
	  if( encodingType != DEREncoding.value )
	  {
	      String error = "Certificate request encoding is not supported.";
	      throw new UnsupportedEncodingException( error );
	  }

	  //
	  // validate the certificate request
	  //

        PKCS10 pkcs10 = null;
	  if( request instanceof PKCS10 )
	  {
		pkcs10 = (PKCS10) request;
	  }
	  else
        {
		try
	      {
	          pkcs10 = new PKCS10Wrapper( encodedData.data );
		}
	      catch( Throwable error )
	      {
		    final String internal = "unexpected internal exception";
		    throw new RuntimeException( internal, error );
	      }
	  }

        PublicKey publicKey = null;
        try
	  {
            publicKey = pkcs10.getSubjectPublicKeyInfo();
        }
	  catch( Exception e )
	  {
		final String error = "unable to resolve public key from the supplied "
		  + "certificate request - cause: ";
		throw new MalformedDataException( error + "\n" + e.getMessage() );
	  }

	  //
	  // construct the status and response content
	  //

        RequestCertificateStorage store = null;
	  org.omg.CORBA.Object manager = null;
        try
	  {
		Date today = new Date();
		int days = getValidityPeriod();
	      sun.security.x509.X500Name subject = new sun.security.x509.X500Name( 
		    pkcs10.getPrincipalName().toString() );
		X509Certificate certificate = createCertificate( subject, today, days, publicKey );

		//
		// Create the PKCS#7 response
	      //

		sun.security.util.DerValue derValue = new sun.security.util.DerValue( 
			sun.security.util.DerValue.tag_Sequence, 
			certificate.getEncoded() );
		sun.security.pkcs.ContentInfo info = new sun.security.pkcs.ContentInfo( 
			sun.security.pkcs.ContentInfo.SIGNED_DATA_OID, 
			derValue );
		sun.security.x509.AlgorithmId[] algorithms = 
		  new sun.security.x509.AlgorithmId[]{ 
		    new sun.security.x509.AlgorithmId(sun.security.x509.AlgorithmId.sha1WithDSA_oid) };

		int i = 1;
		List certs = vault.getCertificatePath().getCertificates();
		Iterator iterator = certs.iterator();
            X509Certificate[] certificates = new X509Certificate[ certs.size() + 1 ];
            certificates[0] = certificate;
	      while( iterator.hasNext() )
	      {
		    	certificates[i] = (X509Certificate) iterator.next();
			i++;
		}

		sun.security.pkcs.SignerInfo[] signers = new sun.security.pkcs.SignerInfo[]{};
		sun.security.pkcs.PKCS7 pkcs7 = new sun.security.pkcs.PKCS7( 
			algorithms, 
			info, 
			certificates,
			signers 
		);

		sun.security.util.DerOutputStream stream = new sun.security.util.DerOutputStream();
		pkcs7.encodeSignedData( stream );
		byte[] byteArray = stream.toByteArray();
			
		//
	      // Create a new transaction identifier based on the time of the request.
		// The status value may be one of PKISuccess, PKISuccessWithWarning, 
		// PKIContinueNeeded, PKIFailed, PKIPending or PKISuccessAfterConfirm.
            // The implementation establishes the status as PKIPending and delegates
	      // responsibility to the servant to handle modification of this (taking
            // into consideration its own configuration) and the continue state.
	      //

		long tid = newTID();
		int status = PKISuccess.value;
		EncodedData data = new EncodedData( DEREncoding.value, byteArray );
		Continue result = new ContinueBase( PKIXCMCConfirm.value, data );
            store = home.create( tid, status, request, result, new org.omg.PKI.Certificate[0] );

		manager = poa.create_reference_with_id( 
		  store.get_pid(), 
	        RequestCertificateManagerHelper.id() 
		);
            if( getLogger().isDebugEnabled() ) getLogger().debug(
		  "new certificate request manager TID: " + tid );
            return RequestCertificateManagerHelper.narrow( manager );
		
        }
        catch (Exception e)
        {
		String error = "RequestCertificateManager instantiation failure";
            if( getLogger().isErrorEnabled() ) getLogger().error( error, e );
            throw new RuntimeException( error, e );
        }
    }

   /**
    * Return the certificate validity period based on the server configuration
    * values.
    */
    private int getValidityPeriod()
    {
	  Configuration conf = configuration.getChild("certificate-request");
	  Configuration grant = conf.getChild("policy").getChild("user").getChild("grant");
        return grant.getAttributeAsInteger("validity",90);
    }

   /**
    * Generate a signed certificate.
    *
    * @param authority - name of the signing entity
    * @param subject - name of subject certificate
    * @param date - date on which the certificate becomes valid
    * @param period - period of validity in days
    * @param privateKey - private key used to sign the certificate
    * @param publicKey - the public key to be signed
    * @return X509Certificate
    * @exception CertificateException
    * @exception InvalidKeyException
    * @exception SignatureException
    * @exception NoSuchAlgorithmException
    * @exception NoSuchProviderException
    */
    private X509Certificate createCertificate( 
      sun.security.x509.X500Name subject, Date date, int days, PublicKey publicKey )
    throws CertificateException, InvalidKeyException, SignatureException, NoSuchAlgorithmException, NoSuchProviderException, IOException, VaultException
    {
	  sun.security.x509.X500Name authority = new sun.security.x509.X500Name(
		    vault.getPrincipal().getEncoded() ); 

        String sigAlg = vault.getSignatureAlgorithm( );
        AlgorithmId aid = (AlgorithmId) AlgorithmId.get( sigAlg );

	  //
	  // the validity period
	  //

	  calendar.add( GregorianCalendar.DAY_OF_YEAR, days );
	  int serial = (int) ( date.getTime() / 1000L );
	  Date expires = calendar.getTime();
        X509CertInfo info = new X509CertInfo();

	  //
	  // the certificate info block
	  //

        info.set("version", new CertificateVersion(0) );
        info.set("serialNumber", new CertificateSerialNumber( serial ));
        info.set("algorithmID", new CertificateAlgorithmId( aid ));
        info.set("subject", new CertificateSubjectName( subject ));
        info.set("key", new CertificateX509Key( publicKey ));
        info.set("validity", new CertificateValidity( date, expires ) );
        info.set("issuer", new CertificateIssuerName( authority ));
	  return vault.createCertificate( info );

    }

   /**
    * Utility to get a transaction identifier based on the current time.
    */
    private long newTID()
    {
	  long tid = 0;
        try
	  {
		Date date = new java.util.Date();
		tid = date.getTime();
		home.find_by_tid( tid );
	  }
	  catch( NotFound notFound )
        {
	      return tid;
        }

	  //
	  // if we get here it means there is already a certificate 
        // request manager with this TID .. so try again
	  //

        return newTID();
    }
 
   /**
    * Returns true if the block has been initialized.
    */
    private boolean initialized()
    {
        return initialized;
    }

   /**
    * Returns true if the block has been disposed of.
    */
    private boolean disposed()
    {
        return disposed;   
    }

    //=======================================================================
    // Disposable
    //=======================================================================

    public void dispose()
    {
        orb = null;
        poa = null;
	  configuration = null;
        connector = null;
        session = null;
        home = null;
        vault = null;
	  calendar = null;
	  store = null;
	  if( getLogger().isDebugEnabled() ) getLogger().debug("disposal");
        disposed = true;
        log = null;
    }
}
