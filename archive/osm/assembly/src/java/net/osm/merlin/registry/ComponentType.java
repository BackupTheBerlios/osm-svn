/*
 * Copyright (C) The Apache Software Foundation. All rights reserved.
 *
 * This software is published under the terms of the Apache Software License
 * version 1.1, a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package net.osm.merlin.registry;

import org.apache.excalibur.containerkit.metainfo.DependencyDescriptor;
import org.apache.excalibur.containerkit.metainfo.ServiceDescriptor;
import org.apache.excalibur.containerkit.metainfo.ComponentInfo;
import org.apache.excalibur.containerkit.metainfo.ComponentInfo;
import org.apache.excalibur.containerkit.verifier.VerifyException;
import net.osm.merlin.registry.UnresolvedProviderException;
import org.apache.avalon.framework.configuration.ConfigurationException;

/**
 * Definition of a navigable meta information structure representing a component type.
 *  
 * @see net.osm.merlin.registry.Registry
 * @author <a href="mailto:mcconnell@apache.org">Stephen McConnell</a>
 * @version $Revision: 1.1 $ $Date: 2002/07/04 05:51:31 $
 */
public interface ComponentType
{
   /**
    * Returns the name of the component type.
    * @return an abstract name
    */
    String getName();

   /**
    * Returns the <code>ComponentInfo</code> instance describing the meta-info structure
    * for this component type.
    *
    * @return the meta-info set for the component type
    */
    ComponentInfo getComponentInfo();

   /**
    * Return the set of profiles for this component type.
    * @return the set of profiles available for this component type
    */
    Profile[] getProfiles() throws UnresolvedProviderException, ConfigurationException;

   /**
    * Return a named profile for this component type.
    * @return the named profile or null if the name doers not match a known profile
    */
    public Profile getProfile( String name ) throws UnresolvedProviderException, ConfigurationException;


}
