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
package net.osm.http.impl;

import java.io.IOException;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletException;

import net.dpml.util.Logger;

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
public class ContextHandler extends org.mortbay.jetty.handler.ContextHandler
{
   /**
    * Deployment context for a generic context handler.
    */
    public interface Context
    {
       /**
        * Get the http context resource base.  The value may contain symbolic
        * property references and should resolve to a local directory.
        *
        * @param base the default base value
        * @return the resource base
        */
        String getResourceBase( String base );
        
       /**
        * Get the array of virtual hosts.
        * @param hosts the default virtual host array
        * @return the resolved virtual host array
        */
        String[] getVirtualHosts( String[] hosts );
        
       /**
        * Get the array of connectors. The function returns the set of 
        * connectors in the form <tt>host:port</tt>.
        * @param connectors the default connector array
        * @return the resolved host array
        */
        String[] getConnectors( String[] connectors );
        
       /**
        * Get the array of welcome files.
        * @param values the default welcome file values
        * @return the resolved array of welcome files
        */
        String[] getWelcomeFiles( String[] values );
        
       /**
        * Get the classloader to assign to the context handler.
        * @param classloader the default classloader
        * @return the resolved classloader
        */
        ClassLoader getClassLoader( ClassLoader classloader );
    
       /**
        * Get the context path under which the http context instance will 
        * be associated.
        *
        * @return the assigned context path
        */
        String getContextPath();
    
       /**
        * Get the context handler display name.
        * @param name the default name
        * @return the resolved name
        */
        String getDisplayName( String name );
    
       /**
        * Get the mime type mapping.
        * @param map the default value
        * @return the resolved value
        */
        Map getMimeTypes( Map map );
    
       /**
        * Get the assigned error handler.
        * @param handler the default handler
        * @return the resolved handler
        */
        ErrorHandler getErrorHandler( ErrorHandler handler );
    }

   /**
    * Internal parts management interface for a generic request context handler.
    */
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
    public ContextHandler( Logger logger, Context context, Parts parts ) throws Exception
    {
        super();
        
        m_logger = logger;
        
        m_logger.info( "establishing context " + context.getContextPath() );
        
        contextualize( logger, this, context );
        
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
    
   /**
    * Contextualize a handler using a supplied context.
    * @param handler the handler to contextualize
    * @param context the context instance
    */
    public static void contextualize( 
      Logger logger, org.mortbay.jetty.handler.ContextHandler handler, Context context ) throws Exception
    {
        String path = context.getContextPath();
        handler.setContextPath( path );
        logger.debug( "setting context path: " + path );
        
        ErrorHandler defaultErrorHandler = new net.osm.http.impl.ErrorHandler();
        ErrorHandler errorHandler = context.getErrorHandler( defaultErrorHandler );
        handler.setErrorHandler( errorHandler );
        
        ClassLoader classloader = context.getClassLoader( null );
        if( null != classloader )
        {
            handler.setClassLoader( classloader );
        }
        
        String base = context.getResourceBase( null );
        if( null != base )
        {
            handler.setResourceBase( base );
        }
        
        String name = context.getDisplayName( null );
        if( null != name )
        {
            handler.setDisplayName( name );
        }
        
        String[] hosts = context.getConnectors( null );
        if( null != hosts )
        {
            handler.setConnectorNames( hosts );
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
    }
}
