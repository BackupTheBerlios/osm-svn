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

import javax.servlet.Servlet;

import net.dpml.util.Logger;

import net.dpml.annotation.Context;
import net.dpml.annotation.Component;
import net.dpml.annotation.Services;
import net.dpml.annotation.Parts;

import static net.dpml.annotation.LifestylePolicy.SINGLETON;

import org.mortbay.jetty.servlet.ServletHolder;
import org.mortbay.jetty.servlet.ServletHandler;

/**
 * Servlet context handler. 
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
@Component( name="servlet", lifestyle=SINGLETON )
public class ServletContainer extends org.mortbay.jetty.servlet.Context
{
    @Parts
    public interface ServletContainerParts
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
    public ServletContainer( 
      Logger logger, ContextConfiguration context, ServletContainerParts parts ) throws Exception
    {
        super();
        
        m_logger = logger;
        
        ContextHelper helper = new ContextHelper( logger );
        helper.contextualize( this, context );
        
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
