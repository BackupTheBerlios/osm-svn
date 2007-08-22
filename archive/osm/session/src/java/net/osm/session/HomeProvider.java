/*
 * Copyright 2000 OSM SARL. All Rights Reserved.
 * 
 * This software is the proprietary information of OSM SARL.  
 * Use is subject to license terms.
 * 
 * @author  Stephen McConnell
 * @version 1.0 26/03/2001
 */

package net.osm.session;

import java.net.URL;
import java.util.Random;
import java.util.Hashtable;
import java.io.File;

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
import org.apache.avalon.framework.service.DefaultServiceManager;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.Serviceable;
import org.apache.excalibur.configuration.CascadingConfiguration;

import org.apache.pss.ORB;
import org.apache.orb.POAContext;
import org.apache.orb.DefaultPOAContext;
import org.apache.pss.Connector;
import org.apache.pss.ConnectorContext;
import org.apache.pss.Session;
import org.apache.orb.ORBContext;
import org.apache.orb.util.IOR;
import org.apache.pss.DefaultSingletonManager;
import org.apache.pss.DefaultSessionContext;
import org.apache.orb.corbaloc.Handler;

import org.omg.CORBA.Any;
import org.omg.CORBA.Policy;
import org.omg.CosTime.TimeService;
import org.omg.CosTime.TimeServiceHelper;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.Servant;
import org.omg.PortableServer.LifespanPolicyValue;
import org.omg.PortableServer.IdUniquenessPolicyValue;
import org.omg.PortableServer.IdAssignmentPolicyValue;
import org.omg.CosPersistentState.NotFound;
import org.omg.CosPersistentState.StorageObject;
import org.omg.CosPersistentState.StorageHomeBase;

import net.osm.sps.StructuredPushSupplierService;
import net.osm.sps.StructuredPushSupplierServantManager;
import net.osm.domain.DomainService;
import net.osm.realm.RealmSingleton;
import net.osm.adapter.Adapter;
import net.osm.adapter.Adaptive;
import net.osm.finder.ObjectNotFound;
import net.osm.finder.InvalidPath;
import net.osm.chooser.Chooser;
import net.osm.chooser.ChooserService;
import net.osm.chooser.ChooserException;
import net.osm.chooser.DefaultChooser;
import net.osm.chooser.DefaultChooserContext;
import net.osm.chooser.UnknownName;
import net.osm.session.SessionSingleton;
import net.osm.session.workspace.WorkspaceServantManager;
import net.osm.session.user.User;
import net.osm.session.user.UserServantManager;
import net.osm.session.task.TaskServantManager;
import net.osm.session.desktop.DesktopServantManager;
import net.osm.session.workspace.WorkspaceService;
import net.osm.session.user.UserService;
import net.osm.session.task.TaskService;
import net.osm.session.desktop.DesktopService;
import net.osm.session.processor.ApplianceChooser;
import net.osm.session.processor.ProcessorServantManager;
import net.osm.session.processor.ProcessorService;
import net.osm.session.resource.AbstractResourceStorageHome;

/**
 * The <code>SessionProvider</code> encapsulates a family of components that 
 * provide support for the establishment and management of <code>User</code>s, 
 * <code>Task</code>s, <code>Workspace</code>s, <code>Messages</code>s that 
 * constitute a distributed people-places-and-things framework.
 *
 * @author <a href="mailto:mcconnell@osm.net">Stephen McConnell</a>
 */
public class HomeProvider extends HomePOA
implements LogEnabled, Contextualizable, Configurable, Serviceable, Initializable, Startable, Disposable,
HomeService
{
    //=======================================================================
    // state
    //=======================================================================

    private Logger m_logger;
    private Context m_context;
    private Configuration m_config;
    private ServiceManager m_manager;

    private boolean m_initialized = false;
    private boolean m_started = false;
    private boolean m_disposed = false;

    private ORB m_orb;
    private POA m_poa;
    private TimeService m_clock;
    private StructuredPushSupplierService m_sps_service;
    private DomainService m_domain_service;
    private ConnectorContext m_connector_context;
    private Connector m_connector;
    private Session m_session;

    private StructuredPushSupplierServantManager m_sps = new StructuredPushSupplierServantManager();

    private DesktopServantManager m_desktop = new DesktopServantManager();
    private WorkspaceServantManager m_workspace = new WorkspaceServantManager();
    private TaskServantManager m_task = new TaskServantManager();
    private UserServantManager m_user = new UserServantManager();
    private ProcessorServantManager m_processor = new ProcessorServantManager();
    private ApplianceChooser m_appliance = new ApplianceChooser();

    private DefaultChooser m_chooser;
    private Home m_service;
    private POA m_root;

    private String m_name = "Session Gateway";
    private String m_description = "Enterprise service management gateway.";
    private String m_key = "session";
    private URL m_corbaloc;

    private final Hashtable m_table = new Hashtable();
    private org.openorb.corbaloc.CorbalocService m_registry;

    //=======================================================================
    // LogEnabled
    //=======================================================================
    
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
    protected Logger getLogger()
    {
        return m_logger;
    }

    //=======================================================================
    // Contextualizable
    //=======================================================================

   /**
    * Set the component context.
    * @param context the component context
    */
    public void contextualize( Context context ) 
    throws ContextException
    {
        m_context = context;
    }

    //=======================================================================
    // Configurable
    //=======================================================================

   /**
    * Used by a container to supply the static component configuration.
    * @param config the static configuration
    */    
    public void configure( final Configuration config )
    throws ConfigurationException
    {
        m_config = config;
    }

    //=======================================================================
    // Serviceable
    //=======================================================================

    /**
     * Pass the <code>ServiceManager</code> to the <code>Serviceable</code>.
     * The <code>Serviceable</code> implementation uses the supplied
     * <code>ServiceManager</code> to acquire the components it needs for
     * execution.
     * @param manager the <code>ServiceManager</code> for this delegate
     * @exception ServiceException
     */
    public void service( ServiceManager manager )
    throws ServiceException
    {
        m_manager = manager;
    }

    //=======================================================================
    // Initializable
    //=======================================================================
    
   /**
    * Initialization of the manager under which workspace, desktop, 
    * user and task managers are established.
    * @exception Exception if an error occurs during initalization
    */
    public void initialize()
    throws Exception
    {

        getLogger().debug("initialization");

        //
        // update the service name and description
        //

        m_name = m_config.getChild("description").getAttribute("name", m_name );
        m_description = m_config.getChild("description").getValue( m_description );

        //
        // get the ORB, TimeService and PSS system
        //

        try
        {
	      m_orb = (ORB) m_manager.lookup( ORBContext.ORB_KEY );
            m_root = (POA) m_orb.resolve_initial_references("RootPOA");
            m_clock = TimeServiceHelper.narrow( m_orb.resolve_initial_references("TimeService"));
            m_registry = org.openorb.corbaloc.CorbalocServiceHelper.narrow(
              m_orb.resolve_initial_references( "CorbalocService" ) );
        }
        catch( Throwable e )
        {
            final String error = "Unexpected exception while resolving an inital reference.";
            throw new SessionException( error, e );
        }

        //
        // create the PSS connector and session
        //

        getLogger().debug("pss initalization");

        try
	  {
            m_connector = m_orb.getConnector();
            Configuration pss = m_config.getChild("pss");
            m_connector.register( pss.getChild("persistence") );
            m_session = m_connector.createBasicSession( pss.getChild("session") );
            Configuration[] homes = pss.getChild("persistence").getChildren("home");
            for( int i=0; i<homes.length; i++ )
            {
                m_session.find_storage_home( homes[i].getAttribute("psdl","") );
            }
	  }
	  catch( Throwable e )
	  {
	      String error = "PSS initalization error in " + 
               this.getClass().getName();
	      throw new SessionException( error, e );
	  }

        RealmSingleton.init( m_orb );
        SessionSingleton.init( m_orb );

        //
        // create the gateway server
        //

        getLogger().debug("poa initalization");

        try
	  {
            m_poa = m_root.create_POA(
                m_key,
                m_root.the_POAManager(),
                new Policy[]
                {
                    m_root.create_id_assignment_policy( IdAssignmentPolicyValue.USER_ID ),
                    m_root.create_lifespan_policy( LifespanPolicyValue.PERSISTENT )
                }
            );
            byte[] ID = m_key.getBytes();
            m_poa.activate_object_with_id( ID, this );
            m_service = HomeHelper.narrow( m_poa.id_to_reference(ID) );
	  }
	  catch( Throwable e )
	  {
	      String error = "Unable to create the application POA";
	      throw new SessionException( error, e );
	  }

        //
        // create the chooser - this is accessible to sub-systems
        // as a service via the HomeService.SESSION_SERVICE_KEY
        // and contains the set of default services accessible 
        // through the User and Workspace types.
        //

        getLogger().debug("chooser initalization");
        try
        {
            m_chooser = DefaultChooser.create( 
                new String[0],
                "services",
                getLogger().getChildLogger("services"), 
                m_context, 
                m_config.getChild("chooser"), 
                m_poa );
        }
        catch( ChooserException e)
        {
            throw new SessionRuntimeException( "Error instantiating chooser.", e);
        }

        //
        // create the default context for the sub-systems
        //

        DefaultSessionContext context = new DefaultSessionContext(
          m_orb, m_connector, m_session, m_context );

        //
        // create sub-subsystems
        //

        getLogger().debug("sub-system creation");
	  try
	  {
            //
            // create SPS service
            //

            m_sps.enableLogging( getLogger().getChildLogger( "sps" ) );
            m_sps.contextualize( context );
            m_sps.configure( m_config.getChild( "sps" ) );
            m_sps.service( m_manager );
            m_sps.initialize();

            //
            // create composite managers
            //

            DefaultServiceManager manager = new DefaultServiceManager( m_manager );
            manager.put( "time", m_clock );
            manager.put( HomeService.SESSION_SERVICE_KEY, this );
            manager.put( POAContext.POA_KEY, new DefaultPOAContext( m_poa ));
            manager.put( StructuredPushSupplierService.SERVICE_KEY, m_sps);
            manager.makeReadOnly();

            DefaultServiceManager common_manager = new DefaultServiceManager( manager );
            common_manager.put( UserService.USER_SERVICE_KEY, m_user );
            common_manager.put( WorkspaceService.WORKSPACE_SERVICE_KEY, m_workspace );
            common_manager.makeReadOnly();

            //
            // create workspace servant manager
            //

            m_workspace.enableLogging( getLogger().getChildLogger( "workspace" ) );
            m_workspace.contextualize( context );
            m_workspace.configure( m_config.getChild( "workspace" ) );
            m_workspace.service( common_manager );

            //
            // create desktop servant manager
            //

            m_desktop.enableLogging( getLogger().getChildLogger( "desktop" ) );
            m_desktop.contextualize( context );
            m_desktop.configure( m_config.getChild( "desktop" ) );
            m_desktop.service( common_manager );

            //
            // create task servant manager
            //

            m_task.enableLogging( getLogger().getChildLogger( "task" ) );
            m_task.contextualize( context );
            m_task.configure( m_config.getChild( "task" ) );
            m_task.service( common_manager );

            //
            // create user servant manager
            //

            DefaultServiceManager user_manager = new DefaultServiceManager( manager );
            user_manager.put( DesktopService.DESKTOP_SERVICE_KEY, m_desktop );
            user_manager.put( WorkspaceService.WORKSPACE_SERVICE_KEY, m_workspace );
            user_manager.put( TaskService.TASK_SERVICE_KEY, m_task );

            m_user.enableLogging( getLogger().getChildLogger( "user" ) );
            m_user.contextualize( context );
            m_user.configure( m_config.getChild( "user" ) );
            m_user.service( user_manager );

            //
            // create processor servant manager
            //

            m_processor.enableLogging( getLogger().getChildLogger( "processor" ) );
            m_processor.contextualize( context );
            m_processor.configure( m_config.getChild( "processor" ) );
            m_processor.service( common_manager );

            //
            // create appliance factory chooser
            //

            DefaultChooserContext applianceContext = new DefaultChooserContext(
               new String[]{"services"}, "appliance", context );

            DefaultServiceManager appliance_manager = new DefaultServiceManager( manager );
            appliance_manager.put( ProcessorService.PROCESSOR_SERVICE_KEY, m_processor );

            m_appliance.enableLogging( getLogger().getChildLogger( "appliance" ) );
            m_appliance.contextualize( applianceContext );
            m_appliance.configure( m_config.getChild( "appliance" ) );
            m_appliance.service( appliance_manager );

	  }
	  catch( Throwable e )
	  {
	      String error = "Unexpected establishment error during pre initalization phase.";
		throw new SessionException( error, e );
	  }

        //
        // initialization phase
        //

        getLogger().debug("sub-system initialization");
	  try
	  {
            getLogger().debug("sub-systems initialization");
            m_workspace.initialize();
            m_desktop.initialize();
            m_task.initialize();
            m_user.initialize();
            m_processor.initialize();
            m_appliance.initialize();

            //
            // register the appliance chooser with the gateway chooser
            //

            getLogger().debug("appliance chooser registration");
            m_chooser.register( 
              "appliance", 
              m_appliance.getChooser() );

	  }
	  catch( Throwable e )
	  {
	      String error = "Unexpected establishment error during initialization phase.";
		throw new SessionException( error, e );
	  }

        //
        // publish the service
        //
        
        try
        {
            String path = m_registry.put_object( m_service, m_key );
            m_corbaloc = new URL( null, path, new Handler( this._orb() ) );

            register( m_workspace, "workspace" );
            register( m_desktop, "desktop" );
            register( m_task, "task" );
            register( m_user, "user" );
            register( m_processor, "processor" );
            
        }
        catch (Exception e)
        {
            getLogger().error("CORBALOC registration failure.", e );
        }

        getLogger().info("Session home available: " + m_corbaloc );
        getLogger().debug("gateway ready");
        m_initialized = true;
    }

    private void register( ReferenceObject object, String name )
    {
        try
        {
            final URL url = new URL( null, 
              m_registry.put_object( object.getReference(), "/" + m_key + "/" + name ),
              new org.apache.orb.corbaloc.Handler( m_orb ));
            m_table.put( name, url );
            getLogger().debug( "registered: " + url );
        }
        catch( Throwable e )
        {
            final String error = "Unexpected exception while publishing manager: " + name;
            throw new SessionRuntimeException( error, e );
        }
    }


    //=======================================================================
    // Startable
    //=======================================================================

   /**
    * Starts the workspace, desktop, task and user managers.
    * @exception Exception if startup error is encountered.
    */
    public void start() throws Exception
    {
        if( !m_initialized ) return;
        if( m_disposed ) return;

        try
        {
            m_poa.the_POAManager().activate();
        }
        catch( Throwable e )
        {
            if( getLogger().isWarnEnabled() ) getLogger().warn( "startup exception", e );
        }

        try
        {
            m_sps.start();
            m_chooser.start();
            m_workspace.start();
            m_desktop.start();
            m_task.start();
            m_user.start();
            m_processor.start();
            m_appliance.start();
	  }
	  catch( Throwable e )
	  {
	      String error = "Unexpected establishment error during startup phase.";
		throw new SessionException( error, e );
	  }

        m_started = true;
    }

   /**
    * Stops the manager and all subsidiary managers.
    * @exception Exception if an error occurs
    */
    public void stop() throws Exception
    {
        if( !m_initialized ) return;
        if( m_disposed ) return;

        if( getLogger().isDebugEnabled() ) getLogger().debug( "stop" );
        try
        {
            m_sps.stop();
            m_chooser.stop();
            m_workspace.stop();
            m_desktop.stop();
            m_task.stop();
            m_user.stop();
            m_processor.stop();
            m_appliance.stop();
	  }
	  catch( Throwable e )
	  {
	      String error = "Unexpected establishment error during shutdown phase.";
		throw new SessionException( error, e );
	  }

        try
        {
            m_poa.destroy( true, true );
        }
        catch( Throwable e )
        {
            if( getLogger().isWarnEnabled() ) getLogger().warn( "ignoring POA related exception" );
        }

        m_started = false;
    }

    //=======================================================================
    // Disposable
    //=======================================================================

   /**
    * Disposal of the manager and release of associated resources.
    */
    public void dispose()
    {
        if( m_disposed ) return;
        m_disposed = true;
        m_session.close();

        try
        {
            m_sps.dispose();
            m_chooser.dispose();
            m_desktop.dispose();
            m_workspace.dispose();
            m_task.dispose();
            m_user.dispose();
            m_processor.dispose();
            m_appliance.dispose();
	  }
	  catch( Throwable e )
	  {
	      String warning = "Unexpected error during disposal phase.";
		if( getLogger().isWarnEnabled() ) getLogger().warn( warning, e );
	  }

        if( getLogger().isDebugEnabled() ) 
           getLogger().debug( "disposal complete" );

    }

    //=======================================================================
    // corbaloc::Finder
    //=======================================================================

    /**
     * Process a query based on a URL query syntax. A url of the 
     * form corbaloc::home.osm.net/gateway?connect=true%amp;value=3 will have 
     * a query element corresponding to <code>connect=true%amp;value=3</code>.
     * Query strings are composed of a sequence of query name value pairs
     * where each pair is delimited by the %amp; character.
     * 
     * @param  query the query string
     * @return  the query value
     * @exception  InvalidQuery if the query is invalid
     */
    public org.omg.CORBA.Any resolve_query(String query)
        throws org.apache.orb.corbaloc.InvalidQuery, org.apache.orb.corbaloc.ServiceRedirection
    {

        getLogger().debug("query: " + query );

        //
        // convert the query into a property set
        //

        java.util.StringTokenizer tokenizer = new java.util.StringTokenizer( query, "&" );
        java.util.Properties table = new java.util.Properties();
        while( tokenizer.hasMoreTokens() )
        {
            String pair = tokenizer.nextToken();
            int i = pair.indexOf("=");
            if( i == -1 || ( pair.length() == i ))
            {
                final String error = "Missing query value in query fragment: " + pair;
                if( getLogger().isWarnEnabled() )
                  getLogger().warn( error );
                throw new org.apache.orb.corbaloc.InvalidQuery( query, error );
            }
            else
            {
                final String key = pair.substring(0,i);
                final String value = pair.substring( i+1 );
                table.setProperty( key, value );
            }
        }
        
        //
        // create an any to hold the result of the query
        //

        Any any = this._orb().create_any();

        //
        // handle the request for a resource home
        //

        if( table.getProperty("manager") != null )
        {
            String home = table.getProperty("manager");
            String id = table.getProperty("id");
            if( id != null )
            {

                //
                // this is a query to lookup an identified object 
                // within a particular home
                //

                URL url = (URL) m_table.get( home );
                if( url == null )
                {

                    //
                    // the requested home is unknown
                    //

                    final String error = "Unknown manager: " + home;
                    if( getLogger().isWarnEnabled() )
                      getLogger().warn( error );
                    throw new org.apache.orb.corbaloc.InvalidQuery( query, error );
                }
                else
                {

                    //
                    // create a redirection to the home using a ref argument
                    //

                    URL redirect = null;
                    try
                    {
                        redirect = new URL( url, "#" + id, null );
                    }
                    catch( Throwable e )
                    {
                        final String error = "Unable to create service redirection address.";
                        if( getLogger().isWarnEnabled() )
                          getLogger().warn( error, e );
                        throw new org.apache.orb.corbaloc.InvalidQuery( query, error );
                    }

                    //
                    // throw the redirect exception back to the client
                    //

                    throw new org.apache.orb.corbaloc.ServiceRedirection( 
                      redirect.toExternalForm() );
                }
            }
            else
            {
                //
                // return the home using the home name as the ref criteria
                //

                try
                {
                    return select( home );
                }
                catch( Throwable e )
                {
                    throw new org.apache.orb.corbaloc.InvalidQuery( query, e.toString() );
                }
            }
        }
        else
        {
            // unsupported query
            final String error = "Supplied query cannot be meaningfully resolved.";
            throw new org.apache.orb.corbaloc.InvalidQuery( query, error );
        }
    }

    //=======================================================================
    // corbaloc::Chooser
    //=======================================================================

    /**
     * Process a query based on a URL ref syntax. A url of the 
     * form corbaloc::home.osm.net/gateway#12345A will have 
     * a ref element corresponding to <code>12345A</code>.
     * 
     * @param  ref the ref string
     * @return  the result of the reference selection
     * @exception  UnknownReference if the supplied ref cannot be resolved
     * @exception  ServiceRedirection if the ref resolution is being redirected
     */
    public org.omg.CORBA.Any select( String ref )
        throws org.apache.orb.corbaloc.UnknownReference, org.apache.orb.corbaloc.InvalidReference, org.apache.orb.corbaloc.ServiceRedirection
    {
        getLogger().debug("select: " + ref );
        int id;

        Any any = m_orb.create_any();
        URL url = (URL) m_table.get( ref );
        if( url == null )
        {
            throw new org.apache.orb.corbaloc.UnknownReference( ref );
        }
        else
        {
            throw new org.apache.orb.corbaloc.ServiceRedirection( url.toExternalForm() );
        }
    }


    //=======================================================================
    // Finder
    //=======================================================================

    /**
     * Returns a object resolved from the supplied path.
     * @param  path a string that identifies a path to an object
     * @return Adaptive an object reference
     * @exception InvalidPath thrown if the path is invalid
     * @exception ObjectNotFound thrown if the path cannot be resolved
     */
    public Adaptive resolve( String path ) throws InvalidPath, ObjectNotFound
    {
        int IDO = 0;
        
        try
        {
            IDO = getIdentifierFromPath( path );
            if( path.startsWith("task=") )
            {
                return m_task.resolve( IDO );
            }
            else if( path.startsWith("workspace=") )
            {
                return m_workspace.resolve( IDO );
            }
            else if( path.startsWith("desktop=") )
            {
                return m_desktop.resolve( IDO );
            }
            else if( path.startsWith("processor=") )
            {
                return m_processor.resolve( IDO );
            }
            else if( path.startsWith("user=") || path.startsWith("principal=") )
            {
                return m_user.resolve( IDO );
            }
            else
            {
                final String error = "Unable to resolve home identifier from path: " + path;
                throw new InvalidPath( error, path );
            }
        }
        catch( NotFound e )
        {
            final String msg = "Unable to locate supplied IDO: " + IDO;
            getLogger().debug( msg );
            throw new ObjectNotFound( msg, path );
        }
        catch( InvalidPath e )
        {
            throw e;
        }
        catch( Throwable e )
        {
            final String error = "Unexpected error while resolving path: " + path;
            getLogger().error( error, e );
            throw new InvalidPath( error, path );
        }
    }

    private int getIdentifierFromPath( String path )
    {
        String ID = path.substring( path.indexOf("=") + 1, path.length() );
        return Integer.parseInt( ID );
    }

    AbstractResourceStorageHome getAbstractResourceHome()
    {
        try
        {
            return (AbstractResourceStorageHome) m_session.find_storage_home( 
	        "PSDL:osm.net/session/resource/AbstractResourceStorageHomeBase:1.0" );
        }
        catch( NotFound nf )
        {
            throw new SessionRuntimeException("Could not resolve resource storage home.", nf );
        }
    }

    //=======================================================================
    // Gateway
    //=======================================================================

    /**
     * Returns a user relative to the undelying principal.
     * @param  policy TRUE if a new should be created if the principal is unknown
     * otherwise, the UnknownPrincipal exception will be thrown if the principal
     * cannot be resolved to a user reference
     * @return  User the user object reference
     * @exception  UnknownPrincipal if the underlying principal does not
     * match a registered user.
     */
    public User resolve_user(boolean policy)
        throws UnknownPrincipal
    {
        // resolve or create user

	  try
	  {
		User user = m_user.locateUser();
		if( getLogger().isDebugEnabled() ) getLogger().debug("located existing user");
		return user;
        }
	  catch( NotFound nf )
	  {
            if( !policy ) throw new UnknownPrincipal( 
              "Could not resolve a user relative to the underlying principal.");
		try
		{
		    User user = m_user.createUser( );
		    if( getLogger().isInfoEnabled() ) getLogger().debug("created new user");
		    return user;
		}
		catch( Throwable e )
	      {
	  	    String error = "Unexpected exception while creating user reference."; 
		    throw new SessionRuntimeException( error, e );
		}
        }
    }

    //=======================================================================
    // Chooser
    //=======================================================================

   /**
    * Get the sequence of keys supported by lookup.
    */
    public String[] get_keys()
    {
        return getChooser().get_keys();
    }

   /**
    * Locates an object reference by name.
    */
    public Adaptive lookup( String name ) throws UnknownName
    {
        return getChooser().lookup( name );
    }

    //=======================================================================
    // ChooserService
    //=======================================================================

   /**
    * Returns the object reference to the singleton chooser service.
    * @return Chooser the chooser object reference
    */
    public Chooser getChooser()
    {
        return m_chooser.getChooser();
    }

   /**
    * Register an adaptive provider with this chooser.
    */
    public void register( String name, Adaptive service )
    {
        m_chooser.register( name, service );
    }

   /**
    * Removes a provider of finder capability from this finder.
    */
    public void deregister( String name )
    {
        m_chooser.deregister( name );
    }

    //=======================================================================
    // Adaptive
    //=======================================================================

   /**
    * Returns a <code>ChooserAdapter</code> to the client.
    * @return Adapter an instance of <code>ChooserAdapter</code>.
    */
    public Adapter get_adapter()
    {
        return new HomeValue( 
          getHome(), m_corbaloc, new String[]{ m_key }, m_name, m_description, m_chooser.get_keys() );
    }

    //=======================================================================
    // HomeService
    //=======================================================================

   /**
    * Returns an object refererence to the home.
    * @return Home the home object reference.
    */
    public Home getHome()
    {
        return m_service;
    }
}
