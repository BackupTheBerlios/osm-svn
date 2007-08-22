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
import org.apache.avalon.framework.logger.Logger;
import org.apache.avalon.framework.logger.LogEnabled;
import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.service.DefaultServiceManager;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.Serviceable;
import org.apache.avalon.framework.CascadingRuntimeException;
import org.apache.avalon.framework.CascadingException;

import org.apache.orb.ORBContext;
import org.apache.orb.DefaultORBContext;
import org.apache.pss.Connector;
import org.apache.pss.ConnectorContext;
import org.apache.pss.Session;
import org.apache.pss.SessionContext;

import org.omg.CORBA_2_3.ORB;
import org.omg.CORBA.Any;
import org.omg.CORBA.LocalObject;
import org.omg.CORBA.Policy;
import org.omg.PortableServer.ServantActivator;
import org.omg.CosPersistentState.NotFound;
import org.omg.CosPersistentState.StorageObject;
import org.omg.CosPersistentState.ConnectorHelper;
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
import net.osm.pki.base.X500Name;

import net.osm.vault.Vault;
import net.osm.vault.VaultException;


/**
 * The abstract <code>RequestCertificateServer</code> block handles the establishment
 * of a certificate request delegate.
 *
 * @author <a href="mailto:mcconnell@osm.net">Stephen McConnell</a>
 */

public class RequestCertificateServer extends LocalObject
implements ServantActivator, LogEnabled, Serviceable, Contextualizable, Configurable, Initializable, Disposable,
RequestCertificateService
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
    private static final String reason = "local object";


    //============================================================================
    // state  
    //============================================================================

    private Logger m_logger;
    private Configuration m_config;
    private ServiceManager m_manager;

    private Vault m_vault;
    private ORB m_orb;
    private POA m_poa;
    private Connector m_connector;
    private Session m_session;
    private RequestCertificateStorageHome m_home;
    private ServiceManager m_servant_manager;

    private boolean initialized = false;
    private boolean disposed = false;

   /**
    * Internal calendar against which dates are evaluated.
    */
    private GregorianCalendar m_calendar = new GregorianCalendar();

    private Context m_context;

    //=================================================================
    // Loggable
    //=================================================================
    
   /**
    * Sets the logging channel.
    * @param logger the logging channel
    */
    public void enableLogging( final Logger logger )
    {
        m_logger = logger;
    }

   /**
    * Returns the logging channel.
    * @return Logger the logging channel
    */
    public Logger getLogger()
    {
        return m_logger;
    }

    //=================================================================
    // Contextualizable
    //=================================================================

   /**
    * Set the component context.
    * @param context the component context
    */
    public void contextualize( Context context )
    {
	  m_context = context;
    }

 
    //============================================================================
    // Serviceable
    //============================================================================
    
    /**
     * Pass the <code>ServiceManager</code> to the <code>Serviceable</code>.
     * The <code>Serviceable</code> implementation should use the supplied
     * <code>ServiceManager</code> to acquire the components it needs for
     * execution.
     *
     * @param manager The <code>ServiceManager</code> which this
     *                <code>Serviceable</code> uses.
     */
    public void service( ServiceManager manager )
    throws ServiceException
    {
        if( getLogger().isDebugEnabled() ) getLogger().debug( "service composition" );
        m_manager = manager;
    }

    //=======================================================================
    // Configurable
    //=======================================================================
    
    public void configure( final Configuration config )
    throws ConfigurationException
    {
	  if( getLogger().isDebugEnabled() ) getLogger().debug( "configuration" );
        if( null != m_config ) throw new ConfigurationException( 
	        "Configurations for block " + this + " already set" );
        m_config = config;
    }

    //=======================================================================
    // Initializable
    //=======================================================================
    
    public void initialize()
    throws Exception
    {
        if( getLogger().isDebugEnabled() ) getLogger().debug( "initialization" );

        DefaultServiceManager manager = new DefaultServiceManager();
	  try
	  {
            m_vault = (Vault) m_manager.lookup( "vault" );
            m_orb = (ORB) m_manager.lookup( "orb" );
            manager.put( "orb", m_orb );
            manager.put( "vault", m_vault );
        }
        catch( Exception e )
	  {
		final String error = "composition phase failure";
		throw new ServiceException( error, e );
	  }

        try
	  {
            Configuration pss = m_config.getChild("pss");
            String mode = pss.getChild("connector").getAttribute("value","file");
            org.omg.CORBA.Object object = m_orb.resolve_initial_references( "PSS:APACHE:" + mode );
            m_connector = (Connector) ConnectorHelper.narrow( object );
            m_connector.register( pss.getChild("persistence") );
            m_session = m_connector.createBasicSession( pss.getChild("session") );
            m_home = (RequestCertificateStorageHome) m_session.find_storage_home( PSDL );
            manager.put( ConnectorContext.CONNECTOR_KEY, m_connector );
            manager.put( SessionContext.SESSION_KEY, m_session );
            m_servant_manager = manager;
	  }
	  catch( Throwable e )
	  {
	      String error = "ORB related initalization error";
	      throw new CascadingException( error, e );
	  }

        //
        // create the POA
        //
            
        try
	  {
            POA root = POAHelper.narrow(m_orb.resolve_initial_references("RootPOA"));
            m_poa = root.create_POA(
                "PKI-CERTIFICATE-REQUEST",
                root.the_POAManager(),
                new Policy[]
                {
                    root.create_id_assignment_policy( 
			    IdAssignmentPolicyValue.USER_ID ),
                    root.create_lifespan_policy( 
			    LifespanPolicyValue.PERSISTENT ),
                    root.create_request_processing_policy( 
			    RequestProcessingPolicyValue.USE_SERVANT_MANAGER ),
                    root.create_servant_retention_policy( 
			    ServantRetentionPolicyValue.RETAIN )
                }
            );
            m_poa.set_servant_manager( this );
	  }
	  catch( Throwable poaError )
	  {
	      String error = "unable to create the certificate request POA";
	      if( getLogger().isErrorEnabled() ) getLogger().error( error, poaError );
	      throw new Exception( error, poaError );
	  }

        initialized = true;
        String message = "OSM PKI Certification initialized";
        getLogger().info( message );

    }

    //=======================================================================
    // ServantActivator
    //=======================================================================

   /**
    * This operation is invoked by the POA whenever the 
    * POA receives a request for an object that is not 
    * currently active.  
    * @param oid identifier of the object on the request was made
    * @param adapter object reference for the POA in which the object is being activated
    * @return Servant the servant handling the request
    * @exception ForwardRequest to indicate to the ORB 
    *  that it is responsible for delivering the current request and subsequent 
    *  requests to the object denoted in the forward_reference member of the exception.
    */
    public org.omg.PortableServer.Servant incarnate (byte[] oid, POA adapter) 
    throws org.omg.PortableServer.ForwardRequest
    {
	  try
	  {
		RequestCertificateStorage store = (RequestCertificateStorage) 
              m_home.get_catalog().find_by_pid( oid );
		RequestCertificateServant servant = new RequestCertificateServant( store );
		servant.enableLogging( getLogger().getChildLogger( "" + store.tid() ));
		servant.service( m_servant_manager );
		servant.configure( m_config );
		servant.initialize();
		System.out.println("INCARNATE: " + servant );
		return servant;
	  }
	  catch( Throwable e )
	  {
	      final String error = "unable to establish a certification request servant";
	      if( getLogger().isErrorEnabled() ) getLogger().error( error, e );
		throw new CascadingRuntimeException( error, e );
	  }
    }

   /**
    * This operation is invoked whenever a servant for 
    * an object is deactivated. 
    * @param oid object Id associated with the object being deactivated.
    * @param adapter object reference for the POA in which the object was active.
    * @param serv contains reference to the servant associated with the object 
    *  being deactivated.
    * @param cleanup_in_progress if TRUE indicates that destroy or deactivate is 
    *  called with etherealize_objects param of TRUE.  FALSE indicates that 
    *  etherealize was called due to other reasons.
    * @param remaining_activations indicates whether the Servant Manager can 
    *  destroy a servant.  If set to TRUE, the Servant Manager should wait
    *  until all invocations in progress have completed.
    */
    public void etherealize (byte[] oid, POA adapter, Servant serv, boolean cleanup_in_progress, boolean remaining_activations)
    {
	  if( !remaining_activations && ( serv instanceof Disposable ))
	  {
            System.out.println("ETHEREALIZE: " + serv );
		((Disposable)serv).dispose();
	  }
    }

    //=======================================================================
    // RequestCertificateService
    //=======================================================================
    
   /**
    * Notification to the service that a manager is no longer required and 
    * can be disposed of.
    * @param manager the manager to be disposed of.
    */
    public boolean disposeRequestCertificateManager( RequestCertificateManager certification_manager )
    {
        try
	  {
	      m_poa.deactivate_object( m_poa.reference_to_id( certification_manager ) );
		return true;
        }
        catch( Exception error )
	  {
	      String warning = "error while attempting to dispose of a certification manager";
		if( getLogger().isWarnEnabled() ) getLogger().warn( warning, error );
	  }
	  finally
	  {
		return false;
	  }
    }

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
		List certs = m_vault.getCertificatePath().getCertificates();
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
            store = m_home.create( tid, status, request, result, new org.omg.PKI.Certificate[0] );

		manager = m_poa.create_reference_with_id( 
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
	  Configuration conf = m_config.getChild("certificate-request");
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
		    m_vault.getPrincipal().getEncoded() ); 

        String sigAlg = m_vault.getSignatureAlgorithm( );
        AlgorithmId aid = (AlgorithmId) AlgorithmId.get( sigAlg );

	  //
	  // the validity period
	  //

	  m_calendar.add( GregorianCalendar.DAY_OF_YEAR, days );
	  int serial = (int) ( date.getTime() / 1000L );
	  Date expires = m_calendar.getTime();
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
	  return m_vault.createCertificate( info );

    }


    //=====================================================================
    // utilities
    //=====================================================================

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
		m_home.find_by_tid( tid );
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

    public void dispose()
    {
        if( getLogger().isDebugEnabled() ) getLogger().debug( "disposal" );
        try
        {
	      m_poa.destroy( true, true );
        }
        catch( Throwable e )
        {
            if( getLogger().isWarnEnabled() ) getLogger().warn( "ignoring POA related exception" );
        }
        m_manager = null;
        m_orb = null;
        m_poa = null;
        m_config = null;
        m_connector = null;
        m_session = null;        
        m_home = null;
        m_vault = null;
        m_calendar = null;
        disposed = true;
        m_logger = null;
    }
}
