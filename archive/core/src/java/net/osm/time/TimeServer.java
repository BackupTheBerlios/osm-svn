/*
 * @(#)TimeServer.java
 *
 * Copyright 2000 OSM SARL. All Rights Reserved.
 * 
 * This software is the proprietary information of OSM SARL.  
 * Use is subject to license terms.
 * 
 * @author  Stephen McConnell
 * @version 1.0 26/03/2001
 */

package net.osm.time;

import java.io.File;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.Properties;

import org.omg.CORBA.ORB;
import org.omg.CORBA.ORBPackage.InvalidName;
import org.omg.CORBA.Policy;
import org.omg.CORBA.LocalObject;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.POAHelper;
import org.omg.PortableServer.ImplicitActivationPolicyValue;
import org.omg.PortableServer.LifespanPolicyValue;
import org.omg.PortableServer.IdUniquenessPolicyValue;
import org.omg.CosTime.TimeServiceHelper;

import org.apache.avalon.framework.logger.Logger;
import org.apache.avalon.framework.context.Context;
import org.apache.avalon.framework.context.Contextualizable;
import org.apache.avalon.framework.context.ContextException;
import org.apache.avalon.framework.component.Component;
import org.apache.avalon.framework.component.ComponentManager;
import org.apache.avalon.framework.activity.Disposable;
import org.apache.avalon.framework.activity.Initializable;
import org.apache.avalon.framework.activity.Startable;
import org.apache.avalon.framework.logger.LogEnabled;
import org.apache.avalon.phoenix.BlockContext;
import org.apache.avalon.phoenix.Block;

import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;

import net.osm.orb.ORBConfigurationHelper;
import net.osm.util.IOR;

/**
 * This is an <code>TimeBlock</code> that provides time services compliant
 * with the OMG CosTime interface specification.
 *
 * @author <a href="mailto:mcconnell@osm.net">Stephen McConnell</a>
 */

public class TimeServer extends LocalObject
implements Block, LogEnabled, Configurable, Contextualizable, Initializable, Startable, Disposable, TimeService
{
    
    private static final String ROOT_POA = "RootPOA";
    protected Configuration configuration;
    protected Logger log;
    protected ORB orb;
    protected POA root;
    private String timeIOR;
    private String port = "2060";
    private int inaccuracy = 10000;
    private org.omg.CosTime.TimeService time;
    private static Thread thread;
    private Properties props;

   /**
    * Application context
    */
    BlockContext context;
    
    //=======================================================================
    // Loggable
    //=======================================================================
    
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

    //=======================================================================
    // Configurable
    //=======================================================================
    
    public void configure( final Configuration config )
    throws ConfigurationException
    {
	  if( log == null ) throw new ConfigurationException(
		"Attempt to configure prior to initializing logger.");

        if( null != configuration )
        {
            throw new ConfigurationException( "Configurations for block " + this +
            " already set" );
        }
        this.configuration = config;
                        
        // ORB arguments
                
        try
        {
	      Configuration orbConfig = this.configuration.getChild("orb");
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
    
    
    //=======================================================================
    // Initializable
    //=======================================================================
    
    public void initialize()
    throws Exception
    {       
        //
        // initialize configuration
        //
        
        try
        {
            port = this.configuration.getAttribute( "port", port );
            log.info("setting port to " + port );
		props.setProperty("iiop.port", port );
        } catch (Exception e)
        {
            log.info("setting port to default " + port );
        }
        
        try
        {
            inaccuracy = this.configuration.getChild("profile").getAttributeAsInteger( 
		  "inaccuracy", inaccuracy );
            log.info("setting inaccuracy to " + inaccuracy );
        } catch (Exception e)
        {
            log.info("setting inaccuracy to default " + inaccuracy );
        }
        
        try
        {
            timeIOR = this.configuration.getAttribute( "ior" );
            log.info("setting IOR path to " + timeIOR );
        } catch (Exception e)
        {
            log.info("IOR publication disabled" );
        }
        
        //
        // create the time server runtime POA
        //
        
        log.info("locating root POA" );
        try
        {
            orb = ORB.init( new String[0], props );
            root = POAHelper.narrow(orb.resolve_initial_references(ROOT_POA));
        }
        catch ( InvalidName e )
        {
            log.fatalError("cannot locate 'RootPOA' initial service", e );
            throw new Exception("cannot locate 'RootPOA' initial service");
        }
        catch ( Exception e )
        {
            log.fatalError("unexpected exception while resolving root POA", e );
            throw new Exception("unexpected exception while resolving root POA");
        }
        
        log.info("creating time service POA" );
        try
        {
            POA timePOA = root.create_POA
            (
            "TimeServicePOA", // adapter name
            root.the_POAManager(), // manager
            new Policy[]
            { // policy set
                root.create_implicit_activation_policy(
                ImplicitActivationPolicyValue.IMPLICIT_ACTIVATION),
                root.create_lifespan_policy( LifespanPolicyValue.PERSISTENT ),
                root.create_id_uniqueness_policy( IdUniquenessPolicyValue.UNIQUE_ID)
            }
            );
            
            org.omg.CosTime.TimeServicePOA servant =
            new org.openorb.time.impl.TimeServiceImpl( orb, inaccuracy );
            byte[] servantID = timePOA.activate_object(servant);
            org.omg.CORBA.Object object = null;
            object = timePOA.id_to_reference(servantID);
            time = TimeServiceHelper.narrow( object );
            log.info("POA established" );
        }
        catch( Exception e)
        {
            log.fatalError("cannot instantiate POA", e );
            throw new Exception("cannot instantiate POA");
        }
        
	  String banner = "OSM Time Service.";
        log.info( banner );
        System.out.println( banner );

    }
    
    //=======================================================================
    // Startable
    //=======================================================================
    
    /**
     * Start the TimeServer.
     */
    public void start()
    throws Exception
    {
        //
        // set object reference
        //
        
        if( timeIOR != null )
        {
            log.info("creating external object reference" );
            try
            {
                IOR.writeIOR( orb, time, timeIOR );
                log.info( "published IOR to: " + timeIOR );
            }
            catch (Exception e)
            {
                log.error("failed to create external IOR on " + timeIOR, e );
                throw new Exception( "failed to create external IOR on " + timeIOR );
            }
        }
        
        thread = new Thread(
        new Runnable() {
            public void run()
            {
                log.info("starting time server" );
                try
                {
                    root.the_POAManager().activate();
                    orb.run();
                    log.info("root POA is activated" );
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

    
    //=======================================================================
    // Disposable
    //=======================================================================
    
    public void dispose()
    {
        log.debug("dispose" );
    }
    
    //=======================================================================
    // TimeService
    //=======================================================================
    
   /**
    * The universal_time operation returns the current time and an estimate of inaccuracy in
    * a UTO. It raises TimeUnavailable exceptions to indicate failure of an underlying time
    * provider. The time returned in the UTO by this operation is not guaranteed to be secure
    * or trusted. If any time is available at all, that time is returned by this operation.
    */

    public org.omg.CosTime.UTO universal_time()
    throws org.omg.CosTime.TimeUnavailable
    {
        return time.universal_time();
    }
    
   /**
    * The secure_universal_time operation returns the current time in a UTO only if the
    * time can be guaranteed to have been obtained securely. In order to make such a
    * guarantee, the underlying Time Service must meet the criteria to be followed for
    * secure time, presented in Appendix A, Implementation Guidelines. If there is any
    * uncertainty at all about meeting any aspect of these criteria, then this operation must
    * return the TimeUnavailable exception. Thus, time obtained through this operation can
    * always be trusted.
    */

    public org.omg.CosTime.UTO secure_universal_time()
    throws org.omg.CosTime.TimeUnavailable
    {
        log.info("secure_universal_time");
        return time.secure_universal_time();
    }
    
   /**
    * The new_universal_time operation is used for constructing a new UTO. The
    * parameters passed in are the time of type TimeT and inaccuracy of type InaccuracyT.
    * This is the only way to create a UTO with an arbitrary time from its components. This
    * is expected to be used for building UTOs that can be passed as the various time
    * arguments to the Timer Event Service, for example. CORBA::BAD_PARAM is
    * raised in the case of an out-of-range parameter value for inaccuracy.
    */

    public org.omg.CosTime.UTO new_universal_time(long t, long inac, short tdf)
    {
        log.info("new_universal_time");
        return time.new_universal_time( t, inac, tdf );
    }
    
   /**
    * The uto_from_utc operation is used to create a UTO given a time in the UtcT form.
    * This has a single in parameter UTC, which contains a time together with inaccuracy
    * and tdf. The UTO returned is initialized with the values from the UTC parameter. This
    * operation is used to convert a UTC received over the wire into a UTO.
    */

    public org.omg.CosTime.UTO uto_from_utc(org.omg.TimeBase.UtcT utc)
    {
        log.info("uto_from_utc");
        return time.uto_from_utc( utc );
    }
    
   /**
    * The new_interval operation is used to construct a new TIO. The parameters are lower
    * and upper, both of type TimeT, holding the lower and upper bounds of the interval. If
    * the value of the lower parameter is greater than the value of the upper parameter, then
    * a CORBA::BAD_PARAM exception is raised.
    */

    public org.omg.CosTime.TIO new_interval(long lower, long upper)
    {
        log.info("new_interval");
        return time.new_interval( lower, upper );
    }
    
    //
    // Internal Utilities
    // ==================
    
    private class ConfigurationProperties extends Properties
    {
        
        public ConfigurationProperties( Logger log, Configuration config, String element, String nameAttribute, String valueAttribute )
        {
            super();
            Configuration[] children = config.getChildren( element );
            log.debug("creating configuration property set from the '" + element + "' element");
		for( int i = 0; i< children.length; i++ )
            {
		    String name = "";
		    String value = "";
                try
                {
                    Configuration c = children[i];
                    name = c.getAttribute( nameAttribute );
                    value = c.getAttribute( valueAttribute );
                    setProperty( name, value );
                }
                catch (ConfigurationException e)
                {
                    log.error("bad " + element + " declaration, ignoring '" + name + "', '" + value + "'", e);
                }
            }
        }
    }

}
