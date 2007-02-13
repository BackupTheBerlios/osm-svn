/*
 * Copyright 1996-2005 Mort Bay Consulting Pty. Ltd.
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

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


/** 
 * Hello World Servlet
 */
public class HelloWorld extends HttpServlet
{
   /**
    * Servlet initialization.
    * @param config the servlet configuration
    * @exception ServletException if a configuration error occurs
    */
    public void init( ServletConfig config ) throws ServletException
    {
        super.init( config );
    }

   /**
    * Process an incomming post request.
    * @param request the http request
    * @param response the http response
    * @exception ServletException if a servlet processing error occurs
    * @exception IOException if an IO error occurs
    */
    public void doPost( HttpServletRequest request, HttpServletResponse response ) 
      throws ServletException, IOException
    {
        doGet( request, response );
    }

   /**
    * Process an incomming get request.
    * @param request the http request
    * @param response the http response
    * @exception ServletException if a servlet processing error occurs
    * @exception IOException if an IO error occurs
    */
    public void doGet( HttpServletRequest request, HttpServletResponse response ) 
      throws ServletException, IOException
    {
        response.setContentType( "text/html" );
        ServletOutputStream out = response.getOutputStream();
        out.println( "<html>" );
        out.println( "<h1>Hello World<h1>" );
        out.println( "</html>" );
        out.flush();
    }
}
