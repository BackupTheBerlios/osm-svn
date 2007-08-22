/*
 * @(#)Server.java
 *
 * Copyright 2000 OSM S.A.R.L. All Rights Reserved.
 * 
 * This software is the proprietary information of OSM S.A.R.L.  
 * Use is subject to license terms.
 * 
 * @author  Stephen McConnell
 * @version 1.0 29/07/2000
 */

package net.osm.portal;

import java.io.File;
import java.io.OutputStream;
import java.util.Properties;
import java.util.Iterator;

import org.omg.CORBA.ORB;
import org.omg.CORBA.Object;
import org.omg.CORBA.Policy;
import org.omg.CORBA.ORBPackage.InvalidName;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.POAHelper;
import org.omg.PortableServer.Servant;
import org.omg.PortableServer.IdAssignmentPolicyValue;
import org.omg.PortableServer.LifespanPolicyValue;
import org.omg.PortableServer.POAManagerPackage.AdapterInactive;
import org.omg.PortableServer.POAPackage.WrongPolicy;
import org.omg.TimeBase.UtcT;
import org.omg.CosTime.TimeService;

import org.apache.avalon.framework.logger.Logger;
import org.apache.avalon.framework.component.Component;
import org.apache.avalon.framework.component.ComponentManager;
import org.apache.avalon.framework.component.ComponentException;
import org.apache.avalon.framework.component.Composable;
import org.apache.avalon.framework.activity.Disposable;
import org.apache.avalon.framework.activity.Initializable;
import org.apache.avalon.framework.activity.Startable;
import org.apache.avalon.framework.logger.LogEnabled;
import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.context.Context;
import org.apache.avalon.framework.context.Contextualizable;
import org.apache.avalon.framework.context.ContextException;
import org.apache.avalon.phoenix.Block;
import org.apache.avalon.phoenix.BlockContext;

import net.osm.discovery.Portal;
import net.osm.discovery.PortalHelper;
import net.osm.discovery.PortalPOATie;
import net.osm.discovery.*;
import net.osm.orb.ORBConfigurationHelper;
import net.osm.util.IOR;

/**
 *
 * @author <a href="mailto:mcconnell@osm.net">Stephen McConnell</a>
 */

public class PortalServer
implements Block, LogEnabled, Contextualizable, Configurable, Composable, Initializable, Startable, Disposable, PortalService
{
    
   /**
    * The configuration is an in memory representation of the XML assembly file used 
    * as a container of static configuration values.
    */

    private Configuration configuration;

   /**
    * The componentManager contains the reference to supporting services (where 
    * services are mapped under a role string - e.g. "net.osm.discovery.Directory").
    */
    private ComponentManager componentManager;

   /**
    * The default logger.
    */
    private Logger log;

   /**
    * The main server object request broker.
    */
    private ORB orb;

   /**
    * The root portable object adapter.
    */
    private POA root;

   /**
    * File path for the IOR (extracted from the configuration).
    */
    private String ior;

   /**
    * Port on which the service will listen for incomming invocations.  Can be 
    * modified through the configuration file.
    */
    private String port = "2062";

   /**
    * Directory object reference.
    */
    private Portal portal;

   /**
    * Thread used to run the orb.
    */
    private Thread thread;

   /**
    * Properties to be passed to the ORB initialization function.
    */
    private Properties props;

   /**
    * Object reference to the TimeService.
    */
    private TimeService time;

   /**
    * The gateway root portable object adapter.
    */
    private POA poa;

   /**
    * Application context
    */
    BlockContext context;

    //================================================================
    // Loggable
    //================================================================
    
   /**
    * Sets the logger to be used during configuration, conposition, initialization 
    * and execution phase.
    *
    * @param logger Logger to direct log entries to
    */ 
    public void enableLogging( final Logger logger )
    {
        log = logger;
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
    
    //================================================================
    // Configurable
    //================================================================
    
   /**
    * Configuration of the runtime environment based on a supplied Configuration arguments
    *
    * @param config Configuration representing an internalized model of the config.xml file.
    * @exception ConfigurationException if the supplied configuration is incomplete or badly formed.
    */
    public void configure( final Configuration config )
    throws ConfigurationException
    {
        
	  String name = "OSM Registration and Discovery Service.";
	  String banner = " \n" + name + "\nCopyright (C), 2001 OSM SARL.\nAll Rights Reserved.";
        log.info( banner );
        System.out.println( banner );
        
        if( null != configuration )
        {
            throw new ConfigurationException( "Configurations for block " + this +
            " already set" );
        }
        this.configuration = config;
                        
        // get the IOR path
        
        try
        {
            ior = this.configuration.getAttribute("ior");
            log.info("setting IOR path to " + ior );
        } catch (Exception e)
        {
            log.info("IOR publication disabled" );
        }
        
        // get the port
        
        try
        {
            port = this.configuration.getAttribute( "port", port );
            log.info("setting port to " + port );
        } catch (Exception e)
        {
            log.info("setting port to default " + port );
        }
        
        // ORB arguments
                
        try
        {
	      Configuration orbConfig = configuration.getChild("orb");
		File root = context.getBaseDirectory();
	      ORBConfigurationHelper helper = new ORBConfigurationHelper( orbConfig, root );
            props = helper.getProperties();
        } 
        catch (Exception e)
        {
            log.error("Failed to establish ORB properties.", e);
            throw new ConfigurationException( "Failed to establish ORB properties.", e);
        }

    }
    
    //================================================================
    // Composable
    // This is where we plug in supporting services.
    //================================================================
    
    /**
     * Pass the <code>ComponentManager</code> to the <code>composer</code>.
     * The <code>Composer</code> implementation should use the specified
     * <code>ComponentManager</code> to acquire the components it needs for
     * execution.
     *
     * @param manager The <code>ComponentManager</code> which this
     *                <code>Composer</code> uses.
     */
    public void compose( ComponentManager manager )
    throws ComponentException
    {
        componentManager = manager;
        time = (TimeService) componentManager.lookup("CLOCK");
    }

    //================================================================
    // Initializable
    //================================================================
    
   /**
    * Initialization is invoked by the framework following configuration, during which 
    * the underlying ORB is initialized.
    */
    public void initialize()
    throws Exception
    {
        
        log.debug("starting intialization");
        try
        {
            orb = ORB.init(new String[]{"-ORBPort=" + port }, props );
            PortalSingleton.init( orb );
            root = POAHelper.narrow(orb.resolve_initial_references("RootPOA"));
        }
        catch ( InvalidName e )
        {
            log.fatalError("cannot locate 'RootPOA' initial service", e );
            throw new Exception("cannot locate 'RootPOA' initial service");
        }
                        
        //
        // create the portal
        //

        PortalDelegate delegate = new PortalDelegate( );
        try
        {
            log.info("creating discovery server" );

	      POA portalPOA = root.create_POA(
		  "PORTAL",
		  root.the_POAManager(),
		  new Policy[] {
		     root.create_id_assignment_policy( IdAssignmentPolicyValue.USER_ID ),
		     root.create_lifespan_policy( LifespanPolicyValue.PERSISTENT )}
		);

		// create servant
	      Servant servant = new PortalPOATie( delegate );
            byte[] ID = "PORTAL".getBytes();
            portalPOA.activate_object_with_id( ID, servant );
            org.omg.CORBA.Object obj = portalPOA.id_to_reference(ID);
            portal = PortalHelper.narrow( obj );
                    
        } 
        catch (Exception e)
        {
            log.fatalError("failed to establish the portal servant", e );
            throw new Exception("failed to establish the portal servant");
        }

	  // configure and initialize the delegate

        try
	  {
            delegate.enableLogging( log.getChildLogger("PORTAL") );
            delegate.contextualize( context );
            delegate.configure( configuration );
            delegate.initialize();
	  }
	  catch( Exception e )
	  {
            log.fatalError("failed to initialize portal delegate", e );
            throw new Exception("failed to initialize portal delegate", e );
	  }
    }
    
    //================================================================
    // Startable
    //================================================================
    
   /**
    * The start operation is invoked by the framework following completion of the 
    * initialization phase, during which a new thread is created for the execution
    * of the ORB (and resulting startup of the POA tree). 
    */
    public void start()
    throws Exception
    {
        
        //
        // set portal object reference
        //
        
        if( ior != null )
        {
            log.info("creating external portal object reference" );
            try
            {
                IOR.writeIOR( orb, portal, ior );
                log.info( "published portal IOR to: " + ior );
            }
            catch (Exception e)
            {
                log.error("failed to create external IOR on " + ior, e );
                throw new Exception( "failed to create external IOR on " + ior );
            }
        }
        
        //
        // start ORB
        //
        
        thread = new Thread(
        new Runnable() {
            public void run()
            {
                log.info("starting ORB" );
                try
                {
                    root.the_POAManager().activate();
                    orb.run();
                }
                catch (Exception e)
                {
                    log.error("failed to activate the POA", e );
                    throw new RuntimeException( "failed to activate the POA" );
                }
            }
        }
        );
        thread.start();
    }
    
    /**
     * Stops the component.
     */
    public void stop()
    throws Exception
    {
        orb.shutdown( false );
    }

    //================================================================
    // Disposable
    //================================================================

   /**
    * Notification by the framework requesting disposal of this component, resulting
    * in the shutdown of the ORB.
    */ 
    
    public void dispose()
    {
	  // not tested
        log.info("closing portal" );
        orb.shutdown( false );
    }
    
    
    //================================================================
    // PortalService
    //================================================================
    
   /**
    * Returns the current ORB for the purpose of valuetype initialization and other 
    * ORB related operations required by servant implmentations.
    */
    
    public ORB getORB( )
    {
        return orb;
    }
        
   /**
    * Returns the generic POA against which object reference can be constructed.
    */
    
    public POA getPOA( )
    {
        return poa;
    }
    
   /**
    * Returns a reference to the TimeService supplied as a supporting services during the 
    * composition phase.
    */
    
    public TimeService getTimeService( )
    {
        return time;
    }
        
   /**
    * Returns a reference to the Portal.
    */
    
    public Portal getPortal( )
    {
        return portal;
    }

}
