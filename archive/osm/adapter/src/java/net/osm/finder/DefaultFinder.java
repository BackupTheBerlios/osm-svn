/**
 * File: DefaultServantManager.java
 * License: etc/LICENSE.TXT
 * Copyright: Copyright (C) The Apache Software Foundation. All rights reserved.
 * Copyright: OSM SARL 2001-2002, All Rights Reserved.
 */

package net.osm.finder;

import java.io.File;
import java.util.List;
import java.util.LinkedList;
import java.util.Iterator;

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
import net.osm.adapter.ServiceContext;

/**
 * <p>The <code>DefaultFinder</code> is an implementation of a 
 * root finder service.</p>
 *
 * @author <a href="mailto:mcconnell@osm.net">Stephen McConnell</a>
 */

public class DefaultFinder extends FinderPOA
implements LogEnabled, Contextualizable, Configurable, Serviceable, 
Initializable, Startable, Executable, Disposable, FinderService
{

    //=================================================================
    // state
    //=================================================================

   /**
    * Looging channel.
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
    * Tha POA created to handle the finder.
    */
    private POA m_poa;

   /**
    * Cached reference to the finder object reference.
    */
    private Finder m_service;

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
    private List m_list = new LinkedList();

   /**
    * Flag holding update state.
    */
    private boolean m_updating = false;

   /**
    * Flag holding execute state.
    */
    private boolean m_executing = false;

   /**
    * The factory key (supplied via context)
    */
    private String m_key;

   /**
    * The factory key path.
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
            m_key = (String) context.get( ServiceContext.SERVICE_KEY );
        }
        catch( ContextException e )
        {
            m_key = "finder";
        }
        try
        {
            m_path = (String[]) context.get( ServiceContext.SERVICE_PATH );
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
	      throw new CascadingException( error, e );
	  }

        byte[] ID = m_key.getBytes();
        m_poa.activate_object_with_id( ID, this );
        m_service = FinderHelper.narrow( m_poa.id_to_reference(ID) );

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
          "Finder has not been initalized.");
        if( m_disposed ) throw new IllegalStateException(
          "Finder is disposed.");
        
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
        if( !m_initialized ) return;
        if( m_disposed ) return;

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
          "Finder has not been initalized.");
        if( m_disposed ) throw new IllegalStateException(
          "Finder is disposed.");

        getLogger().debug("execution");
        getLogger().info("providers: " + m_list.size() );
        Finder finder = getFinder();
        FinderAdapter adapter = (FinderAdapter) finder.get_adapter();
        try
        {
            adapter.resolve("anything");
        }
        catch( ObjectNotFound e )
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
    // Finder
    //=======================================================================

   /**
    * Returns an object reference from the supplied path.
    *
    * @param path a URL identifying an object within the system
    * @return org.omg.CORBA.Object an object reference
    * @exception InvalidPath may be thrown if path is invalid
    * @exception ObjectNotFound thrown if the supplied path can be resolved 
    *    to an object
    */
    public Adaptive resolve( String path ) throws InvalidPath, ObjectNotFound
    {
        if( m_updating ) throw new TRANSIENT("Provider update in progress.");
        m_executing = true;
        final Iterator iterator = m_list.iterator();
        while( iterator.hasNext() )
        {
            final Finder finder = ((FinderService)iterator.next()).getFinder();
            try
            {
                finder.resolve( path );
            }
            catch( ObjectNotFound e )
            {
                 // ignore and try next
            }
        }
        m_executing = false;
        throw new ObjectNotFound( "Could not find object from path: " + path );
    }

    //=======================================================================
    // Adaptive
    //=======================================================================

   /**
    * Returns a <code>FinderAdapter</code> to the client.
    * @return Adapter an instance of <code>FinderAdapter</code>.
    */
    public Adapter get_adapter()
    {
        return new FinderValue( m_service, null, m_path, "Finder", "Generic service resolver." );
    }

    //=======================================================================
    // FinderService
    //=======================================================================

   /**
    * Returns the object reference to the singleton finder service.
    * @return Finder the finder service object reference
    */
    public Finder getFinder()
    {
        return m_service;
    }

   /**
    * Register a provider of finder acapability with the this finder.
    */
    public void register( FinderService service )
    {
        if( service == null ) throw new NullPointerException(
          "Illegal null service argument during registration.");

        m_updating = true;
        waitForCompletion( 1000 );
        synchronized( m_list )
        {
            m_list.remove( service );
            m_list.add( service );
        }
        m_updating = false;
    }

   /**
    * Removes a provider of finder acapability from this finder.
    */
    public void deregister( FinderService service )
    {
        if( service == null ) throw new NullPointerException(
          "Illegal null service argument during deregistration.");

        m_updating = true;
        waitForCompletion( 1000 );
        synchronized( m_list )
        {
            m_list.remove( service );
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
}
