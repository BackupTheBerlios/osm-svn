/*
 * Copyright (C) The Apache Software Foundation. All rights reserved.
 *
 * This software is published under the terms of the Apache Software License
 * version 1.1, a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package net.osm.merlin.kernel;

import org.apache.avalon.framework.configuration.Configuration;
import java.net.MalformedURLException;
import java.net.URL;
import java.io.File;
import java.util.Vector;
import java.util.StringTokenizer;


/**
 * Provides support for general manipulation of configuration fragments
 * related to classpath and fileset declarations.
 * @author <a href="mailto:mcconnell@apache.org">Stephen McConnell</a>
 * @version $Revision: 1.1 $ $Date: 2002/07/04 05:51:00 $
 */
class Fileset 
{
    //=======================================================================
    // static
    //=======================================================================

    public static File[] expandExtensions( Configuration conf )
    {
        Vector vector = new Vector();
        Configuration[] dirsets = conf.getChildren("dirset");
        for( int i=0; i<dirsets.length; i++ )
        {
            expandDirSetToVector( vector, dirsets[i] );
        }
        return (File[]) vector.toArray( new File[ vector.size() ] );
    }


    public static URL[] expandClasspath( Configuration conf )
    {
        Vector vector = new Vector();
        Configuration[] filesets = conf.getChildren("fileset");
        for( int i=0; i<filesets.length; i++ )
        {
            expandFileSetToVector( vector, filesets[i] );
        }
        return (URL[]) vector.toArray( new URL[ vector.size() ] );
    }

    public static URL[] expandFileSet( Configuration conf )
    {
        Vector vector = new Vector();
        expandFileSetToVector( vector, conf );
        return (URL[]) vector.toArray( new URL[ vector.size() ] );
    }

    private static void expandDirSetToVector( Vector vector, Configuration dirset )
    {
        File base = new File( dirset.getAttribute("dir", System.getProperty("user.dir") ) );
        if( !base.isDirectory() )
          throw new IllegalArgumentException("Base dir does not refer to a directory in path: " + base );
        Configuration[] includes = dirset.getChildren("include");
        for( int i=0; i<includes.length; i++ )
        {
            String name = includes[i].getAttribute("name", null );
            if( name == null ) 
              throw new IllegalArgumentException(
                "Include does not contain the name attribute: " + includes[i].getLocation() );
            File file = new File( base, name );
            if( file.isDirectory() )
            {
                vector.add( file );
            }
            else
            {
               throw new IllegalArgumentException(
                "Include dir does not refer to a directory: " + file + ", base: " + base );
            }
        }
    }

    private static void expandFileSetToVector( Vector vector, Configuration conf )
    {
        File base = new File( conf.getAttribute("dir", System.getProperty("user.dir") ) );
        if( !base.isDirectory() )
          throw new IllegalArgumentException("Base dir does not refer to a directory in path: " + base );
        
        Configuration[] includes = conf.getChildren("include");
        for( int i=0; i<includes.length; i++ )
        {
            String name = includes[i].getAttribute("name", null );
            if( name == null ) 
              throw new IllegalArgumentException(
                "Include does not contain the name attribute: " + includes[i].getLocation() );
            File file = new File( base, name );
            if( !file.exists() ) 
              throw new IllegalArgumentException(
                "Include references file that does not exist: " + includes[i].getLocation() );
            try
            {
                vector.add( file.toURL() );
            }
            catch( Throwable e )
            {
              throw new IllegalArgumentException(
                "Could not convert include to a URL: " + includes[i].getLocation() );
            }
        }
    }


    public static String[] splitPath( String path, String token )
    {
        StringTokenizer tokenizer = new StringTokenizer( path, token );
        Vector vector = new Vector();
        while( tokenizer.hasMoreElements() )
        {
            vector.add( tokenizer.nextToken() );
        }
        return (String[]) vector.toArray( new String[ vector.size() ] );
    }

    public static File[] expandPath( String[] path )
    {
        Vector vector = new Vector();
        for( int i=0; i<path.length; i++ )
        {
            File file = new File( path[i] );
            vector.add( file );
            if( file.isDirectory() )
            {
                expandDirectory( vector, file );
            }
        }
        return (File[]) vector.toArray( new File[ vector.size() ] );
    }

    private static void expandDirectory( Vector vector, File dir )
    {
        if( !dir.isDirectory() ) return;
        final File[] files = dir.listFiles();
        for( int i=0; i<files.length; i++ )
        {
            File file = files[i];
            vector.add( file ); 
            if( file.isDirectory() )
            {
                expandDirectory( vector, file );
            }
        }
    }

    public static String[] urlsToStrings( URL[] urls )
    {
        Vector vector = new Vector();
        for( int i=0; i<urls.length; i++ )
        {
            vector.add( urls[i].toString() );
        }
        return (String[]) vector.toArray( new String[ vector.size() ] );
    }


    public static URL[] fileToURL( File[] files ) throws MalformedURLException
    {
        Vector vector = new Vector();
        for( int i=0; i<files.length; i++ )
        {
            vector.add( files[i].toURL() );
        }
        return (URL[]) vector.toArray( new URL[ vector.size() ] );
    }
}
