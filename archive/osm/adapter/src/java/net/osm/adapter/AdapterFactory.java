package net.osm.adapter;

import java.io.Serializable;

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
import org.apache.avalon.framework.service.DefaultServiceManager;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.Serviceable;
import org.apache.orb.util.LifecycleHelper;

import org.omg.CORBA_2_3.portable.InputStream;
import org.omg.CORBA.portable.ValueFactory;

/**
 * Abstract base class for an adapter factory.
 */
public abstract class AdapterFactory 
implements ValueFactory, LogEnabled, Contextualizable, Configurable, Serviceable, Initializable
{
    //============================================================
    // state
    //============================================================

   /**
    * The logging channel supplied by the ORB.
    */
    private Logger m_logger;

   /**
    * The aplication context supplied by the ORB.
    */
    private Context m_context;

   /**
    * The configuration supplied by the ORB.
    */
    private Configuration m_config;

   /**
    * The manager supplied by the ORB.
    */
    private ServiceManager m_manager;

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
      
    //=================================================================
    // Contextualizable
    //=================================================================

   /**
    * Set the block context.
    * @param context the block context
    * @exception ContextException if the block cannot be narrowed to BlockContext
    */
    public void contextualize( Context context ) throws ContextException
    {
        //if( getLogger().isDebugEnabled() ) getLogger().debug( "contextualize" );
        m_context = context;
    }

    
    //=======================================================================
    // Configurable
    //=======================================================================
    
    public void configure( final Configuration config )
    throws ConfigurationException
    {
	  //if( getLogger().isDebugEnabled() ) getLogger().debug( "configuration" );
        m_config = config;
    }

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
	  //if( getLogger().isDebugEnabled() ) getLogger().debug("service");
        m_manager = manager;
    }

    //============================================================
    // Initializable
    //============================================================

   /**
    * Initalization of the factory.
    * @exception Exception if an initalization related error occurs
    */
    public void initialize() throws Exception
    {
	  //if( getLogger().isDebugEnabled() ) getLogger().debug("initialize");
        if( m_logger == null ) throw new NullPointerException(
          "Factory has not been log enabled.");
        if( m_context == null ) throw new NullPointerException(
          "Factory has not been contextualized.");
        if( m_config == null ) throw new NullPointerException(
          "Factory has not been configured.");
        if( m_manager == null ) throw new NullPointerException(
          "Factory has not been serviced.");
    }
    
    //============================================================
    // ValueFactory
    //============================================================
  
    public Serializable read_value( InputStream input ) 
    {
        Serializable adapter = null;
        try
        {
            adapter = (Serializable) this.getClass().newInstance();
        }
        catch( Throwable e )
        {
            final String error = "Adapter instantiation error.";
            throw new AdapterRuntimeException( error, e );
        }

        try
        {
            input.read_value( adapter );
        }
        catch( Throwable e )
        {
            final String error = "Adapter error during internalization.";
            throw new AdapterRuntimeException( error, e );
        }

        try
        {
            LifecycleHelper.pipeline( 
              adapter, 
              getLogger().getChildLogger( "" + System.identityHashCode( adapter ) ), 
              m_context, m_config, m_manager );
        }
        catch( Throwable e )
        {
            final String error = "Adapter error during pipeline processing.";
            throw new AdapterRuntimeException( error, e );
        }

        return adapter;
    }
}

