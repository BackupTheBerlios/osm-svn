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

import dpml.lang.ContextInvocationHandler;

import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Hashtable;

import net.dpml.annotation.Context;
import net.dpml.annotation.Component;
import net.dpml.annotation.Services;
import net.dpml.annotation.Parts;

import static net.dpml.annotation.LifestylePolicy.SINGLETON;

import javax.servlet.http.HttpServletRequest;

import net.dpml.util.Logger;

import org.mortbay.jetty.Connector;
import org.mortbay.jetty.Handler;
import org.mortbay.jetty.handler.HandlerCollection;
import org.mortbay.jetty.handler.ContextHandler;
import org.mortbay.jetty.handler.ContextHandlerCollection;
import org.mortbay.jetty.handler.DefaultHandler;
import org.mortbay.jetty.security.UserRealm;
import org.mortbay.thread.ThreadPool;
import org.mortbay.xml.XmlConfiguration;

import net.osm.http.impl.SelectChannelConnector.SelectChannelContext;
import net.osm.http.impl.SslSocketConnector.HttpsContext;
import net.osm.http.impl.HashUserRealm.RealmContext;
import net.osm.http.impl.NCSARequestLogHandler.NCSAContext;

/**
 * HTTP server implementation.
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
@Component( name="server", lifestyle=SINGLETON )
public class DefaultServer extends org.mortbay.jetty.Server
{
    @Context
    public interface ServerContext
    {
        PoolConfiguration getThreads( PoolConfiguration context );
        
        SelectChannelContext getHttp( SelectChannelContext context );
        
        HttpsContext getHttps( HttpsContext context );
        
        RealmContext getRealm( RealmContext context );
        
        NCSAContext getLog( NCSAContext context );
    }
    
   /**
    * Internal parts management interface.
    */
    @Parts
    public interface ServerConfiguration
    {
        ContextHandler[] getContextHandlers();
    }
    
    private final Logger m_logger;
    private final ContextHandlerCollection m_contextHandlers = 
      new ContextHandlerCollection();
    private final ServerConfiguration m_parts;

   /**
    * Creation of a new HTTP server implementation.
    * @param logger the assigned logging channel
    * @param context the assigned deployment context
    * @exception Exception if an instantiation error occurs
    */
    public DefaultServer( Logger logger, ServerContext context, ServerConfiguration config ) throws Exception
    {
        super();
        
        m_logger = logger;
        m_parts = config;
        
        if( getLogger().isDebugEnabled() )
        {
            getLogger().debug( "commencing http server deployment" );
        }
        Logger internal = logger.getChildLogger( "jetty" );
        LoggerAdapter.setRootLogger( internal );
        Thread.currentThread().setContextClassLoader( getClass().getClassLoader() );
        
        //
        // setup the thread pool
        //
        
        PoolConfiguration defaultPool = 
          ContextInvocationHandler.getProxiedInstance( 
            PoolConfiguration.class, 
            new Hashtable() );
        PoolConfiguration poolConfig = context.getThreads( defaultPool );
        BoundedThreadPool pool = new BoundedThreadPool( poolConfig );
        super.setThreadPool( pool );
        
        //
        // setup the http connector
        //
        
        SelectChannelContext httpConfig = context.getHttp( null );
        if( null != httpConfig )
        {
            SelectChannelConnector http = new SelectChannelConnector( httpConfig );
            super.addConnector( http );
        }
        
        //
        // setup the ssl connector
        //
        
        HttpsContext sslConfig = context.getHttps( null );
        if( null != sslConfig )
        {
            SslSocketConnector ssl = new SslSocketConnector( sslConfig );
            super.addConnector( ssl );
        }
        
        //
        // setup the user realm
        //
        
        RealmContext realmConfig = context.getRealm( null );
        if( null != realmConfig )
        {
            HashUserRealm realm = new HashUserRealm( realmConfig );
            super.addUserRealm( realm );
        }
        
        //
        // setup the request log handler
        // 
        
        NCSAContext logDefaultContext = 
          ContextInvocationHandler.getProxiedInstance( 
            NCSAContext.class, 
            new Hashtable() );
        NCSAContext logContext = context.getLog( logDefaultContext );
        NCSARequestLogHandler requestLogHandler = new NCSARequestLogHandler( logContext );
        
        //
        // populate the context handler collection with the set of context
        // handler components declared under the component parts definition
        //
        
        ContextHandler[] contextHandlers = config.getContextHandlers();
        m_contextHandlers.setHandlers( contextHandlers );
        
        //
        // declare the handler collection containing our root context and request log
        // within which all handlers will be invoked irrespective of response status
        //
        
        HandlerCollection handlerCollection = new HandlerCollection();
        handlerCollection.setHandlers(
          new Handler[]
          { 
            m_contextHandlers,
            requestLogHandler
          } );
        
        super.setHandler( handlerCollection );
        
        //
        // notify completion of the setup process
        //
        
        if( getLogger().isDebugEnabled() )
        {
            getLogger().debug( "server established" );
        }
    }
    
    public ContextHandler getContextHandler( String path )
    {
        ContextHandler[] handlers = m_parts.getContextHandlers();
        for( ContextHandler handler : handlers )
        {
            String spec = handler.getContextPath();
            if( path.equals( spec ) )
            {
                return handler;
            }
        }
        return null;
    }
    
    private Logger getLogger()
    {
        return m_logger;
    }

   /**
    * Jetty lifecycle start operation.
    * @exception Exception if a startup error occurs
    */
    protected void doStart() throws Exception
    {
        getLogger().info( "starting" );
        super.doStart();
    }

   /**
    * Jetty lifecycle stop operation.
    * @exception Exception if a shutdown error occurs
    */
    protected void doStop() throws Exception
    {
        getLogger().info( "stopping" );
        super.doStop();
    }
}