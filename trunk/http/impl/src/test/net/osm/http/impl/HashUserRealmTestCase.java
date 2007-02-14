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
import java.util.Hashtable;

import net.osm.http.impl.HashUserRealm.RealmContext;

import dpml.lang.ContextInvocationHandler;

import junit.framework.TestCase;

/**
 * BoundedThreadPool Test.
 */
public class HashUserRealmTestCase extends TestCase
{
    private HashUserRealm m_realm;
    private String m_config;
    
    /**
     * Test context value handling integrity.
     * @throws Exception if an error occurs during test execution
     */
    public void setUp() throws Exception
    {
        Map map = new Hashtable();
        map.put( "name", "test" );
        File test = new File( System.getProperty( "project.test.dir" ) );
        File properties = new File( test, "realm.properties" );
        URI uri = properties.toURI();
        m_config = uri.toASCIIString();
        map.put( "URI", uri );
        Class clazz = RealmContext.class;
        RealmContext context = (RealmContext) ContextInvocationHandler.getProxiedInstance( clazz, map );
        m_realm = new HashUserRealm( context );
    }
    
   /**
    * Test name assignment integrity.
    * @throws Exception if an error occurs during test execution
    */
    public void testName() throws Exception
    {
        assertEquals( "name", "test", m_realm.getName() );
    }

   /**
    * Test priority assignment integrity.
    * @throws Exception if an error occurs during test execution
    */
    public void testConfig() throws Exception
    {
        assertEquals( "config", m_config, m_realm.getConfig() );
    }
}

