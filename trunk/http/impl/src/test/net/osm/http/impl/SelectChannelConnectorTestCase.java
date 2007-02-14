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

import java.util.Map;

import net.osm.http.spi.HttpConnectionContext;

import dpml.lang.ContextInvocationHandler;

import org.mortbay.jetty.AbstractConnector;

/**
 * SelectChannelConnectorTestCase.
 */
public class SelectChannelConnectorTestCase extends AbstractConnectorContextTestCase
{
    private static final boolean ASSUME_SHORT_DISPATH_POLICY = true;
    
    private SelectChannelConnector m_connector;
    
    /**
     * Test context value handling integrity.
     * @throws Exception if an error occurs during test execution
     */
    public void setUp() throws Exception
    {
        Map map = createMap();
        
        map.put( "delaySelectKeyUpdate", new Boolean( ASSUME_SHORT_DISPATH_POLICY ) );
        
        Class clazz = HttpConnectionContext.class;
        HttpConnectionContext context = 
          (HttpConnectionContext) ContextInvocationHandler.getProxiedInstance( clazz, map );
        m_connector = new SelectChannelConnector( context );
    }
    
   /**
    * Return the connector under evaluation.
    * @return the connector
    */
    protected AbstractConnector getConnector()
    {
        return m_connector;
    }
    
   /**
    * Test min-thread assignment integrity.
    * @throws Exception if an error occurs during test execution
    */
    public void testAssumeShortDispatch() throws Exception
    {
        assertEquals( "delaySelectKeyUpdate", ASSUME_SHORT_DISPATH_POLICY, m_connector.getDelaySelectKeyUpdate() );
    }

}

