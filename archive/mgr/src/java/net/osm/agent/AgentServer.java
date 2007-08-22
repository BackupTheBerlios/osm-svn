
package net.osm.agent;

import java.io.File;
import java.io.InputStream;
import java.math.BigInteger;
import java.util.Set;
import java.util.List;
import java.util.LinkedList;
import java.util.Iterator;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.Action;

import java.security.Principal;
import java.security.cert.CertPath;
import java.security.cert.X509Certificate;
import javax.security.auth.Subject;
import javax.security.auth.x500.X500Principal;

import org.apache.avalon.phoenix.Block;
import org.apache.avalon.phoenix.BlockContext;
import org.apache.avalon.framework.activity.Disposable;
import org.apache.avalon.framework.activity.Initializable;
import org.apache.avalon.framework.activity.Startable;
import org.apache.avalon.framework.component.Composable;
import org.apache.avalon.framework.component.ComponentManager;
import org.apache.avalon.framework.component.ComponentException;
import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.configuration.DefaultConfigurationBuilder;
import org.apache.avalon.framework.context.Context;
import org.apache.avalon.framework.context.Contextualizable;
import org.apache.avalon.framework.context.ContextException;
import org.apache.avalon.framework.logger.LogEnabled;
import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.avalon.framework.logger.AvalonFormatter;
import org.apache.avalon.framework.logger.Logger;
import org.apache.avalon.framework.logger.LogKitLogger;
import org.apache.log.output.io.FileTarget;
import org.apache.log.Hierarchy;

import org.omg.CORBA_2_3.ORB;
import org.omg.CollaborationFramework.CollaborationSingleton;
import org.omg.CommunityFramework.Criteria;
import org.omg.CommunityFramework.Community;
import org.omg.CommunityFramework.CommunityHelper;
import org.omg.Session.AbstractResource;
import org.omg.Session.AbstractResourceHelper;
import org.omg.Session.User;
import org.omg.Session.SessionSingleton;
import org.omg.PKIAuthority.RegistrationAuthority;
import org.omg.PKIAuthority.RegistrationAuthorityHelper;
import org.omg.PKIAuthority.RequestCertificateManager;
import org.omg.PKI.CertificateListHolder;
import org.omg.PKI.Continue;
import org.omg.PKI.ContinueHolder;
import org.omg.PKI.Certificate;
import org.omg.PKI.PKISuccess;
import org.omg.PKI.PKISuccessWithWarning;
import org.omg.PKI.PKIContinueNeeded;
import org.omg.PKI.PKIFailed;
import org.omg.PKI.PKIPending;
import org.omg.PKI.PKISuccessAfterConfirm;

import net.osm.pki.authority.RegistrationAuthoritySingleton;
import net.osm.pki.base.PKISingleton;

import net.osm.audit.AuditService;
import net.osm.audit.home.Manager;
import net.osm.audit.home.Adapter;
import net.osm.entity.EntityService;
import net.osm.entity.EntityServer;
import net.osm.hub.home.Finder;
import net.osm.hub.home.FinderHelper;
import net.osm.hub.home.ResourceFactory;
import net.osm.orb.ORBService;
import net.osm.pki.base.ContinueBase;
import net.osm.pki.pkcs.PKCS10;
import net.osm.pki.pkcs.PKCS10Wrapper;
import net.osm.pki.pkcs.PKCSSingleton;
import net.osm.realm.StandardPrincipal;
import net.osm.realm.StandardPrincipalBase;
import net.osm.realm.RealmSingleton;
import net.osm.realm.PrincipalManager;
import net.osm.realm.PrincipalManagerBase;
import net.osm.shell.Shell;
import net.osm.shell.vault.VaultEntity;
import net.osm.shell.Service;
import net.osm.shell.Entity;
import net.osm.shell.GenericAction;
import net.osm.util.IOR;
import net.osm.util.ExceptionHelper;
import net.osm.vault.Vault;
import net.osm.vault.PrincipalEvent;
import net.osm.vault.PrincipalListener;
import net.osm.vault.LocalVault;

/**
 * The AgentServer provides support for the creation of Business Agents 
 * representing people, places, things, work-in-progress, communities, collaborative
 * encounters, and the business rules that enable collaborative interaction.
 * The <code>AgentServer</code> provides a set of operations enabling agent creation, 
 * event management and persistent logging of structured events backed by a business 
 * object platform that maintains the persistent state of the underlying object.
 *
 * The <code>AgentServer</code> implements the interface <code>Service</code> 
 * interface and a such provides a set of horizontal management services into the 
 * user's desktop environment. These services include the establishment of a principal
 * <code>User</code> against which asynchrouse tasks can be executed, and the rresources
 * can be maintained a shared with other users.  A set of supporting processes available 
 * to the user include digital identity and community membership management.
 *
 * @author Stephen McConnell
 */

public class AgentServer extends EntityServer
implements Block, Contextualizable, Configurable, Composable, Initializable, Startable, Disposable, AgentService, PrincipalListener, Service
{

    //=========================================================
    // static
    //=========================================================

   /**
    * Trace mode.
    */
    private static boolean trace = false;

    //=========================================================
    // state
    //=========================================================

   /**
    * The runtime ORB
    */
    private ORB orb;


   /**
    * The configuration is an in memory representation of the XML assembly file used 
    * as a container of static configuration values.
    */
    private Configuration configuration;

   /**
    * Singleton reference to the agent factory.
    */
    private static AgentServer server;

   /**
    * Singleton reference to a root Agent representing a point-of-presence
    * on the primary server.
    */
    private static Agent root;

   /**
    * Internal stack of recently accessed agent instances.
    */
    protected static Cache cache;

   /**
    * The componentManager contains the reference to supporting services (where 
    * services are mapped under a role string).
    */
    private ComponentManager componentManager;

   /**
    * The shell within which agents can be presented.
    */
    private Shell shell;

   /**
    * Factory finder reference.
    */
    private static Finder finder;

   /**
    * The root community from the finder.
    */
    private static Community community;

    private RegistrationAuthority authority; // to be removed

    private UserAgent userAgent;

    private Vault vault;

    private AuditService audit;

    private boolean interactive;

   /**
    * Application context
    */
    private BlockContext context;

   /**
    * List of tools to be published by the agent server.
    */
    private List toolsList;
    
    //=================================================================
    // Contextualizable
    //=================================================================

    public void contextualize( Context context ) throws ContextException
    {
	  getLogger().debug("commencing agent contextualize phase");
        super.contextualize( context );
	  if( context instanceof BlockContext ) 
	  {
	      this.context = (BlockContext) context;
	      getLogger().debug("agent contextualize phase complete");
	  }
	  else
	  {
		throw new ContextException("Supplied context does not implement BlockContext.");
	  }
    }

    //=========================================================
    // Composable implementation
    //=========================================================

    /**
     * Pass the <code>ComponentManager</code> to the <code>Composable</code>.
     * The <code>Composable</code> implementation should use the specified
     * <code>ComponentManager</code> to acquire the components it needs for
     * execution.
     *
     * @param manager The <code>ComponentManager</code> which this
     *                <code>Composer</code> uses.
     * @exception ComponentException
     */
    public void compose( ComponentManager manager )
    throws ComponentException
    {
        super.compose( manager );
	  try
	  {
	      getLogger().debug("commencing agent composition phase");
            componentManager = manager;
            orb = ((ORBService)componentManager.lookup("ORB")).getOrb();
            audit = (AuditService) componentManager.lookup("AUDIT");
		shell = (Shell) componentManager.lookup("SHELL");
		vault = (Vault) componentManager.lookup("VAULT");
	      getLogger().debug("agent composition phase completed");
       }
        catch( Exception e )
        {
		final String error = "unexpected exception during agent composition phase";
		ExceptionHelper.printException( e );
	      if( getLogger().isErrorEnabled() ) getLogger().error( error, e );
		throw new ComponentException("unexpected exception during agent composition phase", e );
        }
    }

    //=========================================================
    // Configurable implementation
    //=========================================================
    
   /**
    * Configuration of the runtime environment based on a supplied Configuration arguments, 
    * including the configuration for an underlying audit services.
    *
    * @param config static configuration block.
    * @exception ConfigurationException if the supplied configuration is incomplete or badly formed.
    */
    public void configure( final Configuration config )
    throws ConfigurationException
    {
        super.configure( config );
	  if( getLogger().isDebugEnabled() ) getLogger().debug("configuration");
        if( null != configuration ) throw new ConfigurationException( 
		"Configurations for block " + this + " already set." );
        this.configuration = config;
    }

    //=========================================================
    // Initializable implementation
    //=========================================================

   /**
    * Initialization is invoked by the framework following configuration.  During 
    * this phase the implementation establishes a transient agent stack that serves
    * as a cache.  Each agent in the cache is associated with a persistent business 
    * object and event history.
    *
    * @exception Exception
    */
    public void initialize()
    throws Exception
    {
        super.initialize();
        try
        {

	      getLogger().debug("commencing agent intialization phase");

		//
		// register valuetypes with the ORB
		//

            CollaborationSingleton.init( orb );
	      RegistrationAuthoritySingleton.init( orb );
		PKISingleton.init( orb );
		PKCSSingleton.init( orb );
		RealmSingleton.init( orb );
		CollaborationSingleton.init( orb );

		server = this;

		//
		// setup the cache
		//

		cache = new Cache( configuration.getAttributeAsInteger("cache",100));

		//
		// setup the vault
		//

		interactive = configuration.getAttributeAsBoolean("interactive", true );
            updatePrincipal( vault.getCertificatePath() );
		vault.addPrincipalListener( this );

	      final String banner = "OSM Business Agent Framework";
	      if( getLogger().isInfoEnabled() ) getLogger().info( banner );

	  }
        catch( Throwable e )
	  {
		ExceptionHelper.printException( e );
            if( getLogger().isErrorEnabled() ) getLogger().error( "Agent initialization failure", e );
		throw new Exception( "Agent initialization failure", e );
        }


        getLogger().debug("agent initialization complete"); 
    }


    //=========================================================
    // Startable
    //=========================================================

   /**
    * The <code>start</code> method handles the startup of the underlying
    * event auditing service and registration of the agent service with the 
    * shell.
    * @exception Exception
    * @osm.warning this will be updated so that resolution of the user is 
    *   moved out to a seperate callable action
    */
    public void start() throws Exception
    {

 	  getLogger().debug("starting agent server");
        try
        {
		Finder finder = getFinder();
		User user = finder.resolve_user( true );
		userAgent = (UserAgent) resolve( user );
		put( "USER", userAgent );
        }
        catch( Exception e )
	  {
		String error = "Failed to establish user agent.";
            if( getLogger().isErrorEnabled() ) getLogger().error( error , e );
 		ExceptionHelper.printException( "AGENT INSTALL: " + error , e, this, true );
		throw new Exception( error, e );
        }

	  try
	  {
		setRoot( this.getCommunity( ) );
		if(( shell != null ) && ( interactive == true ))
		{
		    getLogger().info( "commencing install phase" );
		    shell.install( userAgent ); 
		    shell.install( getRoot() );
		}

        }
        catch( Exception e )
	  {
            if( getLogger().isErrorEnabled() ) getLogger().error( "Install failure.", e );
 		ExceptionHelper.printException( "AGENT INSTALL", e, this, true );
		throw new RuntimeException( "Failed to install root agent into the shell. ", e );
        }

   	  getLogger().debug("agent server started");
    }

    //==========================================================================
    // Stoppable
    //==========================================================================

    public void stop()
    throws Exception
    {
        if( getRoot() != null ) getRoot().dispose();
   	  getLogger().debug("agent server stopped");
    }

    //==========================================================================
    // Disposable
    //==========================================================================

   /**
    * Handles disposal of this block.
    */  
    public void dispose()
    {
	  final String message = "disposing of the agent server";
   	  if( getLogger().isInfoEnabled() ) getLogger().info( message );

        // do agent server stuff here

	  try
	  {
		if( userAgent != null ) userAgent.dispose();

	      super.dispose();
	  }
	  catch( Throwable e )
	  {
	      final String warning = "unexpected exception during dispose";
		if( getLogger().isWarnEnabled() ) getLogger().warn( warning, e );
	  }
    }

   /**
    * Returns a list of Action instances to be installed as tool
    * menu items within the desktop for the lifetime of the service.
    */
    public List getTools( )
    {
        if( toolsList != null ) return toolsList;
        toolsList = new LinkedList();
	  toolsList.add( new CertificationRequestActivity( 
          "Certification Request", shell, vault, userAgent, getFinder().resource_factory(), this ) );
        toolsList.add( new CertificationAuthorityTest(
          "Certification Authority Test", shell, vault, userAgent, getFinder().resource_factory(), this, orb ) );

	  return toolsList;
    }

   /**
    * Returns an Action instances to be installed as 
    * menu items within the desktop preferences menu group.
    */
    public Action getPreferencesAction( )
    {
        return new GenericAction( "General Preferences", this, "handlePreferences", true );
    }

    public void handlePreferences()
    {
	  System.out.println("do preferences stuff now");
    }

    //=========================================================
    // PrincipalListener
    //=========================================================

   /**
    * Notify the listener of a change to the principal.
    * @osm.warning impelmentation assumes local client execution - does
    *   not support modification of a principal established by a 
    *   web-client
    */
    public void principalChanged( PrincipalEvent event )
    {
	  try
	  {
            updatePrincipal( vault.getCertificatePath( ) );
	  }
	  catch( Exception e )
	  {
		throw new RuntimeException("AgentServer. Unable to get certificate path.", e );
	  }
    }

   /**
    * Updates the <code>PrincipalManagerBase</code> with the 
    * new principal's certificate path.
    */
    private void updatePrincipal( CertPath path )
    {
        try
        {
	      PrincipalManagerBase manager = (PrincipalManagerBase)
			orb.resolve_initial_references("PRINCIPAL");
		manager.setLocalPrincipal( new StandardPrincipalBase( path ));
	  }
	  catch( Throwable e )
	  {
		String error = "Failed to set principal.";
		ExceptionHelper.printException( 
		  "AgentServer, PRINCIPAL RESOLVER", e, this, true );
            if( getLogger().isErrorEnabled() ) getLogger().error( error , e );
		throw new RuntimeException( error, e );
	  }
    }

    //=========================================================
    // AgentService implementation
    //=========================================================

   /**
    * Returns the ORB established during the initalization phase.
    */
    public ORB getOrb()
    {
       return orb;
    }

   /**
    * Returns a <code>UserAgent</code> instance representing the 
    * principal user of the agent service.  This agent is typically 
    * used by the framework for task and message management.
    */
    public UserAgent getUserAgent()
    {
        return userAgent;
    }

   /**
    * Method used following server startup to establish the reference to a 
    * root object.
    */

    public Agent setRoot( Object object )
    {
	  if( object == null ) throw new RuntimeException("Null value supplied to setRoot.");
	  try
        {
		root = (Agent) resolve( object );
            return root;
        }
        catch( Exception e )
        {
            throw new RuntimeException("Failed to resolve root agent.", e );
        }
    }
    
   /**
    * Returns the root agent.
    * @osm.warning uses 'getCommunity' - will be changed to use
    *   configuration based name and finder lookup
    */
    public Agent getRoot()
    {
	  if( this.root != null ) return this.root;
        try
        {	  
	      return setRoot( getCommunity() );
        }
        catch( Exception e )
        {
		if( getLogger().isErrorEnabled() ) getLogger().error( "failed to resolve root community", e );
		throw new RuntimeException( "failed to resolve root community", e );
        }
    }

    //==========================================================================
    // Internal utilities
    //==========================================================================


   /**
    * Returns a factory finder reference.
    */
    public Finder getFinder()
    {
        if( this.finder != null ) return finder;
	  try
	  {
            Configuration c = configuration.getChild("finder");
            this.finder = FinderHelper.narrow( 
              IOR.readIOR( orb, c.getAttribute("ior","finder.ior") ) );
        }
        catch( Exception e )
	  {
		if( getLogger().isErrorEnabled() ) getLogger().error( "failed to resolve finder", e );
		throw new RuntimeException( 
              "Unable to resolve finder", e );
        }
        return finder;
    }

   /**
    * Returns the root community.
    * @deprecated
    */
    private Community getCommunity()
    {
	  if( this.community != null ) return this.community;
        try
        {
            //this.community = getFinder().community();
            this.community = CommunityHelper.narrow( getFinder().lookup("osm.planet") );
		return this.community;
        }
        catch( Exception e )
        {
		if( getLogger().isErrorEnabled() ) getLogger().error( "failed to resolve community", e );
		throw new RuntimeException( 
              "Unable to resolve community", e );
        }
    }
}

//================================================================================

   /**
    * Handle a PKCS10 request.
    */
/*
    private void handlePKCS10Request( ) throws Exception
    {
        //
        // Prepare a PKCS#10 certificate request and include
	  // it in the list of public credentials
        //
		
        getLogger().debug("commencing PKS#10 request" );
		
        //
	  // locate an X509 principal and use that to 
	  // create a certificate request
	  //

	  Set paths = subject.getSubject().getPublicCredentials( CertPath.class );
	  if( paths.size() == 0 )
	  {
	      //
	      // This user does not have any public credentials.
	      // 

	      throw new Exception("Operator does not have a public credential. " +
			"Please verify local keystore and security policy." );
	  }

        try
        {
            shell.setMessage("Creating PKCS#10 certificate request");
		request = new PKCS10Wrapper( subject.getSubject() );
	  }
	  catch( Exception e )
	  {
	      String warning = "Failed to create a new PKCS10 certificate request.";
		shell.setMessage( warning );
	      throw new Exception( warning, e );
	  }

        try
	  {
		//
		// get the PKI RA and issue the PKCS#10 request
		//

		shell.setMessage( "Locating certification authority." );
	      Configuration authorityConfig = configuration.getChild( "authority" );
	      String iorPath = authorityConfig.getAttribute(
			"ior", "http://home.osm.net/planet/authority.ior" ); 
		org.omg.CORBA.Object object = IOR.readIOR( orb, iorPath );
		authority = RegistrationAuthorityHelper.narrow( object );
        }
        catch( Exception e )
        {
	      String warning = "Failed to locate the registration authority.";
   	      if( getLogger().isErrorEnabled() ) getLogger().error( warning, e );
		shell.setMessage( warning );
		throw new Exception( warning, e );
	  }

        try
        {
	      //
		// issue the PKCS#10 request
		//

		shell.setMessage( "Registering certification request." );
		RequestCertificateManager manager = 
		  authority.request_certificate( request );
		shell.setMessage( "Processing Registration Authority response." );

		//
		// The osm.planet PKI is configured to provide a minimal 
		// level certificate providing the above generated request 
		// is valid
		//

		CertificateListHolder certificates = new CertificateListHolder( );
		ContinueHolder continuation = new ContinueHolder( );
		int status = manager.get_certificate_request_result( certificates, continuation );

		//
		// check out the response
	      //

		String message = "";
		if( status == PKIFailed.value )
		{
		    //
		    // Need to unpack the result and get the reason for the failure
		    //
		    message = "Certificate request was not sucessful.";
		}
            else if( status == PKIPending.value )
	      {
		    //
		    // Need to set up a thread to monitor the RA
		    //
		    message = "Certificate request in process.";
	      }
            else if( status == PKIContinueNeeded.value )
	      {
		    //
		    // Need to unpack the result and prepare an input request for 
		    // the supplimentary informaton
		    //
		    message = "Certificate request pending supplimentary information.";
	      }
            else if( status == PKISuccessWithWarning.value )
	      {
		    //
		    // Need to update the pricipal but also post a warning message to
		    // the operator
		    //
		    message = "Certificate request successfull (with modifications).";
	      }
            else if( status == PKISuccess.value )
	      {
		    //
		    // Need to update the principal and a post a notification message to
		    // the operator
		    //
		    // NOTE:
		    // ignoring continue type
		    // ignoring content
		    // just grab the certificates
                //

		    message = "Certificate request successfull.";
		    byte[] pkcs7array = continuation.value.getEncoded();
		    System.out.println("CONTINUE: " + pkcs7array );
		    sun.security.pkcs.PKCS7 pkcs7 = new sun.security.pkcs.PKCS7( pkcs7array );
		    //sun.security.pkcs.ContentInfo info = pkcs7.getContentInfo();
		    X509Certificate[] certs = pkcs7.getCertificates();
		    for( int i=0; i<certs.length; i++ )
                {
                    System.out.println("CERTIFICATE:\n" + certs[i]);
                }
	      }
            else if( status == PKISuccessAfterConfirm.value )
	      {
		    //
		    // Need to issue a confirmation
		    //
		    message = "Certificate request pending confirmation.";
	      }
		getLogger().debug( message );
		shell.setMessage( message );
        }
	  catch( Exception e )
	  {
	      String warning = 
	        "Failed to register certificate request with the Registration Authority.";
	      if( getLogger().isErrorEnabled() ) getLogger().error( warning, e );
		shell.setMessage( warning );
		throw new Exception( warning, e );
	  }
    }
*/

