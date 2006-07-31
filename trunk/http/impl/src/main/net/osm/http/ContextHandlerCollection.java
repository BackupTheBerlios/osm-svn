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

import net.dpml.metro.PartsManager;
import net.dpml.metro.ComponentHandler;
import net.dpml.component.Provider;

/**
 * Context handler collection.
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public class ContextHandlerCollection extends org.mortbay.jetty.handler.ContextHandlerCollection
{
   /**
    * Internal parts management interface.
    */
    public interface Parts extends PartsManager
    {
    }
    
    private final Logger m_logger;
    private final Parts m_parts;

   /**
    * Creation of a new HTTP server implementation.
    * @param logger the assigned logging channel
    * @param parts the parts manager
    * @exception Exception if an instantiation error occurs
    */
    public ContextHandlerCollection( Logger logger, Parts parts ) throws Exception
    {
        super();
         
        m_logger = logger;
        m_parts = parts;

        getLogger().debug( "commencing handler addition" );
        String[] keys = parts.getKeys();
        getLogger().debug( "handler count: " + keys.length );
        for( int i=0; i<keys.length; i++ )
        {
            String key = keys[i];
            ComponentHandler handler = parts.getComponentHandler( key );
            getLogger().info( "adding handler: " + handler );
            try
            {
                Provider provider = handler.getProvider();
                org.mortbay.jetty.Handler ch = 
                  (org.mortbay.jetty.Handler) provider.getValue( false );
                super.addHandler( ch );
            }
            catch( Throwable e )
            {
                final String error = 
                  "Failed to deploy handler: " + handler;
                throw new Exception( error, e );
            }
        }
    }
    
    private Logger getLogger()
    {
        return m_logger;
    }
}
