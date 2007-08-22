/*
 * Copyright (C) The Apache Software Foundation. All rights reserved.
 *
 * This software is published under the terms of the Apache Software License
 * version 1.1, a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package net.osm.merlin.registry;

import java.io.InputStream;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.JarURLConnection;
import java.net.URLClassLoader;
import java.util.List;
import java.util.LinkedList;
import java.util.Map;
import java.util.Hashtable;
import java.util.Properties;
import java.util.Vector;
import java.util.Iterator;
import java.util.jar.Attributes;
import java.util.jar.Manifest;
import java.util.Enumeration;
import java.security.Policy;
import java.io.FileInputStream;

import org.apache.avalon.excalibur.i18n.ResourceManager;
import org.apache.avalon.excalibur.i18n.Resources;
import org.apache.excalibur.configuration.ConfigurationUtil;
import org.apache.avalon.framework.logger.Logger;
import org.apache.avalon.framework.logger.LogEnabled;
import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.avalon.framework.logger.AvalonFormatter;
import org.apache.avalon.framework.logger.LogKitLogger;
import org.apache.avalon.framework.activity.Initializable;
import org.apache.avalon.framework.activity.Disposable;
import org.apache.avalon.framework.activity.Executable;
import org.apache.avalon.framework.CascadingRuntimeException;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.DefaultConfiguration;
import org.apache.avalon.framework.configuration.DefaultConfigurationBuilder;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.context.Context;
import org.apache.avalon.framework.context.Contextualizable;
import org.apache.avalon.framework.context.ContextException;
import org.apache.avalon.framework.context.DefaultContext;
import org.apache.avalon.framework.service.Serviceable;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.DefaultServiceManager;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.Version;
import org.apache.avalon.framework.ExceptionUtil;
import org.apache.avalon.excalibur.extension.PackageRepository;
import org.apache.avalon.excalibur.extension.Extension;
import org.apache.avalon.excalibur.extension.OptionalPackage;
import org.apache.avalon.excalibur.extension.DefaultPackageRepository;
import org.apache.excalibur.containerkit.metainfo.ComponentDescriptor;
import org.apache.excalibur.containerkit.metainfo.ComponentInfo;
import org.apache.excalibur.containerkit.metainfo.ServiceDescriptor;
import org.apache.excalibur.containerkit.metainfo.DependencyDescriptor;
import org.apache.excalibur.containerkit.metainfo.ServiceDesignator;
import org.apache.excalibur.containerkit.metadata.ComponentMetaData;
import org.apache.excalibur.containerkit.metadata.DependencyMetaData;
import org.apache.excalibur.containerkit.verifier.AssemblyVerifier;
import org.apache.excalibur.containerkit.verifier.MetaDataVerifier;
import org.apache.excalibur.containerkit.verifier.VerifyException;
import org.apache.excalibur.containerkit.dependency.DependencyMap;
import org.apache.log.output.io.StreamTarget;
import org.apache.log.Hierarchy;
import org.apache.log.Priority;

import net.osm.merlin.registry.*;
import net.osm.merlin.kernel.Container;
import net.osm.merlin.kernel.ContainerClassLoader;
import net.osm.merlin.kernel.Verifiable;

/**
 * Provides support for the maintenance of a registry of 
 * component type definitions established within a classloader.
 * @author <a href="mailto:mcconnell@apache.org">Stephen McConnell</a>
 * @version $Revision: 1.1 $ $Date: 2002/07/04 05:51:30 $
 */
public class DefaultRegistry implements LogEnabled, Contextualizable, Configurable, Initializable, Executable, Disposable, Registry, Verifiable
{
    //=======================================================================
    // static
    //=======================================================================

   /**
    * Context key used to locate the application classloader.
    */
    public static final String CLASSLOADER_KEY = "classloader";

   /**
    * Context key used to locate the application classloader.
    */
    public static final String CONTAINER_KEY = "container";

   /**
    * Context key used to locate the application classloader.
    */
    public static final String MAP_KEY = "map";

    private static final Resources REZ =
        ResourceManager.getPackageResources( DefaultRegistry.class );

    //=======================================================================
    // state
    //=======================================================================

   /**
    * The parent registry to this registry.
    * (not currently in use - will come into effect as registry hierachies as 
    * dealt with).
    */
    private Container m_parent;

   /**
    * The context argument supplied by the container.
    */
    private Context m_context;

   /**
    * The list of all ComponentDefinition instances keyed by classname.
    */
    private Hashtable m_componentRegistry = new Hashtable();

   /**
    * The list of all ComponentDefinition instances keyed by service classname.
    */
    private ServiceRegistry m_services;

    private ContainerClassLoader m_classloader;

   /**
    * List of the component classname recorded in the jar file manifests.
    */
    private List m_classnames = new LinkedList();

    private Configuration m_config;

    private MetaDataVerifier m_verifier;

    private AssemblyVerifier m_assemblyVerifier;

    private Hashtable m_profileMapping = new Hashtable();

    private Hashtable m_profiles = new Hashtable();

    private DependencyMap m_map;

    private Logger m_logger;

    //=======================================================================
    // LogEnabled
    //=======================================================================

   /**
    * Invoked by the parent to assign the logging channel.
    * @param logger the logging channel
    */
    public void enableLogging( Logger logger )
    {
        m_logger = logger;
    }

   /**
    * Returns the assigned logging channel.
    * @return the logging channel
    */
    protected Logger getLogger()
    {
        return m_logger;
    }

    //=======================================================================
    // Contextualizable
    //=======================================================================

   /**
    * Service context from which the registry classloader is resolved.
    * @param context a context value containing the key 'classloader' 
    */
    public void contextualize( Context context ) throws ContextException
    {
        m_context = context;
        m_classloader = (ContainerClassLoader) context.get( CLASSLOADER_KEY );
        m_parent = (Container) context.get( CONTAINER_KEY );
        m_map = (DependencyMap) context.get( MAP_KEY );
    }

    //=======================================================================
    // Configure
    //=======================================================================

   /**
    * Invoked by the container to establish the registry configuration.
    * @param config a component configuration
    */
    public void configure( Configuration config)
    {
        m_config = config;
        getLogger().debug("registry configuration");
        getLogger().debug( ConfigurationUtil.list( config ) );
    }

    //=======================================================================
    // Initializable
    //=======================================================================

   /**
    * Initalization of a <code>Registry</code> during which an application
    * scoped classloader is created.
    * @exception Exception if an error occurs during initialization.
    */
    public void initialize() throws Exception
    {
        getLogger().debug("registry initialization");
        m_services = new ServiceRegistry( this, m_classloader, m_map, m_config );
        m_services.enableLogging( getLogger().getChildLogger("services") );
        String[] blocks = m_classloader.getComponentClassnames();

        try
        {
            //
            // register all of the the component providers implied by the classpath
            // manifest declarations
            //

            for( int i=0; i<blocks.length; i++ )
            {
                // initialize the component type defintions
                final String classname = blocks[i].replace('/','.');
                m_services.register( classname );
            }

            //
            // for all of the components declared in the application profiles,
            // install each one in the service repository - the side effect of 
            // this is the buildup of the m_profiles table that will be used to 
            // construct the application context
            //

            Configuration[] entries = m_config.getChildren( "component" );
            for( int i=0; i<entries.length; i++ )
            {
                final Configuration factory = entries[i];
                final String name = factory.getAttribute("name");
                final String classname = factory.getAttribute("class");

                getLogger().debug("component configuration");
                getLogger().debug( ConfigurationUtil.list( factory ) );
                Profile profile = m_services.install( classname, name );
                populate( m_map, profile );
                //listProfile( profile );
            }

            //
            // if its a container we need to instantiate it and verify it.
            //

            if( m_config.getName().equals("containers"))
            {
                getLogger().debug(
                  "nested containers (" 
                  + entries.length 
                  + ") pending validation."
                );
            }
        }
        catch( Throwable e )
        {
            final String error = "Internal registry initialization failure.";
            throw new RegistryException( error, e );
        }
    }

    //=======================================================================
    // Executable
    //=======================================================================

   /**
    * Executes composition of a target component assembly options relative to a target 
    * component classname within the scope of a classpath declaration containing jar file 
    * references (refer contextualize), and generate a report to the logging channel.
    *
    * @exception Exception if an error occurs during execution
    */
    public void execute() throws Exception
    {
    }

    //=======================================================================
    // Verifiable
    //=======================================================================

   /**
    * Method invoked by a parent container to request type level validation of 
    * the container.
    *
    * @exception ValidationException if a validation failure occurs
    */
    public void verify() throws VerifyException
    {
        getLogger().debug("DependencyMap listing");
        ComponentMetaData[] startup = m_map.getStartupGraph();
        doVerify( startup );
    }

    //=======================================================================
    // Registry
    //=======================================================================

   /**
    * Return the preferred profile for a depedency.
    * @param dependency a consumer component dependecy declaration
    * @return the preferred candidate supplier profile
    */
    public Profile getCandidateProfile( DependencyDescriptor dependency ) 
      throws UnresolvedProviderException
    {
        return m_services.getCandidateProfile( dependency );
    }

   /**
    * Returns an array of component profiles representing candidate component types 
    * capable of acting as a service supplier relative to the supplied dependecy descriptor.
    * @param dependency the dependency descriptor to evaluate
    * @return the set of profiles
    */
    public Profile[] getCandidateProfiles( DependencyDescriptor dependency ) 
    {
        return m_services.getProfiles( dependency.getService() );
    }

   /**
    * Method invoked by a DefaultProfile to declare itself within the application scope.
    * @param profile the Profile to include in application scope
    */
    void install( Profile profile )
    {
        m_services.install( profile );
    }

    //=======================================================================
    // implementation
    //=======================================================================

    private void populate( DependencyMap map, Profile profile )
    {
        map.add( profile );
        DependencyMetaData[] dependencies = profile.getDependencies();
        for( int j=0; j<dependencies.length; j++ )
        {
            populate( map, dependencies[j] );
        }
    }

    private void populate( DependencyMap map, DependencyMetaData dependency )
    {
        Profile profile = m_services.getInstalledProfile( dependency.getProviderName() );
        map.add( profile );
        DependencyMetaData[] dependencies = profile.getDependencies();
        for( int j=0; j<dependencies.length; j++ )
        {
            populate( map, dependencies[j] );
        }
    }

   /**
    * Test if the registry can resolve a request for a component with the supplied classname
    * @param classname a component or service class name
    * @return TRUE if the registry can service the request
    */
    private boolean hasComponentDefinition( String classname )
    {
        return m_services.getComponentType( classname ) != null;
    }

   /**
    * Returns an component defintion either through referdnce to an exiting defintion
    * or through defintion coreation if no exiting defintion is available relative to 
    * the request classname key.
    * @param classname the class name of the component defintion to locate or create
    * @return the corresponding component defintion
    * @exception RegistryRuntimeException if an error occurs during the 
    *    construction of a new component defintion
    */
    private ComponentType getComponentDefinition( String classname )
    {
        return m_services.getComponentType( classname );
    }

   /**
    * Returns the component type implementation class.
    * @param type the component type descriptor
    * @return the class implementing the component type
    */
    private Class getComponentClass( ComponentType type ) throws RegistryException
    {
        if( null == type )
          throw new NullPointerException("Illegal null component type argument.");

        final String classname = type.getComponentInfo().getComponentDescriptor().getImplementationKey();
        try
        {
            return m_classloader.loadClass( classname );
        }
        catch( Throwable e )
        {
            final String error = "Could not load implementation class for component type: "
              + classname;
            throw new RegistryException( error, e );
        }
    }


   /**
    * Returns the service type implementation class.
    * @param service the service type descriptor
    * @return the class implementing the service type
    */
    private Class getServiceClass( ServiceDescriptor service ) throws RegistryException
    {
        final String classname = service.getServiceDesignator().getClassname();
        try
        {
            return m_classloader.loadClass( classname );
        }
        catch( Throwable e )
        {
            final String error = "Could not load implementation class for service type: "
              + classname;
            throw new RegistryException( error, e );
        }
    }

    private void listProfile( Profile profile )
    {
        List reported = new LinkedList();
        listProfile( profile, reported );
    }

    private void listProfile( Profile profile, List reported )
    {
        if( !reported.contains( profile ) )
        {
            getLogger().debug( profile.report() );
            reported.add( profile );
        }
        DependencyMetaData[] dependencies = profile.getDependencies();
        for( int j=0; j<dependencies.length; j++ )
        {
            Profile p = m_services.getInstalledProfile( dependencies[j].getProviderName() );
            listProfile( p, reported );
        }
    }

    private void doVerify( ComponentMetaData[] assembly ) throws VerifyException
    {
        MetaDataVerifier mdv = new MetaDataVerifier();
        for( int i=0; i<assembly.length; i++ )
        {
            try
            {
                getLogger().debug("verifying: " + assembly[i].getName() );
                mdv.verifyType( assembly[i], m_classloader );
            }
            catch( Throwable e )
            {
                getLogger().error("verification failure", e );
            }
        }
        if( m_assemblyVerifier == null ) 
        {
            m_assemblyVerifier = new AssemblyVerifier();
            m_assemblyVerifier.enableLogging( getLogger().getChildLogger("verifier") );
        }
        try
        {
            getLogger().debug("commencing assembly verification");
            m_assemblyVerifier.verifyAssembly( assembly );
        }
        catch( Throwable e )
        {
            getLogger().error("assembly verification failure", e );
        }
    }

    //=======================================================================
    // Disposable
    //=======================================================================

   /**
    * Disposal of this component.
    */
    public void dispose()
    {
    }
}
