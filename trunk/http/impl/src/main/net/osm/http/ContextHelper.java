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

import java.util.Map;

import net.dpml.logging.Logger;

import org.mortbay.jetty.MimeTypes;
import org.mortbay.jetty.handler.ContextHandler;
import org.mortbay.jetty.handler.ErrorHandler;

/**
 * Utility class that provides support for parameterization of a Jetty context instance.
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
class ContextHelper
{
    private Logger m_logger;
    
    ContextHelper( Logger logger )
    {
        m_logger = logger;
    }
    
   /**
    * Contextualize a handler using a supplied context.
    * @param handler the handler to contextualize
    * @param context the context instance
    */
    void contextualize( ContextHandler handler, ContextHandlerContext context ) throws Exception
    {
        String path = context.getContextPath();
        handler.setContextPath( path );
        getLogger().debug( "setting context path: " + path );
        
        ClassLoader classloader = context.getClassLoader( null );
        if( null != classloader )
        {
            handler.setClassLoader( classloader );
        }
        
        String name = context.getDisplayName( null );
        if( null != name )
        {
            handler.setDisplayName( name );
        }
        
        String[] hosts = context.getConnectors( null );
        if( null != hosts )
        {
            handler.setConnectors( hosts );
        }
        
        String[] virtual = context.getVirtualHosts( null );
        if( null != virtual )
        {
            handler.setVirtualHosts( virtual );
        }
        
        String[] welcome = context.getWelcomeFiles( null );
        if( null != welcome )
        {
            handler.setWelcomeFiles( welcome );
        }
        
        Map map = context.getMimeTypes( null );
        if( null != map )
        {
            MimeTypes types = new MimeTypes();
            types.setMimeMap( map );
            handler.setMimeTypes( types );
        }
        
        ErrorHandler errorHandler = context.getErrorHandler( null );
        if( null != errorHandler )
        {
            handler.setErrorHandler( errorHandler );
        }
    }
    
    Logger getLogger()
    {
        return m_logger;
    }
}
