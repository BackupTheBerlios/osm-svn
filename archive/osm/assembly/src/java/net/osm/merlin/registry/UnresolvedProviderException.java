/*
 * Copyright (C) The Apache Software Foundation. All rights reserved.
 *
 * This software is published under the terms of the Apache Software License
 * version 1.1, a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package net.osm.merlin.registry;

import org.apache.excalibur.containerkit.metainfo.DependencyDescriptor;

import org.apache.avalon.framework.CascadingException;

/**
 * Exception to indicate that a service provider could not be found.
 *
 * @author <a href="mailto:mcconnell@apache.org">Stephen McConnell</a>
 * @version $Revision: 1.1 $ $Date: 2002/07/04 05:51:29 $
 */
public final class UnresolvedProviderException
    extends CascadingException
{

    private DependencyDescriptor m_dependency;

   /**
    * Construct a new <code>UnresolvedProviderException</code> instance.
    *
    * @param dependency the unresolved dependency
    */
    public UnresolvedProviderException( String message, DependencyDescriptor dependency )
    {
        this( message, dependency, null );
    }

   /**
    * Construct a new <code>UnresolvedProviderException</code> instance.
    *
    * @param dependency the unresolved dependency
    * @param cause the causal exception
    */
    public UnresolvedProviderException( String message, DependencyDescriptor dependency, Throwable cause )
    {
        super( message, cause );
        m_dependency = dependency;
    }

    public String getMessage()
    {
        return "Could not resolve provider for dependency " 
          + m_dependency.getService() 
          + " for role: " + m_dependency.getRole() 
          + " due to " + super.getMessage();
    }
}

