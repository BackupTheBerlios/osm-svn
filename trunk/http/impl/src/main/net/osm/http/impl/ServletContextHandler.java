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

import javax.servlet.Servlet;

import net.dpml.util.Logger;

import net.dpml.annotation.Context;
import net.dpml.annotation.Component;
import net.dpml.annotation.Services;

import org.mortbay.jetty.servlet.ServletHolder;
import org.mortbay.jetty.servlet.ServletHandler;

import static net.dpml.annotation.LifestylePolicy.SINGLETON;

/**
 * Servlet context handler. 
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
@Component( name="servlet", lifestyle=SINGLETON )
public class ServletContextHandler extends org.mortbay.jetty.servlet.Context
{
   /**
    * Deployment context for a servlet context handler.
    */
    public interface Context extends ContextHandler.Context
    {
    }
    
   /**
    * Servlet context handler internal parts management interface.
    */
    public interface Parts
    {
        ServletEntry[] getServletEntries();
    }
    
    private final Logger m_logger;
    
   /**
    * Creation of a new servlet context handler.
    * @param logger the assigned logging channel
    * @param context the deployment context
    * @param parts the internal comparts
    * @exception Exception if an instantiation error occurs
    */
    public ServletContextHandler( 
      Logger logger, Context context, Parts parts ) throws Exception
    {
        super();
        
        m_logger = logger;
        
        ContextHandler.contextualize( logger, this, context );
        
        for( ServletEntry entry : parts.getServletEntries() )
        {
            if( m_logger.isDebugEnabled() )
            {
                m_logger.debug( "adding servlet " + entry );
            }
            String path = entry.getPath();
            Servlet[] servlets = entry.getServlets();
            for( Servlet servlet : servlets )
            {
                ServletHolder holder = new ServletHolder( servlet );
                super.addServlet( holder , path );
            }
        }
    }
}
