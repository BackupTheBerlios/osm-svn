/**
 */

package net.osm.pss;

import java.util.Iterator;
import java.io.File;
import java.io.OutputStream;
import java.util.Properties;
import java.util.Hashtable;
import java.util.Random;
import java.security.Principal;
import java.security.cert.CertPath;

import org.apache.avalon.framework.activity.Startable;
import org.apache.avalon.framework.activity.Disposable;
import org.apache.avalon.framework.activity.Initializable;
import org.apache.avalon.framework.component.Component;
import org.apache.avalon.framework.component.ComponentManager;
import org.apache.avalon.framework.component.ComponentException;
import org.apache.avalon.framework.component.Composable;
import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.context.Context;
import org.apache.avalon.framework.context.Contextualizable;
import org.apache.avalon.framework.context.ContextException;
import org.apache.avalon.framework.logger.Logger;
import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.avalon.phoenix.Block;
import org.apache.avalon.phoenix.BlockContext;

import org.omg.CORBA_2_3.ORB;
import org.omg.CosPersistentState.Connector;
import org.omg.CosPersistentState.ConnectorHelper;
import org.omg.CosPersistentState.Session;
import org.omg.CosPersistentState.Parameter;
import org.omg.CosPersistentState.READ_WRITE;

import net.osm.orb.ORBService;

/**
 * PSS Block providing connector establishment and persistent session services.
 * @author <a href="mailto:mcconnell@osm.net">Stephen McConnell</a>
 */

public class PSSServer extends AbstractLogEnabled
implements Block, Composable, Contextualizable, Configurable, Initializable, Startable, Disposable, PSSConnectorService, PSSSessionService
{

   /**
    * Static server configuration.
    */
    private Configuration configuration;

   /**
    * Block context.
    */
    private BlockContext context;

   /**
    * The object request broker.
    */
    private ORB orb;

   /**
    * PSS connector.
    */
    Connector connector = null;

   /**
    * PSS session
    */
    Session session;
    
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
        if( orb != null ) throw new ComponentException(
	    "orb has already been initialized");
	  orb = ((ORBService)manager.lookup("ORB")).getOrb();
    }

    //=================================================================
    // Contextualizable
    //=================================================================

    public void contextualize( Context context ) throws ContextException
    {
	  if( context instanceof BlockContext ) 
	  {
	      this.context = (BlockContext) context;
	  }
	  else
	  {
		throw new ContextException("Supplied context does not implement BlockContext.");
	  }
    }

    //==========================================================================
    // Configurable
    //==========================================================================
    
   /**
    * Configuration of the runtime environment based on a supplied Configuration arguments
    * which contains the general arguments for ORB initalization, PSS subsystem initialization, 
    * PSDL type to class mappings, preferences and debug information.
    *
    * @param config Configuration representing an internalized model of the assembly.xml file.
    * @exception ConfigurationException if the supplied configuration is incomplete or badly formed.
    */
    public void configure( final Configuration config )
    throws ConfigurationException
    {
        if( null != configuration ) throw new ConfigurationException( 
          "configuration already set" );
        this.configuration = config;
    }

    //=================================================================
    // Initializable
    //=================================================================
    
   /**
    * Initialization is invoked by the framework following configuration, during which 
    * the PSS session is created and configured.
    */
    public void initialize()
    throws Exception
    {
        if( null != session ) throw new ConfigurationException( 
          "PSS service cannot be reinitialized" );
	  try
	  {
            if( getLogger().isDebugEnabled() ) getLogger().debug("intialization");
            connector = createPSSConnector( configuration.getChild("connector") );
		session = createSession( connector, configuration );
		PersistanceHandler.register( connector, configuration.getChild("persistence"));
        }
        catch (Throwable e)
        {
		final String error = "failed to establish PSS session";
            throw new Exception( error, e );
        }
	  finally
	  {
            if( getLogger().isDebugEnabled() ) getLogger().debug("initialization complete");   
	      final String banner = "OSM PSS Service";
            if( getLogger().isInfoEnabled() ) getLogger().info( banner );
            System.out.println( banner );
        }        
    } 

   /**
    * Get the PSS Connector from the PSS Connector Registry.  The
    * connector returned from the registry is used to publish the
    * the mappings between PSDL types and implementation classes.
    * @param config the connector configuration
    * @return Connector
    * @exception Exception 
    */
    private Connector createPSSConnector( Configuration config )
    throws Exception
    {
        if( orb == null ) throw new NullPointerException("null ORB argument");
        if( config == null ) throw new NullPointerException("null Configuration argument");
        if( !(config.getName().equals("connector" ))) throw new Exception(
          "supplied configuration is not a 'connector' element" );

	  String ID = config.getAttribute( "value", "PSS:OSM:memory" );
        try
        {
            return ConnectorHelper.narrow( 
              orb.resolve_initial_references( ID ) );
        }
        catch( Exception e )
        {
            final String error = "unable to resolve PSS connector for '" + ID;
		throw new Exception( error, e );
        }
    }
    

   /**
    * Creates a PSS Session using a supplied connector and parameters 
    * declared under a configuration instance.
    * @param orb the ORB to use to resolve inital references
    * @param connector the PSS connector
    * @param config the configuration containing PSS parameters and an 
    *  optional catalog declaration
    * @return Session
    * @exception Exception 
    */
    private Session createSession( Connector connector, Configuration config )
    throws Exception
    {
        if( orb == null ) throw new NullPointerException("null orb argument");
        if( connector == null ) throw new NullPointerException("null connector argument");
        if( config == null ) throw new NullPointerException("null Configuration argument");

	  try
	  {
	      Configuration[] params = config.getChildren("parameter");
		Parameter[] parameters = new Parameter[ params.length ];
		for( int i=0; i<params.length; i++ )
		{
		    final Parameter p = new Parameter();
		    p.name = params[i].getAttribute("name");
		    p.val = orb.create_any();
                p.val.insert_string( parseValue( params[i].getAttribute("value") ));
		    parameters[i] = p;
		}

		session = connector.create_basic_session(
                  org.omg.CosPersistentState.READ_WRITE.value, parameters );
            return session;
	  }
	  catch( Throwable e )
	  {
		final String error = "failed to create a PSS session";
		throw new Exception( error, e );
	  }
    }

    //=================================================================
    // Startable
    //=================================================================
    
   /**
    * The start operation is invoked by a manager following completion of the 
    * initialization phase.  The current implementation is empty.
    */
    public void start()
    throws Exception
    {
    }
    
    /**
     * The stop operation is invoked by a manager causing closure of 
     * the persistent session.
     */
    public void stop()
    throws Exception
    {
	  final String debug = "closing PSS session";
	  if( getLogger().isDebugEnabled() ) getLogger().debug( debug );
        try
        {
            session.flush();
            session.close();
        }
        catch( Throwable e )
        {
	      final String error = "Internal error while stopping PSS";
	      if( getLogger().isErrorEnabled() ) getLogger().error( error, e );
        }
    }

    //=======================================================================
    // Disposable
    //=======================================================================

   /**
    * Notification by the framework requesting disposal of this component, resulting
    * in the shutdown of the PSS service.
    */
    public synchronized void dispose()
    {
        getLogger().info("PSS diposal" );
	  this.configuration = null;
	  this.orb = null;
        this.connector = null;
        this.session = null;
    }

    //=======================================================================
    // PSSConnectorService
    //=======================================================================

   /**
    * Returns the PSS connector
    * @return Connector PSS storage connector
    */
    public Connector getPSSConnector( )
    {
        return connector;
    }

    //=======================================================================
    // PSSSessionService
    //=======================================================================

   /**
    * Returns the PSS session
    * @return Session PSS session
    */
    public Session getPSSSession( )
    {
        return session;
    }

    //=======================================================================
    // internals
    //=======================================================================

    /**
    * Parse a string value and replace occurances of "${<name>}" with the 
    * corresponding system property value.  If a system property is undefined, 
    * throw a runtime exception with context message.
    */
    public String parseValue( String input )
    {
        int i = input.indexOf("${");
        if( i > -1 ) 
        {
            int j = input.indexOf("}",i);
            String key = input.substring( i+2, j );
            String value = System.getProperty( key );
            return parseValue( input.substring( 0, i ) + value + input.substring( j+1, input.length() ));
        }
        else
        {
            return input;
        }
    }
}
