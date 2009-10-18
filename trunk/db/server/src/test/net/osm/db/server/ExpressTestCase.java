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

package net.osm.db.server;

import dpml.util.DefaultLogger;

import java.io.File;
import java.net.URI;

import junit.framework.TestCase;

import net.dpml.lang.Strategy;
import net.dpml.runtime.Component;
import net.dpml.runtime.Provider;
import net.dpml.util.Logger;

/**
 * Express test case.
 */
public class ExpressTestCase extends TestCase
{
    private Logger m_logger = new DefaultLogger( "test" );
    private Component m_component;
    private Server m_server;

    public void setUp() throws Exception
    {
        m_logger.info( "commencing setup" );
        URI uri = getPartURI();
        Strategy strategy = Strategy.load( uri );
        m_component = strategy.getInstance( Component.class );
        Provider provider = m_component.getProvider();
        m_server = provider.getInstance( Server.class );
        m_logger.info( "setup complete" );
    }
    
    public void tearDown() throws Exception
    {
        m_logger.info( "commencing tear-down" );
        try
        {
            m_component.terminate();
            m_logger.info( "tear-down complete" );
         }
        catch( Throwable e )
        {
            m_logger.error( "termination error", e );
        }
    }
    
    public void testServerPing() throws Exception
    {   
        m_logger.info( "executing test" );
        
        int n = 0;
        boolean test = true;
        while ( test == true ) 
        {
            n++;
            try
            {
                m_server.ping();
                test = false;
                m_logger.info( "ping resolved after " + n + " cycles" );
            }
            catch ( Throwable e )
            {
                if( n > 1000 )
                {
                    test = false;
                    m_logger.info( "ping FAILED" );
                    throw new Exception( "Failed to invoke a successful ping." );
                }
            }
        }
    }
    
    private URI getPartURI() throws Exception
    {
        String path = System.getProperty( "basedir" );
        File file = new File( path );
        File target = new File( file, "target/test/test.part" );
        return target.toURI();
    }
}

