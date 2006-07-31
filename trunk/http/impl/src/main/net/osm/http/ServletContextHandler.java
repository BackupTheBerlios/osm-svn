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

/**
 * Servlet context handler. 
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public class ServletContextHandler extends org.mortbay.jetty.handler.ContextHandler
{
   /**
    * HTTP static resource vontext handler parameters.
    */
    public interface Context extends ContextHandlerContext
    {
       /**
        * Get the http context resource base.  The value may contain symbolic
        * property references and should resolve to a local directory.
        *
        * @return the resource base
        */
        String getResourceBase();
        
       /**
        * Get the array of servlet holders.
        * @param holders the default value
        * @return the resolved value
        */
        ServletHolder[] getServletHolders( ServletHolder[] holders );
        
       /**
        * Get the array of servlet name to path mappings.
        * @param entries the default value
        * @return the resolved array of name to path mappings
        */
        ServletEntry[] getServletEntries( ServletEntry[] entries );
    }
    
    private int m_priority = 0;
    
   /**
    * Creation of a new servlet context handler.
    * @param logger the assigned logging channel
    * @param context the deployment context
    * @exception Exception if an instantiation error occurs
    */
    public ServletContextHandler( Logger logger, Context context ) throws Exception
    {
        super();
        
        ContextHelper helper = new ContextHelper( logger );
        helper.contextualize( this, context );
        
        String base = context.getResourceBase();
        super.setResourceBase( base );
        
        ServletHolder[] holders = context.getServletHolders( new ServletHolder[0] );
        ServletEntry[] entries = context.getServletEntries( new ServletEntry[0] );
        ServletHandler handler = new ServletHandler( holders, entries );
        super.setHandler( handler );
    }
}
