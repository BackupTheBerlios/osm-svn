/*
 * Copyright (C) The Apache Software Foundation. All rights reserved.
 *
 * This software is published under the terms of the Apache Software License
 * version 1.1, a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */
package net.osm.sps;

/**
 * EventBuffer supports FIFI order of events pending delivery.
 */
public final class EventBuffer
{
    protected Object[] m_buffer;
    protected int m_head;
    protected int m_tail;

    /**
     * Initialize the EventBuffer with the specified number of elements.  The
     * integer must be a positive integer.
     */
    public EventBuffer( int size )
    {
        m_buffer = new Object[ size + 1 ];
        m_head = 0;
        m_tail = 0;
    }

    /**
     * Initialize the EventBuffer with the default number of elements.  
     *
     * <pre>
     *   new EventBuffer( 32 );
     * </pre>
     */
    public EventBuffer()
    {
        this( 32 );
    }

    /**
     * Tests to see if the buffer is empty.
     */
    public final boolean isEmpty()
    {
        return ( size() == 0 );
    }

    /**
     * Returns the number of elements stored in the buffer.
     * @return int the buffer size
     */
    public final int size()
    {
        int size = 0;

        if( m_tail < m_head )
        {
            size = m_buffer.length - m_head + m_tail;
        }
        else
        {
            size = m_tail - m_head;
        }

        return size;
    }

    /**
     * Add an object to the end of the buffer
     * @param o the object to add to the buffer
     * @exception NullPointerException if the supplied object is null
     */
    public final void add( final Object o )
    {
        if( null == o )
        {
            throw new NullPointerException( "Attempted to add null object to buffer" );
        }

        if( size() + 1 >= m_buffer.length )
        {
            Object[] tmp = new Object[ ( ( m_buffer.length - 1 ) * 2 ) + 1 ];

            int j = 0;
            for( int i = m_head; i != m_tail; )
            {
                tmp[ j ] = m_buffer[ i ];
                m_buffer[ i ] = null;

                j++;
                i++;
                if( i == m_buffer.length )
                {
                    i = 0;
                }
            }

            m_buffer = tmp;
            m_head = 0;
            m_tail = j;
        }

        m_buffer[ m_tail ] = o;
        m_tail++;
        if( m_tail >= m_buffer.length )
        {
            m_tail = 0;
        }
    }

    /**
     * Removes the next object from the buffer
     * @return the object from the top of the buffer
     * @exception IllegalStateException if the buffer is empty
     */
    public Object remove()
    {
        if( isEmpty() )
        {
            throw new IllegalStateException( "The buffer is already empty" );
        }

        Object element = m_buffer[ m_head ];

        if( null != element )
        {
            m_buffer[ m_head ] = null;

            m_head++;
            if( m_head >= m_buffer.length )
            {
                m_head = 0;
            }
        }

        return element;
    }
}

