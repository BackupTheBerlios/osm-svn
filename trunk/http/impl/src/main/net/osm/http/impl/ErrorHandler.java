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

import java.io.Writer;
import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.mortbay.util.StringUtil;

/**
 * Default error handler.
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public class ErrorHandler extends org.mortbay.jetty.handler.ErrorHandler
{
    protected void writeErrorPageBody(
      HttpServletRequest request, Writer writer, int code, String message, boolean showStacks )
      throws IOException
    {
        String uri= unpack( request.getRequestURI() );
        writeErrorPageMessage(request,writer,code,message,uri);
        if( showStacks )
        {
            writeErrorPageStacks(request,writer);
        }
        
        writer.write( "<p><i><small>Powered by OSM Express.</br>" );
        writer.write( "A <a href=\"http://www.dpml.net/\">DPML</a> component solution.</small></i></p>" );
        writer.write( "</p>" );
    }

    protected void writeErrorPageMessage(
      HttpServletRequest request, Writer writer, int code, String message, String uri )
      throws IOException
    {
        writer.write( "<h2>HTTP ERROR: ");
        writer.write( Integer.toString( code ) );
        writer.write( "</h2><pre>" );
        writer.write( message );
        writer.write( "</pre>\n<p>RequestURI=" );
        writer.write( uri );
        writer.write( "</p>" );
    }
    
    private String unpack( String string )
    {
        String s = string;
        if(s != null )
        {
            s = StringUtil.replace( s, "&", "&amp;" );
            s = StringUtil.replace( s, "<", "&lt;" );
            s = StringUtil.replace( s, ">", "&gt;" );
        }
        return s;
    }
    
    public String toString()
    {
        return getClass().getName() + "@" + System.identityHashCode( this );
    }
}
