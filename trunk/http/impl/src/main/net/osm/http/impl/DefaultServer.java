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

import dpml.lang.ContextInvocationHandler;

import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Hashtable;

import net.dpml.annotation.Context;
import net.dpml.annotation.Component;
import net.dpml.annotation.Services;

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

import net.osm.http.SelectChannelConnector.SelectChannelContext;
import net.osm.http.SslSocketConnector.HttpsContext;
import net.osm.http.HashUserRealm.RealmContext;
import net.osm.http.NCSARequestLogHandler.NCSAContext;

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
    
    private final Logger m_logger;
    private final ContextHandlerCollection m_contextHandlers = new ContextHandlerCollection();
    private final org.mortbay.jetty.servlet.Context m_root;

   /**
    * Creation of a new HTTP server implementation.
    * @param logger the assigned logging channel
    * @param context the assigned deployment context
    * @exception Exception if an instantiation error occurs
    */
    public DefaultServer( Logger logger, ServerContext context ) throws Exception
    {
        super();
        
        m_logger = logger;
        
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
        // setup root context "/" with an error handler
        // 
        
        m_root = 
          new org.mortbay.jetty.servlet.Context( 
            this, "/", org.mortbay.jetty.servlet.Context.SESSIONS );
        ErrorHandler handler = new ErrorHandler();
        m_root.setErrorHandler( handler );
        m_contextHandlers.addHandler( m_root );
        
        //
        // declare the handler collection (containing our root context and request log)
        //
        
        HandlerCollection handlers = new HandlerCollection();
        handlers.setHandlers(
          new Handler[]
          { 
            m_contextHandlers,
            requestLogHandler
          } );
        
        super.setHandler( handlers );
        
        //
        // notify completion of the setup process
        //
        
        if( getLogger().isDebugEnabled() )
        {
            getLogger().debug( "server established" );
        }
    }
    
    org.mortbay.jetty.servlet.Context getRootContext()
    {
        return m_root;
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
