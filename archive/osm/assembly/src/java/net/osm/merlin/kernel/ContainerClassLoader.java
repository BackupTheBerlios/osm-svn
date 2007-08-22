/*
 * Copyright (C) The Apache Software Foundation. All rights reserved.
 *
 * This software is published under the terms of the Apache Software License
 * version 1.1, a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */
package net.osm.merlin.kernel;

import java.io.File;
import java.io.IOException;
import java.util.jar.Attributes;
import java.util.jar.Manifest;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Enumeration;
import java.util.Vector;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;
import java.util.List;
import java.util.LinkedList;
import java.net.URLClassLoader;
import java.net.URL;
import java.net.JarURLConnection;

import org.apache.avalon.framework.logger.Logger;
import org.apache.avalon.framework.logger.LogEnabled;
import org.apache.avalon.framework.CascadingRuntimeException;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.excalibur.i18n.ResourceManager;
import org.apache.avalon.excalibur.i18n.Resources;
import org.apache.avalon.excalibur.extension.Extension;
import org.apache.avalon.excalibur.extension.PackageManager;
import org.apache.avalon.excalibur.extension.PackageRepository;
import org.apache.avalon.excalibur.extension.OptionalPackage;

import net.osm.merlin.registry.*;

/**
 * Classloader for an assembly of components.
 *
 * @author <a href="mailto:mcconnell@apache.org">Stephen McConnell</a>
 * @version $Revision: 1.1 $ $Date: 2002/07/04 05:51:00 $
 */ 

public class ContainerClassLoader extends URLClassLoader implements LogEnabled
{

    //===================================================================
    // static
    //===================================================================

    private static final Resources REZ =
        ResourceManager.getPackageResources( ContainerClassLoader.class );

    //===================================================================
    // state
    //===================================================================

    private File[] m_stack;

   /**
    * List of names of block implementation classes.
    */
    private final List m_blocks = new LinkedList();

    private PackageManager m_manager;

    private Logger m_logger;

    //===================================================================
    // constructor
    //===================================================================

    ContainerClassLoader( ClassLoader parent, Configuration classpath, Logger logger )
    {
        this( null, parent, classpath, logger );
    }

    ContainerClassLoader( 
      PackageRepository repository, ClassLoader parent, Configuration classpath, Logger logger )
    {
        super( new URL[ 0 ], parent );
        m_logger = logger;
        if( repository != null )
          m_manager = new PackageManager( repository );
        addClasspath( classpath );
    }

    public void addClasspath( Configuration classpath )
    {
        try
        {
            URL[] urls = Fileset.expandClasspath( classpath );
            for( int i = 0; i < urls.length; i++ )
            {
                addURL( urls[ i ] );
            }
            String[] path = Fileset.urlsToStrings( urls );
            final File[] extensions = getOptionalPackagesFor( path );
            for( int i = 0; i < extensions.length; i++ )
            {
                addURL( extensions[ i ].toURL() );
            }
        }
        catch( Throwable e )
        {
            final String error = "Unexpected exception while creating classloader";
            throw new RegistryRuntimeException( error, e );
        }
    }

    //===================================================================
    // LogEnabled
    //===================================================================

    public void enableLogging( Logger logger )
    {
        m_logger = logger;
    }

    public Logger getLogger()
    {
        return m_logger;
    }

    //===================================================================
    // ClassLoader
    //===================================================================

   /**
    * 
    */
    protected void addURL( URL url )
    {
        try
        {
            super.addURL( url );
            getLogger().debug("scanning: " + url );    
            String[] entries = getBlocksEntries( url );
            for( int i=0; i<entries.length; i++ )
            {
                getLogger().debug("component: " + entries[i] );
                m_blocks.add( entries[i] );
            }
        }
        catch( Throwable e )
        {
            throw new CascadingRuntimeException(
              "Unexpected error while attempting to add classloader URL: " + url, e );
        }
    }

    //===================================================================
    // ClassLoader extensions
    //===================================================================

   /**
    * Returns TRUE is the component classname is know by the classloader.
    * @return a component availablity status
    */
    public boolean hasComponent( String classname )
    {
        return m_blocks.contains( classname );
    }

   /**
    * Return the array of component implementation class names within the scope
    * of the classloader.
    * @return the block names array
    */
    public String[] getComponentClassnames()
    {
        return (String[]) m_blocks.toArray( new String[0] );
    }

    //===================================================================
    // internals
    //===================================================================

    /**
     * Returns an array of <code>String</code>s corresponding to the set of classnames
     * where each classname is a declared block within the supplied jar file.
     * @param file a jar file
     * @exception RegistryRuntimeException if a general exception occurs
     */
    private String[] getBlocksEntries( URL base )
        throws RegistryRuntimeException
    {
        Vector vector = new Vector();
        try
        {
            final URL url = new URL( "jar:" + base.toString() + "!/" );
            final JarURLConnection jar = (JarURLConnection)url.openConnection();
            final Map map = jar.getManifest().getEntries();
            final Iterator iterator = map.keySet().iterator();
            while( iterator.hasNext() )
            {
                final String name = (String)iterator.next();
                final Attributes attributes = (Attributes)map.get( name );
                final Iterator it = attributes.keySet().iterator();
                while( it.hasNext() )
                {
                    final Object entry = it.next();
                    if( entry.toString().equals( "Avalon-Block" ) )
                    {
                        if( attributes.get( entry ).equals( "true" ) )
                        {
                            vector.add( name.substring( 0, name.indexOf( ".class" ) ) );
                        }
                    }
                }
            }
        }
        catch( IOException e )
        {
            final String error = "IO exception while attempt to read block manifest.";
            throw new RegistryRuntimeException( error, e );
        }
        catch( Throwable e )
        {
            final String error = "Unexpected exception while inspecting manifest on file: ";
            throw new RegistryRuntimeException( error + base, e );
        }
        finally
        {
            return (String[]) vector.toArray( new String[0] );
        }
    }

    /**
     * Retrieve the files for the optional packages required by
     * the jars in ClassPath.
     *
     * @param classPath the Classpath array
     * @return the files that need to be added to ClassLoader
     */
    private File[] getOptionalPackagesFor( final String[] classPath )
        throws Exception
    {

        if( m_manager == null )
        {
            ClassLoader parent = getParent();
            if( parent != null )
            {
                if( parent instanceof ContainerClassLoader ) 
                {
                    return ((ContainerClassLoader)parent).getOptionalPackagesFor( classPath );
                }
            }
            else
            {
                return new File[0];
            }
        }

        final Manifest[] manifests = getManifests( classPath );
        final Extension[] available = Extension.getAvailable( manifests );
        final Extension[] required = Extension.getRequired( manifests );

        final ArrayList dependencies = new ArrayList();
        final ArrayList unsatisfied = new ArrayList();

        m_manager.scanDependencies( required,
                                           available,
                                           dependencies,
                                           unsatisfied );

        if( 0 != unsatisfied.size() )
        {
            final int size = unsatisfied.size();
            for( int i = 0; i < size; i++ )
            {
                final Extension extension = (Extension)unsatisfied.get( i );
                final Object[] params = new Object[]
                {
                    extension.getExtensionName(),
                    extension.getSpecificationVendor(),
                    extension.getSpecificationVersion(),
                    extension.getImplementationVendor(),
                    extension.getImplementationVendorID(),
                    extension.getImplementationVersion(),
                    extension.getImplementationURL()
                };
                final String message = REZ.format( "missing.extension", params );
                getLogger().warn( message );
            }

            final String message =
                REZ.getString( "unsatisfied.extensions", new Integer( size ) );
            throw new Exception( message );
        }

        final OptionalPackage[] packages =
            (OptionalPackage[])dependencies.toArray( new OptionalPackage[ 0 ] );
        return OptionalPackage.toFiles( packages );
    }

    private Manifest[] getManifests( final String[] classPath )
        throws Exception
    {
        final ArrayList manifests = new ArrayList();

        for( int i = 0; i < classPath.length; i++ )
        {
            final String element = classPath[ i ];

            if( element.endsWith( ".jar" ) )
            {
                try
                {
                    final URL url = new URL( "jar:" + element + "!/" );
                    final JarURLConnection connection = (JarURLConnection)url.openConnection();
                    final Manifest manifest = connection.getManifest();
                    manifests.add( manifest );
                }
                catch( final IOException ioe )
                {
                    final String message = REZ.getString( "bad-classpath-entry", element );
                    getLogger().warn( message );
                    throw new Exception( message );
                }
            }
        }

        return (Manifest[])manifests.toArray( new Manifest[ 0 ] );
    }

}
