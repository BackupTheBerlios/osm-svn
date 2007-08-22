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
import net.osm.merlin.registry.ComponentType;
import net.osm.merlin.registry.Profile;
import net.osm.merlin.kernel.Container;

/**
 * A service that provides support for location of components types.
 * @author <a href="mailto:mcconnell@apache.org">Stephen McConnell</a>
 * @version $Revision: 1.1 $ $Date: 2002/07/04 05:51:30 $
 */
public interface Registry 
{
   /**
    * Default role.
    */
    static final String ROLE = Registry.class.getName();

   /**
    * Returns an array of component profiles representing candidate component types 
    * capable of acting as a service supplier relative to the supplied dependecy descriptor.
    * @param dependency the dependency descriptor to evaluate
    * @return the set of profiles
    */
    Profile[] getCandidateProfiles( DependencyDescriptor dependency );

   /**
    * Return a single preferred profile capable of supporting the supplied dependency.
    * @param dependency a consumer component dependecy declaration
    * @return the preferred candidate supplier profile or null if not candidates found
    */
    Profile getCandidateProfile( DependencyDescriptor dependency )
      throws UnresolvedProviderException;

   /**
    * Test if the registry can resolve a request for a component with the supplied classname
    * @param classname a component or service class name
    * @return TRUE if the registry can service the request
    */
    //boolean hasComponentDefinition( String classname );

   /**
    * Returns an component defintion relative to the requested classname.
    * @param classname the class name of the componet defintion to locate or create
    * @return the corresponding component defintion
    * @exception AssemblyException if an error occurs during the construction of a 
    *    new component defintion
    */
    //ComponentType getComponentDefinition( String classname ) throws RegistryException;

   /**
    * Returns an array of component definitions capable of acting as a service supplier
    * relative to the supplied dependecy descriptor.
    * @param dependency the dependency descriptor to evaluate
    * @return the set of matching service descriptors capable of supporting the dependency
    */
    //public ComponentType[] getCandidateProviders( DependencyDescriptor dependency );
}
