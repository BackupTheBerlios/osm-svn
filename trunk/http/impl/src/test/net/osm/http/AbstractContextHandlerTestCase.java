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

package net.osm.http;

import java.util.Arrays;
import java.util.Map;
import java.util.Hashtable;

import org.mortbay.jetty.handler.ErrorHandler;
import org.mortbay.jetty.handler.ContextHandler;

import junit.framework.TestCase;

/**
 * NCSARequestLog test case.
 */
public abstract class AbstractContextHandlerTestCase extends TestCase
{
    private static final String[] VIRTUAL_HOSTS = new String[]{"virtual.acme.org", "virtual.secret.acme.org"};
    private static final String[] CONNECTORS = new String[]{"www.acme.org", "secret.acme.org"};
    private static final String[] WELCOME_FILES = new String[]{"about.html", "index.html", "index.htm"};
    private static final ClassLoader CLASSLOADER = AbstractContextHandlerTestCase.class.getClassLoader();
    private static final Map MIME_TYPES = new Hashtable();
    private static final String CONTEXT_PATH = "acme";
    private static final String DISPLAY_NAME = "testing";
    private static final ErrorHandler ERROR_HANDLER = new ErrorHandler();
    
   /**
    * Setup the context map.
    * @return the context map
    * @throws Exception if an error occurs
    */
    protected Map createMap() throws Exception
    {
        Map map = new Hashtable();
        map.put( "virtualHosts", VIRTUAL_HOSTS );
        map.put( "connectors", CONNECTORS );
        map.put( "welcomeFiles", WELCOME_FILES );
        map.put( "classLoader", CLASSLOADER );
        map.put( "contextPath", CONTEXT_PATH );
        map.put( "displayName", DISPLAY_NAME );
        map.put( "mimeTypes", MIME_TYPES );
        map.put( "errorHandler", ERROR_HANDLER );
        return map;
    }

   /**
    * Return the test context handler.
    * @return the context handler
    */
    protected abstract ContextHandler getContextHandler();

   /**
    * Test virtual hosts assignment integrity.
    * @throws Exception if an error occurs during test execution
    */
    public void testVirtualHosts() throws Exception
    {
        String[] hosts = getContextHandler().getVirtualHosts();
        if( !Arrays.equals( VIRTUAL_HOSTS, hosts ) )
        {
            final String error =
              "Supplied virtual hosts array does not equal return value.";
            fail( error );
        }
    }

   /**
    * Test hosts assignment integrity.
    * @throws Exception if an error occurs during test execution
    */
    public void testConnectors() throws Exception
    {
        String[] connectors = getContextHandler().getConnectors();
        if( !Arrays.equals( CONNECTORS, connectors ) )
        {
            final String error =
              "Supplied connector array does not equal return value.";
            fail( error );
        }
    }
    
   /**
    * Test welcome files assignment integrity.
    * @throws Exception if an error occurs during test execution
    */
    public void testWelcomeFiles() throws Exception
    {
        String[] files = getContextHandler().getWelcomeFiles();
        if( !Arrays.equals( WELCOME_FILES, files ) )
        {
            final String error =
              "Supplied welcome files array does not equal return value.";
            fail( error );
        }
    }
    
   /**
    * Test classloader assignment integrity.
    * @throws Exception if an error occurs during test execution
    */
    public void testClassLoader() throws Exception
    {
        assertEquals( "classloader", CLASSLOADER, getContextHandler().getClassLoader() );
    }
    
   /**
    * Test context path assignment integrity.
    * @throws Exception if an error occurs during test execution
    */
    public void testContextPath() throws Exception
    {
        assertEquals( "contextPath", CONTEXT_PATH, getContextHandler().getContextPath() );
    }
    
   /**
    * Test display name assignment integrity.
    * @throws Exception if an error occurs during test execution
    */
    public void testDisplayName() throws Exception
    {
        assertEquals( "displayName", DISPLAY_NAME, getContextHandler().getDisplayName() );
    }
    
   /**
    * Test error handler assignment integrity.
    * @throws Exception if an error occurs during test execution
    */
    public void testErrorHandler() throws Exception
    {
        
        assertEquals( "errorHandler", ERROR_HANDLER, getContextHandler().getErrorHandler() );
    }
}

