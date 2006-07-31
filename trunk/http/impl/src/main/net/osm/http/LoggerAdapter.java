/*
 * Copyright 2006 Stephen McConnell.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.osm.http;

import net.dpml.logging.Logger;

import net.dpml.util.ExceptionHelper;

/**
 * Wrapper to redirect Jetty logging to DPML logging.
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public class LoggerAdapter implements org.mortbay.log.Logger
{    
    private static Logger m_LOGGER;
    
    static void setRootLogger( Logger logger )
    {
        if( null == m_LOGGER )
        {
            m_LOGGER = logger;
            System.setProperty( "org.mortbay.log.class", LoggerAdapter.class.getName() );
            m_LOGGER.debug( "logging adapter established" );
        }
    }
    
    private final Logger m_logger;
    
   /**
    * Creation of a new logger adapter.
    */
    public LoggerAdapter()
    {
        if( null == m_LOGGER )
        {
            throw new IllegalStateException( "m_LOGGER not initialized." );
        }
        else
        {
            m_logger = m_LOGGER;
        }
    }
    
   /**
    * Creation of a new logger adapter.
    * @param logger the underlying log channel
    */
    LoggerAdapter( Logger logger )
    {
        m_logger = logger;
    }
    
   /**
    * Get the debug enabled status.
    * @return true if debug is enabled
    */
    public boolean isDebugEnabled()
    {
        return m_logger.isDebugEnabled();
    }
    
   /**
    * Set the debug enabled status.
    * @param flag true if debug is enabled
    */
    public void setDebugEnabled( boolean flag )
    {
        // ignore
    }
    
   /**
    * Publish an info level log message.
    * @param msg the message
    * @param arg0 an intial argument
    * @param arg1 a subsequent argument
    */
    public void info( String msg, Object arg0, Object arg1 )
    {
        if( m_logger.isInfoEnabled() )
        {
            String message = format( msg, arg0, arg1 );
            m_logger.info( message );
        }
    }
    
   /**
    * Publish an debug level log message.
    * @param message the message
    * @param cause an exception
    */
    public void debug( String message, Throwable cause )
    {
        if( isDebugEnabled() )
        {
            if( null == cause )
            {
                m_logger.debug( message );
            }
            else
            {
                String error = ExceptionHelper.packException( message, cause, false );
                m_logger.debug( error );
            }
        }
    }
    
   /**
    * Publish an debug level log message.
    * @param msg the message
    * @param arg0 an intial argument
    * @param arg1 a subsequent argument
    */
    public void debug( String msg, Object arg0, Object arg1 )
    {
        if( isDebugEnabled() )
        {
            String message = format( msg, arg0, arg1 );
            m_logger.debug( message );
        }
    }
    
   /**
    * Publish an warning level log message.
    * @param msg the message
    * @param arg0 an intial argument
    * @param arg1 a subsequent argument
    */
    public void warn( String msg, Object arg0, Object arg1 )
    {
        if( m_logger.isWarnEnabled() )
        {
            String message = format( msg, arg0, arg1 );
            m_logger.warn( message );
        }
    }
    
   /**
    * Publish an warning level log message.
    * @param message the message
    * @param error an exception
    */
    public void warn( String message, Throwable error )
    {
        if( m_logger.isWarnEnabled() )
        {
            m_logger.warn( message, error );
        }
    }

    private String format( String msg, Object arg0, Object arg1 )
    {
        int i0 = msg.indexOf( "{}" );
        
        int i1 = 0;
        if( i0 < 0 )
        { 
            i1 = -1;
        }
        else
        {
            i1 = msg.indexOf( "{}" , i0 + 2 );
        }
        
        if( ( arg1 != null ) && ( i1 >= 0 ) )
        {
            msg = 
              msg.substring( 0, i1 ) 
              + arg1 
              + msg.substring( i1 + 2 );
        }
        if( ( arg0 != null ) && ( i0 >= 0 ) )
        {
            msg = 
              msg.substring( 0, i0 )
              + arg0
              + msg.substring( i0 + 2 );
        }
        return msg;
    }
    
   /**
    * Create a logger matching the supplied category.
    * @param category the category name
    * @return the logging channel
    */
    public org.mortbay.log.Logger getLogger( String category )
    {
        Logger logger = m_LOGGER.getChildLogger( category );
        return new LoggerAdapter( logger );
    }
    
    private String trim( String path )
    {
        if( path.startsWith( "." ) )
        {
            return trim( path.substring( 1 ) );
        }
        else if( ".".equals( path ) )
        {
            return "";
        }
        else
        {
            return path;
        }
    }
    
   /**
    * Return a string representation of this logger.
    * @return the string value
    */
    public String toString()
    {
        return "net.dpml.logging.Logger";
    }

}

