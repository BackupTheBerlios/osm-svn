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

import java.io.File;
import java.net.URI;
import java.util.Map;

import net.osm.http.spi.HttpsConnectionContext;

import dpml.lang.ContextInvocationHandler;

import org.mortbay.jetty.AbstractConnector;

/**
 * SslSocketConnectorTestCase.
 */
public class SslSocketConnectorTestCase extends AbstractConnectorContextTestCase
{
    //private static final String[] CIPHER_SUITES = new String[0];
    private static final String ALGORITHM = "QWERTY";
    private static final String PROTOCOL = "XYZ";
    private static final URI KEYSTORE_URI = createKeystoreURI();
    private static final String KEYSTORE_TYPE = "ABC";
    private static final boolean WANT_CLIENT_AUTH = true;
    private static final boolean NEED_CLIENT_AUTH = true;
    private static final String PROVIDER = "ACME";
    private static final String TRUST_ALGORITHM = "QWERTY";
    
    private SslSocketConnector m_connector;
    
    /**
     * Setup the SslSocketConnector instance.
     * @throws Exception if an error occurs
     */
    public void setUp() throws Exception
    {
        Map map = createMap();
        
        //map.put( "cipherSuites", CIPHER_SUITES );
        map.put( "algorithm", ALGORITHM );
        map.put( "protocol", PROTOCOL );
        map.put( "keyStore", KEYSTORE_URI );
        map.put( "keyStoreType", KEYSTORE_TYPE );
        map.put( "wantClientAuth", new Boolean( WANT_CLIENT_AUTH ) );
        map.put( "needClientAuth", new Boolean( NEED_CLIENT_AUTH ) );
        map.put( "provider", PROVIDER );
        map.put( "trustAlgorithm", TRUST_ALGORITHM );
        
        Class clazz = HttpsConnectionContext.class;
        HttpsConnectionContext context = 
          (HttpsConnectionContext) ContextInvocationHandler.getProxiedInstance( clazz, map );
        m_connector = new SslSocketConnector( context );
    }
    
   /**
    * Return the connector under test.
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
    //public void testCipherSuites() throws Exception
    //{
    //    assertEquals( "cipherSuites", CIPHER_SUITES, m_connector.getCipherSuites() );
    //}

   /**
    * Test algorithm assignment integrity.
    * @throws Exception if an error occurs during test execution
    */
    public void testAlgorith() throws Exception
    {
        assertEquals( "algorith", ALGORITHM, m_connector.getSecureRandomAlgorithm() );
    }
    
   /**
    * Test protocol assignment integrity.
    * @throws Exception if an error occurs during test execution
    */
    public void testProtocol() throws Exception
    {
        assertEquals( "protocol", PROTOCOL, m_connector.getProtocol() );
    }
    
   /**
    * Test keystore path assignment integrity.
    * @throws Exception if an error occurs during test execution
    */
    public void testKeystorePath() throws Exception
    {
        assertEquals( "keystore", KEYSTORE_URI.toASCIIString(), m_connector.getKeystore() );
    }
    
   /**
    * Test keystore type assignment integrity.
    * @throws Exception if an error occurs during test execution
    */
    public void testKeystoreType() throws Exception
    {
        assertEquals( "keystoreType", KEYSTORE_TYPE, m_connector.getKeystoreType() );
    }
    
   /**
    * Test want-client-auth policy assignment integrity.
    * @throws Exception if an error occurs during test execution
    */
    public void testWantClientAuthentication() throws Exception
    {
        assertEquals( "wantClientAuth", WANT_CLIENT_AUTH, m_connector.getWantClientAuth() );
    }
    
   /**
    * Test need-client-auth policy assignment integrity.
    * @throws Exception if an error occurs during test execution
    */
    public void testNeedClientAuthentication() throws Exception
    {
        assertEquals( "needClientAuth", NEED_CLIENT_AUTH, m_connector.getNeedClientAuth() );
    }
    
   /**
    * Test provider assignment integrity.
    * @throws Exception if an error occurs during test execution
    */
    public void testProvider() throws Exception
    {
        assertEquals( "provider", PROVIDER, m_connector.getProvider() );
    }

    private static URI createKeystoreURI()
    {
        try
        {
            return new File( ".keystore" ).toURI();
        }
        catch( Throwable e )
        {
            return null;
        }
    }
}

