/*
 * Copyright (C) The Apache Software Foundation. All rights reserved.
 *
 * This software is published under the terms of the Apache Software License
 * version 1.1, a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package net.osm.merlin.registry;

import net.osm.merlin.registry.Profile;


/**
 * Interface implemented by selection services that can provide a sorted 
 * set of profiles for a paricular service type.
 *
 * @author <a href="mailto:mcconnell@apache.org">Stephen McConnell</a>
 * @version $Revision: 1.1 $ $Date: 2002/07/04 05:51:29 $
 */
public interface Selector
{
   /**
    * Returns the preferred profile for an available selection of provider profiles.
    * @param profiles the set of profiles of potential service providers
    * @return the preferred provider or null if no satisfactory provider can be established 
    *    from the supplied profiles.
    */
    Profile select( Profile[] profiles ) throws UnresolvedProviderException;
}
