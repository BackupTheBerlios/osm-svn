/**
 * File: RuntimeApplicationException.java
 * License: OSM-PSS-LICENSE.TXT
 * Copyright: OSM SARL 2002, All Rights Reserved.
 */

package net.osm.util;

import org.apache.avalon.framework.CascadingThrowable;
import org.omg.CORBA.CompletionStatus;
import org.omg.CORBA.SystemException;

/**
 * General runtime application exception with causal parameters..
 */
public class RuntimeApplicationException extends SystemException
         implements CascadingThrowable
{

    private final Throwable m_cause;


    /**
     * Constructor for the RuntimeApplicationException object
     */
    public RuntimeApplicationException()
    {
        super( null, 0, CompletionStatus.COMPLETED_MAYBE );
        m_cause = null;
    }


    /**
     * Constructor for the RuntimeApplicationException object
     *
     * @param throwable 
     */
    public RuntimeApplicationException( Throwable throwable )
    {
        super( null, 0, CompletionStatus.COMPLETED_MAYBE );
        m_cause = throwable;
    }


    /**
     * Constructor for the PersistenceException object
     *
     * @param s 
     */
    public RuntimeApplicationException( String s )
    {
        super( s, 0, CompletionStatus.COMPLETED_MAYBE );
        m_cause = null;
    }


    /**
     * Constructor for the RuntimeApplicationException object
     *
     * @param s 
     * @param throwable 
     */
    public RuntimeApplicationException( String s, Throwable throwable )
    {
        super( s, 0, CompletionStatus.COMPLETED_MAYBE );
        m_cause = throwable;
    }


    /**
     * Constructor for the RuntimeApplicationException object
     *
     * @param i 
     * @param completionstatus 
     */
    public RuntimeApplicationException( int i, CompletionStatus completionstatus )
    {
        super( null, i, completionstatus );
        m_cause = null;
    }


    /**
     * Constructor for the RuntimeApplicationException object
     *
     * @param i 
     * @param completionstatus 
     * @param throwable 
     */
    public RuntimeApplicationException( int i, CompletionStatus completionstatus, Throwable throwable )
    {
        super( null, i, completionstatus );
        m_cause = throwable;
    }


    /**
     * Constructor for the RuntimeApplicationException object
     *
     * @param s 
     * @param i 
     * @param completionstatus 
     */
    public RuntimeApplicationException( String s, int i, CompletionStatus completionstatus )
    {
        super( s, i, completionstatus );
        m_cause = null;
    }


    /**
     * Constructor for the RuntimeApplicationException object
     *
     * @param s 
     * @param i 
     * @param completionstatus 
     * @param throwable 
     */
    public RuntimeApplicationException( String s, int i, CompletionStatus completionstatus, Throwable throwable )
    {
        super( s, i, completionstatus );
        m_cause = throwable;
    }


    /**
     * Gets the cause attribute of the RuntimeApplicationException object
     *
     * @return The cause value
     */
    public Throwable getCause()
    {
        return m_cause;
    }
}
