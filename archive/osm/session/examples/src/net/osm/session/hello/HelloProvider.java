/**
 * File: DefaultServantManager.java
 * License: etc/LICENSE.TXT
 * Copyright: Copyright (C) The Apache Software Foundation. All rights reserved.
 * Copyright: OSM SARL 2001-2002, All Rights Reserved.
 */

package net.osm.session.hello;

import java.io.File;
import java.util.Map;
import java.util.Hashtable;

import org.apache.avalon.framework.activity.Disposable;
import org.apache.avalon.framework.activity.Initializable;
import org.apache.avalon.framework.activity.Startable;
import org.apache.avalon.framework.context.Context;
import org.apache.avalon.framework.context.Contextualizable;
import org.apache.avalon.framework.context.ContextException;
import org.apache.avalon.framework.component.Component;
import org.apache.avalon.framework.logger.Logger;
import org.apache.avalon.framework.logger.LogEnabled;
import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.service.DefaultServiceManager;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.DefaultServiceManager;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.Serviceable;
import org.apache.avalon.framework.CascadingRuntimeException;
import org.apache.avalon.framework.CascadingException;
import org.apache.orb.ORBContext;
import org.apache.orb.POAContext;
import org.apache.orb.ORB;
import org.apache.orb.util.IOR;
import org.apache.orb.DefaultPOAContext;
import org.apache.pss.ConnectorContext;

import org.omg.CORBA.Policy;
import org.omg.CORBA.TRANSIENT;
import org.omg.CORBA.LocalObject;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.Servant;
import org.omg.PortableServer.ServantActivator;
import org.omg.PortableServer.ForwardRequest;
import org.omg.PortableServer.POAHelper;
import org.omg.PortableServer.IdAssignmentPolicyValue;
import org.omg.PortableServer.RequestProcessingPolicyValue;
import org.omg.PortableServer.ImplicitActivationPolicyValue;
import org.omg.PortableServer.LifespanPolicyValue;
import org.omg.PortableServer.IdUniquenessPolicyValue;
import org.omg.PortableServer.ServantRetentionPolicyValue;

import net.osm.adapter.Adapter;
import net.osm.adapter.Adaptive;
import net.osm.adapter.AdaptiveHelper;
import net.osm.factory.FactoryPOA;
import net.osm.factory.FactoryService;
import net.osm.factory.FactoryProvider;
import net.osm.factory.FactoryHelper;
import net.osm.factory.UnrecognizedCriteria;
import net.osm.factory.InvalidCriteria;
import net.osm.factory.CreationException;
import net.osm.factory.Argument;
import net.osm.factory.Parameter;
import net.osm.session.processor.ProcessorService;


/**
 * <p>The <code>HelloProvider</code> is a server implementation
 * that handles hello appliance reference creation requests.</p>
 *
 * @author <a href="mailto:mcconnell@osm.net">Stephen McConnell</a>
 */

public class HelloProvider extends FactoryProvider
{

    //=================================================================
    // state
    //=================================================================

   /**
    * The factory key.
    */
    private String m_key = "HELLO";

   /**
    * The default instance name.
    */
    private final String m_name = "Hello World";

   /**
    * The default parameter list.
    */
    private Parameter[] m_params = new Parameter[0];

   /**
    * The service manager supplied by the provider's container.
    */
    private ServiceManager m_manager;

    
   /**
    * Internal reference to the processor service, aquired from the service 
    * manager, and used to request processor instance creation.
    */
    private ProcessorService m_processor_service;

    //=======================================================================
    // Contextualizable
    //=======================================================================

   /**
    * Set the component context.
    * @param context the component context
    * @see FactoryContext
    */
    public void contextualize( Context context )
    throws ContextException
    {
        super.contextualize( context );
    }

    //============================================================================
    // Serviceable
    //============================================================================
    
    /**
     * Pass the <code>ServiceManager</code> to the <code>Serviceable</code>.
     * The <code>Serviceable</code> implementation should use the supplied
     * <code>ServiceManager</code> to acquire the components it needs for
     * execution.
     *
     * @param manager The <code>ServiceManager</code> which this
     *                <code>Serviceable</code> uses.
     */
    public void service( ServiceManager manager )
    throws ServiceException
    {
        super.service( manager );
        m_manager = manager;
    }

    //=======================================================================
    // Initializable
    //=======================================================================
    
   /**
    * Initialization of the provider.
    * @exception Exception if an error occurs during initalization
    */
    public void initialize()
    throws Exception
    {
        try
        {
            m_processor_service = (ProcessorService) m_manager.lookup(
              ProcessorService.PROCESSOR_SERVICE_KEY );
        }
        catch( Throwable e )
        {
            getLogger().error("initalization problem", e );
        }
        super.initialize();
    }

    //=======================================================================
    // Factory
    //=======================================================================


   /**
    * Returns the default name to apply to instances created by this factory.
    * @return String default name
    */
    public String get_default_name()
    {
        return m_name;
    }

    /**
     * Creates a new object reference
     * @param  arguments an array of arguments 
     * @return  Adaptive an adaptive object reference to a new processor
     *   associated with the HelloAppliance.
     * @exception  UnrecognizedCriteria if the arguments established by the
     *   adapter implementation is unknown to the factory
     * @exception  InvalidCriteria if the arguments created by the 
     *   implementation is recognized but rejected as invalid
     */
    public Adaptive create( Argument[] arguments ) 
    throws UnrecognizedCriteria, InvalidCriteria, CreationException
    {
        try
        {
            return m_processor_service.createProcessor( 
              "hello", "net.osm.session.hello.HelloAppliance" );
        }
        catch( Throwable e )
        {
            final String error = "Processor creation failure.";
            throw new CreationException( error + " Cause: " + e.toString() );
        }
    }
}
