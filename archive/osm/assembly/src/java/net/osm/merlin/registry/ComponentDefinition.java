/*
 * Copyright (C) The Apache Software Foundation. All rights reserved.
 *
 * This software is published under the terms of the Apache Software License
 * version 1.1, a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package net.osm.merlin.registry;

import java.net.URL;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.Properties;
import java.util.Vector;
import java.util.ArrayList;
import java.util.List;
import java.util.LinkedList;
import java.util.Hashtable;

import org.apache.avalon.excalibur.i18n.ResourceManager;
import org.apache.avalon.excalibur.i18n.Resources;
import org.apache.excalibur.configuration.ConfigurationUtil;
import org.apache.avalon.framework.logger.Logger;
import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.avalon.framework.activity.Disposable;
import org.apache.avalon.framework.activity.Initializable;
import org.apache.avalon.framework.CascadingRuntimeException;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.DefaultConfigurationBuilder;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.context.Context;
import org.apache.avalon.framework.Version;
import org.apache.excalibur.containerkit.metainfo.ContextDescriptor;
import org.apache.excalibur.containerkit.metainfo.ComponentDescriptor;
import org.apache.excalibur.containerkit.metainfo.ComponentInfo;
import org.apache.excalibur.containerkit.metainfo.ServiceDescriptor;
import org.apache.excalibur.containerkit.metainfo.DependencyDescriptor;
import org.apache.excalibur.containerkit.verifier.ComponentVerifier;
import org.apache.excalibur.containerkit.verifier.VerifyException;
import org.apache.excalibur.containerkit.metadata.ComponentMetaData;
import org.apache.excalibur.containerkit.metadata.DependencyMetaData;

import net.osm.merlin.registry.Registry;
import net.osm.merlin.registry.UnresolvedProviderException;

/**
 * <p>Provides support for the navigation across <code>ComponentDefinition</code> instances 
 * implied by service and dependency relationships.  
 * @author <a href="mailto:mcconnell@apache.org">Stephen McConnell</a>
 * @version $Revision: 1.1 $ $Date: 2002/07/04 05:51:31 $
 */
class ComponentDefinition extends AbstractLogEnabled implements ComponentType
{
    //=======================================================================
    // static
    //=======================================================================

    private static final Resources REZ =
        ResourceManager.getPackageResources( ComponentDefinition.class );

    private static final Profile[] EMPTY_PROFILES = new Profile[0];

    //=======================================================================
    // state
    //=======================================================================

   /**
    * The registry against which dependecies can be resolved.
    */
    private DefaultRegistry m_registry;

   /**
    * The compontent info instance backing the type defintion.
    */
    private ComponentInfo m_component;

   /**
    * The set of factory profile criteria supplied for this type.
    */
    private Configuration[] m_criteria;

   /**
    * The set of factory profiles derived from the supplied criteria.
    */
    private Hashtable m_profiles = new Hashtable();

   /**
    * Interal flag indicating that profile initialization has been 
    * applied.
    */
    private boolean m_init = false;


    //=======================================================================
    // constructor
    //=======================================================================

   /**
    * Creation of a new ComponentDefinition.
    * @param registry the component registry
    * @param component a component info descriptor
    * @param profiles a set of configuration fragments describing component profiles
    *    declared for this component type
    */
    public ComponentDefinition( 
      DefaultRegistry registry, ComponentInfo component, Configuration[] profiles )
      throws ConfigurationException
    {
        m_registry = registry;
        m_component = component;
        m_criteria = profiles;
    }

    //=======================================================================
    // Initializable
    //=======================================================================

   /**
    * Initialization of the component type during which the validation of 
    * type and integrity of dependecies is undertaken.  Initialization failure 
    * may occur is a dependecy cannot be resolved.
    * @exception Initializable
    */
    private void initialize() throws UnresolvedProviderException, ConfigurationException
    {
        if( m_init ) return;
        if( m_criteria.length == 0 )
        {
            // create an implicit profile
            Profile profile = new Profile( m_registry, this );
            m_profiles.put( profile.getName(), profile );
        }
        else
        {
            // use the explicit profiles
            for( int i=0; i<m_criteria.length; i++ )
            {
                Profile profile = new Profile( m_registry, this, m_criteria[i] );
                m_profiles.put( profile.getName(), profile );
            }
        }
        m_init = true;
    }

    //=======================================================================
    // ComponentType
    //=======================================================================

   /**
    * Convinience operation to construct an abstract name representing this 
    * component defintion.
    * @return an abstract name
    */
    public String getName()
    {
        return getComponentInfo().getComponentDescriptor().getImplementationKey() + ":" 
          + m_component.getComponentDescriptor().getVersion();
    }

   /**
    * Returns the <code>ComponentInfo</code> instance describing the meta-info structure
    * for this component type.
    *
    * @return the meta-info set for the component type
    */
    public ComponentInfo getComponentInfo()
    {
        return m_component;
    }

   /**
    * Return the set of profiles for this component type.
    * @return the set of profiles available for this component type
    */
    public Profile[] getProfiles() 
      throws UnresolvedProviderException, ConfigurationException
    {
        initialize();
        return (Profile[]) m_profiles.values().toArray( EMPTY_PROFILES );
    }

   /**
    * Return a named profile for this component type.
    * @return the named profile or null if the name doers not match a known profile
    */
    public Profile getProfile( String name ) 
      throws UnresolvedProviderException, ConfigurationException
    {
        initialize();
        return (Profile) m_profiles.get( name );
    }

   /**
    * Returns a stringified representation of the component definition.
    * @return string representation of the conponent type.
    */
    public String toString()
    {
        return getComponentInfo().getComponentDescriptor().getImplementationKey() + "/" 
          + getComponentInfo().getComponentDescriptor().getVersion() + "/"
          + " (" + getComponentInfo().getComponentDescriptor().getName() + ")";
    }
}
