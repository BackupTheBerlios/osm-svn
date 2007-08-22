/*
 * @(#)FinderServer.java
 *
 * Copyright 2000 OSM SARL. All Rights Reserved.
 * 
 * This software is the proprietary information of OSM SARL.  
 * Use is subject to license terms.
 * 
 * @author  Stephen McConnell
 * @version 1.0 26/03/2001
 */

package net.osm.hub.gateway;

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
import org.omg.CosNaming.NameComponent;
import org.omg.CosLifeCycle.NoFactory;
import org.omg.CommunityFramework.Criteria;
import org.omg.CommunityFramework.CommunityCriteria;
import org.omg.CommunityFramework.Community;
import org.omg.CommunityFramework.Criteria;
import org.omg.CommunityFramework.ResourceFactoryProblem;
import org.omg.CommunityFramework.Problem;
import org.omg.Session.AbstractResource;
import org.omg.Session.User;

import net.osm.hub.gateway.FactoryException;
import net.osm.hub.gateway.ResourceFactoryService;
import net.osm.pss.PSSConnectorService;
import net.osm.pss.PSSSessionService;
import net.osm.hub.gateway.FactoryService;
import net.osm.hub.gateway.DomainService;
import net.osm.hub.gateway.RandomService;
import net.osm.pss.PersistanceHandler;
import net.osm.hub.user.UserAdministratorService;
import net.osm.hub.user.UserService;
import net.osm.hub.community.CommunityAdministratorService;
import net.osm.hub.pss.GenericStorage;
import net.osm.hub.pss.FinderStorage;
import net.osm.hub.pss.FinderStorageHome;
import net.osm.hub.pss.UserStorage;
import net.osm.hub.home.ResourceFactory;
import net.osm.hub.home.Finder;
import net.osm.hub.home.FinderPOA;
import net.osm.hub.home.FinderHelper;
import net.osm.hub.home.UnknownName;
import net.osm.hub.home.UnknownPrincipal;
import net.osm.orb.ORBService;
import net.osm.dpml.DPML;
import net.osm.util.IOR;
import net.osm.util.ExceptionHelper;
import net.osm.realm.PrincipalManager;
import net.osm.realm.PrincipalManagerHelper;
import net.osm.realm.PrincipalManagerBase;
import net.osm.realm.StandardPrincipal;
import net.osm.realm.StandardPrincipalBase;
import net.osm.vault.Vault;
import net.osm.vault.LocalVault;
import net.osm.util.X500Helper;

/**
 * The <code>FinderServer</code> block provides bootstrap services 
 * enabling the establishment of initial objects into the environment.
 *
 * @author <a href="mailto:mcconnell@osm.net">Stephen McConnell</a>
 */

public class FinderServer extends FinderPOA 
implements Block, LogEnabled, Contextualizable, Composable, Configurable, Initializable, Disposable, FinderService
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

    private FinderStorageHome home;
    private boolean initialized = false;
    private boolean disposed = false;
    private BlockContext block;
    private ResourceFactory factory; 
    private Finder finder;
    private String ior;

    private PrincipalManager manager;
    private UserAdministratorService userAdmin;
    private CommunityAdministratorService communityAdmin;
    private UserService userService;

    private Vault vault;

   /**
    * The principal representing the server.
    */
    private StandardPrincipal principal;

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
        if( getLogger().isDebugEnabled() ) getLogger().debug( "default contextualize phase" );
	  if( !(block instanceof BlockContext ) )
	  {
		final String error = "supplied context does not implement BlockContext.";
		if( getLogger().isErrorEnabled() ) getLogger().error( error );
		throw new ContextException( error );
        }
	  this.block = (BlockContext) block;
        if( getLogger().isDebugEnabled() ) getLogger().debug( "default contextualize complete" );
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
		factory = ((ResourceFactoryService)manager.lookup("FACTORY")).getResourceFactory();
            connector = ((PSSConnectorService) manager.lookup("PSS-CONNECTOR")).getPSSConnector();
            session = ((PSSSessionService) manager.lookup("PSS-SESSION")).getPSSSession();
            userAdmin = (UserAdministratorService) manager.lookup("USER-ADMINISTRATOR");
            communityAdmin = (CommunityAdministratorService) manager.lookup("COMMUNITY-ADMINISTRATOR");
            userService = (UserService) manager.lookup("USER");
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

        //
        // create the POA
        //
            
        try
	  {
            POA root = POAHelper.narrow(orb.resolve_initial_references("RootPOA"));
            POA poa = root.create_POA(
              "FINDER",
              root.the_POAManager(),
              new Policy[]{
                  root.create_id_assignment_policy( IdAssignmentPolicyValue.USER_ID ),
                  root.create_lifespan_policy( LifespanPolicyValue.PERSISTENT )});

            //
            // create and bind this object as the servant and create an 
            // object reference for publication
            //
                        
            byte[] ID = "FINDER".getBytes();
            poa.activate_object_with_id( ID, this );
            org.omg.CORBA.Object obj = poa.id_to_reference(ID);
            finder = FinderHelper.narrow( obj );
	  }
	  catch( Throwable poaError )
	  {
	      String error = "unable to create the finder object reference";
	      if( getLogger().isErrorEnabled() ) getLogger().error( error, poaError );
	      throw new Exception( error, poaError );
	  }

	  //
	  // Setup the pricipal representing the server (when the server 
        // executes any resource creation actions and when the server acts as 
        // a client).
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
        // bootstrap the gateway
        //

	  try
	  {
            Configuration persistence = configuration.getChild("persistence");
	      PersistanceHandler.register( connector, persistence );
            home = (FinderStorageHome) session.find_storage_home(
 		    "PSDL:osm.net/hub/pss/FinderStorageHomeBase:1.0" );

		Configuration profile = configuration.getChild("bootstrap").getChild("profile");
            String name = profile.getAttribute("name");
		try
		{
		    FinderStorage rootCommunity = home.find_by_name( name );
		}
	      catch( NotFound _nf_ )
		{
                Configuration dpml = profile.getChild("dpml");
		    CommunityCriteria criteria = (CommunityCriteria) DPML.buildCriteria( dpml );
		    if( getLogger().isDebugEnabled() ) getLogger().debug(
			"creating bootstrap administrator");
		    UserStorage admin = userAdmin.createUserStorage( principal );
		    if( getLogger().isDebugEnabled() ) getLogger().debug(
			"creating bootstrap community");
		    StorageObject community = communityAdmin.createCommunityStorage( 
			name, criteria, admin, principal );
		    if( getLogger().isDebugEnabled() ) getLogger().debug(
			"bootstrap community established");
		    home.create( name, communityAdmin.getCommunityReference( community ));
		    if( getLogger().isDebugEnabled() ) getLogger().debug(
			"bootstrap community installed");
            }
	  }
	  catch( Throwable throwable )
	  {
	      String error = "unexpected error during finder bootstrap phase";
		if( getLogger().isErrorEnabled() ) getLogger().error( error, throwable );
		throw new Exception( error, throwable );
	  }

/*
		Configuration[] profiles = configuration.getChild("bootstrap").getChild("bootstrap").getChildren("profile");
		for( int i=0; i<profiles.length; i++ )
	      {
		    String name = "";
		    Configuration profile = profiles[i];
		    try
		    {
			  name = profile.getAttribute("name");
                    if( getLogger().isDebugEnabled() ) getLogger().debug("checking bootstrap profile '" + name + "'");
		        FinderStorage entry = home.find_by_name( name );
	          }
		    catch( NotFound notFound )
		    {
			  // we need to apply bootstrap actions to bring the object into
			  // existance

			  try
		        {
		            Configuration dpml = profile.getChild("dpml");
			      Criteria criteria = DPML.buildCriteria( dpml );
			      AbstractResource resource = factory.create( name, criteria );
			      home.create( name, resource );
			      if( getLogger().isDebugEnabled() ) getLogger().debug("created bootstrap entry for '" + name + "'");
			  }
			  catch( Exception bootstrapException )
			  {
				String error = "unable to create a bootstrap resource";
			      if( getLogger().isWarnEnabled() ) getLogger().warn( error, bootstrapException );
				System.err.println(error);
			  }
                }
                catch(Throwable e)
	          {
		        String error = "bootstrap exception";
	              if( getLogger().isErrorEnabled() ) getLogger().error( error, e);
		        e.printStackTrace();
		        throw new Exception( error, e );
		    }
		}
	  }
	  catch( Throwable throwable )
	  {
	      String error = "unexpected error during finder primary initialization";
		if( getLogger().isErrorEnabled() ) getLogger().error( error, throwable );
		throw new Exception( error, throwable );
	  }
*/
        try
        {
            ior = this.configuration.getAttribute( "ior" );
            getLogger().info("setting IOR path to " + ior );
            try
            {
                IOR.writeIOR( orb, finder, ior );
                getLogger().info( "published finder IOR to: " + ior );
            }
            catch (Exception e)
            {
                if( getLogger().isErrorEnabled() ) getLogger().error("failed to create external IOR on " + ior, e );
                throw new Exception( "failed to create external IOR on " + ior, e );
            }
        } 
        catch (Exception e)
        {
            getLogger().info("IOR publication disabled" );
        }

        String message = "OSM Finder";
        getLogger().info( message );
	  System.out.println( message );
    }

    //=======================================================================
    // Disposable
    //=======================================================================
        
    public void dispose()
    {
        if( getLogger().isDebugEnabled() ) getLogger().debug( "disposing of finder");
    }

    //=======================================================================
    // Finder
    //=======================================================================

   /**
    * Returns a user object reference representing the user identifier by 
    * the security current principal.
    *
    * @param policy a boolean value that if true means that the factory
    *   should create a new user reference if no existing user reference 
    *   can be found for the pricipal.
    * @exception UnknownPrincipal may be thrown if the policy is false and the 
    *   current principal does not map to an existing user
    */
    public User resolve_user( boolean policy ) throws UnknownPrincipal
    {

        // resolve or create user

	  try
	  {
		
		User user = userService.locateUser();
		if( getLogger().isDebugEnabled() ) getLogger().debug("located existing user");
		return user;
        }
	  catch( NotFound nf )
	  {
            if( !policy ) throw new UnknownPrincipal( );
		try
		{
		    User user = userService.createUser( );
		    if( getLogger().isInfoEnabled() ) getLogger().info("created new user");
		    return user;
		}
		catch( FactoryException e )
	      {
	  	    String error = "Unexpected exception while creating user reference."; 
	          if( getLogger().isErrorEnabled() ) getLogger().error( error, e );
		    throw new org.omg.CORBA.INTERNAL( error );
		}
        }
    }


   /**
    * Locate a resource by name.
    * @param name the lookup name for the resource
    * @return AbstractResource
    * @exception UnknownName if the name is unknown
    */
    public AbstractResource lookup( String name ) throws UnknownName, ResourceFactoryProblem
    {
        try
	  {
	      FinderStorage entry = home.find_by_name( name );
		return entry.resource();
	  }
	  catch( NotFound notFound )
	  {
		throw new UnknownName( name );
	  }
	  catch( Throwable e )
	  {
		final String error = "unexpected error while locating resource";
		throw new ResourceFactoryProblem( 
		  factory, 
		  new Problem( getClass().getName(), error, e ) 
	      );
	  }
    }

   /**
    * Return the root resource factory.
    * @return ResourceFactory the root resource factory.
    */
    public ResourceFactory resource_factory()
    {
        return factory;
    }
    
   /**
    * The find_factories operation is passed a key used to identify the desired factory.
    * The key is a name, as defined by the naming service. More than one factory may
    * match the key. As such, the factory finder returns a sequence of factories. If there are
    * no matches, the NoFactory exception is raised.<p>
    * The scope of the key is the factory finder. The factory finder assigns no semantics to
    * the key. It simply matches keys. It makes no guarantees about the interface or
    * implementation of the returned factories or objects they create.
    */
    public org.omg.CORBA.Object[] find_factories( NameComponent[] factory_key )
    throws NoFactory
    {
	  if( getLogger().isDebugEnabled() ) getLogger().debug("supplying factory");
	  return new org.omg.CORBA.Object[]{ factory };
    }

   /**
    * Returns a client principal invoking the operation.
    * @return StandardPrincipal the client principal
    */
    private StandardPrincipal getPrincipal() throws Exception
    {
        if( manager == null ) manager = PrincipalManagerHelper.narrow( 
			orb.resolve_initial_references( "PrincipalManager" ) );
	  return manager.getPrincipal();
    }

}
