/*
 * Copyright (C) The Apache Software Foundation. All rights reserved.
 *
 * This software is published under the terms of the Apache Software License
 * version 1.1, a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */
package net.osm.merlin.registry;

import java.util.Enumeration;
import java.util.Dictionary;
import java.util.Hashtable;

import org.apache.avalon.framework.CascadingException;

/**
 * Exception to indicate that there was an error Assembling a component model.
 *
 * @author <a href="mailto:mcconnell@apache.org">Stephen McConnell</a>
 * @version $Revision: 1.1 $ $Date: 2002/07/04 05:51:32 $
 */
public final class AssemblyException
    extends CascadingException
{

    private static final Hashtable EMPTY_TABLE = new Hashtable();

    private Dictionary m_errors;

    /**
     * Construct a new <code>AssemblyException</code> instance.
     *
     * @param message The detail message for this exception.
     */
    public AssemblyException( final String message )
    {
        this( null, message, null );
    }

    /**
     * Construct a new <code>AssemblyRuntimeException</code> instance.
     *
     * @param errors a list of warning messages related to the exception.
     * @param message The detail message for this exception.
     */
    public AssemblyException( final Dictionary errors, final String message )
    {
        this( errors, message, null );
    }


    /**
     * Construct a new <code>AssemblyException</code> instance.
     *
     * @param message The detail message for this exception.
     * @param throwable the root cause of the exception
     */
    public AssemblyException( final String message, final Throwable throwable )
    {
        this( null, message, throwable );
    }

    /**
     * Construct a new <code>AssemblyException</code> instance.
     *
     * @param errors a list of warning messages related to the exception.
     * @param message The detail message for this exception.
     * @param throwable the root cause of the exception
     */
    public AssemblyException( final Dictionary errors, final String message, 
      final Throwable throwable )
    {
        super( message, throwable );
        if( errors != null )
        {
            m_errors = errors;
        }
        else
        {
            m_errors = EMPTY_TABLE;
        }
    }

   /**
    * Return the table of supplimentary messages.
    * @return the messages table
    */
    public Dictionary getDictionary()
    {
       return m_errors;
    }

   /**
    * Returns a stringified representation of the exception.
    * @return the exception as a string.
    */
    public String toString()
    {
        StringBuffer buffer = new StringBuffer();
        buffer.append( super.toString() );
        buffer.append( " Errors: " + m_errors.size() );
        Enumeration keys = m_errors.keys();
        while( keys.hasMoreElements() )
        {
             Object key = keys.nextElement();
             buffer.append( "\n  source: " + key.toString() + " cause: " + m_errors.get( key ) );
        }
        return buffer.toString();
    }
}

