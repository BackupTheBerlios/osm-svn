/*
 * Copyright 2004-2005 Stephen J. McConnell.
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

package net.osm.tutorial;

import java.io.InputStream;
import java.util.Properties;
import java.net.URL;

import junit.framework.TestCase;

/**
 * Example of loading a property file using the artifact protocol with 
 * the protocol handler setup as part of the jvm.
 */
public class TransitTestCase extends TestCase
{
    protected void setUp() throws Exception
    {
        System.setProperty( "java.protocol.handler.pkgs", "net.dpml.transit" );
    }

    public void testPropertyLoading() throws Exception
    {
        URL url = new URL( "artifact:spec:dpml/test/example-property-file#2.2" );
        InputStream input = url.openStream();
        Properties properties = new Properties();
        properties.load( input );
        String message = properties.getProperty( "tutorial.message" );
        System.out.println( message );
    }
}

