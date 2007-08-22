/*
 * @(#)RepositoryServer.java
 *
 * Copyright 2000 OSM SARL. All Rights Reserved.
 * 
 * This software is the proprietary information of OSM SARL.  
 * Use is subject to license terms.
 * 
 * @author  Stephen McConnell
 * @version 1.0 26/03/2001
 */

package net.osm.pki.repository;

import java.io.File;
import java.util.Hashtable;
import java.security.Principal;
import java.security.cert.CertPath;
import javax.security.auth.x500.X500Principal;

import org.apache.avalon.framework.activity.Disposable;
import org.apache.avalon.framework.activity.Initializable;
import org.apache.avalon.framework.activity.Startable;
import org.apache.avalon.framework.context.Context;
import org.apache.avalon.framework.context.Contextualizable;
import org.apache.avalon.framework.context.ContextException;
import org.apache.avalon.framework.component.Component;
import org.apache.avalon.framework.component.ComponentManager;
import org.apache.avalon.framework.component.ComponentException;
import org.apache.avalon.framework.component.Composable;
import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.logger.Logger;
import org.apache.avalon.framework.logger.LogEnabled;
import org.apache.avalon.framework.logger.AbstractLogEnabled;
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
import org.omg.PKIRepository.Repository;
import org.omg.PKIRepository.RepositoryHelper;
import org.omg.PKIRepository.RepositoryPOA;
import org.omg.PKIRepository.RepositoryProviderInfo;
import org.omg.PKIRepository.PrincipalValue;
import org.omg.PKIRepository.DuplicatePrincipal;
import org.omg.PKIRepository.UnknownPrincipal;
import org.omg.PKIRepository.RepositoryError;
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

import net.osm.pki.base.CRLInfoBase;
import net.osm.pki.base.CertificateInfoBase;
import net.osm.pki.base.CRLInfoBase;
import net.osm.pki.base.PKISingleton;
import net.osm.pki.repository.PrincipalStorage;
import net.osm.pki.repository.PrincipalStorageHome;

import net.osm.pss.PSSConnectorService;
import net.osm.pss.PSSSessionService;
import net.osm.pss.PersistanceHandler;
import net.osm.orb.ORBService;
import net.osm.orb.ORBServer;
import net.osm.util.ExceptionHelper;
import net.osm.vault.Vault;
import net.osm.vault.LocalVault;
import net.osm.util.X500Helper;
import net.osm.util.IOR;

/**
 * The <code>FinderServer</code> block provides bootstrap services 
 * enabling the establishment of initial objects into the environment.
 *
 * @author <a href="mailto:mcconnell@osm.net">Stephen McConnell</a>
 */

public class RepositoryServer extends RepositoryPOA 
implements Block, LogEnabled, Contextualizable, Composable, Configurable, Initializable, Disposable, RepositoryService
{
    //==================================================
    // state
    //==================================================

    private ORB orb;
    private POA poa;
    private Configuration configuration;
    private Connector connector;
    private Session session;
    private Logger log;

   /**
    * PSS storage home used to persist supplied Principals.
    */
    protected PrincipalStorageHome home;

    private boolean initialized = false;
    private boolean disposed = false;
    private BlockContext block;

    private Repository repository;
    private String ior;

    private Vault vault;

   /**
    * Description of the standard that this repository suports.
    */
    private static final String standardDescription = "OMG PKI Repository";

   /**
    * Description of the standard version that this repository suports.
    */
    private static final String standardVersion = "2.0";

   /**
    * Description of product.
    */
    private static final String productDescription = 
		"Public Key Infrastructure (PKI) repository supporting the " 
		+ "registration, retrieval and modification of "
            + "Principals, public-key credentials, and certificate "
		+ "revocation lists.";

   /**
    * Product version.
    */
    private static final String productVersion = "1.0";

   /**
    * Product vendor.
    */
    private static final String productVendor = "OSM";

   /**
    * Sequence of certificate descriptions that the implementation supports.
    */
    private static final CertificateInfo[] supportedCertificates = new CertificateInfo[]{
		new CertificateInfoBase( X509v1Certificate.value, DEREncoding.value ),
		new CertificateInfoBase( X509v2Certificate.value, DEREncoding.value ),
		new CertificateInfoBase( X509v3Certificate.value, DEREncoding.value )};

   /**
    * Sequence of CRL descriptions that the implementation supports.
    */
    private static final CRLInfo[] supportedCRLs = new CRLInfo[]{ 
		new CRLInfoBase( X509v1CRL.value, DEREncoding.value )};

   /**
    * Sequence of cross-certificate agreements supported by the server.
    */
    private static final CertificateInfo[] supportedCrossCertificates = new CertificateInfo[0];

   /**
    * Aggregated collection of repository provider information.
    */
    protected static RepositoryProviderInfo info;


    //==================================================
    // Loggable
    //==================================================
    
   /**
    * Sets the logging channel.
    * @param logger the logging channel
    */
    public void enableLogging( final Logger logger )
    {
        if( logger == null ) throw new NullPointerException("null logger argument");
        log = logger;
    }

   /**
    * Returns the logging channel.
    * @return Logger the logging channel
    * @exception IllegalStateException if the logging channel has not been set
    */
    public Logger getLogger()
    {
        if( log == null ) throw new IllegalStateException("logging has not been enabled");
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
        if( getLogger().isDebugEnabled() ) getLogger().debug( "contextualize phase" );
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
            vault = (Vault) manager.lookup("VAULT");
            orb = ((ORBService) manager.lookup("ORB")).getOrb();
            connector = ((PSSConnectorService) manager.lookup("PSS-CONNECTOR")).getPSSConnector();
            session = ((PSSSessionService) manager.lookup("PSS-SESSION")).getPSSSession();
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
            
        //
        // create the POA
        //

        try
	  {
            POA root = POAHelper.narrow(orb.resolve_initial_references("RootPOA"));
            POA poa = root.create_POA(
              "REPOSITORY",
              root.the_POAManager(),
              new Policy[]{
                  root.create_id_assignment_policy( IdAssignmentPolicyValue.USER_ID ),
                  root.create_lifespan_policy( LifespanPolicyValue.PERSISTENT )});

            //
            // create and bind this object as the servant and create an 
            // object reference for publication
            //
                        
            byte[] ID = "REPOSITORY".getBytes();
            poa.activate_object_with_id( ID, this );
            org.omg.CORBA.Object obj = poa.id_to_reference(ID);
            repository = RepositoryHelper.narrow( obj );
	  }
	  catch( Throwable poaError )
	  {
	      String error = "unable to create the repository object reference";
	      if( getLogger().isErrorEnabled() ) getLogger().error( error, poaError );
	      throw new Exception( error, poaError );
	  }

        //
        // handle persistence configuration
        //

	  try
	  {
            Configuration persistence = configuration.getChild("persistence");
	      PersistanceHandler.register( connector, persistence );
            home = (PrincipalStorageHome) session.find_storage_home(
 		    "PSDL:osm.net/pki/repository/PrincipalStorageHomeBase:1.0" );
	  }
	  catch( Throwable throwable )
	  {
	      String error = "unable to establish storage home";
		if( getLogger().isErrorEnabled() ) getLogger().error( error, throwable );
		throw new Exception( error, throwable );
	  }

        //
        // handle IOR stuff
        //

        try
        {
            ior = this.configuration.getAttribute( "ior" );
            getLogger().info("setting IOR path to " + ior );
            try
            {
                IOR.writeIOR( orb, repository, ior );
                getLogger().info( "published repository IOR to: " + ior );
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
        // setup static provider info
        //

        info = new RepositoryProviderInfoBase(
        	standardDescription, standardVersion, productDescription,
	  	productVersion, productVendor, supportedCertificates, supportedCRLs,
	  	supportedCrossCertificates );

        String message = "OSM PKI Repository";
        getLogger().info( message );
	  System.out.println( message );
    }

    //=======================================================================
    // PKIRepository
    //=======================================================================

   /**
    * Returns the aggregated repository provider information.
    * @return RepositoryProviderInfo structure describing the repository and provider.
    */
    public RepositoryProviderInfo get_provider_info()
    {
        return info;
    }

   /**
    * Enters a new principal valuetype into the repository.
    * <p>
    * @param principal the pricipal valuetype to be added to the repository
    * @exception DuplicatePrincipal if there is an existing pricipal with the same name
    * @exception RepositoryError
    */
    public void publish( PrincipalValue principal )
    throws DuplicatePrincipal, RepositoryError
    {
        if( home == null ) throw new RepositoryError(
		"Repository storage home has not been initialized.");

	  try 
        {
	      home.find_by_name( principal.name );
		throw new DuplicatePrincipal( principal.name );
        }
        catch( NotFound notFound )
        {
		try
	      {
                final String debug = "publishing principal to PSS";
                home.create( principal.name, principal );
		    if( getLogger().isDebugEnabled() ) getLogger().debug( debug );
            }
            catch( Exception e )
            {
		    final String error = "unable to store principal due to a persistence error";
		    throw new RepositoryError( error );
            }
        }
    }

   /**
    * Locates a principal in the repository using the supplied name as a key.
    * <p>
    * @param name the principal valuetype repository key
    * @return PrincipalValue the pricipal valuetype
    * @exception UnknownPrincipal if requested name is not found
    * @exception RepositoryError
    */
    public PrincipalValue locate( String name )
    throws UnknownPrincipal, RepositoryError
    {
        if( home == null ) throw new RepositoryError(
	    "Repository storage home has not been initialized.");
	  try 
        {
	      PrincipalStorage store = home.find_by_name( name );
            final String debug = "located principal in PSS: ";
		if( getLogger().isDebugEnabled() ) getLogger().debug( debug + name );
		return store.value();
        }
        catch( NotFound notFound )
        {
		throw new UnknownPrincipal( name );
        }
        catch( Exception e )
        {
		final String error = "unexpected exception while attempting to locate a principal";
	      throw new RepositoryError( error );
        }
    }

   /**
    * Deletes a principal in the repository using the supplied name as a key.
    * <p>
    * @param name the privipal valuetype repository key
    * @exception UnknownPrincipal if requested name is not found
    * @exception RepositoryError
    */
    public void delete( String name )
    throws UnknownPrincipal, RepositoryError
    {
        if( home == null ) throw new RepositoryError(
		"Repository storage home has not been initialized.");
	  try
        {
	      PrincipalStorage store = home.find_by_name( name );
            final String debug = "deleting principal in PSS: ";
		if( getLogger().isDebugEnabled() ) getLogger().debug( debug + name );
		store.destroy_object();
        }
        catch( NotFound notFound )
        {
		throw new UnknownPrincipal( name );
        }
        catch( Exception e )
        {
		final String error = "unexpected exception while attempting to delete a principal";
	      throw new RepositoryError( error );
        }
    }

   /**
    * Replaces an existing pricipal valuetype in the repository 
    * with the supplied pricipal valuetype.
    * <p>
    * @param principal the replacement pricipal valuetype
    * @exception UnknownPrincipal if requested name is not found
    * @exception RepositoryError
    */
    public void update( PrincipalValue principal )
    throws UnknownPrincipal, RepositoryError
    {
        if( home == null ) throw new RepositoryError(
		"Repository storage home has not been initialized.");
	  try 
        {
	      PrincipalStorage store = home.find_by_name( principal.name );
            final String debug = "updating principal in PSS: ";
		if( getLogger().isDebugEnabled() ) getLogger().debug( debug + principal.name );
		store.value( principal );
        }
        catch( NotFound notFound )
        {
		throw new UnknownPrincipal( principal.name );
        }
        catch( Exception e )
        {
		final String error = "unexpected exception while attempting to update a principal";
	      throw new RepositoryError( error );
        }
    }

    //=======================================================================
    // RepositoryService
    //=======================================================================

   /**
    * Returns the realm authenticator.
    * @return Repository the PKI repository service
    */
    public Repository getRepository( )
    {
	  return repository;
    }

    //=======================================================================
    // Disposable
    //=======================================================================
        
    public void dispose()
    {
        if( getLogger().isDebugEnabled() ) getLogger().debug( "disposal" );
        poa.destroy( true, true );
        orb = null;
        configuration = null;
        connector = null;
        session = null;
        log = null;
        home = null;
        block = null;
        repository = null;
        ior = null;
        vault = null;
    }
}
