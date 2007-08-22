/*
 * @(#)RegistrationAuthorityServer.java
 *
 * Copyright 2001 OSM SARL. All Rights Reserved.
 * 
 * This software is the proprietary information of OSM SARL.  
 * Use is subject to license terms.
 * 
 * @author  Stephen McConnell
 * @version 1.1 28/12/2001
 */

package net.osm.pki.authority;

import java.io.File;
import java.util.Hashtable;
import java.security.Principal;
import java.security.cert.CertPath;
import javax.security.auth.x500.X500Principal;

import org.apache.avalon.framework.logger.Logger;
import org.apache.avalon.framework.logger.LogEnabled;
import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.avalon.framework.context.Context;
import org.apache.avalon.framework.context.Contextualizable;
import org.apache.avalon.framework.context.ContextException;
import org.apache.avalon.framework.component.Component;
import org.apache.avalon.framework.component.ComponentManager;
import org.apache.avalon.framework.activity.Disposable;
import org.apache.avalon.framework.activity.Initializable;
import org.apache.avalon.framework.activity.Startable;
import org.apache.avalon.framework.logger.LogEnabled;
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
import org.omg.CosPersistentState.NotFound;
import org.omg.CosPersistentState.Connector;
import org.omg.CosPersistentState.Session;
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

import org.omg.PKIAuthority.RegistrationAuthority;
import org.omg.PKIAuthority.RegistrationAuthorityHelper;
import org.omg.PKIAuthority.RegistrationAuthorityPOA;
import org.omg.PKIAuthority.AuthorityProviderInfo;
import org.omg.PKIAuthority.UnsupportedTypeException;
import org.omg.PKIAuthority.UnsupportedEncodingException;
import org.omg.PKIAuthority.MalformedDataException;
import org.omg.PKIAuthority.AuthorityProviderInfo;
import org.omg.PKIAuthority.RegistrationAuthorityPOA;
import org.omg.PKIAuthority.RequestCertificateManager;
import org.omg.PKIAuthority.RequestKeyUpdateManager;
import org.omg.PKIAuthority.RequestKeyRecoveryManager;
import org.omg.PKIAuthority.RequestRevocationManager;

import org.omg.PKI.AuthorityInfo;
import org.omg.PKI.AuthorityInfoHolder;
import org.omg.PKI.CertificateRequest;
import org.omg.PKI.CertRevocation;
import org.omg.PKI.CertificateRequestInfo;
import org.omg.PKI.CertificateRevocationInfo;
import org.omg.PKI.KeyRecoveryInfo;
import org.omg.PKI.CertificatePair;
import org.omg.PKI.Certificate;
import org.omg.PKI.CRL;
import org.omg.PKI.CRLInfo;
import org.omg.PKI.CertificateInfo;
import org.omg.PKI.DEREncoding;
import org.omg.PKI.X509v1CRL;
import org.omg.PKI.X509v1Certificate;
import org.omg.PKI.X509v2Certificate;
import org.omg.PKI.X509v3Certificate;
import org.omg.PKI.PKCS10CertificateRequest;

import net.osm.pki.base.CRLInfoBase;
import net.osm.pki.base.CertificateInfoBase;
import net.osm.pki.base.CertificateRequestInfoBase;
import net.osm.pki.base.CRLInfoBase;
import net.osm.pki.base.PKISingleton;
import net.osm.pki.pkcs.PKCSSingleton;

import net.osm.orb.ORBService;
import net.osm.util.ExceptionHelper;
import net.osm.util.IOR;

/**
 * The <code>RegistrationAuthorityServer</code> block provides PKI
 * registriation authority services covering certificate issuance, 
 * key recovery, updating, and revocation request management.
 *
 * @author <a href="mailto:mcconnell@osm.net">Stephen McConnell</a>
 */

public class RegistrationAuthorityServer extends RegistrationAuthorityPOA 
implements Block, LogEnabled, Contextualizable, Composable, Configurable, Initializable, Disposable, RegistrationAuthorityService
{
    //==================================================
    // state
    //==================================================

    private ORB orb;
    private POA poa;
    private Configuration configuration;
    private Logger log;

    private boolean initialized = false;
    private boolean disposed = false;
    private BlockContext block;

   /**
    * The certificate request manager against which requests for 
    * certification processes can be established.
    */
    private RequestCertificateService certification;

   /**
    * CORBA object reference to this server.
    */
    private RegistrationAuthority authority;

   /**
    * Interoperable object reference to the server.
    */
    private String ior;

   /**
    * Description of the standard that this repository suports.
    */
    private String standardDescription;

   /**
    * Description of the standard version that this repository suports.
    */
    private String standardVersion;

   /**
    * Description of product.
    */
    private String productDescription;

   /**
    * Product version.
    */
    private String productVersion;

   /**
    * Product vendor.
    */
    private String productVendor;

   /**
    * Sequence of certificate descriptions that the implementation supports.
    */
    private CertificateInfo[] supportedCertificates;


   /**
    * Sequence of CRL descriptions that the implementation supports.
    */
    private CRLInfo[] supportedCRLs;

   /**
    * Sequence of supported certificate request types.
    */
    private CertificateRequestInfo[] supportedCertRequestTypes;

   /**
    * Sequence of supported certificate revocation types.
    */
    private CertificateRevocationInfo[] supportedCertRevocationTypes;

   /**
    * Sequence of supported key recovery types.
    */
    private KeyRecoveryInfo[] supportedKeyRecoveryTypes;

   /**
    * Aggregated collection of repository provider information.
    */
    private AuthorityProviderInfo info;

   /**
    * URL of a HTML page containing the public key of the RA.
    */
    private String publicKeyURL;
  
   /**
    * URL of a HTML page containing the RA policy.
    */
    private String policyURL;

   /**
    * Sequence of public key certificates.
    */
    private Certificate[] publicKeys = new Certificate[0];

    //==================================================
    // Loggable
    //==================================================
    
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
    * @exception IllegalStateException if the logging channel has not been set
    */
    public Logger getLogger()
    {
        return log;
    }

    //=================================================================
    // Contextualizable
    //=================================================================

   /**
    * Set the block context.
    * @param context the block context
    * @exception ContextException if the block cannot be narrowed to BlockContext
    */
    public void contextualize( Context block ) throws ContextException
    {
        if( getLogger().isDebugEnabled() ) getLogger().debug( "contextualize" );
	  if( !(block instanceof BlockContext ) )
	  {
		final String error = "supplied context does not implement BlockContext.";
		if( getLogger().isErrorEnabled() ) getLogger().error( error );
		throw new ContextException( error );
        }
	  this.block = (BlockContext) block;
        if( getLogger().isDebugEnabled() ) getLogger().debug( "contextualize complete" );
    }

    //=================================================================
    // Composable
    //=================================================================
    
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
	  try
	  {
            if( getLogger().isDebugEnabled() ) getLogger().debug( "compose" );
            orb = ((ORBService) manager.lookup("ORB")).getOrb();
		
            certification = 
              ((RequestCertificateService) manager.lookup("PKI-CERTIFICATION"));
            if( getLogger().isDebugEnabled() ) getLogger().debug( "compose complete" );
	  }
	  catch( Exception e )
	  {
		String error = "unexpected exception during composition";
		if( getLogger().isErrorEnabled() ) getLogger().error( error, e );
            throw new ComponentException( error, e );
	  }
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

        RegistrationAuthoritySingleton.init( orb );
        PKISingleton.init( orb );
        PKCSSingleton.init( orb );

        //
        // create the POA
        //
            
        try
	  {
            POA root = POAHelper.narrow(orb.resolve_initial_references("RootPOA"));
            POA poa = root.create_POA(
              "REGISTRATION-AUTHORITY",
              root.the_POAManager(),
              new Policy[]{
                  root.create_id_assignment_policy( IdAssignmentPolicyValue.USER_ID ),
                  root.create_lifespan_policy( LifespanPolicyValue.PERSISTENT )});

            //
            // create and bind this object as the servant and create an 
            // object reference for publication
            //
                        
            byte[] ID = "REGISTRATION-AUTHORITY".getBytes();
            poa.activate_object_with_id( ID, this );
            org.omg.CORBA.Object obj = poa.id_to_reference(ID);
            authority = RegistrationAuthorityHelper.narrow( obj );
	  }
	  catch( Throwable poaError )
	  {
	      String error = "unable to create the authority object reference";
	      if( getLogger().isErrorEnabled() ) getLogger().error( error, poaError );
	      throw new Exception( error, poaError );
	  }

        //
        // handle IOR publication
        //

        try
        {
            ior = this.configuration.getAttribute( "ior" );
            getLogger().info("setting IOR path to " + ior );
            try
            {
                IOR.writeIOR( orb, authority, ior );
                getLogger().info( "published authority IOR to: " + ior );
            }
            catch (Exception e)
            {
		    final String error = "failed to create external IOR on ";
                if( getLogger().isErrorEnabled() ) getLogger().error( error + ior, e );
                throw new Exception( error + ior, e );
            }
        }
        catch (Exception e)
        {
            getLogger().info("IOR publication disabled" );
        }

        //
        // setup static authority info
        //

	  try
	  {
            info = createAuthorityInfo( configuration );
	  }
	  catch( ConfigurationException e )
	  {
		final String error = "unable to establish authority provider information";
		throw new Exception( error, e );
	  }

        String message = "OSM PKI Registration Authority";
	  System.out.println( message );
        getLogger().info( message + " initialized" );

    }

   /**
    * Creation of the authority provider info valuetype.
    * @osm.warning public key list is not populated yet
    */
    private AuthorityProviderInfo createAuthorityInfo( Configuration conf ) 
    throws ConfigurationException
    {
        Configuration standard = conf.getChild("standard");
        standardVersion = standard.getAttribute("version");
        standardDescription = standard.getAttribute("name");

        Configuration product = conf.getChild("product");
        productVersion = product.getAttribute("version");
        productVendor = product.getAttribute("vendor");
        productDescription = product.getValue();        

        Configuration provider = conf.getChild("provider");
        publicKeyURL = provider.getAttribute("public-key-url");
        policyURL = provider.getAttribute("policy-url");

	  //
	  // WARNING: the following info members are hard coded here and 
        // need to be replaced with info extracted from the configuration
	  //

	  supportedCertificates = 
	    new CertificateInfo[]{
		new CertificateInfoBase( X509v1Certificate.value, DEREncoding.value ),
		new CertificateInfoBase( X509v2Certificate.value, DEREncoding.value ),
		new CertificateInfoBase( X509v3Certificate.value, DEREncoding.value )
    	    };

        supportedCRLs = 
	    new CRLInfo[]{ 
		new CRLInfoBase( X509v1CRL.value, DEREncoding.value )
	    };

        supportedCertRequestTypes = 
	    new CertificateRequestInfo[]{
            new CertificateRequestInfoBase( PKCS10CertificateRequest.value, DEREncoding.value )
          };

        supportedCertRevocationTypes = new CertificateRevocationInfo[0];
        supportedKeyRecoveryTypes = new KeyRecoveryInfo[0];

        return new AuthorityProviderInfoBase( standardDescription, standardVersion, 
	    productDescription, productVersion, productVendor, supportedCertificates,
	    supportedCRLs, supportedCertRequestTypes, supportedCertRevocationTypes, 
	    supportedKeyRecoveryTypes, publicKeys, publicKeyURL, policyURL );

    }

    //=======================================================================
    // RegistrationAuthorityService
    //=======================================================================

   /**
    * Returns a CORBA object reference to a Registration Authority
    * @return RegistrationAuthority the PKI Registration Authority
    */
    public RegistrationAuthority getRegistrationAuthority( )
    {
	  if( authority == null ) throw new IllegalStateException(
		"registration authority has not been declared");
        return authority;
    }

   /**
    * Returns a reference to a RequestCertificateService.
    * @return RequestCertificateService the certification request service
    */
    public RequestCertificateService getRequestCertificateService()
    {
	  if( certification == null ) throw new IllegalStateException(
		"certification service has not been declared");
        return certification;
    }

    //=======================================================================
    // RegistrationAuthority
    //=======================================================================

   /**
    * Get the provider info for this authority.
    */
    public AuthorityProviderInfo get_provider_info()
    {
	  if( info == null ) throw new IllegalStateException(
		"server has not been initalized");
        return info;
    }

   /**
    * Message exchange between a client entity and authority. For example 
    * this may provide a method for a client to determine the authentication 
    * policy of the authority.
    * 
    * @param  in_authority_info The encoded message input to authority.
    * @param  out_authority_info The encoded returned message from
    * authority.
    * @return  Status value.
    * @exception  UnsupportedTypeException
    * @exception  UnsupportedEncodingException
    * @exception  MalformedDataException
    */
    public int get_authority_info(AuthorityInfo in_authority_info, AuthorityInfoHolder out_authority_info)
    throws UnsupportedTypeException, UnsupportedEncodingException, MalformedDataException
    {
        throw new org.omg.CORBA.NO_IMPLEMENT();
    }

   /**
    * Called to make a request for a certificate from an
    * authority such as a Certificate Authority (CA) or Registration
    * Authority (RA).
    * 
    * @param  certificate_request <code>PKI::CertificateRequest</code>
    * structure containing details of the clients request.
    * @return  <code>RequestCertificateManager</code> object reference to
    * extract details regarding the particular request,
    * continue interaction and obtain results.
    * @exception  UnsupportedTypeException
    * @exception  UnsupportedEncodingException
    * @exception  MalformedDataException
    */
    public RequestCertificateManager request_certificate( CertificateRequest request )
    throws UnsupportedTypeException, UnsupportedEncodingException, MalformedDataException
    {
        return certification.createRequestCertificateManager( request );
    }

   /**
    * Called to request revocation of a certificate from a (CA) or (RA). 
    * @param  cert_rev_request <code>PKI::CertRevRequest</code> structure
    * containing details of the clients request for
    * certificate revocation.
    * @return  <code>RequestRevocationManager</code> object reference
    * used to extract details pertaining to the request,
    * continue interaction and obtain results.
    * @exception  UnsupportedTypeException
    * @exception  UnsupportedEncodingException
    * @exception  MalformedDataException
    */
    public RequestRevocationManager request_revocation( CertRevocation cert_rev_request)
    throws UnsupportedTypeException, UnsupportedEncodingException, MalformedDataException
    {
        throw new org.omg.CORBA.NO_IMPLEMENT();
    }

   /**
    * Called to request key update of a certificate from a (CA) or (RA).
    * @param   key_request <code>PKI::CertificateRequest</code> structure
    * containing details of the clients request for key
    * update.
    * @return  <code>RequestKeyUpdateManager</code> object reference
    * used to extract details pertaining to the
    * request, continue interaction and obtain results.
    * @exception  UnsupportedTypeException
    * @exception  UnsupportedEncodingException
    * @exception  MalformedDataException
    */
    public RequestKeyUpdateManager request_key_update( CertificateRequest key_request)
    throws UnsupportedTypeException, UnsupportedEncodingException, MalformedDataException
    {
        throw new org.omg.CORBA.NO_IMPLEMENT();
    }

   /**
    * Returns a key recover manager.
    * @param  key_request <code>PKI::CertificateRequest</code> structure
    * containing details of the clients request for key
    * recovery.
    * @return  <code>RequestKeyRecoveryManager</code> object reference
    * that can be used extract details pertaining to the
    * request, continue interaction and obtain results.
    * @exception  UnsupportedTypeException
    * @exception  UnsupportedEncodingException
    * @exception  MalformedDataException
    */
    public RequestKeyRecoveryManager request_key_recovery( CertificateRequest key_request )
    throws UnsupportedTypeException, UnsupportedEncodingException, MalformedDataException
    {
        throw new org.omg.CORBA.NO_IMPLEMENT();
    }


    //=======================================================================
    // Disposable
    //=======================================================================
        
    public void dispose()
    {
	  poa.destroy( true, true );
	  orb = null;
        poa = null;
        configuration = null;
        block = null;
        certification = null;        
        authority = null;
        ior = null;
        standardDescription = null;
        standardVersion = null;
        productDescription = null;
        productVersion = null;
        productVendor = null;
        supportedCertificates = null;
        supportedCRLs = null;
        supportedCertRequestTypes = null;
        supportedCertRevocationTypes = null;
        supportedKeyRecoveryTypes = null;
        info = null;
        publicKeyURL = null;
        policyURL = null;
        publicKeys = null;
        if( getLogger().isDebugEnabled() ) getLogger().debug( "disposal" );
        log = null;
        disposed = true;
    }
}
