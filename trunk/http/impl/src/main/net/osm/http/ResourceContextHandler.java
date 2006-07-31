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
 * Context handler with enhanced support for symbolic property dereferencing. 
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public class ResourceContextHandler extends org.mortbay.jetty.handler.ContextHandler
{
   /**
    * HTTP static resource context handler parameters.
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
    }
    
   /**
    * Creation of a new resource context handler.
    * @param logger the assigned logging channel
    * @param context the deployment context
    * @exception Exception if an instantiation error occurs
    */
    public ResourceContextHandler( Logger logger, Context context ) throws Exception
    {
        ContextHelper helper = new ContextHelper( logger );
        helper.contextualize( this, context );
        
        String base = context.getResourceBase();
        super.setResourceBase( base );
        logger.debug( "resource path: " + base );
        
        ResourceHandler handler = new ResourceHandler( "static", "/" );
        super.setHandler( handler );
    }
}
