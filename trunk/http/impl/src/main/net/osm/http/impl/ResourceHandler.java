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

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletException;

import net.dpml.util.Logger;

import net.dpml.annotation.Component;
import net.dpml.annotation.Services;

import static net.dpml.annotation.LifestylePolicy.SINGLETON;

import org.mortbay.io.Buffer;
import org.mortbay.jetty.Request;
import org.mortbay.resource.Resource;
import org.mortbay.jetty.HttpConnection;
import org.mortbay.jetty.HttpMethods;
import org.mortbay.jetty.HttpHeaders;
import org.mortbay.io.WriterOutputStream;
import org.mortbay.util.TypeUtil;

/**
 * Simple resource handler. 
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
@Component( name="resource", lifestyle=SINGLETON )
public class ResourceHandler extends org.mortbay.jetty.handler.ResourceHandler
{
   /**
    * HTTP static resource context handler parameters.
    */
    public interface Context
    {
       /**
        * Get the http context resource base.  The value may contain symbolic
        * property references and should resolve to a local directory.
        *
        * @return the resource base
        */
        String getResourceBase();

       /**
        * Get the array of welcome files.
        * @param values the default welcome file values
        * @return the resolved array of welcome files
        */
        String[] getWelcomeFiles( String[] values );
    }
    
   /**
    * Creation of a new servlet context handler.
    * @param logger the assigned logging channel
    * @param context the deployment context
    * @exception Exception if an instantiation error occurs
    */
    public ResourceHandler( Logger logger, Context context ) throws Exception
    {
        super();
        
        String base = context.getResourceBase();
        
        File file = new File( base );
        String path = file.getCanonicalPath();
        if( logger.isTraceEnabled() )
        {
            logger.trace( "setting resource base to: " + path );
        }
        
        super.setResourceBase( path );
        
        String[] welcome = context.getWelcomeFiles( null );
        if( null != welcome )
        {
            super.setWelcomeFiles( welcome );
        }
    }
}
