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
import java.util.Hashtable;

import net.osm.http.spi.ThreadContext;
import net.osm.http.impl.BoundedThreadPool;

import dpml.lang.ContextInvocationHandler;

import junit.framework.TestCase;

/**
 * BoundedThreadPool Test.
 */
public class BoundedThreadPoolTestCase extends TestCase
{
    private static final int MIN_THREADS = 10;
    private static final int MAX_THREADS = 100;
    private static final String NAME = "test";
    private static final int PRIORITY = 5;
    private static final int IDLE = 1000;
    
    private BoundedThreadPool m_pool;
    
    /**
     * Test context value handling integrity.
     * @throws Exception if an error occurs during test execution
     */
    public void setUp() throws Exception
    {
        Map map = new Hashtable();
        map.put( "min", new Integer( MIN_THREADS ) );
        map.put( "max", new Integer( MAX_THREADS ) );
        map.put( "daemon", new Boolean( true ) );
        map.put( "name", NAME );
        map.put( "priority", new Integer( PRIORITY ) );
        map.put( "idle", new Integer( IDLE ) );
        Class clazz = ThreadContext.class;
        ThreadContext context = (ThreadContext) ContextInvocationHandler.getProxiedInstance( clazz, map );
        m_pool = new BoundedThreadPool( context );
    }
    
    
   /**
    * Test min-thread assignment integrity.
    * @throws Exception if an error occurs during test execution
    */
    public void testMinimumThreads() throws Exception
    {
        assertEquals( "min", MIN_THREADS, m_pool.getMinThreads() );
    }

   /**
    * Test max-thread assignment integrity.
    * @throws Exception if an error occurs during test execution
    */
    public void testMaximumThreads() throws Exception
    {
        assertEquals( "max", MAX_THREADS, m_pool.getMaxThreads() );
    }

   /**
    * Test daemon policy assignment integrity.
    * @throws Exception if an error occurs during test execution
    */
    public void testDaemon() throws Exception
    {
        assertTrue( "daemon", m_pool.isDaemon() );
    }

   /**
    * Test name assignment integrity.
    * @throws Exception if an error occurs during test execution
    */
    public void testName() throws Exception
    {
        assertEquals( "name", NAME, m_pool.getName() );
    }

   /**
    * Test priority assignment integrity.
    * @throws Exception if an error occurs during test execution
    */
    public void testPriority() throws Exception
    {
        assertEquals( "priority", PRIORITY, m_pool.getThreadsPriority() );
    }

   /**
    * Test idle assignment integrity.
    * @throws Exception if an error occurs during test execution
    */
    public void testIdle() throws Exception
    {
        assertEquals( "idle", IDLE, m_pool.getMaxIdleTimeMs() );
    }
}

