/*
 * DefaultServer.java
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
import javax.security.auth.x500.X500Principal;

import org.apache.avalon.framework.component.DefaultComponentManager;
import org.apache.avalon.framework.logger.Logger;
import org.apache.avalon.framework.logger.LogEnabled;
import org.apache.avalon.framework.context.Context;
import org.apache.avalon.framework.context.Contextualizable;
import org.apache.avalon.framework.context.ContextException;
import org.apache.avalon.framework.activity.Disposable;
import org.apache.avalon.framework.activity.Initializable;
import org.apache.avalon.framework.activity.Startable;
import org.apache.avalon.framework.logger.LogEnabled;
import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.component.Component;
import org.apache.avalon.framework.component.DefaultComponentManager;
import org.apache.avalon.framework.component.ComponentManager;
import org.apache.avalon.framework.component.ComponentException;
import org.apache.avalon.framework.component.Composable;
import org.apache.avalon.phoenix.BlockContext;
import org.apache.avalon.phoenix.Block;

import org.omg.CORBA_2_3.ORB;
import org.omg.CORBA.CompletionStatus;
import org.omg.CORBA.LocalObject;
import org.omg.CORBA.Policy;
import org.omg.CORBA.PERSIST_STORE;
import org.omg.CORBA.OBJECT_NOT_EXIST;
import org.omg.CORBA.CompletionStatus;
import org.omg.PortableServer.portable.Delegate;
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
import net.osm.orb.ORBService;
import net.osm.pss.PersistanceHandler;
import net.osm.pss.PSSConnectorService;
import net.osm.pss.PSSSessionService;
import net.osm.hub.gateway.POAService;
import net.osm.realm.PrincipalManager;
import net.osm.realm.PrincipalManagerHelper;
import net.osm.realm.StandardPrincipal;
import net.osm.realm.MissingPrincipalException;
import net.osm.realm.PrincipalServerRequestInterceptor;
import net.osm.dpml.DPML;
import net.osm.util.X500Helper;
import net.osm.util.ExceptionHelper;

/**
 * A <code>DefaultServer</code> provides abstract support for the 
 * creation of resource homes enabling the delivery of factory services
 * by blocks that extend this class.  The <code>DefaultServer</code> 
 * establishes a POA and assigns this object as the servant locator.  During 
 * establishment, the server collects DPML criteria descriptions contained
 * in the configuration and passes these to a registry for publication.
 *
 * @author <a href="mailto:mcconnell@osm.net">Stephen McConnell</a>
 */

public abstract class DefaultServer extends LocalObject 
implements Block, LogEnabled, Contextualizable, Composable, Configurable, Initializable, Disposable, ServantLocator, Manager, POAService, ComponentManager
{

    //=======================================================================
    // state
    //=======================================================================

    protected ORB orb;
    protected POA poa;
    protected Configuration configuration;
    protected Connector connector;
    protected Session session;

    private Logger log;
    private boolean initialized = false;
    private boolean disposed = false;
    private BlockContext block;

   /**
    * cached servants.
    */
    private ServantCache servants;

   /**
    * The component manager that we delegate component management
    * operations to.
    */
    protected DefaultComponentManager manager = new DefaultComponentManager();

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
    // Composable
    //=================================================================
    
    /**
     * The compose operation handles the aggregation of dependent services:
     * @param provider The <code>ComponentManager</code> which this
     *                <code>Composer</code> uses.
     */
    public void compose( ComponentManager provider )
    throws ComponentException
    {
        if( getLogger().isDebugEnabled() ) getLogger().debug( "composition [default server]" );
	  try
	  {
	      ORBService orbService = (ORBService) provider.lookup("ORB");
		manager.put( "ORB", orbService );
            orb = orbService.getOrb();

		PSSConnectorService connectorService = 
		  (PSSConnectorService) provider.lookup("PSS-CONNECTOR");
		manager.put( "PSS-CONNECTOR", connectorService );
            connector = connectorService.getPSSConnector();

            PSSSessionService sessionService = 
		  (PSSSessionService) provider.lookup("PSS-SESSION");
		manager.put( "PSS-SESSION", sessionService );
            session = sessionService.getPSSSession();
        }
        catch( Exception e )
	  {
		final String error = "default composition phase failure";
		throw new ComponentException( error, e );
	  }
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
        if( getLogger().isDebugEnabled() ) getLogger().debug( "contextualize [server]" );
	  if( !(block instanceof BlockContext ) )
	  {
		final String error = "supplied context does not implement BlockContext.";
		throw new ContextException( error );
        }
	  this.block = (BlockContext) block;
    }


    //=======================================================================
    // Configurable
    //=======================================================================
    
    public void configure( final Configuration config )
    throws ConfigurationException
    {
        if( getLogger().isDebugEnabled() ) getLogger().debug( "configuration [server]" );
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
        if( getLogger().isDebugEnabled() ) getLogger().debug( "initialization [default server]" );

  	  if( initialized() ) throw new IllegalStateException("server has been initialized");
	  if( disposed() ) throw new IllegalStateException("server is in disposed state");

	  try
	  {
            this.servants = new ServantCache( block.getName() );
            Configuration persistence = configuration.getChild("persistence");
	      PersistanceHandler.register( connector, persistence );

		//
		// create the POA to handle incomming requests
		//

            if( getLogger().isDebugEnabled() ) getLogger().debug( "creating POA" );
            POA root = POAHelper.narrow(orb.resolve_initial_references("RootPOA"));
            poa = root.create_POA(
                this.block.getName(),
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
			    ServantRetentionPolicyValue.NON_RETAIN )
                }
            );
            poa.set_servant_manager( this );
            manager.put("POA", this );
        }
        catch (Exception e)
        {
		final String error = "abstract initialization failure";
            if( getLogger().isErrorEnabled() ) getLogger().error( error, e );
            throw new Exception( error, e );
        }

        //
        // advertising
        //

	  final String banner = getTitle();
        if( getLogger().isInfoEnabled() ) getLogger().info( banner );
        System.out.println( banner );
	  initialized = true;
    }

   /**
    * Return the default human friendly name of the service.
    * @return String the service title
    */
    protected String getTitle()
    {
	  try
	  {
	      String name = block.getName().substring(0,1).toUpperCase() +
		    block.getName().substring(1,block.getName().length() );
	      return "OSM " + name + " Service";
        }
	  catch( Throwable e )
	  {
		return block.getName();
	  }
    }

   /**
    * Returns true if the block has been initialized.
    */
    public boolean initialized()
    {
        return initialized;   
    }
    
    //=======================================================================
    // Disposable
    //=======================================================================
    
   /**
    * Clean up state members, dispose of resource and mark instance as disposed.
    */
    public void dispose()
    {
        servants.dispose();
        configuration = null;
        orb = null;
        poa = null;
        block = null;
        log = null;
        session = null;
        connector = null;
        disposed = true;
    }

   /**
    * Returns true if the block has been disposed of.
    */
    public boolean disposed()
    {
        return disposed;
    }


   /**
    * Factory operation to create a servant to handle an incomming request.  This 
    * operation is normally invoked by the preinvoke method if a servant is not already
    * assigned to handle a request.
    * @param oid the persistent object identifier
    */
    public abstract Servant createServant( byte[] oid );

   /**
    * Convinence utility to handle the processing of the lifecycle phases
    * oof a delegate.  The implementation applies a logger, composes the 
    * the delegate using this object as the manager, configures,
    * initalizes and starts the delegate.  To ensure that these phases are
    * invoked, a delegate implementation must implemement the respective
    * lifecycle interfaces.
    * @param delegate the object to process through component lifecycle phases
    * @param oid the object identifier
    * @return Object the instantiated object 
    */
    public Object processLifecycle( Object delegate, byte[] oid )
    {
        return processLifecycle( delegate, oid, configuration );
    }

   /**
    * Convinence utility to handle the processing of the lifecycle phases
    * of a delegate.  The implementation applies a logger, composes the 
    * the delegate using this object as the manager, configures,
    * initalizes and starts the delegate.  To ensure that these phases are
    * invoked, a delegate implementation must implemement the respective
    * lifecycle interfaces.
    * @param delegate the object to process through component lifecycle phases
    * @param oid the object identifier
    * @param config the configuration to apply to the delegate
    * @return Object the instantiated object 
    */
    public Object processLifecycle( Object delegate, byte[] oid, Configuration config )
    {
	  if( disposed() ) throw new IllegalStateException( 
		"Attempting to invoke lifecyle processing in a disposed server.");

        try
        {
		if( delegate instanceof LogEnabled )
            {
		    Logger logger = getLogger().getChildLogger( getIdentifyingTag( delegate ) );
		    ((LogEnabled)delegate).enableLogging( logger );
            }
            if( delegate instanceof Composable ) ((Composable)delegate).compose( this );
            if( delegate instanceof Contextualizable )
		{
		    ServantContext context = getContext( oid );
		    context.makeReadOnly();
		    ((Contextualizable)delegate).contextualize( context );
            }
            if( delegate instanceof Configurable ) ((Configurable)delegate).configure( config );
            if( delegate instanceof Initializable )((Initializable)delegate).initialize();
            if( delegate instanceof Startable ) ((Startable)delegate).start();
            return delegate;
        }
	  catch( Throwable e )
	  {
            final String error = "Error processing lifecycle for delegate: ";
		throw new RuntimeException( error + delegate, e );
	  }
    }

    protected String getIdentifyingTag( Object object )
    {
        return "" + System.identityHashCode( object );
    }

   /**
    * Utility method that creates the default servant context object.
    * @param oid object identifier
    * @return Context the context object to be applied to the delegate
    */
    protected ServantContext getContext( byte[] oid )
    {
	  try
	  {
	      return getContext( (StorageObject) session.find_by_pid( oid ) );
	  }
	  catch( Throwable e )
	  {
		final String error = "ServantContext creation failure.";
		throw new RuntimeException( error, e );
	  }
    }

   /**
    * Utility method that creates the default servant context object.
    * @param store storage object
    * @return Context the context object to be applied to the delegate
    */
    protected ServantContext getContext( StorageObject store )
    {
        return new ServantContext( this.block, store );
    }

    //=================================================================
    // Manager
    //=================================================================

   /**
    * Operation invoked to removal a servant from the cache.
    * @param oid persistent object identifier
    */
    public void remove( byte[] oid )
    {
        servants.remove( oid );
    }

   /**
    * Creation of an object reference given persistent storage object 
    * identifier and IDL identifier.
    * @param pid the persitent object identifier
    * @param id the IDL type identifier
    * @return org.omg.CORBA.Object the object reference
    */
    public org.omg.CORBA.Object getReference( byte[] pid, String id )
    {
        try
	  {
            return poa.create_reference_with_id( pid, id );
	  }
	  catch( Throwable e )
	  {
		String error = "unexpected exception while resolving reference";
	      throw new RuntimeException( error, e );
	  }
    }

    //=================================================================
    // POAService
    //=================================================================

   /**
    * Accessor to the current POA (Portable Object Adapter).
    * @return POA current POA
    */
    public POA getPoa( )
    {
        return poa;
    }

    //=======================================================================
    // DefaultServer
    //=======================================================================

   /**
    * Returns the name of the block.
    */
    public String getName()
    {
        return block.getName();
    }

    //=======================================================================
    // ComponentManager
    //=======================================================================

    /**
     * Get the <code>Component</code> associated with the given role.  
     *
     * @param name The role name of the <code>Component</code> to retrieve.
     * @exception ComponentException if an error occurs
     */
    public Component lookup( String role ) throws ComponentException
    {
        return manager.lookup( role );
    }

    /**
     * Check to see if a <code>Component</code> exists for a role.
     *
     * @param role  a string identifying the role to check.
     * @return True if the component exists, False if it does not.
     */
    public boolean hasComponent( String role )
    {
        return manager.hasComponent( role );
    }

    /**
     * Return the <code>Component</code> when you are finished with it.  This
     * allows the <code>ComponentManager</code> to handle the End-Of-Life Lifecycle
     * events associated with the Component.  Please note, that no Exceptions
     * should be thrown at this point.  This is to allow easy use of the
     * ComponentManager system without having to trap Exceptions on a release.
     *
     * @param component The Component we are releasing.
     */
    public void release( Component component )
    {
        manager.release( component );
    }

    //=======================================================================
    // ServantLocator
    //=======================================================================
        
   /**
    * This operations is used to get a servant that will be
    * used to process the request that caused preinvoke to
    * be called.
    *
    * @param oid the object id associated with object on which the request was made. 
    * @param adapter the reference for POA in which the object is being activated.
    * @param operation the operation name.
    * @param the_cookie an opaque value that can be set by the servant manager to be used
    *   during postinvoke.
    * @return Servant used to process incoming request.
    * @exception ForwardRequest to indicate to the ORB that it is responsible for delivering 
    *   the current request and subsequent requests to the object denoted in the 
    *   forward_reference member of the exception.
    */
    public Servant preinvoke( final byte[] oid, final POA adapter, final String operation, 
      final CookieHolder cookie)
    throws ForwardRequest
    {
        StorageObject s = null;
	  try
	  {
	      s = (StorageObject) session.find_by_pid( oid );
	  }
	  catch( NotFound nf )
	  {
            if( getLogger().isDebugEnabled() ) getLogger().debug(
		  "preinvoke: " + operation + ", object not found" );
		throw new OBJECT_NOT_EXIST( 0, CompletionStatus.COMPLETED_NO );
	  }
        catch( Throwable shouldNotHappen )
        {
		final String error = "Unexpected PSS related error while attempting to resolve an oid:"
		  + " against the operation: " + operation;
            if( getLogger().isErrorEnabled() ) getLogger().error( error, shouldNotHappen );
		ExceptionHelper.printException( error, shouldNotHappen, this, true );

		// WARNING: minor code needs to be checked against spec
		throw new PERSIST_STORE( error , 0, org.omg.CORBA.CompletionStatus.COMPLETED_NO );
        }

        Servant servant = null;
        if( getLogger().isDebugEnabled() ) getLogger().debug("preinvoke: " + operation );
	  synchronized( servants )
	  {
            try
            {
                servant = servants.locate( oid );
            }
            catch( NotFound nf )
            {
		    try
		    {
                    servant = createServant( oid );
		        servants.add( oid, servant );
		    }
                catch( NotFound e )
	          {
                    //
                    // indicates that a storage object existed when the invocation
                    // came in, but was removed before reaching this point in the 
                    // implementation
                    //

                    if( getLogger().isDebugEnabled() ) getLogger().debug(
		           "preinvoke/2: " + operation + ", object not found" );
		        throw new OBJECT_NOT_EXIST( 0, CompletionStatus.COMPLETED_NO );
	          }
		    catch( Throwable e )
	          {
			  final String error = "Internal error while creating servant.";
			  final String detail = error
			    + "\n\tException raised while handling invocation against operation '" 
			    +  operation + "'";
		        if( getLogger().isErrorEnabled() ) getLogger().error( detail, e );
			  ExceptionHelper.printException( detail, e, this, true );
		        throw new org.omg.CORBA.INTERNAL( error );
		    }
		}
        }
        cookie.value = oid;
        return servant;
    }

   /**
    * This operation is invoked whenever a servant completes
    * a request.
    *
    * @param oid the object id ssociated with object on which the request was made.
    * @param adapter the reference for POA in which the object was active.
    * @param operation the operation name.
    * @param the_cookie  an opaque value that contains the data set by preinvoke.
    * @param the_servant reference to the servant that is associated with the object.
    */
    public void postinvoke( final byte[] oid, final POA adapter, final String operation, 
      final Object cookie, final Servant the_servant)
    {
        if(oid != cookie )
        {
		final String error = "invocation against an OID that does not exist";
            if( getLogger().isErrorEnabled() ) getLogger().error( error );
            throw new OBJECT_NOT_EXIST( error , 0, org.omg.CORBA.CompletionStatus.COMPLETED_NO );
        }
        if( !operation.equals("_is_a")) if( getLogger().isDebugEnabled()  )  
	  {
		getLogger().debug("postinvoke: " + operation );
	  }
    }

    //==================================================================================
    // internals 
    //==================================================================================
    
   /**
    * Returns the current security principal.
    */
    public StandardPrincipal getCurrentPrincipal( ) throws Exception
    {
        return PrincipalServerRequestInterceptor.getCarrierPrincipal();
    }

    private String getPrincipalName( StandardPrincipal standard )
    {
	  return getPrincipalName( standard.getPrincipal() );
    }

    private String getPrincipalName( Principal principal )
    {
        if( principal instanceof X500Principal ) return getPrincipalName( (X500Principal) principal );
        return principal.getName();
    }

    private String getPrincipalName( X500Principal principal )
    {
        X500Helper helper = new X500Helper( principal.getName() );
        return helper.getCommonName();
    }

}
