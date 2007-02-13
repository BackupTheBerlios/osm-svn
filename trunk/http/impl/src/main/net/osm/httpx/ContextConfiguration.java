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

import java.util.Map;

import net.dpml.annotation.Context;

import org.mortbay.jetty.handler.ErrorHandler;

/**
 * Common http context handler context contract.
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
 @Context
public interface ContextConfiguration
{
   /**
    * Get the http context resource base.  The value may contain symbolic
    * property references and should resolve to a local directory.
    *
    * @param base the default base value
    * @return the resource base
    */
    String getResourceBase( String base );
    
   /**
    * Get the array of virtual hosts.
    * @param hosts the default virtual host array
    * @return the resolved virtual host array
    */
    String[] getVirtualHosts( String[] hosts );
    
   /**
    * Get the array of connectors. The function returns the set of 
    * connectors in the form <tt>host:port</tt>.
    * @param connectors the default connector array
    * @return the resolved host array
    */
    String[] getConnectors( String[] connectors );
    
   /**
    * Get the array of welcome files.
    * @param values the default welcome file values
    * @return the resolved array of welcome files
    */
    String[] getWelcomeFiles( String[] values );
    
   /**
    * Get the classloader to assign to the context handler.
    * @param classloader the default classloader
    * @return the resolved classloader
    */
    ClassLoader getClassLoader( ClassLoader classloader );

   /**
    * Get the context path under which the http context instance will 
    * be associated.
    *
    * @return the assigned context path
    */
    String getContextPath();

   /**
    * Get the context handler display name.
    * @param name the default name
    * @return the resolved name
    */
    String getDisplayName( String name );

   /**
    * Get the mime type mapping.
    * @param map the default value
    * @return the resolved value
    */
    Map getMimeTypes( Map map );

   /**
    * Get the assigned error handler.
    * @param handler the default handler
    * @return the resolved handler
    */
    ErrorHandler getErrorHandler( ErrorHandler handler );
}
