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
import org.apache.avalon.framework.activity.Startable;
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
import org.apache.log.Hierarchy;
import org.apache.log.Priority;
import org.apache.log.output.io.StreamTarget;

import net.osm.merlin.registry.DefaultRegistry;

/**
 * Default kernel implementation.
 *
 * @author <a href="mailto:mcconnell@apache.org">Stephen McConnell</a>
 * @version $Revision: 1.1 $ $Date: 2002/07/04 05:51:00 $
 */
public class DefaultKernel extends AbstractLogEnabled 
  implements Kernel, Configurable, Initializable
{

    //=======================================================================
    // state
    //=======================================================================

    private Configuration m_config;

    private DefaultContainer m_container = new DefaultContainer();

    private boolean m_verified = false;

    //=======================================================================
    // Configurable
    //=======================================================================

   /**
    * Invoked by the container to establish the registry configuration.
    * @param config a component configuration
    */
    public void configure( Configuration config)
    {
        m_config = config;
        getLogger().debug("kernel configuration");
    }

    //=======================================================================
    // Initializable
    //=======================================================================

    public void initialize() throws Exception
    {
        final ContainerClassLoader loader = new ContainerClassLoader(
          new DefaultPackageRepository( 
            Fileset.expandExtensions( 
              m_config.getChild( "extensions" ) 
            ) 
          ),
          Thread.currentThread().getContextClassLoader(), 
          m_config.getChild("container").getChild("classpath"),
          getLogger().getChildLogger( m_config.getName())
        );

        DefaultContext context = new DefaultContext();
        context.put( DefaultContainer.CLASSLOADER_KEY, loader );
        context.put( DefaultContainer.MAP_KEY, new DependencyMap() );
        m_container.enableLogging( getLogger().getChildLogger("container") );
        m_container.contextualize( context );
        m_container.configure( m_config.getChild("container" ) );
        m_container.initialize( );
        m_container.verify();
    }

    //=======================================================================
    // Startable
    //=======================================================================

    public void startup() throws Exception
    {
        m_container.startup();
    }

    public void shutdown()
    {
        m_container.shutdown();
    }
}
