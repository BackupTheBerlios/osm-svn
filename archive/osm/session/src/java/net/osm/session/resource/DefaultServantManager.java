/**
 * File: DefaultServantManager.java
 * License: etc/LICENSE.TXT
 * Copyright: Copyright (C) The Apache Software Foundation. All rights reserved.
 * Copyright: OSM SARL 2001-2002, All Rights Reserved.
 */

package net.osm.session.resource;

import java.net.URL;
import java.util.Hashtable;

import org.apache.avalon.framework.activity.Disposable;
import org.apache.avalon.framework.activity.Initializable;
import org.apache.avalon.framework.activity.Startable;
import org.apache.avalon.framework.context.Context;
import org.apache.avalon.framework.context.Contextualizable;
import org.apache.avalon.framework.context.ContextException;
import org.apache.avalon.framework.logger.Logger;
import org.apache.avalon.framework.logger.LogEnabled;
import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.service.DefaultServiceManager;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.Serviceable;
import org.apache.avalon.framework.CascadingRuntimeException;
import org.apache.avalon.framework.CascadingException;
import org.apache.orb.ORBContext;
import org.apache.orb.corbaloc.Handler;
import org.apache.pss.ORB;
import org.apache.pss.Connector;
import org.apache.pss.ConnectorContext;
import org.apache.pss.Session;
import org.apache.pss.SessionContext;
import org.apache.pss.StorageContext;
import org.apache.pss.DefaultStorageContext;

import org.omg.CORBA.Request;
import org.omg.CORBA.DomainManager;
import org.omg.CORBA.NVList;
import org.omg.CORBA.NamedValue;
import org.omg.CORBA.ContextList;
import org.omg.CORBA.ExceptionList;
import org.omg.CORBA.SetOverrideType;

import org.omg.PortableServer.POA;
import org.omg.PortableServer.Servant;
import org.omg.PortableServer.ServantActivator;
import org.omg.PortableServer.ForwardRequest;
import org.omg.PortableServer.POAHelper;
import org.omg.PortableServer.IdAssignmentPolicyValue;
import org.omg.PortableServer.RequestProcessingPolicyValue;
import org.omg.PortableServer.LifespanPolicyValue;
import org.omg.PortableServer.ServantRetentionPolicyValue;
import org.omg.CosPersistentState.StorageObject;
import org.omg.CORBA.Policy;
import org.omg.CORBA.LocalObject;

import net.osm.adapter.Adapter;
import net.osm.adapter.Adaptive;
import net.osm.finder.FinderPOA;
import net.osm.finder.FinderAdapter;
import net.osm.finder.FinderValue;
import net.osm.finder.FinderHelper;
import net.osm.finder.Finder;
import net.osm.session.ReferenceObject;

/**
 * The <code>DefaultServantManager</code> is an abstract utility class that provides
 * a CORBA based server default implmentation.
 *
 * @author <a href="mailto:mcconnell@osm.net">Stephen McConnell</a>
 */

//public abstract class DefaultServantManager extends FinderPOA
public abstract class DefaultServantManager extends ManagerPOA
implements ServantActivator, LogEnabled, Contextualizable, Configurable, Serviceable, 
Initializable, Startable, Disposable, ActivatorService, ReferenceObject
{

    //=================================================================
    // state
    //=================================================================

    private Logger m_logger;
    private Configuration m_config;
    private ServiceManager m_manager;
    private Context m_context;

    private POA m_root;
    private ORB m_orb;
    private POA m_poa;
    private Connector m_connector;
    private Session m_session;
    private ServiceManager m_serviceManager;

    private boolean m_initialized = false;
    private boolean m_disposed = false;

    private FinderAdapter m_adapter;

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
    // Configurable
    //=======================================================================
    
   /**
    * Configuration of the component by its container.
    * @param config the configuration to apply
    * @exception ConfigurationException if a configuration related 
    *   exception occurs
    */
    public void configure( final Configuration config )
    throws ConfigurationException
    {
        if( getLogger().isDebugEnabled() ) 
        {
            getLogger().debug( "configuration of " + this.getClass().getName() );
        }
        if( null != m_config ) 
        {
            throw new ConfigurationException( 
              "Configurations for block " + this + " already set" );
        }
        m_config = config;
    }

    //=======================================================================
    // Contextualizable
    //=======================================================================

   /**
    * Set the component context.
    * @param context the component context
    */
    public void contextualize( Context context )
    {
        if( getLogger().isDebugEnabled() ) 
        {
            getLogger().debug( "contextualization of " + this.getClass().getName() );
        }
        m_context = context;
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
     * @exception ServiceException if a service related error occurs
     */
    public void service( ServiceManager manager )
    throws ServiceException
    {
        if( getLogger().isDebugEnabled() ) 
        {
            getLogger().debug( "service " + this.getClass().getName() );
        }
        m_manager = manager;
    }

    //=======================================================================
    // Initializable
    //=======================================================================
    
   /**
    * Initialization of the component.
    * @exception Exception if an initalixation error occurs
    */
    public void initialize()
    throws Exception
    {
        if( m_logger == null )
        {
            throw new IllegalStateException(
              "DefaultServantManager has not been log enabled.");
        }
        else
        {
            getLogger().debug( "initialize " + this.getClass().getName() );
        }

        if( m_config == null ) 
        {
            throw new IllegalStateException(
              "DefautlServantManager has not been supplied with a configuration.");
        }

        if( m_context == null ) 
        {
            throw new IllegalStateException(
              "DefautlServantManager has not been supplied with a context.");
        }

        if( m_manager == null ) 
        {
            throw new IllegalStateException(
              "DefautlServantManager has not been supplied with a service manager.");
        }

        if( getLogger().isDebugEnabled() ) 
        {
             getLogger().debug( "initialization" );
        }

        //
        // get the orb
        //

        m_orb = (ORB) m_manager.lookup( ORBContext.ORB_KEY );

        //
        // if the PSS conntetor already exists in the context that use that, otherwise
        // create a connector based on the configuration and either case, apply persistent
        // home and type declarations
        //

        try
        {
            m_connector = (Connector) m_context.get( ConnectorContext.CONNECTOR_KEY );
        }
        catch( ContextException e )
        {
            m_connector = m_orb.getConnector();
        }

        Configuration pss = m_config.getChild("pss");
        try
        {
            m_connector.register( pss.getChild("persistence") );
        }
        catch( Throwable e )
        {
            String error = "PSS related initalization error in " + 
               this.getClass().getName();
            throw new CascadingException( error, e );
        }

        //
        // if the PSS session already exists in the context that use that, otherwise
        // create a basic session based on the configuration 
        //

        try
        {
            m_session = (Session) m_context.get( SessionContext.SESSION_KEY );
        }
        catch( ContextException e )
        {
            m_session = m_connector.createBasicSession( pss.getChild("session") );
        }

        //
        // create the POA
        //
            
        m_poa = createPOA();
        m_initialized = true;

        //
        // create the adapter
        //
/*
        try
        {
            Manager manager = ManagerHelper.narrow( 
              m_poa.create_reference_with_id( 
                getPoaName().getBytes(), ManagerHelper.id()));
            m_adapter = new FinderValue( manager, null, new String[0], getPoaName(), "default description" );
        }
        catch( Throwable e )
        {
            throw new AbstractResourceException(
              "Failed to create finder reference or adapter.", e );
        }
*/
    }

    //=======================================================================
    // Startable
    //=======================================================================

   /**
    * Start the component.
    * @exception Exception if a startup related error occurs.
    */
    public void start() throws Exception
    {
        if( getLogger().isDebugEnabled() ) 
        {
            getLogger().debug( "starting " + this.getClass().getName() );
        }
        try
        {
            m_poa.the_POAManager().activate();
        }
        catch( Throwable e )
        {
            if( getLogger().isWarnEnabled() ) 
            {
                getLogger().warn( "startup exception", e );
            }
        }
    }

   /**
    * Stop the component.
    * @exception Exception if a shutdown related error occurs.
    */
    public void stop() throws Exception
    {
        if( getLogger().isDebugEnabled() ) 
        {
            getLogger().debug( "stopping " + this.getClass().getName() );
        }
        try
        {
            m_poa.destroy( true, true );
        }
        catch( Throwable e )
        {
            if( getLogger().isWarnEnabled() ) 
            {
                getLogger().warn( "ignoring POA related exception" );
            }
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
        if( getLogger().isDebugEnabled() ) 
        {
            getLogger().debug( "disposal of " + this.getClass().getName() );
        }
        m_session.close();
        m_disposed = true;
    }

   /**
    * Returns the disposed state of the instance.
    * @return boolean TRUE if the instance has been initialized
    */
    protected boolean disposed()
    {
        return m_disposed;
    }

    //=======================================================================
    // utilities
    //=======================================================================

   /**
    * Returns the initalized state of the instance.
    * @return boolean TRUE if the instance has been initialized
    */
    protected boolean initialized()
    {
        return m_initialized;
    }

   /**
    * Returns the PSS connector established during initialization.
    * @return Connector the PSS connector
    */
    protected Connector getConnector()
    {
        return m_connector;
    }

   /**
    * Returns the PSS session established during initialization.
    * @return Session the PSS session
    */
    protected Session getSession()
    {
        return m_session;
    }

   /**
    * Method by which derived types can override POA creation.
    * @return POA the portable object adapter
    * @exception Exception if an error occurs while attempt to create the POA
    */
    protected POA createPOA() throws Exception
    {
        try
        {
            POA root = POAHelper.narrow(m_orb.resolve_initial_references("RootPOA"));
            POA poa = root.create_POA(
                getPoaName(),
                root.the_POAManager(),
                new Policy[]
                {
                    root.create_id_assignment_policy( 
                      IdAssignmentPolicyValue.USER_ID ),
                    root.create_lifespan_policy( 
                      LifespanPolicyValue.PERSISTENT ),
                    root.create_request_processing_policy( 
                      RequestProcessingPolicyValue.USE_SERVANT_MANAGER ),
                    root.create_servant_retention_policy( 
                      ServantRetentionPolicyValue.RETAIN )
                }
            );
            poa.set_servant_manager( this );
            return poa;
        }
        catch( Throwable e )
        {
            String error = "Unable to create the POA";
            throw new CascadingException( error, e );
        }
    }

   /**
    * Retuns the name to be assigned to the POA on creation.  This is an 
    * abstract method that must be overriden by derived types.
    * @return String the POA name
    */
    protected abstract String getPoaName();

    //=======================================================================
    // ServantActivator
    //=======================================================================

   /**
    * This operation is invoked by the POA whenever the 
    * POA receives a request for an object that is not 
    * currently active.  
    * @param oid identifier of the object on the request was made
    * @param adapter object reference for the POA in which the object is being activated
    * @return Servant the servant handling the request
    * @exception ForwardRequest to indicate to the ORB 
    *  that it is responsible for delivering the current request and subsequent 
    *  requests to the object denoted in the forward_reference member of the exception.
    */
    public Servant incarnate (byte[] oid, POA adapter) 
    throws ForwardRequest
    {
        if( compare( oid, getPoaName().getBytes() ) )
        {
            if( getLogger().isDebugEnabled() ) 
            {
                getLogger().debug( "local incarnation" );
            }
            return this;
        }
        else
        {
            if( getLogger().isDebugEnabled() ) 
            {
                getLogger().debug( "delegate incarnation" );
            }
            try
            {
                StorageObject store = (StorageObject) m_session.find_by_pid( oid );
                DefaultStorageContext context = 
                  new DefaultStorageContext( m_context, store );
                context.makeReadOnly();

                return createServant( context, m_config, 
                  getServiceManager( m_manager ) );
            }
            catch( Throwable e )
            {
                throw new CascadingRuntimeException( 
                  "Unable to incarnate servant", e );
            }
        }
    }

   /**
    * Returns the servant manager to by applied to servants created by this manager.
    * @param parent the parent service manager
    * @return ServiceManager the manager to apply to created servants
    */
    protected ServiceManager getServiceManager( ServiceManager parent )
    {
        if( m_serviceManager != null ) 
        {
            return m_serviceManager;
        }
        DefaultServiceManager manager = new DefaultServiceManager( parent );
        manager.put( ORBContext.ORB_KEY, m_orb );
        manager.put( ConnectorContext.CONNECTOR_KEY, m_connector );
        manager.put( SessionContext.SESSION_KEY, m_session );
        manager.put( ActivatorService.ACTIVATOR_KEY, this );
        manager.makeReadOnly();
        m_serviceManager = manager;
        return manager;
    }

   /**
    * Returns a servant implmentation that will be managed by the servant manager.
    * This abstract method must be overriden by derived types.
    * @param context the servant context to apply
    * @param config the servant configuration to apply
    * @param manager the servant manager to apply
    * @return Servant a managable servant
    * @exception Exception is a servant resolution error occured
    */
    protected abstract Servant createServant( StorageContext context, Configuration config, 
      ServiceManager manager ) throws Exception;

   /**
    * This operation is invoked whenever a servant for an object is deactivated. 
    * @param oid object Id associated with the object being deactivated.
    * @param adapter object reference for the POA in which the object was active.
    * @param serv contains reference to the servant associated with the object 
    *  being deactivated.
    * @param cleanup if TRUE indicates that destroy or deactivate is 
    *  called with etherealize_objects param of TRUE.  FALSE indicates that 
    *  etherealize was called due to other reasons.
    * @param remaining indicates whether the Servant Manager can 
    *  destroy a servant.  If set to TRUE, the Servant Manager should wait
    *  until all invocations in progress have completed.
    */
    public void etherealize (byte[] oid, POA adapter, Servant serv, boolean cleanup, 
       boolean remaining )
    {
        if( getLogger().isDebugEnabled() ) 
        {
            getLogger().debug( "etherealize" );
        }
        if( !remaining && ( serv instanceof Disposable ))
        {
            ((Disposable)serv).dispose();
        }
    }

    //=======================================================================
    // ReferenceObject
    //=======================================================================

   /**
    * Returns an object reference to the server.
    * @return the object reference
    */
    public abstract org.omg.CORBA.Object getReference();

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
    public abstract org.omg.CORBA.Any select(String ref)
        throws org.apache.orb.corbaloc.UnknownReference, org.apache.orb.corbaloc.InvalidReference, org.apache.orb.corbaloc.ServiceRedirection;


    //=======================================================================
    // Finder
    //=======================================================================
/*
    public Adaptive resolve( String query )
    {
        System.out.println("QUERY: " + query );
        return null;
    }

    public Adapter get_adapter()
    {
        return m_adapter;
    }
*/
    //=======================================================================
    // Servant
    //=======================================================================

    public Request _create_request( org.omg.CORBA.Context ctx,
                                    String operation,
                                    NVList arg_list,
                                    NamedValue result )
    {
        throw new org.omg.CORBA.NO_IMPLEMENT();
    }

    public Request _create_request( org.omg.CORBA.Context ctx,
                                    String operation,
                                    NVList arg_list,
                                    NamedValue result,
                                    ExceptionList exceptions,
                                    ContextList contexts )
    {
        throw new org.omg.CORBA.NO_IMPLEMENT();
    }

    public org.omg.CORBA.Object _duplicate()
    {
        throw new org.omg.CORBA.NO_IMPLEMENT();
    }

    public DomainManager[] _get_domain_managers()
    {
        throw new org.omg.CORBA.NO_IMPLEMENT();
    }

    public org.omg.CORBA.Object _get_interface_def()
    {
        throw new org.omg.CORBA.NO_IMPLEMENT();
    }

    public Policy _get_policy( int policy_type )
    {
        throw new org.omg.CORBA.NO_IMPLEMENT();
    }

    public int _hash( int maximum )
    {
        return hashCode() % ( maximum + 1 );
    }

    public boolean _is_a( String id )
    {
        return _get_delegate().is_a( this, id );
    }

    public boolean _is_equivalent( org.omg.CORBA.Object that )
    {
        return equals( that );
    }

    public boolean _non_existent()
    {
        return _get_delegate().non_existent( this );
    }

    public void _release()
    {
    }

    public Request _request( String operation )
    {
        throw new org.omg.CORBA.NO_IMPLEMENT();
    }

    public org.omg.CORBA.Object _set_policy_override( Policy[] policies,
            SetOverrideType set_add )
    {
        throw new org.omg.CORBA.NO_IMPLEMENT();
    }

    //=======================================================================
    // internal utilities
    //=======================================================================

    /**
     * Compares two byte arrays for equality.
     *
     * @param abyte0 first array
     * @param abyte1 second array
     * @return boolean true if these two byte arrays are 
     *    byte-for-byte equivalent
     */
    private static boolean compare( byte abyte0[], byte abyte1[] )
    {
        if ( abyte0.length != abyte1.length ) 
        {
            return false;
        }
        for ( int i = 0; i < abyte0.length; i++ )
        {
            if ( abyte0[i] != abyte1[i] ) 
            {
                return false;
            }
        }
        return true;
    }

    private POA getRootPOA()
    {
        if( m_root != null ) 
        {
            return m_root;
        }
        try
        {
            m_root = POAHelper.narrow(m_orb.resolve_initial_references("RootPOA"));
            return m_root;
        }
        catch( Throwable e )
        {
            final String error = 
              "Unexpected exception while atttempt to get the root POA.";
            throw new CascadingRuntimeException( error, e );
        }
    }


}
