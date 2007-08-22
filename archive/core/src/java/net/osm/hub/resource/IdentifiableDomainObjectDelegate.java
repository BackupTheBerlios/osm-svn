

package net.osm.hub.resource;

import java.lang.reflect.Method;

import org.apache.avalon.framework.logger.Logger;
import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.avalon.framework.activity.Disposable;
import org.apache.avalon.framework.activity.Initializable;
import org.apache.avalon.framework.context.Context;
import org.apache.avalon.framework.context.Contextualizable;
import org.apache.avalon.framework.context.ContextException;
import org.apache.avalon.framework.logger.LogEnabled;
import org.apache.avalon.framework.component.Component;
import org.apache.avalon.framework.component.Composable;
import org.apache.avalon.framework.component.ComponentManager;
import org.apache.avalon.framework.component.ComponentException;

import org.omg.CORBA.ORB;
import org.omg.CORBA.NO_IMPLEMENT;
import org.omg.CosTime.TimeService;
import org.omg.CosTime.TimeUnavailable;
import org.omg.CosObjectIdentity.IdentifiableObject;
import org.omg.CosPersistentState.NotFound;
import org.omg.CosPersistentState.Connector;
import org.omg.CosPersistentState.Session;
import org.omg.NamingAuthority.AuthorityId;
import org.omg.NamingAuthority.RegistrationAuthority;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.portable.Delegate;
import org.omg.PortableServer.Servant;
import org.omg.Session.IdentifiableDomainObject;
import org.omg.Session.IdentifiableDomainObjectOperations;

import net.osm.hub.gateway.ServantContext;
import net.osm.hub.pss.AbstractResourceStorage;
import net.osm.hub.pss.DomainStorage;
import net.osm.hub.pss.DomainStorageHome;
import net.osm.hub.gateway.Vulnerable;
import net.osm.hub.gateway.CannotTerminate;
import net.osm.hub.gateway.POAService;
import net.osm.hub.gateway.Manager;
import net.osm.hub.gateway.DefaultDelegate;
import net.osm.orb.ORBService;
import net.osm.pss.PSSConnectorService;
import net.osm.pss.PSSSessionService;

/**
 * IdentifiableDomainObjectDelegate is an impementation of
 * the <code>org.omg.CosObjectIdentity.IdentifiableObject</code>
 * interface, and the <code>org.omg.Session.IdentifiableDomainObject</code>
 * interface.  As such, it provides delcaration of the domain that the
 * the object is managed under, and services supporting domain and
 * object identity comparison.
 */

public class IdentifiableDomainObjectDelegate extends DefaultDelegate
implements IdentifiableDomainObjectOperations, Vulnerable
{

    //=======================================================================
    // static
    //=======================================================================

   /**
    * Internal reference to the AuthorityId representing the domain of the server.
    */
    private static AuthorityId authority;

    //=======================================================================
    // state
    //=======================================================================

    private AbstractResourceStorage store;

   /**
    * the POA against which object references can be created.
    */
    protected POA poa; 

   /**
    * The component manager for this delegate.
    */
    private Manager manager;

   /**
    * The time service
    */
    private TimeService clock;

    //=======================================================================
    // Composable
    //=======================================================================

    /**
     * Pass the <code>ComponentManager</code> to the <code>Composable</code>.
     * The <code>Composable</code> implementation uses the supplied
     * <code>ComponentManager</code> to acquire the components it needs for
     * execution.
     * @param controller the <code>ComponentManager</code> for this delegate
     * @exception ComponentException
     */
    public void compose( ComponentManager controller )
    throws ComponentException
    {
	  super.compose( controller );
	  final String pre = "IDC composition";
        if( getLogger().isDebugEnabled() ) getLogger().debug( pre );
        this.manager = (Manager) controller;
	  this.poa = ((POAService)controller.lookup("POA")).getPoa();
	  this.clock = (TimeService)controller.lookup("CLOCK");
	  final String post = "IDC composition complete";
        if( getLogger().isDebugEnabled() ) getLogger().debug( post );
    }

   /**
    * Returns the time service.
    */
    protected TimeService getTimeService()
    {
        return clock;
    }

   /**
    * Returns the component manager for this instance.
    */
    protected Manager getManager()
    {
	  if( manager != null ) return manager;
	  final String error = "manager has not been initalized";
	  throw new NullPointerException( error );
    }

   /**
    * Returns the POA for this instance.
    */
    protected POA getPoa()
    {
        return poa;
    }
 
    //=======================================================================
    // Initializable
    //=======================================================================
    
   /**
    * Signal completion of contextualization phase and readiness to serve requests.
    * @exception Exception if the servant cannot complete normal initialization
    */
    public void initialize()
    throws Exception
    {       
	  super.initialize();
        if( getLogger().isDebugEnabled() ) getLogger().debug("IDC initialization");
        this.store = (AbstractResourceStorage) getContext().getStorageObject();
        if( getLogger().isDebugEnabled() ) getLogger().debug("IDC initialization complete");
    }

    //=======================================================================
    // Vulnerable
    //=======================================================================

   /**
    * Test is this instance can be terminated or not.
    * @return boolean true if the persistent identity of this 
    * instance can be destroyed.
    */
    public boolean expendable( )
    {
        return true;
    }

   /**
    * Destroys the persistent identity of this object. An 
    * implementation is responsible for disposing of references 
    * to external resources on completion of termiation actions. 
    * @exception CannotTerminate if the resource cannot be terminated
    */
    public void terminate( ) throws CannotTerminate
    {
	  synchronized( store )
        {
		if( getLogger().isDebugEnabled() ) getLogger().debug("[IDO] terminate");
		try
		{
		    store.destroy_object();
		}
		catch( Exception e )
		{
		    if( getLogger().isErrorEnabled() ) getLogger().error("unexpected termination exception", e );
		}
        }
    }

   /**
    * Dispose is invoked on completion and is used to clean up local 
    * state members.
    */
    public synchronized void dispose()
    {
	  if( getLogger().isDebugEnabled() ) getLogger().debug("[IDO] dispose");
        this.authority = null;
        this.store = null;
	  super.dispose();
    }

    //=======================================================================
    // IdentifiableDomainObject
    //=======================================================================

   /**
    * The attribute domain qualifies the name space associated with the
    * object identity provided under the IdentifiableObject interface.
    * The AuthorityId type is a struct containing the declaration of a
    * naming authority (ISO, DNS, IDL, OTHER, DCE), and a string defining
    * the naming entity.
    */
    public AuthorityId domain()
    {
        getLogger().info("domain");
        if( authority != null ) return authority;
	  DomainStorageHome h = null;
	  try
	  {
            h = getDomainHome();
	  }
	  catch( NotFound nf )
	  {
		throw new org.omg.CORBA.INTERNAL("unable to locate the domain storage home");
	  }
        synchronized( h )
        {
		try
		{
                DomainStorage domain = (DomainStorage) 
			h.find_by_short_pid( store.domain_short_pid() );
                RegistrationAuthority ra = RegistrationAuthority.from_int( 
			domain.authority() );
                authority = new AuthorityId( ra, domain.naming_entity() );
		    return authority;
		}
		catch( NotFound nfs )
            {
		    throw new org.omg.CORBA.INTERNAL("unable to locate the domain storage instance");
            }
        } 
    }
    
   /**
    * The same_domain operation is a convenience operation to compare two
    * IdentifiableDomainObject object instances for domain equivalence.
    * @return  boolean true if the supplied IdentifiableDomainObject reference
    * has the same domain identity as this instance.
    * @param  IdentifiableDomainObject comparable object
    */
    public boolean same_domain(IdentifiableDomainObject remote )
    {
        getLogger().info("same_domain");
        try
        {
            AuthorityId remoteDomain = (AuthorityId) remote.domain();
            if( remoteDomain.authority != domain().authority ) return false;
            return remoteDomain.naming_entity.equals( domain().naming_entity );
        }
        catch (Exception e)
        {
            String s = "failed to resolve remote domain";
            if( getLogger().isErrorEnabled() ) getLogger().error(s,e);
            throw new org.omg.CORBA.INTERNAL(s);
        }
    }

   /**
    * Object identifier.
    * @return int random identifier
    */
    public int constant_random_id()
    {
        getLogger().info("constant_random");
        touch( this.store );
        return this.store.random();
    }
    
   /**
    * Determination if two identifiable objects have the same identifier
    * @return boolean true if this object is identical to the supplied object
    */
    public boolean is_identical(IdentifiableObject other_object)
    {
        getLogger().info("is_identical");
        touch( this.store );
        try
        {
		return ( other_object.constant_random_id() == this.store.random() );
        } 
        catch (Exception e)
        {
            String s = "failed to resolve random value";
            if( getLogger().isErrorEnabled() ) getLogger().error(s,e);
            throw new org.omg.CORBA.INTERNAL(s);
        }
    }

   /**
    * Returns the storage home for DomainStorage types.
    * @osm.warning this service should be moved to the component manager
    */
    protected DomainStorageHome getDomainHome() throws NotFound
    {
        return ( DomainStorageHome ) getSession().find_storage_home( 
			"PSDL:osm.net/hub/pss/DomainStorageHomeBase:1.0" );
    }


    //=======================================================================
    // Utilities
    //=======================================================================
    
   /**
    * Update the access date in the supplied persistent store object.
    */
    public void touch( AbstractResourceStorage storage )
    {
        storage.access( now() );
    }
    
   /**
    * Update the access and modification dates in the supplied persistent
    * store object.
    */
    public void modify( AbstractResourceStorage  storage )
    {
        long t = now();
        storage.access( t );
        storage.modification( t );
    }
    
   /**
    * Static method to return current time from the time service.
    * Used the static <code>time</time> state member as the TimeServer
    * reference.
    */
    private long now()
    {
        long t = 0;
        try
        {
            t = getTimeService().universal_time().time();
        } catch (TimeUnavailable e)
        {
            System.err.println("time unavailable" );
        } finally // set default values
        {
            return t;
        }
    }

    //=======================================================================
    // Delegate (yuck)
    //=======================================================================
    
   /**
    * Convenience method that returns the instance of the ORB
    * currently associated with the Servant.
    * @param Self the servant.
    * @return ORB associated with the Servant.
    */
    org.omg.CORBA.ORB orb(Servant Self)
    {
        return getOrb();
    }

   /**
    * This allows the servant to obtain the object reference for
    * the target CORBA Object it is incarnating for that request.
    * @param Self the servant.
    * @return Object reference associated with the request.
    */
    org.omg.CORBA.Object this_object(Servant Self)
    {
        throw new org.omg.CORBA.NO_IMPLEMENT();
    }


   /**
    * The method _poa() is equivalent to
    * calling PortableServer::Current:get_POA.
    * @param Self the servant.
    * @return POA associated with the servant.
    */
    POA poa(Servant Self)
    {
        return this.poa; 
    }

   /**
    * The method _object_id() is equivalent
    * to calling PortableServer::Current::get_object_id.
    * @param Self the servant.
    * @return ObjectId associated with this servant.
    */
    byte[] object_id(Servant Self)
    {
        return store.get_pid();
    }

   /**
    * The default behavior of this function is to return the
    * root POA from the ORB instance associated with the servant.
    * @param Self the servant.
    * @return POA associated with the servant class.
    */
    POA default_POA(Servant Self)
    {
        return this.poa;
    }

   /**
    * This method checks to see if the specified repid is present
    * on the list returned by _all_interfaces() or is the
    * repository id for the generic CORBA Object.
    * @param Self the servant.
    * @param Repository_Id the repository_id to be checked in the
    *            repository list or against the id of generic CORBA
    *            object.
    * @return boolean indicating whether the specified repid is
    *         in the list or is same as that got generic CORBA
    *         object.
    */
    boolean is_a(Servant Self, String Repository_Id)
    {
        throw new org.omg.CORBA.NO_IMPLEMENT();
    }

   /**
    * This operation is used to check for the existence of the
    * Object.
    * @param Self the servant.
    * @return boolean true to indicate that object does not exist,
    *                 and false otherwise.
    */
    boolean non_existent(Servant Self)
    {
        if( !initialized() || disposed() ) return true;
        return false;
    }

   /**
    * This operation returns an object in the Interface Repository
    * which provides type information that may be useful to a program.
    * @param Self the servant.
    * @return type information corresponding to the object.
    */
    org.omg.CORBA.Object get_interface_def(Servant self)
    {
        throw new org.omg.CORBA.NO_IMPLEMENT();
    }

}
