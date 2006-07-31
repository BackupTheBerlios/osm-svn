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

import org.mortbay.jetty.servlet.ServletHolder;
import org.mortbay.jetty.servlet.ServletMapping;

/**
 * Default implementation of a static resource handler.
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public class ResourceHandler extends org.mortbay.jetty.servlet.ServletHandler
{
   /**
    * Creation of a new resource handler.
    * @param name the resource handler name
    * @param path the resourdce path
    */
    public ResourceHandler( String name, String path )
    {
        super();
       
        ServletHolder holder = new ServletHolder();
        holder.setName( name );
        holder.setClassName( "org.mortbay.jetty.servlet.DefaultServlet" );
        ServletHolder[] servlets = new ServletHolder[]{holder};
        setServlets( servlets );
        
        ServletMapping mapping = new ServletMapping();
        mapping.setPathSpec( path );
        mapping.setServletName( name );
        ServletMapping[] mappings = new ServletMapping[]{mapping};
        setServletMappings( mappings );
    }
}
