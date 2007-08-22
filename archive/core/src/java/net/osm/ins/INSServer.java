/*
 * @(#)INSServer.java
 *
 * Copyright 2000 OSM SARL. All Rights Reserved.
 * 
 * This software is the proprietary information of OSM SARL.  
 * Use is subject to license terms.
 * 
 * @author  Stephen McConnell
 * @version 1.0 12/03/2001
 */

package net.osm.ins;

import java.io.File;
import java.io.OutputStream;
import java.util.Properties;
import java.util.Hashtable;
import java.util.Random;
import java.util.Iterator;

import org.apache.avalon.framework.logger.Logger;
import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.avalon.framework.component.Component;
import org.apache.avalon.framework.component.ComponentManager;
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
import org.apache.avalon.framework.component.ComponentManager;
import org.apache.avalon.framework.component.ComponentException;
import org.apache.avalon.framework.component.Composable;
import org.apache.avalon.phoenix.Block;
import org.apache.avalon.phoenix.BlockContext;

import org.omg.CORBA.ORB;
import org.omg.CORBA.ORBPackage.InvalidName;
import org.omg.CORBA.Policy;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.POAHelper;
import org.omg.PortableServer.IdAssignmentPolicyValue;
import org.omg.PortableServer.RequestProcessingPolicyValue;
import org.omg.PortableServer.ImplicitActivationPolicyValue;
import org.omg.PortableServer.LifespanPolicyValue;
import org.omg.PortableServer.IdUniquenessPolicyValue;
import org.omg.PortableServer.ServantRetentionPolicyValue;
import org.omg.CosPersistentState.Connector;
import org.omg.CosPersistentState.Session;
import org.omg.CosPersistentState.NotFound;
import org.omg.CosNotifyComm.StructuredPushSupplier;
import org.omg.CosNaming.NamingContext;
import org.omg.CosNaming.NamingContextHelper;

import net.osm.pss.PSSConnectorService;
import net.osm.pss.PSSSessionService;
import net.osm.pss.PersistanceHandler;

import net.osm.ins.pss.NCStorageHome;
import net.osm.ins.pss.LOStorageHome;
import net.osm.ins.pss.POStorageHome;
import net.osm.orb.ORBService;
import net.osm.util.IOR;


/**
 * Interoperable Naming Service server block.
 * @author <a href="mailto:mcconnell@osm.net">Stephen McConnell</a>
 */

public class INSServer extends AbstractLogEnabled
implements Block, LogEnabled, Composable, Contextualizable, Configurable, Initializable, Startable, Disposable, INSService
{
    private Configuration configuration;
    private ORB orb;
    private POA root;
    private POA poa;
    private String ior;
    private String port = "999";
    private Connector connector;
    private Session session;

   /**
    * PSS storage home for naming context storage objects.
    */
    private NCStorageHome contextHome;

   /**
    * PSS storage home for local named objects.
    */
    private LOStorageHome localHome;

   /**
    * PSS storage home for remote named objects.
    */
    private POStorageHome proxyHome;

   /**
    * Object reference to the root naming context.
    */
    private NamingContext rootNamingContext;

   /**
    * INS servant.
    */
    private NamingContextServant servant;

   /**
    * Application context
    */
    BlockContext context;

    
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
            connector = ((PSSConnectorService) manager.lookup("CONNECTOR")).getPSSConnector();
            session = ((PSSSessionService) manager.lookup("SESSION")).getPSSSession();
	  }
	  catch( Exception e )
	  {
		String error = "unexpected exception during composition";
		if( getLogger().isErrorEnabled() ) getLogger().error( error, e );
            throw new ComponentException( error, e );
	  }
    }

    //=======================================================================
    // Configurable
    //=======================================================================
    
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
                
        if( null != configuration )
        {
            throw new ConfigurationException( "Configurations for block " + this +
            " already set" );
        }
        this.configuration = config;
        
        
        // get the IOR path
        
        try
        {
            ior = this.configuration.getAttribute( "ior" );
            getLogger().info("setting IOR path to " + ior );
        } catch (Exception e)
        {
            getLogger().info("IOR publication disabled" );
        }
        
        // get the port
        
        try
        {
            port = this.configuration.getAttribute( "port", port );
            getLogger().info("setting port to " + port );
        } catch (Exception e)
        {
            getLogger().info("setting port to default " + port );
        }
    }

    //=======================================================================
    // Initializable
    //=======================================================================
    
   /**
    * Initialization is invoked by the framework following configuration, contrextualization
    * and service composition.
    */
    public void initialize()
    throws Exception
    {
        getLogger().debug("initialization");
        try
        {
            root = POAHelper.narrow( orb.resolve_initial_references("RootPOA") );
        }
        catch ( InvalidName e )
        {
            getLogger().fatalError("cannot locate 'RootPOA' initial service", e );
            throw new Exception("cannot locate 'RootPOA' initial service");
        }
        catch ( Exception e )
        {
            getLogger().fatalError("unexpected exception during ORB initialization", e );
            throw new Exception("unexpected exception during ORB initialization");
        }

        //
        // get storage home implementations
        //

        try
        {
            Configuration persistence = configuration.getChild("persistence");
	      PersistanceHandler.register( connector, persistence );

		// get the respective homes
            getLogger().debug("locating storage homes");
		contextHome = ( NCStorageHome ) 
			session.find_storage_home( "PSDL:osm.net/ins/pss/NCStorageHomeBase:1.0" );
		localHome = ( LOStorageHome ) 
			session.find_storage_home( "PSDL:osm.net/ins/pss/LOStorageHomeBase:1.0" );
		proxyHome = ( POStorageHome ) 
			session.find_storage_home( "PSDL:osm.net/ins/pss/POStorageHomeBase:1.0" );

            getLogger().debug("PSS initialization complete");
            
        }
        catch (Exception e)
        {
		String error = "failed to establish the PSS storage homes";
            getLogger().fatalError( error, e );
            throw new Exception( error );
        }
        
        //
        // create the INS POA
        //
        
        try
        {
            
		// create the poa
            getLogger().info("creating idc servant" );
            poa = root.create_POA(
            "INS",
            root.the_POAManager(),
            new Policy[]
            {
                root.create_id_assignment_policy( IdAssignmentPolicyValue.USER_ID ),
                root.create_request_processing_policy( RequestProcessingPolicyValue.USE_DEFAULT_SERVANT),
                root.create_id_uniqueness_policy( IdUniquenessPolicyValue.MULTIPLE_ID ),
                root.create_servant_retention_policy( ServantRetentionPolicyValue.NON_RETAIN ),
            }
            );

		// create the servant
            servant = new NamingContextServant( 
			orb, contextHome, localHome, proxyHome );
		servant.enableLogging( getLogger() );
            poa.set_servant( servant );
        }
        catch (Exception e)
        {
            String s = "failed to establish the IDC locator";
            getLogger().fatalError( s, e );
            throw new Exception( s );
        }

        //
        // instantiate the root naming context PSS instance
        //

        try
	  {

		// Establish the root node
		
		net.osm.ins.pss.NCStorage rootNamingContextStore = null;
		try 
            { 
		    rootNamingContextStore = contextHome.find_by_componentName( "root" );
		}
		catch ( org.omg.CosPersistentState.NotFound e )
            {
		    net.osm.ins.pss.NCStorageRef[] nc_children = 
				new net.osm.ins.pss.NCStorageRef[0];
		    net.osm.ins.pss.LOStorageRef[] no_children = 
				new net.osm.ins.pss.LOStorageRef[0];
		    net.osm.ins.pss.POStorageRef[] pnc_children = 
				new net.osm.ins.pss.POStorageRef[0];
		
		    rootNamingContextStore = contextHome.create( 
			"root", nc_children, no_children, pnc_children );
		}

		// configure the servant with the root naming context
		
		INSContext context = 
			new INSContextBase( rootNamingContextStore );
		servant.contextualize( context );

		//
		// create the object reference to the root of the naming context tree
		//

            byte[] ID = "NameService".getBytes();
		org.omg.CORBA.Object obj = null;
		obj = poa.create_reference_with_id( ID, "IDL:omg.org/CosNaming/NamingContext:1.0" );
		rootNamingContext = NamingContextHelper.narrow( obj );

		getLogger().debug("INS reference:\n" + rootNamingContext );
        }
        catch( Exception e )
        {
		String error = "failed to resolve the root naming context";
		getLogger().error( error, e );
		throw new Exception( error );
        }

	  String banner = "OSM INS Service.";
        getLogger().info( banner );
        System.out.println( banner );

    }
    
    //
    // Startable implementation
    //
    
   /**
    * The start operation is invoked by the framework following completion of the 
    * initialization phase, during which a new thread is created for the execution
    * of the ORB (and resulting startup of the POA tree). 
    */
    public void start()
    throws Exception
    {
        
        //
        // set object reference
        //
     
        if( ior != null )
        {
            getLogger().info("creating external object reference" );
            try
            {
                IOR.writeIOR( orb, rootNamingContext, ior );
                getLogger().info( "published IOR to: " + ior );
            }
            catch ( Exception e )
            {
		    String problem = "failed to create external IOR on " + ior;
                getLogger().error( problem, e );
                throw new Exception( problem );
            }
        }
    }

    /**
     * Stops the component.
     */
    public void stop()
    throws Exception
    {
    }

    
    //=====================================
    // Disposable implementation
    //=====================================

   /**
    * Disposal of this component.
    */ 
    public void dispose()
    {
        getLogger().info("disposal" );
    }
        
    //=====================================
    // Service implementation
    //=====================================
    
   /**
    * Returns a reference to an Interoperable Naming Service root NamingContext.
    */
    public NamingContext getRootNamingContext( )
    {
	  return rootNamingContext;
    }
}


