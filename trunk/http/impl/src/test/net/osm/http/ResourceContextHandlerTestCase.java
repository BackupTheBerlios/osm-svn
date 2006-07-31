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
import java.util.Map;

import net.osm.http.ResourceContextHandler.Context;

import net.dpml.util.ContextInvocationHandler;

import net.dpml.logging.Logger;

import org.mortbay.jetty.handler.ContextHandler;

/**
 * NCSARequestLog test case.
 */
public class ResourceContextHandlerTestCase extends AbstractContextHandlerTestCase
{
    private static final String RESOURCE_BASE = ".";
    
    private ResourceContextHandler m_handler;
    
    /**
     * Setup the ResourceContextHandler.
     * @throws Exception if an error occurs during test execution
     */
    public void setUp() throws Exception
    {
        Map map = createMap();
        
        map.put( "resourceBase", RESOURCE_BASE );
        
        Class clazz = Context.class;
        Context context = (Context) ContextInvocationHandler.getProxiedInstance( clazz, map );
        Logger logger = new StandardLogger( "test" );
        m_handler = new ResourceContextHandler( logger, context );
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
    * Test resource base assignment integrity.
    * @throws Exception if an error occurs during test execution
    */
    public void testResourceBase() throws Exception
    {
        File file = new File( RESOURCE_BASE );
        String base = file.getCanonicalFile().toURI().toString();
        assertEquals( "resourceBase", base, m_handler.getResourceBase() );
    }

}

