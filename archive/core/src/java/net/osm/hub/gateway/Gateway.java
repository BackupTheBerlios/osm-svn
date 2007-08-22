/**
 */

package net.osm.hub.gateway;

import java.util.Iterator;
import java.io.File;
import java.io.OutputStream;
import java.util.Properties;
import java.util.Hashtable;
import java.util.Random;
import java.security.Principal;
import java.security.cert.CertPath;

import org.apache.avalon.framework.logger.Logger;
import org.apache.avalon.framework.logger.LogEnabled;
import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.avalon.framework.component.Component;
import org.apache.avalon.framework.component.ComponentManager;
import org.apache.avalon.framework.component.DefaultComponentManager;
import org.apache.avalon.framework.component.ComponentException;
import org.apache.avalon.framework.component.Composable;
import org.apache.avalon.framework.context.Context;
import org.apache.avalon.framework.context.Contextualizable;
import org.apache.avalon.framework.context.ContextException;
import org.apache.avalon.framework.activity.Disposable;
import org.apache.avalon.framework.activity.Initializable;
import org.apache.avalon.framework.activity.Startable;
import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.phoenix.Block;
import org.apache.avalon.phoenix.BlockContext;

import org.omg.CORBA_2_3.ORB;
import org.omg.CORBA.ORBPackage.InvalidName;
import org.omg.CORBA.Policy;
import org.omg.CORBA.PolicyManager;
import org.omg.CORBA.SetOverrideType;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.POAHelper;
import org.omg.PortableServer.IdAssignmentPolicyValue;
import org.omg.PortableServer.RequestProcessingPolicyValue;
import org.omg.PortableServer.ImplicitActivationPolicyValue;
import org.omg.PortableServer.LifespanPolicyValue;
import org.omg.PortableServer.IdUniquenessPolicyValue;
import org.omg.PortableServer.ServantRetentionPolicyValue;
import org.omg.CosPersistentState.Connector;
import org.omg.CosPersistentState.Session;
import org.omg.CosPersistentState.Parameter;
import org.omg.CosPersistentState.READ_WRITE;
import org.omg.CosPersistentState.NotFound;
import org.omg.CosNotifyComm.StructuredPushSupplier;
import org.omg.CosTime.TimeServiceHelper;
import org.omg.CosTime.TimeService;
import org.omg.CosLifeCycle.FactoryFinder;
import org.omg.NamingAuthority.AuthorityId;
import org.omg.NamingAuthority.RegistrationAuthority;
import org.omg.Session.AbstractResourceHelper;
import org.omg.Session.WorkspaceHelper;
import org.omg.CommunityFramework.MembershipModel;
import org.omg.CommunityFramework.MembershipPolicy;
import org.omg.CommunityFramework.PrivacyPolicyValue;
import org.omg.CommunityFramework.Role;
import org.omg.CommunityFramework.Criteria;
import org.omg.CollaborationFramework.CollaborationSingleton;

import net.osm.hub.home.ResourceFactory;
import net.osm.hub.home.ResourceFactoryHelper;
import net.osm.hub.pss.DomainStorage;
import net.osm.hub.pss.DomainStorageHome;
import net.osm.hub.pss.CommunityStorage;
import net.osm.hub.pss.CommunityStorageRef;
import net.osm.hub.pss.CommunityStorageBaseRef;
import net.osm.hub.pss.ProcessorStorageHome;
import net.osm.hub.pss.GenericStorageHome;
import net.osm.util.IOR;
import net.osm.util.ExceptionHelper;
import net.osm.util.Incrementor;
import net.osm.time.TimeUtils;
import net.osm.realm.RealmSingleton;
import net.osm.realm.AccessManager;
import net.osm.realm.PrincipalManager;
import net.osm.realm.PrincipalManagerHelper;
import net.osm.realm.PrincipalManagerBase;
import net.osm.realm.StandardPrincipal;
import net.osm.realm.StandardPrincipalBase;
import net.osm.vault.Vault;
import net.osm.vault.LocalVault;
import net.osm.hub.user.UserService;
import net.osm.hub.workspace.WorkspaceService;
import net.osm.hub.desktop.DesktopService;
import net.osm.hub.task.TaskService;
import net.osm.hub.processor.ProcessorService;
import net.osm.hub.community.CommunityService;
import net.osm.hub.generic.GenericResourceService;
import net.osm.orb.ORBConfigurationHelper;
import net.osm.orb.ORBService;
import net.osm.orb.ORBServer;
import net.osm.pss.PSSSessionService;
import net.osm.pss.PSSConnectorService;
import net.osm.pss.PersistanceHandler;
import net.osm.pss.PSSServer;

/**
 * The Gateway class is the pricipal startup component of the OSM Gateway platform and
 * is responsible for ensuring proper configuration, initialization and execution based 
 * on supplied configuration parameters.
 * The Gateway is responsible for the establishment of the application context, ORB, 
 * storage catalogs, time service, factory support, portable object adapters (POAs) 
 * and related servants, the root business domain, and the mappings between business
 * processes and appliance adapters.
 * 
 * <p><table border="1" cellpadding="3" cellspacing="0" width="100%">
 * <tr bgcolor="#ccccff">
 * <td colspan="2"><b><code>Gateway</code>Lifecycle Phases</b></td>
 * <tr><td width="20%"></td><td><b>Description</b></td></tr>
 * <tr><td width="20%"><b>Contextualizable</b></td>
 * <td>
 * The <code>Context</code> value passed to the gateway during this phase
 * provides the runtime execution context including the root application directory 
 * from which supporting resources can be internalized during the subsequent compositioin,
 * configuration and initalization phases.</td></tr>
 * <tr><td width="20%"><b>Composable</b></td>
 * <td>
 * The composition phase handles association of service blocks to which the gateway
 * is dependent.  Current dependecies include the <code>CosTime</code> time service block.
 * </td></tr>
 * <tr><td width="20%"><b>Configurable</b></td>
 * <td>
 * The configuration phase handles the internalization of a static configuration data 
 * including ORB bootstrap properties, ORB specific execution properties, site domain
 * name, persistence mapping, declaration of the DPML repository, and the mapping of 
 * DPML criteria to enterprise application appliance implementations (back-end wrappers).
 * </td></tr>
 * <tr><td width="20%"><b>Initalizable</b></td>
 * <td>
 * The initialization phases handles the initialization of the underlying Object Request 
 * Broker (ORB), security vault loading and pricipal establishment, PSS initialization
 * and persistence session establishment, creation of the root domain identity, access 
 * manager initialization, establishment of the dynamic servant locator, and factories. 
 * </td></tr>
 * <tr><td width="20%"><b>Startable</b></td>
 * <td>
 * Handles startup and shutdown of the block, including the establishment and subsequent
 * termination (shutdown) of the underlying ORB. 
 * </td></tr>
 * <tr><td width="20%"><b>Disposable</b></td>
 * <td>
 * Handles the release of resources following completion of block execution.
 * </td></tr>
 * </table>
 *
 * @author <a href="mailto:mcconnell@osm.net">Stephen McConnell</a>
 */

public class Gateway extends DefaultComponentManager
implements Block, LogEnabled, Composable, Configurable, Contextualizable, Initializable, Startable, Disposable, ORBService, PSSConnectorService, PSSSessionService, Registry, DomainService, RandomService, ResourceFactoryService
{

   /**
    * Random seed against which constant random identifiers are generated.
    */
    private static final Random seed = new Random();

   /**
    * The configuration is an in memory representation of the XML assembly file used 
    * as a container of static configuration values.
    */
    private Configuration configuration;

   /**
    * The default logger for the gateway.
    */
    //private Logger log;

   /**
    * The main server object request broker.
    */
    private ORB orb;

   /**
    * The root portable object adapter.
    */
    private POA root;

   /**
    * Port on which the gateway will listen for incomming invocations.  Can be 
    * modified through the configuration file.
    */
    private String port = "2061";

   /**
    * Default domain authority type.  
    * Can be modified through the configuration file.
    */
    private RegistrationAuthority authority = RegistrationAuthority.DNS;

   /**
    * Default naming entity.
    * Can be modified through the configuration file.
    */
    private String address = "localhost";

   /**
    * DomainStorage object representing the domain that this installation is
    * identified by.  Value is a function of the supplied authority and address.
    */
    private DomainStorage domain;

   /**
    * ResourceFactory object reference.
    */
    private ResourceFactory factory;

   /**
    * Thread used to run the gateway orb.
    */
    private Thread thread;

   /**
    * PSS connector.
    */
    Connector connector = null;

   /**
    * Object reference to the TimeService.
    */
    private TimeService time;

   /**
    * The gateway root portable object adapter.
    */
    private POA poa;

   /**
    * The POA for transient iterators.
    */
    private POA iteratorPOA;

   /**
    * Mapping of criteria labels to factory implementations.
    */
    private Configuration mapping;

   /**
    * Server vault.
    */
    private Vault vault;

   /**
    * The principal representing the server.
    */
    private StandardPrincipal principal;

   /**
    * Base directory for the application.
    */
    File baseDirectory;

   /**
    * Application context
    */
    BlockContext context;

   /**
    * The factory servant.
    */
    ResourceFactoryServant factoryServant;

   /**
    * PSS session
    */
    Session session;

   /**
    * Logging channel.
    */
    Logger log;

    UserService userService;
    WorkspaceService workspaceService;
    DesktopService desktopService;
    TaskService taskService;
    ProcessorService processorService;
    CommunityService communityService;
    GenericResourceService genericResourceService;
    private ORBServer orbServer;

    private PSSServer pss;
    
    //=================================================================
    // Loggable
    //=================================================================
    
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

    public void contextualize( Context context ) throws ContextException
    {
	  if( context instanceof BlockContext ) 
	  {
	      this.context = (BlockContext) context;
	  }
	  else
	  {
		throw new ContextException("Supplied context does not implement BlockContext.");
	  }
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
        time = (TimeService) manager.lookup("CLOCK");
        vault = (Vault) manager.lookup("VAULT");
        put( "ORB", manager.lookup("ORB"));
	  orb = ((ORBService)lookup("ORB")).getOrb();
    }

    //==========================================================================
    // Configurable
    //==========================================================================
    
   /**
    * Configuration of the runtime environment based on a supplied Configuration arguments
    * which contains the general arguments for ORB initalization, PSS subsystem initialization, 
    * PSDL type to class mappings, preferences and debug information.
    *
    * @param config Configuration representing an internalized model of the assembly.xml file.
    * @exception ConfigurationException if the supplied configuration is incomplete or badly formed.
    */
    public void configure( final Configuration config )
    throws ConfigurationException
    {
        if( null != configuration )
        {
            throw new ConfigurationException( "Configurations for block " + this +
            " already set" );
        }
        this.configuration = config;
    }

    //=================================================================
    // Initializable
    //=================================================================
    
   /**
    * Initialization is invoked by the framework following configuration, during which 
    * the underlying ORB is initialized, the PSS session is created and configured, and 
    * the servant locators and POAs for factories are created.
    */
    public void initialize()
    throws Exception
    {
        if( getLogger().isDebugEnabled() ) getLogger().debug("starting intialization");

        //
        // Initialize the root POA
	  //

        try
        {
            root = POAHelper.narrow(orb.resolve_initial_references("RootPOA"));
            CollaborationSingleton.init( orb );
	      RealmSingleton.init( orb );
        }
        catch ( InvalidName e )
        {
            if( getLogger().isFatalErrorEnabled() ) getLogger().fatalError(
	        "cannot locate 'RootPOA' initial service", e );
            throw new Exception("cannot locate 'RootPOA' initial service", e );
        }

	  //
	  // Setup the principal representing the server (when the server 
        // executes any resource creation actions and when the server acts as 
        // as a client).
        //
        
        try
        {
	      PrincipalManagerBase manager = (PrincipalManagerBase)
			orb.resolve_initial_references("PRINCIPAL");
		principal = new StandardPrincipalBase( vault.getCertificatePath() );
		manager.setLocalPrincipal( principal );
        }
        catch (Exception e)
        {
            String error = "GATEWAY principal establishment error";
            if( getLogger().isErrorEnabled() ) getLogger().error( error, e );
	      ExceptionHelper.printException( error, e, this, true );
        }
        
        //
        // PSS subsystem
        //
        
	  try
	  {
		pss = new PSSServer();
		pss.enableLogging( getLogger().getChildLogger("pss") );
		pss.compose( this );
	      pss.configure( configuration.getChild("pss") );
		pss.initialize();

            put( "PSS-CONNECTOR", pss );
            put( "PSS-SESSION", pss );
		connector = pss.getPSSConnector();
		session = pss.getPSSSession();

            if( getLogger().isDebugEnabled() ) getLogger().debug("pss extension");
            Configuration persistence = configuration.getChild("persistence");
	      PersistanceHandler.register( connector, persistence );
            if( getLogger().isDebugEnabled() ) getLogger().debug("PSS initialization complete");            
        } 
        catch (Throwable e)
        {
		final String error = "PSS extension failed";
            throw new Exception( error, e );
        }
        
        //
        // domain registration
        //

	  DomainStorageHome domainHome = null;
	  try
	  {
            domainHome = (DomainStorageHome) session.find_storage_home(
 		      "PSDL:osm.net/hub/pss/DomainStorageHomeBase:1.0"  );
	  }
	  catch( Throwable e )
        {
		String error = "failed to resolve the domain home";
		throw new Exception( error, e );
	  }

        if( domainHome == null )
        {
            String s = "Domain home unavailable.";
            throw new RuntimeException(s);
        }

	  //
	  // If the domain name and type declared in the configuration does 
        // not point to a persistent domain storage entry, then create a new 
        // domain entry.
        //
        // NOTE: addition content needed here to establish the domain 
        // digital identity, registration of the new identity with 
        // osm.planet, and issuance of a set of certificates that 
        // uniquely identify this server.
        //

        try
        {        
            String str = "DNS";
            try
            {
                str = this.configuration.getChild("domain").getAttribute( "authority", str );
            
                if( str.equals("OTHER"))
                {
                    authority = RegistrationAuthority.OTHER;
                } else if( str.equals("ISO"))
                {
                    authority = RegistrationAuthority.ISO;
                } else if( str.equals("DNS"))
                {
                    authority = RegistrationAuthority.DNS;
                } else if( str.equals("IDL"))
                {
                    authority = RegistrationAuthority.IDL;
                } else if( str.equals("DCE"))
                {
                    authority = RegistrationAuthority.DCE;
                } else
                {
                    if( getLogger().isErrorEnabled() ) getLogger().error(
                    "bad domain attribute value '" + str + "' supplied for authority - " +
                    "value must be one of 'ISO', 'DNS', 'IDL', 'DCE' or 'OTHER' " +
                    "- using default value of 'DNS'");
                    authority = RegistrationAuthority.DNS;
                }
            }
            catch (Exception e)
            {
                getLogger().info("setting domain authority to DNS default" );
            }
        
            address = this.configuration.getChild("domain").getAttribute( "address", address );
            getLogger().info("setting domain address to " + address );
            domain = domainHome.find_by_domain_key( authority.value(), address );
		if( getLogger().isDebugEnabled() ) getLogger().debug("domain already exists");
        } 
	  catch (NotFound e)
        {
            domain = domainHome.create( authority.value(), address );
		if( getLogger().isDebugEnabled() ) getLogger().debug("created new domain entry");
            session.flush();
        }
        catch(Exception e)
	  {
		final String error = "unexpected exception while resolving local domain";
		throw new Exception( error, e );
	  }

        //
        // configure the singleton access control service with the PSS Session
        //
        
        try
        {
            AccessManager.setSession( session );
        }
        catch(Exception e)
	  {
	      if( getLogger().isErrorEnabled() ) getLogger().error(
		  "unexpected exception while configuring access control service", e);
		throw e;
	  }
        
        //
        // create resource factory POA
        //
        
        try
        {

            getLogger().info("creating resource factory " );
            
            //
            // create the POA
            //
            
            POA factoryPOA = root.create_POA
            (
            "Factory",
            root.the_POAManager(),
            new Policy[]
            {
                root.create_id_assignment_policy( IdAssignmentPolicyValue.USER_ID ),
                root.create_lifespan_policy( LifespanPolicyValue.PERSISTENT )
            }
            );
            
            //
            // create and bind a servant to the factory
            //
            
            factoryServant = new ResourceFactoryServant( );
            factoryServant.enableLogging( getLogger().getChildLogger("FACTORY") );
            
            byte[] ID = "FACTORY".getBytes();
            factoryPOA.activate_object_with_id( ID, factoryServant );
            org.omg.CORBA.Object obj = factoryPOA.id_to_reference(ID);
            factory = ResourceFactoryHelper.narrow( obj );
            put("FACTORY", this );
            
            factoryServant.compose( this );
	      factoryServant.configure( this.configuration.getChild("dpml"));
            factoryServant.initialize( );

	      final String banner = "OSM Collaboration Hub";
            getLogger().info( banner );
            System.out.println( banner );
        }
        catch (Exception e)
        {
            final String error =  "failed to establish the factory servant";
            throw new Exception( error, e );
        }
    } 
    
    //=======================================================================
    // Startable
    //=======================================================================
    
   /**
    * The start operation is invoked by the framework following completion of the 
    * initialization phase, during which a new thread is created for the execution
    * of the ORB (and resulting startup of the POA tree). 
    */
    public void start()
    throws Exception
    {
        
        //
        // activate the root POA and start the ORB server
        //

        root.the_POAManager().activate();
        pss.start();
        if( getLogger().isInfoEnabled() ) getLogger().info("Gateway the started" );
    }
    
    /**
     * Stops the component.
     */
    public void stop()
    throws Exception
    {
	  pss.stop();
        getLogger().info("stopping the gateway" );
    }

    //=======================================================================
    // Disposable
    //=======================================================================

   /**
    * Notification by the framework requesting disposal of this component, resulting
    * in the shutdown of the ORB.
    */
    public synchronized void dispose()
    {
        getLogger().info("closing gateway" );
    }

    //=======================================================================
    // ORBService
    //=======================================================================

   /**
    * Returns the current ORB for the purpose of valuetype initialization and other 
    * ORB related operations.
    */
    public ORB getOrb( )
    {
        return orb;
    }

    //=======================================================================
    // PSSConnectorService
    //=======================================================================

   /**
    * Returns the PSS connector
    * @return Connector PSS storage connector
    */
    public Connector getPSSConnector( )
    {
        return connector;
    }

    //=======================================================================
    // PSSSessionService
    //=======================================================================

   /**
    * Returns the PSS session
    * @return Session PSS session
    */
    public Session getPSSSession( )
    {
        return session;
    }

    //=======================================================================
    // DomainService
    //=======================================================================

   /**
    * Returns the short PID of the root gateway domain.
    * @return byte[] the PSS short persistent identifier
    */
    public byte[] getDomainShortPID( )
    {
        return domain.get_short_pid();
    }

    //=======================================================================
    // Registry
    //=======================================================================

   /**
    * Register a criteria and the supporting factory.
    * @param criteria the <code>Criteria</code> supported by a server
    * @param source the factory supporting resource creation for the criteria
    *   identified by the label
    */
    public void register( Criteria criteria, FactoryService source )
    {
        factoryServant.register( criteria, source );
    }

    //=======================================================================
    // RandomService
    //=======================================================================

   /**
    * Return a random seed used by factory implmentation during the creation of 
    * constant random identifiers.
    */
    public int getRandom( )
    {
	  return seed.nextInt( Integer.MAX_VALUE );
    }

    //=======================================================================
    // ResourceFactoryService
    //=======================================================================

   /**
    * Returns a reference to a ResourceFactory providing convinence operations for creation 
    * of a number of concrete AbstractResource types (User, Message, Processor, etc.).
    */
    public ResourceFactory getResourceFactory( )
    {
        return factory;
    }
}
