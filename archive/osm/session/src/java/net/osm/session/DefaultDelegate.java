

package net.osm.session;

import org.apache.avalon.framework.logger.Logger;
import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.avalon.framework.activity.Disposable;
import org.apache.avalon.framework.activity.Initializable;
import org.apache.avalon.framework.context.Context;
import org.apache.avalon.framework.context.Contextualizable;
import org.apache.avalon.framework.context.ContextException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.Serviceable;
import org.apache.orb.ORBContext;
import org.apache.pss.ConnectorContext;
import org.apache.pss.SessionContext;
import org.apache.pss.Connector;
import org.apache.pss.Session;
import org.apache.pss.StorageContext;

import org.omg.CORBA_2_3.ORB;
import org.omg.PortableServer.POA;
import org.omg.CORBA.LocalObject;
import org.omg.CosPersistentState.StorageObject;

import net.osm.realm.StandardPrincipal;
import net.osm.realm.PrincipalManager;
import net.osm.realm.PrincipalManagerHelper;
import net.osm.realm.PrincipalServerRequestInterceptor;
import net.osm.realm.RealmSingleton;

/**
 * Abstract delegate default implementation that handles basic POA and 
 * ORB service establishemnt.
 */
public class DefaultDelegate extends AbstractLogEnabled
implements Contextualizable, Serviceable, Initializable
{

    //=======================================================================
    // state
    //=======================================================================

   /**
    * Application context.
    */
    private StorageContext m_context;

   /**
    * Storage object.
    */
    private StorageObject m_store;

   /**
    * Runtime ORB.
    */
    private ORB m_orb;

   /**
    * The POA in which this delegate is running.
    */
    private POA m_poa;

   /**
    * PSS connector.
    */
    private Connector connector;

   /**
    * PSS Session
    */
    private Session session;

   /**
    * Initialized flag.
    */
    private boolean initialized = false;

   /**
    * Disposed flag.
    */
    private boolean disposed = false;

   /**
    * Flag indicating if the storage object should be destroy or disposal.
    */
    private boolean m_destroy_on_dispose = false;

    private PrincipalManager m_principal_manager;


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
	  if( getLogger().isDebugEnabled() ) getLogger().debug("service");
	  try
	  {
            m_poa = (POA) manager.lookup("poa");
            m_orb = (ORB) manager.lookup("orb");
	  }
	  catch( Exception e )
	  {
		final String error = "failed to resolve connection context";
		throw new ServiceException( error, e);
	  }
    }

    //=======================================================================
    // Contextualizable
    //=======================================================================

   /**
    * Establish the servant context.
    * @param context the servant context
    */
    public void contextualize( Context context ) throws ContextException
    {
	  if( getLogger().isDebugEnabled() ) getLogger().debug("contextualize");
        try
        {
	      m_context = (StorageContext) context;
            m_store = m_context.getStorageObject();
        }
        catch( Throwable e )
        {
            final String error = "Unable to resolve StorageContext.";
            throw new ContextException( error, e );
        }
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
	  if( getLogger().isDebugEnabled() ) getLogger().debug("initialize");
        m_principal_manager = PrincipalManagerHelper.narrow( 
              m_orb.resolve_initial_references( "PrincipalManager" ) );
        initialized = true;
    }

    //=======================================================================
    // Helpers
    //=======================================================================

   /**
    * Returns the current ORB.
    */
    protected ORB getORB()
    {
        return m_orb;
    }

   /**
    * Returns the current ORB.
    */
    protected StorageObject getStorageObject()
    {
        return m_store;
    }

   /**
    * Returns true if the block has been initialized.
    */
    public boolean initialized()
    {
        return initialized;   
    }

   /**
    * Returns true if the block has been disposed of.
    */
    public boolean disposed()
    {
        return disposed;
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
	  if( getLogger().isDebugEnabled() ) getLogger().debug("terminate");
        m_destroy_on_dispose = true;
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
	  if( getLogger().isDebugEnabled() ) getLogger().debug("dispose");

        final byte[] pid = m_store.get_pid();
	  if( m_destroy_on_dispose ) synchronized( m_store )
        {
	      if( getLogger().isDebugEnabled() ) getLogger().debug("[DEF] destroying storage");
		try
		{
		    m_store.destroy_object();
		}
		catch( Exception e )
		{
		    if( getLogger().isErrorEnabled() ) getLogger().error(
                  "unexpected termination exception", e );
		}
        }

        try
        {
            m_poa.deactivate_object( pid );
        }
        catch( Throwable e )
        {
            final String warning = "Ignoring error on deactivation.";
            getLogger().warn( warning );
        }


        this.m_store = null;
        this.m_context = null;
        this.session = null;
        this.connector = null;
        this.m_orb = null;
        disposed = true;
    }

    //=======================================================================
    // utilities
    //=======================================================================

   /**
    * Returns a client principal invoking the operation.
    * @return StandardPrincipal the client principal
    */
    public final StandardPrincipal getPrincipal() throws Exception
    {
	  return m_principal_manager.getPrincipal();
    }

}
