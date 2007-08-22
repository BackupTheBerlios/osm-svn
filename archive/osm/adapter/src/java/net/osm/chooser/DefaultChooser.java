/**
 * File: DefaultServantManager.java
 * License: etc/LICENSE.TXT
 * Copyright: Copyright (C) The Apache Software Foundation. All rights reserved.
 * Copyright: OSM SARL 2001-2002, All Rights Reserved.
 */

package net.osm.chooser;

import java.io.File;
import java.util.Map;
import java.util.Hashtable;

import org.apache.avalon.framework.activity.Disposable;
import org.apache.avalon.framework.activity.Executable;
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
import org.apache.avalon.framework.service.DefaultServiceManager;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.Serviceable;
import org.apache.avalon.framework.CascadingRuntimeException;
import org.apache.avalon.framework.CascadingException;
import org.apache.orb.ORBContext;
import org.apache.orb.POAContext;
import org.apache.orb.ORB;
import org.apache.orb.util.IOR;
import org.apache.orb.DefaultPOAContext;

import org.omg.CORBA.Policy;
import org.omg.CORBA.TRANSIENT;
import org.omg.CORBA.LocalObject;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.Servant;
import org.omg.PortableServer.ServantActivator;
import org.omg.PortableServer.ForwardRequest;
import org.omg.PortableServer.POAHelper;
import org.omg.PortableServer.IdAssignmentPolicyValue;
import org.omg.PortableServer.RequestProcessingPolicyValue;
import org.omg.PortableServer.ImplicitActivationPolicyValue;
import org.omg.PortableServer.LifespanPolicyValue;
import org.omg.PortableServer.IdUniquenessPolicyValue;
import org.omg.PortableServer.ServantRetentionPolicyValue;

import net.osm.adapter.Adapter;
import net.osm.adapter.Adaptive;
import net.osm.adapter.AdaptiveHelper;

/**
 * <p>The <code>DefaultFinder</code> is an implementation of a 
 * root finder service.</p>
 *
 * @author <a href="mailto:mcconnell@osm.net">Stephen McConnell</a>
 */

public class DefaultChooser extends ChooserPOA
implements LogEnabled, Contextualizable, Configurable, Serviceable, 
Initializable, Startable, Executable, Disposable, ChooserService
{
    //=================================================================
    // static
    //=================================================================

   /**
    * Static utility method supporting the creation of a new chooser 
    * server instance as child POA of the supplied POA.
    * @param key the key used by the parent to reference the chooser
    * @param log the logging channel to apply to the chooser
    * @param context the application context
    * @param config the chooser configuration
    * @param root the parent POA
    * @return DefaultChooser the chooser
    * @exception ChooserException if an instantiation exception occurs
    */
    public static DefaultChooser create( 
      String[] base, String key, Logger log, Context context, Configuration config, POA root )
    throws ChooserException
    {
        try
        {
            if( log.isDebugEnabled() ) log.debug("creating service chooser" );
            DefaultChooser target = new DefaultChooser();
            target.enableLogging( log );
            target.contextualize( new DefaultChooserContext( base, key, context ));
            target.configure( config );
            DefaultServiceManager manager = new DefaultServiceManager();
            manager.put( POAContext.POA_KEY, new DefaultPOAContext( root, null ) );
            manager.makeReadOnly();
            target.service( manager );
            target.initialize();
            return target;
        }
        catch( Throwable e )
        {
            throw new ChooserException( "Unable to instantiate chooser implementation.", e );
        }
    }

    //=================================================================
    // state
    //=================================================================

   /**
    * Logging channel.
    */
    private Logger m_logger;

   /**
    * Service static configuration.
    */
    private Configuration m_config;

   /**
    * Service manager that provides dependant POA context.
    */
    private ServiceManager m_manager;

   /**
    * Application context.
    */
    private Context m_context;

   /**
    * The root POA provided via the service manager.
    */
    private POA m_root;

   /**
    * Tha POA created to handle the finder.
    */
    protected POA m_poa;

   /**
    * Cached reference to the finder object reference.
    */
    private Chooser m_service;

   /**
    * Flag holding initialized state.
    */
    private boolean m_initialized = false;

   /**
    * Flag holding disposed state.
    */
    private boolean m_disposed = false;

   /**
    * List of other finder services that finder operations are 
    * delegated to.
    */
    private final Map m_table = new Hashtable();

   /**
    * Flag holding update state.
    */
    private boolean m_updating = false;

   /**
    * Flag holding execute state.
    */
    private boolean m_executing = false;

   /**
    * The chooser name.
    */
    private String m_name = "Chooser";

   /**
    * Chooser description.
    */
    private String m_description = "Generic service chooser.";

   /**
    * Chooser POA key.
    */
    private String m_key = "CHOOSER";

   /**
    * Chooser parent identifier chain.
    */
    private String[] m_path;

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
	  if( getLogger().isDebugEnabled() ) getLogger().debug( "contextualization" );
	  m_context = context;
        try
        {
            m_key = (String) context.get( ChooserContext.CHOOSER_KEY );
        }
        catch( ContextException e )
        {
            m_key = "CHOOSER";
        }
        try
        {
            m_path = (String[]) context.get( ChooserContext.CHOOSER_PATH );
        }
        catch( ContextException e )
        {
            m_path = new String[]{ m_key };
        }

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
    // Initializable
    //=======================================================================
    
   /**
    * One-time initalization of the service during which the POA is established.
    * @exception IllegalStateException if the server has been disposed of, if 
    *  has not log enabled, configured, contextualized, or serviced.
    * @exception Exception if a general initalization error occurs
    */
    public void initialize()
    throws Exception
    {
        if( m_disposed ) throw new IllegalStateException(
          "Finder has been disposed.");

        if( m_initialized ) throw new IllegalStateException(
          "Finder cannot be re-initialized.");

        if( m_logger == null ) throw new IllegalStateException(
          "Missing logger.");

        if( m_config == null ) throw new IllegalStateException(
          "Missing configuration.");

        if( m_context == null ) throw new IllegalStateException(
          "Missing context.");

        if( m_manager == null ) throw new IllegalStateException(
          "Missing service manager.");

        if( getLogger().isDebugEnabled() ) getLogger().debug( "initialization" );

        //
        // create the POA
        //

        m_root = ((POAContext)m_manager.lookup( POAContext.POA_KEY )).getPOA();

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
	  }
	  catch( Throwable e )
	  {
	      String error = "Unable to create the application POA";
	      throw new ChooserException( error, e );
	  }

        byte[] ID = m_key.getBytes();
        m_poa.activate_object_with_id( ID, this );
        m_service = ChooserHelper.narrow( m_poa.id_to_reference(ID) );

        //
        // update the name and description
        //

        m_name = m_config.getChild("description").getAttribute("name", m_name );
        m_description = m_config.getChild("description").getValue( m_description );

        //
        // publish the service
        //
        
        String ior = m_config.getChild("ior").getAttribute("value", null );
        if( ior != null )
        {
            try
            {
                IOR.writeIOR( this._orb(), m_service, ior );
                if( getLogger().isDebugEnabled() ) getLogger().debug( 
                  "IOR published to path: " + ior ); 
            }
            catch( Throwable e )
            {
                final String warning = "IOR publication disabled due to internal error.";
                if( getLogger().isWarnEnabled() ) getLogger().warn( warning, e ); 
            }
        }

        m_initialized = true;

    }

    //=======================================================================
    // Startable
    //=======================================================================

   /**
    * Starts the finder resulting in the finder's readiness to handle 
    * requests.
    */
    public void start() 
    {
        if( !m_initialized ) throw new IllegalStateException(
          "Chooser has not been initalized.");
        if( m_disposed ) throw new IllegalStateException(
          "Chooser is disposed.");
        
        if( getLogger().isDebugEnabled() ) getLogger().debug( "start" );
        try
        {
            m_poa.the_POAManager().activate();
        }
        catch( Throwable e )
        {
            if( getLogger().isWarnEnabled() ) getLogger().warn( "startup exception", e );
        }
    }

   /**
    * Stops the finder and destroys the underlying POA.
    */
    public void stop() 
    {
        if( !m_initialized ) throw new IllegalStateException(
          "Chooser has not been initalized.");
        if( m_disposed ) throw new IllegalStateException(
          "Chooser is disposed.");

        if( getLogger().isDebugEnabled() ) getLogger().debug( "stop" );
        try
        {
            m_poa.destroy( true, true );
        }
        catch( Throwable e )
        {
            if( getLogger().isWarnEnabled() ) getLogger().warn( "ignoring POA related exception" );
        }
    }

    //=======================================================================
    // Executable
    //=======================================================================

   /**
    * Executes an establishment test, attempts to resolve an object and 
    * verifies that the <code>ObjectNotFound</code> is correctly thrown.
    * @exception throw as a result of an internal execution error
    */
    public void execute() throws Exception
    {
        if( !m_initialized ) throw new IllegalStateException(
          "Chooser has not been initalized.");
        if( m_disposed ) throw new IllegalStateException(
          "Chooser is disposed.");

        getLogger().debug("execution");
        getLogger().info("providers: " + m_table.size() );
        Chooser chooser = getChooser();
        ChooserAdapter adapter = (ChooserAdapter) chooser.get_adapter();
        try
        {
            adapter.lookup("anything");
        }
        catch( UnknownName e )
        {
            getLogger().debug("normal completion: " + e.toString() );
        }
        catch( Throwable e )
        {
            getLogger().error("abnormal completion", e );
        }
    }

    //=======================================================================
    // Disposable
    //=======================================================================

   /**
    * Dispose of the manager and release associated resources.
    */
    public void dispose()
    {
        if( m_disposed ) return;

        if( getLogger().isDebugEnabled() ) getLogger().debug( "disposal" );

        m_disposed = true;

        //
        // cleanup the IOR
        //

        if( m_config != null )
        {
            final String ior = m_config.getChild("ior").getAttribute("value", null );
            if( ior != null )
            {
                File file = new File( ior );
                try
                {
                    if( file.exists() );
                    file.delete();
                    if( getLogger().isDebugEnabled() ) getLogger().debug( "IOR removed" );
                }
                catch( Throwable e )
                {
                    final String warning = "Failed to remove the IOR.";
                    if( getLogger().isWarnEnabled() ) getLogger().warn( warning, e );
                }
            }
        }

        //
        // cleanup state members
        //

        m_config = null;
        m_context = null;
        m_manager = null;
        m_root = null;
        m_poa = null;
    }

    //=======================================================================
    // Chooser
    //=======================================================================

   /**
    * Get the sequence of keys supported by lookup.
    */
    public String[] get_keys()
    {
        return (String[]) m_table.keySet().toArray( new String[0] );
    }

   /**
    * Locates an object reference by name.
    */
    public Adaptive lookup( final String name ) throws UnknownName
    {
        if( m_updating ) throw new TRANSIENT("Provider update in progress.");
        m_executing = true;

        getLogger().debug("lookup: " + name );

        String key = name;
        if( key.startsWith("/") )
        {
            key = name.substring( 1, name.length() );
        }

        if( key.indexOf("/") > -1 )
        {
            String primary = key.substring( 0, key.indexOf("/") );
            String remainder = key.substring( key.indexOf("/"), key.length() );
            getLogger().debug("PRIMARY: " + primary );
            getLogger().debug("REMAINDER: " + remainder );
            Chooser chooser = ChooserHelper.narrow( lookup( primary ));
            return chooser.lookup( remainder );
        }
        else 
        {
            if( !m_table.containsKey( key ) ) 
            {
                final String error = "Could not locate service: " + key;
                getLogger().warn("Lookup of name: " + key + " in: " + getKeysAsString() + " unsucessful." ); 
                throw new UnknownName( error, key );
            }
            else
            {
                try
                {
                    getLogger().debug("lookup: " + key );
                    Adaptive adaptive = AdaptiveHelper.narrow( m_table.get( key) );
                    m_executing = false;
                    getLogger().debug("resolved: " + adaptive.getClass().getName() );
                    return adaptive;
                }
                catch( Throwable e )
                {
                    m_executing = false;
                      throw new ChooserRuntimeException(
                         "Unexpected error while attempt to return service: " + key );
                }
            }
        }
    }

    //=======================================================================
    // Adaptive
    //=======================================================================

   /**
    * Returns a <code>ChooserValue</code> to the client.
    * @return Adapter an instance of <code>ChooserValue</code>.
    */
    public Adapter get_adapter()
    {
        return new ChooserValue( 
          m_service, null, m_path, m_name, m_description, this.get_keys() );
    }

    //=======================================================================
    // ChooserService
    //=======================================================================

   /**
    * Returns the object reference to the chooser service.
    * @return Finder the finder service object reference
    */
    public Chooser getChooser()
    {
        return m_service;
    }

   /**
    * Register an adaptive provider with this chooser.
    */
    public void register( String name, Adaptive service )
    {
        if( service == null ) throw new NullPointerException(
          "Illegal null service argument during registration.");

        if( !(service instanceof org.omg.CORBA.Object )) throw new
          IllegalArgumentException("Supplied service is not an object reference");

        m_updating = true;
        waitForCompletion( 1000 );
        synchronized( m_table )
        {
            m_table.remove( service );
            m_table.put( name, service );
            getLogger().debug("register: " + name );
        }
        m_updating = false;
    }

   /**
    * Removes a provider from this chooser.
    */
    public void deregister( String name )
    {
        if( name == null ) throw new NullPointerException(
          "Illegal null service name argument during deregistration.");

        m_updating = true;
        waitForCompletion( 1000 );
        synchronized( m_table )
        {
            m_table.remove( name );
            getLogger().debug("deregister: " + name );
        }
        m_updating = false;
    }

   /**
    * Wait for the execution flag to be cleared.
    * @param time the number of millisonds to wait
    */
    private void waitForCompletion( int time )
    {
        while( m_executing )
        {
            try
            {
                Thread.currentThread().sleep( time );
            }
            catch( Throwable e )
            {
            }
        }
    }

    private String getKeysAsString()
    {
        StringBuffer buffer = new StringBuffer();
        buffer.append("[");
        String[] keys = get_keys();
        for( int i=0; i<keys.length; i++ )
        {
            if(( i > 0 ) && ( i < ( keys.length )))
            {
                buffer.append( ", " );
            }
            buffer.append( keys[i] );
        }
        buffer.append("]");
        return buffer.toString();
    }
}
