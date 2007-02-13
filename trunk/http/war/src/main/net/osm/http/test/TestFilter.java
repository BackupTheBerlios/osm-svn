/*
 * Copyright 2004-2005 Mort Bay Consulting Pty. Ltd.
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

package net.osm.http.impl.test;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

/** 
 * TestFilter.
 * @author gregw
 */
public class TestFilter implements Filter
{
    private ServletContext m_context;
    
   /** 
    * Filter initialization.
    * @param filterConfig the filter configuration
    * @exception ServletException if a servlet error occurs
    */
    public void init( FilterConfig filterConfig ) throws ServletException
    {
        m_context = filterConfig.getServletContext();
    }

    /**
    * Process a filter request.
    * @param request the request
    * @param response the response
    * @param chain the filter chain
    * @exception IOException if an I/O error occurs
    * @exception ServletException if a servlet error occurs
    * @see javax.servlet.Filter#doFilter(javax.servlet.ServletRequest, javax.servlet.ServletResponse, javax.servlet.FilterChain)
     */
    public void doFilter( ServletRequest request, ServletResponse response, FilterChain chain )
        throws IOException, ServletException
    {
        Integer oldValue = null;
        try
        {
            oldValue = (Integer) request.getAttribute( "testFilter" );
            Integer value = null;
            if( null == oldValue )
            {
                value = new Integer( 1 );
            }
            else
            {
                value = new Integer( oldValue.intValue() + 1 );
            }
            
            request.setAttribute( "testFilter", value );
            m_context.setAttribute( "request" + request.hashCode(), value );
            chain.doFilter( request, response );
        }
        finally
        {
            request.setAttribute( "testFilter", oldValue );
            m_context.setAttribute( "request" + request.hashCode(), oldValue );
        }
    }

   /**
    * Destroy the instance.
    * @see javax.servlet.Filter#destroy()
    */
    public void destroy()
    {
    }

}
