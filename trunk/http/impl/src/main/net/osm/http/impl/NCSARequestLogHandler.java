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

import net.dpml.annotation.Context;
import net.dpml.annotation.Component;
import net.dpml.annotation.Services;

import net.osm.http.spi.NCSAContext;

import dpml.util.PropertyResolver;

import org.mortbay.jetty.handler.RequestLogHandler;
import org.mortbay.jetty.NCSARequestLog;

import static net.dpml.annotation.LifestylePolicy.SINGLETON;

/** 
 * Wrapper for the Jetty NCSA request logger.
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
@Component( name="ncsa", lifestyle=SINGLETON )
//@Services( RequestLogHandler.class )
public class NCSARequestLogHandler extends RequestLogHandler
{
    private final NCSARequestLog m_ncsa;

   /**
    * Creation of a new NCSA request log.
    * @param context the deployment context
    */
    public NCSARequestLogHandler( NCSAContext context )
    {
        m_ncsa = new NCSARequestLog();
        
        boolean append = context.getAppend( false );
        m_ncsa.setAppend( append );
        
        boolean extended = context.getExtended( false );
        m_ncsa.setExtended( extended );
        
        boolean preferProxiedFor = context.getPreferProxiedForAddress( false );
        m_ncsa.setPreferProxiedForAddress( preferProxiedFor );
        
        String filename = context.getFilename( null );
        if( filename != null )
        {
            filename = PropertyResolver.resolve( System.getProperties(), filename );
            File file = new File( filename );
            File parent = file.getParentFile();
            if( null != parent )
            {
                parent.mkdirs();
            }
            m_ncsa.setFilename( filename );
        }
        
        String dateformat = context.getLogDateFormat( null );
        if( dateformat != null )
        {
            m_ncsa.setLogDateFormat( dateformat );
        }
        
        String[] ignorepaths = context.getIgnorePaths( null );
        if( ignorepaths != null )
        {
            m_ncsa.setIgnorePaths( ignorepaths );
        }
        
        String timezone = context.getLogTimeZone( null );
        if( timezone != null )
        {
            m_ncsa.setLogTimeZone( timezone );
        }
        
        int retain = context.getRetainDays( -1 );
        if( retain > 0 )
        {
            m_ncsa.setRetainDays( retain );
        }
        
        boolean recordLatencyPolicy = context.getLogLatency( false );
        m_ncsa.setLogLatency( recordLatencyPolicy );
        
        boolean cookiesPolicy = context.getLogCookies( false );
        m_ncsa.setLogCookies( cookiesPolicy );
        
        setRequestLog( m_ncsa );
    }
}
