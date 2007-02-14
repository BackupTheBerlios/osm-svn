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

package net.osm.express.test;

import dpml.util.DefaultLogger;

import java.io.File;
import java.net.URI;

import junit.framework.TestCase;

import org.mortbay.jetty.Server;

import net.dpml.lang.Strategy;
import net.dpml.runtime.Component;
import net.dpml.runtime.Provider;
import net.dpml.util.Logger;

/**
 * Express test case.
 */
public class MinimalTestCase extends TestCase
{
    private Logger m_logger = new DefaultLogger( "test" );
    
    public void testExpress() throws Exception
    {
        URI uri = getPartURI();
        Strategy strategy = Strategy.load( uri );
        Component component = strategy.getInstance( Component.class );
        Provider provider = component.getProvider();
        Server server = provider.getInstance( Server.class );
        component.terminate();
    }
    
    private URI getPartURI() throws Exception
    {
        String path = System.getProperty( "basedir" );
        File file = new File( path );
        File target = new File( file, "target/test/minimal.part" );
        return target.toURI();
    }

    //static
    //{
    //    System.setSecurityManager( new RMISecurityManager() );
    //}
}

