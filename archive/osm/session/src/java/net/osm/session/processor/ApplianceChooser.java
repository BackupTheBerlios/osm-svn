/*
 * Copyright 2001 OSM All Rights Reserved.
 * 
 * This software is the proprietary information of OSM.
 * Use is subject to license terms.
 * 
 * @author  Stephen McConnell
 * @version 2.0 28-MAR-2002
 */

package net.osm.session.processor;

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

import org.apache.excalibur.configuration.ConfigurationUtil;

import org.apache.orb.ORBContext;
import org.apache.orb.POAContext;
import org.apache.orb.util.LifecycleHelper;
import org.apache.orb.ORB;
import org.apache.orb.util.IOR;
import org.apache.orb.DefaultPOAContext;

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
import net.osm.adapter.ServiceContext;
import net.osm.adapter.DefaultServiceContext;
import net.osm.chooser.DefaultChooser;
import net.osm.chooser.ChooserContext;
import net.osm.chooser.DefaultChooserContext;
import net.osm.factory.FactoryHelper;
import net.osm.factory.Parameter;

/**
 * <p>The <code>ApplianceChooser</code> is a chooser implementation that handles
 * the registration and publication of processor appliance extensions. Client
 * application access full constructor <code>Processor</code> instances by
 * invoking the factory <code>create</code> operation.</p>
 *
 * @author <a href="mailto:mcconnell@osm.net">Stephen McConnell</a>
 */
public class ApplianceChooser extends DefaultChooser
{

    //=================================================================
    // state
    //=================================================================

   /**
    * Service static configuration.
    */
    private Configuration m_config;

   /**
    * Application context
    */
    private Context m_context;

   /**
    * Service manager.
    */
    private ServiceManager m_manager;

   /**
    * Chooser appliance key.
    */
    private String m_key;

   /**
    * Appliance parent identifier chain.
    */
    private String[] m_path;


    //=======================================================================
    // Contextualizable
    //=======================================================================

   /**
    * Set the component context.
    * @param context the component context
    */
    public void contextualize( Context context )
    throws ContextException
    {
        super.contextualize( context );
        m_context = context;
        m_key = (String) context.get( ServiceContext.SERVICE_KEY );
        m_path = (String[]) context.get( ServiceContext.SERVICE_PATH );

    }

    //=======================================================================
    // Configurable
    //=======================================================================
    
   /**
    * Configuration of the chooser by its container.
    * @param config the configuration supplied by the container
    * @exception ConfigurationException if a configuration error occurs
    */
    public void configure( final Configuration config )
    throws ConfigurationException
    {
	  super.configure( config );
        m_config = config;
        getLogger().debug("chooser configuration:\n" + 
          ConfigurationUtil.list( m_config ));
    }

    //=======================================================================
    // Serviceable
    //=======================================================================

    /**
     * Pass the <code>ServiceManager</code> to the <code>Serviceable</code>.
     * The <code>Serviceable</code> implementation uses the supplied
     * <code>ServiceManager</code> to acquire the components it needs for
     * execution.
     * @param parent the <code>ServiceManager</code> for this delegate
     * @exception ServiceException
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
    * One-time initalization of the service during which the POA is established
    * and appliance extensions are installed.  The implementation locatates 
    * <code>factory</code> child elements ion ths supplied configuration and 
    * applies classic component pipeline processing and subsequently registers
    * the factory as a member of the chooser.
    * @exception IllegalStateException if the server has been disposed of, if 
    *  has not log enabled, configured, contextualized, or serviced.
    * @exception Exception if a general initalization error occurs
    */
    public void initialize()
    throws Exception
    {
        super.initialize();

        //
        // read in the list of applicance factory declarations from the 
        // configuration and register each one under this chooser
        //

        try
        {
            ClassLoader loader = Thread.currentThread().getContextClassLoader();
            Configuration[] factories = m_config.getChild("factories").getChildren("factory");
            for( int i=0; i<factories.length; i++ )
            {
                Configuration config = factories[i];
                try
                {

                    String key = config.getAttribute("key");
                    String provider = config.getAttribute("provider");

                    //
                    // create a new instance of the factory class
                    //

                    getLogger().debug( "creating factory: " + key + " using: " + provider );
                    Servant target = (Servant) loader.loadClass( provider ).newInstance();

                    //
                    // create the factory configuration argument
                    //

                    //###
                    // not implemented yet - need to get default configuration
                    // and gfenerate a cascading configuration from the supplied
                    // configuration

                    final Configuration factoryConfig = config;

                    //
                    // create the factory context argument
                    //

                    DefaultServiceContext context = new DefaultServiceContext(
                       m_path, key, m_context );

                    //
                    // create the factory service argument
                    //

                    DefaultServiceManager manager = new DefaultServiceManager( m_manager );
                    manager.put( POAContext.POA_KEY, new DefaultPOAContext( m_poa ));
                    manager.makeReadOnly();

                    Logger logger = getLogger().getChildLogger( key );
                    LifecycleHelper.pipeline( target, logger, context, factoryConfig, manager );

                    register( key, FactoryHelper.narrow( target._this_object() ) );
                    getLogger().debug( 
                      "registered factory: " 
                      + target.getClass().getName() 
                      + " as: " 
                      + key );
                }
                catch( Throwable fe )
                {
                    final String error = "Unable to instantiate factory.";
                    throw new ProcessorException( error, fe );
                }
            }
        }
        catch( Throwable e)
        {
            throw new ProcessorRuntimeException( "Error populating appliance chooser.", e);
        }
    }
}
