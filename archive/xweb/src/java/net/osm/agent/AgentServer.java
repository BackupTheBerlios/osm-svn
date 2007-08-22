
package net.osm.agent;

import java.io.InputStream;

import org.apache.avalon.framework.activity.Initializable;
import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.CascadingException;
import org.apache.avalon.framework.CascadingRuntimeException;
import org.apache.avalon.framework.logger.Loggable;
import org.apache.avalon.framework.configuration.DefaultConfigurationBuilder;
import org.apache.log.Logger;
import org.apache.log.Hierarchy;
import org.apache.log.output.FileOutputLogTarget;

import org.omg.CORBA.ORB;
import org.omg.CommunityFramework.Community;
import org.omg.Session.AbstractResource;
import org.omg.Session.AbstractResourceHelper;

import net.osm.audit.Audit;
import net.osm.audit.AuditService;
import net.osm.audit.home.Manager;
import net.osm.audit.home.Adapter;


/**
 * The AgentServer provides support for the creation of Business Agents 
 * representing people, places, things, work-in-progress, communities, collaborative
 * encounters, and the business rules that enable collaborative interaction.
 * The <code>AgentServer</code> provides a set of operations enabling agent creation, 
 * event management and persistent logging of structured events backed by a business 
 * object platform that maintains the persistent state of the underlying object.
 */

public class AgentServer extends Audit implements AgentService, Loggable, Configurable, Initializable
{

    //=========================================================
    // State members
    //=========================================================

   /**
    * The configuration is an in memory representation of the XML assembly file used 
    * as a container of static configuration values.
    */

    private Configuration configuration;

   /**
    * The default logger.
    */
    private Logger log;

   /**
    * Singleton reference to the agent factory.
    */
    private static AgentServer server;

   /**
    * Singleton reference to a root Agent
    */
    private static Agent root;

   /**
    * Internal stack of recently accessed agent instances.
    */
    protected static Cache cache;


    //=========================================================
    // Constructor
    //=========================================================

   /**
    * Default constructor.
    */
    public AgentServer()
    {
        super();
    }

    //=========================================================
    // State members
    //=========================================================

    //
    // Loggable implementation
    //
    
   /**
    * Sets the logger to be used during configuration, initialization 
    * and execution phase of this component.  This implementation 
    * directs logging events including debugging, information, error
    * and fatal error messages to the supplied logger.
    *
    * @param logger sink to which log entries will be directed
    */ 
    public void setLogger( final Logger logger )
    {
        super.setLogger( logger );
        log = logger;
    }
    
    //=========================================================
    // Configurable implementation
    //=========================================================
    
   /**
    * Configuration of the runtime environment based on a supplied Configuration arguments, 
    * including the configuration for an underlying audit services.
    *
    * @param config static configuration block.
    * @throws ConfigurationException if the supplied configuration is incomplete or badly formed.
    */
    public void configure( final Configuration config )
    throws ConfigurationException
    {

        // validate context before proceeding

        if( null != configuration ) throw new ConfigurationException( 
		"Configurations for block " + this + " already set" );
        this.configuration = config;

        if( null == log ) throw new ConfigurationException( 
		"Lifecycle sequence - logger not set." );

	  String name = "OSM Business Agents.";
	  String banner = "\n" + name + "\nCopyright (C), 2000-2001 OSM SARL.\nAll Rights Reserved.";
        log.info( banner );
        System.out.println( banner );

        // configure the audit service

	  try
	  {
            super.configure( configuration.getChild("audit"));
        }
        catch( Exception e )
	  {
            log.error( "Audit configuration failure", e );
		throw new ConfigurationException( "Audit configuration failure", e );
        }
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
    * @raises Exception
    */
    public void initialize()
    throws Exception
    {
        if( null == configuration ) throw new Exception( 
		"Lifecycle sequence - configuration phase has not been executed." );

        try
        {
	      super.initialize( );
		server = this;

		//
		// setup the cache
		//

		cache = new Cache( 20 );

        }
        catch( Exception e )
	  {
            log.error( "Audit initialization failure", e );
		throw new CascadingException( "Audit initialization failure", e );
        }
    }

    //=========================================================
    // AgentService implementation
    //=========================================================

   /**
    * Method used following server startup to establish the reference to a 
    * root object.
    */

    public Agent setRoot( Object object )
    {
	  if( object == null ) throw new RuntimeException("Null value supplied to setRoot.");
	  try
        {
            root = resolve( object );
            return root;
        }
        catch( Exception e )
        {
            throw new CascadingRuntimeException("Failed to resolve root agent.", e );
        }
    }
    
   /**
    * Returns the root agent.
    */
    public Agent getRoot()
    {
        if( root == null ) throw new RuntimeException("Root agent has not been initalized - use setRoot");
        return root;
    }

   /**
    * The <code>resolve</code>Method supporting the establishment of a new 
    * <code>Agent</code> instance - where the returned agent may be created or 
    * returned from an internal stack.  The agent will be bound to the supplied
    * object (typically a CORBA Business Object or related Valuetype).
    *
    * @param object the object that the agent will represent
    * @returns Agent of class selected to represent the supplied object
    */
    public Agent resolve( Object object )
    {

        Class clazz = null;
        if( object instanceof org.omg.CORBA.Object )
        {
            org.omg.CORBA.Object ref = (org.omg.CORBA.Object) object;
            if( ref._is_a( org.omg.Session.AbstractResourceHelper.id() ))
            {

	          //
	          // Check if the object is already represented by an existing agent
		    // maintained within the cache, otherwise, create a new adapter.
                //

		    AbstractResource r = AbstractResourceHelper.narrow( (org.omg.CORBA.Object) object );
	          Agent agent = cache.locate( r );
	          if( agent != null ) return agent;
		    return resolveUsingAdapter( r );
            }
            else
            {
		    System.out.println("Failed to resolve agent for non A/R object: " + ref.getClass() );
                clazz = UnknownAgent.class;
            }
        }
        else if( object instanceof org.omg.CORBA.portable.ValueBase )
        {
            if( object instanceof org.omg.Session.Link )
            {
                clazz = LinkAgent.class;
		}
		else if( object instanceof org.omg.CommunityFramework.Control )
            {
		    if( object instanceof org.omg.CommunityFramework.MembershipModel )
                {
                    clazz = MembershipModelAgent.class;
                }
		    else if( object instanceof org.omg.CollaborationFramework.ProcessorModel )
                {
                    clazz = ProcessorModelAgent.class;
                }
		    else if( object instanceof org.omg.CommunityFramework.Role )
                {
                    clazz = RoleAgent.class;
                }
		    else
		    {
                    clazz = ControlAgent.class;
		    }
            }
		else if( object instanceof org.omg.Session.SystemMessage )
            {
                clazz = MessageAgent.class;
            }
		else if( object instanceof org.omg.CollaborationFramework.UsageDescriptor )
            {
                clazz = UsageDescriptorAgent.class;
            }
		else 
            {
                clazz = ValueAgent.class;
            }
        }
        else
        {
            clazz = UnknownAgent.class;
        }

        try
        {
            return resolveUsingClass( object, clazz );
        }
        catch( Exception e )
        {
		throw new CascadingRuntimeException( "Failed to resolve agent for class " + clazz, e );
        }
    }

   /**
    * Returns the ORB established during the initalization phase.
    */

    public ORB getOrb()
    {
       return orb;
    }

    //==========================================================================
    // Internal utilities
    //==========================================================================

   /**
    * Creates an agent with a base class matching as closely as possible the 
    * supplied object.  This method will be typically invoked for non-AbstractResource
    * objects such as valuetypes.
    * 
    * @param object the object that the agent will represent
    * @param clazz the class of agent to be used to instantiate the returned agent
    * @returns Agent wrapping the supplied object
    */
    private Agent resolveUsingClass( Object object, Class clazz )
    {
        try
        {
            Agent agent = (Agent) clazz.newInstance();
            agent.setReference( object );
		if( agent instanceof ActiveAgent ) ((ActiveAgent) agent).setOrb( getOrb() );
            return agent;
        }
        catch( Exception e )
        {
            throw new CascadingRuntimeException( "Failed to resolve agent from class '" + clazz + "'.", e );
        }
    }

   /**
    * Creates an agent with a base class corresponding to the supplied clazz 
    * argument wrapping an AbstractResource supplied under the <code>resource</code>
    * argument.  The implementation establishes a subscription between the supplied 
    * resource and a local adapter.  The adapter provides local event propergation
    * to associated agents.
    * 
    * @param resource the object reference of an AbstractResource that the agent will represent
    * @param clazz the class of agent to be used to instantiate the returned agent
    * @returns Agent wrapping the supplied AbstractResource
    */
    private Agent resolveUsingAdapter( org.omg.Session.AbstractResource resource )
    {
        Class clazz = getAdaperClass( resource );
        try
        {
            AbstractResourceAgent agent = (AbstractResourceAgent) clazz.newInstance();
		if( agent instanceof ActiveAgent ) ((ActiveAgent) agent).setOrb( getOrb() );
		agent.setAdapter( servant.create( resource ));
		cache.add( agent );
            return agent;
        }
        catch( Exception e )
        {
            throw new CascadingRuntimeException( "Failed to resolve agent from adapter using '" + clazz + "'.", e );
        }
    }

   /**
    * Convenience operation that returns the singleton instance of the AgentService.  
    * This method is initialized during the initialization phase of the AgentServer.
    * @returns AgentService the singleton agent service
    */
    public static AgentService getAgentService()
    {
	  if( server == null ) throw new RuntimeException("AgentServer has not been initialized.");
        return server;
    }

   /**
    * Return the agent class from a base AbstractResource.
    */

    private Class getAdaperClass( AbstractResource ref )
    {
	  Class clazz = AbstractResourceAgent.class;
        if( ref._is_a( org.omg.Session.UserHelper.id() ))
        {
            if( ref._is_a( net.osm.hub.home.PrincipalHelper.id() ))
            {
                clazz = PrincipalAgent.class;
            }
		else
	      {
                clazz = UserAgent.class;
            }
        }
        else if( ref._is_a( org.omg.Session.WorkspaceHelper.id() ))
        {
            if( ref._is_a( org.omg.Session.DesktopHelper.id() ))
            {
                clazz = DesktopAgent.class;
            }
            else if( ref._is_a( org.omg.CommunityFramework.CommunityHelper.id() ))
            {
                clazz = CommunityAgent.class;
            }
            else 
            {
                 clazz = WorkspaceAgent.class;
		}
	  }
        else if( ref._is_a( org.omg.Session.TaskHelper.id() ))
        {
            clazz = TaskAgent.class;
        }
        else if( ref._is_a( org.omg.CollaborationFramework.ProcessorHelper.id() ))
        {
            clazz = ProcessorAgent.class;
        }
        return clazz;
    }

    
   /**
    * The <code>init</code> method invoked by the servlet loader.  The 
    * implementation resolves the configuration file for the servlet and executes  
    * server configuration, initialization and statrup consistent with the Avalon 
    * lifecycle patterns.
    */

    public static void boot( )
    {

        server = new AgentServer();

	  Logger log = createLogger( "agent.log" );
	  Configuration config = getConfiguration( server );

        //
        // proceed with AgentServer lifecycle phases, starting with server 
        // configuration followed by initialization and server startup
        //

	  try
	  {
            server.setLogger( log );
            server.configure( config  );
            server.initialize();
            server.start();
            //server.setRoot( this.getCommunity() );
		
        }
	  catch( Exception e )
	  {
		String error = "Lifecycle failure during servlet init.";
            log.error( error , e );
		throw new CascadingRuntimeException( error, e );
        }
    }

    private static Logger createLogger( String filename )
    {
	  try
	  {
            Hierarchy hierarchy = Hierarchy.getDefaultHierarchy();
            hierarchy.setDefaultLogTarget( new FileOutputLogTarget( filename ) );
            return hierarchy.getLoggerFor( "AGENTS" );
        }
        catch( Exception e)
	  {
		throw new CascadingRuntimeException("Failed to establish logger.",e);
	  }
    }

   /**
    * Returns the configuration resource.
    */

    private static Configuration getConfiguration( AgentServer server ) 
    {
	  String path = "/xweb.xml";
	  try
	  {
	      DefaultConfigurationBuilder builder = new DefaultConfigurationBuilder( );
	      InputStream is = server.getClass().getClassLoader().getResourceAsStream( path );
		if( is == null ) throw new RuntimeException("Could not find the configuration resource \"" + path + "\"");
		Configuration c = builder.build( is );
		return c.getChild("agent");
        }
	  catch(Exception e)
        {
	      throw new CascadingRuntimeException("Failed to establish AgentServer configuration.", e );
        }
    }
}
