/**
 */

package net.osm.orb;

import java.io.File;
import java.util.Properties;

import org.apache.avalon.framework.logger.Logger;
import org.apache.avalon.framework.logger.LogEnabled;
import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.avalon.framework.context.Context;
import org.apache.avalon.framework.context.Contextualizable;
import org.apache.avalon.framework.context.ContextException;
import org.apache.avalon.framework.activity.Disposable;
import org.apache.avalon.framework.activity.Initializable;
import org.apache.avalon.framework.activity.Startable;
import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.phoenix.Block;
import org.apache.avalon.phoenix.BlockContext;

import org.omg.CORBA_2_3.ORB;

/**
 * The <code>ORBServer</code> class is an Avalon block that encapsulates the configuration
 * initalization and startup of a portable ORB service.
 * 
 * <p><table border="1" cellpadding="3" cellspacing="0" width="100%">
 * <tr bgcolor="#ccccff">
 * <td colspan="2"><b><code>ORBServer</code>Lifecycle Phases</b></td>
 * <tr><td width="20%"></td><td><b>Description</b></td></tr>
 * <tr>
 * <td width="20%"><b>Contextualizable</b></td>
 * <td>
 * The <code>Context</code> value passed to the <code>ORBServer</code> during this phase
 * provides the runtime execution context including the root application directory 
 * from which a ORB configuration file can be resolved.</td></tr>
 * <tr>
 * <td width="20%"><b>Configurable</b></td>
 * <td>
 * The configuration phase handles the internalization of a static configuration data 
 * including ORB bootstrap properties and ORB specific execution properties.
 * </td></tr>
 * <tr><td width="20%"><b>Initalizable</b></td>
 * <td>
 * The initialization phases handles the initialization of the ORB.
 * </td></tr>
 * <tr><td width="20%"><b>Startable</b></td>
 * <td>
 * Handles startup and shutdown of the ORB. 
 * </td></tr>
 * <tr><td width="20%"><b>Disposable</b></td>
 * <td>
 * Handles the release of resources consumed by the ORB.
 * </td></tr>
 * </table>
 *
 * @author <a href="mailto:mcconnell@osm.net">Stephen McConnell</a>
 */

public class ORBServer extends AbstractLogEnabled
implements Block, Contextualizable, Configurable, Initializable, Startable, Disposable, ORBService
{

    //=================================================================
    // state
    //=================================================================

   /**
    * The configuration is an in memory representation of the XML assembly file used 
    * as a container of static configuration values.
    */
    private Configuration configuration;

   /**
    * The main server object request broker established by this server and 
    * made available under <code>getOrb</code> method.
    */
    private ORB orb;

   /**
    * Thread used to run the orb.
    */
    private Thread thread;

   /**
    * Properties to be passed to the ORB initialization function.
    */
    private Properties props;

   /**
    * Base directory for the application.
    */
    File baseDirectory;

   /**
    * Application context
    */
    BlockContext context;

    
    //=================================================================
    // Contextualizable
    //=================================================================

    public void contextualize( Context context ) throws ContextException
    {
        if( getLogger().isDebugEnabled() ) getLogger().debug( "contextualize" );
	  if( context instanceof BlockContext ) 
	  {
		this.context = (BlockContext) context;
	  }
	  else
	  {
		final String error = "supplied context does not implement BlockContext";
	      throw new ContextException( error );
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
        if( getLogger().isDebugEnabled() ) getLogger().debug( "configure" );
        if( null != configuration ) throw new ConfigurationException( 
		"Configurations for block " + this + " already set" );
        this.configuration = config;
    }

    //=================================================================
    // Initializable
    //=================================================================
    
   /**
    * Initialization is invoked by the framework following configuration, during which 
    * the ORB is initialized.
    */
    public void initialize()
    throws Exception
    {
        final String pre = "ORB intialization";

        // Collection of the ORB initalization arguments.
        //
                
        try
        {
		File root = context.getBaseDirectory();
	      ORBConfigurationHelper helper = new ORBConfigurationHelper( configuration, root );
            props = helper.getProperties();
        }
        catch (Exception e)
        {
	      final String error = "Failed to establish ORB properties in ";
            throw new ConfigurationException( error + context.getBaseDirectory(), e);
        }

        if( getLogger().isDebugEnabled() ) getLogger().debug( pre );
        try
        {
            orb = (ORB) ORB.init( new String[0], props );
        }
        catch ( Throwable e )
        {
		final String error = "ORB initialization failure under ";
            throw new Exception( error + context.getBaseDirectory(), e );
        }

	  final String banner = "OSM ORB Service";
        if( getLogger().isInfoEnabled() ) getLogger().info( banner );
        System.out.println( banner );
    }
    
    //=================================================================
    // Startable
    //=================================================================
    
   /**
    * The start operation is invoked by a manager following completion of the 
    * initialization phase, during which a new thread is created for the execution
    * of the ORB.
    */
    public void start()
    throws Exception
    {
        final String status = "start";
        thread = new Thread(
        new Runnable() {
            public void run()
            {
                if( getLogger().isDebugEnabled() ) getLogger().debug( status );
                try
                {
                    orb.run();
                }
                catch (Exception e)
                {
			  final String error = "unexpected exception raised by the ORB";
                    if( getLogger().isErrorEnabled() ) getLogger().error( error, e );
                    throw new RuntimeException( error, e );
                }
            }
        }
        );
        thread.start();
	  final String debug = "statup complete";
        if( getLogger().isDebugEnabled() ) getLogger().debug( debug );
    }
    
    /**
     * Stops the component.
     */
    public void stop()
    throws Exception
    {
        if( getLogger().isDebugEnabled() ) getLogger().debug( "stop" );
	  try
	  {
            orb.shutdown( true );
	  }
	  catch( Throwable e )
	  {
	      final String warning = "Internal error while shutting down the ORB.";
            if( getLogger().isWarnEnabled() ) getLogger().warn( warning, e );
        }
    }

    //=================================================================
    // Disposable
    //=================================================================

   /**
    * Notification by the framework requesting disposal of this component.
    */ 
    public synchronized void dispose()
    {
        if( getLogger().isDebugEnabled() ) getLogger().debug( "disposal" );
	  try
	  {
	      orb.destroy();
        }
        catch( org.omg.CORBA.NO_IMPLEMENT e )
	  {
            // ignore
        }
        catch( Throwable e )
	  {
	      final String warning = "Internal error while disposing of ORB related resources.";
		if( getLogger().isWarnEnabled() ) getLogger().warn( warning, e );
        }
        this.orb = null;
        this.thread = null;
        this.context = null;
        this.configuration = null;
        this.props = null;
    }

    //=================================================================
    // ORBService
    //=================================================================

   /**
    * Returns the current ORB for the purpose of valuetype initialization and other 
    * ORB related operations.
    */
    public ORB getOrb( )
    {
        return orb;
    }
}
