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
import java.util.Map;
import java.security.PermissionCollection;
import java.io.File;

import net.dpml.logging.Logger;

import org.mortbay.jetty.security.SecurityHandler;
import org.mortbay.jetty.servlet.SessionHandler;
import org.mortbay.jetty.servlet.ServletHandler;

/**
 * Servlet context handler. 
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public class WebAppContextHandler extends org.mortbay.jetty.webapp.WebAppContext
{
   /**
    * HTTP static resource vontext handler parameters.
    */
    public interface Context extends ContextHandlerContext
    {
       /**
        * Get the war artifact uri.
        * @return the uri identifying the war artifact
        */
        URI getWar();
        
       /**
        * Get the assigned temp directory.
        * @param dir the default temp directory
        * @return the resolved temp directory
        */
        File getTempDirectory( File dir );
        
       /**
        * Get the resource alias map.
        * @param map the default mapping
        * @return the resolved map
        */
        Map getResourceAliases( Map map );
        
       /**
        * Get the war extraction policy.
        * @param policy the default policy (true)
        * @return the resolved policy
        */
        boolean getExtractionPolicy( boolean policy );
        
       /**
        * Get the assigned permission collection.
        * @param permissions the default permissions value
        * @return the resolved permissions collection
        */
        PermissionCollection getPermissions( PermissionCollection permissions );
        
       /**
        * Get the assigned security handler.
        * @param handler the default value
        * @return the resolved handler
        */
        SecurityHandler getSecurityHandler( SecurityHandler handler );
        
       /**
        * Get the assigned session handler.
        * @param handler the default value
        * @return the resolved handler
        */
        SessionHandler getSessionHandler( SessionHandler handler );
        
       /**
        * Get the assigned servlet handler.
        * @param handler the default value
        * @return the resolved handler
        */
        ServletHandler getServletHandler( ServletHandler handler );
    }
    
    private int m_priority = 0;
    
   /**
    * Creation of a new web-application context handler.
    * @param logger the assigned logging channel
    * @param context the deployment context
    * @exception Exception if an instantiation error occurs
    */
    public WebAppContextHandler( Logger logger, Context context ) throws Exception
    {
        ContextHelper helper = new ContextHelper( logger );
        helper.contextualize( this, context );
        
        SecurityHandler securityHandler = 
          context.getSecurityHandler( new SecurityHandler() );
        setSecurityHandler( securityHandler );
        setHandler( securityHandler );
        
        SessionHandler sessionHandler = 
          context.getSessionHandler( new SessionHandler() );
        securityHandler.setHandler( sessionHandler );
        setSessionHandler( sessionHandler );
        
        ServletHandler servletHandler = 
          context.getServletHandler( new ServletHandler() );
        sessionHandler.setHandler( servletHandler );
        setServletHandler( servletHandler );
        
        URI uri = context.getWar();
        setWar( uri.toASCIIString() );

        File temp = context.getTempDirectory( null );
        if( null != temp )
        {
            setTempDirectory( temp );
        }
        
        Map map = context.getResourceAliases( null );
        if( null != map )
        {
            setResourceAliases( map );
        }
        
        boolean extractionPolicy = context.getExtractionPolicy( true );
        setExtractWAR( extractionPolicy );
        
        PermissionCollection permissions = context.getPermissions( null );
        if( null != permissions )
        {
            setPermissions( permissions );
        }
    }
}
