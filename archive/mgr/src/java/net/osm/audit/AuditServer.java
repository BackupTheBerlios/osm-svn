/*
 * @(#)AuditServer.java
 *
 * Copyright 2000 OSM SARL. All Rights Reserved.
 * 
 * This software is the proprietary information of OSM SARL.  
 * Use is subject to license terms.
 * 
 * @author  Stephen McConnell
 * @version 1.0 26/03/2001
 */

package net.osm.audit;

import java.io.File;
import java.util.List;
import java.util.Enumeration;
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
import org.omg.Session.AbstractResource;
import org.omg.Session.AbstractResourceHelper;
import org.omg.Session.IdentifiableDomainConsumer;
import org.omg.Session.IdentifiableDomainConsumerPOA;
import org.omg.Session.IdentifiableDomainConsumerHelper;
import org.omg.CosEventComm.Disconnected;
import org.omg.CosNotification.StructuredEvent;
import org.omg.CosNotification.EventType;
import org.omg.CosNotifyComm.InvalidEventType;
import org.omg.CosNotifyComm.StructuredPushSupplier;
import org.omg.CosObjectIdentity.IdentifiableObject;
import org.omg.NamingAuthority.AuthorityId;
import org.omg.NamingAuthority.RegistrationAuthority;

import net.osm.orb.ORBService;
import net.osm.util.ExceptionHelper;


/**
 * 
 *
 * @author <a href="mailto:mcconnell@osm.net">Stephen McConnell</a>
 */

public class AuditServer extends IdentifiableDomainConsumerPOA 
implements Block, LogEnabled, Contextualizable, Composable, Configurable, Initializable, Startable, Disposable, AuditService
{

    //==================================================
    // state
    //==================================================

    private ORB orb;
    private Connector connector;
    private Session session;
    private Configuration configuration;

    private Logger log;
    private POA poa;
    private boolean initialized = false;
    private boolean disposed = false;
    private BlockContext block;
    private IdentifiableDomainConsumer audit;

    private final Hashtable table = new Hashtable();
    private AuthorityId authorityId;
    private POA root;

    //==================================================
    // Loggable
    //==================================================
    
   /**
    * Sets the logging channel.
    * @param logger the logging channel
    */
    public void enableLogging( final Logger logger )
    {
        log = logger;
    }

   /**
    * Returns the logging channel.
    * @return Logger the logging channel
    * @exception IllegalStateException if the logging channel has not been set
    */
    public Logger getLogger()
    {
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
	  if( !(block instanceof BlockContext ) )
	  {
		final String error = "supplied context does not implement BlockContext.";
		throw new ContextException( error );
        }
	  this.block = (BlockContext) block;
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
            orb = ((ORBService) manager.lookup("ORB")).getOrb();
	  }
	  catch( Exception e )
	  {
		String error = "unexpected exception during composition";
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

        final RegistrationAuthority authority = getAuthorityFromConfiguration( configuration );
        final String address = this.configuration.getChild("domain").getAttribute( "address", "localhost" );
	  authorityId = new AuthorityId( authority, address );

        //
        // create the POA
        //
            
        try
	  {
            root = POAHelper.narrow(orb.resolve_initial_references("RootPOA"));
            POA poa = root.create_POA(
              "AUDIT",
              root.the_POAManager(),
              new Policy[]{
                  root.create_id_assignment_policy( IdAssignmentPolicyValue.USER_ID ),
                  root.create_lifespan_policy( LifespanPolicyValue.PERSISTENT )});

            //
            // create and bind this object as the servant and create an 
            // object reference for publication
            //
                        
            byte[] ID = "AUDIT".getBytes();
            poa.activate_object_with_id( ID, this );
            org.omg.CORBA.Object obj = poa.id_to_reference(ID);
            audit = IdentifiableDomainConsumerHelper.narrow( obj );
	  }
	  catch( Throwable poaError )
	  {
	      String error = "Unable to create the audit IDC object reference";
	      throw new Exception( error, poaError );
	  }

        String message = "OSM Audit";
        getLogger().info( message );
    }

    public void start() throws Exception
    {
	  try
	  {
            root.the_POAManager().activate();
        }
	  catch( Throwable e )
	  {
		final String error = "Unable to start audit server due to POA activation error.";
		throw new Exception( error, e );
	  }
    }

    public void stop() throws Exception
    {
	  synchronized( table )
	  {
            Enumeration elements = table.elements();
		while( elements.hasMoreElements() )
	      {
		    ListenerList list = (ListenerList) elements.nextElement();
		    try
		    {
		        StructuredPushSupplier supplier = list.getSupplier();
			  if( !supplier._non_existent() ) supplier.disconnect_structured_push_supplier();
		    }
		    catch( org.omg.CORBA.OBJECT_NOT_EXIST e )
	          {
		    }
		    catch( Throwable e )
	          {
			  final String warning = "Unexpected exception throw by server while disconnecting supplier.";
			  if( getLogger().isWarnEnabled() ) getLogger().warn( warning, e );
		    }
	      }
	  }
    }

    //=======================================================================
    // AuditService
    //=======================================================================

   /**
    * Associate the audit service with a primary <code>AbstractResource</code>.
    * @param resource the primary <code>AbstractResource</code> event source
    * @param listener the <code>RemoteEventListener</code> to be notified of 
    *   incomming events and disconnection actions
    */
    public void addRemoteEventListener( AbstractResource resource, RemoteEventListener listener )
    {

        // add the listener to the set of listeners for that resource, 
        // get the structured push supplier (if not already known) and 
        // associate this instance as a listener

        synchronized( table )
        {
            ListenerList listeners = (ListenerList) table.get( resource );
            if( listeners == null ) try
            {
		    // we need to locate the supplier and establish a 
                // subscription

		    listeners = new ListenerList( resource.add_consumer( audit ) );
		    listeners.enableLogging( getLogger().getChildLogger("list") );
		    if( getLogger().isDebugEnabled() ) getLogger().debug(
			"adding listener " + System.identityHashCode( listener ) + " to new list " 
			+ System.identityHashCode( listeners ));
		    listeners.add( listener );
		    table.put( resource, listeners );
	      }
            catch( Throwable e )
            {
                final String error = "Unable to create a new listener.";
		    throw new RuntimeException( error, e );
            }
	      else
	      {
	          if( listeners.locate( listener ) < 0 )
                {
                    listeners.add( listener );
		        if( getLogger().isDebugEnabled() ) getLogger().debug( 
				"adding listener " + System.identityHashCode( listener ) + " to existing list " 
				+ System.identityHashCode( listeners ));
                }
		    else
		    {
		        if( getLogger().isWarnEnabled() ) getLogger().warn("listener already registered" 
			    + "\n\texisting: " + listeners.get( listeners.locate( listener ) )  
			    + "\n\tsupplied: " + listener
		        );
		    }
            }
	  }
    }

   /**
    * Remove a listener from from a primary <code>AbstractResource</code>.
    * @param resource the primary <code>AbstractResource</code> event source
    * @param listener the <code>RemoteEventListener</code> to be removed 
    * from the set of listeners.
    */
    public void removeRemoteEventListener( AbstractResource resource, RemoteEventListener listener )
    {
        if( getLogger().isDebugEnabled() ) getLogger().debug(
		"removing listener " + System.identityHashCode( listener ));

        synchronized( table )
	  {
            final ListenerList listeners = (ListenerList) table.get( resource );
            if( listeners != null ) 
            {
	          listeners.remove( listener );
                if( listeners.size() == 0 ) 
		    {
		        // no more interest in this resource so we can disconnect ourselves

	              if( getLogger().isDebugEnabled() ) getLogger().debug("disconnecting from supplier");
			  try
			  {
		            listeners.getSupplier().disconnect_structured_push_supplier();
			  }
			  catch( Throwable e )
	              {
				// log error but don't throw an exception
                        final String warning = "subscriber disconnection exception";
				if( getLogger().isWarnEnabled() ) getLogger().warn( warning, e );
			  }
                }
		}
        }
    }

   /**
    * Returns the boolean status of the connection between the supplied 
    * resource structured push supplier and the audit service consumer.
    * @return boolean tru if a subscription is established
    */
    public boolean getConnected( AbstractResource resource )
    {
        try
	  {
		return !resource._non_existent();
	  }
	  catch( Throwable e )
	  {
            return false;
	  }
    }

    //=======================================================================
    // StructuredPushConsumer
    //=======================================================================

   /**
    * The push_structured_event operation takes as input a parameter of type
    * StructuredEvent as defined in the CosNotification module. Upon invocation, this
    * parameter will contain an instance of a Structured Event being delivered to the
    * consumer by the supplier to which it is connected. If this operation is invoked upon a
    * StructuredPushConsumer instance that is not currently connected to the supplier of
    * the event, the Disconnected exception will be raised.
    */
    public void push_structured_event( StructuredEvent event )
    throws Disconnected
    {

        AbstractResource source = null;
        RemoteEvent remote = new RemoteEvent( audit, event );
        if( getLogger().isDebugEnabled() ) getLogger().debug( 
		"incomming event\n\tEVENT: " + remote );
        try
	  {
            final Any any = remote.getProperty("source");
		if( any == null )
		{
		    final String warning = "Structured event source property is missing.";
                if( getLogger().isWarnEnabled() ) getLogger().warn( warning );
                return;
		}
		else
		{
		    source = AbstractResourceHelper.extract( any );
		}
	  }
	  catch( org.omg.CORBA.MARSHAL e )
	  {
	      final String warning = "Unable to resolve the source AbstractResource property value";
		if( getLogger().isWarnEnabled() ) getLogger().warn( warning , e );
		ExceptionHelper.printException( warning, e, this );
		try
		{
		    final Any any = remote.getProperty("source");
		    org.omg.CORBA.Object object = any.extract_Object();
		    if( getLogger().isWarnEnabled() ) getLogger().warn( "\tobject: " + object );
		    System.out.println("OBJECT: " + object );
		}
		catch( Throwable internal )
		{
		}
		return;
	  }
	  catch( Throwable e )
	  {
	      final String warning = "Undocumented exception while resolving event source property.";
		if( getLogger().isWarnEnabled() ) getLogger().warn( warning, e );
		ExceptionHelper.printException( warning, e, this );
		return;
	  }

        synchronized( table )
        {
            final ListenerList listeners = (ListenerList) table.get( source );
            if( listeners != null ) 
	      {
	          final String debug = "Located listener list containing " + listeners.size() + " listeners.";
		    if( getLogger().isDebugEnabled() ) getLogger().debug( debug );
		    listeners.fireStructuredEvent( remote );
	      }
	      else
	      {
	          final String warning = "Incomming event does not map to to a listener list.";
		    if( getLogger().isWarnEnabled() ) getLogger().warn( warning );
		    System.out.println( warning + "\n" + remote );
            }
        }
    }

   /**
    * The disconnect_structured_push_consumer operation is invoked to terminate a
    * connection between the target StructuredPushConsumer, and its associated supplier.
    * This operation takes no input parameters and returns no values. The result of this
    * operation is that the target StructuredPushConsumer will release all resources it had
    * allocated to support the connection, and dispose its own object reference.
    */
    public void disconnect_structured_push_consumer()
    {
	  //log.debug("Disconnecting adapter.");
	  //if( store == null ) return;
        //store.connected( false );
	  //dispose();

	  final String debug = "Received disconnection request.";
        if( getLogger().isDebugEnabled() ) getLogger().debug( debug );
        System.out.println( debug );
    }

    //=======================================================================
    // NotifyPublishOperations
    //=======================================================================

   /**
    * The NotifyPublish interface supports an operation which allows a supplier of
    * Notifications to announce, or publish, the names of the types of events it will be
    * supplying, It is intended to be an abstract interface which is inherited by all
    * Notification Service consumer interfaces, and enables suppliers to inform consumers
    * supporting this interface of the types of events they intend to supply.
    */
    public void offer_change(EventType[] added, EventType[] removed)
    throws InvalidEventType
    {
        //
        // implementation pending
	  //

	  final String debug = "Received offer_change.";
        if( getLogger().isDebugEnabled() ) getLogger().debug( debug );
    }

    //=======================================================================
    // IdentifiableDomainConsumer
    //=======================================================================

   /**
    * Object identifier.
    */
    public int constant_random_id()
    {
        return 0;
    }
    
   /**
    * Determination if two identifiable objects have the same identifier.
    */
    public boolean is_identical( IdentifiableObject other_object )
    {
        try
        {
            return( other_object.constant_random_id() == constant_random_id() );
        } 
        catch (Exception e)
        {
            throw new org.omg.CORBA.INTERNAL(
		    "Adapter. Unable to complete 'is_identical' assessment");
        }
    }
    
    // ===========================================================
    // IdentifiableDomainObject
    // ===========================================================
    
   /**
    * The attribute domain qualifies the name space associated with the
    * object identity provided under the IdentifiableObject interface.
    * The AuthorityId type is a struct containing the declaration of a
    * naming authority (ISO, DNS, IDL, OTHER, DCE), and a string defining
    * the naming entity.
    */
    public org.omg.NamingAuthority.AuthorityId domain()
    {
        return authorityId;
    }
    
    
   /**
    * The same_domain operation is a convenience operation to compare two
    * IdentifiableDomainObject object instances for domain equivalence.
    * @return  boolean true if the supplied IdentifiableDomainObject reference
    * has the same domain identity as this instance.
    * @param  IdentifiableDomainObject comparable object
    */
    public boolean same_domain(org.omg.Session.IdentifiableDomainObject other_object)
    {
        try
        {
            AuthorityId remoteDomain = other_object.domain();
            if( remoteDomain.authority != domain().authority ) return false;
            return remoteDomain.naming_entity.equals( domain().naming_entity );
        } 
        catch (Exception e)
        {
		String error = "failed to complete same domain assessment";
		log.error( error, e );
            throw new org.omg.CORBA.INTERNAL( error );
        }
    }   

    private RegistrationAuthority getAuthorityFromConfiguration( Configuration config )
    throws ConfigurationException
    {
        if( config == null ) throw new NullPointerException(
          "Illegal null configuration argument.");

        final String str = config.getChild("domain").getAttribute( "authority", "DNS" );

        RegistrationAuthority authority = null;
        if( str.equals("OTHER"))
        {
            authority = RegistrationAuthority.OTHER;
        }
        else if( str.equals("ISO"))
        {
            authority = RegistrationAuthority.ISO;
        } 
        else if( str.equals("DNS"))
        {
            authority = RegistrationAuthority.DNS;
        } 
        else if( str.equals("IDL"))
        {
            authority = RegistrationAuthority.IDL;
        } 
        else if( str.equals("DCE"))
        {
            authority = RegistrationAuthority.DCE;
        } 
        else
        {
            final String error =
                "Bad domain attribute value '" + str + "' supplied for authority - " +
                "value must be one of 'ISO', 'DNS', 'IDL', 'DCE' or 'OTHER'. ";
            throw new ConfigurationException( error );
        }
        return authority;
    }

    //=======================================================================
    // Disposable
    //=======================================================================
        
    public void dispose()
    {
        if( getLogger().isDebugEnabled() ) getLogger().debug( "dispose");
    }


}
