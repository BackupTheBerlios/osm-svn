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

package net.osm.discovery;

import java.io.File;
import java.io.OutputStream;
import java.util.Properties;
import java.util.Iterator;

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
import org.omg.CosTime.TimeServiceHelper;

import org.apache.avalon.framework.logger.Logger;
import org.apache.avalon.framework.component.Component;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.Serviceable;
import org.apache.avalon.framework.activity.Disposable;
import org.apache.avalon.framework.activity.Initializable;
import org.apache.avalon.framework.activity.Startable;
import org.apache.avalon.framework.logger.LogEnabled;
import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.context.Context;
import org.apache.avalon.framework.context.Contextualizable;
import org.apache.avalon.framework.context.ContextException;
import org.apache.avalon.framework.CascadingException;

import org.apache.orb.ORB;
import org.apache.orb.util.IOR;

import org.openorb.CORBA.LoggableLocalObject;


/**
 * Server that handles the establishment of a portal delegate.
 * @author <a href="mailto:mcconnell@osm.net">Stephen McConnell</a>
 */

public class PortalServer extends LoggableLocalObject
implements LogEnabled, Contextualizable, Configurable, Serviceable, Initializable, Startable, Disposable
{
    
   /**
    * The configuration is an in memory representation of the XML assembly file used 
    * as a container of static configuration values.
    */
    private Configuration m_config;

   /**
    * The m_manager contains the reference to supporting services (where 
    * services are mapped under a role string - e.g. "net.osm.discovery.Directory").
    */
    private ServiceManager m_manager;

   /**
    * The main server object request broker.
    */
    private ORB m_orb;

   /**
    * The root portable object adapter.
    */
    private POA root;

   /**
    * Directory object reference.
    */
    protected Portal m_portal;

   /**
    * Object reference to the TimeService.
    */
    private TimeService time;

   /**
    * The gateway portable object adapter.
    */
    private POA m_poa;

   /**
    * Application context
    */
    private Context m_context;

    //=================================================================
    // Contextualizable
    //=================================================================

    public void contextualize( Context context ) throws ContextException
    {
	  m_context = (Context) context;
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
                
        if( null != m_config )
        {
            throw new ConfigurationException( 
               "Configurations for block " + this + " already set" );
        }
        m_config = config;
    }
    
    //================================================================
    // Serviceable
    //================================================================
    
    /**
     * Pass the <code>ServiceManager</code> to the <code>composer</code>.
     * The <code>Serviceable</code> implementation should use the specified
     * <code>ServiceManager</code> to acquire the components it needs for
     * execution.
     *
     * @param manager The <code>ServiceManager</code> which this
     *                <code>Serviceable</code> uses.
     */
    public void service( ServiceManager manager )
    throws ServiceException
    {
        m_manager = manager;
        m_orb = (ORB) m_manager.lookup("orb");
        time = (TimeService) m_manager.lookup("time");
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
        
        getLogger().debug("starting intialization");
        try
        {
            PortalSingleton.init( m_orb );
            root = POAHelper.narrow(m_orb.resolve_initial_references("RootPOA"));
        }
        catch ( InvalidName e )
        {
            throw new CascadingException("ORB initalization failure.", e );
        }

        //
        // create the portal
        //

        PortalDelegate delegate = new PortalDelegate( );
        try
        {
            getLogger().debug("creating servant" );

	      m_poa = root.create_POA(
		  "PORTAL",
		  root.the_POAManager(),
		  new Policy[] {
		     root.create_id_assignment_policy( IdAssignmentPolicyValue.USER_ID ),
		     root.create_lifespan_policy( LifespanPolicyValue.PERSISTENT )}
		);

		// create servant
	      Servant servant = new PortalPOATie( delegate );
            byte[] ID = "PORTAL".getBytes();
            m_poa.activate_object_with_id( ID, servant );
            org.omg.CORBA.Object obj = m_poa.id_to_reference(ID);
            m_portal = PortalHelper.narrow( obj );
                    
        } 
        catch (Exception e)
        {
            getLogger().fatalError("failed to establish the portal servant", e );
            throw new Exception("failed to establish the portal servant");
        }

	  // configure and initialize the delegate

        try
	  {
            delegate.enableLogging( getLogger().getChildLogger("portal") );
            delegate.contextualize( m_context );
            delegate.configure( m_config );
            delegate.initialize();
	  }
	  catch( Exception e )
	  {
            getLogger().fatalError("failed to initialize portal delegate", e );
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
        
        try
        {
            String ior = m_config.getChild("ior").getAttribute("value");
            IOR.writeIOR( m_orb, m_portal, ior );
            if( getLogger().isDebugEnabled() ) getLogger().debug( "published portal IOR to: " + ior );
        }
        catch (Exception e)
        {
            // IOR publication disabled
        }
        
        m_orb.start();

        getLogger().debug("started" );
    }
    
    /**
     * Stops the component.
     */
    public void stop()
    throws Exception
    {
        m_orb.stop();
        getLogger().debug("stopped" );
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
        getLogger().debug("disposal" );
        try
        {
            m_poa.destroy( true, true );
        }
        catch( Throwable e )
        {
            if( getLogger().isWarnEnabled() ) getLogger().warn( "ignoring POA related exception" );
        }
    }
}
