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
import java.util.Properties;
import java.security.Principal;
import java.security.cert.CertPath;
import javax.security.auth.x500.X500Principal;

import org.apache.avalon.framework.activity.Disposable;
import org.apache.avalon.framework.activity.Initializable;
import org.apache.avalon.framework.activity.Startable;
import org.apache.avalon.framework.activity.Executable;
import org.apache.avalon.framework.context.Context;
import org.apache.avalon.framework.context.Contextualizable;
import org.apache.avalon.framework.context.ContextException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.Serviceable;
import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.logger.Logger;
import org.apache.avalon.framework.logger.LogEnabled;
import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.avalon.framework.CascadingException;

import org.apache.orb.ORBContext;
import org.apache.orb.DefaultORBContext;
import org.apache.pss.Connector;
import org.apache.pss.ConnectorContext;
import org.apache.pss.Session;
import org.apache.pss.SessionContext;

import org.omg.CORBA.Any;
import org.omg.CORBA.LocalObject;
import org.omg.CORBA.Policy;
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
import net.osm.pki.pkcs.PKCSSingleton;

import org.apache.orb.ORB;
import org.apache.orb.util.IOR;


/**
 * The <code>RepositoryService</code> is a component that provides support 
 * for the registration and retrival of digital certificates.
 *
 * <p><table border="1" cellpadding="3" cellspacing="0" width="100%">
 * <tr bgcolor="#ccccff">
 * <td colspan="2"><font size="+2"><b>Lifecycle</b></font></td>
 * <tr><td width="20%"><b>Phase</b></td><td><b>Description</b></td></tr>
 * <tr>
 * <td width="20%" valign="top">Contextualizable</td>
 * <td>
 * The <code>Context</code> value is not used at this time.</td></tr>
 * 
 * <td width="20%" valign="top">Serviceable</td>
 * <td>
 * The repository is provided with an ORB from which PSS services are aquired.  
 * The default composition profile is detailed below:
 * <pre>
 *  &lt;dependencies&gt;
 *       &lt;dependency&gt;
 *           &lt;role&gt;orb&lt;/role&gt;
 *           &lt;service name="org.apache.orb.ORB" version="2.4"/&gt;
 *           &lt;configuration&gt;
 *             &lt;property name="iiop.port" value="2511" /&gt;
 *             &lt;initializer class="org.apache.pss.Initializer" name="pss"/&gt;
 *           &lt;/configuration&gt;
 *       &lt;/dependency&gt;
 *   &lt;/dependencies&gt;
 * </pre>
 * </td></tr>
 *
 * <tr>
 * <td width="20%" valign="top">Configurable</td>
 * <td>
 * The configuration contains the the parameters for PSS sub-system population
 * and prefgerences concerning IOR publication.  The default configuration is 
 * detailed below: 
 * <pre>
 * 
 *   &lt;configuration>
 * 
 *     <font color="blue"><i>&lt;!--
 *     Service publication
 *     --&gt;</i></font>
 * 
 *     &lt;ior value="repository.ior"/&gt;
 * 
 *     <font color="blue"><i>&lt;!--
 *     The PSS configuration.
 *     --&gt;</i></font>
 * 
 *     &lt;pss&gt;
 * 
 *       &lt;connector value="file" /&gt;
 * 
 *       &lt;session&gt;
 *         &lt;parameter name="PSS.File.DataStore.Directory" value="pss" /&gt;
 *         &lt;parameter name="PSS.File.DataStore.Name" value="pki-repository" /&gt;
 *       &lt;/session&gt;
 * 
 *       &lt;persistence&gt;
 *         &lt;storage psdl="PSDL:osm.net/pki/repository/PrincipalStorageBase:1.0"
 *           class="net.osm.pki.repository.PrincipalStorageBase" /&gt;
 *         &lt;home psdl="PSDL:osm.net/pki/repository/PrincipalStorageHomeBase:1.0"
 *           class="net.osm.pki.repository.PrincipalStorageHomeBase" /&gt;
 *       &lt;/persistence&gt;
 * 
 *     &lt;/pss&gt;
 * 
 *   &lt;/configuration&gt;
 * </pre>
 * </td></tr>
 * 
 * <tr><td  valign="top">Initalizable</td>
 * <td>
 * Repository POA creation, valuetype factory registration and PSS parameterization 
 * are executed during this phase based on the supplied ORB instance.  Initialization
 * will fail if the supplied ORB has not been configured to support PSS. 
 * </td></tr>
 * 
 * <tr><td  valign="top">Executable</td>
 * <td>
 * Utility phase used for validation of composition and initialization phaes. Prints
 * service provider information to the logging channel. 
 * </td></tr>
 * 
 * <tr><td valign="top">Disposable</td>
 * <td>
 * Cleanup and disposal of state members.
 * </td></tr>
 * </table>
 *
 * @author <a href="mailto:mcconnell@osm.net">Stephen McConnell</a>
 */

public class RepositoryServer extends RepositoryPOA 
implements LogEnabled, Configurable, Contextualizable, Serviceable, Initializable, Executable, Disposable, RepositoryService
{
    //==================================================
    // static
    //==================================================

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
    private static RepositoryProviderInfo info;

    //==================================================
    // state
    //==================================================

   /**
    * Manager provided by the container - provides a PSS configured ORB.
    */
    private ServiceManager m_manager;

   /**
    * Configuration provided by the container.
    */
    private Configuration m_config;

   /**
    * Logging channel provided by the container.
    */
    private Logger m_logger;

   /**
    * Application context provided by the container.
    */
    private Context m_context;

   /**
    * Internal reference to the ORB provided by the manager against which
    * valuetype factories are declared.
    */
    private ORB m_orb;

   /**
    * Reference to the Repository POA created during initialization.
    */
    private POA m_poa;

   /**
    * Reference to the PSS Connector.
    */
    private Connector m_connector;

   /**
    * Reference to the PSS Session.
    */
    private Session m_session;

   /**
    * Reference to the PKI Repository object reference.
    */
    private Repository m_repository;

   /**
    * PSS storage home used to persist supplied Principals.
    */
    private PrincipalStorageHome m_home;

    //==================================================
    // Loggable
    //==================================================
    
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
    * @exception IllegalStateException if the logging channel has not been set
    */
    public Logger getLogger()
    {
        return m_logger;
    }

    //=================================================================
    // Contextualizable
    //=================================================================

   /**
    * Set the application context.
    * @param context the application context
    */
    public void contextualize( Context context ) throws ContextException
    {
        if( getLogger().isDebugEnabled() ) getLogger().debug( "contextualize" );
	  m_context = context;
    }

    //=================================================================
    // Serviceable
    //=================================================================
    
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
	  try
	  {
            if( getLogger().isDebugEnabled() ) getLogger().debug( "compose" );
            m_manager = manager;
	  }
	  catch( Exception e )
	  {
		String error = "unexpected exception during composition";
		if( getLogger().isErrorEnabled() ) getLogger().error( error, e );
            throw new ServiceException( error, e );
	  }
    }
    
    //=======================================================================
    // Configurable
    //=======================================================================

   /**
    * Used by a container to supply the static component configuration.
    * @param config the static configuration
    */    
    public void configure( final Configuration config )
    {
	  if( getLogger().isDebugEnabled() ) getLogger().debug( "configuration" );
        m_config = config;
    }

    //=======================================================================
    // Initializable
    //=======================================================================
    
   /**
    * Used by a container to initialize the component.
    * @exception Exception if an error occurs during component initialization
    */
    public void initialize()
    throws Exception
    {
        if( getLogger().isDebugEnabled() ) getLogger().debug( "initialization" );
            
        try
        {
            m_orb = (ORB) m_manager.lookup( "orb" );
            Configuration pss = m_config.getChild("pss");
            String mode = pss.getChild("connector").getAttribute("value","file");
            org.omg.CORBA.Object object = m_orb.resolve_initial_references( "PSS:APACHE:" + mode );
            m_connector = (Connector) ConnectorHelper.narrow( object );
            m_connector.register( pss.getChild("persistence") );
            m_session = m_connector.createBasicSession( pss.getChild("session") );
        }
        catch( Throwable e )
        {
            getLogger().error( "intialization failure", e );
            throw new CascadingException( "Client initization failed.", e);
        }

        //
        // create the POA
        //

        try
	  {
            POA root = POAHelper.narrow( m_orb.resolve_initial_references("RootPOA") );
            m_poa = root.create_POA(
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
            m_poa.activate_object_with_id( ID, this );
            org.omg.CORBA.Object obj = m_poa.id_to_reference(ID);
            m_repository = RepositoryHelper.narrow( obj );
	  }
	  catch( Throwable poaError )
	  {
	      String error = "unable to create the repository object reference";
	      if( getLogger().isErrorEnabled() ) getLogger().error( error, poaError );
	      throw new Exception( error, poaError );
	  }

        //
        // storage home
        //

	  try
	  {
            m_home = (PrincipalStorageHome) m_session.find_storage_home(
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
            String ior = m_config.getChild("ior").getAttribute( "value" );
            try
            {
                IOR.writeIOR( m_orb, m_repository, ior );
                getLogger().debug( "publishing repository IOR to: " + ior );
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
            // IOR publication disabled
        }

        //
        // setup static provider info
        //

        info = new RepositoryProviderInfoBase(
        	standardDescription, standardVersion, productDescription,
	  	productVersion, productVendor, supportedCertificates, supportedCRLs,
	  	supportedCrossCertificates );

        String message = "OSM PKI Repository available";
        getLogger().debug( message );
    }

    //=======================================================================
    // Executable (for testing)
    //=======================================================================

   /**
    * Utility operation used by a container to invoke componet execution.  The 
    * implementation generates provider information to the logging channel.
    */    
    public void execute() throws Exception
    {
        getLogger().info( "execution:\n\n" + getRepository().get_provider_info().toString() );
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
        if( m_home == null ) throw new RepositoryError(
		"Repository storage home has not been initialized.");

	  try 
        {
	      m_home.find_by_name( principal.name );
		throw new DuplicatePrincipal( principal.name );
        }
        catch( NotFound notFound )
        {
		try
	      {
                final String debug = "publishing principal to PSS";
                m_home.create( principal.name, principal );
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
        if( m_home == null ) throw new RepositoryError(
	    "Repository storage home has not been initialized.");
	  try 
        {
	      PrincipalStorage store = m_home.find_by_name( name );
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
        if( m_home == null ) throw new RepositoryError(
		"Repository storage home has not been initialized.");
	  try
        {
	      PrincipalStorage store = m_home.find_by_name( name );
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
        if( m_home == null ) throw new RepositoryError(
		"Repository storage home has not been initialized.");
	  try 
        {
	      PrincipalStorage store = m_home.find_by_name( principal.name );
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
	  return m_repository;
    }

    //=======================================================================
    // Disposable
    //=======================================================================
        
   /**
    * Disposal of the component and related resources.
    */    
    public void dispose()
    {
        if( getLogger().isDebugEnabled() ) getLogger().debug( "disposal" );
        m_manager.release( m_orb );
        try
        {
            m_poa.destroy( true, true );
        }
        catch( Throwable e )
        {
            if( getLogger().isWarnEnabled() ) getLogger().warn( "ignoring POA related exception" );
        }
        m_orb = null;
        m_config = null;
        m_connector = null;
        m_session = null;
        m_home = null;
        m_context = null;
        m_repository = null;
        m_logger = null;
    }
}
