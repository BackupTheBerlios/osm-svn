/*
 * Copyright 2006 Stephen J. McConnell.
 *
 * Licensed  under the  Apache License,  Version 2.0  (the "License");
 * you may not use  this file  except in  compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed  under the  License is distributed on an "AS IS" BASIS,
 * WITHOUT  WARRANTIES OR CONDITIONS  OF ANY KIND, either  express  or
 * implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.osm.http.impl;

import dpml.lang.ContextInvocationHandler;

import java.util.Map;
import java.util.Hashtable;

import junit.framework.TestCase;

import net.osm.http.spi.NCSAContext;

import org.mortbay.jetty.NCSARequestLog;

/**
 * NCSARequestLog test case.
 */
public class NCSARequestLogTestCase extends TestCase
{
    private static final String[] IGNORE_PATHS = new String[]{"abc", "def"};
    private static final boolean APPEND_POLICY = true;
    private static final boolean EXTENDED_POLICY = true;
    private static final boolean PROXY_PREFERRED_POLICY = true;
    private static final String FILENAME = "foo.log";
    private static final String LOG_DATE_FORMAT = "dd/mmm/yyyy:HH:mm:ss ZZZ";
    private static final String LOG_TIME_ZONE = "PST";
    private static final int RETAIN_DAYS = 70;
    private static final boolean LATENCY_POLICY = true;
    private static final boolean COOKIE_POLICY = true;
    
    private NCSARequestLogHandler m_handler;
    private NCSARequestLog m_logger;
    
    /**
     * Setup the NCSARequestLog.
     * @throws Exception if an error occurs during test execution
     */
    public void setUp() throws Exception
    {
        Map<String,Object> map = new Hashtable<String,Object>();
        map.put( "ignorePaths", IGNORE_PATHS );
        map.put( "append", new Boolean( APPEND_POLICY ) );
        map.put( "extended", new Boolean( EXTENDED_POLICY ) );
        map.put( "preferProxiedForAddress", new Boolean( PROXY_PREFERRED_POLICY ) );
        map.put( "filename", FILENAME );
        map.put( "logDateFormat", LOG_DATE_FORMAT );
        map.put( "logTimeZone", LOG_TIME_ZONE );
        map.put( "retainDays", new Integer( 70 ) );
        map.put( "logLatency", new Boolean( LATENCY_POLICY ) );
        map.put( "logCookies", new Boolean( COOKIE_POLICY ) );
        
        Class<?> clazz = NCSAContext.class;
        NCSAContext context = (NCSAContext) ContextInvocationHandler.getProxiedInstance( clazz, map );
        m_handler = new NCSARequestLogHandler( context );
        m_logger = (NCSARequestLog) m_handler.getRequestLog(); 
    }
    
    
   /**
    * Test ignore-paths assignment integrity.
    * @throws Exception if an error occurs during test execution
    */
    public void testIgnorePaths() throws Exception
    {
        assertEquals( "ignorePaths", IGNORE_PATHS, m_logger.getIgnorePaths() );
    }

   /**
    * Test append policy assignment integrity.
    * @throws Exception if an error occurs during test execution
    */
    public void testAppendPolicy() throws Exception
    {
        assertEquals( "append", APPEND_POLICY, m_logger.isAppend() );
    }
    
   /**
    * Test extended policy assignment integrity.
    * @throws Exception if an error occurs during test execution
    */
    public void testExtendedPolicy() throws Exception
    {
        assertEquals( "extended", EXTENDED_POLICY, m_logger.isExtended() );
    }
    
   /**
    * Test filename assignment integrity.
    * @throws Exception if an error occurs during test execution
    */
    public void testFilename() throws Exception
    {
        assertEquals( "filename", FILENAME, m_logger.getFilename() );
    }
    
   /**
    * Test the log date format assignment integrity.
    * @throws Exception if an error occurs during test execution
    */
    public void testLogDateFormat() throws Exception
    {
        assertEquals( "logDateFormat", LOG_DATE_FORMAT, m_logger.getLogDateFormat() );
    }
    
   /**
    * Test the log time zone assignment integrity.
    * @throws Exception if an error occurs during test execution
    */
    public void testLogTimeZone() throws Exception
    {
        assertEquals( "logTimeZone", LOG_TIME_ZONE, m_logger.getLogTimeZone() );
    }
    
   /**
    * Test the retain days assignment integrity.
    * @throws Exception if an error occurs during test execution
    */
    public void testRetainDays() throws Exception
    {
        assertEquals( "retainDays", RETAIN_DAYS, m_logger.getRetainDays() );
    }
    
   /**
    * Test the latency policy assignment integrity.
    * @throws Exception if an error occurs during test execution
    */
    public void testLatencyPolicy() throws Exception
    {
        assertEquals( "latencyPolicy", LATENCY_POLICY, m_logger.getLogLatency() );
    }
    
   /**
    * Test the cookies policy assignment integrity.
    * @throws Exception if an error occurs during test execution
    */
    public void testLogCookiesPolicy() throws Exception
    {
        assertEquals( "cookiesPolicy", COOKIE_POLICY, m_logger.getLogCookies() );
    }
}

