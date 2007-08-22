/*
 * Copyright (C) The Apache Software Foundation. All rights reserved.
 *
 * This software is published under the terms of the Apache Software License
 * version 1.1, a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */
package net.osm.merlin.registry;

import java.util.List;
import java.util.LinkedList;
import java.util.Hashtable;
import java.util.Vector;
import java.util.Iterator;
import org.apache.avalon.framework.CascadingException;
import org.apache.avalon.framework.logger.Logger;
import org.apache.avalon.framework.logger.LogEnabled;
import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.avalon.framework.configuration.Configuration;
import net.osm.merlin.registry.ComponentType;
import net.osm.merlin.registry.Profile;
import net.osm.merlin.registry.ComponentDefinition;
import org.apache.excalibur.containerkit.metainfo.ServiceDescriptor;
import org.apache.excalibur.containerkit.metainfo.ServiceDesignator;
import org.apache.excalibur.containerkit.metainfo.DependencyDescriptor;
import org.apache.excalibur.containerkit.metainfo.ComponentInfo;
import org.apache.excalibur.containerkit.infobuilder.ComponentInfoBuilder;
import org.apache.excalibur.configuration.ConfigurationUtil;
import org.apache.excalibur.containerkit.dependency.DependencyMap;

/**
 * Internal table that holds available component type keyed relative
 * to the service it provides.
 *
 * @author <a href="mailto:mcconnell@apache.org">Stephen McConnell</a>
 * @version $Revision: 1.1 $ $Date: 2002/07/04 05:51:29 $
 */
final class ServiceRegistry extends AbstractLogEnabled
{

    //=======================================================================
    // state
    //=======================================================================

    private ComponentInfoBuilder m_infoBuilder = new ComponentInfoBuilder();

    private ClassLoader m_classloader;
    private Configuration m_profiles;
    private DefaultRegistry m_registry;

   /**
    * List of ServiceTable instances.
    */
    private List m_services = new LinkedList();

   /**
    * Component types keyed by classname.
    */
    private Hashtable m_types = new Hashtable();

   /**
    * The set of installed profiles keyed by profile name.
    */
    private Hashtable m_installed = new Hashtable();

    private DependencyMap m_map;

    //=======================================================================
    // constructor
    //=======================================================================

   /**
    * Creation of a new service registry.
    * @param registry the registry that will be supplied to new component defintions
    * @param loader the registry class loader
    * @param profiles the configuration fragment containing explicit component profiles
    */
    public ServiceRegistry( 
      DefaultRegistry registry, ClassLoader loader, DependencyMap map, Configuration profiles )
    {
        m_classloader = loader;
        m_profiles = profiles;
        m_registry = registry;
        m_map = map;
    }

    //=======================================================================
    // LogEnabled
    //=======================================================================
        
   /**
    * Set the logging channel for the service registry.
    * @param logger the logging channel
    */
    public void enableLogging( Logger logger )
    {
        super.enableLogging( logger );
        m_infoBuilder.enableLogging( logger.getChildLogger("builder") );
    }

    //=======================================================================
    // ServiceRegistry
    //=======================================================================

   /**
    * Register a potential supplier component type.  The implementation will
    * create a component type instance for the entry if not already known and 
    * return the existing or new instance to the invoking client.
    *
    * @param classname the component class name
    * @return the component type
    */
    public ComponentType register( String classname ) throws Exception
    {
        ComponentType type = getComponentType( classname );
        if( type == null )
        {
            type = createComponentDefinition( classname );
            register( type );
        }
        return type;
    }

   /**
    * Install a provider and return the profile.  The side effect of this operation
    * is the resolution of profiles for the runtime application context based on dependecies
    * declared by the component identified by the classname. 
    *
    * @param classname the classname of the provider to install
    * @param name the component profile to use for the provider
    */
    public Profile install( String classname, String name ) throws Exception
    {
        return install( register( classname ).getProfile( name ) );
    }

   /**
    * Install a provider and return the profile.
    * @param classname the classname of the provider to install
    * @param name the component profile to use for the provider
    */
    public Profile install( Profile profile )
    {
        if( m_installed.get( profile.getName() ) != null )
          return profile;
        m_installed.put( profile.getName(), profile ); 
        return profile;  
    }

   /**
    * Return the set of profiles representing the application scope.
    * @return the installed profiles
    */
    public Profile[] getInstalledProfiles()
    {
        return (Profile[]) m_installed.values().toArray( new Profile[0] );
    }

   /**
    * Returns a named profile.
    * @return the named profile
    */
    public Profile getInstalledProfile( String name )
    {
        return (Profile) m_installed.get( name );
    }


   /**
    * Returns the set of component types know to the registry.
    * @return the set of component types registered with the registry
    */
    public ComponentType[] getTypes()
    {
        return (ComponentType[]) m_types.entrySet().toArray( new ComponentType[0] );
    }

   /**
    * Returns the set of component types know to the registry that are capable of 
    * supporting the supplied service.
    * @return the set of candidate component types
    */
    public ComponentType[] getComponentTypes( ServiceDesignator service )
    {
        return getTable( service ).getTypes();
    }

   /**
    * Returns a registered component type.
    * @return the component type from the registry or null if the type is unknown
    * @exception IllegalArgumentException if the cklassname does not correpond to a know component
    */
    public ComponentType getComponentType( String classname ) throws IllegalArgumentException
    {
        return (ComponentType) m_types.get( classname );
    }

    public Profile[] getProfiles( ServiceDesignator service )
    {
        //
        // before argregating profiles, make sure that all of the components
        // getProfiles() operation has been invoked - which ensures that 
        // the type has been properly initialized (yes - it sucks but it works).
        //

        ServiceTable table = getTable( service );
        ComponentType[] types = table.getTypes();
        for( int i=0; i<types.length; i++ )
        {
            try
            {
                types[i].getProfiles();
            }
            catch( Throwable e )
            {
                getLogger().warn("skipping type: " + types[i], e  );
            }
        }

        //
        // with the pre-init of profiles complete, we can go ahead and get the 
        // set of available profiles in the knowlege that everything is behaving 
        // consitently
        //

        getLogger().debug("getting profiles for service: " + service );
        Vector vector = new Vector();
        ComponentType[] components = table.getTypes();
        //getLogger().debug("located table: = " 
        //  + table + ", size: " + ", " + components.length );
        for( int i=0; i<components.length; i++ )
        {
            try
            {
                Profile[] profiles = components[i].getProfiles();
                //getLogger().debug("  located provider: " + components[i] + ", profile: " + profiles.length);
                for( int j=0; j<profiles.length; j++ )
                {
                    vector.add( profiles[j] );
                    //getLogger().debug("    profile: " + profiles[j].getComponentType() );
                }
            }
            catch( Throwable e )
            {
                getLogger().warn("skipping type: " + components[i], e  );
            }
        }
        return (Profile[]) vector.toArray( new Profile[0] );
    }

    public Profile getCandidateProfile( DependencyDescriptor dependency )
        throws UnresolvedProviderException
    {
        Profile[] profiles = getProfiles( dependency.getService() );
        if( profiles.length == 0 )
        {
            throw new UnresolvedProviderException( "No candidates matching dependency.", dependency );
        }
        else
        {
            Selector selector = getSelector( dependency );
            if( selector == null )
            {
                return profiles[0];
            }
            else
            {
                return selector.select( profiles );
            }
        }
    }

    private void register( ComponentType type )
    {
        //getLogger().debug("registering provider type: " + getImplementationKey( type ) );
        m_types.put( getImplementationKey( type ), type );
        ServiceDescriptor[] services = type.getComponentInfo().getServices();
        for( int i=0; i<services.length; i++ )
        {
            register( services[i].getServiceDesignator(), type );
        }
    }

    private void register( ServiceDesignator service, ComponentType type )
    {
        ServiceTable table = getTable( service );
        table.add( type );
    }

    private ServiceTable getTable( ServiceDesignator service )
    {
        ServiceTable table = null;
        Iterator iterator = m_services.iterator();
        while( iterator.hasNext() )
        {
            table = (ServiceTable) iterator.next();
            if( table.matches( service ) )
              return table;
        }

        // otherwise create a new table
        table = new ServiceTable( service, getLogger().getChildLogger("table")  );
        m_services.add( table );
        return table;
    }  

   /**
    * Create a new ComponentDefintion instance.
    * @param registry the component defintion registry
    * @param classname the name of the component class
    * @return the component info instance
    * @exception AssemblyException if an assembly error occurs
    */
    private ComponentDefinition createComponentDefinition( final String classname ) 
      throws RegistryException
    {
        if( classname == null )
          throw new NullPointerException("classname");

        if( classname.indexOf("/") > -1 )
          throw new IllegalArgumentException( "invlaid classname" );

        try
        {
            ComponentInfo info = m_infoBuilder.build( classname, m_classloader );
            final String name = info.getComponentDescriptor().getName();
            Configuration[] profiles = ConfigurationUtil.match( 
              m_profiles, "component", "class", classname );
            ComponentDefinition type = new ComponentDefinition( m_registry, info, profiles );
            type.enableLogging( getLogger().getChildLogger( name ) );
            return type;
        }
        catch( Throwable e )
        {
            final String error = "Internal error while attempting to read xinfo.";
            throw new RegistryException( error, e );
        }
    }

    private Selector getSelector( DependencyDescriptor dependency )
    {

        // if the dependency declares a selector class then use that, 
        // otherwise, look for a default service type selector

        String selectorName = dependency.getAttribute("avalon.service.selector");

        if( selectorName == null )
          selectorName = dependency.getService().getClassname() + "Selector";

        try
        {
            Class clazz = m_classloader.loadClass( selectorName );
            Selector selector = (Selector) clazz.newInstance();
            if( selector instanceof LogEnabled ) 
            {
                ((LogEnabled)selector).enableLogging( getLogger().getChildLogger("selector") );
            }
            return selector;
        }
        catch( Throwable e )
        {
            return null;
        }
    }

    private String getImplementationKey( ComponentType type )
    {
        return type.getComponentInfo().getComponentDescriptor().getImplementationKey();
    }

}

