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

import java.net.URI;
import java.net.URL;
import java.util.ArrayList;

import net.dpml.annotation.Context;
import net.dpml.annotation.Component;
import net.dpml.annotation.Services;

import static net.dpml.annotation.LifestylePolicy.SINGLETON;

import net.dpml.util.Logger;

import net.dpml.transit.Artifact;

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
public class ClassicServer extends org.mortbay.jetty.Server
{
   /**
    * Component context through which the server configuration uri may be declared.
    */
    public interface Configuration
    {
       /**
        * Get the Jetty XML configuration uri.  The configuration uri is 
        * used to establish the default server configuration prior to 
        * customization via the server context. If not supplied the server
        * will be deployed relative to the supplied context.
        *
        * @return a uri referencing a Jetty configuration profile
        */
        URI getURI();
        
    }
    
    private final Logger m_logger;
    
   /**
    * Creation of a new HTTP server implementation.
    * @param logger the assigned logging channel
    * @param context the assigned deployment context
    * @exception Exception if an instantiation error occurs
    */
    public ClassicServer( Logger logger, Configuration context ) throws Exception
    {
        super();
        
        m_logger = logger;
        
        if( logger.isDebugEnabled() )
        {
            logger.debug( "commencing http server deployment" );
        }
        Logger internal = logger.getChildLogger( "jetty" );
        LoggerAdapter.setRootLogger( internal );
        Thread.currentThread().setContextClassLoader( getClass().getClassLoader() );
        URI uri = context.getURI();
        if( logger.isDebugEnabled() )
        {
            getLogger().debug( "applying server configuration: " + uri );
        }
        URL url = Artifact.toURL( uri );
        XmlConfiguration config = new XmlConfiguration( url );
        config.configure( this );
    }
    
    private Logger getLogger()
    {
        return m_logger;
    }

    protected void doStart() throws Exception
    {
        if( getLogger().isDebugEnabled() )
        {
            getLogger().debug( "starting" );
        }
        super.doStart();
    }

    protected void doStop() throws Exception
    {
        if( getLogger().isDebugEnabled() )
        {
            getLogger().debug( "stopping" );
        }
        super.doStop();
    }
}
