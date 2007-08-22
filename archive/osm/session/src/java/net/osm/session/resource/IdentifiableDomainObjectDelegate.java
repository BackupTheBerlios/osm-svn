

package net.osm.session.resource;

import java.lang.reflect.Method;

import org.apache.avalon.framework.logger.Logger;
import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.avalon.framework.activity.Disposable;
import org.apache.avalon.framework.activity.Initializable;
import org.apache.avalon.framework.context.Context;
import org.apache.avalon.framework.context.Contextualizable;
import org.apache.avalon.framework.context.ContextException;
import org.apache.avalon.framework.logger.LogEnabled;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.Serviceable;

import org.apache.pss.Connector;
import org.apache.pss.Session;

import org.omg.CORBA.ORB;
import org.omg.CORBA.NO_IMPLEMENT;
import org.omg.CosTime.TimeService;
import org.omg.CosTime.TimeUnavailable;
import org.omg.CosObjectIdentity.IdentifiableObject;
import org.omg.CosPersistentState.NotFound;
import org.omg.NamingAuthority.AuthorityId;
import org.omg.NamingAuthority.RegistrationAuthority;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.portable.Delegate;
import org.omg.PortableServer.Servant;
import org.omg.Session.IdentifiableDomainObject;
import org.omg.Session.IdentifiableDomainObjectOperations;

import net.osm.domain.DomainStorage;
import net.osm.domain.DomainStorageHome;
import net.osm.domain.DomainService;
import net.osm.session.DefaultDelegate;
import net.osm.session.Vulnerable;

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

    private AbstractResourceStorage m_store;

   /**
    * The component manager for this delegate.
    */
    private ServiceManager m_manager;

   /**
    * The time service
    */
    private TimeService m_clock;

   /**
    * The domain service.
    */
    private DomainService m_domain_service;

   /**
    * Cached value of the AuthorityID.
    */
    private AuthorityId m_authority_id;

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
	  super.service( manager );
        m_manager = manager;
	  try
	  {
            m_clock = (TimeService) manager.lookup("time");
            m_domain_service = (DomainService) manager.lookup("domain");
	  }
	  catch( Exception e )
	  {
		final String error = "failed to aquire dependent services";
		throw new ServiceException( error, e);
	  }
    }

    //=======================================================================
    // utilities
    //=======================================================================

   /**
    * Returns the time service.
    */
    protected TimeService getTimeService()
    {
        return m_clock;
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
        m_store = (AbstractResourceStorage) getStorageObject();
    }

    //=======================================================================
    // Disposable
    //=======================================================================

   /**
    * Dispose is invoked on completion and is used to clean up local 
    * state members.
    */
    public synchronized void dispose()
    {
        this.authority = null;
        this.m_store = null;
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
        getLogger().debug("domain");
        touch( this.m_store );
        if( m_authority_id != null ) return m_authority_id;
        m_authority_id = m_domain_service.authorityFromPID( m_store.domain_short_pid() );
        return m_authority_id;
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
        getLogger().debug("same_domain");
        touch( this.m_store );
        try
        {
            AuthorityId remoteDomain = (AuthorityId) remote.domain();
            if( remoteDomain.authority != domain().authority ) return false;
            return remoteDomain.naming_entity.equals( domain().naming_entity );
        }
        catch (Exception e)
        {
            String s = "Unable to resolve remote domain.";
            throw new AbstractResourceRuntimeException( s, e );
        }
    }

   /**
    * Object identifier.
    * @return int random identifier
    */
    public int constant_random_id()
    {
        getLogger().debug("constant_random");
        touch( this.m_store );
        return this.m_store.random();
    }
    
   /**
    * Determination if two identifiable objects have the same identifier
    * @return boolean true if this object is identical to the supplied object
    */
    public boolean is_identical(IdentifiableObject other_object)
    {
        getLogger().debug("is_identical");
        touch( m_store );
        try
        {
		return ( other_object.constant_random_id() == m_store.random() );
        } 
        catch (Exception e)
        {
            String s = "Unable to resolve random value";
            throw new AbstractResourceRuntimeException( s, e );
        }
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
}
