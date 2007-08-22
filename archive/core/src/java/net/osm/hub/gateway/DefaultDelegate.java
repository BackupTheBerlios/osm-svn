

package net.osm.hub.gateway;

import org.apache.avalon.framework.logger.Logger;
import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.avalon.framework.activity.Disposable;
import org.apache.avalon.framework.activity.Initializable;
import org.apache.avalon.framework.context.Context;
import org.apache.avalon.framework.context.Contextualizable;
import org.apache.avalon.framework.context.ContextException;
import org.apache.avalon.framework.component.Composable;
import org.apache.avalon.framework.component.ComponentManager;
import org.apache.avalon.framework.component.ComponentException;

import org.omg.CORBA_2_3.ORB;
import org.omg.CosPersistentState.Connector;
import org.omg.CosPersistentState.Session;
import org.omg.CosPersistentState.StorageObject;

import net.osm.hub.gateway.ServantContext;
import net.osm.pss.PSSConnectorService;
import net.osm.pss.PSSSessionService;
import net.osm.realm.StandardPrincipal;
import net.osm.realm.PrincipalManager;
import net.osm.realm.PrincipalManagerHelper;
import net.osm.realm.PrincipalServerRequestInterceptor;
import net.osm.orb.ORBService;

/**
 */

public class DefaultDelegate extends AbstractLogEnabled
implements Contextualizable, Composable, Initializable
{

    //=======================================================================
    // static
    //=======================================================================
    
    private static final String nullObjectIdentifier = "thread level object identifier is null";


    //=======================================================================
    // state
    //=======================================================================

    private ServantContext context;

    protected ORB orb;
    protected Connector connector;
    protected Session session;
    private boolean initialized = false;
    private boolean disposed = false;


    //=======================================================================
    // Composable
    //=======================================================================

    /**
     * Pass the <code>ComponentManager</code> to the <code>Composable</code>.
     * The <code>Composable</code> implementation uses the supplied
     * <code>ComponentManager</code> to acquire the components it needs for
     * execution.
     * @param manager the <code>ComponentManager</code> for this delegate
     * @exception ComponentException
     */
    public void compose( ComponentManager manager )
    throws ComponentException
    {
        if( manager == null )
	  {
	      final String nullArgument = "null manager supplied under compose";
		throw new NullPointerException( nullArgument );
	  }

	  final String pre = "default composition";
        if( getLogger().isDebugEnabled() ) getLogger().debug( pre );

        if( initialized() )
        {
	      final String warning = "attempt to modify the component manager";
            if( getLogger().isWarnEnabled() ) getLogger().warn( warning );
            throw new IllegalStateException( warning );
        }

	  try
	  {
	      this.orb = ((ORBService)manager.lookup("ORB")).getOrb();
	      this.connector = ((PSSConnectorService)manager.lookup("PSS-CONNECTOR")).getPSSConnector();
	      this.session = ((PSSSessionService)manager.lookup("PSS-SESSION")).getPSSSession();
	  }
	  catch( Exception e )
	  {
		final String error = "manager failed to provide the PSS connector";
		throw new ComponentException( error, e);
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
	  try
	  {
		this.context = (ServantContext) context;
	  }
	  catch( Exception e)
	  {
		String error = "failed to establish the servant context";
		if( getLogger().isErrorEnabled() ) getLogger().error( error, e );
		throw new RuntimeException( error, e );
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
        if( getLogger().isDebugEnabled() ) getLogger().debug("default initialization");
        initialized = true;
    }

   /**
    * Returns true if the block has been initialized.
    */
    public boolean initialized()
    {
        return initialized;   
    }

   /**
    * Returns the current ORB.
    */
    protected ORB getOrb()
    {
        return orb;
    }
   
   /**
    * Returns the PSS connector.
    */
    protected Connector getConnector()
    {
        return connector;
    }
   
   /**
    * Returns the PSS session.
    */
    protected Session getSession()
    {
        return session;
    }

   /**
    * Returns the servant context.
    */
    protected ServantContext getContext()
    {
        return this.context;
    }

   /**
    * Returns true if the block has been disposed of.
    */
    public boolean disposed()
    {
        return disposed;
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
	  if( getLogger().isDebugEnabled() ) getLogger().debug("default dispose");
        this.context = null;
        this.session = null;
        this.connector = null;
        this.orb = null;
        disposed = true;
    }

    //=======================================================================
    // utilities
    //=======================================================================

   /**
    * Returns the current security principal.
    */
    public StandardPrincipal getCurrentPrincipal( ) throws Exception
    {
        return PrincipalServerRequestInterceptor.getCarrierPrincipal();
    }

   /**
    * Returns the storage object for this instance.
    */
    public StorageObject getStorageObject()
    {
	  if( context == null ) throw new IllegalStateException("context unavailable");
	  return context.getStorageObject();
    }
}
