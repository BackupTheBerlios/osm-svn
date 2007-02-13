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

import java.io.IOException;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletException;

import net.dpml.util.Logger;

import net.dpml.annotation.Context;
import net.dpml.annotation.Component;
import net.dpml.annotation.Services;

import static net.dpml.annotation.LifestylePolicy.SINGLETON;

import org.mortbay.jetty.Handler;
import org.mortbay.jetty.MimeTypes;
import org.mortbay.jetty.handler.ErrorHandler;
import org.mortbay.jetty.handler.HandlerCollection;

/**
 * Servlet context handler. 
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
@Component( name="context", lifestyle=SINGLETON )
//@Services( ServletContextHandler.class )
public class ContextHandler extends org.mortbay.jetty.handler.ContextHandler
{
    public interface Parts
    {
        Handler[] getHandlers();
    }
    
    private Logger m_logger;
    
   /**
    * Creation of a new servlet context handler.
    * @param logger the assigned logging channel
    * @param context the deployment context
    * @exception Exception if an instantiation error occurs
    */
    public ContextHandler( Logger logger, ContextConfiguration context, Parts parts ) throws Exception
    {
        super();
        
        m_logger = logger;
        
        m_logger.info( "establishing context " + context.getContextPath() );
        
        configure( context );
        
        HandlerCollection collection = new HandlerCollection();
        for( Handler handler : parts.getHandlers() )
        {
            if( logger.isTraceEnabled() )
            {
                logger.trace( "adding handler " + handler );
            }
            collection.addHandler( handler );
        }
        setHandler( collection );
    }

    private Logger getLogger()
    {
        return m_logger;
    }
    
    private void configure( ContextConfiguration context )
    {
        String path = context.getContextPath();
        super.setContextPath( path );
        
        ClassLoader classloader = context.getClassLoader( null );
        if( null != classloader )
        {
            super.setClassLoader( classloader );
        }
        
        String name = context.getDisplayName( null );
        if( null != name )
        {
            super.setDisplayName( name );
        }
        
        String[] hosts = context.getConnectors( null );
        if( null != hosts )
        {
            super.setConnectorNames( hosts );
        }
        
        String[] virtual = context.getVirtualHosts( null );
        if( null != virtual )
        {
            super.setVirtualHosts( virtual );
        }
        
        String[] welcome = context.getWelcomeFiles( null );
        if( null != welcome )
        {
            super.setWelcomeFiles( welcome );
        }
        
        Map map = context.getMimeTypes( null );
        if( null != map )
        {
            MimeTypes types = new MimeTypes();
            types.setMimeMap( map );
            super.setMimeTypes( types );
        }
        
        ErrorHandler errorHandler = context.getErrorHandler( null );
        if( null != errorHandler )
        {
            super.setErrorHandler( errorHandler );
        }
    }
}
