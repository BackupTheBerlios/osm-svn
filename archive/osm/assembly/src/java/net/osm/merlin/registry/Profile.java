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
import java.security.Policy;

import org.apache.avalon.excalibur.i18n.ResourceManager;
import org.apache.avalon.excalibur.i18n.Resources;
import org.apache.excalibur.configuration.ConfigurationUtil;
import org.apache.avalon.framework.logger.Logger;
import org.apache.avalon.framework.logger.LogEnabled;
import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.avalon.framework.parameters.Parameters;
import org.apache.avalon.framework.activity.Initializable;
import org.apache.avalon.framework.activity.Disposable;
import org.apache.avalon.framework.CascadingRuntimeException;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.DefaultConfiguration;
import org.apache.avalon.framework.configuration.DefaultConfigurationBuilder;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.context.Context;
import org.apache.avalon.framework.parameters.Parameters;
import org.apache.avalon.framework.Version;
import org.apache.avalon.excalibur.extension.PackageRepository;
import org.apache.avalon.excalibur.extension.Extension;
import org.apache.avalon.excalibur.extension.OptionalPackage;
import org.apache.excalibur.containerkit.metainfo.ComponentDescriptor;
import org.apache.excalibur.containerkit.metainfo.ComponentInfo;
import org.apache.excalibur.containerkit.metainfo.ServiceDescriptor;
import org.apache.excalibur.containerkit.metainfo.DependencyDescriptor;
import org.apache.excalibur.containerkit.metainfo.ServiceDesignator;

import org.apache.excalibur.containerkit.metadata.ComponentMetaData;
import org.apache.excalibur.containerkit.metadata.DependencyMetaData;

import net.osm.merlin.registry.Registry;
import net.osm.merlin.registry.UnresolvedProviderException;
import org.apache.excalibur.configuration.ContextFactory;
import org.apache.excalibur.configuration.ConfigurationUtil;

/**
 * The default implementation of a profile under which configuration, context
 * and parameterization criteria is associated against a component type defintion.
 *
 * @author <a href="mailto:mcconnell@apache.org">Stephen McConnell</a>
 * @version $Revision: 1.1 $ $Date: 2002/07/04 05:51:30 $
 */
public class Profile extends ComponentMetaData 
{
    private static String getAbstractName( Configuration profile )
    {
        return profile.getAttribute("name",null);
    }

    private static DependencyMetaData[] getDependencyMetaData( 
      final DefaultRegistry registry, final ComponentType type ) 
      throws UnresolvedProviderException
    {
        Vector vector = new Vector();
        DependencyDescriptor[] deps = type.getComponentInfo().getDependencies();
        for( int i=0; i<deps.length; i++ )
        {
            if( !deps[i].isOptional() )
            {
                final String role = deps[i].getRole();
                final Profile provider = registry.getCandidateProfile( deps[i] );
                DependencyMetaData data = new DependencyMetaData( 
                  role, 
                  provider.getName() 
                );
                vector.add( data );
            }
        }
        return (DependencyMetaData[]) vector.toArray( new DependencyMetaData[0] );
    }

    private final ComponentType m_type;

    private final Configuration m_profile;

    private final DefaultRegistry m_registry;

   /**
    * Creation of a default profile.
    * @param type the component type that this profile is qualifying
    */
    public Profile( final DefaultRegistry registry, final ComponentType type )
       throws ConfigurationException, UnresolvedProviderException

    {
        this( registry, type, new DefaultConfiguration("profile") );
    }

   /**
    * Creation of a profile of a component type. The 
    * configuration is a profile instance containing criteria for for the 
    * profiles default configuration, parameters and content.
    * 
    * @param type the component type that this profile is qualifying
    * @param profile a configuration instance possibly containing a context, 
    *   parameters and configuration element.
    * @param name the profile name
    */
    public Profile( 
      final DefaultRegistry registry, final ComponentType type, final Configuration profile )
      throws ConfigurationException, UnresolvedProviderException
    {
        super( 
          getAbstractName( profile ), 
          getDependencyMetaData( registry, type ), 
          Parameters.fromConfiguration( profile.getChild("paramerters") ),
          profile.getChild("configuration"),
          type.getComponentInfo() 
        );
        m_type = type;
        m_profile = profile;
        m_registry = registry;
        m_registry.install( this );
    }

   /**
    * Returns the component type bound to the profile.
    * @return the component type
    */
    public ComponentType getComponentType()
    {
        return m_type;
    }

   /**
    * Returns the context to be supplied to an instance of the profile
    * @return the profile context object
    */
    public Context getContext( Context parent )
    {
        try
        {
            Configuration criteria = m_profile.getChild("context");
            return ContextFactory.createContextFromConfiguration( parent, criteria );
        }
        catch( Throwable e )
        {
            throw new ProfileRuntimeException( 
              "Unexpected error while creating context.", e );
        }

    }
    
    public String toString()
    {
        return "DefaultProfile name: '" + getName() + "' type: " + getComponentType();
    }

   /**
    * Provide a textual report on the profile.
    * @return the formatted profile report
    */
    public String report()
    {
        StringBuffer buffer = new StringBuffer();
        buffer.append( "PROFILE REPORT\n" );
        buffer.append( "\n  name: " + getName() );
        buffer.append( "\n  type: " + getComponentType() );
        buffer.append( "\n  context: " + getContext( null ) );
        buffer.append( "\n  configuration: \n" + ConfigurationUtil.list( getConfiguration() ));
        buffer.append( "\n  parameters: " + getParameters() );
        buffer.append( "\n  dependecies" );

        DependencyMetaData[] dependencies = getDependencies();
        if( dependencies.length == 0 )
        {
            buffer.append( " (none)\n\n" );
            return buffer.toString();
        }

        for( int i=0; i<dependencies.length; i++ )
        {
            buffer.append( "\n  dependency " + i );
            buffer.append( "\n    role: " + dependencies[i].getRole() );
            buffer.append( "\n    provider: " + dependencies[i].getProviderName() );
        }

        buffer.append( "\n\n" );
        return buffer.toString();
    }

}
