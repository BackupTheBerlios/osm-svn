/*
 * Copyright 2000 OSM SARL. All Rights Reserved.
 * 
 * This software is the proprietary information of OSM SARL.  
 * Use is subject to license terms.
 * 
 * @author  Stephen McConnell
 * @version 1.0 26/03/2001
 */

package net.osm.session.workspace;

import org.apache.avalon.framework.logger.Logger;
import org.apache.avalon.framework.logger.LogEnabled;
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

import org.apache.orb.ORB;
import org.apache.orb.ORBContext;
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

import net.osm.session.resource.AbstractResource;
import net.osm.session.resource.AbstractResourceServantManager;
import net.osm.session.SessionException;
import net.osm.session.ProducedBy;

/**
 * <p>The <code>AbstractResourceServantManager</code> provides services supporting the 
 * creation of new <code>AbstractResource</code> instances and a servant activation
 * model for handling the redirection of incomming requests.</p>
 *
 * @author <a href="mailto:mcconnell@osm.net">Stephen McConnell</a>
 */
public class WorkspaceServantManager extends AbstractResourceServantManager
implements WorkspaceService
{
    //=======================================================================
    // static
    //=======================================================================

    private static final String c_key = "workspace";

    //=======================================================================
    // state
    //=======================================================================

    private WorkspaceStorageHome m_home;
    private POA m_poa;

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
            m_home = (WorkspaceStorageHome) super.getSession().find_storage_home( 
	        "PSDL:osm.net/session/workspace/WorkspaceStorageHomeBase:1.0" );
	  }
	  catch( Throwable throwable )
	  {
	      String error = "unable to complete supplier initialization due to a unexpected error";
		throw new WorkspaceException( error, throwable );
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
        try
        {
            WorkspaceDelegate delegate = new WorkspaceDelegate();
            Servant servant = new WorkspacePOATie( delegate );
            Logger log = getLogger().getChildLogger( "" + System.identityHashCode( delegate ) );
            LifecycleHelper.pipeline( delegate, log, context, config, manager );
            return servant;
        }
        catch( Throwable e )
        {
            throw new WorkspaceRuntimeException("Workspace servant creation failure.", e );
        }
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
            return getWorkspaceReference( (WorkspaceStorage) store );
        }
        catch( Throwable e )
        {
            throw new WorkspaceRuntimeException("Invalid storage type argument.", e );
        }
    }

    //=======================================================================
    // WorkspaceService
    //=======================================================================
    

   /**
    * Return a reference to an object as an Workspace.
    * @param StorageObject storage object
    * @return Workspace object reference
    */
    public Workspace getWorkspaceReference( WorkspaceStorage store )
    {
        return WorkspaceHelper.narrow( 
          m_poa.create_reference_with_id( 
            store.get_pid(), 
            WorkspaceHelper.id() ) );
    }

   /**
    * Creation of a Workspace.
    * @param name the initial name to assign to the workspace
    * @return Workspace a new Workspace object reference
    */
    public Workspace createWorkspace( String name ) 
    throws WorkspaceException
    {
        if( getLogger().isDebugEnabled() ) getLogger().debug("creating workspace");
        try
        {
            WorkspaceStorage store = createWorkspaceStorage( name );
            return getWorkspaceReference( store );
        }
        catch( Throwable e )
        {
            final String error = "The workspace servant manager was unable to create a "
             + "workspace due to an unexpected error.";
            throw new WorkspaceRuntimeException( error, e ); 
        }
    }

   /**
    * Creation of a new <code>StorageObject</code> representing the 
    * state of a new <code>AbstractResource</code> instance.
    * @param name the name to apply to the new resource
    * @return StorageObject storage object encapsulating the resource state
    */
    public WorkspaceStorage createWorkspaceStorage( String name ) throws WorkspaceException
    {
        return createWorkspaceStorage( (WorkspaceStorageHome) getStorageHome(), name );
    }

   /**
    * Creation of a new <code>StorageObject</code> representing the 
    * state of a new <code>AbstractResource</code> instance.
    * @param name the name to apply to the new resource
    * @return StorageObject storage object encapsulating the resource state
    */
    public WorkspaceStorage createWorkspaceStorage( WorkspaceStorageHome home, String name ) 
    throws WorkspaceException
    {
        if( getLogger().isDebugEnabled() ) getLogger().debug("creating workspace storage");

        try
        {
            return (WorkspaceStorage) createAbstractResourceStorage( home, name );
        }
        catch( Throwable e )
        {
            final String error = "Unexpected problem while creating workspace storage.";
            if( getLogger().isErrorEnabled() ) getLogger().error( error, e );
            throw new WorkspaceException( error, e );
        }
    }
}
