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
import org.apache.log.Hierarchy;
import org.apache.log.Priority;
import org.apache.log.output.io.StreamTarget;

import net.osm.merlin.registry.DefaultRegistry;

/**
 * Application bootstrap.
 *
 * @author <a href="mailto:mcconnell@apache.org">Stephen McConnell</a>
 * @version $Revision: 1.1 $ $Date: 2002/07/04 05:51:00 $
 */
public class Main
{
    //=======================================================================
    // static
    //=======================================================================

    private static final String DEFAULT_FORMAT =
        "[%7.7{priority}] (%{category}): %{message}\\n%{throwable}";
    private static final Resources REZ =
        ResourceManager.getPackageResources( DefaultRegistry.class );

   /**
    * Creation of a root type registry.
    */
    public static void main( String[] args )
    {

        final DefaultKernel kernel = new DefaultKernel();

        // get a configuration object containing the kernel profile
        // from which we can establish the logger and extensions directory

        String path = null;
        Configuration config = null;
        if( args.length > 0 )
        {
            path = args[0];
            config = getProfile( new File( path ) );
        }
        else
        {
            throw new RuntimeException("Missing kernel configuration path argument.");
        }

        // create a bootstrap logger - this needs to be replaced with
        // the Avalon Exvalibur Logkit package

        final Logger logger = getLogger( config ); 
        DefaultContext context = new DefaultContext();
        try
        {
            kernel.enableLogging( logger );
            kernel.configure( config );
            kernel.initialize( );
        }
        catch( Throwable e )
        {
            logger.error("Unexpected error while processing kernel lifecycle.", e);
            System.exit(0);
        }

        //
        // add a shutdown hook so we can stop services and target and invoke shutdown
        //

        Runtime.getRuntime().addShutdownHook(
            new Thread()
            {
                public void run()
                {
                    kernel.shutdown();
                }
            }
        );

        // invoke the registry demo
        try
        {
            kernel.startup();
        }
        catch( Throwable e )
        {
            logger.error("Kernel startup failure.", e );
            System.exit(0);
        }
    }

    private static Logger getLogger( Configuration config )
    {
        // create an internal logger for the registry
        final Hierarchy hierarchy = createHierarchy(
           Priority.getPriorityForName( 
              config.getChild("logger").getAttribute("priority","INFO") 
           )
        );
        return new LogKitLogger( hierarchy.getLoggerFor( "" ) );
    }

    private static Hierarchy createHierarchy( Priority priority )
    {
        try
        {
            Hierarchy hierarchy = Hierarchy.getDefaultHierarchy();
            hierarchy.setDefaultLogTarget(
                new StreamTarget( System.out, new AvalonFormatter( DEFAULT_FORMAT ) ) );
            hierarchy.setDefaultPriority( priority );
            return hierarchy;
        }
        catch( Throwable e )
        {
            final String error = "Unexpected exception while creating bootstrap logger.";
            throw new CascadingRuntimeException( error, e );
        }
    }

    private static Configuration getProfile( final File file )
    {
        try
        {
            DefaultConfigurationBuilder builder = new DefaultConfigurationBuilder();
            InputStream is = new FileInputStream( file );
            if( is == null )
            {
                throw new RuntimeException(
                    "Could not load the configuration resource \"" + file + "\"" );
            }
            return builder.build( is );
        }
        catch( Throwable e )
        {
            final String error = "Unable to create configuration from file: " + file;
            throw new CascadingRuntimeException( error, e );
        }
    }

}
