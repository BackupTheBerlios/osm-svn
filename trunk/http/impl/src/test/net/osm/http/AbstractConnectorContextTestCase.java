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

import java.util.Map;
import java.util.Hashtable;

import org.mortbay.jetty.AbstractConnector;

import junit.framework.TestCase;

/**
 * NCSARequestLog test case.
 */
public abstract class AbstractConnectorContextTestCase extends TestCase
{
    private static final String HOST = "localhost";
    private static final int PORT = 1234;
    private static final int HEADER_BUFFER_SIZE = 64;
    private static final int MAX_IDLE_TIME = 32;
    private static final int REQUEST_BUFFER_SIZE = 32;
    private static final int RESPONSE_BUFFER_SIZE = 33;
    private static final int ACCEPT_QUEUE_SIZE = 18;
    private static final int INITIAL_ACCEPTORS_SIZE = 18;
    private static final int SO_LINGER_TIME = 130;
    private static final int CONFIDENTIAL_PORT = 1234;
    private static final String CONFIDENTIAL_SCHEME = "http"; // normally https
    private static final int INTEGRAL_PORT = 1289;
    private static final String INTEGRAL_SCHEME = "http"; // normally https
    
    /**
     * Setup the context map.
     * @return the context map
     * @throws Exception if an error occurs
     */
    protected Map createMap() throws Exception
    {
        Map map = new Hashtable();
        map.put( "host", HOST );
        map.put( "port", new Integer( PORT ) );
        map.put( "maxIdleTime", new Integer( MAX_IDLE_TIME ) );
        map.put( "headerBufferSize", new Integer( HEADER_BUFFER_SIZE ) );
        map.put( "requestBufferSize", new Integer( REQUEST_BUFFER_SIZE ) );
        map.put( "responseBufferSize", new Integer( RESPONSE_BUFFER_SIZE ) );
        map.put( "acceptQueueSize", new Integer( ACCEPT_QUEUE_SIZE ) );
        map.put( "acceptors", new Integer( INITIAL_ACCEPTORS_SIZE ) );
        map.put( "soLingerTime", new Integer( SO_LINGER_TIME ) );
        map.put( "confidentialPort", new Integer( CONFIDENTIAL_PORT ) );
        map.put( "confidentialScheme", CONFIDENTIAL_SCHEME );
        map.put( "integralPort", new Integer( INTEGRAL_PORT ) );
        map.put( "integralScheme", INTEGRAL_SCHEME );
        return map;
    }

   /**
    * Return the connector under test.
    * @return the connector
    */
    protected abstract AbstractConnector getConnector();

   /**
    * Test hosts assignment integrity.
    * @throws Exception if an error occurs during test execution
    */
    public void testHost() throws Exception
    {
        assertEquals( "host", HOST, getConnector().getHost() );
    }
    
   /**
    * Test port assignment integrity.
    * @throws Exception if an error occurs during test execution
    */
    public void testPort() throws Exception
    {
        assertEquals( "port", PORT, getConnector().getPort() );
    }
    
   /**
    * Test hewder buffer size assignment integrity.
    * @throws Exception if an error occurs during test execution
    */
    public void testMaxIdleTime() throws Exception
    {
        assertEquals( "maxIdleTime", MAX_IDLE_TIME, getConnector().getMaxIdleTime() );
    }
    
   /**
    * Test header buffer size assignment integrity.
    * @throws Exception if an error occurs during test execution
    */
    public void testHeaderBufferSize() throws Exception
    {
        assertEquals( "headerBufferSize", HEADER_BUFFER_SIZE, getConnector().getHeaderBufferSize() );
    }
    
   /**
    * Test request buffer size assignment integrity.
    * @throws Exception if an error occurs during test execution
    */
    public void testRequestBufferSize() throws Exception
    {
        assertEquals( "requestBufferSize", REQUEST_BUFFER_SIZE, getConnector().getRequestBufferSize() );
    }
    
   /**
    * Test responce buffer size assignment integrity.
    * @throws Exception if an error occurs during test execution
    */
    public void testResponseBufferSize() throws Exception
    {
        assertEquals( "responseBufferSize", RESPONSE_BUFFER_SIZE, getConnector().getResponseBufferSize() );
    }
    
   /**
    * Test accept queue size assignment integrity.
    * @throws Exception if an error occurs during test execution
    */
    public void testAcceptQueueSize() throws Exception
    {
        assertEquals( "acceptQueueSize", ACCEPT_QUEUE_SIZE, getConnector().getAcceptQueueSize() );
    }
    
   /**
    * Test initial accept size assignment integrity.
    * @throws Exception if an error occurs during test execution
    */
    public void testAcceptors() throws Exception
    {
        assertEquals( "acceptors", INITIAL_ACCEPTORS_SIZE, getConnector().getAcceptors() );
    }
    
   /**
    * Test so-lingert-time assignment integrity.
    * @throws Exception if an error occurs during test execution
    */
    public void testSoLingerTime() throws Exception
    {
        assertEquals( "soLingerTime", SO_LINGER_TIME, getConnector().getSoLingerTime() );
    }
    
   /**
    * Test confidential port assignment integrity.
    * @throws Exception if an error occurs during test execution
    */
    public void testConfidentialPort() throws Exception
    {
        assertEquals( "confidentialPort", CONFIDENTIAL_PORT, getConnector().getConfidentialPort() );
    }
    
   /**
    * Test confidential scheme assignment integrity.
    * @throws Exception if an error occurs during test execution
    */
    public void testConfidentialScheme() throws Exception
    {
        assertEquals( "confidentialScheme", CONFIDENTIAL_SCHEME, getConnector().getConfidentialScheme() );
    }

   /**
    * Test integral port assignment integrity.
    * @throws Exception if an error occurs during test execution
    */
    public void testIntegralPort() throws Exception
    {
        assertEquals( "integralPort", INTEGRAL_PORT, getConnector().getIntegralPort() );
    }
    
   /**
    * Test integal scheme assignment integrity.
    * @throws Exception if an error occurs during test execution
    */
    public void testIntegralScheme() throws Exception
    {
        assertEquals( "integralScheme", INTEGRAL_SCHEME, getConnector().getIntegralScheme() );
    }
}

