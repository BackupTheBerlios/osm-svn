/*
 * Copyright (C) The Apache Software Foundation. All rights reserved.
 *
 * This software is published under the terms of the Apache Software License
 * version 1.1, a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package net.osm.merlin.kernel;

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
import java.security.Policy;
import java.io.FileInputStream;

import org.apache.avalon.excalibur.i18n.ResourceManager;
import org.apache.avalon.excalibur.i18n.Resources;
import org.apache.excalibur.configuration.ConfigurationUtil;
import org.apache.avalon.framework.logger.Logger;
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
import org.apache.excalibur.containerkit.dependency.DependencyMap;
import org.apache.excalibur.containerkit.metadata.ComponentMetaData;
import org.apache.log.Hierarchy;
import org.apache.log.Priority;
import org.apache.log.output.io.StreamTarget;

import net.osm.merlin.registry.DefaultRegistry;
import org.apache.excalibur.containerkit.verifier.VerifyException;

/**
 * Default container implementation that manages a registry of componet providers and 
 * a registry of child containers.
 *
 * @author <a href="mailto:mcconnell@apache.org">Stephen McConnell</a>
 * @version $Revision: 1.1 $ $Date: 2002/07/04 05:51:00 $
 */
public class DefaultContainer extends DefaultRegistry implements Container
{
    //=======================================================================
    // state
    //=======================================================================

   /**
    * Configuration.
    */
    private Configuration m_config;

   /**
    * The registry of embedded containers.
    */
    private List m_containers = new LinkedList();

   /**
    * Parent container.
    */
    private Container m_parent;

    private ContainerClassLoader m_classloader;

    private Logger m_logger;

    private DependencyMap m_map;

    //=======================================================================
    // Contextualizable
    //=======================================================================

   /**
    * Service context from which the registry classloader is resolved.
    * @param context a context value containing the key 'classloader' 
    */
    public void contextualize( Context context ) throws ContextException
    {
        m_classloader = (ContainerClassLoader) context.get( CLASSLOADER_KEY );
        try
        {
            m_map = (DependencyMap) context.get( MAP_KEY );
            m_parent = (Container) context.get( CONTAINER_KEY );
            super.contextualize( context );
        }
        catch( ContextException e )
        {
            DefaultContext c = new DefaultContext( context );
            c.put( CONTAINER_KEY, this );
            super.contextualize( c );
        }
    }

    //=======================================================================
    // Configurable
    //=======================================================================

   /**
    * Invoked by the container to establish the registry configuration.
    * @param config a component configuration
    */
    public void configure( Configuration config)
    {
        getLogger().debug("container configuration");
        super.configure( config );
        m_config = config;
    }

    //=======================================================================
    // Initializable
    //=======================================================================

   /**
    * Initalization of a <code>Container</code>.
    * @exception Exception if an error occurs during initialization.
    */
    public void initialize() throws Exception
    {
        getLogger().debug("container initialization (" + m_config.getAttribute("name","?") + ")" );
        super.initialize();
        Configuration[] containers = m_config.getChildren("container");
        for( int i=0; i<containers.length; i++ )
        {
            m_containers.add( createContainer( containers[i] ) );
        }
    }

    //=======================================================================
    // Verifiable
    //=======================================================================

   /**
    * Method invoked by a parent container to request type and assembly validation of 
    * this container.
    *
    * @exception ValidationException if a validation failure occurs
    */
    public void verify() throws VerifyException
    {
        super.verify();
        Iterator iterator = m_containers.iterator();
        while( iterator.hasNext() )
        {
            ((Verifiable)iterator.next()).verify();
        }
    }

    //=======================================================================
    // Container
    //=======================================================================

    public void startup() throws Exception
    {
        ComponentMetaData[] startup = m_map.getStartupGraph();
        getLogger().debug("startup");
        for( int i=0; i<startup.length; i++ )
        {
            getLogger().debug("start: " + startup[i].getName() );
        }
        Iterator iterator = m_containers.iterator();
        while( iterator.hasNext() )
        {
            ((Container)iterator.next()).startup();
        }
    }

    public void shutdown()
    {
        getLogger().debug("shutdown");
        ComponentMetaData[] shutdown = m_map.getShutdownGraph();
        for( int i=0; i<shutdown.length; i++ )
        {
            getLogger().debug("stop: " + shutdown[i].getName() );
        }
        Iterator iterator = m_containers.iterator();
        while( iterator.hasNext() )
        {
            ((Container)iterator.next()).shutdown();
        }
    }

    //=======================================================================
    // private
    //=======================================================================

    private DefaultContainer createContainer( Configuration conf ) throws Exception
    {
        Logger logger = getLogger().getChildLogger( conf.getAttribute("name","child") );
        Logger loaderLogger = logger.getChildLogger( "loader" );

        final ContainerClassLoader loader = new ContainerClassLoader(
          m_classloader, 
          conf.getChild("classpath"),
          loaderLogger
        );

        DefaultContext context = new DefaultContext();
        context.put( CLASSLOADER_KEY, loader );
        context.put( CONTAINER_KEY, this );
        context.put( MAP_KEY, new DependencyMap( m_map ) );

        DefaultContainer container = new DefaultContainer();
        container.enableLogging( logger );
        container.contextualize( context );
        container.configure( conf );
        container.initialize( );
        return container;
    }
}
