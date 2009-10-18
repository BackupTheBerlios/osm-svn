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
package osm.http.express;

import dpml.lang.ContextInvocationHandler;

import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Hashtable;

import net.dpml.annotation.Context;
import net.dpml.annotation.Component;
import net.dpml.annotation.Services;

import javax.servlet.http.HttpServletRequest;

import net.dpml.util.Logger;

import net.osm.http.spi.ThreadContext;
import net.osm.http.spi.NCSAContext;
import net.osm.http.spi.HttpConnectionContext;
import net.osm.http.spi.HttpsConnectionContext;
import net.osm.http.spi.ServerContext;
import net.osm.http.spi.RealmContext;

import net.osm.http.impl.LoggerAdapter;
import net.osm.http.impl.BoundedThreadPool;
import net.osm.http.impl.SelectChannelConnector;
import net.osm.http.impl.SslSocketConnector;
import net.osm.http.impl.NCSARequestLogHandler;

import org.mortbay.jetty.Connector;
import org.mortbay.jetty.Handler;
import org.mortbay.jetty.handler.HandlerCollection;
import org.mortbay.jetty.handler.ContextHandler;
import org.mortbay.jetty.handler.ContextHandlerCollection;
import org.mortbay.jetty.handler.DefaultHandler;
import org.mortbay.jetty.security.UserRealm;
import org.mortbay.thread.ThreadPool;
import org.mortbay.xml.XmlConfiguration;

import static net.dpml.annotation.LifestylePolicy.SINGLETON;

/**
 * HTTP server implementation.
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
@Component( name="jetty", lifestyle=SINGLETON )
public class ExpressServer extends org.mortbay.jetty.Server
{
   /**
    * Internal parts management interface.
    */
    @net.dpml.annotation.Parts
    public interface Parts
    {
        ContextHandler[] getContextHandlers();
        UserRealm[] getUserRealms();
    }
    
    private final Logger m_logger;
    private final ContextHandlerCollection m_contextHandlers = 
      new ContextHandlerCollection();
    private final Parts m_parts;
    
   /**
    * Creation of a new HTTP server implementation.
    * @param logger the assigned logging channel
    * @param context the assigned deployment context
    * @exception Exception if an instantiation error occurs
    */
    public ExpressServer( Logger logger, ServerContext context, Parts parts ) throws Exception
    {
        super();
        
        m_logger = logger;
        m_parts = parts;
        
        if( getLogger().isDebugEnabled() )
        {
            getLogger().debug( "commencing http server deployment" );
        }
        Logger internal = logger.getChildLogger( "jetty" );
        LoggerAdapter.setRootLogger( internal );
        Thread.currentThread().setContextClassLoader( getClass().getClassLoader() );
        
        //
        // configure the shutdown policy
        //
        
        int grace = context.getGracefulShutdown( 0 );
        super.setGracefulShutdown( grace );
        
        //
        // setup the thread pool
        //
        
        ThreadContext defaultPool = 
          ContextInvocationHandler.getProxiedInstance( 
            ThreadContext.class, 
            new Hashtable<String,Object>() );
        ThreadContext poolConfig = context.getThreads( defaultPool );
        BoundedThreadPool pool = new BoundedThreadPool( poolConfig );
        super.setThreadPool( pool );
        
        //
        // setup the http connector
        //
        
        HttpConnectionContext httpConfig = context.getHttp( null );
        if( null != httpConfig )
        {
            getLogger().info( "adding HTTP connector on port: " + httpConfig.getPort( 0 ) );
            SelectChannelConnector http = new SelectChannelConnector( httpConfig );
            super.addConnector( http );
        }
        
        //
        // setup the ssl connector
        //
        
        HttpsConnectionContext sslConfig = context.getHttps( null );
        if( null != sslConfig )
        {
            getLogger().info( "adding SSL connector on port: " + sslConfig.getPort( 0 ) );
            SslSocketConnector ssl = new SslSocketConnector( sslConfig );
            super.addConnector( ssl );
        }
        
        //
        // setup the user realm
        //
        
        setUserRealms( parts.getUserRealms() );
        
        //
        // setup the request log handler
        // 
        
        NCSAContext logDefaultContext = 
          ContextInvocationHandler.getProxiedInstance( 
            NCSAContext.class, 
            new Hashtable<String,Object>() );
        NCSAContext logContext = context.getLog( logDefaultContext );
        NCSARequestLogHandler requestLogHandler = new NCSARequestLogHandler( logContext );
        
        //
        // populate the context handler collection with the set of context
        // handler components declared under the component parts definition
        //
        
        ContextHandler[] contextHandlers = parts.getContextHandlers();
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
        return getContextHandler( path, false );
    }
    
    public ContextHandler getContextHandler( String path, boolean create )
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
        getLogger().info( "started" );
    }
    
   /**
    * Jetty lifecycle stop operation.
    * @exception Exception if a shutdown error occurs
    */
    protected void doStop() throws Exception
    {
        getLogger().debug( "stopping" );
        super.doStop();
        getLogger().info( "stopped" );
    }
}
