/*
 * Copyright 2000 OSM SARL. All Rights Reserved.
 * 
 * This software is the proprietary information of OSM SARL.  
 * Use is subject to license terms.
 * 
 * @author  Stephen McConnell
 * @version 1.0 26/03/2001
 */

package net.osm.session.processor;

import org.apache.avalon.framework.logger.Logger;
import org.apache.avalon.framework.logger.LogEnabled;
import org.apache.avalon.framework.context.Context;
import org.apache.avalon.framework.context.Contextualizable;
import org.apache.avalon.framework.context.ContextException;
import org.apache.avalon.framework.context.DefaultContext;
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

import org.apache.orb.ORB;
import org.apache.orb.ORBContext;
import org.apache.orb.POAContext;
import org.apache.orb.DefaultPOAContext;
import org.apache.orb.util.LifecycleHelper;
import org.apache.pss.Connector;
import org.apache.pss.Session;
import org.apache.pss.ConnectorContext;
import org.apache.pss.StorageContext;
import org.apache.time.TimeUtils;

import org.omg.CORBA.Any;
import org.omg.CosTime.TimeService;
import org.omg.CORBA.LocalObject;
import org.omg.CORBA.Policy;
import org.omg.CORBA.TypeCode;
import org.omg.CosPersistentState.NotFound;
import org.omg.PortableServer.ServantLocator;
import org.omg.PortableServer.Servant;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.ServantLocatorPackage.CookieHolder ;
import org.omg.PortableServer.ForwardRequest;
import org.omg.PortableServer.POAHelper;
import org.omg.PortableServer.IdAssignmentPolicyValue;
import org.omg.PortableServer.RequestProcessingPolicyValue;
import org.omg.PortableServer.ImplicitActivationPolicyValue;
import org.omg.PortableServer.LifespanPolicyValue;
import org.omg.PortableServer.IdUniquenessPolicyValue;
import org.omg.PortableServer.ServantRetentionPolicyValue;
import org.omg.CosPersistentState.StorageObject;
import org.omg.CosPersistentState.StorageHomeBase;
import org.omg.CosPersistentState.NotFound;

import org.omg.CosPropertyService.PropertySet;
import org.omg.CosPropertyService.PropertySetDef;
import org.omg.CosPropertyService.PropertySetDefHelper;
import org.omg.CosPropertyService.PropertySetDefPOATie;
import org.omg.CosPropertyService.PropertyModeType ;
import org.omg.CosPropertyService.PropertyDef;
import org.omg.CosPropertyService.PropertyHolder;
import org.omg.CosPropertyService.PropertiesHolder;
import org.omg.CosPropertyService.PropertiesIteratorHolder;
import org.omg.Session.task_state;

import net.osm.adapter.Adaptive;
import net.osm.chooser.Chooser;
import net.osm.chooser.ChooserService;
import net.osm.chooser.ChooserException;
import net.osm.chooser.DefaultChooser;
import net.osm.chooser.UnknownName;
import net.osm.factory.FactoryHelper;
import net.osm.session.SessionException;
import net.osm.session.ProducedBy;
import net.osm.session.Executes;
import net.osm.session.DefaultExecutes;
import net.osm.session.resource.AbstractResource;
import net.osm.session.resource.AbstractResourceServantManager;

import org.apache.excalibur.merlin.PipelineService;

/**
 * <p>The <code>ProcessorServantManager</code> provides services supporting the 
 * creation of new <code>Processor</code> instances and a servant activation
 * model for handling the redirection of incomming requests.</p>
 *
 * @author <a href="mailto:mcconnell@osm.net">Stephen McConnell</a>
 */
public class ProcessorServantManager extends AbstractResourceServantManager
implements ProcessorService
{
    //=======================================================================
    // static
    //=======================================================================

    private static final String c_key = "processor";

    //=======================================================================
    // state
    //=======================================================================

    private Context m_context;
    private Configuration m_config;
    private ServiceManager m_manager;
    private ProcessorStorageHome m_home;
    private DefaultChooser m_chooser;
    private POA m_poa;

    //=======================================================================
    // Contextualizable
    //=======================================================================

   /**
    * Set the component context.
    * @param context the component context
    */
    public void contextualize( Context context )
    {
	  super.contextualize( context );
	  m_context = context;
    }

    //=======================================================================
    // Configurable
    //=======================================================================

   /**
    * Used by a container to supply the static component configuration.
    * @param config the static configuration
    */    
    public void configure( final Configuration config )
    throws ConfigurationException
    {
        super.configure( config );
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
        super.service( manager );
        m_manager = manager;
    }

    //=======================================================================
    // Initializable
    //=======================================================================
    
   /**
    * Initialization of the manager during which the container storage home is 
    * resolved following supertype initialization.
    * @exception Exception if an error occurs during initalization
    */
    public void initialize()
    throws Exception
    {
	  super.initialize();

	  try
	  {
            m_home = (ProcessorStorageHome) super.getSession().find_storage_home( 
	        "PSDL:osm.net/session/processor/ProcessorStorageHomeBase:1.0" );
	  }
	  catch( Throwable throwable )
	  {
	      String error = "unable to complete supplier initialization due to a unexpected error";
		throw new ProcessorException( error, throwable );
	  }
    }

    //=======================================================================
    // DefaultServantManager
    //=======================================================================

   /**
    * Method by which derived types can override POA creation.
    * @return POA the portable object adapter
    */
    protected POA createPOA() throws Exception
    {
        m_poa = super.createPOA();
        return m_poa;
    }

   /**
    * Returns the name to be assigned to the POA on creation.
    * @return String the POA name
    */
    protected String getPoaName()
    {
        return c_key;
    }

   /**
    * Returns a servant implmentation.
    * @return Servant a managable servant
    */
    protected Servant createServant( 
      StorageContext context, Configuration config, ServiceManager manager 
    ) throws Exception
    {
        ProcessorDelegate delegate = new ProcessorDelegate();
        Servant servant = new ProcessorPOATie( delegate );
        Logger log = getLogger().getChildLogger( "" + System.identityHashCode( delegate ) );
        LifecycleHelper.pipeline( delegate, log, context, config, manager );
        return servant;
    }

   /**
    * Returns the storage home managed by the manager.
    * @return StorageHomeBase the storage home
    */
    protected StorageHomeBase getStorageHome()
    {
        return m_home;
    }
    
   /**
    * Returns an object reference for a storage object.
    * @param store the storage object
    */
    protected AbstractResource getObjectReference( StorageObject store )
    {
        try
        {
            return getProcessorReference( (ProcessorStorage) store );
        }
        catch( Throwable e )
        {
            throw new ProcessorRuntimeException("Invalid storage type argument.", e );
        }
    }

    //=======================================================================
    // ProcessorService
    //=======================================================================
    
   /**
    * Return a reference to an object as an Processor.
    * @param store ProcessorStorage storage object
    * @return Processor object reference
    */
    public Processor getProcessorReference( ProcessorStorage store )
    {
        return ProcessorHelper.narrow( 
          m_poa.create_reference_with_id( 
            store.get_pid(), 
            ProcessorHelper.id() ) );
    }

   /**
    * Creation of a Processor.
    * @param name the initial name to assign to the processor
    * @param appliance the class name of the appliance to establish as the process logic
    * @return Processor a new Processor object reference
    */
    public Processor createProcessor( String name, String appliance ) 
    throws ProcessorException
    {
        ProcessorStorage store = createProcessorStorage( name, appliance );
        return getProcessorReference( store );
    }

   /**
    * Creation of a new <code>StorageObject</code> representing the 
    * state of a new <code>Processor</code> instance.
    * @param name the name to apply to the new resource
    * @param appliance the class name of the appliance to establish as the process logic
    * @return StorageObject storage object encapsulating the resource state
    */
    public ProcessorStorage createProcessorStorage( String name, String appliance ) throws ProcessorException
    {
        return createProcessorStorage( m_home, name, appliance );
    }

   /**
    * Creation of a new <code>StorageObject</code> representing the 
    * state of a new <code>Processor</code> instance.
    * @param home the processor storage home
    * @param name the name to apply to the new resource
    * @param appliance the class name of the appliance to establish as the process logic
    * @return StorageObject storage object encapsulating the resource state
    */
    public ProcessorStorage createProcessorStorage( 
      ProcessorStorageHome home, String name, String appliance ) 
    throws ProcessorException
    {
        if( getLogger().isDebugEnabled() ) getLogger().debug("creating processor storage");

        try
        {
            ProcessorStorage store = (ProcessorStorage) createAbstractResourceStorage( home, name );
            store.description("default description");
            store.processor_state( task_state.notstarted.value() );
            store.executes( new DefaultExecutes() );
            store.checkpoint(0);
            store.appliance( appliance );
            return store;
        }
        catch( Throwable e )
        {
            final String error = "Unexpected problem while creating processor storage.";
            if( getLogger().isErrorEnabled() ) getLogger().error( error, e );
            throw new ProcessorException( error, e );
        }
    }
}
