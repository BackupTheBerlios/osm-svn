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

import java.net.URI;
import java.net.URL;
import java.util.ArrayList;

import net.dpml.annotation.Context;
import net.dpml.annotation.Component;
import net.dpml.annotation.Services;

import static net.dpml.annotation.LifestylePolicy.SINGLETON;

import net.dpml.util.Logger;

import org.mortbay.jetty.Connector;
import org.mortbay.jetty.Handler;
import org.mortbay.jetty.security.UserRealm;
import org.mortbay.thread.ThreadPool;
import org.mortbay.xml.XmlConfiguration;
import org.mortbay.jetty.handler.HandlerCollection;

/**
 * HTTP server implementation.
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
@Component( name="server", lifestyle=SINGLETON )
public class StandardServer extends org.mortbay.jetty.Server
{
   /**
    * Internal parts managemwent interface.
    */
    public interface Parts
    {
        UserRealm[] getUserRealms();
        
        Connector[] getConnectors();
        
       /**
        * Return the collection of handlers.  For any given request all handlers
        * in the collection will be supplied with a request irrespective of response 
        * status.
        *
        * @return the configured handler collection.
        */
        Handler[] getHandlers();
    }
    
    private final Logger m_logger;

   /**
    * Creation of a new HTTP server implementation.
    * @param logger the assigned logging channel
    * @param context the assigned deployment context
    * @param parts the parts manager
    * @exception Exception if an instantiation error occurs
    */
    public StandardServer( Logger logger, PoolConfiguration context, Parts parts ) throws Exception
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
        
        try
        {
            BoundedThreadPool pool = new BoundedThreadPool( context );
            super.setThreadPool( pool );
        }
        catch( Throwable e )
        {
            e.printStackTrace();
        }
        
        //
        // add connectors, realms and handlers
        //
        
        setUserRealms( parts.getUserRealms() );
        
        setConnectors( parts.getConnectors() );
        
        HandlerCollection handlers = new HandlerCollection();
        for( Handler handler : parts.getHandlers() )
        {
            if( m_logger.isDebugEnabled() )
            {
                m_logger.debug( "adding handler " + handler );
            }
            handlers.addHandler( handler );
        }
        setHandler( handlers );
        
        if( getLogger().isDebugEnabled() )
        {
            getLogger().debug( "server established" );
        }
    }
    
    private Logger getLogger()
    {
        return m_logger;
    }

    protected void doStart() throws Exception
    {
        getLogger().info( "starting" );
        super.doStart();
    }

    protected void doStop() throws Exception
    {
        getLogger().info( "stopping" );
        super.doStop();
    }
}
