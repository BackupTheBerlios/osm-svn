/*
 * Copyright 2007 Stephen J. McConnell
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

package org.acme.api;

import dpml.util.DefaultLogger;

import java.io.File;
import java.net.URI;

import junit.framework.TestCase;

import net.dpml.util.Logger;
import net.dpml.lang.Strategy;
import net.dpml.runtime.Component;

/**
 * Demonstration of application managed persistence.
 */
public class CustomerCatalogTestCase extends TestCase
{
    private Logger m_logger;
    private Component m_component;
    
   /**
    * Setup the testcase during which we establish the entity manager to 
    * be used throught the test process.
    */
    public void setUp() throws Exception
    {
        m_logger = new DefaultLogger( "test" );
        Strategy strategy = Strategy.load( getPartURI() );
        m_component = strategy.getInstance( Component.class );
    }
    
    public void testCustomerCatalog() throws Exception
    {
        Catalog catalog = m_component.getProvider().getInstance( Catalog.class );
        catalog.create( "Charles Dickens" );
        catalog.create( "Agatha Christie" );
        int n = listCustomers( catalog, true );
        assertEquals( "count", 2, n );
    }
    
    public void tearDown() throws Exception
    {
        m_component.terminate();
    }
    
    private int listCustomers( Catalog catalog, boolean verbose)
    {
        int n = 0;
        Iterable<Customer> customers = catalog.getCustomers();
        for( Customer customer : customers )
        {
            if( verbose )
            {
                m_logger.info( "  " + customer.getName() + ", " + customer.getID() );
            }
            n++;
        }
        return n;
    }
    
    private URI getPartURI() throws Exception
    {
        String path = System.getProperty( "project.test.dir" );
        File test = new File( path );
        File file = new File( test, "test.part" );
        return file.toURI();
    }
}
