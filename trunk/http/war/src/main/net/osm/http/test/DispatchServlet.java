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

package net.osm.http.test;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

/** Test Servlet RequestDispatcher.
 * 
 * @author Greg Wilkins (gregw)
 */
public class DispatchServlet extends HttpServlet
{
   /**
    * Servlet initialization.
    * @param config the servlet config
    * @exception ServletException in an initialization error occurs
    */
    public void init( ServletConfig config ) throws ServletException
    {
        super.init( config );
    }

   /**
    * Process a post request.
    * @param sreq the servlet http request
    * @param sres the servlet http response
    * @exception ServletException in a processing error occurs
    * @exception IOException in an I/O error occurs
    */
    public void doPost( HttpServletRequest sreq, HttpServletResponse sres ) 
      throws ServletException, IOException
    {
        doGet( sreq, sres );
    }

   /**
    * Process a get request.
    * @param sreq the servlet http request
    * @param sres the servlet http response
    * @exception ServletException in a processing error occurs
    * @exception IOException in an I/O error occurs
    */
    public void doGet( HttpServletRequest sreq, HttpServletResponse sres ) 
      throws ServletException, IOException
    {
        if( sreq.getParameter( "wrap" ) != null )
        {
            sreq = new HttpServletRequestWrapper( sreq );
            sres = new HttpServletResponseWrapper( sres );
        }

        String prefix;
        if( sreq.getContextPath() != null )
        {
            prefix = sreq.getContextPath() + sreq.getServletPath();
        }
        else
        {
            prefix = sreq.getServletPath();
        }
        
        String info;
        if( sreq.getAttribute( "javax.servlet.include.servlet_path" ) != null )
        {
            info = (String) sreq.getAttribute( "javax.servlet.include.path_info" );
        }
        else
        {
            info = sreq.getPathInfo();
        }

        if( info == null )
        {
            info= "NULL";
        }
        
        if( info.startsWith( "/includeW/" ) )
        {
            doGetIncludeW( info, sreq, sres );
        }
        else if( info.startsWith( "/includeS/" ) )
        {
            doGetIncludeS( info, sreq, sres );
        }
        else if( info.startsWith( "/forward/" ) )
        {
            doGetForward( info, sreq, sres );
        }
        else if( info.startsWith( "/forwardC/" ) )
        {
            doGetForwardC( info, sreq, sres );
        }
        else if( info.startsWith( "/forwardSC/" ) )
        {
            doGetForwardSC( info, sreq, sres );
        }
        else if( info.startsWith( "/includeN/" ) )
        {
            doGetIncludeN( info, sreq, sres );
        }
        else if( info.startsWith( "/forwardN/" ) )
        {
            doGetForwardN( info, sreq, sres );
        }
        else
        {
            sres.setContentType( "text/html" );
            PrintWriter pout= sres.getWriter();
            pout.write(
              "<H1>Dispatch URL must be of the form: </H1>"
              + "<PRE>"
              + prefix
              + "/includeW/path\n"
              + prefix
              + "/includeS/path\n"
              + prefix
              + "/forward/path\n"
              + prefix
              + "/includeN/name\n"
              + prefix
              + "/forwardC/_context/path\n"
              + prefix
              + "/forwardSC/_context/path</PRE>" );
            pout.flush();
        }
    }

    private void doGetIncludeW( String info, HttpServletRequest sreq, HttpServletResponse sres ) 
      throws ServletException, IOException
    {
        sres.setContentType( "text/html" );
        info = info.substring( 9 );
        if( info.indexOf( '?' ) < 0 )
        {
            info += "?Dispatch=include";
        }
        else
        {
            info += "&Dispatch=include";
        }
        
        PrintWriter pout = null;
        pout = sres.getWriter();
        pout.write( 
          "<H1>Include (writer): " 
          + info 
          + "</H1><HR>" );
        
        RequestDispatcher dispatch = 
          getServletContext().getRequestDispatcher( info );
        if( dispatch == null )
        {
            pout = sres.getWriter();
            pout.write( "<H1>Null dispatcher</H1>" );
        }
        else
        {
            dispatch.include( sreq, sres );
        }
        pout.write( "<HR><H1>-- Included (writer)</H1>" );
    }
    
    private void doGetIncludeS( String info, HttpServletRequest sreq, HttpServletResponse sres ) 
      throws ServletException, IOException
    {
        sres.setContentType( "text/html" );
        info = info.substring( 9 );
        if( info.indexOf( '?' ) < 0 )
        {
            info += "?Dispatch=include";
        }
        else
        {
            info += "&Dispatch=include";
        }
        
        OutputStream out = null;
        out = sres.getOutputStream();
        out.write( 
          (
            "<H1>Include (outputstream): " 
            + info 
            + "</H1><HR>" 
          ).getBytes() 
        );
        
        RequestDispatcher dispatch = 
          getServletContext().getRequestDispatcher( info );
        if( dispatch == null )
        {
            out = sres.getOutputStream();
            out.write( "<H1>Null dispatcher</H1>".getBytes() );
        }
        else
        {
            dispatch.include( sreq, sres );
        }
        out.write( "<HR><H1>-- Included (outputstream)</H1>".getBytes() );
    }
    
    private void doGetForward( String info, HttpServletRequest sreq, HttpServletResponse sres ) 
      throws ServletException, IOException
    {
        info = info.substring( 8 );
        if( info.indexOf( '?' ) < 0 )
        {
            info += "?Dispatch=forward";
        }
        else
        {
            info += "&Dispatch=forward";
        }
        
        RequestDispatcher dispatch = 
          getServletContext().getRequestDispatcher( info );
          
        if( dispatch != null )
        {
            ServletOutputStream out = sres.getOutputStream();
            out.print( "Can't see this" );
            dispatch.forward( sreq, sres );
            try
            {
                out.println( "IOException" );
                throw new IllegalStateException();
            }
            catch( IOException e )
            {
            }
        }
        else
        {
            sres.setContentType( "text/html" );
            PrintWriter pout= sres.getWriter();
            pout.write( "<H1>No dispatcher for: " + info + "</H1><HR>" );
            pout.flush();
        }
    }
    
    private void doGetForwardC( String info, HttpServletRequest sreq, HttpServletResponse sres ) 
      throws ServletException, IOException
    {
        info = info.substring( 9 );
        if( info.indexOf( '?' ) < 0 )
        {
            info += "?Dispatch=forward";
        }
        else
        {
            info += "&Dispatch=forward";
        }
        
        String cpath = info.substring( 0, info.indexOf( '/', 1 ) );
        info = info.substring( cpath.length() );
        ServletContext context= getServletContext().getContext( cpath );
        RequestDispatcher dispatch = context.getRequestDispatcher( info );
        
        if( dispatch != null )
        {
            dispatch.forward( sreq, sres );
        }
        else
        {
            sres.setContentType( "text/html" );
            PrintWriter pout = sres.getWriter();
            pout.write( 
              "<H1>No dispatcher for: " 
              + cpath 
              + "/" 
              + info 
              + "</H1><HR>" );
            pout.flush();
        }
    }
    
    private void doGetForwardSC( String info, HttpServletRequest sreq, HttpServletResponse sres ) 
      throws ServletException, IOException
    {
        sreq.getSession( true );
        info = info.substring( 10 );
        if( info.indexOf( '?' ) < 0 )
        {
            info += "?Dispatch=forward";
        }
        else
        {
            info += "&Dispatch=forward";
        }
        String cpath = info.substring( 0, info.indexOf( '/', 1 ) );
        info = info.substring( cpath.length() );
        
        ServletContext context = getServletContext().getContext( cpath );
        RequestDispatcher dispatch = context.getRequestDispatcher( info );
        
        if( dispatch != null )
        {
            dispatch.forward( sreq, sres );
        }
        else
        {
            sres.setContentType( "text/html" );
            PrintWriter pout= sres.getWriter();
            pout.write( 
              "<H1>No dispatcher for: " 
              + cpath 
              + "/" 
              + info 
              + "</H1><HR>" );
            pout.flush();
        }
    }
    
    private void doGetIncludeN( String info, HttpServletRequest sreq, HttpServletResponse sres ) 
      throws ServletException, IOException
    {
        sres.setContentType( "text/html" );
        info = info.substring( 10 );
        if( info.indexOf( "/" ) >= 0 )
        {
            info = info.substring( 0, info.indexOf( "/" ) );
        }
        PrintWriter pout;
        if( info.startsWith( "/null" ) )
        {
            info = info.substring( 5 );
        }
        else
        {
            pout = sres.getWriter();
            pout.write( 
              "<H1>Include named: " 
              + info 
              + "</H1><HR>" );
        }
        
        RequestDispatcher dispatch = 
          getServletContext().getNamedDispatcher( info );
        if( dispatch != null )
        {
            dispatch.include( sreq, sres );
        }
        else
        {
            pout = sres.getWriter();
            pout.write( 
              "<H1>No servlet named: " 
              + info 
              + "</H1>" );
        }
        
        pout = sres.getWriter();
        pout.write( "<HR><H1>Included " );
    }
    
    private void doGetForwardN( String info, HttpServletRequest sreq, HttpServletResponse sres ) 
      throws ServletException, IOException
    {
        info = info.substring( 10 );
        if( info.indexOf( "/" ) >= 0 )
        {
            info= info.substring( 0, info.indexOf( "/" ) );
        }
        RequestDispatcher dispatch = 
          getServletContext().getNamedDispatcher( info );
        if( dispatch != null )
        {
            dispatch.forward( sreq, sres );
        }
        else
        {
            sres.setContentType( "text/html" );
            PrintWriter pout = sres.getWriter();
            pout.write(
              "<H1>No servlet named: " 
              + info 
              + "</H1>" );
            pout.flush();
        }
    }
    
   /**
    * Return thr servlet info.
    * @return the info
    */
    public String getServletInfo()
    {
        return "Include Servlet";
    }

   /**
    * Destroy the servlet.
    */
    public synchronized void destroy()
    {
    }

}
