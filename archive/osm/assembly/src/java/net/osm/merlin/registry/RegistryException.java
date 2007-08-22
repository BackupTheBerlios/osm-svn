/*
 * Copyright (C) The Apache Software Foundation. All rights reserved.
 *
 * This software is published under the terms of the Apache Software License
 * version 1.1, a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */
package net.osm.merlin.registry;

import org.apache.avalon.framework.CascadingException;

/**
 * Exception to indicate that there was a repository related error.
 *
 * @author <a href="mailto:mcconnell@apache.org">Stephen McConnell</a>
 * @version $Revision: 1.1 $ $Date: 2002/07/04 05:51:29 $
 */
public final class RegistryException
    extends CascadingException
{

    /**
     * Construct a new <code>RegistryException</code> instance.
     *
     * @param message The detail message for this exception.
     */
    public RegistryException( final String message )
    {
        this( message, null );
    }

    /**
     * Construct a new <code>AssemblyException</code> instance.
     *
     * @param message The detail message for this exception.
     * @param throwable the root cause of the exception
     */
    public RegistryException( final String message, final Throwable throwable )
    {
        super( message, throwable );
    }
}

