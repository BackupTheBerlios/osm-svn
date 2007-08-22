/**
 */

package net.osm.factory;

import java.io.File;
import java.util.Map;
import java.util.Hashtable;

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
import net.osm.adapter.ServiceContext;
import net.osm.factory.FactoryPOA;
import net.osm.factory.FactoryService;
import net.osm.factory.FactoryHelper;
import net.osm.factory.Argument;
import net.osm.factory.Parameter;

/**
 * <p>The <code>FactoryProvider</code> is a server implementation
 * that handles object reference creation requests.</p>
 *
 * @author <a href="mailto:mcconnell@osm.net">Stephen McConnell</a>
 */

public abstract class FactoryProvider extends FactoryPOA
implements LogEnabled, Contextualizable, Configurable, Serviceable, 
Initializable, Startable, Disposable, FactoryService
{

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
    * (not used)
    */
    private Context m_context;

   /**
    * The root POA provided via the service manager.
    */
    private POA m_root;

   /**
    * Tha POA created to handle the factory.
    */
    private POA m_poa;

   /**
    * Cached reference to the factory object reference.
    */
    private Factory m_service;

   /**
    * Flag holding initialized state.
    */
    private boolean m_initialized = false;

   /**
    * Flag holding disposed state.
    */
    private boolean m_disposed = false;

   /**
    * The factory name (changable via configuration)
    */
    private String m_name = "Factory";

   /**
    * The factory description (changable via configuration)
    */
    private String m_description = "Support for the creation of new object references.";

   /**
    * The factory key (supplied via context)
    */
    private String m_key;

   /**
    * The factory key path.
    */
    private String[] m_path;

   /**
    * The default parameter list
    */
    private Parameter[] m_params = new Parameter[0];

   /**
    * The default name to apply to new instances produced by this factory.
    */
    private String m_defaultName = "Untitled Service";


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
    * @see ServiceContext
    */
    public void contextualize( Context context )
    throws ContextException
    {
	  if( getLogger().isDebugEnabled() ) getLogger().debug( "contextualization" );
	  m_context = context;
        m_key = (String) context.get( ServiceContext.SERVICE_KEY );
        m_path = (String[]) context.get( ServiceContext.SERVICE_PATH );
    }

    //=======================================================================
    // Configurable
    //=======================================================================
    
    public void configure( final Configuration config )
    throws ConfigurationException
    {
	  if( getLogger().isDebugEnabled() ) getLogger().debug( "configuration" );
        if( null != m_config ) throw new ConfigurationException( 
	        "Configurations for " + this + " already set" );
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
          "Service has been disposed.");

        if( m_initialized ) throw new IllegalStateException(
          "Service cannot be re-initialized.");

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
	      throw new FactoryException( error, e );
	  }

        byte[] ID = m_key.getBytes();
        m_poa.activate_object_with_id( ID, this );
        m_service = FactoryHelper.narrow( m_poa.id_to_reference(ID) );

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
          "Service has not been initalized.");
        if( m_disposed ) throw new IllegalStateException(
          "Service is disposed.");
        
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
          "Service has not been initalized.");
        if( m_disposed ) throw new IllegalStateException(
          "Service is disposed.");

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
    // Factory
    //=======================================================================

   /**
    * Returns an object refererence to the factory.
    * @return Factory the factory object reference.
    */
    public Factory getFactory()
    {
        return m_service;
    }

   /**
    * Returns the default name to apply to instances created by this factory.
    * @return String default name
    */
    public String get_default_name()
    {
        return m_defaultName;
    }
    

   /**
    * Returns an array of <code>Parameter</code> values describing 
    * the arguments to the <code>create</code> operation.
    * @return Parameter[] parameter array
    */
    public Parameter[] get_parameters()
    {
        return m_params;
    }

    /**
     * Creates a new object reference
     * @param  arguments an array of arguments 
     * @return  Adaptive an adaptive object reference
     * @exception  UnrecognizedCriteria if the arguments established by the
     *   adapter implementation is unknown to the factory
     * @exception  InvalidCriteria if the arguments created by the 
     *   implementation is recognized but rejected as invalid
     */
    public abstract Adaptive create( Argument[] arguments ) 
    throws UnrecognizedCriteria, InvalidCriteria, CreationException;

    //=======================================================================
    // Adaptive
    //=======================================================================

   /**
    * Returns a <code>FactoryValue</code> to the client. 
    * @return Adapter an instance of <code>FactoryValue</code>.
    */
    public Adapter get_adapter()
    {
        return new FactoryValue( 
          m_service, null, m_path, m_name, m_description, get_parameters(), get_default_name() );
    }
}
