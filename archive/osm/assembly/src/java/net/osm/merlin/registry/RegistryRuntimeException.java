/*
 * Copyright (C) The Apache Software Foundation. All rights reserved.
 *
 * This software is published under the terms of the Apache Software License
 * version 1.1, a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */
package net.osm.merlin.registry;

import org.apache.avalon.framework.CascadingRuntimeException;

/**
 * Exception to indicate that there was a repository related runtime error.
 *
 * @author <a href="mailto:mcconnell@apache.org">Stephen McConnell</a>
 * @version $Revision: 1.1 $ $Date: 2002/07/04 05:51:29 $
 */
public final class RegistryRuntimeException
    extends CascadingRuntimeException
{

    /**
     * Construct a new <code>RegistryRuntimeException</code> instance.
     *
     * @param message The detail message for this exception.
     */
    public RegistryRuntimeException( final String message )
    {
        this( message, null );
    }

    /**
     * Construct a new <code>RegistryRuntimeException</code> instance.
     *
     * @param message The detail message for this exception.
     * @param throwable the root cause of the exception
     */
    public RegistryRuntimeException( final String message, final Throwable throwable )
    {
        super( message, throwable );
    }
}

