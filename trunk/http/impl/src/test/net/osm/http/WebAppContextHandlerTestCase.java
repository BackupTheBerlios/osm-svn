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

import java.io.File;
import java.io.FilePermission;
import java.net.URI;
import java.util.Map;
import java.util.Hashtable;
import java.security.Permission;
import java.security.PermissionCollection;

import net.osm.http.WebAppContextHandler.Context;

import net.dpml.util.ContextInvocationHandler;

import net.dpml.logging.Logger;

import org.mortbay.jetty.handler.ContextHandler;
import org.mortbay.jetty.security.SecurityHandler;
import org.mortbay.jetty.servlet.SessionHandler;
import org.mortbay.jetty.servlet.ServletHandler;

/**
 * WebAppContextHandler test case.
 */
public class WebAppContextHandlerTestCase extends AbstractContextHandlerTestCase
{
    private static final URI WAR_URI = createWarURI();
    private static final File TEMP_DIR = new File( "." );
    private static final Map RESOURCE_ALIASES = new Hashtable();
    private static final boolean EXTRACTION_POLICY = true;
    private static final PermissionCollection PERMISSIONS = createPermissionCollection();
    private static final SecurityHandler SECURITY_HANDLER = new SecurityHandler();
    private static final SessionHandler SESSIONS_HANDLER = new SessionHandler();
    private static final ServletHandler SERVLET_HANDLER = new ServletHandler();
    
    private WebAppContextHandler m_handler;
    
    /**
     * Setup the WebAppContextHandler.
     * @throws Exception if an error occurs during test execution
     */
    public void setUp() throws Exception
    {
        Map map = createMap();
        
        map.put( "war", WAR_URI );
        map.put( "tempDirectory", TEMP_DIR );
        map.put( "resourceAliases", RESOURCE_ALIASES );
        map.put( "extractionPolicy", new Boolean( EXTRACTION_POLICY ) );
        map.put( "permissions", PERMISSIONS );
        map.put( "securityHandler", SECURITY_HANDLER );
        map.put( "sessionHandler", SESSIONS_HANDLER );
        map.put( "servletHandler", SERVLET_HANDLER );
        
        Class clazz = Context.class;
        Context context = (Context) ContextInvocationHandler.getProxiedInstance( clazz, map );
        Logger logger = new StandardLogger( "test" );
        m_handler = new WebAppContextHandler( logger, context );
    }
    
   /**
    * Return the context handler under evaluation.
    * @return the context handler
    */
    protected ContextHandler getContextHandler()
    {
        return m_handler;
    }
    
   /**
    * Test war path assignment integrity.
    * @throws Exception if an error occurs during test execution
    */
    public void testWar() throws Exception
    {
        assertEquals( "war", WAR_URI.toASCIIString(), m_handler.getWar() );
    }

   /**
    * Test temp dir assignment integrity.
    * @throws Exception if an error occurs during test execution
    */
    public void testTempDirectory() throws Exception
    {
        assertEquals( "tempDir", TEMP_DIR.getCanonicalFile(), m_handler.getTempDirectory() );
    }

   /**
    * Test resource aliases assignment integrity.
    * @throws Exception if an error occurs during test execution
    */
    public void testResourceAliases() throws Exception
    {
        assertEquals( "resourceAliases", RESOURCE_ALIASES, m_handler.getResourceAliases() );
    }

   /**
    * Test permissions assignment integrity.
    * @throws Exception if an error occurs during test execution
    */
    public void testPermissions() throws Exception
    {
        assertEquals( "permissions", PERMISSIONS, m_handler.getPermissions() );
    }

   /**
    * Test security handler assignment integrity.
    * @throws Exception if an error occurs during test execution
    */
    public void testSecurityHandler() throws Exception
    {
        assertEquals( "securityHandler", SECURITY_HANDLER, m_handler.getSecurityHandler() );
    }

   /**
    * Test session handler assignment integrity.
    * @throws Exception if an error occurs during test execution
    */
    public void testSessionHandler() throws Exception
    {
        assertEquals( "sessionHandler", SESSIONS_HANDLER, m_handler.getSessionHandler() );
    }

   /**
    * Test servlet handler assignment integrity.
    * @throws Exception if an error occurs during test execution
    */
    public void testServletHandler() throws Exception
    {
        assertEquals( "servletHandler", SERVLET_HANDLER, m_handler.getServletHandler() );
    }

    private static URI createWarURI()
    {
        try
        {
            return new URI( "artifact:war:acme/demo" );
        }
        catch( Throwable e )
        {
            return null;
        }
    }

    private static PermissionCollection createPermissionCollection()
    {
        Permission permission = new FilePermission( ".", "read" );
        return permission.newPermissionCollection();
    }
}

